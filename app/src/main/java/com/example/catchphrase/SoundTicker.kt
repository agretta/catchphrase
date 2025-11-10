package com.example.catchphrase

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import androidx.annotation.RawRes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.yield
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.random.Random

class SoundTicker(
    context: Context,
    @RawRes private val tickRes: Int,
    @RawRes private val buzzerRes: Int
) {
    private val TAG = "SoundTicker"
    private val appContext = context.applicationContext

    private val attrs = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(2)
        .setAudioAttributes(attrs)
        .build()

    // events exposed to callers so they can react (restart timers, update UI, etc.)
    sealed class Event {
        object BuzzerStarted : Event()
        object Buzz : Event()
        object BuzzerEnded : Event()
        object Tick : Event()
    }

    private val _events = MutableSharedFlow<Event>(extraBufferCapacity = 8)
    val events: SharedFlow<Event> = _events

    private val _isBeeping = MutableStateFlow(false)
    val isBeeping: StateFlow<Boolean> = _isBeeping

    private val _isDone = MutableStateFlow(true)
    val isDone: StateFlow<Boolean> = _isDone

    private var tickId: Int = 0
    private var buzzerId: Int = 0
    private val tickLoaded = AtomicBoolean(false)
    private val buzzerLoaded = AtomicBoolean(false)
    private var job: Job? = null

    init {
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status != 0) {
                Log.w(TAG, "sound load failed id=$sampleId status=$status")
                return@setOnLoadCompleteListener
            }
            if (sampleId == tickId) tickLoaded.set(true)
            if (sampleId == buzzerId) buzzerLoaded.set(true)
            Log.d(TAG, "sound loaded id=$sampleId tickId=$tickId buzzerId=$buzzerId")
        }
        try {
            tickId = soundPool.load(appContext, tickRes, 1)
            buzzerId = soundPool.load(appContext, buzzerRes, 1)
            Log.d(TAG, "requested load tickId=$tickId buzzerId=$buzzerId")
        } catch (e: Exception) {
            Log.e(TAG, "failed to load sounds", e)
        }
    }

    private suspend fun waitForLoaded(flag: AtomicBoolean, timeoutMs: Long = 1000L): Boolean {
        val ok = withTimeoutOrNull(timeoutMs) {
            while (!flag.get()) {
                delay(10)
            }
            true
        }
        return ok == true
    }

    private suspend fun waitForAllLoaded(timeoutMsPerSound: Long = 1000L): Boolean {
        val tickOk = waitForLoaded(tickLoaded, timeoutMsPerSound)
        val buzzerOk = waitForLoaded(buzzerLoaded, timeoutMsPerSound)
        return tickOk && buzzerOk
    }

    private val slowIntervalMs: Long = 2000L
    private val fastIntervalMs: Long = 300L

    /**
     * Start beeping with a single ramp: interval linearly decreases from 2000ms to 300ms
     * over the provided roundLengthMs. If randomizeLength is true, the round length is
     * multiplied by a random factor in \[0.75, 1.25\].
     *
     * @param scope coroutine scope for the ticker
     * @param roundLengthMs total duration of the ramping tick phase (before buzzer)
     * @param randomizeLength whether to randomize the round length by ±25%
     * @param tickVolume tick playback volume
     * @param buzzerVolume buzzer playback volume
     */
    fun start(
        scope: CoroutineScope,
        roundLengthMs: Long = 60000L,
        randomizeLength: Boolean = false,
        tickVolume: Float = 1.0f,
        buzzerVolume: Float = 1.0f
    ) {
        _isDone.value = false
        job = scope.launch(Dispatchers.Main) {
            // Wait for loads up-front to avoid race conditions
            if (!waitForAllLoaded(1500L)) {
                Log.w(TAG, "not all sounds loaded in time; will attempt playback anyway")
            } else {
                Log.d(TAG, "all sounds loaded, starting ticker")
            }

            // compute effective round length with optional randomization ±25%
            val effectiveRoundLength = if (randomizeLength && roundLengthMs > 0L) {
                val factor = Random.nextDouble(0.75, 1.25)
                (roundLengthMs * factor).toLong().coerceAtLeast(0L)
            } else {
                roundLengthMs.coerceAtLeast(0L)
            }
            Log.d(TAG, "roundLengthMs=$roundLengthMs randomize=$randomizeLength effective=$effectiveRoundLength")

            _isBeeping.value = true
            if (effectiveRoundLength > 0L) {
                val startTime = System.currentTimeMillis()
                while (isActive && System.currentTimeMillis() - startTime < effectiveRoundLength) {
                    // compute progress 0.0 .. 1.0
                    val elapsed = (System.currentTimeMillis() - startTime).coerceAtLeast(0L)
                    val progress = (elapsed.toDouble() / effectiveRoundLength.toDouble()).coerceIn(0.0, 1.0)

                    // linear interpolate interval: slow -> fast as progress goes 0 -> 1
                    val intervalMsDouble = slowIntervalMs * (1.0 - progress) + fastIntervalMs * progress
                    val intervalMs = intervalMsDouble.toLong().coerceAtLeast(100L)

                    // ensure tick is loaded before each play
                    if (!waitForLoaded(tickLoaded, 500L)) {
                        Log.w(TAG, "tick not loaded in time")
                    } else {
                        val streamId = soundPool.play(tickId, tickVolume, tickVolume, 1, 0, 1f)
                        if (streamId == 0) Log.w(TAG, "tick play returned 0")
                        Log.d(TAG, "tick played streamId=$streamId intervalMs=$intervalMs progress=$progress")
                        _events.tryEmit(Event.Tick)
                    }

                    // wait for the computed interval (respect cancellation)
                    if (intervalMs > 0L) delay(intervalMs) else yield()
                }
            } else {
                Log.d(TAG, "effectiveRoundLength is 0; skipping tick phase")
            }

            if (isActive) {
                val times = 3

                _isBeeping.value = false
                _events.tryEmit(Event.BuzzerStarted)
                repeat(times) { i ->
                    val streamId = soundPool.play(buzzerId, buzzerVolume, buzzerVolume, 1, 0, 1f)
                    Log.d(TAG, "buzzer played attempt=${i + 1} streamId=$streamId")
                    _events.tryEmit(Event.Buzz)
                    delay(700L)
                }
                _events.tryEmit(Event.BuzzerEnded)
                _isDone.value = true
            }
        }
    }

    fun stop() {
        _isDone.value = true
        job?.cancel()
        job = null
        try { soundPool.autoPause() } catch (e: Exception) { Log.w(TAG, "autoPause fail", e) }
    }

    fun release() {
        stop()
        try { soundPool.release() } catch (e: Exception) { /* ignore */ }
    }
}
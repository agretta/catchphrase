import 'dart:async';
//import 'package:audioplayers/audio_cache.dart';
import 'package:audioplayers/audioplayers.dart';
import 'dart:developer' as dev;

class Countdown {
  Duration totalDuration = const Duration(seconds: 6);
  Duration beepDuration = const Duration(seconds: 2);
  bool _isRunning = false;
  bool _playAudio = true;

  StreamController<bool> timeIsUp = StreamController<bool>();

  late Timer totalTimer;
  late Timer beepTimer;

  AudioPlayer audioPlayer = AudioPlayer();
  AudioPlayer audioPlayer2 = AudioPlayer();

  Countdown();

  void beep() {
    // dev.log("beep");
    if (_playAudio) {
      AssetSource bp = AssetSource("audio/beep.mp3");
      audioPlayer.play(bp);
    }
  }

  void buzzer() {
    // dev.log("buzzer");
    if (_playAudio) {
      AssetSource buzzer = AssetSource("audio/buzzer.mp3");
      audioPlayer2.play(buzzer);
    }
  }

  void countdownInterval(Duration beepDuration, int iterations) {
    beepTimer = Timer.periodic(beepDuration, (Timer timer) {
      beep();
    });

    totalTimer = Timer(totalDuration, () {
      // stop the beeper, recursively call self with iterations-1
      dev.log("iteration: $iterations");
      beepTimer.cancel();
      if (iterations <= 0 || !_isRunning) {
        cancel();
        buzzer();
        timeIsUp.add(true);
        return;
      }
      int tick = beepDuration.inSeconds ~/ 2;
      countdownInterval(Duration(seconds: tick), iterations - 1);
    });
  }

  void start() {
    dev.log("starting countdown");
    _isRunning = true;
    countdownInterval(beepDuration, 3);
  }

  void cancel() {
    dev.log("stopping countdown");
    _isRunning = false;
    totalTimer.cancel();
    beepTimer.cancel();
  }
}

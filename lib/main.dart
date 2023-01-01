// main.dart
import 'package:flutter/material.dart';
import 'package:flutter/services.dart' show rootBundle;
import 'countdown.dart';

import 'package:csv/csv.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return const MaterialApp(
      // Hide the debug banner
        debugShowCheckedModeBanner: false,
        title: 'Catchphrase',
        home: HomeScreen());
  }
}

class HomeScreen extends StatefulWidget {
  const HomeScreen({Key? key}) : super(key: key);

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}


class _HomeScreenState extends State<HomeScreen> {
  bool _isOver = false;
  String _phrase = "";
  Countdown countdown = Countdown();
  List<String> _phrases = [];

  List<List<dynamic>> rowsAsListOfValues = const CsvToListConverter().convert("assets/csv/name.csv");

/*
  Future<String> loadAsset() async {
    return await rootBundle.loadString('assets/phrases/test_phrases.csv');
  }
*/

  late Iterator<String> _phraseIterator = _phrases.iterator;

  @override
  void initState() {
    // Setup the first word
    _phrases = ["One", "Two", "Three", "Four", "Five", "Six", "Seven" ];
    _phraseIterator.moveNext();
    _phrase = _phraseIterator.current;

    countdown.start();

    // what to do when the countdown is over
    countdown.timeIsUp.stream.listen((value) {
      print('Value from controller: $value');
      _isOver = true;
      setState(() {
        _phrase = "TIME'S UP";
      });
    });
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Catchphrase'),
      ),
     body: GestureDetector(
       onTap: (){
         setState(() {
           nextPhrase();
         });
         if (_isOver) {
           countdown.start();
           _isOver = false;
         }
       },
       child: Container(
         color: Colors.grey,
         child: Center(
           child: Text(
              _phrase,
             textAlign: TextAlign.center,
             overflow: TextOverflow.ellipsis,
             style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 60),
           ),
         ),
       ),
     ),
      // This button will stop the timer
      /*floatingActionButton: FloatingActionButton(
        onPressed: () {
          setState(() {
            _isRunning = false;
          });
        },
        child: const Icon(Icons.stop_circle),
      ),*/
    );
  }

  void nextPhrase() {
    print("Next Phrase");
    if(_phraseIterator.moveNext()) {
      _phrase = _phraseIterator.current;
      print(_phrase);
    } else {
      _phraseIterator = _phrases.iterator;
      _phraseIterator.moveNext();
      _phrase = _phraseIterator.current;
      print(_phrase);
    }
  }
}

# Performance Analysis of Flow and RxJava
This benchmark relies on the famous "Shakespeare plays Scrabble"-Benchmark used to measure the overhead of various libraries. It relies heavily on David Karnok's implementation and has been ported to several other libraries like Kotlin Flow, Kotlin Sequences, Java Streams and so on.

#### Results
Benchmark results for throughput mode, Java ```11.0.7``` running on ```Intel(R) Core(TM) i5-7267U CPU @ 3.1 GHz``` under ```macOS 10.15.6 Darwin Kernel Version 19.6.0```.
```
Benchmark                                   Mode  Cnt    Score    Error  Units`
b.flow.scrabble.FlowPlaysScrabbleBase.play  avgt    7  142.084 ±  5.571  ms/op
b.rx.scrabble.RxJava2PlaysScrabble.play     avgt    7  156.307 ± 15.836  ms/op
```

#### Sources
1. [Kotlin Flow implementation](https://github.com/Kotlin/kotlinx.coroutines/blob/master/benchmarks/src/jmh/kotlin/benchmarks/flow/scrabble/FlowPlaysScrabbleBase.kt)
2. [RxJava implementation](https://github.com/Kotlin/kotlinx.coroutines/blob/master/benchmarks/src/jmh/java/benchmarks/flow/scrabble/RxJava2PlaysScrabble.java)
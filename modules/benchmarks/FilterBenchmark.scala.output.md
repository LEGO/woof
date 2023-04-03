# The configuration and hardware info

```
Software:

  System Version: macOS 13.2.1 (22D68)
  Kernel Version: Darwin 22.3.0
  Boot Volume: Macintosh HD
  Boot Mode: Normal
  Computer Name: MacBook Pro (21)
  Secure Virtual Memory: Enabled

Hardware:

  Model Name: MacBook Pro
  Model Identifier: MacBookPro18,1
  Model Number: Z14Y00047DK/A
  Chip: Apple M1 Pro
  Total Number of Cores: 10 (8 performance and 2 efficiency)
  Memory: 32 GB
```

```
[info] # JMH version: 1.32
[info] # VM version: JDK 17.0.1, OpenJDK 64-Bit Server VM, 17.0.1+12
[info] # VM options: <none>
[info] # Blackhole mode: full + dont-inline hint
[info] # Warmup: 5 iterations, 10 s each
[info] # Measurement: 5 iterations, 10 s each
[info] # Timeout: 10 min per iteration
[info] # Threads: 1 thread, will synchronize iterations
[info] # Benchmark mode: Throughput, ops/time
```

# The results

```
[info] # Run complete. Total time: 00:25:04
[info] REMEMBER: The numbers below are just data. To gain reusable insights, you need to follow up on
[info] why the numbers are the way they are. Use profilers (see -prof, -lprof), design factorial
[info] experiments, perform baseline and negative tests that provide experimental control, make sure
[info] the benchmarking environment is safe on JVM/OS/HW level, ask for reviews from the domain experts.
[info] Do not assume the numbers tell you what you want them to tell.
[info] Benchmark                        Mode  Cnt      Score     Error  Units
[info] FilterBenchmark.testEverything  thrpt   25    255,250 ±  12,763  ops/s
[info] FilterBenchmark.testInfo        thrpt   25    722,664 ±  25,608  ops/s
[info] FilterBenchmark.testNothing     thrpt   25  11275,189 ± 314,631  ops/s
[success] Total time: 1509 s (25:09), completed 31 Mar 2023, 10.38.35
```

## Interpretation

My hypothesis going in was that materializing the output string was a significant part of the work, and the results seem
to corroborate this.

The benchmarks log 3 times at different levels (Info, Warn, Error) in a
tight loop of 1000 iterations. `testEverything` uses `Filter.everything`, i.e. it has to _always_ materialize the log
output. This is the worst case scenario.

`testInfo` uses the `Filter.exactLevel(LogLevel.Info)` which will only materialize `1/3` of the lines. This is almost 3
times as fast as `everything`.

`testNothing` is the best case scenario where nothing is materialized. This is more than 3 times faster
than `testEverything`, since not only
is nothing materialized, the outputs are never called, i.e. the compiler should be able to eliminate a lot of code (no
blackhole consume calls anymore!).

In general, these results are in line with the outcome I hoped for with the refactoring
from `type Filter = LogLine => Boolean` to
this new applicative style.

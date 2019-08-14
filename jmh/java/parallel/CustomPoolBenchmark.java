package parallel;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;


@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Fork(value = 2, jvmArgs = {"-Xms4G", "-Xmx4G"})
public class CustomPoolBenchmark {
    private static final int SIZE = 3_000_000;

    @Benchmark
    public long addNumsCommonPool() {
        return LongStream.rangeClosed(1, SIZE)
                .parallel()
                .sum();
    }

    @Benchmark
    @Fork(jvmArgsAppend = "-Djava.util.concurrent.ForkJoinPool.common.parallelism=32")
    public long addNumsBiggerCommonPool() {
        return LongStream.rangeClosed(1, SIZE)
                .parallel()
                .sum();
    }

    @Benchmark
    public long addNumsCustomFJPool() {
        long total = 0;
        ForkJoinPool pool = new ForkJoinPool(16);
        ForkJoinTask<Long> task = pool.submit(() -> LongStream.rangeClosed(1, SIZE)
                .parallel()
                .sum()
        );
        try {
            total = task.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            pool.shutdown();
        }
        return total;
    }

    @TearDown(Level.Invocation)
    public void tearDown() {
        System.gc();
    }
}

package com.arangodb.velocypack;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.profile.StackProfiler;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.spf4j.stackmonitor.JmhFlightRecorderProfiler;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 8, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(3)
public class Bench {
    @State(Scope.Benchmark)
    public static class Data {

        public final String str;

        public Data() {
            try {
                this.str = new String(
                    Files.readAllBytes(
                        Paths.get(this.getClass().getResource("/api-docs.json").toURI())
                    )
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(Bench.class.getSimpleName())
            .addProfiler(GCProfiler.class)
            .addProfiler(JmhFlightRecorderProfiler.class)
            .jvmArgs("-Xmx256m", "-Xms256m", "-XX:+UnlockCommercialFeatures") // https://stackoverflow.com/a/37857708
            .resultFormat(ResultFormatType.JSON)
            .result("target/jmh-result-" + System.currentTimeMillis() + ".json")
            .build();

        new Runner(opt).run();
    }


    @Benchmark
    public void fromJson(Data data, Blackhole bh) {
        VPackParser parser = new VPackParser.Builder().build();
        VPackSlice slice = parser.fromJson(data.str);
        bh.consume(slice);
    }
}

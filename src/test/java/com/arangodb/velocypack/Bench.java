package com.arangodb.velocypack;

import com.arangodb.jackson.dataformat.velocypack.VPackMapper;
import com.arangodb.velocypack.exception.VPackBuilderException;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 8, time = 1)
@Measurement(iterations = 30, time = 1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(1)
public class Bench {
    @State(Scope.Benchmark)
    public static class Data {

        public final String str;
        public final VPackSlice slice;
        public final VPackSlice koko = buildKoko();

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

            VPackParser parser = new VPackParser.Builder().build();
            slice = parser.fromJson(str);
        }

        public VPackSlice buildKoko() {
            VPackBuilder builder = new VPackBuilder();
            builder.add(ValueType.OBJECT);
            builder.add("name", "Koko");
            builder.add("species", "Gorilla");
            builder.add("language", "GSL");
            builder.add("knownSigns", 1000);
            builder.add("knownEnglishWords", 2000);
            builder.add("age", 46);
            builder.add("hairy", true);
            builder.add("iq", 80);
            builder.add("pet", "All Ball");
            builder.close();
            return builder.slice();
        }

    }

    public static void main(String[] args) throws RunnerException, IOException {
        String datetime = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        Path target = Files.createDirectories(Paths.get("target", "jmh-result"));

        ArrayList<String> jvmArgs = new ArrayList<>();
        jvmArgs.add("-Xms256m");
        jvmArgs.add("-Xmx256m");
        if (Integer.parseInt(System.getProperty("java.version").split("\\.")[0]) >= 11) {
            jvmArgs.add("-XX:StartFlightRecording=filename=" + target.resolve(datetime + ".jfr") + ",settings=profile");
        }

        Options opt = new OptionsBuilder()
            .include(Bench.class.getSimpleName())
            .addProfiler(GCProfiler.class)
            .jvmArgs(jvmArgs.toArray(new String[0]))
            .resultFormat(ResultFormatType.JSON)
            .result(target.resolve(datetime + ".json").toString())
            .build();

        new Runner(opt).run();
    }


    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Benchmark
    public void builder(Data data, Blackhole bh) {
        bh.consume(data.buildKoko());
    }

    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Benchmark
    public void sliceGet(Data data, Blackhole bh) {
        VPackSlice koko = data.koko;
        bh.consume(koko.get("name"));
        bh.consume(koko.get("species"));
        bh.consume(koko.get("language"));
        bh.consume(koko.get("knownSigns"));
        bh.consume(koko.get("knownEnglishWords"));
        bh.consume(koko.get("age"));
        bh.consume(koko.get("hairy"));
        bh.consume(koko.get("iq"));
        bh.consume(koko.get("pet"));
    }

    @Benchmark
    public void fromJson(Data data, Blackhole bh) {
        VPackParser parser = new VPackParser.Builder().build();
        VPackSlice slice = parser.fromJson(data.str);
        bh.consume(slice);
    }

    @Benchmark
    public void fromJsonJackson(Data data, Blackhole bh) throws JsonProcessingException {
        JsonNode vpackNode = new ObjectMapper().readTree(data.str);
        byte[] vpack = new VPackMapper().writeValueAsBytes(vpackNode);
        bh.consume(vpack);
    }

    @Benchmark
    public void toJson(Data data, Blackhole bh) {
        VPackParser parser = new VPackParser.Builder().build();
        String str = parser.toJson(data.slice);
        bh.consume(str);
    }

    @Benchmark
    public void toJsonJackson(Data data, Blackhole bh) throws IOException {
        JsonNode vpackNode = new VPackMapper().readTree(data.slice.toByteArray());
        String str = new ObjectMapper().writeValueAsString(vpackNode);
        bh.consume(str);
    }

    @Benchmark
    public void toJsonString(Data data, Blackhole bh) {
        String json = VPackParser.toJSONString(data.str);
        bh.consume(json);
    }

    @Benchmark
    public void toJsonStringJackson(Data data, Blackhole bh) {
        String json = toJSONStringJackson(data.str);
        bh.consume(json);
    }

    private static String toJSONStringJackson(final String text) {
        final StringWriter writer = new StringWriter();
        try {
            final JsonGenerator generator = new JsonFactory().createGenerator(writer);
            generator.writeString(text);
            generator.close();
        } catch (final IOException e) {
            throw new VPackBuilderException(e);
        }
        return writer.toString();
    }

}

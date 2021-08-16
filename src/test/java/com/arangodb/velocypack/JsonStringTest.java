package com.arangodb.velocypack;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.junit.Test;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assume.assumeTrue;

public class JsonStringTest {
    private static List<String> allChars = generateAllInputChars();
    private Context context;
    private Value jsStringify;
    private Value jsParse;
    private final String stringifyFn = "(function stringify(x){return JSON.stringify(x);})";
    private final String parseFn = "(function parse(x){return JSON.parse(x);})";

    @Test
    public void stringToJsonRoundTrip() {
        assumeTrue("This test requires GraalVM", org.graalvm.home.Version.getCurrent().isRelease());

        context = Context.create();
        jsStringify = context.eval("js", stringifyFn);
        jsParse = context.eval("js", parseFn);

        VPack vpack = new VPack.Builder().build();
        VPackParser parser = new VPackParser.Builder().build();

        // chars
        allChars.forEach(value -> {
            String expected = jsStringify.execute(value).as(String.class);
            assertThat(jsParse.execute(expected).as(String.class), is(value));
            assertThat(VPackParser.toJSONString(value), is(expected));
            assertThat(parser.toJson(vpack.serialize(value)), is(expected));
            assertThat(parser.fromJson(expected).getAsString(), is(value));
        });

        // random strings
        IntStream.range(0, 1000)
                .mapToObj(i -> generateRandomString(100))
                .forEach(value -> {
                    String expected = jsStringify.execute(value).as(String.class);
                    assertThat(jsParse.execute(expected).as(String.class), is(value));
                    assertThat(VPackParser.toJSONString(value), is(expected));
                    assertThat(parser.toJson(vpack.serialize(value)), is(expected));
                    assertThat(parser.fromJson(expected).getAsString(), is(value));
                });

        context.close();
    }

    private static List<String> generateAllInputChars() {
        return IntStream
                .range(0, Character.MAX_CODE_POINT + 1)
                .mapToObj(codePoint -> new String(Character.toChars(codePoint)))
                .filter(s -> !Character.isLowSurrogate(s.charAt(0)) &&
                        (!Character.isHighSurrogate(s.charAt(0)) || s.length() != 1))
                .collect(Collectors.toList());
    }

    private String generateRandomString(int length) {
        int max = allChars.size();
        Random r = new Random();
        return IntStream.range(0, length)
                .mapToObj(i -> allChars.get(r.nextInt(max)))
                .collect(Collectors.joining());
    }

}

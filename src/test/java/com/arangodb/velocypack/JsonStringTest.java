package com.arangodb.velocypack;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.junit.Test;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assume.assumeTrue;

public class JsonStringTest {
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
        generateInput().forEach(value -> {
            String expected = jsStringify.execute(value).as(String.class);
            assertThat(jsParse.execute(expected).as(String.class), is(value));
            assertThat(VPackParser.toJSONString(value), is(expected));
            assertThat(parser.toJson(vpack.serialize(value)), is(expected));
            assertThat(parser.fromJson(expected).getAsString(), is(value));
        });
        context.close();
    }

    private Stream<String> generateInput() {
        return IntStream
                .range(0, Character.MAX_CODE_POINT + 1)
                .filter(codePoint -> {
                    int type = Character.getType(codePoint);
                    return type != Character.PRIVATE_USE &&
                            type != Character.CONTROL &&
                            type != Character.UNASSIGNED;
                })
                .mapToObj(codePoint -> new String(Character.toChars(codePoint)))
                .filter(s -> !Character.isLowSurrogate(s.charAt(0)) &&
                        (!Character.isHighSurrogate(s.charAt(0)) || s.length() != 1));
    }
}

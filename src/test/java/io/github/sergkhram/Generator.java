package io.github.sergkhram;

import com.mifmif.common.regex.Generex;
import io.github.sergkhram.data.entity.Host;
import lombok.SneakyThrows;

import java.security.SecureRandom;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.IntStream;

public class Generator {
    public static String generateRandomString(String pattern) {
        return new Generex(pattern).random();
    }

    public static String generateRandomString(Integer length) {
        return generateRandomString(String.format("[A-Z]{1}[a-z]{%s}", length));
    }

    public static String generateRandomString() {
        return generateRandomString(10);
    }

    public static Integer generateRandomInt() {
        return generateRandomInt(1, 20);
    }

    @SneakyThrows
    public static Integer generateRandomInt(Integer startInt, Integer endInt) {
        return SecureRandom.getInstance("SHA1PRNG").ints(startInt, endInt).findFirst().getAsInt();
    }

    public static CopyOnWriteArrayList<Host> generateHosts(int count) {
        CopyOnWriteArrayList<Host> hosts = new CopyOnWriteArrayList<>();
        IntStream.range(0, count).parallel().forEach(
            it -> {
                Host host = new Host();
                host.setName(generateRandomString());
                host.setAddress(generateRandomString());
                hosts.add(
                    host
                );
            }
        );
        return hosts;
    }
}

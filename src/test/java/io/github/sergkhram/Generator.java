package io.github.sergkhram;

import com.mifmif.common.regex.Generex;
import io.github.sergkhram.data.entity.Host;

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

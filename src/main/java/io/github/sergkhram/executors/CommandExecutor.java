package io.github.sergkhram.executors;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.function.Consumer;

public class CommandExecutor {
    private final ProcessBuilder pB;
    private Process p = null;
    private static final File directory = new File(System.getProperty("user.home"));

    public CommandExecutor(List<String> cmd) {
        this.pB = new ProcessBuilder(cmd);
        pB.directory(directory);
    }

    public void execute(
        Consumer<Integer> logAtStart,
        Consumer<Integer> logAtFinish
    ) {
        execute(
            logAtStart,
            (outputCmdLine) -> {},
            (errorCmdLine) -> {},
            logAtFinish
        );
    }

    public void execute(
        Consumer<Integer> logAtStart,
        Consumer<String> inputStreamAction,
        Consumer<String> errorStreamAction,
        Consumer<Integer> logAtFinish
    ) {
        try {
            logAtStart.accept(null);
            p = pB.start();

            BufferedReader readOutput =
                new BufferedReader(new InputStreamReader(p.getInputStream()));
            String outputCommandLine;
            while ((outputCommandLine = readOutput.readLine()) != null) {
                inputStreamAction.accept(outputCommandLine);
            }

            BufferedReader errorOutput =
                new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String errorCommandLine;
            while ((errorCommandLine = errorOutput.readLine()) != null) {
                errorStreamAction.accept(errorCommandLine);
            }

            int exitCode = p.waitFor();
            logAtFinish.accept(exitCode);
        } catch (IOException |InterruptedException e) {
            e.printStackTrace();
        } finally {
            if(p != null) p.destroy();
        }
    }
}

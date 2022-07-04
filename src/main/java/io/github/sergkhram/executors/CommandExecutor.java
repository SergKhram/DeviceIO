package io.github.sergkhram.executors;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class CommandExecutor {
    private final ProcessBuilder pB;
    private Process p = null;
    private static final File directory = new File(System.getProperty("user.home"));

    public CommandExecutor(List<String> cmd) {
        this.pB = new ProcessBuilder(cmd);
        pB.directory(directory);
    }

    public void execute(
        LogAction logAtStart,
        LogAction logAtFinish
    ) {
        execute(
            logAtStart,
            (outputCmdLine) -> {},
            (errorCmdLine) -> {},
            logAtFinish
        );
    }

    public void execute(
        LogAction logAtStart,
        ProcessStreamAction inputStreamAction,
        ProcessStreamAction errorStreamAction,
        LogAction logAtFinish
    ) {
        try {
            logAtStart.print(null);
            p = pB.start();

            BufferedReader readOutput =
                new BufferedReader(new InputStreamReader(p.getInputStream()));
            String outputCommandLine;
            while ((outputCommandLine = readOutput.readLine()) != null) {
                inputStreamAction.apply(outputCommandLine);
            }

            BufferedReader errorOutput =
                new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String errorCommandLine;
            while ((errorCommandLine = errorOutput.readLine()) != null) {
                errorStreamAction.apply(errorCommandLine);
            }

            int exitCode = p.waitFor();
            logAtFinish.print(exitCode);
        } catch (IOException |InterruptedException e) {
            e.printStackTrace();
        } finally {
            if(p != null) p.destroy();
        }
    }

    @FunctionalInterface
    public interface LogAction {
        void print(Integer code);
    }

    @FunctionalInterface
    public interface ProcessStreamAction {
        void apply(String data);
    }
}

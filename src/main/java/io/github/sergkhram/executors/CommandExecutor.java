package io.github.sergkhram.executors;

import lombok.SneakyThrows;

import java.io.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class CommandExecutor {
    private final ProcessBuilder pB;
    private Process p = null;
    private static final File directory = new File(System.getProperty("user.home"));
//    private ExecutorService executor = Executors.newFixedThreadPool(2);

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

//            Callable<Void> outputTask = createTask(p.getInputStream(), inputStreamAction);
//            Callable<Void> errorTask = createTask(p.getErrorStream(), errorStreamAction);
//
//            List<Future<Void>> futures = executor.invokeAll(List.of(outputTask, errorTask));
//
//            futures.stream().parallel().forEach(it -> {
//                try {
//                    it.get(5000, TimeUnit.MILLISECONDS);
//                } catch (InterruptedException | ExecutionException | TimeoutException e) {
//                    e.printStackTrace();
//                }
//            });

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
        } catch (IOException|InterruptedException e) {
            e.printStackTrace();
        } finally {
            if(p != null) p.destroy();
        }
    }

//    private Callable<Void> createTask(
//        InputStream stream,
//        Consumer<String> action
//    ) {
//        return () -> {
//            BufferedReader readOutput = new BufferedReader(new InputStreamReader(stream));
//            String outputCommandLine;
//            try {
//                while ((outputCommandLine = readOutput.readLine()) != null) {
//                    action.accept(outputCommandLine);
//                }
//            } catch (IOException e) {}
//            return null;
//        };
//    }
}

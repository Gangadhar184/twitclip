package com.example.twitclip.util;

import org.springframework.stereotype.Component;
import java.io.BufferedReader;
import java.io.InputStreamReader;
@Component
public class CommandExecutor {

    public void execute(String... command) {
        try {
            Process process = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .start();

            StringBuilder output = new StringBuilder();

            try (BufferedReader reader =
                         new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                reader.lines().forEach(line -> {
                    System.out.println(line);
                    output.append(line).append("\n");
                });
            }

            boolean finished = process.waitFor(5, java.util.concurrent.TimeUnit.MINUTES);
            if (!finished) {
                process.destroyForcibly();
                throw new IllegalStateException("Command timed out");
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                throw new IllegalStateException(
                        "Command failed: " + String.join(" ", command) + "\n" + output
                );
            }

        } catch (Exception e) {
            throw new IllegalStateException("Execution failed: " + String.join(" ", command), e);
        }
    }
}

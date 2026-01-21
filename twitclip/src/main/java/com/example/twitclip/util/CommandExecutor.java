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

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Command failed:\n" + output);
            }
        } catch (Exception e) {
            throw new RuntimeException("Command execution failed", e);
        }
    }

}

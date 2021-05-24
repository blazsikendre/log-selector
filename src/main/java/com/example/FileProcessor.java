package com.example;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.*;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@AllArgsConstructor
public class FileProcessor {

    private final String inputFolder;
    private final String outputFolder;
    private final String configFile;

    public void listFiles() throws IOException, InterruptedException, ExecutionException {
        var cores = Runtime.getRuntime().availableProcessors();
        var threadPool = new ForkJoinPool(cores);
        try (Stream<Path> stream = Files.list(Paths.get(inputFolder)).parallel()) {
            threadPool.submit(() -> stream
                .parallel()
                .filter(file -> !Files.isDirectory(file))
                .map(Path::toAbsolutePath)
                .forEach(process())).get();
        } finally {
            threadPool.shutdown();
        }
    }

    public Consumer<Path> process() {
        return pathOfLogFile -> {
            try (FileInputStream configInputStream = new FileInputStream(Paths.get(configFile).toFile());
                 Scanner configFileScanner = new Scanner(configInputStream, StandardCharsets.UTF_8);
                 RandomAccessFile file = new RandomAccessFile(pathOfLogFile.toFile(), "r");
                 FileChannel fileChannel = file.getChannel())
            {
                MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY,  0, fileChannel.size());
                if (buffer != null) {
                    CharBuffer charBuffer = Charset.forName(StandardCharsets.UTF_8.name()).decode(buffer);
                    while (configFileScanner.hasNextLine()) {
                        Config.create(configFileScanner.nextLine()).ifPresent(config -> {
                            try (Scanner scanner = new Scanner(charBuffer.toString());
                                 FileOutputStream outputStream = new FileOutputStream(Paths.get(outputFolder).toFile(), true)) {
                                while (scanner.hasNextLine()) {
                                    var line = scanner.nextLine();
                                    if (config.getPattern().matcher(line).find()) {
                                        outputStream.write(line.concat(System.lineSeparator()).getBytes());
                                    }
                                }
                            } catch (IOException ignore) { }
                        });
                    }
                }
            } catch (IOException ignore) { }
        };
    }

    @Getter
    @AllArgsConstructor
    public static class Config {
        private static final String CONFIG_SEPARATOR = ":";

        private final String outputFile;
        private final Pattern pattern;

        public static Optional<Config> create(String line) {
            var partsOfConfigFile = line.split(CONFIG_SEPARATOR);
            if (partsOfConfigFile.length == 2) {
                return Optional.of(new Config(partsOfConfigFile[0].trim(), Pattern.compile(partsOfConfigFile[1].trim())));
            } else {
                return Optional.empty();
            }
        }
    }
}

package com.example;

import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class FileProcessorTest {

    @Test
    public void testListFiles() throws IOException, InterruptedException, ExecutionException {
        var inputFolder = Objects.requireNonNull(getClass().getResource("/log")).getPath();
        var outputFolder = System.getProperty("java.io.tmpdir");
        var configFile = Objects.requireNonNull(getClass().getResource("/config")).getPath();
        var fileProcessor = new FileProcessor(inputFolder, outputFolder, configFile);
        fileProcessor.listFiles();
    }

    @Test
    public void testProcess() {
    }
}
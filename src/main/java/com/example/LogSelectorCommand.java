package com.example;

import picocli.CommandLine;

@CommandLine.Command
public class LogSelectorCommand implements Runnable {

    @CommandLine.Option(names = {"-i", "--input-folder"})
    private String inputFolder;

    @CommandLine.Option(names = {"-o", "--output-folder"})
    private String outputFolder;

    @CommandLine.Option(names = {"-c", "--config-file"})
    private String configFile;

    public static void main(String[] args) {
        CommandLine.populateCommand(new LogSelectorCommand(), args);
    }

    @Override
    public void run() {
        var fileProcessor = new FileProcessor(inputFolder, outputFolder, configFile);
    }
}

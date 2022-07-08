package com.company;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static String rootPath = ".\\trains\\trainset";
    private static String mask = "*.sms";
    private static String newVolume = "0.1";
    private static Boolean start = false;

    public static void main(String[] args) throws IOException {

        if (args.length > 0) {
            for (String arg:args) {
                if (arg.toLowerCase().contains("path=")) {
                    rootPath = arg.substring(arg.indexOf("=") + 1);
                }
                if (arg.toLowerCase().contains("mask=")) {
                    mask = "glob:" + arg.substring(arg.indexOf("=") + 1);
                }
                if (arg.toLowerCase().contains("volume=")) {
                    newVolume = arg.substring(arg.indexOf("=") + 1);
                }
                if (arg.toLowerCase().equals("-start")) {
                    start = true;
                }
            }
        } else {
            System.out.println("ex., normalSound -path=D:\\games\\msts\\trains\\trainset -mask=*eng_cab*.sms -volume=0.1");
        }

        List<String> files = searchFiles(Path.of(rootPath), mask);

        if (start) {
            System.out.println("\nStart processing files!\n");
            for (String file : files) {
                List<String> lines = loadSMSFileAndReplaceVolume(file);
                saveSMSFile(file, lines);
            }
            System.out.println("\nDone!");
        } else {
            System.out.println("\nFile processing skipped!");
        }
    }

    private static List<String> searchFiles(Path rootDir, String pattern)  {
        System.out.print("Searching files ... ");
        List<String> files = new ArrayList<>();
        FileVisitor<Path> matcherVisitor = new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attribs) {
                FileSystem fs = FileSystems.getDefault();
                PathMatcher matcher = fs.getPathMatcher(pattern);
                Path name = file.getFileName();
                Path absolutePathToFile = file.toAbsolutePath();
                if (matcher.matches(name)) {
                    files.add(absolutePathToFile.toString());
                }
                return FileVisitResult.CONTINUE;
            }
        };
        try {
            Files.walkFileTree(rootDir, matcherVisitor);
            System.out.println("done");
        } catch (IOException e) {
            System.out.println("no files found, try again!");
        }
        System.out.println("Found files:");
        for (String file: files) {
            System.out.println(file);
        }
        if (files.isEmpty())
            System.out.println("nothing!");
        return files;
    }

    private static List<String> loadSMSFileAndReplaceVolume(String fileName) throws IOException {
        System.out.print("Changing volume parameter in the " + fileName + " ... ");
        List<String> oldLines = Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_16LE);
        ArrayList<String> newLines = new ArrayList<>();
        Boolean fix = true;
        for(String line: oldLines){
            if (line.toLowerCase().trim().contains("volume") && fix) {
                newLines.add(line.substring(0, line.indexOf("(") + 1) + newVolume +")");
                fix = false;
            } else {
                newLines.add(line);
            }
        }
        System.out.println("done");
        return newLines;
    }

    private static void saveSMSFile(String fileName, List<String> lines) throws IOException {
        FileWriter fw = new FileWriter(fileName, StandardCharsets.UTF_16LE);
        for(String line: lines) {
            fw.write(line + "\n");
        }
        fw.close();
    }
}
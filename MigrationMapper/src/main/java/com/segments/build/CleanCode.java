package com.segments.build;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public abstract class CleanCode {

    public ArrayList<Segment> getListOfCleanedFiles(String path, ArrayList<String> diffsFilePath) {
        System.out.println("\n**************** Start cleanning file from Java code **************");

        ArrayList<Segment> segmentList = new ArrayList<Segment>();
        for (String diffPath : diffsFilePath) {
            System.out.println("=========>======" + diffPath);
            System.out.println("Start Cleanning File:" + diffPath);
            ArrayList<Segment> listOfblocks = startClearn(diffPath);
            if (listOfblocks.size() > 0) {
                segmentList.addAll(listOfblocks);
            }

        }
        return segmentList;

    }

    protected abstract ArrayList<Segment> getBlocksList(String diffFilePath);

    protected ArrayList<Segment> startClearn(String diffFilePath) {
        ArrayList<Segment> listOfblocks = getBlocksList(diffFilePath);
        try {

            StringBuilder fileWithoutJavaCode = new StringBuilder();
            // read list of blocks
            for (Segment segment : listOfblocks) {
                System.out.println("Remove:" + segment.getCountRemovedLines());
                System.out.println("Add:" + segment.getCountAddLines());
                for (String lineIn : segment.blockCode) {
                    fileWithoutJavaCode.append(lineIn);
                    fileWithoutJavaCode.append("\n");
                    System.out.println(lineIn);
                }
                System.out.println("----------------------------");
            }
            ArrayList<String> listOfCleanedDiff = new ArrayList<String>();
            // write code segment to new clean file
            if (fileWithoutJavaCode.length() > 0) {
                String cleanDiffPath = diffFilePath.replace(".txt", "_clean.txt");
                listOfCleanedDiff.add(cleanDiffPath);
                FileWriter fr = new FileWriter(cleanDiffPath); // After '.' write
                fr.write(fileWithoutJavaCode.toString()); // Warning: this will REPLACE your old file content!
                fr.close();
                System.out.println("Complete Clean Up File successfully\n");

                // Apply algorithims on the functions
                // Just show data finding on console
                // new SegmentRule(listOfblocks);
            } else {
                try {
                    TerminalCommand terminalCommand = new TerminalCommand();

                    terminalCommand.deleteFolder(diffFilePath);
                    terminalCommand.deleteFolder(diffFilePath.replace(".txt", "_before.java"));
                    terminalCommand.deleteFolder(diffFilePath.replace(".txt", "_after.java"));
                    System.err.println("Delete File because it dosenot have migration");

                    // delete DR if there is no files there
                    String commitDir = diffFilePath.substring(0, diffFilePath.lastIndexOf("/"));
                    File folderDir = new File(commitDir);
                    if (folderDir.isDirectory()) {

                        if (folderDir.list().length <= 0) {
                            terminalCommand.deleteFolder(commitDir);
                            System.err.println("Directory is empty! will delete it");

                        }
                    }
                } catch (Exception ex) {
                }
            }

        } catch (IOException e) {
            // do something
        }

        return listOfblocks;
    }

    // Return line without >,<
    String cleanLineOfCode(String line) {
        if (line.length() < 2) {
            return "";
        }
        String linewithDot = line.substring(1).trim();
        return linewithDot;
    }

    boolean isStartwithNumber(String line) {
        boolean isNumber = false;
        if (line.startsWith("0") || line.startsWith("1") || line.startsWith("2") || line.startsWith("3")
                || line.startsWith("4") || line.startsWith("5") || line.startsWith("6") || line.startsWith("7")
                || line.startsWith("8") || line.startsWith("9")) {
            isNumber = true;
        }
        return isNumber;
    }

    public abstract boolean isUsedNewLibrary(String filePath, String addedLibraryName);
}


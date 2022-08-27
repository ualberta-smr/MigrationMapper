package com.segments.build;

import com.library.source.MigratedLibraries;
import com.main.parse.PythonHelper;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CleanPythonCode extends CleanCode {


    public ArrayList<Segment> getBlocksList(String diffFilePath) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(diffFilePath)));
            List<String> lines = br.lines().collect(Collectors.toList());
            ArrayList<Segment> segments = new ArrayList<>();

            Segment segment = new Segment();
            for (String line : lines) {
                if (line.startsWith("@@")) {
                    String[] parts = line.split(" ");
                    String[] before = parts[1].substring(1).split(",");
                    String[] after = parts[2].substring(1).split(",");
                    segment = new Segment();
                    segment.removedCode = PythonHelper.getUsedFunctions(MigratedLibraries.fromLibrary, before[0], before[1], diffFilePath.replace(".txt", "_before.java"));
                    segment.addedCode = PythonHelper.getUsedFunctions(MigratedLibraries.toLibrary, after[0], after[1], diffFilePath.replace(".txt", "_after.java"));

                    if (!segment.removedCode.isEmpty() && !segment.addedCode.isEmpty()) {
                        segment.fileName = Paths.get(diffFilePath).getFileName().toString().replace(".txt", "");
                        segments.add(segment);
                    }
                }
            }
            return segments;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

//        ArrayList<Segment> listOfblocks = new ArrayList<Segment>();
//
//        //ArrayList<String> listOfAddLibraryClassesName = classStructure.getLibraryClasses(MigratedLibraries.toLibrary);
//        //ArrayList<String> listOfRemovedLibraryClassesName = classStructure
//        //        .getLibraryClasses(MigratedLibraries.fromLibrary);
//        //ArrayList<String> listOfAllClassesName = new ArrayList<String>();
//        //listOfAllClassesName.addAll(listOfAddLibraryClassesName);
//        //listOfAllClassesName.addAll(listOfRemovedLibraryClassesName);
////        HashMap<String, String> listOfClassesInsatnces = listOfLibraryClassesInstance(listOfAllClassesName,
////                diffFilePath);
////        ArrayList<String> listOfAllRemovedStaticMethod = classStructure.getStaticMethods(MigratedLibraries.fromLibrary);
////        ArrayList<String> listOfAllAddedStaticMethod = classStructure.getStaticMethods(MigratedLibraries.toLibrary);
//        // This will hold previous segment to
//        // catch case when complete block removed and complete block is removed
//        // TODO: we need to add ablility to read metho that lives in multi line
//        try {
//            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(diffFilePath)));
//            String line;
//            boolean isApenndLine = false; // if developer used '.' to refer to instance from prevouse line
//            boolean isScannedLine = false;
//
//            ArrayList<String> junkText = new ArrayList<>();// save junk temprory before move
//            while ((line = br.readLine()) != null) {
//
//                // ignore empty line that that has only >, or <
//                if (line.trim().length() <= 1) {
//                    continue;
//                }
//
//                // ignore commented line
//                String lineClean = cleanLineOfCode(line);
//                if (lineClean.startsWith("*") || lineClean.startsWith("/")) {
//                    continue;
//                }
//
//                isScannedLine = false; // if the line is procsses already
//                if (line.length() <= 0) {
//                    continue;
//                }
//
//                if (isStartwithNumber(line)) {
//
//                    Segment cleanBlockSegment = isGoodBlock(junkText, listOfClassesInsatnces,
//                            listOfAddLibraryClassesName, listOfRemovedLibraryClassesName);
//                    if (cleanBlockSegment.addedCode.size() > 0 && cleanBlockSegment.removedCode.size() > 0) {
//                        // Segment segment= new
//                        // Segment(cleanBlockSegment.blockCode,cleanBlockSegment.countAddLines,cleanBlockSegment.countRemovedLines);
//                        cleanBlockSegment.addFileName(diffFilePath);
//                        listOfblocks.add(cleanBlockSegment);
//                    }
//
//                    junkText = new ArrayList<String>();
//
//                }
////
////                // if it use library class
////                for (String className : listOfAllClassesName) {
////                    if (line.contains(className)) {
////                        junkText.add(line);
////                        isScannedLine = true;
////                        isApenndLine = true;
////
////                        break;
////                    }
////                }
//                if (isScannedLine) {
//                    continue;
//                }
//                ;
//
//                // if it use library instace
////                for (String classInstance : listOfClassesInsatnces.keySet()) {
////                    if (line.contains(classInstance + ".")) {
////                        // line=line.replace(classInstance +".",
////                        // listOfClassesInsatnces.get(classInstance) +".");
////                        junkText.add(line);
////                        isScannedLine = true;
////                        isApenndLine = true;
////                        // make sure we used added and removed library
////                        break;
////                    }
////                }
//                if (isScannedLine) {
//                    continue;
//                }
//                ;
//
//                // see if he use static method
//                if (line.trim().startsWith("<")) {
//                    for (String methodName : listOfAllRemovedStaticMethod) {
//                        // either line stated with method with space or return data from method assgin
//                        // to object
//                        if (line.contains(" " + methodName + "(") || line.contains("=" + methodName + "(")) {
//                            junkText.add(line);
//                            isScannedLine = true;
//                            isApenndLine = true;
//
//                            break;
//                        }
//                    }
//                }
////                if (line.trim().startsWith(">")) {
////                    for (String methodName : listOfAllAddedStaticMethod) {
////                        if (line.contains(" " + methodName + "(") || line.contains("=" + methodName + "(")) {
////                            junkText.add(line);
////                            isScannedLine = true;
////                            isApenndLine = true;
////
////                            break;
////                        }
////                    }
////                }
//
//                if (isScannedLine) {
//                    continue;
//                }
//                ;
//
//
////                 * In case developer used '.' instance ex return new OkHttpClient.Builder() >
////                 * .connectTimeout(15, TimeUnit.SECONDS) > .build(); Read this data and change
////                 * it to return new OkHttpClient.Builder().connectTimeout(15,
////                 * TimeUnit.SECONDS).build();
//
//                if (startWithDot(line) == true && isApenndLine == true) {
//                    String prevousLine = junkText.get(junkText.size() - 1);
//                    String newLineConcat = prevousLine.trim() + cleanLineOfCode(line);
//                    junkText.set(junkText.size() - 1, newLineConcat);
//                } else {
//
//                    // another line of code added or removed
//                    if (line.trim().startsWith(">") || line.trim().startsWith("<")) {
//                        isApenndLine = false;
//
//                    } else {
//                        junkText.add(line);
//                    }
//                }
//
//            } // while
//
//            // check if last block is vaild
//            Segment cleanBlockSegment = isGoodBlock(junkText, listOfClassesInsatnces, listOfAddLibraryClassesName,
//                    listOfRemovedLibraryClassesName);
//            if (cleanBlockSegment.addedCode.size() > 0 && cleanBlockSegment.removedCode.size() > 0) {
//                // Segment segment= new
//                // Segment(cleanBlockSegment.blockCode,cleanBlockSegment.countAddLines,cleanBlockSegment.countRemovedLines);
//                cleanBlockSegment.addFileName(diffFilePath);
//                listOfblocks.add(cleanBlockSegment);
//            }
//
//            br.close();
//
//        } catch (Exception e) {
//            // TODO: handle exception
//        }
//
//        return listOfblocks;
    }

    @Override
    public boolean isUsedNewLibrary(String codeFilePath, String addedLibrarySpec) {
        return true;
//        if (!Paths.get(codeFilePath).toFile().exists())
//            return false;
//        return PythonHelper.isLibraryImported(codeFilePath, addedLibrarySpec);
    }
}



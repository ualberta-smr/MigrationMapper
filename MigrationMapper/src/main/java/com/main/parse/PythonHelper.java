package com.main.parse;

import com.project.settings.AppSettings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class PythonHelper {
    static HashMap<String, String[]> pythonCache = new HashMap<>();
    static String libDownloadDir = "librariesClasses/py";

    public static String[] runPython(String scriptName, boolean cache, String... arguments) {
        String scriptPath = Paths.get("python-scripts", scriptName).toString();
        String cacheKey = scriptPath + " " + String.join(" ", arguments);
        if (cache && pythonCache.containsKey(cacheKey))
            return pythonCache.get(cacheKey);

        List<String> cmds = new ArrayList<>();
        Collections.addAll(cmds, AppSettings.pythonCmd, scriptPath);
        Collections.addAll(cmds, arguments);
        System.out.println(String.join(" ", cmds));

        String[] results = runCommand(cmds.toArray(new String[0]));
        pythonCache.put(cacheKey, results);
        return results;
    }

    private static String[] runCommand(String... cmds) {
        Process process;
        try {
            process = Runtime.getRuntime().exec(cmds);
            process.waitFor();
            // process.waitFor(0, TimeUnit.MINUTES);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (process.exitValue() != 0) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String error = reader.lines().collect(Collectors.joining("\n"));
            throw new RuntimeException(error);
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String[] results = reader.lines().toArray(String[]::new);
        return results;
    }

    public static String[] getLibSpec(String requirementLine) {
        if (!requirementLine.matches(".*\\d.*"))
            return new String[]{requirementLine, "", ""};
        return runPython("readRequirement.py", true, requirementLine);
    }

    public static boolean indexLibrary(String packageSpec) {
        String[] results = runPython("indexLibrary.py", false, packageSpec);
        return results.length > 0 && results[0].equals("True");
    }

    public static boolean isLibraryImported(String codeFilePath, String librarySpec) {
        String[] results = runPython("isLibraryImported.py", true, codeFilePath, librarySpec);
        return results.length > 0 && results[0].equals("True");
    }

    public static Path libraryRootPath(String[] libSpec) {
        String libFolderName = normalizeLibSpecRemovingBrackets(libSpec[0])
                .replace('-', '_'); // the folder names use underscore instead of hyphen
        if (libSpec.length > 1)
            libFolderName += "-" + libSpec[2];

        return Paths.get(libDownloadDir, libFolderName);
    }

    public static boolean isLibIndexed(String libSpecString) {
        String[] result = runPython("isLibIndexed.py", true, libSpecString);
        return result.length > 0 && result[0].equals("True");
    }

    public static Path functionsPath(String libSpecString) {
        String[] libSpec = getLibSpec(libSpecString);
        Path root = libraryRootPath(libSpec);
        return root.resolve("functions.txt");
    }

    public static ArrayList<String> getUsedFunctions(String librarySpecStr, String startLine, String linesCount, String sourceFile) {
        String[] funcs = runPython("getUsedFunctions.py", true, librarySpecStr, sourceFile, startLine, linesCount);
        return new ArrayList<>(Arrays.asList(funcs));
    }

    public static String getLibIndexPath(String libraryInfo) {
        String[] results = runPython("getLibIndexPath.py", true, libraryInfo);
        return results[0];
    }

    public static String normalizeLibrarySpec(String librarySpec) {
        return librarySpec.trim().toLowerCase().replace('_', '-');
    }

    public static String normalizeLibSpecRemovingBrackets(String librarySpec) {
        String normal = librarySpec.trim().toLowerCase().replace('_', '-');
        String bracketRemoved = normal.replaceAll("\\[.*\\]", ""); // from sentry-sdk[flask] to sentry-sdk;

        return bracketRemoved;
    }
}

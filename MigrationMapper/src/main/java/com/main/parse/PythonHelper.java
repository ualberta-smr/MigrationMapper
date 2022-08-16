package com.main.parse;

import com.project.settings.AppSettings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Scanner;

public class PythonHelper {
    static HashMap<String, String[]> pythonCache = new HashMap<>();

    public static String[] runPython(String scriptName, String arguments) throws IOException, InterruptedException {
        String scriptPath = Paths.get("python-scripts", scriptName).toString();
        String cacheKey = scriptPath + " " + arguments;
        if (pythonCache.containsKey(cacheKey))
            return pythonCache.get(cacheKey);

        Process process = Runtime.getRuntime().exec(new String[]{AppSettings.pythonCmd, scriptPath, arguments});
        process.waitFor();
        if (process.exitValue() != 0) {
            Scanner scanner = new Scanner(process.getErrorStream());
            String results = scanner.nextLine();
            throw new RuntimeException(results);
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String[] results = reader.lines().toArray(String[]::new);
        pythonCache.put(cacheKey, results);
        return results;
    }

    public static String[] getLibSpec(String requirementLine) throws IOException, InterruptedException {
        return runPython("readRequirement.py", requirementLine);
    }
}

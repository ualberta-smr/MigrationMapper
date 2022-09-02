package com.main.parse;

import com.project.settings.AppSettings;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public interface ValidPairSet {
    boolean isValidPair(String sourceSpec, String targetSpec);

    boolean isValidSource(String sourceSpec);

    boolean isValidTarget(String targetSpec);

    ValidPairSet validPairSet = AppSettings.usePredefinedLibraryPairs ? new ConfiguredValidPairSet() : new AcceptAllValidPairSet();
}

class ConfiguredValidPairSet implements ValidPairSet {
    private final Set<String> validPairs = new HashSet<>();
    private final Set<String> validSources = new HashSet<>();
    private final Set<String> validTargets = new HashSet<>();

    ConfiguredValidPairSet() {
        try {
            Files.readAllLines(Path.of("data", "validPairs.csv"), StandardCharsets.UTF_8)
                    .forEach(this::addValidPair);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isValidPair(String sourceSpec, String targetSpec) {
        return validPairs.contains(getPair(sourceSpec, targetSpec));
    }

    @Override
    public boolean isValidSource(String sourceSpec) {
        var libName = PythonHelper.normalizeLibSpecRemovingBrackets(PythonHelper.getLibSpec(sourceSpec)[0]);
        return validSources.contains(libName);
    }

    @Override
    public boolean isValidTarget(String targetSpec) {
        var libName = PythonHelper.normalizeLibSpecRemovingBrackets(PythonHelper.getLibSpec(targetSpec)[0]);
        return validTargets.contains(libName);
    }

    public void addValidPair(String pairCsv) {
        var pair = pairCsv.split(",");
        var source = PythonHelper.normalizeLibrarySpec(pair[0]);
        var target = PythonHelper.normalizeLibrarySpec(pair[1]);
        pairCsv = getPair(source, target);
        validPairs.add(pairCsv);
        validSources.add(source);
        validTargets.add(target);
    }

    private String getPair(String source, String target) {
        return source + "," + target;
    }
}

class AcceptAllValidPairSet implements ValidPairSet {

    @Override
    public boolean isValidPair(String sourceSpec, String targetSpec) {
        return true;
    }

    @Override
    public boolean isValidSource(String sourceSpec) {
        return true;
    }

    @Override
    public boolean isValidTarget(String targetSpec) {
        return true;
    }
}
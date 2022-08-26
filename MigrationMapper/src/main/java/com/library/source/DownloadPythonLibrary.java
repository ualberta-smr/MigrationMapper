package com.library.source;

import com.main.parse.PythonHelper;

import java.nio.file.Path;

public class DownloadPythonLibrary implements DownloadLibraryBase {

    private String pathToSaveLib;

    public DownloadPythonLibrary(String pathToSaveLib) {
        this.pathToSaveLib = pathToSaveLib;
    }

    @Override
    public void download(String LibraryInfo, boolean isDocs) {
        if (!PythonHelper.indexLibrary(LibraryInfo))
            System.out.println("could not install " + LibraryInfo);
    }

    @Override
    public void buildTFfiles(String LibraryInfo) {
    }

    @Override
    public boolean isLibraryFound(String LibraryInfo) {
        return PythonHelper.isLibIndexed(LibraryInfo);
    }
}

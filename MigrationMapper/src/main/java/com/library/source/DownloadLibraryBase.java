package com.library.source;

public interface DownloadLibraryBase {
    void download(String LibraryInfo, boolean isDocs);
    void buildTFfiles(String LibraryInfo);
    boolean isLibraryFound(String LibraryInfo);
}

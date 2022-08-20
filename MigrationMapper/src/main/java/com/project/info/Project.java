package com.project.info;

import com.project.settings.AppSettings;
import com.project.settings.ProjectType;

import java.util.ArrayList;

public class Project {
    public int ProjectID;
    public String CommitID;
    public String LibraryName;
    public int isAdded;
    public String PomPath;

    public Project(int ProjectID, String CommitID, String LibraryName, int isAdded, String PomPath) {
        this.ProjectID = ProjectID;
        this.CommitID = CommitID;
        this.LibraryName = LibraryName;
        this.isAdded = isAdded;
        this.PomPath = PomPath;
    }

    @Override
    public String toString() {
        return LibraryName;
    }

    // return only library name
    // if it com.google:gson-core:1.2 will return gson only
    public String getLibraryName() {
        String[] libraryinfo = LibraryName.split(":");
        String[] artificeID = libraryinfo[1].split("-");
        return artificeID[0];

    }

    static public String isFound(ArrayList<Project> listOfLibraries, String librarName) {
        if (AppSettings.isPython()) {
            librarName = librarName.charAt(0) == ':' ? librarName.substring(1) : librarName;
            for (Project p : listOfLibraries) {
                if (p.LibraryName.startsWith(librarName))
                    return p.LibraryName;
            }
            return "";
        }
        String libraryName = "";
        for (Project project : listOfLibraries) {
            if (project.LibraryName.contains(librarName)) {
                libraryName = project.LibraryName;
                break;
            }
        }
        return libraryName;
    }

    // Make sure if it found as upgrade or as
    static public String isFoundUpgrade(ArrayList<Project> listOfLibraries, String librarName) {

        String libraryNameFound = "";
        for (Project project : listOfLibraries) {
            if (project.LibraryName.contains(librarName)) {
                libraryNameFound = project.LibraryName;
                break;
            }
        }
        return libraryNameFound;
    }
}

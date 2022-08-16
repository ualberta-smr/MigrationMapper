package com.database.mysql;

import com.project.info.Project;
import com.project.settings.AppSettings;
import com.project.settings.DatabaseLogin;
import com.project.settings.ProjectType;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class ProjectLibrariesDB {
    /*
     * CREATE TABLE `Projects` ( `ProjectsID` INTEGER, `CommitID` TEXT,
     * `LibraryName` TEXT );
     */
    public static void main(String args[]) {
        System.out.println("*****Loading all projects libraries (will take some time) *****");
        ArrayList<Project> listOfProjectLibraries = new ProjectLibrariesDB().getProjectLibraries();
        for (Project project : listOfProjectLibraries) {
            System.out.println(project.ProjectID);
        }
    }

    // isAdded=1 for add , isAdded=0 for removed
    public void addProjectLibrary(int ProjectID, String CommitID, String LibraryName, int isAdded, String projectPath) {
        // TODO: bad library donot get, for future we need to filter
        if (!isValidLibrary(LibraryName))
            return;

        Statement stmt = null;
        try {
            Connection c = null;
            Class.forName("com.mysql.cj.jdbc.Driver");
            c = DriverManager.getConnection(DatabaseLogin.url, DatabaseLogin.username, DatabaseLogin.password);

            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "INSERT INTO ProjectLibraries (ProjectsID,CommitID,LibraryName,isAdded, PomPath) " + "VALUES ("
                    + ProjectID + ", '" + CommitID + "','" + LibraryName + "'," + isAdded + ",'" + projectPath + "');";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        // System.out.println("Records created successfully");
    }

    private boolean isValidLibrary(String libName) {
        if (AppSettings.projectType == ProjectType.Java || AppSettings.projectType == ProjectType.Android) {
            String[] libraryInfo = libName.split(":");
            if (libraryInfo.length != 3)
                return false;
            if (libraryInfo[0].length() == 0 || libraryInfo[1].length() == 0 || libraryInfo[2].length() == 0)
                return false;
        }
        return true;
    }

    public ArrayList<Project> getProjectLibraries() {
        ArrayList<Project> listOfProjectLibraries = new ArrayList<Project>();

        Statement stmt = null;
        try {
            Connection c = null;
            Class.forName("com.mysql.cj.jdbc.Driver");
            c = DriverManager.getConnection(DatabaseLogin.url, DatabaseLogin.username, DatabaseLogin.password);

            c.setAutoCommit(false);
            stmt = c.createStatement();
            // TODO: Set only one commit for testing
            // avoid wrong data like ${project.groupId}:owlapi:xxx or org.json:gson:null
            String query = "SELECT * from ProjectLibrariesView WHERE LibraryName not LIKE '%$%' and  LibraryName not LIKE '%:null%'";// and
            // ProjectsID
            // in
            // (SELECT
            // AppID
            // from
            // MigrationSegmentsTemp
            // GROUP
            // BY
            // AppID)";
            ResultSet rs = stmt.executeQuery(query); // where ProjectsID=192"
            // System.out.println(query);
            while (rs.next()) {
                if (!isValidLibrary(rs.getString("LibraryName")))
                    continue;

                listOfProjectLibraries.add(new Project(rs.getInt("ProjectsID"), rs.getString("CommitID"),
                        rs.getString("LibraryName").trim(), rs.getInt("isAdded"), rs.getString("PomPath")));
            }
            rs.close();
            stmt.close();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());

        }

        return listOfProjectLibraries;

    }

    public ArrayList<String> getMigrationProjectLibraries(String LibraryName) {
        ArrayList<String> listOfProjectLibraries = new ArrayList<String>();

        Statement stmt = null;
        try {
            Connection c = null;
            Class.forName("com.mysql.cj.jdbc.Driver");
            c = DriverManager.getConnection(DatabaseLogin.url, DatabaseLogin.username, DatabaseLogin.password);

            c.setAutoCommit(false);
            stmt = c.createStatement();
            // TODO: Set only one commit for testing
            // avoid wrong data like ${project.groupId}:owlapi:xxx or org.json:gson:null
            ResultSet rs = stmt
                    .executeQuery("SELECT LibraryName from MigrationProjectLibrariesView WHERE LibraryName  LIKE '%"
                            + LibraryName + "%' and LibraryName  not LIKE '%null%' group by LibraryName");// and
            // ProjectsID
            // in
            // (SELECT
            // AppID
            // from
            // MigrationSegments
            // WHERE
            // MigrationRuleID="+
            // MigratedLibraries.ID+"
            // GROUP BY
            // AppID)"
            // );
            // //where
            // ProjectsID=192"

            while (rs.next()) {
                if (!isValidLibrary(rs.getString("LibraryName")))
                    continue;

                listOfProjectLibraries.add(rs.getString("LibraryName").trim());
            }
            rs.close();
            stmt.close();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());

        }

        return listOfProjectLibraries;

    }

    // Get libraries without sub package
    // ex (mokito-core, mokito-all will be called as mokito)
    public ArrayList<Project> getProjectLibrariesNoSub() {
        ArrayList<Project> listOfProjectLibraries = new ArrayList<Project>();

        Statement stmt = null;
        try {
            Connection c = null;
            Class.forName("com.mysql.cj.jdbc.Driver");
            c = DriverManager.getConnection(DatabaseLogin.url, DatabaseLogin.username, DatabaseLogin.password);

            c.setAutoCommit(false);
            stmt = c.createStatement();
            // TODO: Set only one commit for testing
            ResultSet rs = stmt.executeQuery("SELECT * from ProjectLibrariesView"); // where ProjectsID=192"

            while (rs.next()) {
                // not usefull library extaratec from pom.xml like ${project.groupId}:owlapi:xxx
                String libraryName1 = rs.getString("LibraryName");
                if (libraryName1.indexOf("$") >= 0) {
                    continue;
                }
                // TODO: bad library donot get, for future we need to filter
                if (!isValidLibrary(libraryName1))
                    continue;

                String[] libraryInfo = libraryName1.split(":");
                String artificateID = libraryInfo[1];
                if (artificateID.indexOf("-") > 0) {
                    artificateID = artificateID.substring(0, artificateID.indexOf("-"));
                }
                String libraryName = libraryInfo[0].trim() + ":" + artificateID.trim() + ":" + libraryInfo[2].trim();
                listOfProjectLibraries.add(new Project(rs.getInt("ProjectsID"), rs.getString("CommitID"), libraryName,
                        rs.getInt("isAdded"), rs.getString("PomPath")));
            }
            rs.close();
            stmt.close();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());

        }

        return listOfProjectLibraries;

    }

}

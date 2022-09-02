package com.main.parse;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import com.database.mysql.LibraryDocumentationDB;
import com.database.mysql.MigrationRule;
import com.database.mysql.MigrationRuleDB;
import com.database.mysql.MigrationSegments;
import com.database.mysql.MigrationSegmentsDB;
import com.database.mysql.ProjectLibrariesDB;
import com.library.Docs.MethodDocs;
import com.library.Docs.ParseHTML;
import com.library.lib.MethodLib;
import com.library.source.DownloadLibrary;
import com.project.info.Project;
import com.project.settings.AppSettings;
import com.segments.build.TerminalCommand;
import org.json.JSONObject;

/*
 * This Class collect Library docs from Maven repository then it convert it to relation database and
 * save it at 'LibraryDocumentation' Table
 */
public class DocManagerClient {
    public static String folderOfDocs = "/Docs";
    public static String pathDocs = Paths.get(".").toAbsolutePath().normalize().toString() + folderOfDocs;

    // Created needed folder
    TerminalCommand terminalCommand = new TerminalCommand();

    public DocManagerClient() {
        terminalCommand.createFolder("Docs");
        // TODO Auto-generated constructor stub
    }

    DownloadLibrary downloadLibrary = new DownloadLibrary(folderOfDocs);
    ParseHTML parseHTML = new ParseHTML();

    public static void main(String[] args) {
        AppSettings.loadAppSettings();
        new DocManagerClient().run();
    }

    void run() {

        LinkedList<MigrationSegments> migrationSegmentsLibraries = new MigrationSegmentsDB()
                .getAllMigrationSegmentsLibraries();

        System.out.println("From Library\t To library\n************************************");
        for (MigrationSegments migrationSegmentsLibraryies : migrationSegmentsLibraries) {
            System.out.println("#" + migrationSegmentsLibraryies.fromLibVersion + "==="
                    + migrationSegmentsLibraryies.toLibVersion);
            if (AppSettings.isPython()) {
                processPythonOptions(migrationSegmentsLibraryies.fromLibVersion);
                processPythonOptions(migrationSegmentsLibraryies.toLibVersion);
            } else {
                processOptions(migrationSegmentsLibraryies.fromLibVersion);
                processOptions(migrationSegmentsLibraryies.toLibVersion);
            }

        }

    }

    private void processPythonOptions(String libraryInfo) {
        if (patsedListOfLibs.contains(libraryInfo)) {
            System.out.println("This library already parsed in database, not need to parse again: " + libraryInfo);
            return;
        }
        patsedListOfLibs.add(libraryInfo);

        File libIndexFile = Path.of(PythonHelper.getLibIndexPath(libraryInfo)).toFile();
        if (!libIndexFile.exists())
            PythonHelper.indexLibrary(libraryInfo);

        try {
            var content = Files.readString(libIndexFile.toPath(), StandardCharsets.UTF_8);
            var json = new JSONObject(content);
            var functions = json.getJSONObject("functions");
            var functionNames = functions.keySet();
            LibraryDocumentationDB libraryDocumentationDB = new LibraryDocumentationDB();
            for (var k : functionNames) {
                var fn = k.toString();
                var info = functions.getJSONObject(fn);
                MethodDocs methodDocs = new MethodDocs(info.getString("fullName"), info.getString("description"),
                        info.getString("inputParams"), info.getString("returnParams"), info.getString("ClassName"),
                        info.getString("PackageName"));
                libraryDocumentationDB.add(libraryInfo, methodDocs);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    ArrayList<String> patsedListOfLibs = new ArrayList<String>();

    void processOptions(String libraryInfo) {

        if (patsedListOfLibs.contains(libraryInfo)) {
            System.out.println("This library already parsed in database, not need to parse again: " + libraryInfo);
            return;
        }
        patsedListOfLibs.add(libraryInfo);

        // System.out.println(libraryInfo);

        String[] LibraryInfos = libraryInfo.split(":");
        if (LibraryInfos.length < 3) {
            System.err.println(" Error in library name (" + libraryInfo + ")");
            return;
        }
        String DgroupId = LibraryInfos[0];
        String DartifactId = LibraryInfos[1];
        String Dversion = LibraryInfos[2];

        String libraryFileNameDocs = DartifactId + "-" + Dversion + "-javadoc.jar";
        String jarDocFolder = libraryFileNameDocs.replace("-javadoc.jar", "Docs");

        // Download Java Docs and extaract HTML
        downloadLibrary.download(libraryInfo, true);
        jar2HTML(libraryInfo);

        // TODO: We distable collect library source code not needed for now
        // Convert jar to source code
        // libManagerClient.jarToSourceCode(libraryInfo);

        // ArrayList<MethodLib> listOfMethodLibs =
        // libManagerClient.parseJarSource(libraryInfo);

        // Collect library documenation and method body
        ArrayList<MethodDocs> listOfMethodDocs = parseHTML.start(pathDocs + "/" + jarDocFolder, 1);
        // If first phase didnot work use second one
        if (listOfMethodDocs.size() == 0) {
            listOfMethodDocs = parseHTML.start(pathDocs + "/" + jarDocFolder, 2);
        }

        // Add in database
        LibraryDocumentationDB libraryDocumentationDB = new LibraryDocumentationDB();
        System.out.println("===> Mapp method code to method Docs and save them in DB");
        System.out.println("listOfMethodDocs: " + listOfMethodDocs.size());
        for (MethodDocs methodDocs : listOfMethodDocs) {

            // Wrong method ignore
            if (methodDocs.methodObj == null) {
                // System.err.println("2- Ignored method not good signature:"+
                // methodDocs.fullName);
                continue;
            }

            libraryDocumentationDB.add(libraryInfo, methodDocs);
        }

        listOfMethodDocs.clear();

    }

    // LibManagerClient libManagerClient = new LibManagerClient();
    void jar2HTML(String LibraryInfo) {

        String[] LibraryInfos = LibraryInfo.split(":");
        if (LibraryInfos.length < 3) {
            System.err.println(" Error in library name (" + LibraryInfo + ")");
            return;
        }
        // String DgroupId=LibraryInfos[0];
        String DartifactId = LibraryInfos[1];
        String Dversion = LibraryInfos[2];
        String libraryFileName = DartifactId + "-" + Dversion + "-javadoc.jar";

        String jarDocFolder = libraryFileName.replace("-javadoc.jar", "Docs");
        String jarDocNameZip = libraryFileName + ".zip";

        File f = new File(pathDocs + "/" + jarDocFolder);
        if (f.exists()) {
            System.out.println("Good! Library Docs source already there :" + pathDocs + "/" + jarDocFolder);
            return;
        }

        try {
            System.out.println("==> Start generate HTML from docs ");
            String cmdStr = "cd " + pathDocs + " && mkdir " + jarDocFolder + " && cp " + libraryFileName + " "
                    + jarDocFolder + "/" + jarDocNameZip + " && cd " + jarDocFolder + " && tar -xvf " + jarDocNameZip
                    + " && rm -rf " + jarDocNameZip;
            System.out.println(cmdStr);
            Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", cmdStr});
            if (!p.waitFor(5, TimeUnit.MINUTES)) {
                //timeout - kill the process.
                p.destroy();
            }
            System.out.println("<== Complete Generate");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}

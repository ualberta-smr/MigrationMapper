package com.main.parse;

import java.util.Scanner;

import com.algorithims.sa.*;
import com.project.settings.AppSettings;
import com.project.settings.ProjectType;

public class Main {

	public static void main(String[] args) {
		
		AppSettings.loadAppSettings();  // load database and github settings

		// 1 Collection
		new CollectorClient().startOnlineSearch();
		// 2- Find migration rule
		new MigrationRulesClient().start();
		// 3- Find code segments
		new DetectorClient().start();
		// 4- Collect Docs
		new DocManagerClient().run();

		// 5- Print Fragments results as HTML 
		new FragmentDocsMapperClient().run();
		
		// 6- Apply SA algorithm
		new FunctionMappingClient().run();
		 
		// 7- Print Method mapping results as HTML 
		new MethodsDocsMapperClient().run();
		

	}

}

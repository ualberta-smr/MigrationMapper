package com.project.settings;

public class ProjectBuildFile {

	static public String getType() {
		String buildFile = "pom.xml";

		if (AppSettings.projectType == ProjectType.Java) {
			buildFile = "pom.xml";

		} else if (AppSettings.projectType == ProjectType.Android) {
			buildFile = "build.gradle";
		} else if (AppSettings.projectType == ProjectType.PYTHON) {
			buildFile = "requirements.txt";
		}

		return buildFile;
	}

}

package com.softwareag.is.scaffold;

import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import com.softwareag.is.scaffold.types.ProjectScaffold;

public class ProjectScaffoldLoader {

	private ProjectScaffold _scaffold;
	
	public ProjectScaffoldLoader(File projectFile) throws InvalidProjectScaffoldFile {
	
		LoaderOptions options = new LoaderOptions();
		Yaml yaml = new Yaml(new Constructor(ProjectScaffold.class, options));
		
		InputStream inputStream;
		try {
			inputStream = new BufferedInputStream(new FileInputStream(projectFile));
		} catch (FileNotFoundException e) {
			throw new InvalidProjectScaffoldFile("Could not read file " + projectFile + " -> " + e.getMessage());
		}
		
		this._scaffold = yaml.load(inputStream);
	}
	
	public ProjectScaffold get() {
		
		return _scaffold;
	}
	
	public class InvalidProjectScaffoldFile extends Exception {

		private static final long serialVersionUID = 3939306395298200979L;

		public InvalidProjectScaffoldFile(String reason) {
			super(reason);
		}
	}
}

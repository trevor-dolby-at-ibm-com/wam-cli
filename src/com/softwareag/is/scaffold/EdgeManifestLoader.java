package com.softwareag.is.scaffold;

import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import com.softwareag.is.scaffold.types.EdgeManifest;
import com.softwareag.is.scaffold.types.ProjectScaffold;

public class EdgeManifestLoader {

	private EdgeManifest _manifest;
	
	public EdgeManifestLoader(File edgeManifestFile) throws InvalidEdgeManifestFile {
	
		LoaderOptions options = new LoaderOptions();
		Yaml yaml = new Yaml(new Constructor(EdgeManifest.class, options));
		
		InputStream inputStream;
		try {
			inputStream = new BufferedInputStream(new FileInputStream(edgeManifestFile));
		} catch (FileNotFoundException e) {
			throw new InvalidEdgeManifestFile("Could not read file " + edgeManifestFile + " -> " + e.getMessage());
		}
		
		this._manifest = yaml.load(inputStream);
	}
	
	public EdgeManifest get() {
		
		return _manifest;
	}
	
	public class InvalidEdgeManifestFile extends Exception {

		private static final long serialVersionUID = 2432942205881395161L;

		public InvalidEdgeManifestFile(String reason) {
			super(reason);
		}
	}
}

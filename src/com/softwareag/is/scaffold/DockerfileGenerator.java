package com.softwareag.is.scaffold;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;

import com.softwareag.is.scaffold.types.Configuration;
import com.softwareag.is.scaffold.types.EdgeManifest;
import com.softwareag.is.scaffold.types.Property;
import com.softwareag.is.scaffold.types.WmPackage;

public class DockerfileGenerator {

	private EdgeManifest _manifest;
	
	private String _from = "sagcr.azurecr.io/webmethods-edge-runtime:latest";
	private String _targetDir = "/opt/softwareag/IntegrationServer";

	private static final String FROM = "sagcr.azurecr.io/webmethods-edge-runtime:latest";
	private static final String TARGET = "/opt/softwareag/IntegrationServer";

	private static final String PROPS_DIR = "/opt/softwareag/IntegrationServer/application.properties";
	private static final String WPM_BASE = "RUN /opt/softwareag/wpm/bin/wpm.sh install ";
	
	private static final String ENV_VALUE = "$env{";
	
	private static final String WPM_USER_ARG = "-u ";
	private static final String WPM_PASSWORD_ARG = "-p ";
	private static final String WPM_GITURL_ARG = "-r ";
	
	private static final String WPM_SERVER_ARG = "-ws ";
	private static final String WPM_REG_ARG = "-wr ";
	private static final String WPM_TOKEN_ARG = "-j ";
	private static final String WPM_DIR_ARG = "-d ";
	
	public DockerfileGenerator(EdgeManifest manifest) {
		this._manifest = manifest;
	}
	
	public DockerfileGenerator(EdgeManifest manifest, String baseImage) {
		this(manifest);
		this._from = baseImage != null ? baseImage : FROM;
	}
	
	public void setTargetDir(String targetDir) {
		this._targetDir = targetDir != null ? targetDir : TARGET;
	}
	
	public String getDockerfile() {
	
		StringWriter str = new StringWriter();
		BufferedWriter w = new BufferedWriter(str);
		
		try {
			w.append("FROM " + _from);
			w.newLine();
			w.newLine();
			
			w.append("WORKDIR /opt/softwareag/wpm");
			w.newLine();
			
			for(WmPackage p : this._manifest.packages) {
				w.append(generateWpmCommandForPackage(p));
				w.newLine();
			};
			
			w.append("WORKDIR /");
			w.newLine();
			
			w.newLine();
			
			if (this._manifest.configurations != null && this._manifest.configurations.size() > 0) {
				w.append("COPY application.properties " + PROPS_DIR);
				
				for(Configuration c : this._manifest.configurations) {
					for (Property p : c.properties) {
						if (p.value.startsWith(ENV_VALUE)) {
							w.append("ENV " + stripEnvMarker(p.value));
						}
					}
				}
			}
			
			w.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		return str.toString();
	}
	
	public String getProperties() {
		
		if (this._manifest.configurations == null)
			return null;
		
		StringWriter str = new StringWriter();
		BufferedWriter w = new BufferedWriter(str);
		
		try {
		
			for(Configuration c : this._manifest.configurations) {
				for (Property p : c.properties) {
					w.append(p.name + "=" + p.value);
					w.newLine();
				}
			}
			
			w.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		return str.toString();
	}
	
	protected String generateWpmCommandForPackage(WmPackage p) {
		
		StringBuilder wpm = new StringBuilder();
		wpm.append(WPM_BASE);
		
		if (p.gitUsername != null)
			wpm.append(WPM_USER_ARG + p.gitUsername + " ");
		
		if (p.gitToken != null)
			wpm.append(WPM_PASSWORD_ARG + p.gitToken + " ");		
		
		if (p.gitUrl != null) 
			wpm.append(WPM_GITURL_ARG + p.gitUrl + " ");		
		
		if (p.wpmServer != null) 
			wpm.append(WPM_SERVER_ARG + p.wpmServer + " ");
		
		if (p.wpmRegistry != null) 
			wpm.append(WPM_REG_ARG + p.wpmRegistry + " ");
		
		if (p.wpmToken != null) 
			wpm.append(WPM_TOKEN_ARG + p.wpmToken + " ");
		
		wpm.append(WPM_DIR_ARG +_targetDir + " ");
		
		wpm.append(p.name);
		
		return wpm.toString();
	}
	
	private String stripEnvMarker(String value) {
		
		return value.substring(0, value.length()-2);
	}
}

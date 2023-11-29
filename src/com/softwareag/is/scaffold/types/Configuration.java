package com.softwareag.is.scaffold.types;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Configuration {

	public String name;
	public List<Property> properties;
	
	public Configuration() {
		properties = new ArrayList<Property>();
	}
	
	public Configuration(String configFile) throws InvalidConfigurationVariablesFile {
		this();
		
		File f = new File(configFile);

		if (f.canRead()) {
			
			System.out.println("Reading configuratigon variables file " + f.getAbsolutePath());
			
			Properties appProps = new Properties();
			try {
				appProps.load(new FileInputStream(configFile));
			} catch (Exception e) {
				throw new InvalidConfigurationVariablesFile("Invalid properties file " + e);
			}

			for (Object key : appProps.keySet()) {
				properties.add(new Property((String) key, appProps.getProperty((String) key)));
			}
		}
	}
	
	public void mergeConfiguration(Configuration c) {
		
		for (Property p : c.properties) {
			this.addProperty(p);
		}
	}
	
	public void mergeConfiguration(Map<String, String> variables) {
	
		for (String name: variables.keySet()) {
			
			Property p = new Property();
			p.name = name;
			p.value = variables.get(name);
			
			addProperty(p);
		}
	}
	
	public boolean addProperty(Property property) {
		
		String matched = null;
		
		if (this.properties == null) {
			this.properties = new ArrayList<Property>();
		}
		
		for (Property p : this.properties) {
			
			if (p.name.equals(property.name)) {
				matched = p.value;
				break;
			}
		}
		
		if (matched == null) {
			this.properties.add(property);
		} else if (!property.value.equals(matched)) {
			System.out.println("Warning, the property " + property.name + " already exists with the value " + matched + ", cannot set value " + property.value);
		}
		
		return matched == null;
	}
	
	public static class InvalidConfigurationVariablesFile extends Exception {

		private static final long serialVersionUID = 1L;

		
		InvalidConfigurationVariablesFile(String reason) {
			super(reason);
		}
	}
}

package com.softwareag.is.scaffold.types;

import java.util.List;
import java.util.Map;

public class ProjectScaffold {

	public String version;
	public Info info;
	public List<WmPackage> packages;
	public List<Service> services;
	public List<Configuration> configurations;
	public Map<String, String> configurationVariables;
}

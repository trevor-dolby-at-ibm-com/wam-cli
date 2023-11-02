package com.softwareag.is.scaffold.types;

import java.util.ArrayList;
import java.util.List;

public class EdgeManifest {

	public String version;
	public Info info;
	public List<WmPackage> packages;
	public List<Configuration> configurations;
	
	
	public void mergeScaffold(ProjectScaffold project) {
		
		for (WmPackage p : project.packages) {
			addPackage(p);
		}
		
		if (project.configurations != null) {
			for (Configuration c : project.configurations) {
				mergeConfigurations(c);
			}
		}
	}
	
	public void mergeConfigurations(Configuration config) {
	
		if (this.configurations == null) {
			this.configurations = new ArrayList<Configuration>();
			this.configurations.add(config);
			
		} else {
		
			for (Configuration c : this.configurations) {
				if (c.name.equals(config.name)) {
					c.mergeConfiguration(config);
					break;
				}
			}
		}
	}
	
	public boolean addPackage(WmPackage pckge) {
		
		boolean matched = false;
		
		if (this.packages == null) {
			this.packages = new ArrayList<WmPackage>();
		}
		
		for (WmPackage p : this.packages) {
			if (p.name.equals(pckge.name)) {
				
				if (p.isVersionInferiorTo(pckge)) {
				
					System.out.println("**WARNING** - Upgrading conflicting package " + p.name + " from " + p.version + " to " + pckge.version);

					// swap out current for latest
					p.version = pckge.version;
					p.gitBranch = pckge.gitBranch;
					p.gitServerName = pckge.gitServerName;
					p.gitUrl = pckge.gitUrl;
					p.gitUsername = pckge.gitUsername;
					p.gitToken = pckge.gitToken;
				}
				
				matched = true;
				break;
			}
		}
		
		if (!matched) {
			this.packages.add(pckge);
		}
		
		return matched;
	}
}

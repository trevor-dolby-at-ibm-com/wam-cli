package com.softwareag.is.scaffold.types;

import java.util.ArrayList;
import java.util.List;

import com.softwareag.is.scaffold.types.Configuration.InvalidConfigurationVariablesFile;

public class EdgeManifest {

	public String version = "1.0";
	public Info info;
	public List<WmPackage> packages;
	public List<Configuration> configurations;
	
	
	public void mergeScaffold(ProjectScaffold project, String service, String gitToken, String wpmToken) {
		
		if (service != null) {
			
			// only add packages that are referenced by the service
			
			for (Service s : project.services) {
				if (s.serviceName.equals(service)) {
					for (WmPackage x: s.packages) {
												
						for (WmPackage p : project.packages) {
														
							if (p.name.equals(x.name)) {
								
								if (gitToken != null && p.gitUrl != null)
									p.gitToken = gitToken;
								else if (wpmToken != null && p.wpmServer != null) 
									p.wpmToken = wpmToken;
								
								addPackage(p);
							}
						}
					}
				}
			}
		} else {
		
			for (WmPackage p : project.packages) {
				
				if (gitToken != null && p.gitUrl != null)
					p.gitToken = gitToken;
				else if (wpmToken != null && p.wpmServer != null) 
					p.wpmToken = wpmToken;
					
				addPackage(p);
			}
		}
		
		if (project.configurations != null) {
			for (Configuration c : project.configurations) {
				mergeConfigurations(c);
			}
		}
		
		if (project.configurationVariables != null && project.configurationVariables.size() > 0) {
			
			Configuration c = null;
			
			if (this.configurations == null) {
				this.configurations = new ArrayList<Configuration>();
				
				c = new Configuration();
				c.name = "default";
				this.configurations.add(c);
			} else {
				c = this.configurations.get(0);
			}
			
			c.mergeConfiguration(project.configurationVariables);
		}
	}
	
	public void mergeConfiguratinVariables(String config) throws InvalidConfigurationVariablesFile {
		
		Configuration configuration = new Configuration(config);
		
		if (this.configurations == null) {
			this.configurations = new ArrayList<Configuration>();
		} 
		
		this.configurations.add(configuration);
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

package com.softwareag.is.scaffold.types;

import org.apache.maven.artifact.versioning.ComparableVersion;

public class WmPackage {
	
	public String name;
	public String version;
	
	public String gitBranch;
	public String gitTag;
	public String gitServerName;
	public String gitUrl;
	public String gitUsername;
	public String gitToken;
	
	public String wpmServer;
	public String wpmRegistry;
	public String wpmToken;
	
	public boolean isVersionInferiorTo(WmPackage pckg) {
		
		ComparableVersion version1 = new ComparableVersion(this.version);
		ComparableVersion version2 = new ComparableVersion(pckg.version);

		return version1.compareTo(version2) < 0;
	}
}

package com.softwareag.is.wam.cli;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.softwareag.is.scaffold.DockerfileGenerator;
import com.softwareag.is.scaffold.EdgeManifestLoader;
import com.softwareag.is.scaffold.EdgeManifestLoader.InvalidEdgeManifestFile;
import com.softwareag.is.scaffold.ProjectScaffoldLoader;
import com.softwareag.is.scaffold.ProjectScaffoldLoader.InvalidProjectScaffoldFile;
import com.softwareag.is.scaffold.types.EdgeManifest;
import com.softwareag.is.scaffold.types.ProjectScaffold;

public class Main {

	public static final String BASE_IMAGE_DEFAULT = "sagcr.azurecr.io/webmethods-edge-runtime:latest";
	public static final String DEFAULT_PROJECT_FILE = "project.yaml";
	
	public static final String PROJECTS_ARG = "projects";
	public static final String EDGE_MANIFEST_ARG = "edge-manifest";
	public static final String BASE_IMAGE_ARG = "base-image";
	public static final String DOCKERFILE_ARG = "dockerfile-name";
	public static final String VERSION_ARG = "version";
	public static final String HELP_ARG = "help";

	
	public static final String VERSION = "0.0.1";
	
	public static final int SUCCESS = 0;
	public static final int FAIL = 0;
	
	public static void main(String[] args) {
		
		System.exit(new Main(convertArgsToCommandLine(args)).run());
	}
	
	private CommandLine _args;
	private EdgeManifest _manifest;
	
	public Main(CommandLine args) {
		this._args = args;
	}
	
	public int run() {
		
		System.out.println("webMethods Application Manager (wam)");
		System.out.println("version " + VERSION);
		System.out.println("");
		
		if (_args != null && _args.getOptions().length > 0 && _args.getOptions()[0].getLongOpt().equals(VERSION_ARG)) {
			
			return SUCCESS;
		} else if (_args != null && _args.getOptions().length > 0 && _args.getOptions()[0].getLongOpt().equals(HELP_ARG)) {

			System.out.println("Use this tool to generate a Dockerfile to build a webMethods runtime image from a project scaffold(s) or edge manifest file");
			System.out.println("");
			
            printHelp(getArgumentTypes());

			return SUCCESS;
		} else if (_args != null && _args.getOptionValue(PROJECTS_ARG) != null) {
			
			File f = new File(_args.getOptionValue(PROJECTS_ARG));
			
			if (f.canRead()) {
				if (f.isDirectory()) {
					
					System.out.println("Reading project scaffolds from directory " + f.getAbsolutePath());
					
					for (String p : f.list()) {
						try {
							if (p.endsWith(".yaml") || p.endsWith(".yml"))
							processScaffoldFile(new File(f, p));
						} catch (InvalidProjectScaffoldFile e) {
							 System.out.println("Ignoring Invalid project file " + p + ", due to error " + e.getMessage());
						}
					}
				} else {
					try {
						processScaffoldFile(f);
					} catch (InvalidProjectScaffoldFile e) {
						 System.out.println("Ouch, Invalid project file " + e.getMessage());
				         System.exit(1);
					}
				}
			}  else {
	            System.out.println("Ouch, cannot read project at " + f.getAbsolutePath());
	            return FAIL;
			}
			
		} else if (_args != null && _args.getOptionValue(EDGE_MANIFEST_ARG) != null) {
			
			File f = new File(_args.getOptionValue(EDGE_MANIFEST_ARG));

			if (f.canRead()) {
				
				System.out.println("Reading edge manifest file " + f.getAbsolutePath());

				try {
					this._manifest = new EdgeManifestLoader(f).get();
				} catch (InvalidEdgeManifestFile e) {
					 System.out.println("Ouch, Invalid manifest file " + e.getMessage());
			         System.exit(1);
				}
			} else {
	            System.out.println("Ouch, cannot read edge manifest file " + f.getAbsolutePath());
	            return FAIL;
			}
		} else {
			
			File f = new File(".", DEFAULT_PROJECT_FILE);
			
			if (f.canRead()) {
				try {
					processScaffoldFile(f);
				} catch (InvalidProjectScaffoldFile e) {
					 System.out.println("Ouch, Invalid project file " + e.getMessage());
			        return FAIL;
				}
			} else {
	            System.out.println("Please provide a project.yaml file or explicitly specify a file or directory with -p | --projects ");
	            return FAIL;
			}
		}
		
		// write out the manifest and properties file
		
		DockerfileGenerator d = new DockerfileGenerator(this._manifest);
		
		String df = null;
		
		if (this._args != null && _args.getOptionValue(DOCKERFILE_ARG) != null)
			df = this._args.getOptionValue(DOCKERFILE_ARG);
		else
			df = "Dockerfile";
		
		System.out.println("Generating docker file " + df);

		try {
			Files.write(Paths.get(df), d.getDockerfile().getBytes());
			
			String config = d.getProperties();
			
			if (config != null) {
				System.out.println("Generating application.properties file");
				Files.write(Paths.get("application.properties"), config.getBytes());
			}
		} catch (IOException e) {
			System.out.println("Ouch, cannot write out Dockerfile to " + df);
			return FAIL;
		}
		
		return SUCCESS;
	}
	
	public void processScaffoldFile(File f) throws InvalidProjectScaffoldFile {
		
		ProjectScaffold p = new ProjectScaffoldLoader(f).get();
		
		System.out.println("processing scaffold file " + f.getAbsolutePath());

		if (this._manifest == null) {
			this._manifest = new EdgeManifest();
		}
		
		this._manifest.mergeScaffold(p);
	}
	
	public static CommandLine convertArgsToCommandLine(String[] args) {
		
		CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;//not a good practice, it serves it purpose 

        Options options = getArgumentTypes();
        
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
        	System.out.println(e.getMessage());
            printHelp(options);
 
            System.exit(1);
        }
        
        return cmd;
	}
	
	private static void printHelp(Options options) {
	
		 HelpFormatter formatter = new HelpFormatter();
         formatter.printHelp("wam", options);
	}
	
	private static Options getArgumentTypes() {
		Options options = new Options();

        Option p = new Option("p", PROJECTS_ARG, true, "Project scaffold file or directory containing project scaffolds");
        p.setRequired(false);
        options.addOption(p);

        Option e = new Option("e", EDGE_MANIFEST_ARG, true, "Edge manifest file");
        e.setRequired(false);
        options.addOption(e);
        
        Option b = new Option("b", BASE_IMAGE_ARG, true, "Base image, defaults to " + BASE_IMAGE_DEFAULT);
        b.setRequired(false);
        options.addOption(b);
        
        Option d = new Option("d", DOCKERFILE_ARG, true, "Name of Dockerfile to generate, defaults to Dockerfile");
        d.setRequired(false);
        options.addOption(d);
       
        Option v = new Option("v", VERSION_ARG, false, "Get the version number of this cli tool");
        v.setRequired(false);
        options.addOption(v);
        
        Option h = new Option("h", HELP_ARG, false, "Help information");
        h.setRequired(false);
        options.addOption(h);
        
        return options;
	}
}

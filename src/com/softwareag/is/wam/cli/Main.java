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
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import com.softwareag.is.scaffold.DockerfileGenerator;
import com.softwareag.is.scaffold.EdgeManifestLoader;
import com.softwareag.is.scaffold.EdgeManifestLoader.InvalidEdgeManifestFile;
import com.softwareag.is.scaffold.ProjectScaffoldLoader;
import com.softwareag.is.scaffold.ProjectScaffoldLoader.InvalidProjectScaffoldFile;
import com.softwareag.is.scaffold.types.Configuration.InvalidConfigurationVariablesFile;
import com.softwareag.is.scaffold.types.EdgeManifest;
import com.softwareag.is.scaffold.types.ProjectScaffold;

public class Main {

	public static final String BASE_IMAGE_DEFAULT = "sagcr.azurecr.io/webmethods-edge-runtime:latest";
	public static final String DEFAULT_PROJECT_FILE = "project.yaml";
	
	public static final String PROJECTS_IN_ARG = "projects";
	public static final String SERVICE_IN_PROJ_ARG = "service";
	public static final String EDGE_MANIFEST_IN_ARG = "edge-manifest";
	public static final String EDGE_CONFIG_IN_ARG = "configuration-variables";
	public static final String BASE_IMAGE_ARG = "base-image";
	public static final String DOCKERFILE_ARG = "dockerfile-name";
	public static final String TARGETDIR_ARG = "target-dir";
	public static final String EDGE_MANIFEST_OUT_ARG = "edge-manifest-name";
	public static final String VERSION_ARG = "version";
	public static final String HELP_ARG = "help";
	public static final String GIT_JWT_ARG = "jwt_git";
	public static final String WPM_JWT_ARG = "wpm_git";

	
	public static final String VERSION = "0.0.3";
	
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
		
		String ef = _args.getOptionValue(EDGE_MANIFEST_OUT_ARG);
		
		if (_args != null && _args.getOptions().length > 0 && _args.getOptions()[0].getLongOpt().equals(VERSION_ARG)) {
			
			// show version info
			
			return SUCCESS;
		} else if (_args != null && _args.getOptions().length > 0 && _args.getOptions()[0].getLongOpt().equals(HELP_ARG)) {

			// show help
			
			System.out.println("Use this tool to generate a Dockerfile to build a webMethods runtime image from a project scaffold(s) or edge manifest file");
			System.out.println("");
			
            printHelp(getArgumentTypes());

			return SUCCESS;
		} else if (_args != null && _args.getOptionValue(EDGE_MANIFEST_IN_ARG) != null) {
			
			// process manifest file 
			
			File f = new File(_args.getOptionValue(EDGE_MANIFEST_IN_ARG));

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
			
			// process project file(s)
			
			// but first read intermediary file 
			
			File ief = ef != null ? new File(ef) : null;

			if (ief != null && ief.canRead()) {
				
				System.out.println("Reading edge manifest file " + ief.getAbsolutePath());

				try {
					this._manifest = new EdgeManifestLoader(ief).get();
				} catch (InvalidEdgeManifestFile e) {
					 System.out.println("Ouch, Invalid manifest file " + e.getMessage());
			         System.exit(1);
				}
			}

			if (_args != null && _args.getOptionValue(PROJECTS_IN_ARG) != null) {
				// read projects
				
				File f = new File(_args.getOptionValue(PROJECTS_IN_ARG));
				
				if (f.canRead()) {
					if (f.isDirectory()) {
						
						System.out.println("Reading project scaffolds from directory " + f.getAbsolutePath());
						
						for (String p : f.list()) {
							try {
								if (p.endsWith(".yaml") || p.endsWith(".yml"))
								processScaffoldFile(new File(f, p), _args.getOptionValue(SERVICE_IN_PROJ_ARG), _args.getOptionValue(GIT_JWT_ARG), _args.getOptionValue(WPM_JWT_ARG));
							} catch (InvalidProjectScaffoldFile e) {
								 System.out.println("Ignoring Invalid project file " + p + ", due to error " + e.getMessage());
							}
						}
					} else {
						try {
							processScaffoldFile(f, _args.getOptionValue(SERVICE_IN_PROJ_ARG), _args.getOptionValue(GIT_JWT_ARG), _args.getOptionValue(WPM_JWT_ARG));
						} catch (InvalidProjectScaffoldFile e) {
							 System.out.println("Ouch, Invalid project file " + e.getMessage());
					         System.exit(1);
						}
					}
				}  else {
		            System.out.println("Ouch, cannot read project at " + f.getAbsolutePath());
		            return FAIL;
				}
				
			} else {
				
				// read default project
				
				File f = new File(".", DEFAULT_PROJECT_FILE);
				
				if (f.canRead()) {
					try {
						processScaffoldFile(f, _args.getOptionValue(SERVICE_IN_PROJ_ARG), _args.getOptionValue(GIT_JWT_ARG), _args.getOptionValue(WPM_JWT_ARG));
					} catch (InvalidProjectScaffoldFile e) {
						 System.out.println("Ouch, Invalid project file " + e.getMessage());
				        return FAIL;
					}
				} else {
		            System.out.println("Please provide a project.yaml file or explicitly specify a file or directory with -p | --projects ");
		            return FAIL;
				}
			}
		}
			
		if (_args != null && _args.getOptionValue(EDGE_CONFIG_IN_ARG) != null) {
			try {
				_manifest.mergeConfiguratinVariables(_args.getOptionValue(EDGE_CONFIG_IN_ARG));
			} catch(InvalidConfigurationVariablesFile e) {
				System.out.println("Invalid configurtation variables file");
				return FAIL;
			}
		}
		
		if (_args != null && ef != null) {
		
			// write out the edge manifest
						
			System.out.println("Generating edge manifest file " + ef);

			try {
				final DumperOptions options = new DumperOptions();
				options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
				options.setPrettyFlow(true);
				options.setTags(null);
				
				// This representer is to avoid adding null values to the output
				Representer representer = new Representer(options) {
				    @Override
				    protected NodeTuple representJavaBeanProperty(Object javaBean, Property property, Object propertyValue,Tag customTag) {
				        // if value of property is null, ignore it.
				        if (propertyValue == null) {
				            return null;
				        }  
				        else {
				            return super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);
				        }
				    }
				};
				
				Yaml yaml = new Yaml(representer, options);
			    
			    Files.write(Paths.get(ef), yaml.dumpAs(this._manifest, Tag.MAP, null).getBytes());
			    
			} catch (IOException e) {
				System.out.println("Ouch, cannot write out edge manifest to " + ef);
				return FAIL;
			}
		} else {
		
			// write out a Dockefile and optional application.properties file
			
			DockerfileGenerator d = new DockerfileGenerator(this._manifest, this._args != null ? this._args.getOptionValue(BASE_IMAGE_ARG) : null);
			
			if (_args != null && _args.getOptionValue(TARGETDIR_ARG) != null) {
				d.setTargetDir(_args.getOptionValue(TARGETDIR_ARG));
			}
			
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
		}
		
		return SUCCESS;
	}
	
	public void processScaffoldFile(File f, String service, String gitToken, String wpmToken) throws InvalidProjectScaffoldFile {
		
		ProjectScaffold p = new ProjectScaffoldLoader(f).get();
		
		if (service != null) {
			System.out.println("extracting " + service + " assets from project scaffold " + f.getAbsolutePath());
		} else {
			System.out.println("extracting all services assets from project scaffold " + f.getAbsolutePath());
		}
		
		if (this._manifest == null) {
			this._manifest = new EdgeManifest();
		}
		
		this._manifest.mergeScaffold(p, service, gitToken, wpmToken);
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

        Option p = new Option("p", PROJECTS_IN_ARG, true, "Project scaffold file or directory containing project scaffolds");
        p.setRequired(false);
        options.addOption(p);
        
        Option s = new Option("s", SERVICE_IN_PROJ_ARG, true, "The project service to reference, if omitted all services will be included");
        s.setRequired(false);
        options.addOption(s);

        Option e = new Option("e", EDGE_MANIFEST_IN_ARG, true, "Edge manifest file to read");
        e.setRequired(false);
        options.addOption(e);
        
        Option c = new Option("c", EDGE_CONFIG_IN_ARG, true, "Additional configuration variables to be merged");
        c.setRequired(false);
        options.addOption(c);
        
        Option b = new Option("b", BASE_IMAGE_ARG, true, "Base image to build from");
        b.setRequired(false);
        options.addOption(b);
        
        Option j = new Option("j", GIT_JWT_ARG, true, "Token to use for accessing git server");
        j.setRequired(false);
        options.addOption(j);
        
        Option w = new Option("w", WPM_JWT_ARG, true, "Token to use for accessing wpm server");
        w.setRequired(false);
        options.addOption(w);
        
        Option d = new Option("d", DOCKERFILE_ARG, true, "Name of Dockerfile to generate, defaults to Dockerfile");
        d.setRequired(false);
        options.addOption(d);
       
        Option t = new Option("t", TARGETDIR_ARG, true, "Target directory for installation, defaults to /opt/softwareag/IntegrationServer");
        t.setRequired(false);
        options.addOption(t);
        
        Option x = new Option("m", EDGE_MANIFEST_OUT_ARG, true, "Write an intermediate edge manifest with the given name instead of a Dockerfile");
        x.setRequired(false);
        options.addOption(x);
        
        Option v = new Option("v", VERSION_ARG, false, "Get the version number of this cli tool");
        v.setRequired(false);
        options.addOption(v);
        
        Option h = new Option("h", HELP_ARG, false, "Help information");
        h.setRequired(false);
        options.addOption(h);
        
        return options;
	}
}

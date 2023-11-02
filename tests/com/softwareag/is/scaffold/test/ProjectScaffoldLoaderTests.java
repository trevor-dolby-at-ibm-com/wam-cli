package com.softwareag.is.scaffold.test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;

import org.junit.jupiter.api.Test;

import com.softwareag.is.scaffold.ProjectScaffoldLoader;
import com.softwareag.is.scaffold.ProjectScaffoldLoader.InvalidProjectScaffoldFile;
import com.softwareag.is.scaffold.types.ProjectScaffold;
import com.softwareag.is.wam.cli.Main;

public class ProjectScaffoldLoaderTests {

	@Test
	void loadProjectScaffold() {
		
		ProjectScaffold scaffold;
		try {
			scaffold = new ProjectScaffoldLoader(new File("project.yaml")).get();
			
			assertTrue(scaffold.version.equals("1.0"));
			assertTrue(scaffold.packages.size() == 2);
			assertTrue(scaffold.services.size() == 5);
					
			assertTrue(scaffold.services.get(0).serviceName.equals("edge.newtest.integrations:test"));
			assertTrue(scaffold.services.get(0).packages.size() == 2);
			
			assert(scaffold.configurations.size() == 1);
			assertTrue(scaffold.configurations.get(0).properties.size() == 2);
			assertTrue(scaffold.configurations.get(0).properties.get(0).value.equals("false"));

			System.out.println("======= service name " + scaffold.services.get(0).serviceName);
		} catch (InvalidProjectScaffoldFile e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	void generateDockerfile() {
		String[] args = {"test"};
		
		try {
			new Main(Main.convertArgsToCommandLine(args)).run();
		} catch(Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}

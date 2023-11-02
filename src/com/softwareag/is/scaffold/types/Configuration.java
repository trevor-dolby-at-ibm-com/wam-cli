package com.softwareag.is.scaffold.types;

import java.util.ArrayList;
import java.util.List;

public class Configuration {

	public String name;
	public List<Property> properties;
	
	public void mergeConfiguration(Configuration c) {
		
		for (Property p : c.properties) {
			this.addProperty(p);
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
		} else {
			System.out.println("Warning, the property " + property.name + " already exists with the value " + matched + ", cannot set value " + property.value);
		}
		
		return matched == null;
	}
}

/*******************************************************************************
 * Copyright (c) 2015 MINRES Technologies GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     MINRES Technologies GmbH - initial API and implementation
 *******************************************************************************/
package com.minres.scviewer.database;

import java.io.Serializable;
import java.util.HashMap;

public class RelationType implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6394859077558971735L;

	private static HashMap<String, RelationType> registry = new HashMap<>();
	
	private String name;

	public static RelationType create(String name){
		if(registry.containsKey(name)){
			return registry.get(name);
		}else{
			RelationType relType = new RelationType(name);
			registry.put(name, relType);
			return relType;
		}
		
	}
	
	private RelationType(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String toString(){
		return name;
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		return name.equals(obj);
	}
}

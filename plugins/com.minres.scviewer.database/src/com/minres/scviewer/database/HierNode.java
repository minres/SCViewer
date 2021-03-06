/*******************************************************************************
 * Copyright (c) 2015-2021 MINRES Technologies GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     MINRES Technologies GmbH - initial API and implementation
 *******************************************************************************/
package com.minres.scviewer.database;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The Class HierNode.
 */
public class HierNode implements IHierNode {

	/** The name. */
	protected String name;

	/** The parent. */
	protected IHierNode parent = null;

	/** The childs. */
	protected List<IHierNode> childNodes = Collections.synchronizedList(new ArrayList<>());

	/** The pcs. */
	protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	/**
	 * Instantiates a new hier node.
	 *
	 * @param name the name
	 */
	public HierNode(String name) {
		this.name = name;
	}

	/**
	 * Instantiates a new hier node.
	 *
	 * @param name   the name
	 * @param parent the parent
	 */
	public HierNode(String name, IHierNode parent) {
		this.name = name;
		this.parent = parent;
	}

	/**
	 * Instantiates a new hier node.
	 */
	public HierNode() {
	}

	/**
	 * Adds the property change listener.
	 *
	 * @param l the l
	 */
	@Override
	public void addPropertyChangeListener(PropertyChangeListener l) {
		pcs.addPropertyChangeListener(l);
	}

	/**
	 * Removes the property change listener.
	 *
	 * @param l the l
	 */
	@Override
	public void removePropertyChangeListener(PropertyChangeListener l) {
		pcs.removePropertyChangeListener(l);
	}

	/**
	 * Gets the full hierarchical name.
	 *
	 * @return the full name
	 */
	@Override
	public String getFullName() {
		if (parent != null)
			return parent.getFullName() + "." + name;
		else
			return name;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	@Override
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the parent.
	 *
	 * @return the parent
	 */
	@Override
	public IHierNode getParent() {
		return parent;
	}

	/**
	 * Sets the parent.
	 *
	 * @param parent the new parent
	 */
	@Override
	public void setParent(IHierNode parent) {
		this.parent = parent;
	}

	/**
	 * Gets the child nodes.
	 *
	 * @return the child nodes
	 */
	@Override
	public List<IHierNode> getChildNodes() {
		return Collections.unmodifiableList(childNodes);
	}

	/**
	 * Adds the child.
	 *
	 * @param child the child
	 */
	@Override
	public void addChild(IHierNode child) {
		if (!childNodes.contains(child)) {
			childNodes.add(child);
			child.setParent(this);
		}
	}

	/**
	 * Compare to.
	 *
	 * @param o the o
	 * @return the int
	 */
	@Override
	public int compareTo(IHierNode o) {
		return getFullName().compareTo(o.getFullName());
	}

	/**
	 * Derive waveform.
	 *
	 * @return the i derived waveform
	 */
	@Override
	public IDerivedWaveform deriveWaveform() {
		return null;
	}

}

/*******************************************************************************
 * Copyright (c) 2015, 2020 MINRES Technologies GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     MINRES Technologies GmbH - initial API and implementation
 *******************************************************************************/
package com.minres.scviewer.database;

/**
 * The Interface IDerivedWaveform.
 */
public interface IDerivedWaveform extends IWaveform {

	/**
	 * Adds the source waveform.
	 *
	 * @param waveform the waveform
	 */
	void addSourceWaveform(IWaveform waveform);
}

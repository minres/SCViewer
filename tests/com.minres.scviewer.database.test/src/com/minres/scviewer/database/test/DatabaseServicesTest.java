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
package com.minres.scviewer.database.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.minres.scviewer.database.EventEntry;
import com.minres.scviewer.database.IWaveform;
import com.minres.scviewer.database.IWaveformDb;

public class DatabaseServicesTest {


	private IWaveformDb waveformDb;
		
	@Before
	public void setUp() throws Exception {
		waveformDb=TestWaveformDbFactory.getDatabase();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testVCD() throws Exception {
		File f = new File("inputs/my_db.vcd").getAbsoluteFile();
		assertTrue(f.exists());
		waveformDb.load(f);
		assertNotNull(waveformDb);
		List<IWaveform> waves= waveformDb.getAllWaves();
		assertEquals(14,  waves.size());
		assertEquals(2,  waveformDb.getChildNodes().size());
		IWaveform bus_data_wave = waves.get(0);
		EventEntry bus_data_entry = bus_data_wave.getEvents().floorEntry(1400000000L);
		assertEquals("01111000", bus_data_entry.events[0].toString());
		IWaveform rw_wave = waves.get(2);
		EventEntry rw_entry = rw_wave.getEvents().floorEntry(2360000000L);
		assertEquals("1", rw_entry.events[0].toString());
	}

	@Test
	public void testTxSQLite() throws Exception {
		File f = new File("inputs/my_sqldb.txdb").getAbsoluteFile();
		assertTrue(f.exists());
		waveformDb.load(f);
		assertNotNull(waveformDb);
		assertEquals(3,  waveformDb.getAllWaves().size());
		assertEquals(1,  waveformDb.getChildNodes().size());
	}

	@Test
	public void testTxText() throws Exception {
		File f = new File("inputs/my_db.txlog").getAbsoluteFile();
		assertTrue(f.exists());
		waveformDb.load(f);
		assertNotNull(waveformDb);
		List<IWaveform> waveforms =  waveformDb.getAllWaves();
		assertEquals(3,  waveforms.size());
		assertEquals(1,  waveformDb.getChildNodes().size());
		for(IWaveform w:waveforms) {
			if(w.getId()==1) {
				assertEquals(2, w.getRowCount());
			} else if(w.getId()==2l) {
				assertEquals(1, w.getRowCount());
			} else if(w.getId()==3l) {
				assertEquals(1, w.getRowCount());
			}
		}
	}

	@Test
	public void testTxTextTruncated() throws Exception {
		File f = new File("inputs/my_db_truncated.txlog").getAbsoluteFile();
		assertTrue(f.exists());
		waveformDb.load(f);
		assertNotNull(waveformDb);
		assertEquals(3,  waveformDb.getAllWaves().size());
		assertEquals(1,  waveformDb.getChildNodes().size());
	}

	@Test
	public void testHierarchicalVCD() throws Exception {
		File f = new File("inputs/simple_system.vcd").getAbsoluteFile();
		assertTrue(f.exists());
		waveformDb.load(f);
		assertNotNull(waveformDb);
		assertEquals(779,  waveformDb.getAllWaves().size());
		assertEquals(1,  waveformDb.getChildNodes().size());
	}

}

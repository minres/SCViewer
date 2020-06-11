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
package com.minres.scviewer.database.vcd;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NavigableMap;
import java.util.Stack;
import java.util.TreeMap;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

import com.minres.scviewer.database.BitVector;
import com.minres.scviewer.database.ISignal;
import com.minres.scviewer.database.IWaveform;
import com.minres.scviewer.database.IWaveformDb;
import com.minres.scviewer.database.IWaveformDbLoader;
import com.minres.scviewer.database.InputFormatException;
import com.minres.scviewer.database.RelationType;

/**
 * The Class VCDDb.
 */
public class VCDDbLoader implements IWaveformDbLoader, IVCDDatabaseBuilder {

	
	/** The Constant TIME_RES. */
	private static final Long TIME_RES = 1000L; // ps;

	/** The db. */
	private IWaveformDb db;
	
	/** The module stack. */
	private Stack<String> moduleStack;
	
	/** The signals. */
	private List<IWaveform> signals;
	
	/** The max time. */
	private long maxTime;
	
	/**
	 * Instantiates a new VCD db.
	 */
	public VCDDbLoader() {
	}

	private static boolean isGzipped(File f) {
		InputStream is = null;
		try {
			is = new FileInputStream(f);
			byte [] signature = new byte[2];
			int nread = is.read( signature ); //read the gzip signature
			return nread == 2 && signature[ 0 ] == (byte) 0x1f && signature[ 1 ] == (byte) 0x8b;
		} catch (IOException e) {
			return false;
		} finally {
			try { is.close();} catch (IOException e) { }
		}
	}


	/* (non-Javadoc)
	 * @see com.minres.scviewer.database.ITrDb#load(java.io.File)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean load(IWaveformDb db, File file) throws Exception {
		if(file.isDirectory() || !file.exists()) return false;
		this.db=db;
		this.maxTime=0;
		String name = file.getCanonicalFile().getName();
		if(!(name.endsWith(".vcd") ||
				name.endsWith(".vcdz") ||
				name.endsWith(".vcdgz")  ||
				name.endsWith(".vcd.gz")) )
			return false;
		signals = new Vector<IWaveform>();
		moduleStack= new Stack<String>();
		FileInputStream fis = new FileInputStream(file);
		boolean res = new VCDFileParser(false).load(isGzipped(file)?new GZIPInputStream(fis):fis, this);
		moduleStack=null;
		if(!res) throw new InputFormatException();
		// calculate max time of database
		for(IWaveform waveform:signals) {
			NavigableMap<Long, ?> events =((ISignal<?>)waveform).getEvents();
			if(events.size()>0)
				maxTime= Math.max(maxTime, events.lastKey());
		}
		// extend signals to hav a last value set at max time
		for(IWaveform s:signals){
			if(s instanceof VCDSignal<?>) {
				TreeMap<Long,?> events = (TreeMap<Long, ?>) ((VCDSignal<?>)s).getEvents();
				if(events.size()>0 && events.lastKey()<maxTime){
					Object val = events.lastEntry().getValue();
					if(val instanceof BitVector) {
						((VCDSignal<BitVector>)s).addSignalChange(maxTime, (BitVector) val);
					} else if(val instanceof Double)
						((VCDSignal<Double>)s).addSignalChange(maxTime, (Double) val);
				}
			}
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see com.minres.scviewer.database.ITrDb#getMaxTime()
	 */
	@Override
	public Long getMaxTime() {
		return maxTime;
	}

	/* (non-Javadoc)
	 * @see com.minres.scviewer.database.ITrDb#getAllWaves()
	 */
	@Override
	public Collection<IWaveform> getAllWaves() {
		return signals;
	}

	/* (non-Javadoc)
	 * @see com.minres.scviewer.database.vcd.ITraceBuilder#enterModule(java.lang.String)
	 */
	@Override
	public void enterModule(String tokenString) {
		if(moduleStack.isEmpty()) {
			if("SystemC".compareTo(tokenString)!=0) moduleStack.push(tokenString);
		} else
			moduleStack.push(moduleStack.peek()+"."+tokenString);

	}

	/* (non-Javadoc)
	 * @see com.minres.scviewer.database.vcd.ITraceBuilder#exitModule()
	 */
	@Override
	public void exitModule() {
		if(!moduleStack.isEmpty()) moduleStack.pop();
	}

	/* (non-Javadoc)
	 * @see com.minres.scviewer.database.vcd.ITraceBuilder#newNet(java.lang.String, int, int)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Integer newNet(String name, int i, int width) {
		String netName = moduleStack.empty()? name: moduleStack.lastElement()+"."+name;
		int id = signals.size();
		assert(width>=0);
		if(width==0) {
			signals.add( i<0 ? new VCDSignal<Double>(db, id, netName, width) :
				new VCDSignal<Double>((VCDSignal<Double>)signals.get(i), id, netName));			
		} else if(width>0){
			signals.add( i<0 ? new VCDSignal<BitVector>(db, id, netName, width) :
				new VCDSignal<BitVector>((VCDSignal<BitVector>)signals.get(i), id, netName));
		}
		return id;
	}

	/* (non-Javadoc)
	 * @see com.minres.scviewer.database.vcd.ITraceBuilder#getNetWidth(int)
	 */
	@Override
	public int getNetWidth(int intValue) {
		VCDSignal<?> signal = (VCDSignal<?>) signals.get(intValue);
		return signal.getWidth();
	}

	/* (non-Javadoc)
	 * @see com.minres.scviewer.database.vcd.ITraceBuilder#appendTransition(int, long, com.minres.scviewer.database.vcd.BitVector)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void appendTransition(int signalId, long currentTime, BitVector value) {
		VCDSignal<BitVector> signal = (VCDSignal<BitVector>) signals.get(signalId);
		Long time = currentTime* TIME_RES;
		signal.getEvents().put(time, value);
	}
	
	/* (non-Javadoc)
	 * @see com.minres.scviewer.database.vcd.ITraceBuilder#appendTransition(int, long, com.minres.scviewer.database.vcd.BitVector)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void appendTransition(int signalId, long currentTime, double value) {
		VCDSignal<?> signal = (VCDSignal<?>) signals.get(signalId);
		Long time = currentTime* TIME_RES;
		if(signal.getWidth()==0){
			((VCDSignal<Double>)signal).getEvents().put(time, value);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.minres.scviewer.database.IWaveformDbLoader#getAllRelationTypes()
	 */
	@Override
	public Collection<RelationType> getAllRelationTypes(){
		return Collections.emptyList();
	}

}
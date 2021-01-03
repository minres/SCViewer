/*******************************************************************************
 * Copyright (c) 2012 IT Just working.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IT Just working - initial API and implementation
 *******************************************************************************/
package com.minres.scviewer.database.text;

import java.util.ArrayList;
import java.util.List;

import com.minres.scviewer.database.IWaveform;
import com.minres.scviewer.database.tx.ITx;
import com.minres.scviewer.database.tx.ITxGenerator;

class TxGenerator implements ITxGenerator {

	Long id;

	IWaveform stream;
	
	String name;
	
	Boolean active = false;
	
	List<ITx> transactions=new ArrayList<>();
	
	List<TxAttributeType> beginAttrs = new ArrayList<>();
	
	List<TxAttributeType> endAttrs= new ArrayList<>();

	TxGenerator(Long id, TxStream stream, String name){
		this.id=id;
		this.stream=stream;
		this.name=name;
	}
	
	@Override
	public Long getId() {
		return id;
	}

	@Override
	public IWaveform getStream(){
		return stream;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public List<ITx> getTransactions(){
		return transactions;
	}
	
	public List<TxAttributeType> getBeginAttrs() {
		return beginAttrs;
	}

	public List<TxAttributeType> getEndAttrs() {
		return endAttrs;
	}
	
	Boolean isActive() {return active;}

}

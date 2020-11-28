package com.minres.scviewer.database.text;

import com.minres.scviewer.database.EventKind;
import com.minres.scviewer.database.WaveformType;
import com.minres.scviewer.database.tx.ITx;
import com.minres.scviewer.database.tx.ITxEvent;

class TxEvent implements ITxEvent {

	final EventKind kind;
	
	final ITx transaction;
	
	final Long time;
	
	TxEvent(EventKind kind, ITx transaction) {
		super();
		this.kind = kind;
		this.transaction = transaction;
		this.time = kind==EventKind.BEGIN?transaction.getBeginTime():transaction.getEndTime();
	}

	public TxEvent(EventKind kind, ITx transaction, Long time) {
		super();
		this.kind = kind;
		this.transaction = transaction;
		this.time = time;
	}

	@Override
	public
	ITxEvent duplicate() throws CloneNotSupportedException {
		return new TxEvent(kind, transaction, time);
	}

//	@Override
//	int compareTo(IWaveformEvent o) {
//		time.compareTo(o.time)
//	}

	@Override
	public
	String toString() {
		return kind.toString()+"@"+time+" of tx #"+transaction.getId();
	}

	@Override
	public WaveformType getType() {
		return WaveformType.TRANSACTION;
	}

	@Override
	public EventKind getKind() {
		return kind;
	}

	@Override
	public Long getTime() {
		return time;
	}

	@Override
	public ITx getTransaction() {
		return transaction;
	}
}

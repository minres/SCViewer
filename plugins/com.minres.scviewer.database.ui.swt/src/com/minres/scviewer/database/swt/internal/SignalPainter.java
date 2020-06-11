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
package com.minres.scviewer.database.swt.internal;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.NavigableMap;

import javax.swing.JPanel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import com.minres.scviewer.database.BitVector;
import com.minres.scviewer.database.ISignal;
import com.minres.scviewer.database.ui.TrackEntry;
import com.minres.scviewer.database.ui.WaveformColors;

public class SignalPainter extends TrackPainter {
	private class SignalChange {
		long time;
		Object value;
		boolean fromMap;

		public SignalChange(Entry<Long, ?> entry) {
			time = entry.getKey();
			value = entry.getValue();
			fromMap = true;
		}

		public void set(Entry<Long, ?> entry, Long actTime) {
			if (entry != null) {
				time = entry.getKey();
				value = entry.getValue();
				fromMap = true;
			} else {
				time = actTime;
				fromMap = false;
			}
		}

		public void assign(SignalChange other) {
			time = other.time;
			value = other.value;
			fromMap = other.fromMap;
		}
	}

	/**
	 * 
	 */
	private static final JPanel DUMMY_PANEL = new JPanel();

	private final WaveformCanvas waveCanvas;

	int yOffsetT;
	int yOffsetM;
	int yOffsetB;
	/// maximum visible canvas position in canvas coordinates
	int maxPosX;
	/// maximum visible position in waveform coordinates
	int maxValX;

	public SignalPainter(WaveformCanvas txDisplay, boolean even, TrackEntry trackEntry) {
		super(trackEntry, even);
		this.waveCanvas = txDisplay;
	}

	private int getXPosEnd(long time) {
		long ltmp = time / this.waveCanvas.getScaleFactor() - waveCanvas.getXOffset();
		return ltmp > maxPosX ? maxPosX : (int) ltmp;
	}
	
	public void paintArea(GC gc, Rectangle area) {
		ISignal<?> signal = trackEntry.getSignal();
		if (trackEntry.selected)
			gc.setBackground(this.waveCanvas.colors[WaveformColors.TRACK_BG_HIGHLITE.ordinal()]);
		else
			gc.setBackground(this.waveCanvas.colors[even ? WaveformColors.TRACK_BG_EVEN.ordinal() : WaveformColors.TRACK_BG_ODD.ordinal()]);
		gc.setFillRule(SWT.FILL_EVEN_ODD);
		gc.fillRectangle(area);

		long scaleFactor = this.waveCanvas.getScaleFactor();
		long beginPos = area.x;
		long beginTime = (beginPos + waveCanvas.getXOffset())*scaleFactor;
        long endTime = beginTime + area.width*scaleFactor;
		
		Entry<Long, ?> first = signal.getEvents().floorEntry(beginTime);
		Entry<Long, ?> last = signal.getEvents().floorEntry(endTime);
		if (first == null) {
			if (last == null)
				return;
			first = signal.getEvents().firstEntry();
		} else if (last == null) {
			last = signal.getEvents().lastEntry();
		}
		gc.setForeground(this.waveCanvas.colors[WaveformColors.LINE.ordinal()]);
		gc.setLineStyle(SWT.LINE_SOLID);
		gc.setLineWidth(1);
		NavigableMap<Long, ?> entries = signal.getEvents().subMap(first.getKey(), false, last.getKey(), true);
		SignalChange left = new SignalChange(first);
		SignalChange right = new SignalChange(entries.size() > 0 ? entries.firstEntry() : first);
		maxPosX = area.x + area.width;
		maxValX = maxPosX + (int)waveCanvas.getXOffset();
		yOffsetT = this.waveCanvas.getTrackHeight() / 5 + area.y;
		yOffsetM = this.waveCanvas.getTrackHeight() / 2 + area.y;
		yOffsetB = 4 * this.waveCanvas.getTrackHeight() / 5 + area.y;
		int xSigChangeBeginVal = Math.max(area.x + (int)waveCanvas.getXOffset(), (int) (left.time / this.waveCanvas.getScaleFactor()));
		int xSigChangeBeginPos = area.x;
		int xSigChangeEndPos = Math.max(area.x, getXPosEnd(right.time));
		
		boolean multiple = false;
		if (xSigChangeEndPos == xSigChangeBeginPos) {
			// this can trigger if
			// a) left == right
			// b) left to close to right
			if (left.time == right.time) {
				right.time = endTime;
			} else {
				multiple = true;
				long eTime = (xSigChangeBeginVal + 1) * this.waveCanvas.getScaleFactor();
				right.set(entries.floorEntry(eTime), endTime);
				right.time = eTime;
			}
			xSigChangeEndPos = getXPosEnd(right.time);
		}

		
		SignalStencil stencil = getStencil(gc, left, entries);
		do {
			stencil.draw(gc, area, left.value, right.value, xSigChangeBeginPos, xSigChangeEndPos, multiple);
			if (right.time >= endTime)
				break;
			left.assign(right);
			xSigChangeBeginPos = xSigChangeEndPos;
			right.set(entries.higherEntry(left.time), endTime);
			xSigChangeEndPos = getXPosEnd(right.time);
			multiple = false;
			if (xSigChangeEndPos == xSigChangeBeginPos) {
				multiple = true;
				long eTime = (xSigChangeBeginPos + 1) * this.waveCanvas.getScaleFactor();
				Entry<Long, ?> entry = entries.floorEntry(eTime);
				if(entry!=null && entry.getKey()> right.time)
					right.set(entry, endTime);
				xSigChangeEndPos = getXPosEnd(eTime);
			}
		} while (left.time < endTime);
	}

	private SignalStencil getStencil(GC gc, SignalChange left, NavigableMap<Long, ?> entries) {
		Object val = left.value;
		if(val instanceof BitVector) {
			BitVector bv = (BitVector) val;
			if(bv.getWidth()==1)
				return new SingleBitStencil();				
			if(trackEntry.waveDisplay==TrackEntry.WaveDisplay.DEFAULT)
				return new MultiBitStencil(gc);
			else
				return new MultiBitStencilAnalog(entries, left.value, 
						trackEntry.waveDisplay==TrackEntry.WaveDisplay.CONTINOUS,
						trackEntry.valueDisplay==TrackEntry.ValueDisplay.SIGNED);
		} else if (val instanceof Double)
			return new RealStencil(entries, left.value, trackEntry.waveDisplay==TrackEntry.WaveDisplay.CONTINOUS);
		else
			return null;
	}

	private interface SignalStencil {

		public void draw(GC gc, Rectangle area, Object left, Object right, int xBegin, int xEnd, boolean multiple);
	}

	private class MultiBitStencil implements SignalStencil {

		private java.awt.Font tmpAwtFont;
		private int height;

		public MultiBitStencil(GC gc) {
			FontData fd = gc.getFont().getFontData()[0];
			height = gc.getDevice().getDPI().y * fd.getHeight() / 72;
			tmpAwtFont = new java.awt.Font(fd.getName(), fd.getStyle(), height);
		}

		public void draw(GC gc, Rectangle area, Object left, Object right, int xBegin, int xEnd, boolean multiple) {
			Color colorBorder = waveCanvas.colors[WaveformColors.SIGNAL0.ordinal()];
			BitVector last = (BitVector) left;
			if (last.getValue().toString().contains("X")) {
				colorBorder = waveCanvas.colors[WaveformColors.SIGNALX.ordinal()];
			} else if (last.getValue().toString().contains("Z")) {
				colorBorder = waveCanvas.colors[WaveformColors.SIGNALZ.ordinal()];
			}
			int width = xEnd - xBegin;
			if (width > 1) {
				int[] points = { 
						xBegin,     yOffsetM, 
						xBegin + 1, yOffsetT, 
						xEnd - 1,   yOffsetT, 
						xEnd,       yOffsetM, 
						xEnd - 1,   yOffsetB, 
						xBegin + 1, yOffsetB
				};
				gc.setForeground(colorBorder);
				gc.drawPolygon(points);
				gc.setForeground(waveCanvas.colors[WaveformColors.SIGNAL_TEXT.ordinal()]);
				//TODO: this code should be provided from a central location
				String label = null;
				switch(trackEntry.valueDisplay) {
				case SIGNED:
					label=Long.toString(last.toSignedValue());
					break;
				case UNSIGNED:
					label=Long.toString(last.toUnsignedValue());
					break;
				default:
					label="h'"+last.toHexString();
				}
				Point bb = getBoxWidth(gc, label);
				if (xBegin < area.x) {
					xBegin = area.x;
					width = xEnd - xBegin;
				}
				if (width > (bb.x+1)) {
					Rectangle old = gc.getClipping();
					gc.setClipping(xBegin + 3, yOffsetT, xEnd - xBegin - 5, yOffsetB - yOffsetT);
					gc.drawText(label, xBegin + 3, yOffsetM - bb.y / 2 - 1);
					gc.setClipping(old);
				}
			} else {
				gc.setForeground(colorBorder);
				gc.drawLine(xEnd, yOffsetT, xEnd, yOffsetB);
			}
		}

		private Point getBoxWidth(GC gc, String label) {
			return new Point(DUMMY_PANEL.getFontMetrics(tmpAwtFont).stringWidth(label), height);
		}

	}

	private class MultiBitStencilAnalog implements SignalStencil {

		final boolean continous;
		private long minVal;
		private long range;

		public MultiBitStencilAnalog(NavigableMap<Long, ?> entries, Object left, boolean continous, boolean signed) {
			this.continous=continous;
			Collection<?> values = ((NavigableMap<Long, ?>) entries).values();
			minVal=((BitVector) left).toUnsignedValue();
			range=2;
			if(!values.isEmpty()) {
				long maxVal=minVal;
				for (Object e : entries.values()) {
					long v = ((BitVector)e).toUnsignedValue();
					maxVal=Math.max(maxVal, v);
					minVal=Math.min(minVal, v);
				}
				if(maxVal==minVal) {
					maxVal--;
					minVal++;
				}
				range = maxVal-minVal;
			} else
				minVal--;
			
		}

		public void draw(GC gc, Rectangle area, Object left, Object right, int xBegin, int xEnd, boolean multiple) {
			long leftVal = ((BitVector) left).toUnsignedValue();
			long rightVal= ((BitVector) right).toUnsignedValue();
			gc.setForeground(waveCanvas.colors[WaveformColors.SIGNAL_REAL.ordinal()]);
			int yOffsetLeft = (int) ((leftVal-minVal) / range * (yOffsetB-yOffsetT));
			int yOffsetRight = (int) ((rightVal-minVal) / range * (yOffsetB-yOffsetT));
			if(continous) {
				if (xEnd > maxPosX) {
					gc.drawLine(xBegin, yOffsetB-yOffsetLeft, maxPosX, yOffsetB-yOffsetRight);
				} else {
					gc.drawLine(xBegin, yOffsetB-yOffsetLeft, xEnd, yOffsetB-yOffsetRight);
				}
			} else {
				if (xEnd > maxPosX) {
					gc.drawLine(xBegin, yOffsetB-yOffsetLeft, maxPosX, yOffsetB-yOffsetLeft);
				} else {
					gc.drawLine(xBegin, yOffsetB-yOffsetLeft, xEnd, yOffsetB-yOffsetLeft);
					if(yOffsetRight!=yOffsetLeft) {
						gc.drawLine(xEnd, yOffsetB-yOffsetLeft, xEnd, yOffsetB-yOffsetRight);
					}
				}
			}
		}
	}

	private class SingleBitStencil implements SignalStencil {
		public void draw(GC gc, Rectangle area, Object left, Object right, int xBegin, int xEnd, boolean multiple) {
			if (multiple) {
				gc.setForeground(waveCanvas.colors[WaveformColors.SIGNALU.ordinal()]);
				gc.drawLine(xBegin, yOffsetT, xBegin, yOffsetB);
				if(xEnd>xBegin) 
					gc.drawLine(xEnd, yOffsetT, xEnd, yOffsetB);
			} else {
				Color color = waveCanvas.colors[WaveformColors.SIGNALX.ordinal()];
				int yOffset = yOffsetM;
				switch (((BitVector) left).getValue()[0]) {
				case '1':
					color = waveCanvas.colors[WaveformColors.SIGNAL1.ordinal()];
					yOffset = yOffsetT;
					break;
				case '0':
					color = waveCanvas.colors[WaveformColors.SIGNAL0.ordinal()];
					yOffset = yOffsetB;
					break;
				case 'Z':
					color = waveCanvas.colors[WaveformColors.SIGNALZ.ordinal()];
					break;
				default:
				}
				gc.setForeground(color);
				if (xEnd > maxPosX) {
					gc.drawLine(xBegin, yOffset, maxPosX, yOffset);
				} else {
					gc.drawLine(xBegin, yOffset, xEnd, yOffset);
					int yNext = yOffsetM;
					switch (((BitVector) right).getValue()[0]) {
					case '1':
						yNext = yOffsetT;
						break;
					case '0':
						yNext = yOffsetB;
						break;
					default:
					}
					if (yOffset != yNext)
						gc.drawLine(xEnd, yOffset, xEnd, yNext);
				}
			}
		}
	}

	private class RealStencil implements SignalStencil {

		double minVal, range;
		
		final double scaleFactor = 1.05;
		
		boolean continous=true;
		
		public RealStencil(NavigableMap<Long, ?> entries, Object left, boolean continous) {
			this.continous=continous;
			Collection<?> values = ((NavigableMap<Long, ?>) entries).values();
			minVal=(Double) left;
			range=2.0;
			if(!values.isEmpty()) {
				double maxVal=minVal;
				for (Object e : entries.values()) {
					double v = ((Double)e);
					if(Double.isNaN(maxVal))
						maxVal=v;
					else if(!Double.isNaN(v))
						maxVal=Math.max(maxVal, v);
					if(Double.isNaN(minVal))
						minVal=v;
					else if(!Double.isNaN(v))
						minVal=Math.min(minVal, v);
				}
				if(Double.isNaN(maxVal)){
					maxVal=minVal=0.0;
				}
				range = (maxVal-minVal)*scaleFactor;
				double avg = (maxVal+minVal)/2.0;
				minVal=avg-(avg-minVal)*scaleFactor;
			}
		}

		public void draw(GC gc, Rectangle area, Object left, Object right, int xBegin, int xEnd, boolean multiple) {
			double leftVal = (Double) left;
			double rightVal= (Double) right;
			if(Double.isNaN(leftVal)) {
				Color color = waveCanvas.colors[WaveformColors.SIGNAL_NAN.ordinal()];
				int width = xEnd - xBegin;
				if (width > 1) {
					int[] points = { 
							xBegin, yOffsetT, 
							xEnd,   yOffsetT, 
							xEnd,   yOffsetB, 
							xBegin, yOffsetB
					};
					gc.setForeground(color);
					gc.drawPolygon(points);
					gc.setBackground(color);
					gc.fillPolygon(points);
				} else {
					gc.setForeground(color);
					gc.drawLine(xEnd, yOffsetT, xEnd, yOffsetB);
				}
			} else {				
				gc.setForeground(waveCanvas.colors[WaveformColors.SIGNAL_REAL.ordinal()]);
				int yOffsetLeft = (int) ((leftVal-minVal) / range * (yOffsetB-yOffsetT));
				int yOffsetRight = Double.isNaN(rightVal)?yOffsetLeft:(int) ((rightVal-minVal) / range * (yOffsetB-yOffsetT));
				if(continous) {
					if (xEnd > maxPosX) {
						gc.drawLine(xBegin, yOffsetB-yOffsetLeft, maxPosX, yOffsetB-yOffsetRight);
					} else {
						gc.drawLine(xBegin, yOffsetB-yOffsetLeft, xEnd, yOffsetB-yOffsetRight);
					}
				} else {
					if (xEnd > maxPosX) {
						gc.drawLine(xBegin, yOffsetB-yOffsetLeft, maxPosX, yOffsetB-yOffsetLeft);
					} else {
						gc.drawLine(xBegin, yOffsetB-yOffsetLeft, xEnd, yOffsetB-yOffsetLeft);
						if(yOffsetRight!=yOffsetLeft) {
							gc.drawLine(xEnd, yOffsetB-yOffsetLeft, xEnd, yOffsetB-yOffsetRight);
						}
					}
				}
			}
		}
	}

}
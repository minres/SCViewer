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

/**
 * The Class BitVector.
 */
public class BitVector implements IEvent {

	/** The width. */
	private final int width;

	/** The packed values. */
	private int[] packedValues;

	/**
	 * Instantiates a new bit vector.
	 *
	 * @param netWidth the net width
	 */
	public BitVector(int netWidth) {
		this.width = netWidth;
		packedValues = new int[(netWidth + 15) / 16];
		for (int i = 0; i < packedValues.length; i++)
			packedValues[i] = 0;
	}

	/**
	 * Sets the value.
	 *
	 * @param i     the i
	 * @param value the value
	 */
	public void setValue(int i, BitValue value) {
		int bitIndex = i * 2;
		int wordOffset = bitIndex >> 5;
		int bitOffset = bitIndex & 31;
		packedValues[wordOffset] &= ~(3 << bitOffset);
		packedValues[wordOffset] |= value.ordinal() << bitOffset;
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public char[] getValue() {
		int bitOffset = 0;
		int wordOffset = 0;
		char[] res = new char[width];
		// Copy values out of packed array
		for (int i = 0; i < width; i++) {
			int currentWord = (packedValues[wordOffset] >> bitOffset) & 3;
			res[width - i - 1] = BitValue.fromInt(currentWord).toChar();
			bitOffset += 2;
			if (bitOffset == 32) {
				wordOffset++;
				bitOffset = 0;
			}
		}
		return res;
	}

	/**
	 * Sets the value.
	 *
	 * @param value the new value
	 */
	public void setValue(char[] value) {
		int bitIndex = width;
		int wordOffset = bitIndex >> 4;
		int bitOffset = (bitIndex * 2) % 32;
		for (int i = Math.min(value.length, width) - 1; i >= 0; i--) {
			packedValues[wordOffset] |= BitValue.fromChar(value[i]).ordinal() << bitOffset;
			bitOffset += 2;
			if (bitOffset == 32) {
				wordOffset++;
				bitOffset = 0;
			}
		}
	}

	/**
	 * Gets the width.
	 *
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * To string.
	 *
	 * @return the string
	 */
	public String toString() {
		return new String(getValue());
	}

	/**
	 * To hex string.
	 *
	 * @return the string
	 */
	public String toHexString() {
		int resWidth = (width - 1) / 4 + 1;
		char[] value = getValue();
		char[] res = new char[resWidth];
		for (int i = resWidth - 1; i >= 0; i--) {
			int digit = 0;
			for (int j = 3; j >= 0; j--) {
				if ((4 * i + j) < value.length) {
					BitValue val = BitValue.fromChar(value[4 * i + j]);
					switch (val) {
					case X:
					case Z:
						res[i] = val.toChar();
						continue;
					case ONE:
						digit += 1 << (3 - j);
						break;
					default:
						break;
					}
				}
			}
			res[i] = Character.forDigit(digit, 16); // ((digit < 10) ? '0' + digit : 'a' + digit -10)
		}
		return new String(res);
	}

	/**
	 * To unsigned value.
	 *
	 * @return the long
	 */
	public long toUnsignedValue() {
		long res = 0;
		int bitOffset = 0;
		int wordOffset = 0;
		int currentWord = 0;
		// Copy values out of packed array
		for (int i = 0; i < width; i++) {
			if (bitOffset == 0)
				currentWord = packedValues[wordOffset];
			switch (currentWord & 3) {
			case 1:
				res |= 1 << i;
				break;
			case 2:
			case 3:
				return 0;
			default:
				break;
			}
			bitOffset += 2;
			if (bitOffset == 32) {
				wordOffset++;
				bitOffset = 0;
			} else {
				currentWord >>= 2;
			}
		}
		return res;
	}

	/**
	 * To signed value.
	 *
	 * @return the long
	 */
	public long toSignedValue() {
		long res = 0;
		int bitOffset = 0;
		int wordOffset = 0;
		int currentWord = 0;
		int lastVal = 0;
		// Copy values out of packed array
		for (int i = 0; i < width; i++) {
			if (bitOffset == 0)
				currentWord = packedValues[wordOffset];
			lastVal = 0;
			switch (currentWord & 3) {
			case 1:
				res |= 1 << i;
				lastVal = 1;
				break;
			case 2:
			case 3:
				return 0;
			default:
			}
			bitOffset += 2;
			if (bitOffset == 32) {
				wordOffset++;
				bitOffset = 0;
			} else {
				currentWord >>= 2;
			}
		}
		if (lastVal != 0)
			res |= -1l << width;
		return res;
	}

	/**
	 * Gets the kind.
	 *
	 * @return the kind
	 */
	@Override
	public EventKind getKind() {
		return EventKind.SINGLE;
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	@Override
	public WaveformType getType() {
		return WaveformType.SIGNAL;
	}

	/**
	 * Duplicate.
	 *
	 * @return the i event
	 * @throws CloneNotSupportedException the clone not supported exception
	 */
	@Override
	public IEvent duplicate() throws CloneNotSupportedException {
		return (IEvent) this.clone();
	}
}

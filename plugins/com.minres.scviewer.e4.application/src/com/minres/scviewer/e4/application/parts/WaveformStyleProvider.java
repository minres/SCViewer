package com.minres.scviewer.e4.application.parts;

import java.util.HashMap;

import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wb.swt.SWTResourceManager;
import org.osgi.service.prefs.Preferences;

import com.minres.scviewer.database.ui.IWaveformStyleProvider;
import com.minres.scviewer.database.ui.WaveformColors;
import com.minres.scviewer.e4.application.preferences.PreferenceConstants;

public class WaveformStyleProvider implements IWaveformStyleProvider {

	private Composite parent;
	
	private Font nameFont;
	
	private Color[] colors = new Color[WaveformColors.values().length];

	// list of random colors
	private static Color[][] randomColors;

	private int trackHeigth=25;
	
	public WaveformStyleProvider() {
		setupDefaults();
	}

	private void setupDefaults() {
		Display display = Display.getCurrent();
		
		nameFont = display.getSystemFont();
        colors[WaveformColors.LINE.ordinal()] = SWTResourceManager.getColor(SWT.COLOR_RED);
        colors[WaveformColors.LINE_HIGHLITE.ordinal()] = SWTResourceManager.getColor(SWT.COLOR_CYAN);
        colors[WaveformColors.TRACK_BG_EVEN.ordinal()] = SWTResourceManager.getColor(SWT.COLOR_BLACK);
        colors[WaveformColors.TRACK_BG_ODD.ordinal()] = SWTResourceManager.getColor(40, 40, 40);
        colors[WaveformColors.TRACK_BG_HIGHLITE.ordinal()] = SWTResourceManager.getColor(40, 40, 80);
        colors[WaveformColors.TX_BG.ordinal()] = SWTResourceManager.getColor(SWT.COLOR_GREEN);
        colors[WaveformColors.TX_BG_HIGHLITE.ordinal()] = SWTResourceManager.getColor(SWT.COLOR_DARK_GREEN);
        colors[WaveformColors.TX_BORDER.ordinal()] = SWTResourceManager.getColor(SWT.COLOR_RED);
        colors[WaveformColors.SIGNAL0.ordinal()] = SWTResourceManager.getColor(SWT.COLOR_GREEN);
        colors[WaveformColors.SIGNAL1.ordinal()] = SWTResourceManager.getColor(SWT.COLOR_GREEN);
        colors[WaveformColors.SIGNALZ.ordinal()] = SWTResourceManager.getColor(SWT.COLOR_DARK_YELLOW);
        colors[WaveformColors.SIGNALX.ordinal()] = SWTResourceManager.getColor(255,  51,  51);
        colors[WaveformColors.SIGNALU.ordinal()] = SWTResourceManager.getColor(SWT.COLOR_YELLOW);
        colors[WaveformColors.SIGNAL_TEXT.ordinal()] = SWTResourceManager.getColor(SWT.COLOR_WHITE);
        colors[WaveformColors.SIGNAL_REAL.ordinal()] = SWTResourceManager.getColor(SWT.COLOR_YELLOW);
        colors[WaveformColors.SIGNAL_NAN.ordinal()] = SWTResourceManager.getColor(SWT.COLOR_RED);
        colors[WaveformColors.CURSOR.ordinal()] = SWTResourceManager.getColor(SWT.COLOR_RED);
        colors[WaveformColors.CURSOR_DRAG.ordinal()] = SWTResourceManager.getColor(SWT.COLOR_GRAY);
        colors[WaveformColors.CURSOR_TEXT.ordinal()] = SWTResourceManager.getColor(SWT.COLOR_WHITE);
        colors[WaveformColors.MARKER.ordinal()] = SWTResourceManager.getColor(SWT.COLOR_DARK_GRAY);
        colors[WaveformColors.MARKER_TEXT.ordinal()] = SWTResourceManager.getColor(SWT.COLOR_WHITE);
        colors[WaveformColors.REL_ARROW.ordinal()] = SWTResourceManager.getColor(SWT.COLOR_MAGENTA);
        colors[WaveformColors.REL_ARROW_HIGHLITE.ordinal()] = SWTResourceManager.getColor(255, 128, 255);
		randomColors = new Color[][] {
				{ SWTResourceManager.getColor( 170,  66,  37 ), SWTResourceManager.getColor( 190,  66,  37 ) },
				{ SWTResourceManager.getColor(  96,  74, 110 ), SWTResourceManager.getColor(  96,  74, 130 ) },
				{ SWTResourceManager.getColor( 133, 105, 128 ), SWTResourceManager.getColor( 153, 105, 128 ) },
				{ SWTResourceManager.getColor(   0, 126, 135 ), SWTResourceManager.getColor(   0, 126, 155 ) },
				{ SWTResourceManager.getColor( 243, 146,  75 ), SWTResourceManager.getColor( 255, 146,  75 ) },
				{ SWTResourceManager.getColor( 206, 135, 163 ), SWTResourceManager.getColor( 226, 135, 163 ) },
				{ SWTResourceManager.getColor( 124, 103,  74 ), SWTResourceManager.getColor( 144, 103,  74 ) },
				{ SWTResourceManager.getColor( 194, 187, 169 ), SWTResourceManager.getColor( 214, 187, 169 ) },
				{ SWTResourceManager.getColor( 104,  73,  71 ), SWTResourceManager.getColor( 124,  73,  71 ) },
				{ SWTResourceManager.getColor(  75, 196, 213 ), SWTResourceManager.getColor(  75, 196, 233 ) },
				{ SWTResourceManager.getColor( 206, 232, 229 ), SWTResourceManager.getColor( 206, 252, 229 ) },
				{ SWTResourceManager.getColor( 169, 221, 199 ), SWTResourceManager.getColor( 169, 241, 199 ) },
				{ SWTResourceManager.getColor( 100, 165, 197 ), SWTResourceManager.getColor( 100, 165, 217 ) },
				{ SWTResourceManager.getColor( 150, 147, 178 ), SWTResourceManager.getColor( 150, 147, 198 ) },
				{ SWTResourceManager.getColor( 200, 222, 182 ), SWTResourceManager.getColor( 200, 242, 182 ) },
				{ SWTResourceManager.getColor( 147, 208, 197 ), SWTResourceManager.getColor( 147, 228, 197 ) }
		};
	}
	
	public WaveformStyleProvider(IEclipsePreferences store) {
		setupDefaults();
		Preferences defaultPrefs= store.parent().parent().node("/"+DefaultScope.SCOPE+"/"+PreferenceConstants.PREFERENCES_SCOPE);
		HashMap<WaveformColors, RGB> colorPref = new HashMap<>();
		for (WaveformColors c : WaveformColors.values()) {
			String key = c.name() + "_COLOR";
			String prefValue = store.get(key, defaultPrefs.get(key,  "")); //$NON-NLS-1$
			RGB rgb = StringConverter.asRGB(prefValue);
			colorPref.put(c, rgb);
		}
		trackHeigth = store.getInt(PreferenceConstants.TRACK_HEIGHT, defaultPrefs.getInt(PreferenceConstants.TRACK_HEIGHT,  25)); //$NON-NLS-1$
	}	
	/** 
	 * needs redraw() afterwards
	 * @param colourMap
	 */
    public void initColors(HashMap<WaveformColors, RGB> colourMap) {
        Display d = parent.getDisplay();
        if (colourMap != null) {
            for (WaveformColors c : WaveformColors.values()) {
                if (colourMap.containsKey(c))
                    colors[c.ordinal()] = new Color(d, colourMap.get(c));
            }
        }
    }

	@Override
	public Font getNameFont() {
		return nameFont;
	}

	@Override
	public Font getNameFontHighlite() {
		return nameFont;
	}

	@Override
	public int getTrackHeight() {
		return trackHeigth;
	}
	@Override
	public Color getColor(WaveformColors type) {
		return colors[type.ordinal()];
	}

	@Override
	public Color[] computeColor (String streamValue) {
		Color[] result = new Color[] {SWTResourceManager.getColor( 200,0,0), SWTResourceManager.getColor( 255,0,0)};
		// assign "random" color here, one name always results in the same color!
		if( streamValue!=null && randomColors.length > 0 ) {
			int index = Math.abs(streamValue.hashCode()) % randomColors.length;
			result[0] = randomColors[index][0];
			result[1] = randomColors[index][1];
		}
		return result;
	}
	
}

package ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * Creates and caches colors for SWT.
 */
public class ColorManager {
	private Display display;
	private Map<RGB, Color> cache = new HashMap<RGB, Color>();

	/**
	 * Create a new ColorManager for a display.
	 */
	public ColorManager(Display display) {
		this.display = display;
	}
	
	/**
	 * Dispose of cached colors.
	 */
	public void dispose() {
		for(Color color:cache.values()) {
			color.dispose();
		}
		cache.clear();
	}

	/**
	 * Returns a color from a hex code as in the form: "rrggbb".
	 */
	public Color getHexColor(String color) {
		int[] v = new int[3];
		for(int x = 0; x < 3; x++) {
			String part = color.substring(x*2, x*2+2);
			v[x] = Integer.parseInt(part, 16);
		}
		return getColor(v[0], v[1], v[2]);
	}

	/**
	 * Returns a color from r, g, b values from 0-255.
	 */
	public Color getColor(int r, int g, int b) {
		RGB rgb = new RGB(r, g, b);
		if(cache.containsKey(rgb)) {
			return cache.get(rgb);
		} else {
			Color color = new Color(display, r, g, b);
			cache.put(rgb, color);
			return color;
		}
	}
}

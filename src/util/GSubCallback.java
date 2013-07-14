package util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Does a replaceAll, but with a callback to get the replacement text.
 */
public class GSubCallback {
	public interface Callback {
		String call(Matcher m);
	}

	public static String replaceAll(String text, String pattern, Callback callback) {
		Matcher m = Pattern.compile(pattern).matcher(text);

		StringBuffer s = new StringBuffer();

		while(m.find()) {
			String r = callback.call(m);
			if(r == null) return null;
			m.appendReplacement(s, r);
		}
		
		m.appendTail(s);

		return s.toString();
	}
}
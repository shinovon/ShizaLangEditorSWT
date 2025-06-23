import java.util.Vector;

/**
 * @author Shinovon
 *
 */
public class StringUtils {

	public static String replace(String str, String from, String to) {
		int j = str.indexOf(from);
		if (j == -1)
			return str;
		final StringBuffer sb = new StringBuffer();
		int k = 0;
		for (int i = from.length(); j != -1; j = str.indexOf(from, k)) {
			sb.append(str.substring(k, j)).append(to);
			k = j + i;
		}
		sb.append(str.substring(k, str.length()));
		return sb.toString();
	}

	public static boolean contains(String x, String find) {
		return x.indexOf(find) > -1;
	}
	
	public static String[] split(String str, char d) {
		int i = str.indexOf(d);
		if(i == -1)
			return new String[] {str};
		Vector v = new Vector();
		v.addElement(str.substring(0, i));
		while(i != -1) {
			str = str.substring(i + 1);
			if((i = str.indexOf(d)) != -1)
				v.addElement(str.substring(0, i));
			i = str.indexOf(d);
		}
		v.addElement(str);
		String[] r = new String[v.size()];
		v.copyInto(r);
		return r;
	}
	
	public static String[] split(String str, String d) {
		int i = str.indexOf(d);
		if(i == -1)
			return new String[] {str};
		Vector v = new Vector();
		v.addElement(str.substring(0, i));
		while(i != -1) {
			str = str.substring(i + d.length());
			if((i = str.indexOf(d)) != -1)
				v.addElement(str.substring(0, i));
			i = str.indexOf(d);
		}
		v.addElement(str);
		String[] r = new String[v.size()];
		v.copyInto(r);
		return r;
	}
 
	public static int count(String in, char t) {
		int r = 0;
		char[] c = in.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] == t) {
				r++;
			}
		}
		return r;
	}
	 
	public static int count(String str, String f) {
		int i = str.indexOf(f);
		int c = 0;
		while(i != -1) {
			str = str.substring(i + f.length());
			c++;
			i = str.indexOf(f);
		}
		return c;
	}
	
	public static String[] splitSingle(String str, String d) {
		int i = str.indexOf(d);
		return new String[] { str.substring(0, i), str.substring(i + d.length()) };
	}
	
	public static String[] splitSingle(String str, char d) {
		int i = str.indexOf(d);
		return new String[] { str.substring(0, i), str.substring(i + 1) };
	}

}

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import jazzlib.ZipEntry;
import jazzlib.ZipInputStream;

public class ShizaLocale {
	
	//protected static Hashtable map;
	static Vector keys;
	static Vector values;
	static String name;
	static String authors;
	static String vk;
	private static int emptyCount;
	
	public static void init() {
		//map = new Hashtable();
		keys = new Vector();
		values = new Vector();
		name = "English";
		authors = "";
		vk = "vk";
	}
	
	public static void load(String loc) throws IOException {
		if(loc == null)
			return;
		if(loc.indexOf('\\') != -1)
			loc = StringUtils.replace(loc, "\\", "/");
		if(!loc.startsWith("file://"))
			loc = "file:///" +loc;
		FileConnection f = (FileConnection) Connector.open(loc);
		InputStream is = f.openInputStream();
		load(is);
		f.close();
	}
	
	static void loadKeys(String loc) throws IOException {
		if(loc == null)
			return;
		if(loc.indexOf('\\') != -1)
			loc = StringUtils.replace(loc, "\\", "/");
		if(!loc.startsWith("file://"))
			loc = "file:///" +loc;
		FileConnection f = (FileConnection) Connector.open(loc);
		InputStream is = null;
		try {
			is = f.openInputStream();
			Reader r = new InputStreamReader(is, "UTF-8");
			char[] chars = new char[96 * 1024];
			int slen = r.read(chars);
			r.close();
			is.close();
			String x = "";
			boolean iscomment = false;
			for (int i = 0; i < slen; i++) {
				final char c = chars[i];
	
				if (c == 0) {
					break;
				}
	
				if (x.length() == 0 && c == '#') {
					iscomment = true;
				}
	
				if (c == '\n') {
					if (!iscomment && x != null && x.length() > 2) {
						if(x.startsWith("langinfo")) {
							if(x.startsWith("langinfo.name"))
								name = StringUtils.split(x, "=")[1];
						} else {
							int splitLoc = x.indexOf("=");
							int len = x.length();
							String key = x.substring(0, splitLoc);
							String val = StringUtils.replace(StringUtils.replace(x.substring(splitLoc + 1, len), "\r", ""), "|", "\n");
							set(key, val);
						}
					}
					iscomment = false;
					x = "";
				} else {
					x += String.valueOf(c);
				}
			}
			x = null;
		} finally {
			try {
				is.close();
			} catch (Exception e) {
			}
		}
		try {
			f.close();
		} catch (Exception e) {
		}
	}

	static void load(String loc, String l) throws IOException {
		if(loc == null)
			return;
		if(loc.indexOf('\\') != -1)
			loc = StringUtils.replace(loc, "\\", "/");
		if(!loc.startsWith("file://"))
			loc = "file:///" +loc;
		FileConnection fc = (FileConnection) Connector.open(loc);
		load(fc.openInputStream(), l);
		try {
			fc.close();
		} catch (Exception e) {
		}
	}
	
	static void load(InputStream is, String lang) throws IOException {
		load(new ZipInputStream(is), lang);
	}
	
	static void load(ZipInputStream zis, String lang) throws IOException {
		ZipEntry en = zis.getNextEntry();
		while(en != null) {
			if(!en.isDirectory() && en.getName().startsWith("langs") && en.getName().endsWith(lang + ".txt")) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				int len = 0;
			    byte[] buf = new byte[2048];
                while ((len = zis.read(buf)) > 0)
                {
                	baos.write(buf, 0, len);
                }
				zis.close();
				byte[] b = baos.toByteArray();
				baos.close();
				load(new ByteArrayInputStream(b));
				return;
			} else {
				en = zis.getNextEntry();
				continue;
			}
		}
		throw new IOException("NOT FOUND!!!");
	}
	
	static void load(InputStream is) throws IOException {
		//map = new Hashtable();
		keys = new Vector();
		values = new Vector();
		Reader r = new InputStreamReader(is, "UTF-8");
		char[] chars = new char[96 * 1024];
		int slen = r.read(chars);
		r.close();
		is.close();
		String x = "";
		boolean iscomment = false;
		for (int i = 0; i < slen; i++) {
			final char c = chars[i];

			if (c == 0) {
				break;
			}

			if (x.length() == 0 && c == '#') {
				iscomment = true;
			}

			if (c == '\n') {
				if (!iscomment && x != null && x.length() > 2) {
					if(x.startsWith("langinfo")) {
						if(x.startsWith("langinfo.name"))
							name = StringUtils.split(x, "=")[1];
					} else {
						int splitLoc = x.indexOf("=");
						int len = x.length();
						String key = x.substring(0, splitLoc);
						String val = StringUtils.replace(StringUtils.replace(x.substring(splitLoc + 1, len), "\r", ""), "|", "\n");
						//map.put(key, val);
						keys.addElement(key);
						values.addElement(val);
					}
				} else {
					if(emptyCount > 10) {
						//map.put("empty" + emptyCount, x);
						keys.addElement("empty" + emptyCount);
						values.addElement(x);
					}
					emptyCount++;
				}
				iscomment = false;
				x = "";
			} else {
				x += String.valueOf(c);
			}
		}
		x = null;
	}

	public static void save(String loc) throws IOException {
		if(loc == null)
			return;
		if(loc.indexOf('\\') != -1)
			loc = StringUtils.replace(loc, "\\", "/");
		if(!loc.startsWith("file://"))
			loc = "file:///" +loc;
		FileConnection f = (FileConnection) Connector.open(loc);
		if(f.exists())
			f.delete();
		f.create();
		String s = makeString();
		OutputStream os = f.openOutputStream();
		os.write(s.getBytes("UTF-8"));
		os.close();
		try {
			f.close();
		} catch (Exception e) {
		}
	}
	
	public static String makeString() {
		String date = getDate();
		String s = "";
		s += "#" + name + "\n";
		s += "#\n";
		s += "#authors: " + authors + "\n";
		s += "#\n";
		s += "#authors' vk:\n";
		s += "#" + vk + "\n";
		s += "#\n";
		s += "\n";
		s += "langinfo.name=" + name + "\n";
		s += "#YYYY.MM.DD\n";
		s += "langinfo.version=" + date + "\n";
		s += "\n";
		s += "langinfo.hints=1\n";
		s += "\n";
		for(int i = 0; i < keys.size(); ) {
			String k = (String) keys.elementAt(i);
			String v = (String) values.elementAt(i);
			if(k.startsWith("empty")) {
				s += v + "\n";
				i++;
				continue;
			}
			s += k + "=" + StringUtils.replace(v, "\n", "|") + "\n";
			i++;
			continue;
		}
		
		return s;
	}

	private static String getDate() {
		Date date = new Date();
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		String y = "" + c.get(Calendar.YEAR);
		String m = "" + (c.get(Calendar.MONTH) + 1);
		String d = "" + c.get(Calendar.DATE);
		return y + "." + m + "." + d;
	}

	public static void set(String k, String v) {
		//map.put(k, v);
		int i = 0;
		boolean found = false;
		while(keys.size() < i) {
			if(!keys.elementAt(i).equals(k))
				i++;
			else {
				found = true;
				break;
			}
		}
		if(found)
			values.setElementAt(v, i);
	}

	public static String get(String x) {
		int i = keys.indexOf(x);
		return i == -1 ? x : (String) values.elementAt(i);
		//return (String) (map.get(x) == null ? x : map.get(x));
	}

}

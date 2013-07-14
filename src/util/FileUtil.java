package util;

import java.io.*;

public class FileUtil {
	public static String readFile(Reader reader) throws IOException {
		StringBuilder text = new StringBuilder();
		BufferedReader bufferedReader = null;
		try {
			boolean first = true;
			
			bufferedReader = new BufferedReader(reader);
			String line;
			while((line = bufferedReader.readLine()) != null) {
				if(first) {
					first = false;
				} else {
					text.append(System.getProperty("line.separator"));
				}
				
				text.append(line);
			}
		} finally {
			close(reader);
		}
		
		return text.toString();
	}
	
	public static String readFile(String path) throws IOException {
		return readFile(new FileReader(new File(path)));
	}
	
	public static String readResource(String path) throws IOException {
		return readFile(new InputStreamReader(FileUtil.class.getResourceAsStream(path)));
	}

	public static void writeFile(String location, String text) throws IOException {
		File file = new File(location);
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		
		try {
			writer.write(text);
		} finally {
			close(writer);
		}
	}
	
	public static void close(Closeable closeable) {
		try {
			if(closeable != null) {
				closeable.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

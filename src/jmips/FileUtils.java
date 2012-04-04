package jmips;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileUtils {
	public static ByteBuffer readFile(String fileName) {
		ByteBuffer bb = null;
		FileChannel fc = null;
		try {
			FileInputStream fis = new FileInputStream(fileName);
			fc = fis.getChannel();
			int size = (int) fc.size();
			bb = ByteBuffer.allocate(size);
			if (fc.read(bb) != fc.size())
				return null;
			bb.rewind();
		} catch(FileNotFoundException ex) {
			System.err.printf("File `%s' not found!\n", fileName);
		} catch(IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (fc != null) fc.close();
			} catch(IOException ex) {
				ex.printStackTrace();
			}
		}
		return bb;
	}

}

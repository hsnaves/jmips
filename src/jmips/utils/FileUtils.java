package jmips.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileUtils {
	public static ByteBuffer readFile(String fileName) {
		ByteBuffer buf = null;
		FileChannel fc = null;
		try {
			FileInputStream fis = new FileInputStream(fileName);
			fc = fis.getChannel();
			int size = (int) fc.size();
			buf = readFileFragment(fc, 0, size);
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
		return buf;
	}

	public static ByteBuffer readFileFragment(FileChannel fc, long pos, int size) {
		ByteBuffer fragment = null;
		try {
			fc.position(pos);
			fragment = ByteBuffer.allocate(size);
			if (fc.read(fragment) != size)
				return null;
			fragment.rewind();
		} catch(IOException ex) {
			ex.printStackTrace();
			return null;
		}
		return fragment;
	}

	public static boolean writeFileFragment(FileChannel fc, long pos, ByteBuffer fragment) {
		try {
			fc.position(pos);
			int size = fragment.remaining();
			if (fc.write(fragment) != size)
				return false;
			fc.force(false);
		} catch(IOException ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}

}

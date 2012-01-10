package jmips.serial;

import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.JFrame;

public class SwingTTYFrame extends JFrame {
	private static final long serialVersionUID = -316995381999163483L;

	private SwingTTY tty;

	public SwingTTYFrame() {
		setTitle("SwingTTY");
		tty = new SwingTTY();
		getContentPane().add(tty);

		pack();
		tty.open();
		tty.setEchoEnabled(true);
	}

	public static void main(String[] args) {
		SwingTTYFrame t = new SwingTTYFrame();
		t.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		t.setVisible(true);
		try {
			ProcessBuilder pb = new ProcessBuilder("/bin/sh");
			pb.redirectErrorStream(true);
			Process p = pb.start();
			OutputStream in = p.getOutputStream();
			InputStream out = p.getInputStream();
			while(true) {
				boolean shouldSleep = true;
				while (out.available() > 0) {
					t.tty.write((byte) out.read());
					shouldSleep = false;
				}
				while (t.tty.available()) {
					in.write(t.tty.read());
					in.flush();
					shouldSleep = false;
				}
				if (shouldSleep) Thread.sleep(10);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}

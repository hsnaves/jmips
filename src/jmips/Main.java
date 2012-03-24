package jmips;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JFrame;

import jmips.serial.SwingTTY;

public class Main {
	private static final int BASE_ADDRESS = 0x80100000;

	private static JFrame createConsoleFrame() {
		JFrame frame = new JFrame("JMIPS");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		return frame;
	}

	private static SwingTTY createSwingTTY(JFrame frame) {
		SwingTTY tty = new SwingTTY();
		frame.getContentPane().add(tty);

		frame.pack();
		tty.open();
		return tty;
	}

	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
		final JFrame frame = createConsoleFrame();
		final SwingTTY tty = createSwingTTY(frame);
		final MipsSystem system = new MipsSystem(tty);
		frame.setVisible(true);

		int initrdAddress = system.load(BASE_ADDRESS, new FileInputStream("vmlinux.bin"));
		initrdAddress = (initrdAddress + 128 * 4096) & ~4095;
		int size = system.load(initrdAddress, new FileInputStream("initrd.gz")) - initrdAddress;

		system.reset(BASE_ADDRESS);
		String cmdLine = String.format("rd_start=0x%08X rd_size=%d", initrdAddress, size);
		system.setKernelCommandLine(cmdLine, initrdAddress + size);
		//GdbServer server = new GdbServer(cpu, tty);
		//server.startServer(1234);
		//return;

		while(true) {
			system.step(4000);
		}
	}


}

package jmips;

import javax.swing.JFrame;
import jmips.serial.SwingTTY;

public class Main {
	private static JFrame createConsoleFrame() {
		JFrame frame = new JFrame("Jmips");
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

	public static void main(String[] args) {
		final JFrame frame = createConsoleFrame();
		final SwingTTY tty = createSwingTTY(frame);
		final MipsSystem system = new MipsSystem(tty);
		frame.setVisible(true);

		String kernelFileName = null;
		String initrdFileName = null;
		String diskFileName = null;
		boolean stop = false;

		if (args.length == 0) {
			kernelFileName = "vmlinux";
			initrdFileName = "initrd.gz";
			diskFileName = "disk.raw";
		} else {
			kernelFileName = args[0];
			if (kernelFileName.startsWith("-")) {
				stop = true;
				kernelFileName = kernelFileName.substring(1);
			}
			if (args.length > 1)
				initrdFileName = args[1];
			if (args.length > 2)
				diskFileName = args[2];
		}

		int initrdAddress = system.loadElf32(kernelFileName);
		if (initrdAddress == 0) {
			System.err.println("Can't load kernel!");
			System.exit(1);
		}

		initrdAddress = (initrdAddress + 128 * 4096) & ~4095;
		if (initrdFileName != null) {
			int lastAddress = system.load(initrdAddress, initrdFileName);
			if (lastAddress == 0) {
				System.err.println("Can't load initrd!");
				System.exit(1);
			}

			int size = lastAddress - initrdAddress;
			String cmdLine = String.format("rd_start=0x%08X rd_size=%d", initrdAddress, size);
			system.setKernelCommandLine(cmdLine, initrdAddress + size);
		} else {
			diskFileName = "disk.raw";
			system.setKernelCommandLine("root=/dev/sb1", initrdAddress);
		}

		if (diskFileName != null) {
			if (!system.setDiskFile(diskFileName)) {
				System.err.println("Can't use disk!");
				System.exit(1);
			}
		}

		system.reset();
		GdbStub stub = new GdbStub(system);
		stub.runServer(1234, stop);

		System.exit(1);
	}


}

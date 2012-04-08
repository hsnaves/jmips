package jmips.cpu;

import static org.junit.Assert.*;
import jmips.MipsSystem;
import jmips.serial.DummyTTY;

public class PerformanceTest {
	private long time;

	private void startTimer() {
		time = System.currentTimeMillis();
	}
	private long stopTimer() {
		return System.currentTimeMillis() - time;
	}

	private MipsSystem createMipsSytem() {
		DummyTTY tty = new DummyTTY();
		MipsSystem system = new MipsSystem(tty);
		int loadAddress = system.loadElf32("vmlinux");
		assertTrue(loadAddress != 0);
		assertTrue(system.setDiskFile("disk.raw"));
		system.setKernelCommandLine("root=/dev/sb1", loadAddress);
		system.reset();
		return system;
	}

	private void doLogin(MipsSystem system) {
		DummyTTY tty = (DummyTTY) system.getTTY();

		startTimer();
		for(int i = 0; i < 41000000; i++)
			system.step(1);
		tty.addInputString("root\n");
		for(int i = 0; i < 1000000; i++)
			system.step(1);
		long time = stopTimer();
		time = System.currentTimeMillis() - time;
		System.out.println("Login in time: " + time + "ms");
	}

	private void testRunLinuxAndPython() {
		MipsSystem system = createMipsSytem();
		DummyTTY tty = (DummyTTY) system.getTTY();
		doLogin(system);

		tty.addInputString("cd /\npython\n");

		startTimer();
		for(int i = 0; i < 100000000; i++)
			system.step(1);
		long time = stopTimer();

		//byte[] out = tty.getOutputBytes();
		//System.out.println(new String(out));
		System.out.println("Python in time: " + time + "ms");
	}

	private void testRunLinuxAndWhetstone() {
		MipsSystem system = createMipsSytem();
		DummyTTY tty = (DummyTTY) system.getTTY();
		doLogin(system);

		tty.addInputString("cd /\nwhetstone\n");
		startTimer();
		for(int i = 0; i < 100000000; i++)
			system.step(1);
		long time = stopTimer();

		System.out.println("Whetstone in time: " + time + "ms");
	}

	public static void main(String[] args) {
		PerformanceTest test = new PerformanceTest();
		test.testRunLinuxAndPython();
		test.testRunLinuxAndWhetstone();
	}

}

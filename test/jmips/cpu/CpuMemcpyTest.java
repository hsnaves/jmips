package jmips.cpu;

public class CpuMemcpyTest {
	private static final int BASE_ADDRESS = 0x80100000;
	private static final int RAM_SIZE = 4 * 1024 * 1024;
	private static final int COPY_SIZE = 256 * 1024;
	private static final int REPEAT_TIMES = 3000;


	private Cpu createCpu() {
		Cpu cpu = new Cpu(RAM_SIZE, null);
		reset(cpu);
		return cpu;
	}

	private void reset(Cpu cpu) {
		cpu.reset();
		cpu.setPc(BASE_ADDRESS);
	}

	private void testMemcpyJava() {
		int[] buffer = new int[RAM_SIZE / 4];
		for(int i = 0; i < REPEAT_TIMES; i++)
			for (int j = 0; j < COPY_SIZE; j++) {
				buffer[COPY_SIZE + j] = buffer[j];
			}
	}

	private void testMemcpyJavaArraycopy() {
		int[] buffer = new int[RAM_SIZE / 4];
		for(int i = 0; i < REPEAT_TIMES; i++)
			System.arraycopy(buffer, 0, buffer, COPY_SIZE, COPY_SIZE);
	}

	private void testMemcpyRam(Cpu cpu) {
		Ram ram = cpu.getRam();
		for(int i = 0; i < REPEAT_TIMES; i++)
			for (int j = 0; j < COPY_SIZE; j++) {
				ram.write32(4 * (COPY_SIZE + j), ram.read32(4 * j));
			}
	}

	private void testMemcpyCpuLoadStore(Cpu cpu) {
		for(int i = 0; i < REPEAT_TIMES; i++)
			for (int j = 0; j < COPY_SIZE; j++) {
				cpu.store32(0x80000000 + 4 * (COPY_SIZE + j), cpu.load32(0x80000000 + 4 * j));
			}
	}

	private void testMemcpyCpuReadWrite(Cpu cpu) {
		for(int i = 0; i < REPEAT_TIMES; i++)
			for (int j = 0; j < COPY_SIZE; j++) {
				cpu.write32(0x80000000 + 4 * (COPY_SIZE + j), cpu.read32(0x80000000 + 4 * j));
			}
	}

	private void testMemcpyCpuSimulatedFast(Cpu cpu) {
		for(int i = 0; i < REPEAT_TIMES; i++) {
			reset(cpu);
			cpu.setGpr(Mips.GPR_A0, 0x80000000);
			cpu.setGpr(Mips.GPR_A1, 0x80010000 + 4 * COPY_SIZE);
			cpu.setGpr(Mips.GPR_A2, COPY_SIZE);
			while(cpu.getGpr(Mips.GPR_A2) != 0) {
				cpu.setGpr(Mips.GPR_T0, cpu.read32(cpu.getGpr(Mips.GPR_A0)));
				cpu.setGpr(Mips.GPR_A0, cpu.getGpr(Mips.GPR_A0) + 4);
				cpu.write32(cpu.getGpr(Mips.GPR_A1),  cpu.getGpr(Mips.GPR_T0));
				cpu.setGpr(Mips.GPR_A1, cpu.getGpr(Mips.GPR_A1) + 4);
				cpu.setGpr(Mips.GPR_A2, cpu.getGpr(Mips.GPR_A2) - 1);
			}
		}
	}

	private void testMemcpyCpuSimulatedFastWithWhileTrue(Cpu cpu) {
		for(int i = 0; i < REPEAT_TIMES; i++) {
			reset(cpu);
			cpu.setGpr(Mips.GPR_A0, 0x80000000);
			cpu.setGpr(Mips.GPR_A1, 0x80010000 + 4 * COPY_SIZE);
			cpu.setGpr(Mips.GPR_A2, COPY_SIZE);
			while(true) {
				cpu.setGpr(Mips.GPR_T0, cpu.read32(cpu.getGpr(Mips.GPR_A0)));
				cpu.setGpr(Mips.GPR_A0, cpu.getGpr(Mips.GPR_A0) + 4);
				cpu.write32(cpu.getGpr(Mips.GPR_A1),  cpu.getGpr(Mips.GPR_T0));
				cpu.setGpr(Mips.GPR_A1, cpu.getGpr(Mips.GPR_A1) + 4);
				cpu.setGpr(Mips.GPR_A2, cpu.getGpr(Mips.GPR_A2) - 1);
				if (cpu.getGpr(Mips.GPR_A2) == 0) break;
			}
		}
	}

	private void testMemcpyCpuSimulated(Cpu cpu) {
		for(int i = 0; i < REPEAT_TIMES; i++) {
			reset(cpu);
			cpu.setGpr(Mips.GPR_A0, 0x80000000);
			cpu.setGpr(Mips.GPR_A1, 0x80010000 + 4 * COPY_SIZE);
			cpu.setGpr(Mips.GPR_A2, COPY_SIZE);
			for(int j = 0; j < COPY_SIZE; j++) {
				cpu.lw(Mips.ENCODE_LW(Mips.GPR_T0, Mips.GPR_A0, 0));
				cpu.addiu(Mips.ENCODE_ADDIU(Mips.GPR_A0, Mips.GPR_A0, 4));
				cpu.sw(Mips.ENCODE_SW(Mips.GPR_T0, Mips.GPR_A1, 0));
				cpu.addiu(Mips.ENCODE_ADDIU(Mips.GPR_A1, Mips.GPR_A1, 4));
				cpu.addiu(Mips.ENCODE_ADDIU(Mips.GPR_A2, Mips.GPR_A2, -1));
				cpu.bne(Mips.ENCODE_BNE(Mips.GPR_A2, Mips.GPR_ZR, BASE_ADDRESS, BASE_ADDRESS + 20));
				cpu.sll(Mips.ENCODE_SLL(0, 0, 0));
			}
		}
	}

	private void testMemcpyCpuInterpretedFast(Cpu cpu) {
		cpu.store32(BASE_ADDRESS, Mips.ENCODE_LW(Mips.GPR_T0, Mips.GPR_A0, 0));
		cpu.store32(BASE_ADDRESS + 4, Mips.ENCODE_ADDIU(Mips.GPR_A0, Mips.GPR_A0, 4));
		cpu.store32(BASE_ADDRESS + 8, Mips.ENCODE_SW(Mips.GPR_T0, Mips.GPR_A1, 0));
		cpu.store32(BASE_ADDRESS + 12, Mips.ENCODE_ADDIU(Mips.GPR_A1, Mips.GPR_A1, 4));
		cpu.store32(BASE_ADDRESS + 16, Mips.ENCODE_ADDIU(Mips.GPR_A2, Mips.GPR_A2, -1));
		cpu.store32(BASE_ADDRESS + 20, Mips.ENCODE_BNE(Mips.GPR_A2, Mips.GPR_ZR, BASE_ADDRESS, BASE_ADDRESS + 20));
		cpu.store32(BASE_ADDRESS + 24, Mips.ENCODE_SLL(0, 0, 0));
		cpu.store32(BASE_ADDRESS + 28, Mips.ENCODE_WAIT(0));
		cpu.store32(BASE_ADDRESS + 32, Mips.ENCODE_J(BASE_ADDRESS + 28, BASE_ADDRESS + 32));
		cpu.store32(BASE_ADDRESS + 36, Mips.ENCODE_SLL(0, 0, 0));

		for(int i = 0; i < REPEAT_TIMES; i++) {
			reset(cpu);
			cpu.setGpr(Mips.GPR_A0, 0x80000000);
			cpu.setGpr(Mips.GPR_A1, 0x80010000 + 4 * COPY_SIZE);
			cpu.setGpr(Mips.GPR_A2, COPY_SIZE);
			cpu.step(7 * COPY_SIZE + 2);
		}
	}

	private void testMemcpyCpuInterpreted(Cpu cpu) {
		cpu.store32(BASE_ADDRESS, Mips.ENCODE_LW(Mips.GPR_T0, Mips.GPR_A0, 0));
		cpu.store32(BASE_ADDRESS + 4, Mips.ENCODE_ADDIU(Mips.GPR_A0, Mips.GPR_A0, 4));
		cpu.store32(BASE_ADDRESS + 8, Mips.ENCODE_SW(Mips.GPR_T0, Mips.GPR_A1, 0));
		cpu.store32(BASE_ADDRESS + 12, Mips.ENCODE_ADDIU(Mips.GPR_A1, Mips.GPR_A1, 4));
		cpu.store32(BASE_ADDRESS + 16, Mips.ENCODE_ADDIU(Mips.GPR_A2, Mips.GPR_A2, -1));
		cpu.store32(BASE_ADDRESS + 20, Mips.ENCODE_BNE(Mips.GPR_A2, Mips.GPR_ZR, BASE_ADDRESS, BASE_ADDRESS + 20));
		cpu.store32(BASE_ADDRESS + 24, Mips.ENCODE_SLL(0, 0, 0));
		cpu.store32(BASE_ADDRESS + 28, Mips.ENCODE_WAIT(0));
		cpu.store32(BASE_ADDRESS + 32, Mips.ENCODE_J(BASE_ADDRESS + 28, BASE_ADDRESS + 32));
		cpu.store32(BASE_ADDRESS + 36, Mips.ENCODE_SLL(0, 0, 0));

		for(int i = 0; i < REPEAT_TIMES; i++) {
			reset(cpu);
			cpu.setGpr(Mips.GPR_A0, 0x80000000);
			cpu.setGpr(Mips.GPR_A1, 0x80010000 + 4 * COPY_SIZE);
			cpu.setGpr(Mips.GPR_A2, COPY_SIZE);
			while(!cpu.isHalted())
				cpu.step(1);
		}
	}

	private void testMemcpyCpuInterpretedWithWhileTrue(Cpu cpu) {
		cpu.store32(BASE_ADDRESS, Mips.ENCODE_LW(Mips.GPR_T0, Mips.GPR_A0, 0));
		cpu.store32(BASE_ADDRESS + 4, Mips.ENCODE_ADDIU(Mips.GPR_A0, Mips.GPR_A0, 4));
		cpu.store32(BASE_ADDRESS + 8, Mips.ENCODE_SW(Mips.GPR_T0, Mips.GPR_A1, 0));
		cpu.store32(BASE_ADDRESS + 12, Mips.ENCODE_ADDIU(Mips.GPR_A1, Mips.GPR_A1, 4));
		cpu.store32(BASE_ADDRESS + 16, Mips.ENCODE_ADDIU(Mips.GPR_A2, Mips.GPR_A2, -1));
		cpu.store32(BASE_ADDRESS + 20, Mips.ENCODE_BNE(Mips.GPR_A2, Mips.GPR_ZR, BASE_ADDRESS, BASE_ADDRESS + 20));
		cpu.store32(BASE_ADDRESS + 24, Mips.ENCODE_SLL(0, 0, 0));
		cpu.store32(BASE_ADDRESS + 28, Mips.ENCODE_WAIT(0));
		cpu.store32(BASE_ADDRESS + 32, Mips.ENCODE_J(BASE_ADDRESS + 28, BASE_ADDRESS + 32));
		cpu.store32(BASE_ADDRESS + 36, Mips.ENCODE_SLL(0, 0, 0));

		for(int i = 0; i < REPEAT_TIMES; i++) {
			reset(cpu);
			cpu.setGpr(Mips.GPR_A0, 0x80000000);
			cpu.setGpr(Mips.GPR_A1, 0x80010000 + 4 * COPY_SIZE);
			cpu.setGpr(Mips.GPR_A2, COPY_SIZE);
			while(true) {
				cpu.step(1);
				if (cpu.isHalted()) break;
			}
		}
	}

	public static void main(String[] args) {
		CpuMemcpyTest test = new CpuMemcpyTest();
		Cpu cpu = test.createCpu();
		long time = System.currentTimeMillis();

		String testName = "MemcpyJava";
		if (args.length > 0) testName = args[0];
		if ("MemcpyJava".equals(testName)) {
			test.testMemcpyJava();
		} else if ("MemcpyJavaArraycopy".equals(testName)) {
			test.testMemcpyJavaArraycopy();
		} else if ("MemcpyRam".equals(testName)) {
			test.testMemcpyRam(cpu);
		} else if ("MemcpyCpuLoadStore".equals(testName)) {
			test.testMemcpyCpuLoadStore(cpu);
		} else if ("MemcpyCpuReadWrite".equals(testName)) {
			test.testMemcpyCpuReadWrite(cpu);
		} else if ("MemcpyCpuSimulatedFastWithWhileTrue".equals(testName)) {
			test.testMemcpyCpuSimulatedFastWithWhileTrue(cpu);
		} else if ("MemcpyCpuSimulatedFast".equals(testName)) {
			test.testMemcpyCpuSimulatedFast(cpu);
		} else if ("MemcpyCpuSimulated".equals(testName)) {
			test.testMemcpyCpuSimulated(cpu);
		} else if ("MemcpyCpuInterpretedFast".equals(testName)) {
			test.testMemcpyCpuInterpretedFast(cpu);
		} else if ("MemcpyCpuInterpreted".equals(testName)) {
			test.testMemcpyCpuInterpreted(cpu);
		} else if ("MemcpyCpuInterpretedWithWhileTrue".equals(testName)) {
			test.testMemcpyCpuInterpretedWithWhileTrue(cpu);
		}

		time = System.currentTimeMillis() - time;
		System.out.println("test" + testName + ": " + time + "ms");
	}
}


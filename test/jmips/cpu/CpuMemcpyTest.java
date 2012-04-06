package jmips.cpu;

import org.junit.Test;

public class CpuMemcpyTest {
	private static final int BASE_ADDRESS = 0x80100000;
	private static final int RAM_SIZE = 4 * 1024 * 1024;
	private static final int COPY_SIZE = 256 * 1024;
	private static final int REPEAT_TIMES = 1000;

	private Cpu createCpu() {
		Cpu cpu = new Cpu(RAM_SIZE, null);
		reset(cpu);
		return cpu;
	}

	private void reset(Cpu cpu) {
		cpu.reset();
		cpu.setPc(BASE_ADDRESS);
	}

	@Test
	public void testMemcpyJava() {
		int[] buffer = new int[RAM_SIZE / 4];
		for(int i = 0; i < REPEAT_TIMES; i++)
			for (int j = 0; j < COPY_SIZE; j++) {
				buffer[COPY_SIZE + j] = buffer[j];
			}
	}

	@Test
	public void testMemcpyJavaArraycopy() {
		int[] buffer = new int[RAM_SIZE / 4];
		for(int i = 0; i < REPEAT_TIMES; i++)
			System.arraycopy(buffer, 0, buffer, COPY_SIZE, COPY_SIZE);
	}

	@Test
	public void testMemcpyRam() {
		Cpu cpu = createCpu();
		Ram ram = cpu.getRam();
		for(int i = 0; i < REPEAT_TIMES; i++)
			for (int j = 0; j < COPY_SIZE; j++) {
				ram.write32(4 * (COPY_SIZE + j), ram.read32(4 * j));
			}
	}

	@Test
	public void testMemcpyCpuLoadStore() {
		Cpu cpu = createCpu();
		for(int i = 0; i < REPEAT_TIMES; i++)
			for (int j = 0; j < COPY_SIZE; j++) {
				cpu.store32(0x80000000 + 4 * (COPY_SIZE + j), cpu.load32(0x80000000 + 4 * j));
			}
	}

	@Test
	public void testMemcpyCpuReadWrite() {
		Cpu cpu = createCpu();
		for(int i = 0; i < REPEAT_TIMES; i++)
			for (int j = 0; j < COPY_SIZE; j++) {
				cpu.write32(0x80000000 + 4 * (COPY_SIZE + j), cpu.read32(0x80000000 + 4 * j));
			}
	}

	@Test
	public void testMemcpyCpuSimulatedFastWithFor() {
		Cpu cpu = createCpu();

		for(int i = 0; i < REPEAT_TIMES; i++) {
			reset(cpu);
			cpu.setGpr(Mips.GPR_A0, 0x80000000);
			cpu.setGpr(Mips.GPR_A1, 0x80010000 + 4 * COPY_SIZE);
			cpu.setGpr(Mips.GPR_A2, COPY_SIZE);
			for(int j = 0; j < COPY_SIZE; j++) {
				cpu.setGpr(Mips.GPR_T0, cpu.read32(cpu.getGpr(Mips.GPR_A0)));
				cpu.setGpr(Mips.GPR_A0, cpu.getGpr(Mips.GPR_A0) + 4);
				cpu.write32(cpu.getGpr(Mips.GPR_A1),  cpu.getGpr(Mips.GPR_T0));
				cpu.setGpr(Mips.GPR_A1, cpu.getGpr(Mips.GPR_A1) + 4);
				cpu.setGpr(Mips.GPR_A2, cpu.getGpr(Mips.GPR_A2) - 1);
			}
		}
	}

	@Test
	public void testMemcpyCpuSimulatedFast() {
		Cpu cpu = createCpu();

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

	@Test
	public void testMemcpyCpuSimulatedFastWithWhileTrue() {
		Cpu cpu = createCpu();

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

	@Test
	public void testMemcpyCpuSimulated() {
		Cpu cpu = createCpu();

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

	@Test
	public void testMemcpyCpuInterpretedFast() {
		Cpu cpu = createCpu();
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

	@Test
	public void testMemcpyCpuInterpreted() {
		Cpu cpu = createCpu();
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

	@Test
	public void testMemcpyCpuInterpretedWithWhileTrue() {
		Cpu cpu = createCpu();
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

}


package jmips.cpu;

public class Utils {
	private static final int[] leading_zeros = {
		8, 7, 6, 6, 5, 5, 5, 5, 4, 4, 4, 4, 4, 4, 4, 4,
		3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
		2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
	};

	private static final int[] leading_ones = {
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
		4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 6, 6, 7, 8
	};

	private static final int[] trailing_zeros = {
		8, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
		4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
		5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
		4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
		6, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
		4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
		5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
		4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
		7, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
		4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
		5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
		4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
		6, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
		4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
		5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
		4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
	};

	private static final int[] trailing_ones = {
		0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 4,
		0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 5,
		0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 4,
		0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 6,
		0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 4,
		0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 5,
		0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 4,
		0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 7,
		0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 4,
		0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 5,
		0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 4,
		0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 6,
		0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 4,
		0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 5,
		0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 4,
		0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 8
	};

	public static int I_OP(int opcode) {
		return (opcode >>> 26);
	}

	public static int I_FUNCT(int opcode) {
		return opcode & 0x3F;
	}

	public static int I_RS(int opcode) {
		return ((opcode >> 21) & 0x1F);
	}

	public static int I_RT(int opcode) {
		return ((opcode >> 16) & 0x1F);
	}

	public static int I_RD(int opcode) {
		return ((opcode >> 11) & 0x1F);
	}

	public static int I_SA(int opcode) {
		return ((opcode >> 6) & 0x1F);
	}

	public static int I_IMM16(int opcode) {
		return (int) ((short) opcode);
	}

	public static int I_IMM16U(int opcode) {
		return opcode & 0xFFFF;
	}

	public static int I_BRANCH(int opcode, int pc) {
		return pc + 4 + 4 * I_IMM16(opcode);
	}

	public static int I_JUMP(int opcode, int pc) {
		return ((pc & 0xF0000000) | ((opcode & 0x3FFFFFF) << 2));
	}

	public static int I_SYSCALLCODE(int opcode) {
		return ((opcode >> 6) & 0xFFFFF);
	}

	public static int I_TRAPCODE(int opcode) {
		return ((opcode >> 6) & 0x3FF);
	}

	public static int I_WAITCODE(int opcode) {
		return ((opcode >> 6) & 0x7FFFF);
	}

	public static int compare_unsigned(int a, int b) {
		if (a == b) return 0;
		if (a < 0) {
			if (b < 0) {
				return (a > b) ?  1 : -1;
			} else {
				return 1;
			}	
		} else {
			if (b >= 0) {
				return (a > b) ?  1 : -1;
			} else {
				return -1;
			}
		}
	}

	public static int count_leading_zeros(int n) {
		int k = leading_zeros[(n >> 24) & 0xFF];
		int m = k;
		if (k != 8) return m;

		k = leading_zeros[(n >> 16) & 0xFF];
		m += k;
		if (k != 8) return m;

		k = leading_zeros[(n >> 8) & 0xFF];
		m += k;
		if (k != 8) return m;

		k = leading_zeros[n & 0xFF];
		m += k;
		return m;
	}

	public static int count_leading_ones(int n) {
		int k = leading_ones[(n >> 24) & 0xFF];
		int m = k;
		if (k != 8) return m;

		k = leading_ones[(n >> 16) & 0xFF];
		m += k;
		if (k != 8) return m;

		k = leading_ones[(n >> 8) & 0xFF];
		m += k;
		if (k != 8) return m;

		k = leading_ones[n & 0xFF];
		m += k;
		return m;
	}

	public static int count_trailing_zeros(int n) {
		int k = trailing_zeros[n & 0xFF];
		int m = k;
		if (k != 8) return m;

		k = trailing_zeros[(n >> 8) & 0xFF];
		m += k;
		if (k != 8) return m;

		k = trailing_zeros[(n >> 16) & 0xFF];
		m += k;
		if (k != 8) return m;

		k = trailing_zeros[(n >> 24) & 0xFF];
		m += k;
		return m;
	}

	public static int count_trailing_ones(int n) {
		int k = trailing_ones[n & 0xFF];
		int m = k;
		if (k != 8) return m;

		k = trailing_ones[(n >> 8) & 0xFF];
		m += k;
		if (k != 8) return m;

		k = trailing_ones[(n >> 16) & 0xFF];
		m += k;
		if (k != 8) return m;

		k = trailing_ones[(n >> 24) & 0xFF];
		m += k;
		return m;
	}
}

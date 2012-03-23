package jmips;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import jmips.cpu.Cpu;
import jmips.serial.TTY;

/**
 * GdbStub class 
 * @author Humberto Silva Naves
 */
public class GdbServer {
	private ServerSocket server;
	private Socket socket;
	private InputStream is;
	private OutputStream os;
	private String lastData;
	private List<Integer> breakPoints = new ArrayList<Integer>();

	private Cpu cpu;
	private TTY tty;

	public GdbServer(Cpu cpu, TTY tty) {
		this.cpu = cpu;
		this.tty = tty;
	}

	public void startServer(int port) throws IOException {
		server = null;
		socket = null;
		is = null;
		os = null;
		try {
			StringBuilder sb = new StringBuilder();
			server = new ServerSocket(port);
			socket = server.accept();
			is = socket.getInputStream();
			os = socket.getOutputStream();

			boolean stop = false;
			while (!stop && socket.isConnected()) {
				int c = is.read();
				if (c == '+' || c == '-') {
					if (c == '-') retransmitLastData();
				} else {
					int sum = 0, test = 0;
					if (c == '$') {
						char ch1, ch2;
						sb.setLength(0);
						while(true) {
							c = is.read();
							if (c == '#') break;
							sb.append((char) c);
							sum += c;
						}
						ch1 = (char) is.read();
						ch2 = (char) is.read();
						test = Integer.parseInt("" + ch1 + ch2, 16);
						if (test != (sum & 0xFF)) {
							sendData("-");
						} else {
							if (!processCommand(sb.toString()))
								stop = true;
						}
					}
				}
			}
		} finally {
			if (is != null) is.close();
			if (os != null) os.close();
			if (socket != null) socket.close();
			if (server != null) server.close();
		}
	}

	private boolean processCommand(String cmd) throws IOException {
		//System.out.println("Recv: " + cmd);
		if (cmd.startsWith("qSupported")) {
			sendPacket("PacketSize=1024");
		} else if (cmd.startsWith("H")) {
			sendPacket("OK");
		} else if (cmd.startsWith("?")) {
			sendPacket("S05");
		} else if (cmd.startsWith("qAttached")) {
			sendPacket("0");
		} else if (cmd.startsWith("qOffsets")) {
			commandQOffsets(cmd);
		} else if (cmd.startsWith("g")) {
			commandReadRegisters(cmd);
		} else if (cmd.startsWith("p")) {
			commandReadRegister(cmd);
		} else if (cmd.startsWith("m")) {
			commandReadMemory(cmd);
		} else if (cmd.startsWith("qSymbol::")) {
			sendPacket("OK");
		} else if (cmd.startsWith("s")) {
			commandStep(cmd);
		} else if (cmd.startsWith("c")) {
			commandContinue(cmd);
		} else if (cmd.toLowerCase().startsWith("z0")) {
			commandBreakpoint(cmd);
		} else if (cmd.startsWith("k")) {
			return false;
		} else {
			sendPacket("");
		}
		return true;
	}

	private void sendData(String data) throws IOException {
		//System.out.println("Send: " + data);
		lastData = data;
		os.write(data.getBytes());
	}

	private void sendPacket(String packet) throws IOException {
		int sum = 0;
		StringBuilder sb = new StringBuilder();
		sb.append("+$");
		for(int i = 0; i < packet.length(); i++) {
			sb.append(packet.charAt(i));
			sum += (byte) packet.charAt(i);
		}
		sb.append("#");
		sum = sum & 0xFF;
		sb.append(String.format("%02x", sum));
		sendData(sb.toString());
	}

	private void retransmitLastData() throws IOException {
		sendData(lastData);
	}

	private void commandReadRegisters(String cmd) throws IOException {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < 32; i++) {
			sb.append(String.format("%08x", cpu.getGpr(i)));
		}
		sendPacket(sb.toString());
	}

	private void commandReadRegister(String cmd) throws IOException {
		int reg = Integer.parseInt(cmd.substring(1), 16);
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("%08x", readRegister(reg)));
		sendPacket(sb.toString());
	}

	private int readRegister(int reg) {
		if (reg < 32) return cpu.getGpr(reg);

		switch (reg) {
		case 32: return cpu.getCop0Reg(Cpu.COP0_REG_STATUS, 0);
		case 33: return cpu.getLo();
		case 34: return cpu.getHi();
		case 35: return cpu.getCop0Reg(Cpu.COP0_REG_BADVADDR, 0);
		case 36: return cpu.getCop0Reg(Cpu.COP0_REG_CAUSE, 0);
		case 37: return cpu.getPc();
		default: return 0;
		}
	}

	private void commandQOffsets(String cmd) throws IOException {
		sendPacket("Text=0;Data=0;Bss=0");
	}

	private void commandReadMemory(String cmd) throws IOException {
		int divider = cmd.indexOf(",");
		int address = (int) Long.parseLong(cmd.substring(1, divider), 16);
		int len = Integer.parseInt(cmd.substring(divider + 1));
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < len ; i++) {
			byte b = cpu.load8(address + i);
			sb.append(String.format("%02X", ((int) b) & 0xFF));
		}
		sendPacket(sb.toString());
	}

	private void commandBreakpoint(String cmd) throws IOException {
		int divider = cmd.substring(3).indexOf(",");
		int address = (int) Long.parseLong(cmd.substring(3, divider + 3), 16);

		if (cmd.startsWith("Z0")) {
			breakPoints.add(address);
		} else {
			breakPoints.remove(new Integer(address));
		}
		sendPacket("OK");
	}

	private void commandStep(String cmd) throws IOException {
		cpu.wakeUp();
		cpu.step();
		sendPacket("S05");
	}

	private void commandContinue(String cmd) throws IOException {
		sendData("+");
		cpu.wakeUp();
		while(true) {
			if (cpu.isHalted()) break;
			int pc = cpu.getPc();
			if (breakPoints.contains(pc)) break;
			cpu.step();
		}
		sendPacket("S05");
	}

}

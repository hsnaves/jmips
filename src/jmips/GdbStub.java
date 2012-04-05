package jmips;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jmips.cpu.Cpu;
import jmips.cpu.Instruction;

/**
 * GdbStub class 
 * @author Humberto Silva Naves
 */
public final class GdbStub {
	private final MipsSystem system;
	private List<Integer> breakPoints;

	private String lastPacket;
	private StringBuilder currentInputPacket;
	private int packetChecksum, packetFinished;
	private boolean closeConnection, serverShutdown, serverRunning, simulationRunning;

	private Selector selector;
	private ServerSocketChannel serverSocketChannel;
	private SocketChannel socketChannel;
	private ByteBuffer input;
	private List<ByteBuffer> pendingWrites;

	public GdbStub(MipsSystem system) {
		this.system = system;
	}

	public MipsSystem getSystem() {
		return system;
	}

	public void runServer(int port, boolean initiallyStopped) {
		selector = null;
		serverSocketChannel = null;
		socketChannel = null;
		try {
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(false);

			serverSocketChannel.socket().bind(new InetSocketAddress(port));

			selector = Selector.open();
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		} catch(IOException ex) {
			ex.printStackTrace();
			try {
				closeServerSocketChannel();
			} catch(Throwable t) { t.printStackTrace(); }

			try {
				closeSelector();
			} catch(Throwable t) { t.printStackTrace(); }

			return;
		}

		pendingWrites = new LinkedList<ByteBuffer>();
		breakPoints = new ArrayList<Integer>();
		currentInputPacket = new StringBuilder();
		input = ByteBuffer.allocate(1024);
		serverShutdown = false;
		serverRunning = true;
		simulationRunning = initiallyStopped ? false : true;

		while(serverRunning) {
			try {
				selector.select(50);
				Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
				while (selectedKeys.hasNext()) {
					SelectionKey key = selectedKeys.next();
					if (key.isValid()) {
						if (key.isAcceptable()) {
							onSelectAccept(key); 
						}
						if (key.isReadable()) {
							onSelectRead(key);
						}
						if (key.isWritable()) {
							onSelectWrite(key);
						}
					}
					selectedKeys.remove();
				}
				processCommandsAndSimulate();
			} catch(IOException ex) {
				ex.printStackTrace();
			}
		}

		try {
			closeServerSocketChannel();
		} catch(Throwable t) { t.printStackTrace(); }

		try {
			closeSelector();
		} catch(Throwable t) { t.printStackTrace(); }

		try {
			closeSocketChannel();
		} catch(Throwable t) { t.printStackTrace(); }
	}

	private void enableNewConnections(boolean enable) {
		if (serverSocketChannel == null) return;
		SelectionKey key = serverSocketChannel.keyFor(selector);
		key.interestOps(enable ? SelectionKey.OP_ACCEPT : 0);
	}

	private void enableWrites(boolean enable) {
		if (socketChannel == null) return;
		SelectionKey key = socketChannel.keyFor(selector);
		key.interestOps(enable ? SelectionKey.OP_WRITE : SelectionKey.OP_READ);
	}

	private void closeServerSocketChannel() throws IOException {
		if (serverSocketChannel == null) return;
		try {
			serverSocketChannel.close();
		} finally {
			serverSocketChannel = null;
		}
	}

	private void closeSelector() throws IOException {
		if (selector == null) return;
		try {
			selector.close();
		} finally {
			selector = null;
		}
	}

	private void closeSocketChannel() throws IOException {
		if (socketChannel == null) return;
		SelectionKey key = socketChannel.keyFor(selector);
		if (key != null) key.cancel();
		try {
			socketChannel.close();
		} finally {
			socketChannel = null;
			if (!serverShutdown)
				enableNewConnections(true);
			else
				serverRunning = false;
		}
	}

	private void shutdownServer() {
		serverShutdown = true;
		closeConnection = true;
		enableWrites(true);
	}

	private void detachServer() {
		closeConnection = true;
		enableWrites(true);
	}

	private void onSelectAccept(SelectionKey key) throws IOException {
		ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
		SocketChannel sc = ssc.accept();
		if (sc != null) {
			closeConnection = false;
			simulationRunning = false;
			pendingWrites.clear();
			input.rewind();
			sc.configureBlocking(false);
			sc.register(key.selector(), SelectionKey.OP_READ);
			socketChannel = sc;
			enableNewConnections(false);
		}
	}

	private void onSelectRead(SelectionKey key) throws IOException {
		SocketChannel sc = (SocketChannel) key.channel();

		int numRead;
		try {
			numRead = sc.read(input);
		} catch(IOException ex) {
			numRead = -1;
		}

		if (numRead == -1) closeSocketChannel();
	}

	private void onSelectWrite(SelectionKey key) throws IOException {
		SocketChannel sc = (SocketChannel) key.channel();
		if (pendingWrites.isEmpty() && closeConnection) {
			closeSocketChannel();
			return;
		}

		while(!pendingWrites.isEmpty()) {
			ByteBuffer bb = pendingWrites.get(0);
			try {
				sc.write(bb);
			} catch(IOException ex) {
				closeSocketChannel();
				throw ex;
			}
			if (bb.remaining() > 0) break;
			pendingWrites.remove(0);
		}

		if (pendingWrites.isEmpty() && !closeConnection) {
			enableWrites(false);
		}
	}

	private void send(String packet) {
		ByteBuffer bb = ByteBuffer.wrap(packet.getBytes());
		pendingWrites.add(bb);
		enableWrites(true);
	}

	private void sendPacket(String packet) {
		lastPacket = packet;
		send(packet);
	}

	private void makePacketAndSend(String data, boolean acknowledge) {
		int checksum = 0;
		data = escapePacketData(data);
		StringBuilder sb = new StringBuilder();
		if (acknowledge) sb.append("+");
		sb.append("$");
		for(int i = 0; i < data.length(); i++) {
			sb.append(data.charAt(i));
			checksum += (byte) data.charAt(i);
		}
		sb.append("#");
		checksum = checksum & 0xFF;
		sb.append(String.format("%02x", checksum));
		sendPacket(sb.toString());
	}

	private String escapePacketData(String data) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < data.length(); i++) {
			char c = data.charAt(i);
			if (c == '$' || c == '#' || c == '}') {
				sb.append("}");
				sb.append(((char) c ^ 0x20));
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	private void processCommandsAndSimulate() {
		input.flip();
		while(input.hasRemaining()) {
			char c = (char) input.get();
			if (currentInputPacket.length() == 0) {
				if (c == '-') {
					retrasmitLastPacket();
				} else if (c == '+') {
					// Silently discard '+' packets
				} else if (c == 0x03) {
					// Ctrl-C requests
					if (simulationRunning) {
						makePacketAndSend("S05", true);
					}
					simulationRunning = false;
				} else {
					if (c != '$') {
						requestRetransmit();
					} else {
						currentInputPacket.append(c);
						packetChecksum = 0;
						packetFinished = 0;
					}
				}
			} else {
				currentInputPacket.append(c);
				if (packetFinished > 0) {
					if (++packetFinished == 3) {
						if (checkPacket()) {
							processCommand(currentInputPacket.substring(1, currentInputPacket.length() - 3));
						} else {
							requestRetransmit();
						}
						currentInputPacket.setLength(0);
					}
				} else if (c == '#') {
					packetFinished = 1;
				} else {
					packetChecksum += c;
				}
			}
		}
		input.clear();
		simulate();
	}

	private void requestRetransmit() {
		send("-");
	}

	private void retrasmitLastPacket() {
		send(lastPacket);
	}

	private boolean checkPacket() {
		try {
			int checksum = Integer.parseInt(currentInputPacket.substring(currentInputPacket.length() - 2), 16);
			return (checksum == (packetChecksum & 0xFF));
		} catch(NumberFormatException ex) {
			return false;
		}
	}

	private void processCommand(String command) {
		for(String prefix : commands.keySet()) {
			if (command.startsWith(prefix)) {
				GdbStubCommand cmd = commands.get(prefix);
				cmd.processCommand(command, this);
				return;
			}
		}
		makePacketAndSend("", true);
	}

	private int readRegister(int reg) {
		final Cpu cpu = system.getCpu();
		if (reg < 32) return cpu.getGpr(reg);

		switch (reg) {
		case 32: return cpu.getCop0Reg(Instruction.COP0_REG_STATUS, 0);
		case 33: return cpu.getLo();
		case 34: return cpu.getHi();
		case 35: return cpu.getCop0Reg(Instruction.COP0_REG_BADVADDR, 0);
		case 36: return cpu.getCop0Reg(Instruction.COP0_REG_CAUSE, 0);
		case 37: return cpu.getPc();
		default: return 0;
		}
	}

	private void writeRegister(int reg, int val) {
		final Cpu cpu = system.getCpu();
		if (reg < 32) {
			cpu.setGpr(reg, val);
			return;
		}

		switch (reg) {
		case 32: cpu.setCop0Reg(Instruction.COP0_REG_STATUS, 0, val); break;
		case 33: cpu.setLo(val); break;
		case 34: cpu.setHi(val); break;
		case 35: cpu.setCop0Reg(Instruction.COP0_REG_BADVADDR, 0, val); break;
		case 36: cpu.setCop0Reg(Instruction.COP0_REG_CAUSE, 0, val); break;
		case 37: cpu.setPc(val); break;
		}
	}

	private void simulate() {
		if (simulationRunning) {
			for(int i = 0; i < 1000000; i++) {
				int pc = system.getCpu().getPc();
				if (breakPoints.contains(pc)) {
					simulationRunning = false;
					makePacketAndSend("S05", true);
					break;
				} else {
					system.step(1);
				}
			}
		}
	}

	private static interface GdbStubCommand {
		public void processCommand(String command, GdbStub stub);
	}

	private static final Map<String, GdbStubCommand> commands;
	private static void registerCommand(String commandPrefix, GdbStubCommand command) {
		commands.put(commandPrefix, command);
	}

	static {
		commands = new HashMap<String, GdbStubCommand>();
		GdbStubCommand commandContinue = new GdbStubCommand() {
			@Override
			public void processCommand(String command, GdbStub stub) {
				stub.send("+");
				stub.simulationRunning = true;
			}
		};
		registerCommand("c", commandContinue);

		GdbStubCommand commandStep = new GdbStubCommand() {
			@Override
			public void processCommand(String command, GdbStub stub) {
				stub.system.step(1);
				stub.makePacketAndSend("S05", true);
			}
		};
		registerCommand("s", commandStep);

		GdbStubCommand commandBreakpoint = new GdbStubCommand() {
			@Override
			public void processCommand(String command, GdbStub stub) {
				int divider = command.substring(3).indexOf(",");
				int address = (int) Long.parseLong(command.substring(3, divider + 3), 16);

				if (command.startsWith("Z0")) {
					stub.breakPoints.add(address);
				} else {
					stub.breakPoints.remove(new Integer(address));
				}
				stub.makePacketAndSend("OK", true);
			}
		};
		registerCommand("z0", commandBreakpoint);
		registerCommand("Z0", commandBreakpoint);

		GdbStubCommand commandMemory = new GdbStubCommand() {
			@Override
			public void processCommand(String command, GdbStub stub) {
				int divider = command.indexOf(",");
				int address = (int) Long.parseLong(command.substring(1, divider), 16);
				if (command.startsWith("m")) {
					int len = Integer.parseInt(command.substring(divider + 1));
					int val;
					String resp = "00";
					switch(len) {
					case 1: val = stub.system.getCpu().load8(address) & 0xFF;
						resp = String.format("%02X", val); break;
					case 2: val = stub.system.getCpu().load16(address) & 0xFFFF;
						resp = String.format("%04X", val); break;
					case 4: val = stub.system.getCpu().load32(address);
						resp = String.format("%08X", val); break;
					}
					stub.makePacketAndSend(resp, true);
				} else {
					int divider2 = command.indexOf(":");
					int len = Integer.parseInt(command.substring(divider + 1, divider2));
					int val = (int) Long.parseLong(command.substring(divider2 + 1), 16);
					switch(len) {
					case 1: stub.system.getCpu().write8(address, (byte) val); break;
					case 2: stub.system.getCpu().write16(address, (short) val); break;
					case 4: stub.system.getCpu().write32(address, val); break;
					}
					stub.makePacketAndSend("OK", true);
				}
			}
		};
		registerCommand("m", commandMemory);
		registerCommand("M", commandMemory);

		GdbStubCommand commandRegisters = new GdbStubCommand() {
			@Override
			public void processCommand(String command, GdbStub stub) {
				if (command.startsWith("g")) {
					StringBuilder sb = new StringBuilder();
					for(int i = 0; i < 32; i++) {
						sb.append(String.format("%08x", stub.readRegister(i)));
					}
					stub.makePacketAndSend(sb.toString(), true);
				} else {
					for(int i = 0; i < 32; i++) {
						stub.system.getCpu().setGpr(i, (int) Long.parseLong(command.substring(1 + 8 * i, 9 + 8 * i), 16));
					}
				}
			}
		};
		registerCommand("g", commandRegisters);
		registerCommand("G", commandRegisters);

		GdbStubCommand commandRegister = new GdbStubCommand() {
			@Override
			public void processCommand(String command, GdbStub stub) {
				if (command.startsWith("p")) {
					int reg = Integer.parseInt(command.substring(1), 16);
					stub.makePacketAndSend(String.format("%08x", stub.readRegister(reg)), true);
				} else {
					int reg = Integer.parseInt(command.substring(1, command.indexOf('=')), 16);
					int val = Integer.parseInt(command.substring(command.indexOf('=') +  1), 16);
					stub.writeRegister(reg, val);
					stub.makePacketAndSend("OK", true);
				}
			}
		};
		registerCommand("p", commandRegister);
		registerCommand("P", commandRegister);

		GdbStubCommand commandKill = new GdbStubCommand() {
			@Override
			public void processCommand(String command, GdbStub stub) {
				stub.send("+");
				stub.shutdownServer();
			}
		};
		registerCommand("k", commandKill);

		GdbStubCommand commandQSupported = new GdbStubCommand() {
			@Override
			public void processCommand(String command, GdbStub stub) {
				stub.makePacketAndSend("PacketSize=1024", true);
			}
		};
		registerCommand("qSupported", commandQSupported);

		GdbStubCommand commandLastSignal = new GdbStubCommand() {
			@Override
			public void processCommand(String command, GdbStub stub) {
				stub.makePacketAndSend("S05", true);
			}
		};
		registerCommand("?", commandLastSignal);

		GdbStubCommand commandDetach = new GdbStubCommand() {
			@Override
			public void processCommand(String command, GdbStub stub) {
				stub.makePacketAndSend("OK", true);
				stub.simulationRunning = true;
				stub.detachServer();
			}
		};
		registerCommand("D", commandDetach);
	}
}

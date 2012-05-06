package jmips.tty;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import javax.swing.JFrame;

import jmips.tty.SwingTTY;

public class TelnetClient extends JFrame {
	private static final long serialVersionUID = -316995381999163483L;

	public static final byte SE   = (byte) 0xF0; // subnegotiation end
	public static final byte SB   = (byte) 0xFA; // subnegotiation start
	public static final byte WILL = (byte) 0xFB;
	public static final byte WONT = (byte) 0xFC;
	public static final byte DO   = (byte) 0xFD;
	public static final byte DONT = (byte) 0xFE;
	public static final byte IAC  = (byte) 0xFF;

	public static final byte ECHO = (byte) 0x01;
	public static final byte SUPPRESS_GO_AHEAD = (byte) 0x03;
	public static final byte TERMINAL_TYPE = (byte) 0x18;
	public static final byte TERMINAL_SPEED = (byte) 0x20;
	public static final byte NEW_ENVIRONMENT_OPTION = (byte) 0x27;
	public static final byte NEGOTIATE_WINDOW_SIZE = (byte) 0x1F;

	private SwingTTY tty;
	private SocketChannel socketChannel;
	private Selector selector;
	private ByteBuffer input, output;

	public TelnetClient() {
		setTitle("Telnet Client");
		tty = new SwingTTY();
		getContentPane().add(tty);

		pack();
		tty.open();
		tty.setEchoEnabled(false);
	}

	private void connect() {
		try {
			selector = Selector.open();
			socketChannel = SocketChannel.open();
			socketChannel.connect(new InetSocketAddress("127.0.0.1", 23));
			socketChannel.configureBlocking(false);
			socketChannel.register(selector, SelectionKey.OP_READ);
		} catch(IOException ex) {
			ex.printStackTrace();
			try {
				closeSocketChannel();
			} catch(Throwable t) { t.printStackTrace(); }

			try {
				closeSelector();
			} catch(Throwable t) { t.printStackTrace(); }
		}

		input = ByteBuffer.allocate(1024);
		output = ByteBuffer.allocate(1024);

		try {
			while(true) {
				selector.select(50);
				Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
				while (selectedKeys.hasNext()) {
					SelectionKey key = selectedKeys.next();
					if (key.isValid()) {
						if (key.isReadable()) {
							onSelectRead(key);
						}
						if (key.isWritable()) {
							onSelectWrite(key);
						}
					}
					selectedKeys.remove();
				}

				while(tty.available() && output.hasRemaining()) {
					byte b = tty.read();
					output.put(b);
				}

				if (output.position() > 0) {
					enableWrites(true);
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}

		try {
			closeSocketChannel();
		} catch(Throwable t) { t.printStackTrace(); }

		try {
			closeSelector();
		} catch(Throwable t) { t.printStackTrace(); }
	}

	private void closeSocketChannel() throws IOException {
		if (socketChannel == null) return;
		try {
			socketChannel.close();
		} finally {
			socketChannel = null;
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

	private void enableWrites(boolean enable) {
		if (socketChannel == null) return;
		SelectionKey key = socketChannel.keyFor(selector);
		key.interestOps(enable ? SelectionKey.OP_WRITE : SelectionKey.OP_READ);
	}

	private void onSelectRead(SelectionKey key) throws IOException {
		SocketChannel sc = (SocketChannel) key.channel();

		input.clear();
		int numRead;
		try {
			numRead = sc.read(input);
		} catch(IOException ex) {
			numRead = -1;
		}

		if (numRead == -1) closeSocketChannel();
		else {
			processTelnetCommand();
		}
	}

	private void onSelectWrite(SelectionKey key) throws IOException {
		output.flip();
		if (output.hasRemaining()) {
			try {
				socketChannel.write(output);
			} catch(IOException ex) {
				closeSocketChannel();
				throw ex;
			}
			output.compact();
			output.limit(output.capacity());
		}

		if (output.position() == 0) {
			enableWrites(false);
		}
	}

	private void processTelnetCommand() {
		input.flip();
		while(input.hasRemaining()) {
			byte b = input.get();
			if (b == IAC) {
				byte cmd = input.get();
				if (cmd == DO) {
					cmd = WONT;
					b = input.get();
					if (b == ECHO || b == SUPPRESS_GO_AHEAD || b == TERMINAL_TYPE ||
						b == TERMINAL_SPEED || b == NEW_ENVIRONMENT_OPTION || b == NEGOTIATE_WINDOW_SIZE) {
						cmd = WILL;
					}
					output.put(IAC);
					output.put(cmd);
					output.put(b);
				} else if (cmd == SB) {
					String response = "";
					b = input.get();
					if (b == TERMINAL_TYPE) {
						response = "LINUX";
					} else if (b == TERMINAL_SPEED) {
						response = "38400,38400";
					}
					output.put(IAC);
					output.put(cmd);
					output.put(b);
					output.put((byte) 0);
					for(int i = 0; i < response.length(); i++) {
						output.put((byte) response.charAt(i)); 
					}
					output.put(IAC);
					output.put(SE);
				}
			} else {
				tty.write(b);
			}
		}
	}

	
	public static void main(String[] args) {
		TelnetClient t = new TelnetClient();
		t.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		t.setVisible(true);
		t.connect();
	}
}

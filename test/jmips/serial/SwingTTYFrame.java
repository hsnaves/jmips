package jmips.serial;

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
	}
}

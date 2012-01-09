package jmips.serial;

import javax.swing.JFrame;

public class SwingTTYTest extends JFrame {
	private static final long serialVersionUID = -316995381999163483L;

	private SwingTTY tty;

	public SwingTTYTest() {
		setTitle("SwingTTY");
		tty = new SwingTTY();
		getContentPane().add(tty);

		pack();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		tty.open();
}

	public static void main(String[] args) {
		SwingTTYTest t = new SwingTTYTest();
		try {
			while(true) {
				if (t.tty.available()) {
					t.tty.write(t.tty.read());
				} else {
					Thread.sleep(10);
				}
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}

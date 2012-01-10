package jmips.debugger;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jcp.xml.dsig.internal.dom.Utils;

import jmips.cpu.CpuState;
import jmips.serial.SwingTTY;

public class Debugger extends JFrame implements ActionListener {
	private static final long serialVersionUID = 2776635517093322890L;

	private final CpuState cpu;

	private JToolBar mainToolBar;
	private JButton stepIntoButton;
	private JButton stepOverButton;
	private JButton stepOutButton;
	private JMenuBar mainMenuBar;
	private JMenu fileMenu;
	private JMenuItem loadMenuItem;
	private JMenuItem dumpMenuItem;
	private JMenuItem exitMenuItem;

	private DisassemblePanel disassemblePanel;
	private GPRTable gprTable;
	private SwingTTY tty;

	private JFileChooser fileChooser;

	public Debugger(CpuState cpu) {
		this.cpu = cpu;
		setTitle("JMIPS Debugger");
		initComponents();
	}

	private void initComponents() {
		setLayout(new BorderLayout());
		setJMenuBar(getMainMenuBar());
		add(getMainToolBar(), BorderLayout.PAGE_START);
		add(getDisassemblePanel(), BorderLayout.CENTER);
		add(getGPRTable(), BorderLayout.EAST);
		add(getTTY(), BorderLayout.SOUTH);
		pack();
	}

	private JToolBar getMainToolBar() {
		if (mainToolBar == null) {
			mainToolBar = new JToolBar();
			mainToolBar.setFloatable(false);
			//mainToolBar.putClientProperty(Options.HEADER_STYLE_KEY,
			//		HeaderStyle.SINGLE);
			//mainToolBar.putClientProperty(PlasticLookAndFeel.BORDER_STYLE_KEY,
			//		BorderStyle.SEPARATOR);
			mainToolBar.add(getStepIntoButton());
			mainToolBar.add(getStepOverButton());
			mainToolBar.add(getStepOutButton());
		}
		return mainToolBar;
	}

	private JButton getStepIntoButton() {
		if (stepIntoButton == null) {
			stepIntoButton = makeToolBarButton("Step into", STEPINTO_COMMAND, "Step into", "StepIntoIcon.png");
		}
		return stepIntoButton;
	}

	private JButton getStepOverButton() {
		if (stepOverButton == null) {
			stepOverButton = makeToolBarButton("Step over", STEPOVER_COMMAND, "Step over", "StepOverIcon.png");
		}
		return stepOverButton;
	}

	private JButton getStepOutButton() {
		if (stepOutButton == null) {
			stepOutButton = makeToolBarButton("Step out", STEPOUT_COMMAND, "Step out", "StepOutIcon.png");
		}
		return stepOutButton;
	}

	private JMenuBar getMainMenuBar() {
		if (mainMenuBar == null) {
			mainMenuBar = new JMenuBar();
			mainMenuBar.add(getFileMenu());
			//mainMenuBar.putClientProperty(Options.HEADER_STYLE_KEY,
			//		HeaderStyle.SINGLE);
			//mainMenuBar.putClientProperty(PlasticLookAndFeel.BORDER_STYLE_KEY,
			//		BorderStyle.SEPARATOR);
		}
		return mainMenuBar;
	}

	private JMenu getFileMenu() {
		if (fileMenu == null) {
			fileMenu = makeMenu("File", KeyEvent.VK_F, "File Menu");
			fileMenu.add(getLoadMenuItem());
			fileMenu.add(getDumpMenuItem());
			fileMenu.addSeparator();
			fileMenu.add(getExitMenuItem());
		}
		return fileMenu;
	}

	private JMenuItem getLoadMenuItem() {
		if (loadMenuItem == null) {
			loadMenuItem = makeMenuItem("Load", KeyEvent.VK_L, LOAD_COMMAND, "Loads a file into memory");
			loadMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
		}
		return loadMenuItem;
	}

	private JMenuItem getDumpMenuItem() {
		if (dumpMenuItem == null) {
			dumpMenuItem = makeMenuItem("Dump", KeyEvent.VK_D, DUMP_COMMAND, "Dumps memory to a file");
			dumpMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK));
		}
		return dumpMenuItem;
	}

	private JMenuItem getExitMenuItem() {
		if (exitMenuItem == null) {
			exitMenuItem = makeMenuItem("Exit", KeyEvent.VK_X, EXIT_COMMAND, "Exit the application");
		}
		return exitMenuItem;
	}

	private DisassemblePanel getDisassemblePanel() {
		if (disassemblePanel == null) {
			disassemblePanel = new DisassemblePanel(cpu);
		}
		return disassemblePanel;
	}

	private GPRTable getGPRTable() {
		if (gprTable == null) {
			gprTable = new GPRTable(cpu);
		}
		return gprTable;
	}

	private SwingTTY getTTY() {
		if (tty == null) {
			tty = new SwingTTY(12, 120, 1000);
		}
		return tty;
	}

	private JFileChooser getFileChooser() {
		if (fileChooser == null) {
			fileChooser = new JFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooser.setMultiSelectionEnabled(false);
			fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Binary Files", "bin"));
			fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("ELF Files", "elf"));
			fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
		}
		return fileChooser;

	}

	private JButton makeToolBarButton(String text, String actionCommand,
			String toopTipText, String imageName) {
		JButton button = new JButton(text);
		button.setToolTipText(toopTipText);
		button.setActionCommand(actionCommand);
		button.addActionListener(this);
		String imgLocation = "icons/" + imageName;
		// URL imageURL = MainFrame.class.getResource(imgLocation);
		button.setIcon(new ImageIcon(imgLocation));
		return button;
	}

	private JMenu makeMenu(String text, int mnemonic, String toolTipText) {
		JMenu menu = new JMenu(text);
		if (mnemonic != KeyEvent.VK_UNDEFINED)
			menu.setMnemonic(mnemonic);
		menu.setToolTipText(toolTipText);
		return menu;
	}

	private JMenuItem makeMenuItem(String text, int mnemonic,
			String actionCommand, String toolTipText) {
		JMenuItem menuItem = new JMenuItem(text);
		if (mnemonic != KeyEvent.VK_UNDEFINED)
			menuItem.setMnemonic(mnemonic);
		menuItem.setToolTipText(toolTipText);
		menuItem.setActionCommand(actionCommand);
		menuItem.addActionListener(this);
		return menuItem;
	}

	private static final String LOAD_COMMAND = "LOAD";
	private static final String DUMP_COMMAND = "DUMP";
	private static final String EXIT_COMMAND = "EXIT";
	private static final String STEPINTO_COMMAND = "STEPINTO";
	private static final String STEPOVER_COMMAND = "STEPOVER";
	private static final String STEPOUT_COMMAND = "STEPOUT";

	private int askUserForInteger(String message) {
		String str = JOptionPane.showInputDialog(this, message, 0);
		return Integer.parseInt(str);
	}

	private void load() {
		JFileChooser fc = getFileChooser();
		int retVal = fc.showOpenDialog(this);
		if (retVal == JFileChooser.APPROVE_OPTION) {
			File f = fc.getSelectedFile();
			try {
				int pc;
				FileInputStream fis = new FileInputStream(f);
				pc = askUserForInteger("Load address");
				byte[] data = Utils.readBytesFromStream(fis);
				for(int i = 0; i < data.length; i++) {
					cpu.write8(pc + i, data[i]);
				}
			} catch(Exception ex) {
				JOptionPane.showMessageDialog(this, ex.getClass().getName() + ": " + ex.getMessage());
			}
		}
	}

	private void dump() {
		JFileChooser fc = getFileChooser();
		int retVal = fc.showSaveDialog(this);
		if (retVal == JFileChooser.APPROVE_OPTION) {
			//File f = fc.getSelectedFile();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (LOAD_COMMAND.equals(e.getActionCommand())) {
			load();
		} else if (DUMP_COMMAND.equals(e.getActionCommand())) {
			dump();
		} else if (EXIT_COMMAND.equals(e.getActionCommand())) {
			System.exit(0);
		} else if (STEPINTO_COMMAND.equals(e.getActionCommand())) {
		} else if (STEPOVER_COMMAND.equals(e.getActionCommand())) {
		} else if (STEPOUT_COMMAND.equals(e.getActionCommand())) {
		}
		updateAndRepaint();
	}

	public final void updateAndRepaint() {
		repaint();
	}


	public static void main(String[] args) {
		CpuState cpu = new CpuState(0x0, 16 * 1024 * 1024);
		Debugger debugger = new Debugger(cpu);
		debugger.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		debugger.setVisible(true);
	}
}

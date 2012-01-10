package jmips.debugger;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListDataListener;

import jmips.cpu.CpuState;
import jmips.cpu.Disassemble;

public final class DisassemblePanel extends JPanel {
	private static final long serialVersionUID = -4642361356734616764L;

	private DisassemblePanelList mainList;
	private final CpuState cpu;
	private int listDataOffset;

	public DisassemblePanel(CpuState cpu) {
		this.cpu = cpu;
		initializeComponents();
	}

	private void initializeComponents() {
		setLayout(new BorderLayout());
		add(getMainList(), BorderLayout.NORTH);
	}

	private DisassemblePanelList getMainList() {
		if (mainList == null) {
			mainList = new DisassemblePanelList();
			mainList.setBorder(new EtchedBorder());
			mainList.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		}
		return mainList;
	}

	private class DisassemblePanelList extends JList {
		private static final long serialVersionUID = 3972362295525175568L;

		public DisassemblePanelList() {
			addKeyListener(new ExpandableListKeyListener());
			setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			setModel(new ExpandableListModel());
		}
	}

	private class ExpandableListModel implements ListModel {
		@Override
		public int getSize() {
			return 17;
		}

		@Override
		public Object getElementAt(int index) {
			int address = cpu.pc + 4 * (index + listDataOffset);
			int opcode = cpu.read32(address);
			return Disassemble.disassemble(address, opcode);
		}

		@Override
		public void addListDataListener(ListDataListener l) {
		}

		@Override
		public void removeListDataListener(ListDataListener l) {
		}
	}

	private class ExpandableListKeyListener implements KeyListener {

		@Override
		public void keyTyped(KeyEvent e) {
		}

		@Override
		public void keyReleased(KeyEvent e) {
		}

		@Override
		public void keyPressed(KeyEvent e) {
			if (e.isActionKey() && e.getSource() == mainList) {
				boolean consume = false;
				if (e.getKeyCode() == KeyEvent.VK_DOWN
						&& mainList.getSelectedIndex() == mainList.getModel()
								.getSize() - 1) {
					listDataOffset += 1;
					consume = true;
				}
				if (e.getKeyCode() == KeyEvent.VK_UP
						&& mainList.getSelectedIndex() == 0) {
					listDataOffset -= 1;
					consume = true;
				}
				if (e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
					listDataOffset += mainList.getModel().getSize();
					consume = true;
				}
				if (e.getKeyCode() == KeyEvent.VK_PAGE_UP) {
					listDataOffset -= mainList.getModel().getSize();
					consume = true;
				}

				if (consume) {
					e.consume();
					repaint();
				}
			}
		}
	}
}

package jmips.debugger;

import java.awt.Color;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import jmips.cpu.CpuState;

public class GPRTable extends JTable {

	private static final long serialVersionUID = 2071267925156377116L;

	private final CpuState cpu;
	public GPRTable(CpuState cpu) {
		this.cpu = cpu;
		super.setModel(new DefaultTableModel(new Object[][] {
				{"pc", "", "next_pc", null},
				{"hi", null, "lo", null},
				{"zr", null, "at", null},
				{"v0", null, "v1", null},
				{"a0", null, "a1", null},
				{"a2", null, "a3", null},
				{"t0", null, "t1", null},
				{"t2", null, "t3", null},
				{"t4", null, "t5", null},
				{"t6", null, "t7", null},
				{"s0", null, "s1", null},
				{"s2", null, "s3", null},
				{"s4", null, "s5", null},
				{"s6", null, "s7", null},
				{"t8", null, "t9", null},
				{"k0", null, "k1", null},
				{"gp", null, "sp", null},
				{"fp", null, "ra", null}
		}, new String[] {
				"REG", "HEX", "REG", "HEX"
		})
		{
			private static final long serialVersionUID = 6245633542359399730L;
			@SuppressWarnings("rawtypes")
			private Class[] types = new Class[] { String.class, Object.class,
			                                      String.class, Object.class };

			@Override
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public Class getColumnClass(int columnIndex) {
				return types[columnIndex];
			}

			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return false;
			}
		});
		setBackground(Color.LIGHT_GRAY);
		setColumnSelectionAllowed(true);
		getTableHeader().setReorderingAllowed(false);
	}

	@Override
	public final void setModel(TableModel dataModel) {
	}

	public final void updateTable() {
		setValueAt(String.format("0x%08X", cpu.pc), 0, 1);
		setValueAt(String.format("0x%08X", cpu.next_pc), 0, 3);
		setValueAt(String.format("0x%08X", cpu.hi), 1, 1);
		setValueAt(String.format("0x%08X", cpu.pc), 1, 3);
		for(int i = 0; i < 32; i++) {
			setValueAt(String.format("0x%08X", cpu.gpr[i]), 2 + (i / 2), 1 + 2 * (i % 2));
		}
	}
}

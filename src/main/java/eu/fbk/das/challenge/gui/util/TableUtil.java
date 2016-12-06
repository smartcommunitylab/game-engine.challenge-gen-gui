package eu.fbk.das.challenge.gui.util;

import java.util.Vector;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * Utility class for working with JTable
 */
public class TableUtil {

	public static Vector<Object> getSelectedList(JTable table, Integer rowFrom) {
		DefaultTableModel tm = (DefaultTableModel) table.getModel();
		Vector<Object> rowData = (Vector<Object>) tm.getDataVector().elementAt(
				rowFrom);
		return rowData;
	}

	public static void removeSelected(JTable table) {
		int rowIndex = table.getSelectedRow();
		DefaultTableModel tm = (DefaultTableModel) table.getModel();
		tm.removeRow(rowIndex);
	}

	public static void addRowAt(JTable table, Object obj, int index) {
		DefaultTableModel tm = (DefaultTableModel) table.getModel();
		tm.insertRow(index, (Vector) obj);
	}

}

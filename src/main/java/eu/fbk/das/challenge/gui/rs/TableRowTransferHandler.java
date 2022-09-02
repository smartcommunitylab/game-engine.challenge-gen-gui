package eu.fbk.das.challenge.gui.rs;

import eu.fbk.das.challenge.gui.util.TableUtil;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragSource;
import java.util.Vector;

/**
 * Handler for moving row inside {@link JTable}
 */
public class TableRowTransferHandler extends TransferHandler {

    private static final Logger logger = Logger.getLogger(TableRowTransferHandler.class);

    private static final long serialVersionUID = -5005950304881134370L;

    private final DataFlavor localObjectFlavor = new DataFlavor(Integer.class,
            "Integer Row Index");
    private JTable table = null;

    public TableRowTransferHandler(JTable table) {
        this.table = table;
    }

    /*
    @Override
    protected Transferable createTransferable(JComponent c) {
        assert (c == table);
        return new DataHandler(new Integer(table.getSelectedRow()),
                localObjectFlavor.getMimeType());
    }*/

    @Override
    public boolean canImport(TransferHandler.TransferSupport info) {
        boolean b = info.getComponent() == table && info.isDrop()
                && info.isDataFlavorSupported(localObjectFlavor);
        table.setCursor(b ? DragSource.DefaultMoveDrop
                : DragSource.DefaultMoveNoDrop);
        return b;
    }

    @Override
    public int getSourceActions(JComponent c) {
        return TransferHandler.COPY_OR_MOVE;
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport info) {
        JTable target = (JTable) info.getComponent();
        JTable.DropLocation dl = (JTable.DropLocation) info.getDropLocation();
        int index = dl.getRow();
        int max = table.getModel().getRowCount();
        if (index < 0 || index > max) {
            index = max;
        }
        target.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

        try {
            Integer rowFrom = (Integer) info.getTransferable().getTransferData(
                    localObjectFlavor);
            if (rowFrom != -1 && rowFrom != index) {

                int[] rows = table.getSelectedRows();
                int dist = 0;
                for (int row : rows) {
                    if (index > row) {
                        dist++;
                    }
                }
                index -= dist;

                Vector<Object> list = TableUtil.getSelectedList(table, rowFrom);
                TableUtil.removeSelected(table);
                TableUtil.addRowAt(table, list, index);

                return true;
            }
        } catch (Exception e) {
            logger.error(e);
        }
        return false;
    }

    @Override
    protected void exportDone(JComponent c, Transferable t, int act) {
        if (act == TransferHandler.MOVE || act == TransferHandler.NONE) {
            table.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }
}

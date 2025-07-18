package jmri.jmrit.throttle;

import java.awt.Cursor;
import java.awt.datatransfer.*;
import java.awt.dnd.DragSource;
import java.io.IOException;

import javax.activation.ActivationDataFlavor;
import javax.activation.DataHandler;
import javax.swing.*;

/**
 *
 * @author lionel
 */
public class ThrottlesTableRowTransferHandler extends TransferHandler {

   private final DataFlavor localObjectFlavor = new ActivationDataFlavor(Integer.class, "application/x-java-Integer;class=java.lang.Integer", "Integer Row Index");
   private JTable           table             = null;

    public ThrottlesTableRowTransferHandler(JTable throttleFrames) {
        this.table = throttleFrames;
    }

   @Override
   protected Transferable createTransferable(JComponent c) {
      assert (c == table);
      return new DataHandler(new Integer(table.getSelectedRow()), localObjectFlavor.getMimeType());
   }

   @Override
   public boolean canImport(TransferHandler.TransferSupport info) {
      boolean b = info.getComponent() == table && info.isDrop() && info.isDataFlavorSupported(localObjectFlavor);
      table.setCursor(b ? DragSource.DefaultMoveDrop : DragSource.DefaultMoveNoDrop);
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
      if (index < 0 || index > max)
         index = max;
      target.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      try {
         Integer rowFrom = (Integer) info.getTransferable().getTransferData(localObjectFlavor);
         if (rowFrom != -1 && rowFrom != index) {
            ((ThrottlesTableModel)table.getModel()).moveValueAt(rowFrom, index);
            if (index > rowFrom)
               index--;
            target.getSelectionModel().addSelectionInterval(index, index);
            return true;
         }
      } catch (UnsupportedFlavorException | IOException e) {
         e.printStackTrace();
      }
      return false;
   }

   @Override
   protected void exportDone(JComponent c, Transferable t, int act) {
      if ((act == TransferHandler.MOVE) || (act == TransferHandler.NONE)) {
         table.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      }
   }

    
}

package jmri.jmrit.throttle;

import java.awt.Cursor;
import java.awt.datatransfer.*;
import java.awt.dnd.DragSource;
import java.io.IOException;
import java.util.ArrayList;

import javax.activation.ActivationDataFlavor;
import javax.activation.DataHandler;
import javax.swing.*;

import jmri.jmrit.roster.RosterEntry;
import jmri.util.datatransfer.RosterEntrySelection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lionel
 */
public class ThrottlesTableTransferHandler extends TransferHandler {

   private final DataFlavor throttleControlObjectFlavor = new ActivationDataFlavor(ThrottleControler.class, "application/x-jmri-throttleControler", "Throttle controler");
   private JTable           table             = null;

    public ThrottlesTableTransferHandler(JTable throttleControlers) {
        this.table = throttleControlers;
    }

   @Override
   protected Transferable createTransferable(JComponent c) {
      assert (c == table);
      ThrottleControler tf = ((ThrottlesTableModel)table.getModel()).getValueAt(table.getSelectedRow(), table.getSelectedColumn());      
      return new DataHandler(tf, throttleControlObjectFlavor.getMimeType());
   }

   @Override
   public boolean canImport(TransferHandler.TransferSupport info) {
      boolean b = info.getComponent() == table && info.isDrop() && ( 
              info.isDataFlavorSupported(throttleControlObjectFlavor) || info.isDataFlavorSupported(RosterEntrySelection.rosterEntryFlavor));
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
       // dl.getRow(); dl.getColumn() ;      
       target.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
       if (info.isDataFlavorSupported(throttleControlObjectFlavor)) {
           try {

               ThrottleControler tf = (ThrottleFrame) info.getTransferable().getTransferData(throttleControlObjectFlavor);
               if (tf != null) {
                   ((ThrottlesTableModel) table.getModel()).moveThrottleControler(tf, dl.getRow(), dl.getColumn());
                   return true;
               }
           } catch (UnsupportedFlavorException | IOException e) {
               log.error("Could not drag'n drop throttle frame.", e);
           }
       }
       if (info.isDataFlavorSupported(RosterEntrySelection.rosterEntryFlavor)) {
           try {
               ArrayList<RosterEntry> REs = RosterEntrySelection.getRosterEntries(info.getTransferable());
               ThrottleControlersContainer tw = ((ThrottlesTableModel) table.getModel()).getThrottleControlersContainerAt(dl.getColumn());
               if (tw == null) {
                   
               }
               for (RosterEntry re : REs) {
                   ThrottleControler tf = tw.newThrottleControler();
                   tf.toFront();
                   tf.setRosterEntry(re);
               }
           } catch (UnsupportedFlavorException | IOException e) {
               log.error("Could not drag'n drop roster entry.", e);
           }
       }
      return false;
   }

   @Override
   protected void exportDone(JComponent c, Transferable t, int act) {
      if ((act == TransferHandler.MOVE) || (act == TransferHandler.NONE)) {
         table.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      }
   }

   private final static Logger log = LoggerFactory.getLogger(ThrottlesTableTransferHandler.class);    
}

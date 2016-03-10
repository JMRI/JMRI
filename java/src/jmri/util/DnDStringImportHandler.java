package jmri.util;

/**
 * Simple TransferHandler that overwrites the text in a JTextField component.
 * Use JTextField default handler if you want insertion
 * <P>
 *
 * @author Pete Cressman Copyright 2010
 * @version $Revision$
 */
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.TransferHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DnDStringImportHandler extends TransferHandler {

    /**
     *
     */
    private static final long serialVersionUID = 2107475489891148707L;

    /////////////////////import
    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
        //if (log.isDebugEnabled()) log.debug("DnDStringImportHandler.canImport ");

        for (int k = 0; k < transferFlavors.length; k++) {
            if (transferFlavors[k].equals(DataFlavor.stringFlavor)) {
                return true;
            }
        }
        return false;
    }

    public boolean importData(JComponent comp, Transferable tr) {
        //if (log.isDebugEnabled()) log.debug("DnDStringImportHandler.importData ");
        DataFlavor[] flavors = new DataFlavor[]{DataFlavor.stringFlavor};

        if (!canImport(comp, flavors)) {
            return false;
        }

        try {
            if (tr.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                String data = (String) tr.getTransferData(DataFlavor.stringFlavor);
                JTextField field = (JTextField) comp;
                field.setText(data);
                //Notify listeners drop happened
                field.firePropertyChange("DnDrop", 0, 1);
                return true;
            }
        } catch (UnsupportedFlavorException ufe) {
            log.warn("DnDStringImportHandler.importData: " + ufe.getMessage());
        } catch (IOException ioe) {
            log.warn("DnDStringImportHandler.importData: " + ioe.getMessage());
        }
        return false;
    }
    private final static Logger log = LoggerFactory.getLogger(DnDStringImportHandler.class.getName());
}

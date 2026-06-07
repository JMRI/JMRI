
package jmri.jmrit.throttle.list;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import jmri.jmrit.throttle.implementation.SimpleThrottlePanel;
import jmri.jmrit.throttle.implementation.ThrottleFrame;
import jmri.jmrit.throttle.interfaces.ThrottleControllerUI;

class ThrottleUITransferable implements Transferable {
    public static final DataFlavor ThrottleFrameObjectFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=" + ThrottleFrame.class.getName(), "JMRI Throttle Controller UI");
    public static final DataFlavor ThrottleSimplePanelObjectFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=" + SimpleThrottlePanel.class.getName(), "JMRI Throttle Controller UI");

    private ThrottleControllerUI tf;
    private DataFlavor flavor;

    public ThrottleUITransferable(ThrottleControllerUI tf) {
        this.tf = tf;
        if (tf instanceof ThrottleFrame) {
            flavor = ThrottleFrameObjectFlavor;
        }
        if (tf instanceof SimpleThrottlePanel) {
            flavor = ThrottleSimplePanelObjectFlavor;
        }
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] { flavor };
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(ThrottleFrameObjectFlavor) || flavor.equals(ThrottleSimplePanelObjectFlavor);        
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        return tf;
    }

}
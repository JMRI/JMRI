package jmri.jmrit.throttle.list;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import jmri.jmrit.throttle.interfaces.ThrottleControllerUI;

/**
 * A class to handle transfers (drag'n drop) of throttle UI controllers within the throttle list panel 
 * 
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Lionel Jeanson
 */

class ThrottleUITransferable implements Transferable { 
    public static final DataFlavor ThrottleControllerUIObjectFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=" + ThrottleControllerUI.class.getName(), "JMRI Throttle Controller UI");
    private ThrottleControllerUI tcui;

    public ThrottleUITransferable(ThrottleControllerUI tcui) {
        this.tcui = tcui;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] { ThrottleControllerUIObjectFlavor };
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(ThrottleControllerUIObjectFlavor);        
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        return tcui;
    }

}

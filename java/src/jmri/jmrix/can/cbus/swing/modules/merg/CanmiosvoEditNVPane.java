package jmri.jmrix.can.cbus.swing.modules.merg;

import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeNVTableDataModel;
import jmri.jmrix.can.cbus.swing.modules.base.Servo8BaseEditNVPane;

/**
 * Node Variable edit frame for a MERG CANMIO-SVO module
 *
 * @author Andrew Crosland Copyright (C) 2021
 */
public class CanmiosvoEditNVPane extends Servo8BaseEditNVPane {
    
    protected CanmiosvoEditNVPane(CbusNodeNVTableDataModel dataModel, CbusNode node) {
        super(dataModel, node);
    }
    
}

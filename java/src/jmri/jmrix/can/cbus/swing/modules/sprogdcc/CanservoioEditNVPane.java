package jmri.jmrix.can.cbus.swing.modules.sprogdcc;

import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeNVTableDataModel;
import jmri.jmrix.can.cbus.swing.modules.base.Servo8BaseEditNVPane;

/**
 * Node Variable edit frame for a SPROG DCC CANSERVOIO module
 *
 * @author Andrew Crosland Copyright (C) 2021
 */
public class CanservoioEditNVPane extends Servo8BaseEditNVPane {
    
    protected CanservoioEditNVPane(CbusNodeNVTableDataModel dataModel, CbusNode node) {
        super(dataModel, node);
    }
    
}

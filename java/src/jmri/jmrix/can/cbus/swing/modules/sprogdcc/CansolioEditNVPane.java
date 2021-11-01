package jmri.jmrix.can.cbus.swing.modules.sprogdcc;

import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeNVTableDataModel;
import jmri.jmrix.can.cbus.swing.modules.base.Sol8BaseEditNVPane;

/**
 * Node Variable edit frame for a MERG CANACC8 CBUS module
 *
 * @author Andrew Crosland Copyright (C) 2021
 */
public class CansolioEditNVPane extends Sol8BaseEditNVPane {
    
    protected CansolioEditNVPane(CbusNodeNVTableDataModel dataModel, CbusNode node) {
        super(dataModel, node);
    }
    
}

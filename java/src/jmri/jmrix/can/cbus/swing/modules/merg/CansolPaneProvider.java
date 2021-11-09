package jmri.jmrix.can.cbus.swing.modules.merg;

import javax.annotation.Nonnull;

import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeNVTableDataModel;
import jmri.jmrix.can.cbus.swing.modules.*;
import jmri.jmrix.can.cbus.swing.modules.base.Sol8BasePaneProvider;

import org.openide.util.lookup.ServiceProvider;

/**
 * Returns configuration objects for a MERG CANACC8
 *
 * @author Andrew Crosland Copyright (C) 2021
 */
@ServiceProvider(service = CbusConfigPaneProvider.class)
public class CansolPaneProvider extends Sol8BasePaneProvider {
    
    String type = "CANSOL";
    
    public CansolPaneProvider() {
        super();
    }
    
    /** {@inheritDoc} */
    @Override
    @Nonnull
    public String getModuleType() {
        return type;
    }

    /** {@inheritDoc} */
    @Override
    public AbstractEditNVPane getEditNVFrame(CbusNodeNVTableDataModel dataModel, CbusNode node) {
        _nVarEditFrame = new CansolEditNVPane(dataModel, node);
        return _nVarEditFrame.getContent();
    }
}

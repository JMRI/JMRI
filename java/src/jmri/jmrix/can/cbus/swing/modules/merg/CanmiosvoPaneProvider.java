package jmri.jmrix.can.cbus.swing.modules.merg;

import javax.annotation.Nonnull;

import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeNVTableDataModel;
import jmri.jmrix.can.cbus.swing.modules.*;
import jmri.jmrix.can.cbus.swing.modules.base.Servo8BasePaneProvider;

import org.openide.util.lookup.ServiceProvider;

/**
 * Returns configuration objects for a MERG CANMIO-SVO
 *
 * @author Andrew Crosland Copyright (C) 2021
 */
@ServiceProvider(service = CbusConfigPaneProvider.class)
public class CanmiosvoPaneProvider extends Servo8BasePaneProvider {
    
    String type = "CANMIO_SVO";
    
    public CanmiosvoPaneProvider() {
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
        _nVarEditFrame = new CanmiosvoEditNVPane(dataModel, node);
        return _nVarEditFrame.getContent();
    }
}

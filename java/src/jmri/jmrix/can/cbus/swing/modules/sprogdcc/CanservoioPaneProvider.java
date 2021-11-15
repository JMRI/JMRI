package jmri.jmrix.can.cbus.swing.modules.sprogdcc;

import javax.annotation.Nonnull;

import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeNVTableDataModel;
import jmri.jmrix.can.cbus.swing.modules.AbstractEditNVPane;
import jmri.jmrix.can.cbus.swing.modules.CbusConfigPaneProvider;
import jmri.jmrix.can.cbus.swing.modules.base.Servo8BasePaneProvider;

import org.openide.util.lookup.ServiceProvider;

/**
 * Returns configuration objects for a SPROG DCC CANSERVOIO
 *
 * @author Andrew Crosland Copyright (C) 2021
 */
@ServiceProvider(service = CbusConfigPaneProvider.class)
public class CanservoioPaneProvider extends Servo8BasePaneProvider {
    
    String type = "CANSERVOIO";
    
    public CanservoioPaneProvider() {
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
        _nVarEditFrame = new CanservoioEditNVPane(dataModel, node);
        return _nVarEditFrame.getContent();
    }
}

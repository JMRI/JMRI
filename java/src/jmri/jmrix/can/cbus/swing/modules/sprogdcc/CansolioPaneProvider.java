package jmri.jmrix.can.cbus.swing.modules.sprogdcc;

import javax.annotation.Nonnull;

import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeNVTableDataModel;
import jmri.jmrix.can.cbus.swing.modules.AbstractEditNVPane;
import jmri.jmrix.can.cbus.swing.modules.CbusConfigPaneProvider;
import jmri.jmrix.can.cbus.swing.modules.base.Sol8BasePaneProvider;

import org.openide.util.lookup.ServiceProvider;

/**
 * Returns configuration objects for a SPROG DCC CANSOLIO
 *
 * @author Andrew Crosland Copyright (C) 2021
 */
@ServiceProvider(service = CbusConfigPaneProvider.class)
public class CansolioPaneProvider extends Sol8BasePaneProvider {
    
    String type = "CANSOLIO";
    
    public CansolioPaneProvider() {
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
        if (_nVarEditFrame == null ){
            _nVarEditFrame = new CansolioEditNVPane(dataModel, node);
        }
        return _nVarEditFrame.getContent();
    }
}

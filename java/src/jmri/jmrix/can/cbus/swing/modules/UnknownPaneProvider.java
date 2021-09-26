package jmri.jmrix.can.cbus.swing.modules;


import javax.annotation.Nonnull;
import javax.swing.JPanel;

import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeNVTableDataModel;

import org.openide.util.lookup.ServiceProvider;

/**
 * Returns configuration objects for an unknown module
 *
 * @author Andrew Crosland Copyright (C) 2021
 */
@ServiceProvider(service = CbusConfigPaneProvider.class)
public class UnknownPaneProvider extends CbusConfigPaneProvider  {
    
    String type = Bundle.getMessage("Unknown");
    
    public UnknownPaneProvider() {
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
    public String getNVNameByIndex(int index) {
        return Bundle.getMessage("UnknownNv");
    }

    /** {@inheritDoc} */
    @Override
    public AbstractEditNVPane getEditNVFrameInstance() {
        return _nVarEditFrame;
    }

    /** {@inheritDoc} */
    @Override
    public JPanel getEditNVFrame(CbusNodeNVTableDataModel dataModel, CbusNode node) {
        if (_nVarEditFrame == null ){
            _nVarEditFrame = new UnknownEditNVPane(dataModel, node);
        }
        return _nVarEditFrame.getContent();
    }
}

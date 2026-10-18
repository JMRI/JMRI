package jmri.jmrix.can.cbus.swing.modules;


import javax.annotation.Nonnull;

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
    public AbstractEditNVPane getEditNVFrame(CbusNodeNVTableDataModel dataModel, CbusNode node) {
        // Always a new pane for this node and data model. Providers are shared by
        // all nodes of a type, so a cached pane would stay bound to the first node and
        // data model it was created for, and gather duplicate components and listeners.
        _nVarEditFrame = new UnknownEditNVPane(dataModel, node);
        return _nVarEditFrame.getContent();
    }
}

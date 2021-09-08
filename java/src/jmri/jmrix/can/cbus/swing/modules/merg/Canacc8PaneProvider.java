package jmri.jmrix.can.cbus.swing.modules.merg;

import java.util.*;

import javax.annotation.Nonnull;
import javax.swing.JPanel;

import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeNVTableDataModel;
import jmri.jmrix.can.cbus.swing.modules.*;

import org.openide.util.lookup.ServiceProvider;

/**
 * Returns configuration objects for a MERG CANACC8
 *
 * @author Andrew Crosland Copyright (C) 2021
 */
@ServiceProvider(service = CbusConfigPaneProvider.class)
public class Canacc8PaneProvider extends CbusConfigPaneProvider {
    
    String type = "CANACC8";
    
    public Canacc8PaneProvider() {
        super();
    }
    
    /** {@inheritDoc} */
    @Override
    @Nonnull
    public String getModuleType() {
        return type;
    }

    /**
     * Hashmap for decoding NV names
     */
    private static final Map<Integer, String> nvMap = createNvMap();

    /*
     * Populate hashmap with nv strings
     *
     */
    private static Map<Integer, String> createNvMap() {
        Map<Integer, String> result = new HashMap<>();
        result.put(0, Bundle.getMessage("OutputX", 1));
        result.put(1, Bundle.getMessage("OutputX", 2));
        result.put(2, Bundle.getMessage("OutputX", 3));
        result.put(3, Bundle.getMessage("OutputX", 4));
        result.put(4, Bundle.getMessage("OutputX", 5));
        result.put(5, Bundle.getMessage("OutputX", 6));
        result.put(6, Bundle.getMessage("OutputX", 7));
        result.put(7, Bundle.getMessage("OutputX", 8));
        result.put(8, Bundle.getMessage("FeedbackDelay"));
        result.put(9, Bundle.getMessage("StartupPosition"));
        result.put(10, Bundle.getMessage("StartupMove"));
        return Collections.unmodifiableMap(result);
    }
    
    /** {@inheritDoc} */
    @Override
    public String getNVNameByIndex(int index) {
        // look for the NV
        String nv = nvMap.get(index);
        if (nv == null) {
            return Bundle.getMessage("UnknownNv");
        } else {
            return nv;
        }
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
            _nVarEditFrame = new Canacc8EditNVPane(dataModel, node);
        }
        return _nVarEditFrame.getContent();
    }

    /** {@inheritDoc} */
    @Override
    public JPanel getNewEditNVFrame(CbusNodeNVTableDataModel dataModel, CbusNode node) {
        _nVarEditFrame = new Canacc8EditNVPane(dataModel, node);
        return _nVarEditFrame.getContent();
    }
}

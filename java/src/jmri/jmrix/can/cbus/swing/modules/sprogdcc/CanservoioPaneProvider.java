package jmri.jmrix.can.cbus.swing.modules.sprogdcc;

import java.util.*;

import javax.annotation.Nonnull;
import javax.swing.JPanel;

import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeNVTableDataModel;
import jmri.jmrix.can.cbus.swing.modules.AbstractEditNVPane;
import jmri.jmrix.can.cbus.swing.modules.CbusConfigPaneProvider;

import org.openide.util.lookup.ServiceProvider;

/**
 * Returns configuration objects for a SPROG DCC CANSERVOIO
 *
 * @author Andrew Crosland Copyright (C) 2021
 */
@ServiceProvider(service = CbusConfigPaneProvider.class)
public class CanservoioPaneProvider extends CbusConfigPaneProvider {
    
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
        result.put(0, "Output 1"); // NOI18N
        result.put(1, "Output 2"); // NOI18N
        result.put(2, "Output 3"); // NOI18N
        result.put(3, "Output 4"); // NOI18N
        result.put(4, "Output 5"); // NOI18N
        result.put(5, "Output 6"); // NOI18N
        result.put(6, "Output 7"); // NOI18N
        result.put(7, "Output 8"); // NOI18N
        result.put(8, "Feedback Delay"); // NOI18N
        result.put(9, "Startup Position"); // NOI18N
        result.put(10, "Startup Move"); // NOI18N
        return Collections.unmodifiableMap(result);
    }
    
    /** {@inheritDoc} */
    @Override
    public String getNVNameByIndex(int index) {
        // look for the NV
        String nv = nvMap.get(index);
        if (nv == null) {
            return "Unknown NV";
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
            _nVarEditFrame = new CanservoioEditNVPane(dataModel, node);
        }
        return _nVarEditFrame.getContent();
    }

    /** {@inheritDoc} */
    @Override
    public JPanel getNewEditNVFrame(CbusNodeNVTableDataModel dataModel, CbusNode node) {
        _nVarEditFrame = new CanservoioEditNVPane(dataModel, node);
        return _nVarEditFrame.getContent();
    }
}

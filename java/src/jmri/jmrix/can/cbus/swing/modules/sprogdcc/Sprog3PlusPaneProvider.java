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
 * Returns configuration objects for a SPROG DCC SPROG 3 Plus
 *
 * @author Andrew Crosland Copyright (C) 2021
 */
@ServiceProvider(service = CbusConfigPaneProvider.class)
public class Sprog3PlusPaneProvider extends CbusConfigPaneProvider {
    
    String type = "CANSPROG3P";
    
    public Sprog3PlusPaneProvider() {
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
        result.put(0, "Command station number"); // NOI18N
        result.put(1, "User flags"); // NOI18N
        result.put(2, "Operation flags"); // NOI18N
        result.put(3, "Debug flags (not used)"); // NOI18N
        result.put(4, "Prog track power mode"); // NOI18N
        result.put(5, "Prog track current limit"); // NOI18N
        result.put(6, "Input voltage (read only)"); // NOI18N
        result.put(7, "Main track current (read only)"); // NOI18N
        result.put(8, "Accessory packet repeat count"); // NOI18N
        result.put(9, "Multimeter mode"); // NOI18N
        result.put(10, "NN map to DCC (hi)"); // NOI18N
        result.put(11, "NN map to DCC (hi)"); // NOI18N
        result.put(12, "Main track current limit"); // NOI18N
        result.put(13, "Prog track current (read only)"); // NOI18N
        result.put(14, "Main track current high water mark (read only)"); // NOI18N
        result.put(15, "Prog track current high water mark (read only)"); // NOI18N
        result.put(16, "Setup mode (do not use)"); // NOI18N
        result.put(17, "CAN ID"); // NOI18N
        result.put(18, "Node Number (NN) hi"); // NOI18N
        result.put(19, "Node Number (NN) lo"); // NOI18N
        result.put(20, "DCC preamble bits"); // NOI18N
        result.put(21, "CAN disable"); // NOI18N
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
            _nVarEditFrame = new Sprog3PlusEditNVPane(dataModel, node);
        }
        return _nVarEditFrame.getContent();
    }

    /** {@inheritDoc} */
    @Override
    public JPanel getNewEditNVFrame(CbusNodeNVTableDataModel dataModel, CbusNode node) {
        _nVarEditFrame = new Sprog3PlusEditNVPane(dataModel, node);
        return _nVarEditFrame.getContent();
    }
}

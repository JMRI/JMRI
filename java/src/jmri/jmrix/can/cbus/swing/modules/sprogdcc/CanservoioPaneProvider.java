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
    
    public static final int OUTPUT1 = 1;
    public static final int OUTPUT2 = 2;
    public static final int OUTPUT3 = 3;
    public static final int OUTPUT4 = 4;
    public static final int OUTPUT5 = 5;
    public static final int OUTPUT6 = 6;
    public static final int OUTPUT7 = 7;
    public static final int OUTPUT8 = 8;
    public static final int FEEDBACK_DELAY = 9;
    public static final int STARTUP_POSITION = 10;
    public static final int STARTUP_MOVE = 11;

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
        result.put(0, "Error - invalid NV index");
        result.put(OUTPUT1, Bundle.getMessage("OutputX", 1));
        result.put(OUTPUT2, Bundle.getMessage("OutputX", 2));
        result.put(OUTPUT3, Bundle.getMessage("OutputX", 3));
        result.put(OUTPUT4, Bundle.getMessage("OutputX", 4));
        result.put(OUTPUT5, Bundle.getMessage("OutputX", 5));
        result.put(OUTPUT6, Bundle.getMessage("OutputX", 6));
        result.put(OUTPUT7, Bundle.getMessage("OutputX", 7));
        result.put(OUTPUT8, Bundle.getMessage("OutputX", 8));
        result.put(FEEDBACK_DELAY, Bundle.getMessage("FeedbackDelay"));
        result.put(STARTUP_POSITION, Bundle.getMessage("StartupPosition"));
        result.put(STARTUP_MOVE, Bundle.getMessage("StartupMove"));
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

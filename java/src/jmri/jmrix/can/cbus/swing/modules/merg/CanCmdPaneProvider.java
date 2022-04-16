package jmri.jmrix.can.cbus.swing.modules.merg;

import java.util.*;

import javax.annotation.Nonnull;

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
public class CanCmdPaneProvider extends CbusConfigPaneProvider {
    
    String type = "CANCMD";
    
    public static final int CMD_STATION_NUMBER = 1;
    public static final int USER_FLAGS = 2;
    public static final int OPERATIONS_FLAGS = 3;
    public static final int DEBUG_FLAGS = 4;
    public static final int WALKABOUT_TIMEOUT = 5;
    public static final int MAIN_TRACK_CURRENT_LIMIT = 6;
    public static final int PROG_TRACK_CURRENT_LIMIT = 7;
    public static final int CURRENT_MULTIPLIER = 8;
    public static final int INC_CURRENT_FOR_ACK = 9;
    public static final int UNUSED_NV10 = 10;
    public static final int NN_MAP_DCC_HI = 11;
    public static final int NN_MAP_DCC_LO = 12;
    public static final int SEND_CURRENT_INTERVAL = 13;
    public static final int SOD_DELAY = 14;
    public static final int UNUSED_NV15 = 15;
    public static final int UNUSED_NV16 = 16;
    
    public CanCmdPaneProvider() {
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
        result.put(CMD_STATION_NUMBER, Bundle.getMessage("CmdStaNo"));
        result.put(USER_FLAGS, Bundle.getMessage("UserFlags"));
        result.put(OPERATIONS_FLAGS, Bundle.getMessage("OperationsFlags"));
        result.put(DEBUG_FLAGS, Bundle.getMessage("DebugFlags"));
        result.put(WALKABOUT_TIMEOUT, Bundle.getMessage("WalkaboutTimeout"));
        result.put(MAIN_TRACK_CURRENT_LIMIT, Bundle.getMessage("MainTrackCurrentLimit"));
        result.put(PROG_TRACK_CURRENT_LIMIT, Bundle.getMessage("ProgTrackCurrentLimit"));
        result.put(CURRENT_MULTIPLIER, Bundle.getMessage("CurrentMultiplier"));
        result.put(INC_CURRENT_FOR_ACK, Bundle.getMessage("IncCurrentForAck"));
        result.put(NN_MAP_DCC_HI, Bundle.getMessage("NnMapToDccHi"));
        result.put(NN_MAP_DCC_LO, Bundle.getMessage("NnMapToDccLo"));
        result.put(SEND_CURRENT_INTERVAL, Bundle.getMessage("SendCurrentInterval"));
        result.put(SOD_DELAY, Bundle.getMessage("SodDelay"));
        
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
    public AbstractEditNVPane getEditNVFrame(CbusNodeNVTableDataModel dataModel, CbusNode node) {
        _nVarEditFrame = new CanCmdEditNVPane(dataModel, node);
        return _nVarEditFrame.getContent();
    }
}

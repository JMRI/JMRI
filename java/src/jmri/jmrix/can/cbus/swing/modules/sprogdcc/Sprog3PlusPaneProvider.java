package jmri.jmrix.can.cbus.swing.modules.sprogdcc;

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
public class Sprog3PlusPaneProvider extends CbusConfigPaneProvider {
    
    String type = "Pi-SPROG 3";
    
    public static final int CMD_STATION_NUMBER = 1;
    public static final int USER_FLAGS = 2;
    public static final int OPERATIONS_FLAGS = 3;
    public static final int DEBUG_FLAGS = 4;
    public static final int PROG_TRACK_POWER_MODE = 5;
    public static final int PROG_TRACK_CURRENT_LIMIT = 6;
    public static final int INPUT_VOLTAGE = 7;
    public static final int MAIN_TRACK_CURRENT = 8;
    public static final int ACCY_PACKET_REPEAT_COUNT = 9;
    public static final int MULTIMETER_MODE = 10;
    public static final int NN_MAP_DCC_HI = 11;
    public static final int NN_MAP_DCC_LO = 12;
    public static final int MAIN_TRACK_CURRENT_LIMIT = 13;
    public static final int PROG_TRACK_CURRENT = 14;
    public static final int MAIN_HIGH_WATER_MARK = 15;
    public static final int PROG_HIGH_WATER_MARK = 16;
    public static final int SETUP = 17;
    public static final int CANID = 18;
    public static final int NN_HI = 19;
    public static final int NN_LO = 20;
    public static final int DCC_PREAMBLE = 21;
    public static final int CAN_DISABLE = 22;
    
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
        result.put(0, "Error - invalid NV index");
        result.put(CMD_STATION_NUMBER, Bundle.getMessage("CmdStaNo"));
        result.put(USER_FLAGS, Bundle.getMessage("UserFlags"));
        result.put(OPERATIONS_FLAGS, Bundle.getMessage("OperationsFlags"));
        result.put(DEBUG_FLAGS, Bundle.getMessage("DebugFlags"));
        result.put(PROG_TRACK_POWER_MODE, Bundle.getMessage("ProgTrackPowerMode"));
        result.put(PROG_TRACK_CURRENT_LIMIT, Bundle.getMessage("ProgTrackCurrentLimit"));
        result.put(INPUT_VOLTAGE, Bundle.getMessage("InputVoltage"));
        result.put(MAIN_TRACK_CURRENT, Bundle.getMessage("MainTrackCurrent"));
        result.put(ACCY_PACKET_REPEAT_COUNT, Bundle.getMessage("AccessoryPacketRepeatCount"));
        result.put(MULTIMETER_MODE, Bundle.getMessage("MultimeterMode"));
        result.put(NN_MAP_DCC_HI, Bundle.getMessage("NnMapToDccHi"));
        result.put(NN_MAP_DCC_LO, Bundle.getMessage("NnMapToDccLo"));
        result.put(MAIN_TRACK_CURRENT_LIMIT, Bundle.getMessage("MainTrackCurrentLimit"));
        result.put(PROG_TRACK_CURRENT, Bundle.getMessage("ProgTackCurrent"));
        result.put(MAIN_HIGH_WATER_MARK, Bundle.getMessage("MainTrackCurrentHWM"));
        result.put(PROG_HIGH_WATER_MARK, Bundle.getMessage("ProgTrackCurrentHWM"));
        result.put(SETUP, Bundle.getMessage("SetupMode"));
        result.put(CANID, Bundle.getMessage("CanId"));
        result.put(NN_HI, Bundle.getMessage("NodeNumberHi"));
        result.put(NN_LO, Bundle.getMessage("NodeNumberLo"));
        result.put(DCC_PREAMBLE, Bundle.getMessage("DccPreambleBits"));
        result.put(CAN_DISABLE, Bundle.getMessage("CanDisable"));
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
        _nVarEditFrame = new Sprog3PlusEditNVPane(dataModel, node);
        return _nVarEditFrame.getContent();
    }
}

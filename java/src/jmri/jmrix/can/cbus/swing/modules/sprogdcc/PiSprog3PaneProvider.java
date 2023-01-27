package jmri.jmrix.can.cbus.swing.modules.sprogdcc;

import java.util.*;

import javax.annotation.Nonnull;

import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeNVTableDataModel;
import jmri.jmrix.can.cbus.swing.modules.AbstractEditNVPane;
import jmri.jmrix.can.cbus.swing.modules.CbusConfigPaneProvider;

import org.openide.util.lookup.ServiceProvider;

/**
 * Returns configuration objects for a SPROG DCC [Pi-]SPROG 3 [v2|Plus]
 *
 * @author Andrew Crosland Copyright (C) 2021
 */
@ServiceProvider(service = CbusConfigPaneProvider.class)
public class PiSprog3PaneProvider extends CbusConfigPaneProvider {
    
    String type = "Pi-SPROG 3";
    
    public static final int SETUP = 1;
    public static final int ZTC_MODE = 2;
    public static final int BLUELINE_MODE = 3;
    public static final int ACK_SENSITIVITY = 4;
    public static final int CMD_STATION_MODE = 5;
    public static final int CURRENT_LIMIT = 6;
    public static final int INPUT_VOLTAGE = 7;
    public static final int TRACK_CURRENT = 8;
    public static final int ACCY_PACKET_REPEAT_COUNT = 9;
    public static final int MULTIMETER_MODE = 10;
    public static final int DCC_PREAMBLE = 11;
    public static final int USER_FLAGS = 12;
    public static final int OPERATIONS_FLAGS = 13;
    
    // These may be overridden in scripts for unusual use cases
    public static int MIN_CANID = 100;
    public static int MAX_CANID = 127;
    public static int MIN_NN = 65520;
    public static int MAX_NN = 65535;

    public PiSprog3PaneProvider() {
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
        result.put(SETUP, Bundle.getMessage("SetupMode"));
        result.put(ZTC_MODE, Bundle.getMessage("ZtcMode"));
        result.put(BLUELINE_MODE, Bundle.getMessage("BluelineMode"));
        result.put(ACK_SENSITIVITY, Bundle.getMessage("AckSensitivity"));
        result.put(CMD_STATION_MODE, Bundle.getMessage("CmdStaMode"));
        result.put(CURRENT_LIMIT, Bundle.getMessage("CurrentLimit"));
        result.put(INPUT_VOLTAGE, Bundle.getMessage("InputVoltage"));
        result.put(TRACK_CURRENT, Bundle.getMessage("TrackCurrent"));
        result.put(ACCY_PACKET_REPEAT_COUNT, Bundle.getMessage("AccessoryPacketRepeatCount"));
        result.put(MULTIMETER_MODE, Bundle.getMessage("MultimeterMode"));
        result.put(DCC_PREAMBLE, Bundle.getMessage("DccPreambleBits"));
        result.put(USER_FLAGS, Bundle.getMessage("UserFlags"));
        result.put(OPERATIONS_FLAGS, Bundle.getMessage("OperationsFlags"));
        result.put(USER_FLAGS, Bundle.getMessage("UserFlags"));
        result.put(OPERATIONS_FLAGS, Bundle.getMessage("OperationsFlags"));
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
        _nVarEditFrame = new PiSprog3EditNVPane(dataModel, node);
        return _nVarEditFrame.getContent();
    }
}

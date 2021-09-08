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
        result.put(0, Bundle.getMessage("CmdStaNo"));
        result.put(1, Bundle.getMessage("UserFlags"));
        result.put(2, Bundle.getMessage("OperationsFlags"));
        result.put(3, Bundle.getMessage("DebugFlags"));
        result.put(4, Bundle.getMessage("ProgTrackPowerMode"));
        result.put(5, Bundle.getMessage("ProgTrackCurrentLimit"));
        result.put(6, Bundle.getMessage("InputVoltage"));
        result.put(7, Bundle.getMessage("MainTrackCurrent"));
        result.put(8, Bundle.getMessage("AccessoryPacketRepeatCount"));
        result.put(9, Bundle.getMessage("MultimeterMode"));
        result.put(10, Bundle.getMessage("NnMapToDccHi"));
        result.put(11, Bundle.getMessage("NnMapToDccLo"));
        result.put(12, Bundle.getMessage("MainTrackCurrentLimit"));
        result.put(13, Bundle.getMessage("ProgTackCurrent"));
        result.put(14, Bundle.getMessage("MainTrackCurrentHWM"));
        result.put(15, Bundle.getMessage("ProgTrackCurrentHWM"));
        result.put(16, Bundle.getMessage("SetupMode"));
        result.put(17, Bundle.getMessage("CanId"));
        result.put(18, Bundle.getMessage("NodeNumberHi"));
        result.put(19, Bundle.getMessage("NodeNumberLo"));
        result.put(20, Bundle.getMessage("DccPreambleBits"));
        result.put(21, Bundle.getMessage("CanDisable"));
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

package jmri.jmrix.can.cbus.swing.modules.sprogdcc;

import java.util.*;

import javax.annotation.Nonnull;

import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeNVTableDataModel;
import jmri.jmrix.can.cbus.swing.modules.AbstractEditNVPane;
import jmri.jmrix.can.cbus.swing.modules.CbusConfigPaneProvider;

import org.openide.util.lookup.ServiceProvider;

/**
 * Returns configuration objects for a SPROG DCC CANISB
 *
 * @author Andrew Crosland Copyright (C) 2021
 */
@ServiceProvider(service = CbusConfigPaneProvider.class)
public class CanisbPaneProvider extends CbusConfigPaneProvider {
    
    String type = "CANISB";
    
    public static final int RX_ERR_CNT = 1;
    public static final int TX_ERR_CNT = 2;
    public static final int TX_FAIL_CNT = 3;
    public static final int TIMEOUT = 4;
    public static final int SETUP = 5;
    public static final int CAN_DISABLE = 6;
    public static final int CANID = 7;
    public static final int NN_HI = 8;
    public static final int NN_LO = 9;

    public CanisbPaneProvider() {
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
    protected static final Map<Integer, String> nvMap = createNvMap();

    /*
     * Populate hashmap with nv strings
     *
     */
    protected static Map<Integer, String> createNvMap() {
        Map<Integer, String> result = new HashMap<>();
        result.put(0, "Error - invalid NV index");
        result.put(RX_ERR_CNT, Bundle.getMessage("CanRxErrorCount"));
        result.put(TX_ERR_CNT, Bundle.getMessage("CanTxErrorCount"));
        result.put(TX_FAIL_CNT, Bundle.getMessage("CanTxFailureCount"));
        result.put(TIMEOUT, Bundle.getMessage("CanTimeoutCount"));
        result.put(SETUP, Bundle.getMessage("SetupMode"));
        result.put(CAN_DISABLE, Bundle.getMessage("CanDisable"));
        result.put(CANID, Bundle.getMessage("CanId"));
        result.put(NN_HI, Bundle.getMessage("NodeNumberHi"));
        result.put(NN_LO, Bundle.getMessage("NodeNumberLo"));

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
        _nVarEditFrame = new CanisbEditNVPane(dataModel, node);
        return _nVarEditFrame.getContent();
    }
}

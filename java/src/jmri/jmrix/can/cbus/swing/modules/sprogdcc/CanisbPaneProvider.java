package jmri.jmrix.can.cbus.swing.modules.sprogdcc;

import java.util.*;

import javax.annotation.Nonnull;

import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeNVTableDataModel;
import jmri.jmrix.can.cbus.swing.modules.*;

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
    public static final int RX_OVFLW_COUNT = 4;
    public static final int SETUP = 5;
    public static final int CAN_DISABLE = 6;
    public static final int CANID = 7;
    public static final int NN_HI = 8;
    public static final int NN_LO = 9;
    public static final int CAN_ERR_STATUS_1 = 10;
    public static final int CAN_ERR_STATUS_2 = 11;
    public static final int CAN_ERR_CONTROL = 12;
    public static final int CAN_NOM_BIT_RATE_RX_COUNT = 13;
    public static final int CAN_NOM_BIT_RATE_TX_COUNT = 14;
    public static final int CAN_ERR_FREE_COUNT_HI = 15;
    public static final int CAN_ERR_FREE_COUNT_LO = 16;
    public static final int CAN_DIAGNOSTICS_HI = 17;
    public static final int CAN_DIAGNOSTICS_LO = 18;
    public static final int HOST_TX_CNT_T = 19;
    public static final int HOST_TX_CNT_U = 20;
    public static final int HOST_TX_CNT_H = 21;
    public static final int HOST_TX_CNT_L = 22;
    public static final int HOST_RX_CNT_T = 23;
    public static final int HOST_RX_CNT_U = 24;
    public static final int HOST_RX_CNT_H = 25;
    public static final int HOST_RX_CNT_L = 26;
    public static final int CAN_TX_CNT_T = 27;
    public static final int CAN_TX_CNT_U = 28;
    public static final int CAN_TX_CNT_H = 29;
    public static final int CAN_TX_CNT_L = 30;
    public static final int CAN_RX_CNT_T = 31;
    public static final int CAN_RX_CNT_U = 32;
    public static final int CAN_RX_CNT_H = 33;
    public static final int CAN_RX_CNT_L = 34;
    public static final int CAN_TX_ARB_DELAY = 35;
    
    // These may be overridden in scripts for unusual use cases
    public static int MIN_CANID = 100;
    public static int MAX_CANID = 127;
    public static int MIN_NN = 65520;
    public static int MAX_NN = 65534;

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
        result.put(RX_OVFLW_COUNT, Bundle.getMessage("CanRxOverflowCount"));
        result.put(SETUP, Bundle.getMessage("SetupMode"));
        result.put(CAN_DISABLE, Bundle.getMessage("CanDisable"));
        result.put(CANID, Bundle.getMessage("CanId"));
        result.put(NN_HI, Bundle.getMessage("NodeNumberHi"));
        result.put(NN_LO, Bundle.getMessage("NodeNumberLo"));
        result.put(CAN_ERR_STATUS_1, Bundle.getMessage("CanErrStatus")+" 1");
        result.put(CAN_ERR_STATUS_2, Bundle.getMessage("CanErrStatus")+" 2");
        result.put(CAN_ERR_CONTROL, Bundle.getMessage("CanErrControl"));
        result.put(CAN_NOM_BIT_RATE_RX_COUNT, Bundle.getMessage("CanNomBitRateRxCount"));
        result.put(CAN_NOM_BIT_RATE_TX_COUNT, Bundle.getMessage("CanNomBitRateTxCount"));
        result.put(CAN_ERR_FREE_COUNT_HI, Bundle.getMessage("CanErrFreeCount")+" Hi");
        result.put(CAN_ERR_FREE_COUNT_LO, Bundle.getMessage("CanErrFreeCount")+" Lo");
        result.put(CAN_DIAGNOSTICS_HI, Bundle.getMessage("CanDiagnostics")+" Hi");
        result.put(CAN_DIAGNOSTICS_LO, Bundle.getMessage("CanDiagnostics")+" Lo");
        result.put(HOST_TX_CNT_T, Bundle.getMessage("HostTxCnt")+" Top");
        result.put(HOST_TX_CNT_U, Bundle.getMessage("HostTxCnt")+" Upper");
        result.put(HOST_TX_CNT_H, Bundle.getMessage("HostTxCnt")+" Hi");
        result.put(HOST_TX_CNT_L, Bundle.getMessage("HostTxCnt")+" Lo");
        result.put(HOST_RX_CNT_T, Bundle.getMessage("HostRxCnt")+" Top");
        result.put(HOST_RX_CNT_U, Bundle.getMessage("HostRxCnt")+" Upper");
        result.put(HOST_RX_CNT_H, Bundle.getMessage("HostRxCnt")+" Hi");
        result.put(HOST_RX_CNT_L, Bundle.getMessage("HostRxCnt")+" Lo");
        result.put(CAN_TX_CNT_T, Bundle.getMessage("CanTxCnt")+" Top");
        result.put(CAN_TX_CNT_U, Bundle.getMessage("CanTxCnt")+" Upper");
        result.put(CAN_TX_CNT_H, Bundle.getMessage("CanTxCnt")+" Hi");
        result.put(CAN_TX_CNT_L, Bundle.getMessage("CanTxCnt")+" Lo");
        result.put(CAN_RX_CNT_T, Bundle.getMessage("CanRxCnt")+" Top");
        result.put(CAN_RX_CNT_U, Bundle.getMessage("CanRxCnt")+" Upper");
        result.put(CAN_RX_CNT_H, Bundle.getMessage("CanRxCnt")+" Hi");
        result.put(CAN_RX_CNT_L, Bundle.getMessage("CanRxCnt")+" Lo");
        result.put(CAN_TX_ARB_DELAY, Bundle.getMessage("TxArbDelay"));

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
//        if (_nVarEditFrame == null ){
            _nVarEditFrame = new CanisbEditNVPane(dataModel, node);
//        }
        return _nVarEditFrame.getContent();
    }
    
}

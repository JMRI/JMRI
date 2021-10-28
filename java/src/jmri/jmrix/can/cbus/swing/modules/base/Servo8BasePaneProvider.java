package jmri.jmrix.can.cbus.swing.modules.base;

import jmri.jmrix.can.cbus.swing.modules.sprogdcc.*;

import java.util.*;

import javax.annotation.Nonnull;
import javax.swing.JPanel;

import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeNVTableDataModel;
import jmri.jmrix.can.cbus.swing.modules.AbstractEditNVPane;
import jmri.jmrix.can.cbus.swing.modules.CbusConfigPaneProvider;

import org.openide.util.lookup.ServiceProvider;

/**
 * Returns configuration objects for a basic 8 channel servo module
 *
 * @author Andrew Crosland Copyright (C) 2021
 */
@ServiceProvider(service = CbusConfigPaneProvider.class)
public class Servo8BasePaneProvider extends CbusConfigPaneProvider {
    
    String type = "SERVO8BASE";
    
    public static final int CUTOFF = 1;
    public static final int STARTUP_POS = 2;
    public static final int STARTUP_MOVE = 3;
    public static final int SEQUENCE = 4;
    public static final int OUT1_ON = 5;
    public static final int OUT1_OFF = 6;
    public static final int OUT1_ON_SPD = 7;
    public static final int OUT1_OFF_SPD = 8;
    public static final int OUT2_ON = 9;
    public static final int OUT2_OFF = 10;
    public static final int OUT2_ON_SPD = 11;
    public static final int OUT2_OFF_SPD = 12;
    public static final int OUT3_ON = 13;
    public static final int OUT3_OFF = 14;
    public static final int OUT3_ON_SPD = 15;
    public static final int OUT3_OFF_SPD = 16;
    public static final int OUT4_ON = 17;
    public static final int OUT4_OFF = 18;
    public static final int OUT4_ON_SPD = 19;
    public static final int OUT4_OFF_SPD = 20;
    public static final int OUT5_ON = 21;
    public static final int OUT5_OFF = 22;
    public static final int OUT5_ON_SPD = 23;
    public static final int OUT5_OFF_SPD = 24;
    public static final int OUT6_ON = 25;
    public static final int OUT6_OFF = 26;
    public static final int OUT6_ON_SPD = 27;
    public static final int OUT6_OFF_SPD = 28;
    public static final int OUT7_ON = 29;
    public static final int OUT7_OFF = 30;
    public static final int OUT7_ON_SPD = 31;
    public static final int OUT7_OFF_SPD = 32;
    public static final int OUT8_ON = 33;
    public static final int OUT8_OFF = 34;
    public static final int OUT8_ON_SPD = 35;
    public static final int OUT8_OFF_SPD = 36;
    public static final int LAST = 37;
    
    public Servo8BasePaneProvider() {
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
        result.put(CUTOFF, Bundle.getMessage("Cutoff"));
        result.put(STARTUP_POS, Bundle.getMessage("StartupPosition"));
        result.put(STARTUP_MOVE, Bundle.getMessage("StartupMove"));
        result.put(SEQUENCE, Bundle.getMessage("SequentialOp", 4));
        result.put(OUT1_ON, Bundle.getMessage("OutputXOnPos", 1));
        result.put(OUT1_OFF, Bundle.getMessage("OutputXOffPos", 1));
        result.put(OUT1_ON_SPD, Bundle.getMessage("OutputXOnSpd", 1));
        result.put(OUT1_OFF_SPD, Bundle.getMessage("OutputXOffSpd", 1));
        result.put(OUT2_ON, Bundle.getMessage("OutputXOnPos", 2));
        result.put(OUT2_OFF, Bundle.getMessage("OutputXOffPos", 2));
        result.put(OUT2_ON_SPD, Bundle.getMessage("OutputXOnSpd", 2));
        result.put(OUT2_OFF_SPD, Bundle.getMessage("OutputXOffSpd", 2));
        result.put(OUT3_ON, Bundle.getMessage("OutputXOnPos", 3));
        result.put(OUT3_OFF, Bundle.getMessage("OutputXOffPos", 3));
        result.put(OUT3_ON_SPD, Bundle.getMessage("OutputXOnSpd", 3));
        result.put(OUT3_OFF_SPD, Bundle.getMessage("OutputXOffSpd", 3));
        result.put(OUT4_ON, Bundle.getMessage("OutputXOnPos", 4));
        result.put(OUT4_OFF, Bundle.getMessage("OutputXOffPos", 4));
        result.put(OUT4_ON_SPD, Bundle.getMessage("OutputXOnSpd", 4));
        result.put(OUT4_OFF_SPD, Bundle.getMessage("OutputXOffSpd", 4));
        result.put(OUT5_ON, Bundle.getMessage("OutputXOnPos", 5));
        result.put(OUT5_OFF, Bundle.getMessage("OutputXOffPos", 5));
        result.put(OUT5_ON_SPD, Bundle.getMessage("OutputXOnSpd", 5));
        result.put(OUT5_OFF_SPD, Bundle.getMessage("OutputXOffSpd", 5));
        result.put(OUT6_ON, Bundle.getMessage("OutputXOnPos", 6));
        result.put(OUT6_OFF, Bundle.getMessage("OutputXOffPos", 6));
        result.put(OUT6_ON_SPD, Bundle.getMessage("OutputXOnSpd", 6));
        result.put(OUT6_OFF_SPD, Bundle.getMessage("OutputXOffSpd", 6));
        result.put(OUT7_ON, Bundle.getMessage("OutputXOnPos", 7));
        result.put(OUT7_OFF, Bundle.getMessage("OutputXOffPos", 7));
        result.put(OUT7_ON_SPD, Bundle.getMessage("OutputXOnSpd", 7));
        result.put(OUT7_OFF_SPD, Bundle.getMessage("OutputXOffSpd", 7));
        result.put(OUT8_ON, Bundle.getMessage("OutputXOnPos", 8));
        result.put(OUT8_OFF, Bundle.getMessage("OutputXOffPos", 8));
        result.put(OUT8_ON_SPD, Bundle.getMessage("OutputXOnSpd", 8));
        result.put(OUT8_OFF_SPD, Bundle.getMessage("OutputXOffSpd", 8));
        result.put(LAST, Bundle.getMessage("LastPos"));
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
            _nVarEditFrame = new Servo8BaseEditNVPane(dataModel, node);
        }
        return _nVarEditFrame.getContent();
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean nvWriteInLearn() {
        return true;
    }

}

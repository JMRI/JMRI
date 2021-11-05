package jmri.jmrix.can.cbus.swing.modules.base;

import java.util.*;

import javax.annotation.Nonnull;
import javax.swing.JPanel;

import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeNVTableDataModel;
import jmri.jmrix.can.cbus.swing.modules.*;

import org.openide.util.lookup.ServiceProvider;

/**
 * Returns configuration objects for a basic 8-channel solenoid module
 *
 * @author Andrew Crosland Copyright (C) 2021
 */
@ServiceProvider(service = CbusConfigPaneProvider.class)
public class Sol8BasePaneProvider extends CbusConfigPaneProvider {
    
    String type = "SOL8BASE";
    
    public static final int OUTPUT1 = 1;
    public static final int OUTPUT2 = 2;
    public static final int OUTPUT3 = 3;
    public static final int OUTPUT4 = 4;
    public static final int OUTPUT5 = 5;
    public static final int OUTPUT6 = 6;
    public static final int OUTPUT7 = 7;
    public static final int OUTPUT8 = 8;
    public static final int RECHARGE_TIME = 9;
    public static final int FIRE_DELAY = 10;
    public static final int ENABLE_DELAY = 11;

    public Sol8BasePaneProvider() {
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
        result.put(RECHARGE_TIME, Bundle.getMessage("RechargeTime"));
        result.put(FIRE_DELAY, Bundle.getMessage("FireDelay"));
        result.put(ENABLE_DELAY, Bundle.getMessage("EnableDelay"));
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
        _nVarEditFrame = new Sol8BaseEditNVPane(dataModel, node);
        return _nVarEditFrame.getContent();
    }
}

package jmri.jmrix.can.cbus.logixng;

import java.util.ArrayList;
import java.util.List;

import jmri.jmrit.logixng.LogixNG_Category;
import jmri.jmrix.can.CanSystemConnectionMemo;

/**
 * Defines the category Cbus
 *
 * @author Daniel Bergqvist Copyright 2025
 */
public final class CategoryMergCbus extends LogixNG_Category {

    /**
     * A item on the layout, for example turnout, sensor and signal mast.
     */
    public static final CategoryMergCbus CBUS = new CategoryMergCbus();


    public CategoryMergCbus() {
        super("CBUS", Bundle.getMessage("MenuCbus"), 300);
    }

    public static void registerCategory() {
        // We don't want to add these classes if we don't have a Cbus connection
        if (hasCbus() && !LogixNG_Category.values().contains(CBUS)) {
            LogixNG_Category.registerCategory(CBUS);
        }
    }

    public static List<CanSystemConnectionMemo> getMergConnections() {
        List<CanSystemConnectionMemo> list = jmri.InstanceManager.getList(CanSystemConnectionMemo.class);
        List<CanSystemConnectionMemo> mergConnections = new ArrayList<>();

        for (CanSystemConnectionMemo memo : list) {
            if (memo.provides(jmri.jmrix.can.cbus.CbusPreferences.class)) {
                // This is a MERG CBUS connection
                mergConnections.add(memo);
            }
        }

        return mergConnections;
    }

    /**
     * Do we have a Cbus connection?
     * @return true if we have Cbus, false otherwise
     */
    public static boolean hasCbus() {
        List<CanSystemConnectionMemo> list = getMergConnections();

        // We have at least one MERG CBUS connection if the list is not empty
        return !list.isEmpty();
    }

}

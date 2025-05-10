package jmri.jmrix.can.cbus.logixng;

import java.util.List;

import jmri.jmrit.logixng.Category;
import jmri.jmrix.can.CanSystemConnectionMemo;

/**
 * Defines the category LocoNet
 *
 * @author Daniel Bergqvist Copyright 2020
 */
public final class CategoryCbus extends Category {

    /**
     * A item on the layout, for example turnout, sensor and signal mast.
     */
    public static final CategoryCbus CBUS = new CategoryCbus();


    public CategoryCbus() {
        super("CBUS", Bundle.getMessage("MenuCbus"), 300);
    }

    public static void registerCategory() {
        // We don't want to add these classes if we don't have a Cbus connection
        if (hasCbus() && !Category.values().contains(CBUS)) {
            Category.registerCategory(CBUS);
        }
    }

    /**
     * Do we have a Cbus connection?
     * @return true if we have Cbus, false otherwise
     */
    public static boolean hasCbus() {
        List<CanSystemConnectionMemo> list = jmri.InstanceManager.getList(CanSystemConnectionMemo.class);

        // We have at least one Cbus connection if the list is not empty
        return !list.isEmpty();
    }

}

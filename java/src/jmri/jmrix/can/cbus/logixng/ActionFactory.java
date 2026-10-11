package jmri.jmrix.can.cbus.logixng;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jmri.Category;
import jmri.jmrit.logixng.DigitalActionFactory;
import jmri.jmrit.logixng.DigitalActionBean;

import org.openide.util.lookup.ServiceProvider;

/**
 * The factory for LogixNG Cbus classes.
 */
@ServiceProvider(service = DigitalActionFactory.class)
public class ActionFactory implements DigitalActionFactory {

    @Override
    public void init() {
        CategoryMergCbus.registerCategory();
    }

    @Override
    public Set<Map.Entry<Category, Class<? extends DigitalActionBean>>> getActionClasses() {
        Set<Map.Entry<Category, Class<? extends DigitalActionBean>>> actionClasses = new HashSet<>();

        // We don't want to add these classes if we don't have a CBUS connection
        if (CategoryMergCbus.hasCbus()) {
            actionClasses.add(new AbstractMap.SimpleEntry<>(CategoryMergCbus.CBUS, SendMergCbusEvent.class));
        }

        return actionClasses;
    }

}

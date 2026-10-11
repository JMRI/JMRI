package jmri.jmrit.operations.logixng;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jmri.Category;
import jmri.jmrit.logixng.DigitalActionFactory;
import jmri.jmrit.logixng.DigitalActionBean;

import org.openide.util.lookup.ServiceProvider;

/**
 * The factory for LogixNG Operation classes.
 */
@ServiceProvider(service = DigitalActionFactory.class)
public class ActionFactory implements DigitalActionFactory {

    @Override
    public void init() {
        CategoryOperations.registerCategory();
    }

    @Override
    public Set<Map.Entry<Category, Class<? extends DigitalActionBean>>> getActionClasses() {
        Set<Map.Entry<Category, Class<? extends DigitalActionBean>>> actionClasses = new HashSet<>();

        actionClasses.add(new AbstractMap.SimpleEntry<>(
                CategoryOperations.OPERATIONS, OperationsProStartAutomation.class));

        return actionClasses;
    }

}

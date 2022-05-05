package jmri.jmrix.mqtt.logixng;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.DigitalActionFactory;
import jmri.jmrit.logixng.DigitalActionBean;

import org.openide.util.lookup.ServiceProvider;

/**
 * The factory for LogixNG MQTT classes.
 */
@ServiceProvider(service = DigitalActionFactory.class)
public class ActionFactory implements DigitalActionFactory {

    @Override
    public void init() {
        CategoryMqtt.registerCategory();
    }

    @Override
    public Set<Map.Entry<Category, Class<? extends DigitalActionBean>>> getActionClasses() {
        Set<Map.Entry<Category, Class<? extends DigitalActionBean>>> actionClasses = new HashSet<>();

        // We don't want to add these classes if we don't have a MQTT connection
        if (CategoryMqtt.hasMQTT()) {
            actionClasses.add(new AbstractMap.SimpleEntry<>(CategoryMqtt.MQTT, Publish.class));
            actionClasses.add(new AbstractMap.SimpleEntry<>(CategoryMqtt.MQTT, Subscribe.class));
        }

        return actionClasses;
    }

}

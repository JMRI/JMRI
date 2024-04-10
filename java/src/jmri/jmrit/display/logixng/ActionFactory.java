package jmri.jmrit.display.logixng;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.DigitalActionFactory;
import jmri.jmrit.logixng.DigitalActionBean;

import org.openide.util.lookup.ServiceProvider;

/**
 * The factory for LogixNG Display classes.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
@ServiceProvider(service = DigitalActionFactory.class)
public class ActionFactory implements DigitalActionFactory {

    @Override
    public void init() {
        CategoryDisplay.registerCategory();
    }

    @Override
    public Set<Map.Entry<Category, Class<? extends DigitalActionBean>>> getActionClasses() {
        Set<Map.Entry<Category, Class<? extends DigitalActionBean>>> actionClasses =
                Set.of(
                        new AbstractMap.SimpleEntry<>(CategoryDisplay.DISPLAY, ActionAudioIcon.class),
                        new AbstractMap.SimpleEntry<>(CategoryDisplay.DISPLAY, ActionLayoutTurnout.class),
                        new AbstractMap.SimpleEntry<>(CategoryDisplay.DISPLAY, ActionPositionable.class),
                        new AbstractMap.SimpleEntry<>(CategoryDisplay.DISPLAY, ActionPositionableByClass.class),
                        new AbstractMap.SimpleEntry<>(CategoryDisplay.DISPLAY, WindowManagement.class)
                );

        return actionClasses;
    }

}

package jmri.jmrit.operations;

import java.util.Locale;

import org.openide.util.lookup.ServiceProvider;

import jmri.jmrit.operations.automation.AutomationsTableFrameAction;
import jmri.jmrit.operations.locations.LocationsTableAction;
import jmri.jmrit.operations.rollingstock.cars.CarsTableAction;
import jmri.jmrit.operations.rollingstock.engines.EnginesTableAction;
import jmri.jmrit.operations.routes.RoutesTableAction;
import jmri.jmrit.operations.setup.OperationsSettingsAction;
import jmri.jmrit.operations.trains.TrainsTableAction;
import jmri.jmrit.operations.trains.schedules.TrainsScheduleAction;
import jmri.util.startup.AbstractStartupActionFactory;
import jmri.util.startup.StartupActionFactory;

/**
 * Factory for Operations-related startup actions.
 * 
 * @author Randall Wood Copyright 2020
 */
@ServiceProvider(service = StartupActionFactory.class)
public final class OperationsStartupActionFactory extends AbstractStartupActionFactory {

    @Override
    public String getTitle(Class<?> clazz, Locale locale) throws IllegalArgumentException {
        if (clazz.equals(AutomationsTableFrameAction.class)) {
            return Bundle.getMessage(locale, "StartupAutomationsTableFrameAction"); // NOI18N
        } else if (clazz.equals(CarsTableAction.class)) {
            return Bundle.getMessage(locale, "StartupCarsTableAction"); // NOI18N
        } else if (clazz.equals(EnginesTableAction.class)) {
            return Bundle.getMessage(locale, "StartupEnginesTableAction"); // NOI18N
        } else if (clazz.equals(LocationsTableAction.class)) {
            return Bundle.getMessage(locale, "StartupLocationsTableAction"); // NOI18N
        } else if (clazz.equals(OperationsSettingsAction.class)) {
            return Bundle.getMessage(locale, "StartupOperationsSettingsAction"); // NOI18N
        } else if (clazz.equals(RoutesTableAction.class)) {
            return Bundle.getMessage(locale, "StartupRoutesTableAction"); // NOI18N
        } else if (clazz.equals(TrainsScheduleAction.class)) {
            return Bundle.getMessage(locale, "StartupTrainsScheduleAction"); // NOI18N
        } else if (clazz.equals(TrainsTableAction.class)) {
            return Bundle.getMessage(locale, "StartupTrainsTableAction"); // NOI18N
        }
        throw new IllegalArgumentException(clazz.getName() + " is not supported by " + this.getClass().getName());
    }

    @Override
    public Class<?>[] getActionClasses() {
        return new Class[]{AutomationsTableFrameAction.class,
            CarsTableAction.class,
            EnginesTableAction.class,
            LocationsTableAction.class,
            OperationsSettingsAction.class,
            RoutesTableAction.class,
            TrainsScheduleAction.class,
            TrainsTableAction.class};
    }

}

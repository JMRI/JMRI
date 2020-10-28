package jmri.jmrit.beantable;

import java.util.Locale;
import jmri.util.startup.AbstractStartupActionFactory;
import jmri.util.startup.StartupActionFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 * Factory for BeanTable startup actions.
 * 
 * @author Randall Wood Copyright 2020
 */
@ServiceProvider(service = StartupActionFactory.class)
public final class BeanTableStartupActionFactory extends AbstractStartupActionFactory {

    @Override
    public String getTitle(Class<?> clazz, Locale locale) throws IllegalArgumentException {
        if (clazz.equals(AudioTableAction.class)) {
            return Bundle.getMessage(locale, "StartupAudioTableAction"); // NOI18N
        } else if (clazz.equals(BlockTableAction.class)) {
            return Bundle.getMessage(locale, "StartupBlockTableAction"); // NOI18N
        } else if (clazz.equals(IdTagTableAction.class)) {
            return Bundle.getMessage(locale, "StartupIdTagTableAction"); // NOI18N
        } else if (clazz.equals(LightTableAction.class)) {
            return Bundle.getMessage(locale, "StartupLightTableAction"); // NOI18N
        } else if (clazz.equals(ListedTableAction.class)) {
            return Bundle.getMessage(locale, "StartupListedTableAction"); // NOI18N
        } else if (clazz.equals(LogixTableAction.class)) {
            return Bundle.getMessage(locale, "StartupLogixTableAction"); // NOI18N
        } else if (clazz.equals(LogixNGTableAction.class)) {
            return Bundle.getMessage(locale, "StartupLogixNGTableAction"); // NOI18N
        } else if (clazz.equals(LogixNGModuleTableAction.class)) {
            return Bundle.getMessage(locale, "StartupLogixNGModuleTableAction"); // NOI18N
        } else if (clazz.equals(LogixNGTableTableAction.class)) {
            return Bundle.getMessage(locale, "StartupLogixNGTableTableAction"); // NOI18N
        } else if (clazz.equals(LRouteTableAction.class)) {
            return Bundle.getMessage(locale, "StartupLRouteTableAction"); // NOI18N
        } else if (clazz.equals(MemoryTableAction.class)) {
            return Bundle.getMessage(locale, "StartupMemoryTableAction"); // NOI18N
        } else if (clazz.equals(OBlockTableAction.class)) {
            return Bundle.getMessage(locale, "StartupOBlockTableAction"); // NOI18N
        } else if (clazz.equals(ReporterTableAction.class)) {
            return Bundle.getMessage(locale, "StartupReporterTableAction"); // NOI18N
        } else if (clazz.equals(RouteTableAction.class)) {
            return Bundle.getMessage(locale, "StartupRouteTableAction"); // NOI18N
        } else if (clazz.equals(SectionTableAction.class)) {
            return Bundle.getMessage(locale, "StartupSectionTableAction"); // NOI18N
        } else if (clazz.equals(SensorTableAction.class)) {
            return Bundle.getMessage(locale, "StartupSensorTableAction"); // NOI18N
        } else if (clazz.equals(SignalGroupTableAction.class)) {
            return Bundle.getMessage(locale, "StartupSignalGroupTableAction"); // NOI18N
        } else if (clazz.equals(SignalHeadTableAction.class)) {
            return Bundle.getMessage(locale, "StartupSignalHeadTableAction"); // NOI18N
        } else if (clazz.equals(SignalMastTableAction.class)) {
            return Bundle.getMessage(locale, "StartupSignalMastTableAction"); // NOI18N
        } else if (clazz.equals(TransitTableAction.class)) {
            return Bundle.getMessage(locale, "StartupTransitTableAction"); // NOI18N
        } else if (clazz.equals(TurnoutTableAction.class)) {
            return Bundle.getMessage(locale, "StartupTurnoutTableAction"); // NOI18N
        }
        throw new IllegalArgumentException(clazz.getName() + " is not supported by " + this.getClass().getName());
    }

    @Override
    public Class<?>[] getActionClasses() {
        return new Class[]{AudioTableAction.class,
            BlockTableAction.class,
            IdTagTableAction.class,
            LightTableAction.class,
            ListedTableAction.class,
            LogixTableAction.class,
            LogixNGTableAction.class,
            LogixNGModuleTableAction.class,
            LogixNGTableTableAction.class,
            LRouteTableAction.class,
            MemoryTableAction.class,
            OBlockTableAction.class,
            ReporterTableAction.class,
            RouteTableAction.class,
            SectionTableAction.class,
            SensorTableAction.class,
            SignalGroupTableAction.class,
            SignalHeadTableAction.class,
            SignalMastTableAction.class,
            TransitTableAction.class,
            TurnoutTableAction.class};
    }
    
}

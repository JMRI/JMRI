package jmri.jmrit.operations;

import jmri.InstanceManager;
import jmri.ShutDownTask;
import jmri.implementation.QuietShutDownTask;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.ScheduleManager;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.operations.trains.TrainScheduleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author rhwood
 */
public final class OperationsManager {

    private ShutDownTask shutDownTask = null;

    static private OperationsManager instance = null;
    static private final Logger log = LoggerFactory.getLogger(OperationsManager.class);

    private OperationsManager() {
        // ensure the default instance of all operations managers
        // are initialized by calling their instance() methods
        // Is there a different, more optimal order for this?
        CarManager.instance();
        EngineManager.instance();
        TrainManager.instance();
        LocationManager.instance();
        RouteManager.instance();
        ScheduleManager.instance();
        TrainScheduleManager.instance();
        this.setShutDownTask(this.getDefaultShutDownTask());
    }

    public static OperationsManager getInstance() {
        if (instance == null) {
            instance = new OperationsManager();
        }
        return instance;
    }

    /**
     * Register the non-default {@link jmri.ShutDownTask}.
     *
     * Replaces the existing operations ShutDownTask with the new task. Use a
     * null value to prevent an operations ShutDownTask from being run when JMRI
     * shuts down. Use {@link #getDefaultShutDownTask() } to use the default
     * operations ShutDownTask.
     *
     * @param shutDownTask The new ShutDownTask or null
     */
    public void setShutDownTask(ShutDownTask shutDownTask) {
        if (InstanceManager.shutDownManagerInstance() != null) {
            if (this.shutDownTask != null) {
                InstanceManager.shutDownManagerInstance().deregister(this.shutDownTask);
            }
            this.shutDownTask = shutDownTask;
            if (this.shutDownTask != null) {
                InstanceManager.shutDownManagerInstance().register(this.shutDownTask);
            }
        }
    }

    public ShutDownTask getDefaultShutDownTask() {
        return new QuietShutDownTask("Save Operations State") { // NOI18N
            @Override
            public boolean execute() {
                try {
                    OperationsXml.save();
                } catch (Exception ex) {
                    log.warn("Error saving operations state: {}", ex);
                    log.debug("Details follow: ", ex);
                }
                return true;
            }
        };
    }
}

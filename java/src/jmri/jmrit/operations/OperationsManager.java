package jmri.jmrit.operations;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.*;
import jmri.implementation.QuietShutDownTask;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.schedules.ScheduleManager;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.setup.AutoBackup;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.operations.trains.schedules.TrainScheduleManager;

/**
 * A manager for Operations. This manager controls the Operations ShutDownTask.
 *
 * @author Randall Wood 2014
 */
public final class OperationsManager implements InstanceManagerAutoDefault, InstanceManagerAutoInitialize {

    private ShutDownTask shutDownTask = null;

    static private final Logger log = LoggerFactory.getLogger(OperationsManager.class);

    public OperationsManager() {
    }

    /**
     * Get the default instance of this class.
     *
     * @return the default instance of this class
     * @deprecated since 4.9.2; use
     * {@link jmri.InstanceManager#getDefault(java.lang.Class)} instead
     */
    @Deprecated
    public synchronized static OperationsManager getInstance() {
        return InstanceManager.getDefault(OperationsManager.class);
    }

    /**
     * Get the path to the Operations folder, rooted in the User's file path, as
     * a String.
     *
     * @return A path
     */
    public String getPath() {
        return OperationsXml.getFileLocation() + OperationsXml.getOperationsDirectoryName() + File.separator;
    }

    /**
     * Get the path to a file rooted in the Operations path.
     *
     * @param name The name of the file
     * @return A path
     * @see #getPath()
     */
    public String getPath(String name) {
        if (name != null) {
            return this.getPath() + name;
        }
        return this.getPath();
    }

    /**
     * Get a {@link java.io.File} rooted in the Operations path.
     *
     * @param name The name of the file
     * @return A file
     * @see #getPath()
     */
    public File getFile(String name) {
        return new File(this.getPath(name));
    }

    /**
     * Register the non-default {@link jmri.ShutDownTask}.
     * <p>
     * Replaces the existing operations ShutDownTask with the new task. Use a
     * null value to prevent an operations ShutDownTask from being run when JMRI
     * shuts down. Use {@link #getDefaultShutDownTask() } to use the default
     * operations ShutDownTask.
     *
     * @param shutDownTask The new ShutDownTask or null
     */
    public void setShutDownTask(ShutDownTask shutDownTask) {
        ShutDownManager manager = InstanceManager.getDefault(ShutDownManager.class);
        if (this.shutDownTask != null) {
            manager.deregister(this.shutDownTask);
        }
        this.shutDownTask = shutDownTask;
        if (this.shutDownTask != null) {
            manager.register(this.shutDownTask);
        }
    }

    /**
     * Get a copy of the default operations {@link jmri.ShutDownTask}. The
     * default ShutDownTask saves the operations state at shutdown without
     * prompting.
     *
     * @return A new ShutDownTask
     */
    public static ShutDownTask getDefaultShutDownTask() {
        return new QuietShutDownTask("Save Operations State") { // NOI18N
            @Override
            public boolean execute() {
                try {
                    OperationsXml.save();
                } catch (Exception ex) {
                    log.warn("Error saving operations state: {}", ex.getMessage());
                    log.debug("Details follow: ", ex);
                }
                return true;
            }
        };
    }

    @Override
    public void initialize() {
        // ensure the default instance of all operations managers
        // are initialized by calling their instance() methods
        // Is there a different, more optimal order for this?
        InstanceManager.getDefault(CarManager.class);
        InstanceManager.getDefault(EngineManager.class);
        InstanceManager.getDefault(TrainManager.class);
        InstanceManager.getDefault(LocationManager.class);
        InstanceManager.getDefault(RouteManager.class);
        InstanceManager.getDefault(ScheduleManager.class);
        InstanceManager.getDefault(TrainScheduleManager.class);
        this.setShutDownTask(OperationsManager.getDefaultShutDownTask());
        // auto backup?
        if (Setup.isAutoBackupEnabled()) {
            try {
                AutoBackup backup = new AutoBackup();
                backup.autoBackup();
            } catch (IOException ex) {
                log.debug("Auto backup after enabling Auto Backup flag.", ex);
            }
        }
    }
}

package jmri.jmrit.operations;

import java.io.File;
import jmri.InstanceManager;
import jmri.ShutDownTask;
import jmri.implementation.QuietShutDownTask;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.ScheduleManager;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.setup.AutoBackup;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.operations.trains.timetable.TrainScheduleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A manager for Operations. This manager controls the Operations ShutDownTask.
 *
 * @author Randall Wood 2014
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
        // auto backup?
        if (Setup.isAutoBackupEnabled()) {
            try {
                AutoBackup backup = new AutoBackup();
                backup.autoBackup();
            } catch (Exception ex) {
                log.debug("Auto backup after enabling Auto Backup flag.", ex);
            }
        }
    }

    /**
     * Get the OperationsManager.
     *
     * @return The OperationsManager default instance.
     */
    public synchronized static OperationsManager getInstance() {
        if (instance == null) {
            instance = new OperationsManager();
        }
        return instance;
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

    /**
     * Get a copy of the default operations {@link jmri.ShutDownTask}. The
     * default ShutDownTask saves the operations state at shutdown without
     * prompting.
     *
     * @return A new ShutDownTask
     */
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

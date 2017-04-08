package jmri.server.web.app;

import java.beans.PropertyChangeEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;
import jmri.profile.Profile;
import jmri.util.FileUtil;
import jmri.util.prefs.AbstractPreferencesManager;
import jmri.util.prefs.InitializationException;
import jmri.web.server.WebServer;
import jmri.web.server.WebServerPreferences;
import org.eclipse.jetty.util.component.LifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manager for the Angular JMRI Web Application.
 *
 * @author Randall Wood (C) 2016
 */
public class WebAppManager extends AbstractPreferencesManager {

    private WatchService watcher = null;
    private final static Logger log = LoggerFactory.getLogger(WebAppManager.class);
    private Map<WatchKey, Path> watchPaths = new HashMap<>();

    public WebAppManager() {
        try {
            this.watcher = FileSystems.getDefault().newWatchService();
        } catch (IOException ex) {
            log.warn("Unable to watch file system for changes.");
        }
    }

    @Override
    public void initialize(Profile profile) throws InitializationException {
        WebServerPreferences.getDefault().addPropertyChangeListener(WebServerPreferences.ALLOW_REMOTE_CONFIG, (PropertyChangeEvent evt) -> {
            this.savePreferences(profile);
        });
        WebServerPreferences.getDefault().addPropertyChangeListener(WebServerPreferences.RAILROAD_NAME, (PropertyChangeEvent evt) -> {
            this.savePreferences(profile);
        });
        WebServerPreferences.getDefault().addPropertyChangeListener(WebServerPreferences.READONLY_POWER, (PropertyChangeEvent evt) -> {
            this.savePreferences(profile);
        });
        WebServer.getDefault().addLifeCycleListener(new LifeCycle.Listener() {
            @Override
            public void lifeCycleStarting(LifeCycle lc) {
                // if no cached data, build it
                WebAppManager.this.savePreferences(profile);
            }

            @Override
            public void lifeCycleStarted(LifeCycle lc) {
                // register watcher to watch web/app directories everywhere
                if (WebAppManager.this.watcher != null) {
                    FileUtil.findFiles("web", ".").stream().filter((file) -> (file.isDirectory())).forEachOrdered((file) -> {
                        try {
                            Path path = file.toPath();
                            WebAppManager.this.watchPaths.put(path.register(watcher,
                                    StandardWatchEventKinds.ENTRY_CREATE,
                                    StandardWatchEventKinds.ENTRY_DELETE,
                                    StandardWatchEventKinds.ENTRY_MODIFY),
                                    path);
                        } catch (IOException ex) {
                            log.error("Unable to watch {} for changes.", file);
                        }
                    });
                }
            }

            @Override
            public void lifeCycleFailure(LifeCycle lc, Throwable thrwbl) {
            }

            @Override
            public void lifeCycleStopping(LifeCycle lc) {
            }

            @Override
            public void lifeCycleStopped(LifeCycle lc) {
                // stop watching web/app directories
            }
        });
        this.setInitialized(profile, true);
    }

    @Override
    public void savePreferences(Profile profile) {
        try {
            FileUtil.delete(FileUtil.getFile("settings:web/app"));
        } catch (FileNotFoundException ex) {
            // silently ignore - the directory to delete does not exist
        }
        FileUtil.createDirectory(FileUtil.getAbsoluteFilename("settings:web/app"));
        try {
            String template = FileUtil.readFile(FileUtil.getFile("program:web/app/index.html"));
        } catch (IOException ex) {
            log.error("Unable to get template for web app.", ex);
        }
    }
}

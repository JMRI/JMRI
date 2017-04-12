package jmri.server.web.app;

import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import jmri.profile.Profile;
import jmri.profile.ProfileUtils;
import jmri.server.web.spi.WebManifest;
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
    private boolean watching = false;
    private final HashMap<Profile, List<WebManifest>> manifests = new HashMap<>();

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
                File cache = ProfileUtils.getCacheDirectory(profile, this.getClass());
                savePreferences(profile);
            }

            @Override
            public void lifeCycleStarted(LifeCycle lc) {
                // register watcher to watch web/app directories everywhere
                if (watcher != null) {
                    watching = true;
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
                        (new Thread() {
                            @Override
                            public void run() {
                                while (watching) {
                                    WatchKey key;
                                    try {
                                        key = watcher.take();
                                    } catch (InterruptedException ex) {
                                        return;
                                    }

                                    key.pollEvents().stream().filter((event) -> (event.kind() != OVERFLOW)).forEachOrdered((event) -> {
                                        WebAppManager.this.savePreferences(profile);
                                    });
                                    watching = key.reset();
                                }
                            }
                        }).start();
                    });
                }
            }

            @Override
            public void lifeCycleFailure(LifeCycle lc, Throwable thrwbl) {
                watching = false;
            }

            @Override
            public void lifeCycleStopping(LifeCycle lc) {
                watching = false;
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
        File cache = ProfileUtils.getCacheDirectory(profile, this.getClass());
        FileUtil.delete(cache);
        this.manifests.get(profile).clear();
    }

    private List<WebManifest> getManifests(Profile profile) {
        if (!this.manifests.containsKey(profile)) {
            this.manifests.put(profile, new ArrayList<>());
        }
        if (this.manifests.get(profile).isEmpty()) {
            ServiceLoader.load(WebManifest.class).forEach((manifest) -> {
                this.manifests.get(profile).add(manifest);
            });
        }
        return this.manifests.get(profile);
    }

    public String getScriptTags(Profile profile) {
        StringBuilder tags = new StringBuilder();
        List<String> scripts = new ArrayList<>();
        this.getManifests(profile).forEach((manifest) -> {
            manifest.getScripts().stream().filter((script) -> (!scripts.contains(script))).forEachOrdered((script) -> {
                scripts.add(script);
            });
        });
        scripts.forEach((script) -> {
            tags.append("<script src=\"").append(script).append("\"></script>\n");
        });
        return tags.toString();
    }

    public String getStyleTags(Profile profile) {
        StringBuilder tags = new StringBuilder();
        List<String> styles = new ArrayList<>();
        this.getManifests(profile).forEach((manifest) -> {
            manifest.getStyles().stream().filter((style) -> (!styles.contains(style))).forEachOrdered((style) -> {
                styles.add(style);
            });
        });
        styles.forEach((style) -> {
            tags.append("<link rel=\"stylesheet\" href=\"").append(style).append("\" type=\"text/css\">\n");
        });
        return tags.toString();
    }
}

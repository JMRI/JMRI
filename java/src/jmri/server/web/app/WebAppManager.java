package jmri.server.web.app;

import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import java.util.Locale;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.StringJoiner;
import jmri.profile.Profile;
import jmri.profile.ProfileUtils;
import jmri.server.web.spi.WebManifest;
import jmri.server.web.spi.WebMenuItem;
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

    public String getNavigation(Profile profile, Locale locale) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode navigation = mapper.createArrayNode();
        List<WebMenuItem> items = new ArrayList<>();
        this.getManifests(profile).forEach((WebManifest manifest) -> {
            manifest.getNavigationMenuItems().stream().filter((WebMenuItem item)
                    -> !item.getPath().startsWith("help") // NOI18N
                    && !item.getPath().startsWith("user") // NOI18N
                    && !items.contains(item))
                    .forEachOrdered((item) -> {
                        items.add(item);
                    });
        });
        items.sort((WebMenuItem o1, WebMenuItem o2) -> o1.getPath().compareToIgnoreCase(o2.getPath()));
        // TODO: get order correct
        items.forEach((item) -> {
            ObjectNode navItem = mapper.createObjectNode();
            navItem.put("title", item.getTitle(locale));
            if (item.getIconClass() != null) {
                navItem.put("iconClass", item.getIconClass());
            }
            if (item.getHref() != null) {
                navItem.put("href", item.getHref());
            }
            // TODO: add children
            // TODO: add badges
            // TODO: handle separator before
            navigation.add(navItem);
            // TODO: handle separator after
        });
        return mapper.writeValueAsString(navigation);
    }

    public String getHelpMenuItems(Profile profile, Locale locale) {
        return this.getMenuItems("help", profile, locale); // NOI18N
    }

    public String getUserMenuItems(Profile profile, Locale locale) {
        return this.getMenuItems("user", profile, locale); // NOI18N
    }
    
    private String getMenuItems(String menu, Profile profile, Locale locale) {
        StringBuilder navigation = new StringBuilder();
        List<WebMenuItem> items = new ArrayList<>();
        this.getManifests(profile).forEach((WebManifest manifest) -> {
            manifest.getNavigationMenuItems().stream().filter((WebMenuItem item)
                    -> item.getPath().startsWith(menu)
                    && !items.contains(item))
                    .forEachOrdered((item) -> {
                        items.add(item);
                    });
        });
        items.sort((WebMenuItem o1, WebMenuItem o2) -> o1.getPath().compareToIgnoreCase(o2.getPath()));
        // TODO: get order correct
        items.forEach((item) -> {
            // TODO: add children
            // TODO: add badges
            // TODO: handle separator before
            // TODO: handle separator after
            String href = item.getHref();
            if (href != null && href.startsWith("ng-click:")) { // NOI18N
                navigation.append(String.format("<li><a ng-click=\"%s\">%s</a></li>", href.substring(href.indexOf(":") + 1, href.length()), item.getTitle(locale))); // NOI18N
            } else {
                navigation.append(String.format("<li><a href=\"%s\">%s</a></li>", href, item.getTitle(locale))); // NOI18N
            }
        });
        return navigation.toString();
    }

    public String getAngularDependencies(Profile profile, Locale locale) {
        StringJoiner dependencies = new StringJoiner("',\n  '", "\n  '", "'");
        List<String> items = new ArrayList<>();
        this.getManifests(profile).forEach((WebManifest manifest) -> {
            manifest.getAngularDependencies().stream().filter((dependency)
                    -> (!items.contains(dependency))).forEachOrdered((dependency) -> {
                items.add(dependency);
            });
        });
        items.forEach((String dependency) -> {
            dependencies.add(dependency);
        });
        return dependencies.toString();
    }

    public String getAngularRoutes(Profile profile, Locale locale) {
        StringJoiner routes = new StringJoiner("\n", "\n", "");
        Map<String, String> items = new HashMap<>();
        this.getManifests(profile).forEach((WebManifest manifest) -> {
            items.putAll(manifest.getAngularRoutes());
        });
        items.forEach((String when, String template) -> {
            routes.add(String.format("      .when('%s', { redirectTo: '%s' })", when, template));
        });
        return routes.toString();
    }
}

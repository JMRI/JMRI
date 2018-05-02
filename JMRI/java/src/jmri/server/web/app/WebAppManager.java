package jmri.server.web.app;

import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.StringJoiner;
import jmri.InstanceManager;
import jmri.profile.Profile;
import jmri.profile.ProfileUtils;
import jmri.server.web.spi.AngularRoute;
import jmri.server.web.spi.WebManifest;
import jmri.server.web.spi.WebMenuItem;
import jmri.spi.PreferencesManager;
import jmri.util.FileUtil;
import jmri.util.prefs.AbstractPreferencesManager;
import jmri.util.prefs.InitializationException;
import jmri.web.server.WebServer;
import jmri.web.server.WebServerPreferences;
import org.eclipse.jetty.util.component.LifeCycle;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manager for the Angular JMRI Web Application.
 *
 * @author Randall Wood (C) 2016
 */
@ServiceProvider(service = PreferencesManager.class)
public class WebAppManager extends AbstractPreferencesManager {

    private final HashMap<Profile, WatchService> watcher = new HashMap<>();
    private final Map<WatchKey, Path> watchPaths = new HashMap<>();
    private final HashMap<Profile, List<WebManifest>> manifests = new HashMap<>();
    private Thread lifeCycleListener = null;
    private final static Logger log = LoggerFactory.getLogger(WebAppManager.class);

    public WebAppManager() {
    }

    @Override
    public void initialize(Profile profile) throws InitializationException {
        WebServerPreferences preferences = InstanceManager.getDefault(WebServerPreferences.class);
        preferences.addPropertyChangeListener(WebServerPreferences.ALLOW_REMOTE_CONFIG, (PropertyChangeEvent evt) -> {
            this.savePreferences(profile);
        });
        preferences.addPropertyChangeListener(WebServerPreferences.RAILROAD_NAME, (PropertyChangeEvent evt) -> {
            this.savePreferences(profile);
        });
        preferences.addPropertyChangeListener(WebServerPreferences.READONLY_POWER, (PropertyChangeEvent evt) -> {
            this.savePreferences(profile);
        });
        WebServer.getDefault().addLifeCycleListener(new LifeCycle.Listener() {
            @Override
            public void lifeCycleStarting(LifeCycle lc) {
                WebAppManager.this.lifeCycleStarting(lc, profile);
            }

            @Override
            public void lifeCycleStarted(LifeCycle lc) {
                WebAppManager.this.lifeCycleStarted(lc, profile);
            }

            @Override
            public void lifeCycleFailure(LifeCycle lc, Throwable thrwbl) {
                WebAppManager.this.lifeCycleFailure(lc, thrwbl, profile);
            }

            @Override
            public void lifeCycleStopping(LifeCycle lc) {
                WebAppManager.this.lifeCycleStopping(lc, profile);
            }

            @Override
            public void lifeCycleStopped(LifeCycle lc) {
                WebAppManager.this.lifeCycleStopped(lc, profile);
            }
        });
        if (WebServer.getDefault().isRunning()) {
            this.lifeCycleStarting(null, profile);
            this.lifeCycleStarted(null, profile);
        }
        this.setInitialized(profile, true);
    }

    @Override
    public void savePreferences(Profile profile) {
        File cache = ProfileUtils.getCacheDirectory(profile, this.getClass());
        FileUtil.delete(cache);
        this.manifests.getOrDefault(profile, new ArrayList<>()).clear();
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
        for (int i = 0; i < items.size(); i++) {
            WebMenuItem item = items.get(i);
            ObjectNode navItem = this.getMenuItem(item, mapper, locale);
            ArrayNode children = mapper.createArrayNode();
            for (int j = i + 1; j < items.size(); j++) {
                if (!items.get(j).getPath().startsWith(item.getPath())) {
                    break;
                }
                // TODO: add children to arbitrary depth
                ObjectNode child = this.getMenuItem(items.get(j), mapper, locale);
                if (items.get(j).getHref() != null) {
                    children.add(child);
                }
                i++;
            }
            navItem.set("children", children);
            // TODO: add badges
            if (item.getHref() != null || children.size() != 0) {
                // TODO: handle separator before
                navigation.add(navItem);
                // TODO: handle separator after
            }
        }
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
            String title = item.getTitle(locale);
            if (title.startsWith("translate:")) {
                title = String.format("<span data-translate>%s</span>", title.substring(10));
            }
            if (href != null && href.startsWith("ng-click:")) { // NOI18N
                navigation.append(String.format("<li><a ng-click=\"%s\">%s</a></li>", href.substring(href.indexOf(":") + 1, href.length()), title)); // NOI18N
            } else {
                navigation.append(String.format("<li><a href=\"%s\">%s</a></li>", href, title)); // NOI18N
            }
        });
        return navigation.toString();
    }

    private ObjectNode getMenuItem(WebMenuItem item, ObjectMapper mapper, Locale locale) {
        ObjectNode navItem = mapper.createObjectNode();
        navItem.put("title", item.getTitle(locale));
        if (item.getIconClass() != null) {
            navItem.put("iconClass", item.getIconClass());
        }
        if (item.getHref() != null) {
            navItem.put("href", item.getHref());
        }
        return navItem;
    }

    public String getAngularDependencies(Profile profile, Locale locale) {
        StringJoiner dependencies = new StringJoiner("',\n  '", "\n  '", "'"); // NOI18N
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
        StringJoiner routes = new StringJoiner("\n", "\n", ""); // NOI18N
        Set<AngularRoute> items = new HashSet<>();
        this.getManifests(profile).forEach((WebManifest manifest) -> {
            items.addAll(manifest.getAngularRoutes());
        });
        items.forEach((route) -> {
            if (route.getRedirection() != null) {
                routes.add(String.format("      .when('%s', { redirectTo: '%s' })", route.getWhen(), route.getRedirection())); // NOI18N
            } else if (route.getTemplate() != null && route.getController() != null) {
                routes.add(String.format("      .when('%s', { templateUrl: '%s', controller: '%s' })", route.getWhen(), route.getTemplate(), route.getController())); // NOI18N
            }
        });
        return routes.toString();
    }

    public String getAngularSources(Profile profile, Locale locale) {
        StringJoiner sources = new StringJoiner("\n", "\n\n", "\n"); // NOI18N
        List<URL> urls = new ArrayList<>();
        this.getManifests(profile).forEach((WebManifest manifest) -> {
            urls.addAll(manifest.getAngularSources());
        });
        urls.forEach((URL source) -> {
            try {
                sources.add(FileUtil.readURL(source));
            } catch (IOException ex) {
                log.error("Unable to read {}", source, ex);
            }
        });
        return sources.toString();
    }

    public Set<URI> getPreloadedTranslations(Profile profile, Locale locale) {
        Set<URI> urls = new HashSet<>();
        this.getManifests(profile).forEach((WebManifest manifest) -> {
            urls.addAll(manifest.getPreloadedTranslations(locale));
        });
        return urls;
    }

    private void lifeCycleStarting(LifeCycle lc, Profile profile) {
        if (this.watcher.get(profile) == null) {
            try {
                this.watcher.put(profile, FileSystems.getDefault().newWatchService());
            } catch (IOException ex) {
                log.warn("Unable to watch file system for changes.");
            }
        }
    }

    private void lifeCycleStarted(LifeCycle lc, Profile profile) {
        // register watcher to watch web/app directories everywhere
        if (this.watcher.get(profile) != null) {
            FileUtil.findFiles("web", ".").stream().filter((file) -> (file.isDirectory())).forEachOrdered((file) -> {
                try {
                    Path path = file.toPath();
                    WebAppManager.this.watchPaths.put(path.register(this.watcher.get(profile),
                            StandardWatchEventKinds.ENTRY_CREATE,
                            StandardWatchEventKinds.ENTRY_DELETE,
                            StandardWatchEventKinds.ENTRY_MODIFY),
                            path);
                } catch (IOException ex) {
                    log.error("Unable to watch {} for changes.", file);
                }
                this.lifeCycleListener = new Thread(() -> {
                    while (WebAppManager.this.watcher.get(profile) != null) {
                        WatchKey key;
                        try {
                            key = WebAppManager.this.watcher.get(profile).take();
                        } catch (InterruptedException ex) {
                            return;
                        }

                        key.pollEvents().stream().filter((event) -> (event.kind() != OVERFLOW)).forEachOrdered((event) -> {
                            WebAppManager.this.savePreferences(profile);
                        });
                        if (!key.reset()) {
                            WebAppManager.this.watcher.remove(profile);
                        }
                    }
                }, "WebAppManager");
                this.lifeCycleListener.start();
            });
        }
    }

    private void lifeCycleFailure(LifeCycle lc, Throwable thrwbl, Profile profile) {
        log.debug("Web server life cycle failure", thrwbl);
        this.lifeCycleStopped(lc, profile);
    }

    private void lifeCycleStopping(LifeCycle lc, Profile profile) {
        this.lifeCycleStopped(lc, profile);
    }

    private void lifeCycleStopped(LifeCycle lc, Profile profile) {
        if (this.lifeCycleListener != null) {
            this.lifeCycleListener.interrupt();
            this.lifeCycleListener = null;
        }
        // stop watching web/app directories
        this.watcher.remove(profile);
    }
}

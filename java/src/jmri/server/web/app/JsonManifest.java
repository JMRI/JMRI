package jmri.server.web.app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import jmri.server.web.spi.AngularRoute;
import jmri.server.web.spi.WebManifest;
import jmri.server.web.spi.WebMenuItem;
import jmri.util.FileUtil;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Web Manifest built from manifest.json files.
 *
 * @author Randall Wood (C) 2016
 */
@ServiceProvider(service = WebManifest.class)
public class JsonManifest implements WebManifest {

    private boolean initialized = false;
    private final Set<WebMenuItem> menuItems = new HashSet<>();
    private final List<String> scripts = new ArrayList<>();
    private final List<String> styles = new ArrayList<>();
    private final List<String> dependencies = new ArrayList<>();
    private final Set<AngularRoute> routes = new HashSet<>();
    private final List<URL> sources = new ArrayList<>();
    private final Set<String> translations = new HashSet<>();
    private final static Logger log = LoggerFactory.getLogger(JsonManifest.class);

    @Override
    public Set<WebMenuItem> getNavigationMenuItems() {
        this.initialize();
        return this.menuItems;
    }

    @Override
    public List<String> getScripts() {
        this.initialize();
        return this.scripts;
    }

    @Override
    public List<String> getStyles() {
        this.initialize();
        return this.styles;
    }

    @Override
    public List<String> getAngularDependencies() {
        this.initialize();
        return this.dependencies;
    }

    @Override
    public Set<AngularRoute> getAngularRoutes() {
        this.initialize();
        return this.routes;
    }

    @Override
    public List<URL> getAngularSources() {
        this.initialize();
        return this.sources;
    }

    @Override
    public Set<URI> getPreloadedTranslations(Locale locale) {
        this.initialize();
        Set<URI> found = new HashSet<>();
        this.translations.forEach((translation) -> {
            URI url = FileUtil.findURI(translation.replaceFirst("\\*", locale.toString()));
            if (url == null) {
                url = FileUtil.findURI(translation.replaceFirst("\\*", locale.getLanguage()));
            }
            if (url == null) {
                url = FileUtil.findURI(translation.replaceFirst("\\*", "en"));
            }
            if (url != null) {
                found.add(url);
            } else {
                log.error("Unable to find localization file {} for any language", translation);
            }
        });
        return found;
    }

    synchronized private void initialize() {
        if (!this.initialized) {
            Set<File> manifests = FileUtil.findFiles("manifest.json", "web"); // NOI18N
            ObjectMapper mapper = new ObjectMapper();
            manifests.forEach((manifest) -> {
                try {
                    JsonNode root = mapper.readTree(manifest);
                    root.path("scripts").forEach((script) -> {
                        if (!this.scripts.contains(script.asText())) {
                            this.scripts.add(script.asText());
                        }
                    });
                    root.path("styles").forEach((style) -> {
                        if (!this.styles.contains(style.asText())) {
                            this.styles.add(style.asText());
                        }
                    });
                    root.path("dependencies").forEach((dependency) -> {
                        if (!this.dependencies.contains(dependency.asText())) {
                            this.dependencies.add(dependency.asText());
                        }
                    });
                    root.path("navigation").forEach((navigation) -> {
                        JsonMenuItem menuItem = new JsonMenuItem(navigation);
                        if (!this.menuItems.contains(menuItem)) {
                            this.menuItems.add(menuItem);
                        }
                    });
                    root.path("routes").forEach((route) -> {
                        String when = route.path("when").asText(); // NOI18N
                        String template = route.path("template").asText(); // NOI18N
                        String controller = route.path("controller").asText(); // NOI18N
                        String redirection = route.path("redirection").asText(); // NOI18N
                        if (template.isEmpty() || controller.isEmpty()) {
                            template = null;
                            controller = null;
                        }
                        if (redirection.isEmpty()) {
                            redirection = null;
                        }
                        if (when != null && !when.isEmpty()) {
                            try {
                                this.routes.add(new AngularRoute(when, template, controller, redirection));
                            } catch (NullPointerException | IllegalArgumentException ex) {
                                log.error("Unable to add route for {}", when);
                            }
                        }
                    });
                    root.path("sources").forEach((source) -> {
                        URL url = FileUtil.findURL(source.asText());
                        if (url != null) {
                            this.sources.add(url);
                        }
                    });
                    root.path("translations").forEach((translation) -> {
                        String url = translation.asText();
                        if (url != null) {
                            this.translations.add(url);
                        }
                    });
                } catch (IOException ex) {
                    log.error("Unable to read {}", manifest, ex);
                }
            });
            this.initialized = true;
        }
    }

}

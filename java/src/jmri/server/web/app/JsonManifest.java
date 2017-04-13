package jmri.server.web.app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jmri.server.web.spi.WebManifest;
import jmri.server.web.spi.WebMenuItem;
import jmri.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Web Manifest built from manifest.json files.
 *
 * @author Randall Wood (C) 2016
 */
public class JsonManifest implements WebManifest {
    
    private boolean initialized = false;
    private final Set<WebMenuItem> menuItems = new HashSet<>();
    private final List<String> scripts = new ArrayList<>();
    private final List<String> styles = new ArrayList<>();
    private final List<String> dependencies = new ArrayList<>();
    private final Map<String, String> routes = new HashMap<>();
    private final static Logger log = LoggerFactory.getLogger(JsonManifest.class);
    
    @Override
    public Set<WebMenuItem> getNavigationMenuItems() {
        if (!initialized) {
            this.initialize();
        }
        return this.menuItems;
    }
    
    @Override
    public List<String> getScripts() {
        if (!initialized) {
            this.initialize();
        }
        return this.scripts;
    }
    
    @Override
    public List<String> getStyles() {
        if (!initialized) {
            this.initialize();
        }
        return this.styles;
    }
    
    @Override
    public List<String> getAngularDependencies() {
        if (!initialized) {
            this.initialize();
        }
        return this.dependencies;
    }
    
    @Override
    public Map<String, String> getAngularRoutes() {
        if (!initialized) {
            this.initialize();
        }
        return this.routes;
    }
    
    synchronized private void initialize() {
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
                    if (when != null && template != null) {
                        this.routes.put(when, template);
                    }
                });
            } catch (IOException ex) {
                log.error("Unable to read {}", manifest, ex);
            }
        });
        this.initialized = true;
    }
    
}

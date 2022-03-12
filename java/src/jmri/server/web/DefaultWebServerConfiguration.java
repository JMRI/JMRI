package jmri.server.web;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

import javax.annotation.Nonnull;

import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.server.web.spi.WebServerConfiguration;

/**
 * Default implementation of {@link jmri.server.web.spi.WebServerConfiguration}.
 * This implementation reads two properties files to build the JMRI default
 * configuration for the Web Server. Default forbidden paths are listed in the
 * WebServlet annotation for {@link jmri.web.servlet.DenialServlet}.
 *
 * @author Randall Wood (C) 2016
 */
@ServiceProvider(service = WebServerConfiguration.class)
public final class DefaultWebServerConfiguration extends AbstractWebServerConfiguration {

    private final HashMap<String, String> redirections = new HashMap<>();
    private final HashMap<String, String> files = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(DefaultWebServerConfiguration.class);

    public DefaultWebServerConfiguration() {
        loadMap(files, "FilePaths.properties"); // NOI18N
        loadMap(redirections, "Redirections.properties"); // NOI18N
    }

    @Override
    @Nonnull
    public HashMap<String, String> getFilePaths() {
        return files;
    }

    @Override
    @Nonnull
    public HashMap<String, String> getRedirectedPaths() {
        return redirections;
    }

    private void loadMap(@Nonnull HashMap<String, String> map, @Nonnull String resource) {
        try (InputStream in = getClass().getResourceAsStream(resource)) {
            Properties properties = new Properties();
            properties.load(in);
            properties.stringPropertyNames().forEach(path -> map.put(path, properties.getProperty(path)));
        } catch (IllegalArgumentException | NullPointerException | IOException ex) {
            log.error("Unable to load {}", resource, ex);
        }
    }
}

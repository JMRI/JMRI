package jmri.server.web;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;
import jmri.server.web.spi.WebServerConfiguration;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final static Logger log = LoggerFactory.getLogger(DefaultWebServerConfiguration.class);

    public DefaultWebServerConfiguration() {
        try (InputStream in = this.getClass().getResourceAsStream("FilePaths.properties")) { // NOI18N
            Properties properties = new Properties();
            properties.load(in);
            in.close();
            properties.stringPropertyNames().forEach((path) -> {
                this.files.put(path, properties.getProperty(path));
            });
        } catch (IOException ex) {
            log.error(ex.getMessage());
        }
        try (InputStream in = this.getClass().getResourceAsStream("Redirections.properties")) { // NOI18N
            Properties properties = new Properties();
            properties.load(in);
            in.close();
            properties.stringPropertyNames().forEach((path) -> {
                this.redirections.put(path, properties.getProperty(path));
            });
        } catch (IOException ex) {
            log.error(ex.getMessage());
        }
    }

    @Override
    public HashMap<String, String> getFilePaths() {
        return files;
    }

    @Override
    public HashMap<String, String> getRedirectedPaths() {
        return redirections;
    }
}

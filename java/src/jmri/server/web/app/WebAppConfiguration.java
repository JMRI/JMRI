package jmri.server.web.app;

import java.util.HashMap;
import java.util.Map;
import jmri.server.web.AbstractWebServerConfiguration;

/**
 * Provide default paths to the Angular JMRI web application.
 *
 * @author Randall Wood (C) 2016
 */
public class WebAppConfiguration extends AbstractWebServerConfiguration {

    @Override
    public Map<String, String> getFilePaths() {
        HashMap<String, String> map = new HashMap<>();
        map.put("/app", "program:web/app"); // NOI18N
        return map;
    }
}

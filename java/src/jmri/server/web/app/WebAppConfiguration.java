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
        map.put("/app/node_modules", "program:web/app/node_modules"); // NOI18N
        map.put("/app/scripts", "program:web/app/scripts"); // NOI18N
        return map;
    }
}

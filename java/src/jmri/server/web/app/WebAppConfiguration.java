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

    /**
     * Get paths for static content that would otherwise be handled by the
     * {@link jmri.server.web.app.WebAppServlet }.
     *
     * {@inheritDoc }
     */
    @Override
    public Map<String, String> getFilePaths() {
        HashMap<String, String> map = new HashMap<>();
        map.put("/app/node_modules", "program:web/app/node_modules"); // NOI18N
        map.put("/app/app", "program:web/app/app"); // NOI18N
        map.put("/app/short-detector", "program:web/app/short-detector"); // NOI18N
        return map;
    }
}

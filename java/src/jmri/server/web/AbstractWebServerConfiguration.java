package jmri.server.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jmri.server.web.spi.WebServerConfiguration;

/**
 * Abstract implementation of {@link jmri.server.web.spi.WebServerConfiguration}
 *
 * @author Randall Wood (C) 2016
 */
public abstract class AbstractWebServerConfiguration implements WebServerConfiguration {

    @Override
    public Map<String, String> getFilePaths() {
        return new HashMap<>();
    }

    @Override
    public Map<String, String> getRedirectedPaths() {
        return new HashMap<>();
    }

    @Override
    public List<String> getForbiddenPaths() {
        return new ArrayList<>();
    }

}

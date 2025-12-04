package jmri.tracktiles.configurexml;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.configurexml.AbstractXmlAdapter;
import jmri.tracktiles.DefaultTrackTileManager;

/**
 * Provides load and store functionality for configuring DefaultTrackTileManager.
 * <p>
 * The DefaultTrackTileManager loads static catalog data from XML files in the
 * tracktiles directory and doesn't have user configuration to persist.
 * This adapter provides minimal XML support to satisfy the configuration framework.
 *
 * @author Ralf Lang
 */
public class DefaultTrackTileManagerXml extends AbstractXmlAdapter {

    public DefaultTrackTileManagerXml() {
        super();
    }

    /**
     * Store the DefaultTrackTileManager.
     * Since this manager only loads static catalog data and has no user configuration,
     * this returns null to indicate nothing needs to be stored.
     *
     * @param o Object to store, should be DefaultTrackTileManager
     * @return null - no configuration to store
     */
    @Override
    public Element store(Object o) {
        // DefaultTrackTileManager only loads static catalog data,
        // no user configuration to persist
        return null;
    }

    /**
     * Load DefaultTrackTileManager configuration.
     * Since this manager only loads static catalog data and has no user configuration,
     * this method does nothing.
     *
     * @param shared  Element to read from
     * @param perNode per-node Element to read from
     * @return true always, as there's nothing to fail
     */
    @Override
    public boolean load(Element shared, Element perNode) {
        // DefaultTrackTileManager auto-loads its catalog data,
        // no user configuration to restore
        log.debug("DefaultTrackTileManager has no user configuration to load");
        return true;
    }

    /**
     * Load DefaultTrackTileManager configuration.
     * Since this manager only loads static catalog data and has no user configuration,
     * this method does nothing.
     *
     * @param element Element to read from
     * @param o       Object to configure, should be DefaultTrackTileManager
     */
    @Override
    public void load(Element element, Object o) {
        log.debug("DefaultTrackTileManager has no user configuration to load");
    }

    private final static Logger log = LoggerFactory.getLogger(DefaultTrackTileManagerXml.class);
}

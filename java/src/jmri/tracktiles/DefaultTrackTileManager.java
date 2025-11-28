package jmri.tracktiles;

import java.io.File;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import jmri.Manager;
import jmri.jmrit.XmlFile;
import jmri.managers.AbstractManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of TrackTileManager.
 * Manages read-only TrackTile catalog objects loaded from XML files.
 * 
 * @author Ralf Lang Copyright (C) 2025
 */
public class DefaultTrackTileManager extends AbstractManager<TrackTile> implements TrackTileManager {

    private static final Logger log = LoggerFactory.getLogger(DefaultTrackTileManager.class);

    /**
     * Create a new DefaultTrackTileManager for the internal system.
     * Loads all catalog data from xml/tracktiles directory.
     */
    public DefaultTrackTileManager() {
        super();
        loadCatalogs();
    }

    /**
     * Load all track tile catalogs from xml/tracktiles directory.
     */
    private void loadCatalogs() {
        File catalogDir = new File(XmlFile.xmlDir() + "tracktiles");
        
        if (!catalogDir.exists() || !catalogDir.isDirectory()) {
            log.warn("Track tiles directory not found: {}", catalogDir.getAbsolutePath());
            return;
        }

        File[] xmlFiles = catalogDir.listFiles((dir, name) -> 
            name.toLowerCase().endsWith(".xml") && !name.startsWith("."));

        if (xmlFiles == null || xmlFiles.length == 0) {
            log.warn("No XML files found in: {}", catalogDir.getAbsolutePath());
            return;
        }

        for (File xmlFile : xmlFiles) {
            try {
                loadCatalog(xmlFile);
            } catch (Exception e) {
                log.error("Error loading catalog file {}: {}", xmlFile.getName(), e.getMessage());
            }
        }

        log.info("Loaded {} track tiles from {} catalogs", getTrackTileCount(), xmlFiles.length);
    }

    /**
     * Load a single track tile catalog file.
     * 
     * @param xmlFile The XML file to load
     * @throws Exception if loading fails
     */
    private void loadCatalog(File xmlFile) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(xmlFile);
        
        doc.getDocumentElement().normalize();

        // Get header information
        NodeList headerList = doc.getElementsByTagName("header");
        if (headerList.getLength() == 0) {
            log.warn("No header found in {}", xmlFile.getName());
            return;
        }

        Element header = (Element) headerList.item(0);
        String vendor = getElementText(header, "vendor", "Unknown");
        String family = getElementText(header, "family", "Unknown");

        // Process tiles
        NodeList tileList = doc.getElementsByTagName("tile");
        for (int i = 0; i < tileList.getLength(); i++) {
            Element tileElement = (Element) tileList.item(i);
            
            String partCode = getElementText(tileElement, "partcode", "");
            String jmriType = getElementText(tileElement, "jmritype", "");

            if (partCode.isEmpty() || jmriType.isEmpty()) {
                log.warn("Skipping incomplete tile in {}", xmlFile.getName());
                continue;
            }

            // Create system name: TT:vendor:family:partcode
            String systemName = "TT:" + vendor + ":" + family + ":" + partCode;
            TrackTile tile = new TrackTile(systemName, vendor, family, jmriType, partCode);

            // Parse geometry if present
            NodeList geometryList = tileElement.getElementsByTagName("geometry");
            if (geometryList.getLength() > 0) {
                Element geometryElement = (Element) geometryList.item(0);
                
                // Check for straight geometry
                NodeList straightList = geometryElement.getElementsByTagName("straight");
                if (straightList.getLength() > 0) {
                    Element straightElement = (Element) straightList.item(0);
                    String lengthStr = straightElement.getAttribute("length");
                    if (!lengthStr.isEmpty()) {
                        try {
                            double length = Double.parseDouble(lengthStr);
                            tile.setLength(length);
                        } catch (NumberFormatException e) {
                            log.warn("Invalid length value '{}' in {}", lengthStr, xmlFile.getName());
                        }
                    }
                }
                
                // Check for curved geometry
                NodeList curvedList = geometryElement.getElementsByTagName("curved");
                if (curvedList.getLength() > 0) {
                    Element curvedElement = (Element) curvedList.item(0);
                    String radiusStr = curvedElement.getAttribute("radius");
                    String arcStr = curvedElement.getAttribute("arc");
                    
                    if (!radiusStr.isEmpty()) {
                        try {
                            double radius = Double.parseDouble(radiusStr);
                            tile.setRadius(radius);
                        } catch (NumberFormatException e) {
                            log.warn("Invalid radius value '{}' in {}", radiusStr, xmlFile.getName());
                        }
                    }
                    
                    if (!arcStr.isEmpty()) {
                        try {
                            double arc = Double.parseDouble(arcStr);
                            tile.setArc(arc);
                        } catch (NumberFormatException e) {
                            log.warn("Invalid arc value '{}' in {}", arcStr, xmlFile.getName());
                        }
                    }
                }
            }

            // Add localizations
            NodeList l10nList = tileElement.getElementsByTagName("l10n");
            for (int j = 0; j < l10nList.getLength(); j++) {
                Element l10nElement = (Element) l10nList.item(j);
                String lang = l10nElement.getAttribute("lang");
                String caption = l10nElement.getTextContent().trim();
                if (!lang.isEmpty() && !caption.isEmpty()) {
                    tile.addLocalization(lang, caption);
                }
            }

            register(tile);  // Register with AbstractManager
        }

        log.debug("Loaded {} tiles from {}", tileList.getLength(), xmlFile.getName());
    }

    /**
     * Helper to extract text from an XML element.
     */
    private String getElementText(Element parent, String tagName, String defaultValue) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent().trim();
        }
        return defaultValue;
    }

    // Implementation of TrackTileManager interface

    /** {@inheritDoc} */
    @Override
    @CheckForNull
    public TrackTile getBySystemName(@Nonnull String systemName) {
        return _tsys.get(systemName);
    }

    /** {@inheritDoc} */
    @Override
    @CheckForNull
    public TrackTile getByUserName(@Nonnull String userName) {
        return _tuser.get(userName);
    }

    /** {@inheritDoc} */
    @Override
    @CheckForNull
    public TrackTile getTrackTile(@Nonnull String name) {
        TrackTile t = getByUserName(name);
        if (t != null) {
            return t;
        }
        return getBySystemName(name);
    }

    /** {@inheritDoc} */
    @Override
    public int getTrackTileCount() {
        return _tsys.size();
    }

    // Required AbstractManager implementation methods

    /** {@inheritDoc} */
    @Override
    public char typeLetter() {
        return 'T';
    }

    /** {@inheritDoc} */
    @Override
    public int getXMLOrder() {
        return Manager.LOGIXS + 1;  // Place after LOGIXS
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public String getBeanTypeHandled(boolean plural) {
        return plural ? "Track Tiles" : "Track Tile";
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public Class<TrackTile> getNamedBeanClass() {
        return TrackTile.class;
    }
}

package jmri.jmrit.display.layoutEditor.configurexml;

import jmri.InstanceManager;
import jmri.configurexml.AbstractXmlAdapter;
import jmri.jmrit.display.layoutEditor.LayoutTrack;
import jmri.jmrit.display.layoutEditor.LayoutTrackView;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.tracktiles.NotATile;
import jmri.tracktiles.TrackTile;
import jmri.tracktiles.TrackTileManager;
import jmri.tracktiles.UnknownTile;

import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Element;

/**
 * Base class for Xml classes for classes that inherits LayoutTrackView.
 * @author Daniel Bergqvist (C) 2022
 */
public abstract class LayoutTrackViewXml extends AbstractXmlAdapter {

    public void storeLogixNG_Data(LayoutTrackView ltv, Element element) {
        if (ltv.getLogixNG() == null) return;

        // Don't save LogixNG data if we don't have any ConditionalNGs
        if (ltv.getLogixNG().getNumConditionalNGs() == 0) return;
        Element logixNG_Element = new Element("LogixNG");
        logixNG_Element.addContent(new Element("InlineLogixNG_SystemName").addContent(ltv.getLogixNG().getSystemName()));
        element.addContent(logixNG_Element);
    }

    public void loadLogixNG_Data(LayoutTrackView ltv, Element element) {
        Element logixNG_Element = element.getChild("LogixNG");
        if (logixNG_Element == null) return;
        Element inlineLogixNG = logixNG_Element.getChild("InlineLogixNG_SystemName");
        if (inlineLogixNG != null) {
            String systemName = inlineLogixNG.getTextTrim();
            ltv.setLogixNG_SystemName(systemName);
            InstanceManager.getDefault(LogixNG_Manager.class).registerSetupTask(() -> {
                ltv.setupLogixNG();
            });
        }
    }

    /**
     * Store TrackTile information to XML element.
     * Adds tile attributes if the track has an associated tile (not NotATile).
     * 
     * @param layoutTrack the LayoutTrack to get tile information from
     * @param element the Element to add tile attributes to
     */
    protected void storeTrackTile(LayoutTrack layoutTrack, Element element) {
        TrackTile trackTile = layoutTrack.getTrackTile();
        if (!(trackTile instanceof NotATile)) {
            element.setAttribute("tileVendor", trackTile.getVendor());
            element.setAttribute("tileFamily", trackTile.getFamily());
            element.setAttribute("tilePartCode", trackTile.getPartCode());
            element.setAttribute("tileJmriType", trackTile.getJmriType());
            
            // Store geometry if present
            if (trackTile.getLength() > 0) {
                element.setAttribute("tileLength", String.valueOf(trackTile.getLength()));
            }
            if (trackTile.getRadius() > 0) {
                element.setAttribute("tileRadius", String.valueOf(trackTile.getRadius()));
            }
            if (trackTile.getArc() != 0) {
                element.setAttribute("tileArc", String.valueOf(trackTile.getArc()));
            }
        }
    }

    /**
     * Load TrackTile information from XML element.
     * Attempts to find the tile in TrackTileManager, creates UnknownTile if not found,
     * or uses NotATile if no tile information is present.
     * 
     * @param layoutTrack the LayoutTrack to set tile information on
     * @param element the Element to read tile attributes from
     */
    protected void loadTrackTile(LayoutTrack layoutTrack, Element element) {
        Attribute tileVendorAttr = element.getAttribute("tileVendor");
        if (tileVendorAttr != null) {
            String tileVendor = tileVendorAttr.getValue();
            String tileFamily = element.getAttributeValue("tileFamily", "");
            String tilePartCode = element.getAttributeValue("tilePartCode", "");
            String tileJmriType = element.getAttributeValue("tileJmriType", "other");
            
            // Try to find the tile in the TrackTileManager
            String systemName = "TT:" + tileVendor + ":" + tileFamily + ":" + tilePartCode;
            TrackTileManager manager = InstanceManager.getDefault(TrackTileManager.class);
            TrackTile tile = manager.getBySystemName(systemName);
            
            if (tile != null) {
                // Found the tile in the manager
                layoutTrack.setTrackTile(tile);
            } else {
                // Tile not found, try to create UnknownTile with saved geometry
                try {
                    UnknownTile unknownTile = new UnknownTile(systemName);
                    
                    // Load geometry if present
                    Attribute tileLengthAttr = element.getAttribute("tileLength");
                    if (tileLengthAttr != null) {
                        try {
                            double length = tileLengthAttr.getDoubleValue();
                            unknownTile.setLength(length);
                        } catch (DataConversionException e) {
                            log.warn("Invalid tileLength value for {}", systemName);
                        }
                    }
                    
                    Attribute tileRadiusAttr = element.getAttribute("tileRadius");
                    if (tileRadiusAttr != null) {
                        try {
                            double radius = tileRadiusAttr.getDoubleValue();
                            unknownTile.setRadius(radius);
                        } catch (DataConversionException e) {
                            log.warn("Invalid tileRadius value for {}", systemName);
                        }
                    }
                    
                    Attribute tileArcAttr = element.getAttribute("tileArc");
                    if (tileArcAttr != null) {
                        try {
                            double arc = tileArcAttr.getDoubleValue();
                            unknownTile.setArc(arc);
                        } catch (DataConversionException e) {
                            log.warn("Invalid tileArc value for {}", systemName);
                        }
                    }
                    
                    layoutTrack.setTrackTile(unknownTile);
                    log.info("Created UnknownTile for missing tile: {}", systemName);
                } catch (Exception e) {
                    log.error("Failed to create UnknownTile for {}, using NotATile", systemName, e);
                    layoutTrack.setTrackTile(NotATile.getInstance());
                }
            }
        } else {
            // No tile information in XML, use NotATile
            layoutTrack.setTrackTile(NotATile.getInstance());
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutTrackViewXml.class);

}

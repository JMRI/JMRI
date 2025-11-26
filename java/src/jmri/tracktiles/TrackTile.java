package jmri.tracktiles;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import jmri.implementation.AbstractNamedBean;

/**
 * Represents a single track tile from a manufacturer's catalog.
 * <p>
 * Extends AbstractNamedBean to integrate with JMRI's Manager framework.
 * System name format: "TT:vendor:family:partcode" (e.g., "TT:Märklin:C-Track:24077")
 * User name: Optional friendly name for the tile
 * 
 * @author Ralf Lang Copyright (C) 2025
 */
public class TrackTile extends AbstractNamedBean {

    private final String vendor;
    private final String family;
    private final String jmriType;
    private final String partCode;
    private final Map<String, String> localizations;
    
    // Geometry fields
    private double length = 0.0;      // For straight tracks, in mm
    private double radius = 0.0;      // For curved tracks, in mm
    private double arc = 0.0;         // For curved tracks, in degrees

    /**
     * Create a TrackTile.
     * 
     * @param systemName The system name in format "TT:vendor:family:partcode"
     * @param vendor     The manufacturer name (e.g., "Märklin")
     * @param family     The track family (e.g., "C-Track")
     * @param jmriType   The JMRI type (e.g., "straight", "curved", "turnout")
     * @param partCode   The manufacturer's part code (e.g., "24077")
     */
    public TrackTile(@Nonnull String systemName, @Nonnull String vendor, @Nonnull String family, 
                     @Nonnull String jmriType, @Nonnull String partCode) {
        super(systemName);
        this.vendor = vendor;
        this.family = family;
        this.jmriType = jmriType;
        this.partCode = partCode;
        this.localizations = new HashMap<>();
    }
    
    /**
     * Create a TrackTile with user name.
     * 
     * @param systemName The system name in format "TT:vendor:family:partcode"
     * @param userName   Optional user-friendly name
     * @param vendor     The manufacturer name (e.g., "Märklin")
     * @param family     The track family (e.g., "C-Track")
     * @param jmriType   The JMRI type (e.g., "straight", "curved", "turnout")
     * @param partCode   The manufacturer's part code (e.g., "24077")
     */
    public TrackTile(@Nonnull String systemName, String userName, @Nonnull String vendor, 
                     @Nonnull String family, @Nonnull String jmriType, @Nonnull String partCode) {
        super(systemName, userName);
        this.vendor = vendor;
        this.family = family;
        this.jmriType = jmriType;
        this.partCode = partCode;
        this.localizations = new HashMap<>();
    }

    /**
     * Add a localized caption for this track tile.
     * 
     * @param lang    Language code (e.g., "en", "de", "es")
     * @param caption Localized description
     */
    public void addLocalization(@Nonnull String lang, @Nonnull String caption) {
        localizations.put(lang, caption);
    }

    @Nonnull
    public String getVendor() {
        return vendor;
    }

    @Nonnull
    public String getFamily() {
        return family;
    }

    @Nonnull
    public String getJmriType() {
        return jmriType;
    }

    @Nonnull
    public String getPartCode() {
        return partCode;
    }

    /**
     * Set the length for straight track tiles.
     * 
     * @param length The length in millimeters
     */
    public void setLength(double length) {
        this.length = length;
    }

    /**
     * Get the length for straight track tiles.
     * 
     * @return The length in millimeters, or 0.0 if not set
     */
    public double getLength() {
        return length;
    }

    /**
     * Set the radius for curved track tiles.
     * 
     * @param radius The radius in millimeters
     */
    public void setRadius(double radius) {
        this.radius = radius;
    }

    /**
     * Get the radius for curved track tiles.
     * 
     * @return The radius in millimeters, or 0.0 if not set
     */
    public double getRadius() {
        return radius;
    }

    /**
     * Set the arc angle for curved track tiles.
     * 
     * @param arc The arc angle in degrees
     */
    public void setArc(double arc) {
        this.arc = arc;
    }

    /**
     * Get the arc angle for curved track tiles.
     * 
     * @return The arc angle in degrees, or 0.0 if not set
     */
    public double getArc() {
        return arc;
    }

    /**
     * Get the localized caption, preferring the given language.
     * Falls back to English, then German, then any available language.
     * 
     * @param preferredLang The preferred language code
     * @return The caption in the best available language
     */
    @Nonnull
    public String getCaption(@Nonnull String preferredLang) {
        // Try preferred language
        String caption = localizations.get(preferredLang);
        if (caption != null) {
            return caption;
        }
        
        // Fall back to English
        caption = localizations.get("en");
        if (caption != null) {
            return caption;
        }
        
        // Fall back to German
        caption = localizations.get("de");
        if (caption != null) {
            return caption;
        }
        
        // Return first available or empty string
        if (!localizations.isEmpty()) {
            return localizations.values().iterator().next();
        }
        
        return "";
    }

    /**
     * Check if localizations are available.
     * 
     * @return true if at least one localization exists
     */
    public boolean hasLocalizations() {
        return !localizations.isEmpty();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getState() {
        // TrackTiles are catalog items with no state
        return 0;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setState(int s) {
        // TrackTiles are catalog items with no state
        // This is a no-op
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBeanType() {
        return "Track Tile";
    }
}

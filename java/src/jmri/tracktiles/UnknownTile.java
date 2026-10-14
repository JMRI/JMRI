package jmri.tracktiles;

import javax.annotation.Nonnull;

/**
 * Represents an unknown or unrecognized track tile.
 * Used as a placeholder when tile data is incomplete or invalid.
 * 
 * @author Ralf Lang Copyright (C) 2025
 */
public class UnknownTile extends TrackTile {

    /**
     * Create an UnknownTile with default values.
     */
    public UnknownTile() {
        super("TT:Unknown:Unknown:Unknown", "Unknown", "Unknown", "other", "Unknown");
    }

    /**
     * Create an UnknownTile with specific system name.
     * 
     * @param systemName The system name for this unknown tile
     */
    public UnknownTile(@Nonnull String systemName) {
        super(systemName, "Unknown", "Unknown", "other", "Unknown");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBeanType() {
        return "Unknown Track Tile";
    }
}

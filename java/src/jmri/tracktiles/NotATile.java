package jmri.tracktiles;

import javax.annotation.Nonnull;

/**
 * Represents "not a tile" selection in the Layout Editor.
 * All JMRI track sections, turnouts etc before 5.14 are NotATile
 * All freeform JMRI items which are not from a tile library are NotATile
 * 
 * @author Ralf Lang Copyright (C) 2025
 */
public class NotATile extends TrackTile {

    private static final NotATile INSTANCE = new NotATile();

    /**
     * Private constructor for singleton pattern.
     */
    private NotATile() {
        super("TT:NotATile:NotATile:NotATile", "Not a tile", "Not a tile", "other", "NotATile");
    }

    /**
     * Get the singleton instance of NotATile.
     * 
     * @return the single NotATile instance
     */
    @Nonnull
    public static NotATile getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBeanType() {
        return "Not a Track Tile";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String getCaption(@Nonnull String preferredLang) {
        return "Not a tile";
    }
}

package jmri.tracktiles;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.Manager;

/**
 * Interface for TrackTile Manager.
 * <p>
 * Manages TrackTile catalog objects loaded from XML files.
 * TrackTiles are read-only catalog items representing manufacturer track tiles.
 * 
 * @author Ralf Lang Copyright (C) 2025
 */
public interface TrackTileManager extends Manager<TrackTile> {

    /**
     * Locate a TrackTile by system name.
     * 
     * @param systemName System name of the TrackTile (format: "TT:vendor:family:partcode")
     * @return requested TrackTile object or null if none exists
     */
    @Override
    @CheckForNull
    TrackTile getBySystemName(@Nonnull String systemName);

    /**
     * Locate a TrackTile by user name.
     * 
     * @param userName User name of the TrackTile
     * @return requested TrackTile object or null if none exists
     */
    @Override
    @CheckForNull
    TrackTile getByUserName(@Nonnull String userName);

    /**
     * Get a TrackTile by name (user name or system name).
     * 
     * @param name TrackTile name to locate
     * @return requested TrackTile object or null if none exists
     */
    @CheckForNull
    TrackTile getTrackTile(@Nonnull String name);

    /**
     * Get the number of TrackTile objects managed.
     * 
     * @return number of TrackTiles
     */
    int getTrackTileCount();
}

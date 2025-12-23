package jmri.jmrit.display.layoutEditor;

import javax.annotation.CheckForNull;
import jmri.tracktiles.TrackTile;

/**
 * Interface for layout elements that can have an associated track tile.
 * <p>
 * This interface provides methods to check if a layout element has a
 * track tile associated with it and to retrieve that tile.
 *
 * @author Ralf Lang
 */
public interface TileSupport {

    /**
     * Check if this layout element has a track tile associated with it.
     *
     * @return true if a track tile is associated, false otherwise
     */
    boolean hasTile();

    /**
     * Get the track tile associated with this layout element.
     *
     * @return the associated TrackTile, or null if none is associated
     */
    @CheckForNull
    TrackTile getTile();
}

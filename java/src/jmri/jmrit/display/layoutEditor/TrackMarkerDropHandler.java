package jmri.jmrit.display.layoutEditor;

import java.awt.Component;

import javax.annotation.Nonnull;
import javax.swing.JPopupMenu;

import jmri.Block;
import jmri.InvokeOnGuiThread;
import jmri.jmrit.display.LocoIcon;

/**
 * Something that takes a loco marker dropped on a Block's track, on a Layout
 * Editor panel in operating mode, before train tracking is told the train is
 * there.
 * <p>
 * Implementations are found with {@link java.util.ServiceLoader}
 * ({@code @ServiceProvider(service = TrackMarkerDropHandler.class)}), so
 * Layout Editor needs to know nothing of them. With no handler
 * {@link #isActive() active}, a dropped marker only tells train tracking
 * where the train is.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
public interface TrackMarkerDropHandler {

    /**
     * @return true if this handler takes dropped markers now
     */
    @InvokeOnGuiThread
    boolean isActive();

    /**
     * A loco marker was dropped on the track of a Block. Called only while
     * this handler is {@link #isActive() active}.
     *
     * @param marker            the marker
     * @param block             the Block of the track it was dropped on
     * @param parent            the panel, for the handler's own windows
     * @param tellTrainTracking run this to tell train tracking the marker's
     *                          train is in the Block, as a drop does with no
     *                          handler; not run, train tracking is not told
     */
    @InvokeOnGuiThread
    void markerDropped(@Nonnull LocoIcon marker, @Nonnull Block block, @Nonnull Component parent,
            @Nonnull Runnable tellTrainTracking);

    /**
     * Add this handler's items for a Block to the menu the Block's track
     * shows in operating mode. Called only while this handler is
     * {@link #isActive() active}.
     *
     * @param popup the menu
     * @param block the Block
     */
    @InvokeOnGuiThread
    void addBlockMenuItems(@Nonnull JPopupMenu popup, @Nonnull Block block);
}

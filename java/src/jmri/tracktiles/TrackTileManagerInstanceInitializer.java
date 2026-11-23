package jmri.tracktiles;

import java.util.Set;

import javax.annotation.Nonnull;

import jmri.InstanceInitializer;

import org.openide.util.lookup.ServiceProvider;

/**
 * Instance initializer for TrackTileManager.
 * 
 * @author Ralf Lang Copyright (C) 2025
 */
@ServiceProvider(service = InstanceInitializer.class)
public class TrackTileManagerInstanceInitializer implements InstanceInitializer {

    @Override
    @Nonnull
    public <T> Object getDefault(@Nonnull Class<T> type) {
        if (type.equals(TrackTileManager.class)) {
            return new DefaultTrackTileManager();
        }
        throw new IllegalArgumentException("TrackTileManagerInstanceInitializer can only create TrackTileManager instances");
    }

    @Override
    @Nonnull
    public Set<Class<?>> getInitalizes() {
        return Set.of(TrackTileManager.class);
    }
}

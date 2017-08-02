package jmri.jmrit.audio;

import java.util.Set;
import jmri.AudioManager;
import jmri.InstanceInitializer;
import jmri.implementation.AbstractInstanceInitializer;
import org.openide.util.lookup.ServiceProvider;

/**
 * Initializer for the default {@link jmri.AudioManager}.
 *
 * @author Randall Wood Copyright 2017
 */
@ServiceProvider(service = InstanceInitializer.class)
public class AudioManagerInstanceInitializer extends AbstractInstanceInitializer {

    @Override
    public <T> Object getDefault(Class<T> type) throws IllegalArgumentException {
        if (type.equals(AudioManager.class)) {
            AudioManager manager = new DefaultAudioManager();
            manager.init();
            return manager;
        }

        return super.getDefault(type);
    }

    @Override
    public Set<Class<?>> getInitalizes() {
        Set set = super.getInitalizes();
        set.add(AudioManager.class);
        return set;
    }

}

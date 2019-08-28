package jmri.jmrit.logixng.template;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logixng.NullNamedBeanInitializer;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provide the usual default implementations for the
 * {@link jmri.jmrit.logixng.template.TemplateInstanceManager}.
 * <P>
 * @author Daniel Bergqvist Copyright (C) 2019
 */
@ServiceProvider(service = NullNamedBeanInitializer.class)
public class DefaultNullNamedBeanInitializer implements NullNamedBeanInitializer {

    @Override
    public <T> Object create(Class<T> type, @Nonnull String name) {

        // In order for getDefault() to create a new object, the type also
        // needs to be added to the method getInitalizes() below.

        if (type == NullAudio.class) {
            return new NullAudio(name);
        }

        if (type == NullIdTag.class) {
            return new NullIdTag(name);
        }

//        if (type == NullConditional.class) {
//            return new NullConditional(name);
//        }

        if (type == NullLight.class) {
            return new NullLight(name);
        }

        if (type == NullLogix.class) {
            return new NullLogix(name);
        }

        if (type == NullMemory.class) {
            return new NullMemory(name);
        }

        if (type == NullReporter.class) {
            return new NullReporter(name);
        }

        if (type == NullSensor.class) {
            return new NullSensor(name);
        }

        if (type == NullSignalHead.class) {
            return new NullSignalHead(name);
        }

        if (type == NullSignalMast.class) {
            return new NullSignalMast(name);
        }

        if (type == NullTurnout.class) {
            return new NullTurnout(name);
        }

        if (type == OBlock.class) {
            return new OBlock(name);
        }

        throw new IllegalArgumentException();
    }

    @Override
    public Set<Class<?>> getInitalizes() {
        Set<Class<?>> set = new HashSet<>();
        set.addAll(Arrays.asList(
                NullAudio.class,
                NullIdTag.class,
                NullLight.class,
                NullLogix.class,
                NullMemory.class,
                NullReporter.class,
                NullSensor.class,
                NullSignalHead.class,
                NullSignalMast.class,
                NullTurnout.class,
                OBlock.class
        ));
        return set;
    }

}

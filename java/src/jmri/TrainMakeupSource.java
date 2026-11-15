package jmri;

import java.util.List;
import java.util.Objects;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import jmri.beans.PropertyChangeProvider;

/**
 * A source of what a train is made up of, and the work it has still to do,
 * such as Operations' trains: its components, front first, each with its
 * length, and the stops ahead with the cars and engines to set out and pick
 * up at each. A component is a part the train is split into or joined from
 * as one piece, as {@link TrainRecord.TrainComponent} describes. Train
 * tracking gives the train record of the same name these components and
 * their length.
 * <p>
 * Sources are found in the InstanceManager's list for this class.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
public interface TrainMakeupSource extends PropertyChangeProvider {

    /**
     * Property name for a change in a train's makeup or work; the new value
     * is the train's name.
     */
    String PROPERTY_MAKEUP = "makeup"; // NOI18N

    /**
     * @param trainName a train's name
     * @return what it is made up of now, or null if this source doesn't know
     *         a train of that name, or it has nothing in it
     */
    @CheckForNull
    TrainMakeup getMakeup(@Nonnull String trainName);

    /**
     * @param trainName a train's name
     * @return what it is made up of, from the first source in the
     *         InstanceManager's list that knows it, or null if none does
     */
    @CheckForNull
    static TrainMakeup find(@Nonnull String trainName) {
        for (TrainMakeupSource s : InstanceManager.getList(TrainMakeupSource.class)) {
            TrainMakeup m = s.getMakeup(trainName);
            if (m != null) {
                return m;
            }
        }
        return null;
    }

    /**
     * What a train is made up of, and the work it has still to do.
     */
    @Immutable
    final class TrainMakeup {

        private final List<TrainRecord.TrainComponent> components;
        private final List<Work> work;

        /**
         * @param components the components, front first, at least one
         * @param work       the stops ahead, in order
         * @throws IllegalArgumentException if there are no components
         */
        public TrainMakeup(@Nonnull List<TrainRecord.TrainComponent> components, @Nonnull List<Work> work) {
            if (components.isEmpty()) {
                throw new IllegalArgumentException("a train is made up of at least one component");
            }
            this.components = List.copyOf(components);
            this.work = List.copyOf(work);
        }

        /**
         * @return the components, front first, never empty
         */
        @Nonnull
        public List<TrainRecord.TrainComponent> getTrainComponents() {
            return components;
        }

        /**
         * @return the sum of the components' lengths in millimetres, or null
         *         if a component has none
         */
        @CheckForNull
        public Double getLength() {
            double sum = 0;
            for (TrainRecord.TrainComponent c : components) {
                Double length = c.getLength();
                if (length == null) {
                    return null;
                }
                sum += length;
            }
            return sum;
        }

        /**
         * @return the stops ahead, in order
         */
        @Nonnull
        public List<Work> getWork() {
            return work;
        }
    }

    /**
     * The work at one stop ahead.
     */
    @Immutable
    final class Work {

        private final String location;
        private final int setOuts;
        private final int pickUps;

        /**
         * @param location the stop's name
         * @param setOuts  how many cars and engines are to be set out there
         * @param pickUps  how many are to be picked up there
         */
        public Work(@Nonnull String location, int setOuts, int pickUps) {
            this.location = Objects.requireNonNull(location, "location");
            this.setOuts = setOuts;
            this.pickUps = pickUps;
        }

        /**
         * @return the stop's name
         */
        @Nonnull
        public String getLocation() {
            return location;
        }

        /**
         * @return how many cars and engines are to be set out there
         */
        public int getSetOuts() {
            return setOuts;
        }

        /**
         * @return how many are to be picked up there
         */
        public int getPickUps() {
            return pickUps;
        }
    }
}

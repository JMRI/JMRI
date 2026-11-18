package jmri.jmrit.operations.trains;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.InstanceManager;
import jmri.TrainMakeupSource;
import jmri.TrainRecord;
import jmri.beans.PropertyChangeSupport;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.roster.RosterEntry;

/**
 * Operations' trains as a {@link TrainMakeupSource}: what a built train is
 * made up of now, and the set-outs and pick-ups at each stop ahead.
 * <p>
 * The train's components are the parts it will be split into: its engines
 * and cars in Operations' order (engines first, then the cars as the
 * manifest blocks them), divided where the stop each is set out at changes.
 * So each rake of cars to be left at a stop is one component, and what goes
 * on to the end, engines included, is another. A component's length is the
 * sum of its engines' and cars' lengths on the model, in millimetres.
 * <p>
 * Registered by the {@link TrainManager} when it starts; it watches the
 * trains only while something listens to it.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
public class OperationsTrainMakeupSource extends PropertyChangeSupport implements TrainMakeupSource {

    private final Set<Train> watched = new HashSet<>();
    private final PropertyChangeListener trainListener = this::trainChanged;
    private final PropertyChangeListener managerListener = e -> watchTrains();
    private boolean watching = false;

    /**
     * Make the source. It watches the default {@link TrainManager}'s trains
     * only while it has listeners, so it costs Operations nothing when train
     * tracking isn't in use.
     */
    public OperationsTrainMakeupSource() {
        // nothing watched until a listener is added
    }

    /** {@inheritDoc} */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        super.addPropertyChangeListener(listener);
        startOrStop();
    }

    /** {@inheritDoc} */
    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        super.addPropertyChangeListener(propertyName, listener);
        startOrStop();
    }

    /** {@inheritDoc} */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        super.removePropertyChangeListener(listener);
        startOrStop();
    }

    /** {@inheritDoc} */
    @Override
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        super.removePropertyChangeListener(propertyName, listener);
        startOrStop();
    }

    private synchronized void startOrStop() {
        boolean wanted = getPropertyChangeListeners().length > 0;
        if (wanted && !watching) {
            watching = true;
            InstanceManager.getDefault(TrainManager.class)
                    .addPropertyChangeListener(TrainManager.LISTLENGTH_CHANGED_PROPERTY, managerListener);
            watchTrains();
        } else if (!wanted && watching) {
            dispose();
        }
    }

    /**
     * @return true while it is watching the trains
     */
    boolean isWatching() {
        return watching;
    }

    private void watchTrains() {
        for (Train t : InstanceManager.getDefault(TrainManager.class).getTrainsByIdList()) {
            if (watched.add(t)) {
                t.addPropertyChangeListener(trainListener);
            }
        }
    }

    private void trainChanged(PropertyChangeEvent e) {
        String p = e.getPropertyName();
        if (Train.BUILT_CHANGED_PROPERTY.equals(p) || Train.TRAIN_LOCATION_CHANGED_PROPERTY.equals(p)
                || Train.TRAIN_MOVE_COMPLETE_CHANGED_PROPERTY.equals(p)) {
            firePropertyChange(PROPERTY_MAKEUP, null, ((Train) e.getSource()).getName());
        }
    }

    /** {@inheritDoc} */
    @Override
    @CheckForNull
    public TrainMakeup getMakeup(@Nonnull String trainName) {
        Train train = InstanceManager.getDefault(TrainManager.class).getTrainByName(trainName);
        if (train == null || !train.isBuilt()) {
            return null;
        }
        Route route = train.getRoute();
        RouteLocation current = train.getCurrentRouteLocation();
        if (route == null || current == null) {
            return null;
        }
        // Operations' order: engines first, then the cars as the manifest blocks them (each once)
        Set<RollingStock> stock = new LinkedHashSet<>();
        stock.addAll(InstanceManager.getDefault(EngineManager.class).getByTrainBlockingList(train));
        stock.addAll(InstanceManager.getDefault(CarManager.class).getByTrainDestinationList(train));
        stock.removeIf(rs -> rs.getTrain() != train);

        // what is in the train now: picked up at or before here, not set out by here
        Set<RollingStock> inTrain = new HashSet<>();
        List<TrainMakeupSource.Work> work = new ArrayList<>();
        boolean ahead = false;
        for (RouteLocation rl : route.getLocationsBySequenceList()) {
            int setOuts = 0;
            int pickUps = 0;
            for (RollingStock rs : stock) {
                // picked up before set out: once the train has moved on, Operations gives every car in it
                // the train's current stop as its location, so a car set out here is both picked up and
                // set out here, and must not be left in the train (as in Train.getTrainLength)
                if (rs.getRouteLocation() == rl) {
                    pickUps++;
                    if (!ahead) {
                        inTrain.add(rs);
                    }
                }
                if (rs.getRouteDestination() == rl) {
                    setOuts++;
                    if (!ahead) {
                        inTrain.remove(rs);
                    }
                }
            }
            if (ahead && (setOuts > 0 || pickUps > 0)) {
                work.add(new TrainMakeupSource.Work(rl.getName(), setOuts, pickUps));
            }
            if (rl.getId().equals(current.getId())) {
                ahead = true;
            }
        }
        List<RollingStock> now = new ArrayList<>();
        for (RollingStock rs : stock) {
            if (inTrain.contains(rs)) {
                now.add(rs);
            }
        }
        if (now.isEmpty()) {
            return null;
        }
        return new TrainMakeup(components(now), work);
    }

    /**
     * Divide a train's rolling stock, in Operations' order, into components
     * where the stop each is set out at changes. Each is named after that
     * stop, and its length is the sum of its rolling stock's lengths, or not
     * known if one of them has no length. Its wheel overhang at each end is
     * the roster entry's of the engine at that end, where it is an engine
     * whose roster entry gives one.
     *
     * @param inTrain the rolling stock in the train now, in Operations' order
     * @return the components, front first
     */
    static List<TrainRecord.TrainComponent> components(List<RollingStock> inTrain) {
        List<TrainRecord.TrainComponent> components = new ArrayList<>();
        List<RollingStock> part = new ArrayList<>();
        RouteLocation stop = null;
        for (RollingStock rs : inTrain) {
            RouteLocation dest = rs.getRouteDestination();
            if (!part.isEmpty() && dest != stop) {
                components.add(component(stop, part));
                part.clear();
            }
            stop = dest;
            part.add(rs);
        }
        if (!part.isEmpty()) {
            components.add(component(stop, part));
        }
        return components;
    }

    private static TrainRecord.TrainComponent component(@CheckForNull RouteLocation stop, List<RollingStock> part) {
        Double length = 0.0;
        for (RollingStock rs : part) {
            Double mm = millimetres(rs.getTotalLength());
            length = (length == null || mm == null) ? null : length + mm;
        }
        return new TrainRecord.TrainComponent(stop != null ? stop.getName() : "", length,
                wheelOverhang(part.get(0), true), wheelOverhang(part.get(part.size() - 1), false));
    }

    /**
     * @param rs    rolling stock at one end of a component
     * @param front true for its front, false for its rear
     * @return its wheel overhang at that end from its roster entry, if it is
     *         an engine whose roster entry gives one, or null
     */
    @CheckForNull
    static Double wheelOverhang(RollingStock rs, boolean front) {
        if (!(rs instanceof Engine)) {
            return null;
        }
        RosterEntry entry = ((Engine) rs).getRosterEntry();
        return entry == null ? null : entry.getWheelOverhangMm(front);
    }

    /**
     * @param length a length in Operations' units: scale feet or metres
     * @return the length on the model in millimetres, or null if not known
     */
    @CheckForNull
    static Double millimetres(int length) {
        int ratio = Setup.getScaleRatio();
        if (length <= 0 || ratio <= 0) {
            return null;
        }
        double scale = Setup.METER.equals(Setup.getLengthUnit()) ? 1000.0 : 304.8;
        return length * scale / ratio;
    }

    /**
     * Stop watching the trains.
     */
    public synchronized void dispose() {
        watching = false;
        InstanceManager.getDefault(TrainManager.class)
                .removePropertyChangeListener(TrainManager.LISTLENGTH_CHANGED_PROPERTY, managerListener);
        for (Train t : watched) {
            t.removePropertyChangeListener(trainListener);
        }
        watched.clear();
    }
}

package jmri.jmrit.dispatcher;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.Block;
import jmri.EntryPoint;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.Path;
import jmri.Section;
import jmri.SectionManager;
import jmri.Transit;
import jmri.TransitManager;
import jmri.TransitSection;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.simulation.SimulatedLoco;
import jmri.jmrit.simulation.SimulationTestLayout;
import jmri.jmrit.simulation.SimulationTestLayouts;
import jmri.jmrit.simulation.SimulationTestUtil;
import jmri.jmrit.simulation.SimulationTruth;
import jmri.jmrit.simulation.VirtualTrain;
import jmri.util.FileUtil;
import jmri.util.JUnitUtil;

/**
 * Dispatcher running simulated trains on one of the standard test layouts
 * ({@link SimulationTestLayouts}), for scenario tests: each scenario sets up
 * Sections, a Transit and a train as a user would, runs the train
 * automatically, and checks what happened against the ground truth of the
 * simulated layout.
 * <p>
 * Dispatcher's options are set for automatic running with No Signals, Use
 * connectivity from the layout's panel, Auto Allocate, Auto Release and Auto
 * Set Turnouts; a scenario changes any it is about. Speeds are percent of
 * throttle, from the test Warrant preferences ("Normal" 100%, "Medium" 50%).
 * <p>
 * Every change of a Block's occupancy is recorded, with where the trains
 * really were at that moment, so a scenario can check the order of events
 * and not how long they took.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public final class DispatcherScenario {

    private final SimulationTestLayout layout;
    private final List<String> events = Collections.synchronizedList(new ArrayList<>());
    private final List<Runnable> undo = new ArrayList<>();
    private DispatcherFrame dispatcher;

    /**
     * Set up the profile, with the test Warrant preferences, and build a
     * layout. Call after {@code JUnitUtil.setUp()}.
     *
     * @param folder an empty folder for the profile
     * @param layout makes the layout, such as
     *               {@code SimulationTestLayouts::loopWithPassingLoop}
     * @throws IOException if the Warrant preferences can't be copied
     */
    public DispatcherScenario(@Nonnull File folder,
            @Nonnull java.util.function.Supplier<SimulationTestLayout> layout) throws IOException {
        SimulationTestLayouts.setUpProfile(folder);
        File signal = new File(FileUtil.getUserFilesPath(), "signal");
        FileUtil.createDirectory(signal);
        Files.copy(new File(FileUtil.getProgramPath(), "java/test/jmri/jmrit/dispatcher/signal/WarrantPreferences.xml")
                .toPath(), new File(signal, "WarrantPreferences.xml").toPath(), StandardCopyOption.REPLACE_EXISTING);
        this.layout = layout.get();
        for (Block b : this.layout.getBlocks().values()) {
            PropertyChangeListener l = e -> {
                if (Block.OCC_SENSOR_CHANGE.equals(e.getPropertyName()) || !"state".equals(e.getPropertyName())) {
                    return;
                }
                int state = (Integer) e.getNewValue();
                if (state == Block.OCCUPIED || state == Block.UNOCCUPIED) {
                    events.add(b.getUserName() + (state == Block.OCCUPIED ? " occupied" : " unoccupied")
                            + where());
                }
            };
            b.addPropertyChangeListener(l);
            undo.add(() -> b.removePropertyChangeListener(l));
        }
    }

    /**
     * @return the Blocks each train is really in, front first, as " [T in
     *         Main West]"
     */
    private static String where() {
        StringBuilder s = new StringBuilder();
        for (SimulationTruth.TrainTruth t : SimulationTestUtil.truth().getTrains()) {
            s.append(s.length() == 0 ? " [" : "; ").append(t.getTrain().getName()).append(" in");
            for (Block b : t.getBlocks()) {
                s.append(' ').append(b.getUserName());
            }
        }
        return s.length() == 0 ? "" : s.append("]").toString();
    }

    /**
     * @return the layout
     */
    @Nonnull
    public SimulationTestLayout layout() {
        return layout;
    }

    /**
     * @param name a Block's user name
     * @return the Block
     */
    @Nonnull
    public Block block(@Nonnull String name) {
        return layout.block(name);
    }

    /**
     * Make a Section of Blocks in order, with entry points as the Section
     * Table makes them: into its first Block from outside, forward; into its
     * last Block, reverse. A one-Block Section needs the neighbours a train
     * enters it from going forward, such as "Main|Loop" for a turnout Block.
     *
     * @param name        the Section's user name
     * @param forwardFrom for a one-Block Section, the neighbours it is entered
     *                    from going forward, separated by "|"; otherwise null
     * @param blocks      its Blocks, in order
     * @return the Section
     */
    @Nonnull
    public Section section(@Nonnull String name, @CheckForNull String forwardFrom, @Nonnull String... blocks) {
        Section s = InstanceManager.getDefault(SectionManager.class).createNewSection(name);
        List<Block> list = new ArrayList<>();
        for (String b : blocks) {
            list.add(block(b));
            s.addBlock(block(b));
        }
        if (list.size() == 1 && forwardFrom == null) {
            throw new IllegalArgumentException("a one-Block Section needs the neighbour it is entered from going forward");
        }
        for (int i = 0; i < list.size(); i++) {
            Block b = list.get(i);
            for (Path p : b.getPaths()) {
                if (list.contains(p.getBlock())) {
                    continue;
                }
                EntryPoint ep = new EntryPoint(b, p.getBlock(), Path.decodeDirection(p.getFromBlockDirection()));
                boolean forward = list.size() == 1
                        ? Arrays.asList(forwardFrom.split("\\|")).contains(p.getBlock().getUserName())
                        : i == 0;
                if (forward) {
                    ep.setTypeForward();
                    s.addToForwardList(ep);
                } else {
                    ep.setTypeReverse();
                    s.addToReverseList(ep);
                }
            }
        }
        return s;
    }

    /**
     * Make a Transit of Sections, each travelled forward, in the order given.
     *
     * @param name     the Transit's user name
     * @param sections its Sections
     * @return the Transit
     * @throws NamedBean.BadNameException if the name is in use
     */
    @Nonnull
    public Transit transit(@Nonnull String name, @Nonnull Section... sections) throws NamedBean.BadNameException {
        Transit t = InstanceManager.getDefault(TransitManager.class).createNewTransit(name);
        int seq = 0;
        for (Section s : sections) {
            t.addTransitSection(new TransitSection(s, ++seq, Section.FORWARD));
        }
        return t;
    }

    /**
     * Make a Transit of Sections, each travelled in the direction given.
     *
     * @param name       the Transit's user name
     * @param directions for each Section, {@link Section#FORWARD} or
     *                   {@link Section#REVERSE}
     * @param sections   its Sections
     * @return the Transit
     * @throws NamedBean.BadNameException if the name is in use
     */
    @Nonnull
    public Transit transit(@Nonnull String name, @Nonnull int[] directions, @Nonnull Section... sections)
            throws NamedBean.BadNameException {
        if (directions.length != sections.length) {
            throw new IllegalArgumentException("one direction for each Section");
        }
        Transit t = InstanceManager.getDefault(TransitManager.class).createNewTransit(name);
        for (int i = 0; i < sections.length; i++) {
            t.addTransitSection(new TransitSection(sections[i], i + 1, directions[i]));
        }
        return t;
    }

    /**
     * Start Dispatcher, with the options this class describes. Call after
     * making the Sections and Transits.
     *
     * @return Dispatcher, to change options on before loading a train
     */
    @Nonnull
    public DispatcherFrame dispatcher() {
        if (dispatcher == null) {
            DispatcherFrame d = InstanceManager.getDefault(DispatcherFrame.class);
            d.setLayoutEditor(layout.getEditor());
            d.setUseConnectivity(true);
            d.setSignalType(DispatcherFrame.SECTIONSALLOCATED);
            d.setHasOccupancyDetection(true);
            d.setAutoAllocate(true);
            d.setAutoRelease(true);
            d.setAutoTurnouts(true);
            d.setTrainsFrom(DispatcherFrame.TrainsFrom.TRAINSFROMROSTER);
            dispatcher = d;
        }
        return dispatcher;
    }

    /**
     * Place a simulated train with a roster loco, and wait for its Blocks to
     * show it.
     *
     * @param name     the train's name
     * @param loco     its loco
     * @param lengthMm its length
     * @param block    the Block its front is in
     * @param from     the Block its rear is towards
     * @param frontMm  how far its front is into the Block
     * @return the train
     * @throws IOException if the roster file can't be written
     */
    @Nonnull
    public VirtualTrain place(@Nonnull String name, @Nonnull SimulationTestLayouts.Loco loco, double lengthMm,
            @Nonnull String block, @Nonnull String from, double frontMm) throws IOException {
        RosterEntry re = SimulationTestLayouts.rosterEntry(loco);
        VirtualTrain t = VirtualTrain.builder(name).loco(SimulatedLoco.builder(re).length(Math.min(200, lengthMm))
                .build()).length(lengthMm).build();
        SimulationTestUtil.place(t, block(block), block(from), frontMm);
        JUnitUtil.waitFor(() -> block(block).getState() == Block.OCCUPIED, name + " shown in " + block);
        return t;
    }

    /**
     * Train information for an automatic train from the roster, running its
     * whole Transit, as Activate New Train would make it: entire train
     * detection, ramp rate none, allocate as far as it can.
     *
     * @param transit  the Transit
     * @param loco     the roster loco
     * @param lengthMm the train's length, for Max Train Length
     * @param start    its start Block, in the Transit's first Section
     * @param end      its destination Block, in the Transit's last Section
     * @return the train information, to change before {@link #run}
     */
    @Nonnull
    public TrainInfo trainInfo(@Nonnull Transit transit, @Nonnull SimulationTestLayouts.Loco loco, double lengthMm,
            @Nonnull String start, @Nonnull String end) {
        TrainInfo info = new TrainInfo();
        info.setTransitName(transit.getDisplayName());
        info.setTransitId(transit.getDisplayName());
        info.setTrainName(loco.getId());
        info.setTrainUserName(loco.getId());
        info.setTrainFromRoster(true);
        info.setRosterId(loco.getId());
        info.setStartBlockName(start);
        info.setStartBlockId(start);
        info.setStartBlockSeq(1);
        info.setDestinationBlockName(end);
        info.setDestinationBlockId(end);
        info.setDestinationBlockSeq(transit.getMaxSequence());
        info.setAutoRun(true);
        info.setAllocationMethod(ActiveTrain.ALLOCATE_AS_FAR_AS_IT_CAN);
        info.setTrainDetection(ActiveTrain.TrainDetection.TRAINDETECTION_WHOLETRAIN);
        info.setRampRate(Bundle.getMessage("RAMP_NONE"));
        info.setSpeedFactor(1.0f);
        info.setMaxSpeed(1.0f);
        info.setMinReliableOperatingSpeed(0.0f);
        info.setPriority(5);
        info.setMaxTrainLengthScaleMeters(
                (float) (lengthMm / 1000.0 / dispatcher().getScale().getScaleFactor()));
        return info;
    }

    /**
     * Load a train into Dispatcher, which starts it.
     *
     * @param info its train information
     * @return its Active Train
     */
    @Nonnull
    public ActiveTrain run(@Nonnull TrainInfo info) {
        dispatcher().loadTrainFromTrainInfoThrowsException(info, DispatcherFrame.OVERRIDETYPE_NONE, "");
        for (ActiveTrain at : dispatcher().getActiveTrainsList()) {
            if (at.getTrainName().equals(info.getTrainName())) {
                return at;
            }
        }
        throw new IllegalStateException("train " + info.getTrainName() + " not loaded");
    }

    /**
     * @return every change of a Block's occupancy so far, in order, as
     *         "Main occupied [T in Main West]"
     */
    @Nonnull
    public List<String> events() {
        synchronized (events) {
            return new ArrayList<>(events);
        }
    }

    /**
     * Wait up to a minute for a condition, failing with the events so far.
     *
     * @param condition   the condition
     * @param description what is waited for
     */
    public void waitFor(@Nonnull JUnitUtil.ReleaseUntil condition, @Nonnull String description) {
        int before = JUnitUtil.WAITFOR_MAX_DELAY;
        JUnitUtil.WAITFOR_MAX_DELAY = 60000;
        try {
            JUnitUtil.waitFor(condition, () -> description + "; events: " + events());
        } finally {
            JUnitUtil.WAITFOR_MAX_DELAY = before;
        }
    }

    /**
     * Wait until a train has really stopped, its throttle at zero and the
     * simulated train still.
     *
     * @param at    the Active Train
     * @param train its simulated train
     */
    public void waitForStop(@Nonnull ActiveTrain at, @Nonnull VirtualTrain train) {
        waitFor(() -> at.getAutoActiveTrain() != null && at.getAutoActiveTrain().getThrottle() != null
                && at.getAutoActiveTrain().getThrottle().getSpeedSetting() <= 0.0f
                && SimulationTestUtil.truth(train).getSpeedMmPerSecond() == 0.0
                && at.getStatus() != ActiveTrain.RUNNING, at.getTrainName() + " stopped");
    }

    /**
     * Terminate every Active Train, remove the trains, close Dispatcher's
     * windows and the layout. Call in the test's tear-down, before
     * {@code JUnitUtil.tearDown()}.
     */
    public void dispose() {
        if (dispatcher != null) {
            for (ActiveTrain at : new ArrayList<>(dispatcher.getActiveTrainsList())) {
                dispatcher.terminateActiveTrain(at, true, false);
            }
        }
        SimulationTestUtil.removeTrains();
        undo.forEach(Runnable::run);
        if (dispatcher != null) {
            JUnitUtil.dispose(dispatcher);
        }
        JUnitUtil.resetWindows(false, false);
        JUnitUtil.clearShutDownManager();
        layout.dispose();
        JUnitUtil.deregisterBlockManagerShutdownTask();
    }
}

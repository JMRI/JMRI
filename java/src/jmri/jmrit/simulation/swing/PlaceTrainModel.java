package jmri.jmrit.simulation.swing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.Block;
import jmri.DccLocoAddress;
import jmri.Path;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.simulation.Detection;
import jmri.jmrit.simulation.SimulatedLayout;
import jmri.jmrit.simulation.SimulatedLoco;
import jmri.jmrit.simulation.SimulationTruth;
import jmri.jmrit.simulation.VirtualTrain;

/**
 * What is known when a loco marker is dropped on a Block, what is offered for
 * the rest, and the placing itself. No windows: {@link PlaceTrainPanel} shows
 * it.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
final class PlaceTrainModel {

    /** The train length offered in millimetres, mm. */
    static final double DEFAULT_LENGTH_MM = 200.0;

    /** The tail marker length offered in millimetres, mm. */
    static final double DEFAULT_TAIL_MM = 50.0;

    /** The train length offered in inches. */
    static final double DEFAULT_LENGTH_INCHES = 8.0;

    /** The tail marker length offered in inches. */
    static final double DEFAULT_TAIL_INCHES = 2.0;

    /** The highest DCC address. */
    static final int MAX_ADDRESS = 10239;

    /** How the train is detected. */
    enum DetectionChoice {
        ENTIRE_TRAIN, HEAD_ONLY, HEAD_AND_TAIL
    }

    private final RosterEntry entry;
    private final String name;
    private final Block block;
    private final List<Block> neighbours;
    private final boolean measuredSpeeds;

    /**
     * @param entry      the marker's roster entry, or null
     * @param markerText the marker's text, used as the train's name when it
     *                   has no roster entry
     * @param block      the Block the marker was dropped on
     */
    PlaceTrainModel(@CheckForNull RosterEntry entry, @CheckForNull String markerText, @Nonnull Block block) {
        this.entry = entry;
        this.block = block;
        if (entry != null) {
            name = entry.getId();
            measuredSpeeds = SimulatedLoco.builder(entry).build().hasMeasuredSpeeds();
        } else {
            name = markerText == null ? "" : markerText.trim();
            measuredSpeeds = false;
        }
        List<Block> found = new ArrayList<>();
        for (Path p : block.getPaths()) {
            Block b = p.getBlock();
            if (b != null && b != block && !found.contains(b)) {
                found.add(b);
            }
        }
        neighbours = Collections.unmodifiableList(found);
    }

    /**
     * @return the train's name: the roster entry's ID, or the marker's text;
     *         empty if the marker has neither
     */
    @Nonnull
    String getName() {
        return name;
    }

    /**
     * @return true if the marker has a roster entry, which gives the address
     */
    boolean hasRosterEntry() {
        return entry != null;
    }

    /**
     * @return the Block the marker was dropped on
     */
    @Nonnull
    Block getBlock() {
        return block;
    }

    /**
     * @return the Blocks next to the Block, any of which the train's rear can
     *         be towards
     */
    @Nonnull
    List<Block> getNeighbours() {
        return neighbours;
    }

    /**
     * @return true if the loco has measured speeds; without them its speed is
     *         in proportion to the throttle
     */
    boolean hasMeasuredSpeeds() {
        return measuredSpeeds;
    }

    /**
     * @return the address to offer for a marker with no roster entry: the
     *         number its text starts with, if that is a DCC address;
     *         otherwise 0
     */
    int suggestedAddress() {
        int end = 0;
        while (end < name.length() && end < 6 && Character.isDigit(name.charAt(end))) {
            end++;
        }
        if (end == 0 || (end < name.length() && Character.isDigit(name.charAt(end)))) {
            return 0;
        }
        int n = Integer.parseInt(name.substring(0, end));
        return n >= 1 && n <= MAX_ADDRESS ? n : 0;
    }

    /**
     * @param trainLengthMm the train's length
     * @return how far into the Block to offer for the train's front: the
     *         train in the middle of the Block if it fits, otherwise its
     *         front at the far end
     */
    double suggestedFrontMm(double trainLengthMm) {
        double blockLength = block.getLengthMm();
        if (blockLength <= 0) {
            return 0.0;
        }
        if (trainLengthMm <= blockLength) {
            return Math.rint((blockLength + trainLengthMm) / 2.0);
        }
        return blockLength;
    }

    /**
     * @param name a train's name
     * @return the simulated train of that name on the layout, or null
     */
    @CheckForNull
    static VirtualTrain placedTrain(@Nonnull String name) {
        for (VirtualTrain t : SimulatedLayout.getDefault().getTrains()) {
            if (t.getName().equals(name)) {
                return t;
            }
        }
        return null;
    }

    /**
     * @param block a Block
     * @return the simulated trains with any part in the Block
     */
    @Nonnull
    static List<VirtualTrain> trainsIn(@Nonnull Block block) {
        List<VirtualTrain> in = new ArrayList<>();
        for (SimulationTruth.TrainTruth t : SimulatedLayout.getDefault().getTruth().getTrains()) {
            if (t.getBlocks().contains(block)) {
                in.add(t.getTrain());
            }
        }
        return in;
    }

    /**
     * @param train a placed train
     * @return the Block its front is in, or null if it is not placed
     */
    @CheckForNull
    static Block frontBlock(@Nonnull VirtualTrain train) {
        SimulationTruth.TrainTruth t = SimulatedLayout.getDefault().getTruth().getTrain(train);
        return t == null ? null : t.getFrontBlock();
    }

    /**
     * Describe the train.
     *
     * @param address      the DCC address, used when the marker has no roster
     *                     entry
     * @param lengthMm     the train's length
     * @param detection    how it is detected
     * @param locoLengthMm the loco's length, used unless the entire train is
     *                     detected
     * @param tailMm       the tail marker's length, used for head and tail
     * @return the train
     * @throws IllegalArgumentException with a message for the user if the
     *                                  train can't be described so
     */
    @Nonnull
    VirtualTrain build(int address, double lengthMm, @Nonnull DetectionChoice detection, double locoLengthMm,
            double tailMm) {
        SimulatedLoco.Builder loco;
        if (entry != null) {
            loco = SimulatedLoco.builder(entry);
        } else {
            if (address < 1 || address > MAX_ADDRESS) {
                throw new IllegalArgumentException(Bundle.getMessage("PlaceAddressNeeded", Integer.toString(MAX_ADDRESS)));
            }
            loco = SimulatedLoco.builder(new DccLocoAddress(address, address > 127));
        }
        String trainName = name.isEmpty() && entry == null ? Integer.toString(address) : name;
        if (trainName.isEmpty()) {
            throw new IllegalArgumentException(Bundle.getMessage("PlaceNameNeeded"));
        }
        Detection d;
        switch (detection) {
            case HEAD_ONLY:
                loco.length(locoLengthMm);
                d = Detection.headOnly();
                break;
            case HEAD_AND_TAIL:
                loco.length(locoLengthMm);
                d = Detection.headAndTail(tailMm);
                break;
            default:
                d = Detection.entireTrain();
                break;
        }
        return VirtualTrain.builder(trainName).loco(loco.build()).length(lengthMm).detection(d).build();
    }

    /**
     * Place the train with its front in the Block. A simulated train of the
     * same name already on the layout is taken off first, so the train is
     * moved.
     *
     * @param train   the train, from {@link #build}
     * @param from    the neighbouring Block its rear is towards, or null for
     *                the end of the track
     * @param frontMm how far its front is into the Block
     * @throws IllegalStateException    if the profile is not fully simulated
     * @throws IllegalArgumentException with a message for the user if the
     *                                  train can't be placed there
     */
    void place(@Nonnull VirtualTrain train, @CheckForNull Block from, double frontMm) {
        SimulatedLayout layout = SimulatedLayout.getDefault();
        VirtualTrain old = placedTrain(train.getName());
        if (old != null) {
            layout.remove(old);
        }
        try {
            layout.place(train, block, from, frontMm);
        } catch (IllegalArgumentException | IllegalStateException e) {
            if (old != null) {
                throw new IllegalArgumentException(
                        Bundle.getMessage("PlaceMovedTrainRemoved", train.getName(), e.getMessage()), e);
            }
            throw e;
        }
    }
}

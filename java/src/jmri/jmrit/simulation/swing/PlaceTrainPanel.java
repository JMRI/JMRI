package jmri.jmrit.simulation.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Set;
import java.util.StringJoiner;

import javax.annotation.Nonnull;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import jmri.Block;
import jmri.InvokeOnGuiThread;
import jmri.Sensor;
import jmri.jmrit.simulation.SimulatedLayout;
import jmri.jmrit.simulation.VirtualTrain;

/**
 * What is asked when a simulated train is placed from a loco marker, filled in
 * with what is known so that the usual answer is OK.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
final class PlaceTrainPanel extends JPanel {

    private final transient PlaceTrainModel model;

    private final JSpinner address = new JSpinner(new SpinnerNumberModel(3, 1, PlaceTrainModel.MAX_ADDRESS, 1));
    private final transient LengthUnits units;
    private final JSpinner length;
    private final JComboBox<String> detection = new JComboBox<>(new String[]{
        Bundle.getMessage("PlaceDetectionEntireTrain"),
        Bundle.getMessage("PlaceDetectionHeadOnly"),
        Bundle.getMessage("PlaceDetectionHeadAndTail")});
    private final JSpinner locoLength;
    private final JSpinner tailLength;
    private final JComboBox<String> rearTowards = new JComboBox<>();
    private final JSpinner front;
    private final JCheckBox tellTrainTracking = new JCheckBox(Bundle.getMessage("PlaceTellTrainTracking"), true);

    /** True until the user changes the front's position: it then follows the train's length. */
    private boolean frontFollowsLength = true;
    private boolean settingFront = false;

    private int row = 0;

    /** The longest length the window takes, mm: 100 m. */
    private static final double LONGEST_MM = 100000.0;

    /**
     * Lengths in the units chosen for Block lengths.
     *
     * @param model what is known of the train and the Block
     */
    @InvokeOnGuiThread
    PlaceTrainPanel(@Nonnull PlaceTrainModel model) {
        this(model, LengthUnits.fromPreferences());
    }

    /**
     * @param model what is known of the train and the Block
     * @param units the units lengths are shown and entered in
     */
    @InvokeOnGuiThread
    PlaceTrainPanel(@Nonnull PlaceTrainModel model, @Nonnull LengthUnits units) {
        super(new GridBagLayout());
        this.model = model;
        this.units = units;
        // round lengths in the units shown: 200 mm and 50 mm, or 8 in and 2 in
        double offered = units.isMetric() ? PlaceTrainModel.DEFAULT_LENGTH_MM
                : units.toMm(PlaceTrainModel.DEFAULT_LENGTH_INCHES);
        double tail = units.isMetric() ? PlaceTrainModel.DEFAULT_TAIL_MM
                : units.toMm(PlaceTrainModel.DEFAULT_TAIL_INCHES);
        length = units.spinner(offered, 1.0, LONGEST_MM);
        locoLength = units.spinner(offered, 1.0, LONGEST_MM);
        tailLength = units.spinner(tail, 1.0, LONGEST_MM);
        front = units.spinner(0.0, 0.0, LONGEST_MM);

        // an address is a number to read as written, not a quantity: no grouping
        address.setEditor(new JSpinner.NumberEditor(address, "#"));
        address.setName("PlaceAddress"); // NOI18N
        length.setName("PlaceLength"); // NOI18N
        detection.setName("PlaceDetection"); // NOI18N
        locoLength.setName("PlaceLocoLength"); // NOI18N
        tailLength.setName("PlaceTailLength"); // NOI18N
        rearTowards.setName("PlaceRearTowards"); // NOI18N
        front.setName("PlaceFront"); // NOI18N
        tellTrainTracking.setName("PlaceTellTrainTracking"); // NOI18N

        addRow(Bundle.getMessage("PlaceTrain"), new JLabel(model.getName()));
        addRow(Bundle.getMessage("PlaceBlock"), new JLabel(model.getBlock().getDisplayName()));
        if (!model.hasRosterEntry()) {
            int suggested = model.suggestedAddress();
            if (suggested > 0) {
                address.setValue(suggested);
            }
            addRow(Bundle.getMessage("PlaceAddress"), address);
        }
        addRow(Bundle.getMessage("PlaceLength", units.getName()), length);
        addRow(Bundle.getMessage("PlaceDetection"), detection);
        addRow(Bundle.getMessage("PlaceLocoLength", units.getName()), locoLength);
        addRow(Bundle.getMessage("PlaceTailLength", units.getName()), tailLength);
        for (Block b : model.getNeighbours()) {
            rearTowards.addItem(b.getDisplayName());
        }
        rearTowards.addItem(Bundle.getMessage("PlaceEndOfTrack"));
        addRow(Bundle.getMessage("PlaceRearTowards"), rearTowards);
        addRow(Bundle.getMessage("PlaceFront", units.getName()), front);
        addWide(tellTrainTracking);

        VirtualTrain placed = model.getName().isEmpty() ? null : PlaceTrainModel.placedTrain(model.getName());
        if (placed != null) {
            Block where = PlaceTrainModel.frontBlock(placed);
            addWide(new JLabel(Bundle.getMessage("PlaceAlreadyPlaced", placed.getName(),
                    where == null ? "" : where.getDisplayName())));
        }
        if (!model.hasMeasuredSpeeds()) {
            addWide(new JLabel(Bundle.getMessage("PlaceSpeedsApproximate")));
        }
        Set<Sensor> unreported = SimulatedLayout.getDefault().getSensorsNotReported();
        if (!unreported.isEmpty()) {
            StringJoiner names = new StringJoiner(", ");
            unreported.stream().map(Sensor::getDisplayName).sorted().forEach(names::add);
            addWide(new JLabel(Bundle.getMessage("PlaceSensorsNotReported", names.toString())));
        }

        detection.addActionListener(e -> enableLengths());
        length.addChangeListener(e -> {
            if (frontFollowsLength) {
                suggestFront();
            }
        });
        front.addChangeListener(e -> {
            if (!settingFront) {
                frontFollowsLength = false;
            }
        });
        enableLengths();
        suggestFront();
    }

    private void addRow(String labelText, JComponent field) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = row++;
        c.insets = new Insets(2, 4, 2, 4);
        c.anchor = GridBagConstraints.LINE_END;
        JLabel label = new JLabel(labelText);
        label.setLabelFor(field);
        add(label, c);
        c.gridx = 1;
        c.anchor = GridBagConstraints.LINE_START;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        add(field, c);
    }

    private void addWide(JComponent component) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = row++;
        c.gridwidth = 2;
        c.insets = new Insets(2, 4, 2, 4);
        c.anchor = GridBagConstraints.LINE_START;
        add(component, c);
    }

    private PlaceTrainModel.DetectionChoice detectionChoice() {
        return PlaceTrainModel.DetectionChoice.values()[Math.max(0, detection.getSelectedIndex())];
    }

    private void enableLengths() {
        PlaceTrainModel.DetectionChoice d = detectionChoice();
        locoLength.setEnabled(d != PlaceTrainModel.DetectionChoice.ENTIRE_TRAIN);
        tailLength.setEnabled(d == PlaceTrainModel.DetectionChoice.HEAD_AND_TAIL);
    }

    private void suggestFront() {
        settingFront = true;
        try {
            front.setValue(units.fromMm(model.suggestedFrontMm(mm(length))));
        } finally {
            settingFront = false;
        }
    }

    /**
     * Place the train as the panel now describes it.
     *
     * @throws IllegalStateException    if the profile is not fully simulated
     * @throws IllegalArgumentException with a message for the user if the
     *                                  train can't be placed so
     */
    @InvokeOnGuiThread
    void place() {
        VirtualTrain train = model.build(((Number) address.getValue()).intValue(), mm(length), detectionChoice(),
                mm(locoLength), mm(tailLength));
        int towards = rearTowards.getSelectedIndex();
        Block from = towards >= 0 && towards < model.getNeighbours().size() ? model.getNeighbours().get(towards) : null;
        model.place(train, from, mm(front));
    }

    /**
     * @param spinner a length spinner
     * @return its length in millimetres
     */
    private double mm(JSpinner spinner) {
        return units.toMm(((Number) spinner.getValue()).doubleValue());
    }

    /**
     * @return true if train tracking is to be told the train is in the Block
     */
    @InvokeOnGuiThread
    boolean isTellTrainTracking() {
        return tellTrainTracking.isSelected();
    }
}

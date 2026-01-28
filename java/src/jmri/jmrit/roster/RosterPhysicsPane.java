package jmri.jmrit.roster;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.MissingResourceException;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JTextArea;
import javax.swing.UIManager;

/**
 * Display and enable editing of Physics parameters for a {@link RosterEntry}.
 * This is intended to be hosted as its own tab in the programmer window.
 */
public class RosterPhysicsPane extends JPanel {

    private static final long serialVersionUID = 1L;

    private RosterEntry re;

    // --- Physics (locomotive-level) controls ---
    private final JRadioButton physicsSteamRadio = new JRadioButton(Bundle.getMessage("PhysicsSteam"));
    private final JRadioButton physicsDieselElectricRadio =
            new JRadioButton(Bundle.getMessage("PhysicsDieselElectric"));
    private final JCheckBox physicsMechanicalTransmissionCheck =
            new JCheckBox(Bundle.getMessage("PhysicsMechanicalTransmission"));
    private final ButtonGroup physicsTractionGroup = new ButtonGroup();

    // Unit combos per field (display units; storage stays metric in RosterEntry)
    private final JComboBox<String> physicsWeightUnitCombo = new JComboBox<>();
    private final JComboBox<String> physicsPowerUnitCombo = new JComboBox<>();
    private final JComboBox<String> physicsTeUnitCombo = new JComboBox<>();
    private final JComboBox<String> physicsSpeedUnitCombo = new JComboBox<>();

    // Editors (spinners)
    private final JSpinner physicsWeightSpinner = new JSpinner();
    private final JSpinner physicsPowerSpinner = new JSpinner();
    private final JSpinner physicsTractiveEffortSpinner = new JSpinner();
    private final JSpinner physicsMaxSpeedSpinner = new JSpinner();

    private boolean refreshing = false;

    /**
     * Return the localized title to use when this pane is added as a tab. This
     * method exists so callers in other packages don't need direct access to
     * this package's Bundle helper.
     *
     * @return localized tab title
     */
    public static String getTabTitle() {
        return Bundle.getMessage("PhysicsTabTitle");
    }

    private static String getPhysicsPaneExplanation() {
        try {
            return Bundle.getMessage("PhysicsPaneExplanation");
        } catch (MissingResourceException ex) {
            // Fallback to avoid hard failure if the resource key is not present.
            return "These controls are primarily used for physics-based acceleration. They affect how JMRI calculates throttle changes over time when accelerating.";
        }
    }

    public RosterPhysicsPane(RosterEntry r) {
        super();
        re = r;

        initGui();
        wireListeners();

        // Initialize display from current roster entry (metric storage -> chosen units)
        refreshFromRosterEntry();
    }

    private void initGui() {
        GridBagLayout gbLayout = new GridBagLayout();
        GridBagConstraints cL = new GridBagConstraints();
        GridBagConstraints cR = new GridBagConstraints();

        setLayout(gbLayout);

        // Explanation text for the Physics pane
        JTextArea explanation = new JTextArea(getPhysicsPaneExplanation());
        explanation.setEditable(false);
        explanation.setLineWrap(true);
        explanation.setWrapStyleWord(true);
        explanation.setOpaque(false);
        explanation.setFocusable(false);
        explanation.setFont(UIManager.getFont("Label.font"));
        explanation.setForeground(UIManager.getColor("Label.foreground"));
        GridBagConstraints cE = new GridBagConstraints();
        cE.gridx = 0;
        cE.gridy = 0;
        cE.gridwidth = 2;
        cE.anchor = GridBagConstraints.WEST;
        cE.fill = GridBagConstraints.HORIZONTAL;
        cE.weightx = 1.0;
        cE.insets = new Insets(0, 10, 20, 10);
        gbLayout.setConstraints(explanation, cE);
        add(explanation);


        cL.gridx = 0;
        cL.gridy = 1;
        cL.ipadx = 3;
        cL.anchor = GridBagConstraints.NORTHWEST;
        cL.insets = new Insets(0, 10, 0, 15);

        cR.gridx = 1;
        cR.gridy = 1;
        cR.anchor = GridBagConstraints.WEST;
        cR.insets = new Insets(0, 0, 0, 10);

        // Traction type (Steam / Diesel/Electric)
        JLabel physicsTractionLabel = new JLabel(Bundle.getMessage("PhysicsTractionType") + ":");
        gbLayout.setConstraints(physicsTractionLabel, cL);
        add(physicsTractionLabel);

        JPanel tractionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        physicsTractionGroup.add(physicsSteamRadio);
        physicsTractionGroup.add(physicsDieselElectricRadio);
        tractionRow.add(physicsSteamRadio);
        tractionRow.add(physicsDieselElectricRadio);

        physicsSteamRadio.getAccessibleContext().setAccessibleName(Bundle.getMessage("PhysicsSteam"));
        physicsDieselElectricRadio.getAccessibleContext().setAccessibleName(Bundle.getMessage("PhysicsDieselElectric"));

        gbLayout.setConstraints(tractionRow, cR);
        add(tractionRow);

        // Transmission (mechanical DMU option)
        cL.gridy++;
        cR.gridy = cL.gridy;

        JLabel physicsTransLabel = new JLabel(Bundle.getMessage("PhysicsTransmission") + ":");
        gbLayout.setConstraints(physicsTransLabel, cL);
        add(physicsTransLabel);

        gbLayout.setConstraints(physicsMechanicalTransmissionCheck, cR);
        add(physicsMechanicalTransmissionCheck);

        physicsMechanicalTransmissionCheck.getAccessibleContext()
                .setAccessibleName(Bundle.getMessage("PhysicsMechanicalTransmission"));
        physicsMechanicalTransmissionCheck.setToolTipText(Bundle.getMessage("ToolTipPhysicsMechanicalTransmission"));

        // Locomotive weight
        cL.gridy++;
        cR.gridy = cL.gridy;

        JLabel physicsWeightLabel = new JLabel(Bundle.getMessage("PhysicsWeight") + ":");
        gbLayout.setConstraints(physicsWeightLabel, cL);
        add(physicsWeightLabel);

        JPanel weightRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        physicsWeightSpinner.setModel(new SpinnerNumberModel(0.0d, 0.0d, 1_000_000.0d, 0.001d));
        physicsWeightSpinner.setEditor(new JSpinner.NumberEditor(physicsWeightSpinner, "0.000"));
        physicsWeightUnitCombo.addItem(Bundle.getMessage("PhysicsWeightUnitTonne"));
        physicsWeightUnitCombo.addItem(Bundle.getMessage("PhysicsWeightUnitLongTon"));
        physicsWeightUnitCombo.addItem(Bundle.getMessage("PhysicsWeightUnitShortTon"));
        weightRow.add(physicsWeightSpinner);
        weightRow.add(physicsWeightUnitCombo);

        physicsWeightSpinner.getAccessibleContext().setAccessibleName(Bundle.getMessage("PhysicsWeightValue"));
        physicsWeightUnitCombo.getAccessibleContext().setAccessibleName(Bundle.getMessage("PhysicsWeightUnits"));

        gbLayout.setConstraints(weightRow, cR);
        add(weightRow);

        // Continuous power
        cL.gridy++;
        cR.gridy = cL.gridy;

        JLabel physicsPowerLabel = new JLabel(Bundle.getMessage("PhysicsPower") + ":");
        gbLayout.setConstraints(physicsPowerLabel, cL);
        add(physicsPowerLabel);

        JPanel powerRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        physicsPowerSpinner.setModel(new SpinnerNumberModel(0.0d, 0.0d, 1_000_000.0d, 0.1d));
        physicsPowerSpinner.setEditor(new JSpinner.NumberEditor(physicsPowerSpinner, "0.000"));
        physicsPowerUnitCombo.addItem(Bundle.getMessage("PhysicsPowerUnitKW"));
        physicsPowerUnitCombo.addItem(Bundle.getMessage("PhysicsPowerUnitHP"));
        powerRow.add(physicsPowerSpinner);
        powerRow.add(physicsPowerUnitCombo);

        physicsPowerSpinner.getAccessibleContext().setAccessibleName(Bundle.getMessage("PhysicsPowerValue"));
        physicsPowerUnitCombo.getAccessibleContext().setAccessibleName(Bundle.getMessage("PhysicsPowerUnits"));

        gbLayout.setConstraints(powerRow, cR);
        add(powerRow);

        // Tractive effort
        cL.gridy++;
        cR.gridy = cL.gridy;

        JLabel physicsTeLabel = new JLabel(Bundle.getMessage("PhysicsTractiveEffort") + ":");
        gbLayout.setConstraints(physicsTeLabel, cL);
        add(physicsTeLabel);

        JPanel teRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        physicsTractiveEffortSpinner.setModel(new SpinnerNumberModel(0.0d, 0.0d, 1_000_000.0d, 0.001d));
        physicsTractiveEffortSpinner.setEditor(new JSpinner.NumberEditor(physicsTractiveEffortSpinner, "0.000"));
        physicsTeUnitCombo.addItem(Bundle.getMessage("PhysicsTeUnitKN"));
        physicsTeUnitCombo.addItem(Bundle.getMessage("PhysicsTeUnitLbf"));
        teRow.add(physicsTractiveEffortSpinner);
        teRow.add(physicsTeUnitCombo);

        physicsTractiveEffortSpinner.getAccessibleContext()
                .setAccessibleName(Bundle.getMessage("PhysicsTractiveEffortValue"));
        physicsTeUnitCombo.getAccessibleContext().setAccessibleName(Bundle.getMessage("PhysicsTractiveEffortUnits"));

        gbLayout.setConstraints(teRow, cR);
        add(teRow);

        // Maximum speed
        cL.gridy++;
        cR.gridy = cL.gridy;

        JLabel physicsSpeedLabel = new JLabel(Bundle.getMessage("PhysicsMaxSpeed") + ":");
        gbLayout.setConstraints(physicsSpeedLabel, cL);
        add(physicsSpeedLabel);

        JPanel speedRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        physicsMaxSpeedSpinner.setModel(new SpinnerNumberModel(0.0d, 0.0d, 1_000.0d, 0.1d));
        physicsMaxSpeedSpinner.setEditor(new JSpinner.NumberEditor(physicsMaxSpeedSpinner, "0.000"));
        physicsSpeedUnitCombo.addItem(Bundle.getMessage("PhysicsSpeedUnitKmh"));
        physicsSpeedUnitCombo.addItem(Bundle.getMessage("PhysicsSpeedUnitMph"));
        speedRow.add(physicsMaxSpeedSpinner);
        speedRow.add(physicsSpeedUnitCombo);

        physicsMaxSpeedSpinner.getAccessibleContext().setAccessibleName(Bundle.getMessage("PhysicsMaxSpeedValue"));
        physicsSpeedUnitCombo.getAccessibleContext().setAccessibleName(Bundle.getMessage("PhysicsSpeedUnits"));

        gbLayout.setConstraints(speedRow, cR);
        add(speedRow);

        // Profile tools row: single button to open JMRI Speed Profiling UI
        cL.gridy++;
        cR.gridy = cL.gridy;

        JLabel profileToolsLabel = new JLabel(Bundle.getMessage("PhysicsProfileTools") + ":");
        gbLayout.setConstraints(profileToolsLabel, cL);
        add(profileToolsLabel);

        JButton physicsSpeedProfileButton = new JButton(Bundle.getMessage("PhysicsSpeedProfileButton"));
        gbLayout.setConstraints(physicsSpeedProfileButton, cR);
        add(physicsSpeedProfileButton);

        physicsSpeedProfileButton.addActionListener(ev -> {
            jmri.util.swing.WindowInterface wi = getWindowInterfaceOrNull();
            jmri.jmrit.roster.swing.speedprofile.SpeedProfileAction act =
                    (wi != null)
                            ? new jmri.jmrit.roster.swing.speedprofile.SpeedProfileAction(
                                    Bundle.getMessage("PhysicsSpeedProfilingTitle"), wi)
                            : new jmri.jmrit.roster.swing.speedprofile.SpeedProfileAction(
                                    Bundle.getMessage("PhysicsSpeedProfilingTitle"));
            act.actionPerformed(new java.awt.event.ActionEvent(this,
                    java.awt.event.ActionEvent.ACTION_PERFORMED, "open"));
        });
    }

    private void wireListeners() {
        physicsSteamRadio.addActionListener(ev -> {
            if (refreshing)
                return;
            if (re != null)
                re.setPhysicsTractionType(RosterEntry.TractionType.STEAM);
        });
        physicsDieselElectricRadio.addActionListener(ev -> {
            if (refreshing)
                return;
            if (re != null)
                re.setPhysicsTractionType(RosterEntry.TractionType.DIESEL_ELECTRIC);
        });
        physicsMechanicalTransmissionCheck.addActionListener(ev -> {
            if (refreshing)
                return;
            if (re != null)
                re.setPhysicsMechanicalTransmission(physicsMechanicalTransmissionCheck.isSelected());
        });

        // Unit combo changes -> refresh display from stored metric
        physicsWeightUnitCombo.addActionListener(ev -> refreshFromRosterEntry());
        physicsPowerUnitCombo.addActionListener(ev -> refreshFromRosterEntry());
        physicsTeUnitCombo.addActionListener(ev -> refreshFromRosterEntry());
        physicsSpeedUnitCombo.addActionListener(ev -> refreshFromRosterEntry());

        // Spinner changes -> update stored metric in RosterEntry
        physicsWeightSpinner.addChangeListener(ev -> {
            if (refreshing)
                return;
            if (re != null)
                re.setPhysicsWeightKg(displayWeightToKg(((Number) physicsWeightSpinner.getValue()).floatValue()));
        });
        physicsPowerSpinner.addChangeListener(ev -> {
            if (refreshing)
                return;
            if (re != null)
                re.setPhysicsPowerKw(displayPowerToKw(((Number) physicsPowerSpinner.getValue()).floatValue()));
        });
        physicsTractiveEffortSpinner.addChangeListener(ev -> {
            if (refreshing)
                return;
            if (re != null)
                re.setPhysicsTractiveEffortKn(
                        displayTeToKn(((Number) physicsTractiveEffortSpinner.getValue()).floatValue()));
        });
        physicsMaxSpeedSpinner.addChangeListener(ev -> {
            if (refreshing)
                return;
            if (re != null)
                re.setPhysicsMaxSpeedKmh(displaySpeedToKmh(((Number) physicsMaxSpeedSpinner.getValue()).floatValue()));
        });
    }

    /**
     * Update the roster entry from the GUI contents.
     *
     * @param r roster entry to update
     */
    public void update(RosterEntry r) {
        if (r == null)
            return;
        RosterEntry.TractionType t = physicsSteamRadio.isSelected()
                ? RosterEntry.TractionType.STEAM
                : RosterEntry.TractionType.DIESEL_ELECTRIC;
        r.setPhysicsTractionType(t);
        r.setPhysicsWeightKg(displayWeightToKg(((Number) physicsWeightSpinner.getValue()).floatValue()));
        r.setPhysicsPowerKw(displayPowerToKw(((Number) physicsPowerSpinner.getValue()).floatValue()));
        r.setPhysicsTractiveEffortKn(displayTeToKn(((Number) physicsTractiveEffortSpinner.getValue()).floatValue()));
        r.setPhysicsMaxSpeedKmh(displaySpeedToKmh(((Number) physicsMaxSpeedSpinner.getValue()).floatValue()));
        r.setPhysicsMechanicalTransmission(physicsMechanicalTransmissionCheck.isSelected());
    }

    /**
     * Fill GUI from roster contents.
     *
     * @param r roster entry to display
     */
    public void updateGUI(RosterEntry r) {
        re = r;
        refreshFromRosterEntry();
    }

    /**
     * Compare GUI with roster contents.
     *
     * @param r roster entry to compare
     * @return true if GUI differs from r
     */
    public boolean guiChanged(RosterEntry r) {
        if (r == null)
            return false;

        RosterEntry.TractionType t = physicsSteamRadio.isSelected()
                ? RosterEntry.TractionType.STEAM
                : RosterEntry.TractionType.DIESEL_ELECTRIC;
        if (r.getPhysicsTractionType() != t)
            return true;

        if (r.isPhysicsMechanicalTransmission() != physicsMechanicalTransmissionCheck.isSelected())
            return true;

        // use a small epsilon to avoid noise from float/double conversions
        final float eps = 0.0005f;

        float wKg = displayWeightToKg(((Number) physicsWeightSpinner.getValue()).floatValue());
        if (Math.abs(r.getPhysicsWeightKg() - wKg) > eps)
            return true;

        float pKw = displayPowerToKw(((Number) physicsPowerSpinner.getValue()).floatValue());
        if (Math.abs(r.getPhysicsPowerKw() - pKw) > eps)
            return true;

        float teKn = displayTeToKn(((Number) physicsTractiveEffortSpinner.getValue()).floatValue());
        if (Math.abs(r.getPhysicsTractiveEffortKn() - teKn) > eps)
            return true;

        float vKmh = displaySpeedToKmh(((Number) physicsMaxSpeedSpinner.getValue()).floatValue());
        if (Math.abs(r.getPhysicsMaxSpeedKmh() - vKmh) > eps)
            return true;

        return false;
    }

    // --- Helpers: convert between metric storage (RosterEntry) and display units ---

    private void refreshFromRosterEntry() {
        if (re == null)
            return;

        refreshing = true;
        try {
            // Traction type
            if (re.getPhysicsTractionType() == RosterEntry.TractionType.STEAM) {
                physicsSteamRadio.setSelected(true);
            } else {
                physicsDieselElectricRadio.setSelected(true);
            }
            physicsMechanicalTransmissionCheck.setSelected(re.isPhysicsMechanicalTransmission());

            // Weight kg -> display
            physicsWeightSpinner.setValue(Double.valueOf(kgToDisplayWeight(re.getPhysicsWeightKg())));

            // Power kW -> display
            physicsPowerSpinner.setValue(Double.valueOf(kwToDisplayPower(re.getPhysicsPowerKw())));

            // Tractive effort kN -> display
            physicsTractiveEffortSpinner.setValue(Double.valueOf(knToDisplayTe(re.getPhysicsTractiveEffortKn())));

            // Max speed km/h -> display
            physicsMaxSpeedSpinner.setValue(Double.valueOf(kmhToDisplaySpeed(re.getPhysicsMaxSpeedKmh())));
        } finally {
            refreshing = false;
        }
    }

    private float displayWeightToKg(float displayVal) {
        switch (physicsWeightUnitCombo.getSelectedIndex()) {
            case 0:
                return displayVal * 1000.0f; // t -> kg
            case 1:
                return displayVal * 1016.0469f; // long ton -> kg
            case 2:
                return displayVal * 907.18474f; // short ton -> kg
            default:
                return displayVal;
        }
    }

    private float kgToDisplayWeight(float kg) {
        switch (physicsWeightUnitCombo.getSelectedIndex()) {
            case 0:
                return kg / 1000.0f; // t
            case 1:
                return kg / 1016.0469f; // long ton
            case 2:
                return kg / 907.18474f; // short ton
            default:
                return kg;
        }
    }

    private float displayPowerToKw(float displayVal) {
        switch (physicsPowerUnitCombo.getSelectedIndex()) {
            case 0:
                return displayVal; // kW
            case 1:
                return displayVal * 0.7456999f; // hp -> kW
            default:
                return displayVal;
        }
    }

    private float displayTeToKn(float displayVal) {
        switch (physicsTeUnitCombo.getSelectedIndex()) {
            case 0:
                return displayVal; // kN
            case 1:
                return displayVal / 224.80894f; // lbf -> kN
            default:
                return displayVal;
        }
    }

    private float knToDisplayTe(float kn) {
        switch (physicsTeUnitCombo.getSelectedIndex()) {
            case 0:
                return kn; // kN
            case 1:
                return kn * 224.80894f; // kN -> lbf
            default:
                return kn;
        }
    }

    private float displaySpeedToKmh(float displayVal) {
        switch (physicsSpeedUnitCombo.getSelectedIndex()) {
            case 0:
                return displayVal; // km/h
            case 1:
                return displayVal * 1.609344f; // mph -> km/h
            default:
                return displayVal;
        }
    }

    private float kmhToDisplaySpeed(float kmh) {
        switch (physicsSpeedUnitCombo.getSelectedIndex()) {
            case 0:
                return kmh; // km/h
            case 1:
                return kmh / 1.609344f; // km/h -> mph
            default:
                return kmh;
        }
    }

    private float kwToDisplayPower(float kw) {
        switch (physicsPowerUnitCombo.getSelectedIndex()) {
            case 0:
                return kw; // kW
            case 1:
                return kw / 0.7456999f; // kW -> hp
            default:
                return kw;
        }
    }

    private jmri.util.swing.WindowInterface getWindowInterfaceOrNull() {
        java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(this);
        return (w instanceof jmri.util.swing.WindowInterface)
                ? (jmri.util.swing.WindowInterface) w
                : null;
    }
}

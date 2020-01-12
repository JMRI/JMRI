package jmri.jmrit.blockboss;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.annotation.Nonnull;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.SignalHead;
import jmri.SignalHeadManager;
import jmri.Turnout;
import jmri.NamedBean.DisplayOptions;
import jmri.swing.NamedBeanComboBox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide a GUI for configuring "Simple Signal Logic" (BlockBossLogic) objects.
 * <p>
 * Provides four panels, corresponding to the four possible modes described in
 * {@link BlockBossLogic}, which are then selected via radio buttons in the GUI.
 * <p>
 * The four modes are:
 * <ul>
 * <li>Single block (s)
 * <li>Facing point (f)
 * <li>Trailing point main (tm)
 * <li>Trailing point diverging (td)
 * </ul>
 * <p>
 * The multiple-panel approach to the GUI is used to make layout easier; the
 * code just flips from one to the other as the user selects a mode. The
 * individual items all share data models to simplify the logic.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2005
 * @author Dick Bronson 2006: Revisions to add facing point sensors,
 * approach lighting, limited speed, changed layout, and tool tips.
 * @author Egbert Broerse 2017
 */
public class BlockBossFrame extends jmri.util.JmriJFrame {

    JPanel modeSingle = new JPanel();
    JRadioButton buttonSingle;
    private transient NamedBeanComboBox<Sensor> sSensorComboBox1 = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SensorManager.class), null, DisplayOptions.DISPLAYNAME);
    private transient NamedBeanComboBox<Sensor> sSensorComboBox2 = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SensorManager.class), null, DisplayOptions.DISPLAYNAME);
    private transient NamedBeanComboBox<Sensor> sSensorComboBox3 = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SensorManager.class), null, DisplayOptions.DISPLAYNAME);
    private transient NamedBeanComboBox<Sensor> sSensorComboBox4 = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SensorManager.class), null, DisplayOptions.DISPLAYNAME);
    private transient NamedBeanComboBox<Sensor> sSensorComboBox5 = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SensorManager.class), null, DisplayOptions.DISPLAYNAME);
    private transient NamedBeanComboBox<SignalHead> sNextSignalComboBox1 = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SignalHeadManager.class), null, DisplayOptions.DISPLAYNAME);
    private transient NamedBeanComboBox<SignalHead> sNextSignalComboBox1Alt = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SignalHeadManager.class), null, DisplayOptions.DISPLAYNAME);
    JCheckBox sLimitBox;
    JCheckBox sRestrictingBox;
    JCheckBox sFlashBox;
    JCheckBox sDistantBox;

    JPanel modeTrailMain = new JPanel();
    JRadioButton buttonTrailMain;
    private transient NamedBeanComboBox<Sensor> tmSensorComboBox1 = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SensorManager.class), null, DisplayOptions.DISPLAYNAME);
    private transient NamedBeanComboBox<Sensor> tmSensorComboBox2 = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SensorManager.class), null, DisplayOptions.DISPLAYNAME);
    private transient NamedBeanComboBox<Sensor> tmSensorComboBox3 = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SensorManager.class), null, DisplayOptions.DISPLAYNAME);
    private transient NamedBeanComboBox<Sensor> tmSensorComboBox4 = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SensorManager.class), null, DisplayOptions.DISPLAYNAME);
    private transient NamedBeanComboBox<Sensor> tmSensorComboBox5 = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SensorManager.class), null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<Turnout> tmProtectTurnoutComboBox = new NamedBeanComboBox<>(
            InstanceManager.turnoutManagerInstance(),
            null, DisplayOptions.DISPLAYNAME);
    private transient NamedBeanComboBox<SignalHead> tmNextSignalComboBox1 = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SignalHeadManager.class), null, DisplayOptions.DISPLAYNAME);
    private transient NamedBeanComboBox<SignalHead> tmNextSignalComboBox1Alt = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SignalHeadManager.class), null, DisplayOptions.DISPLAYNAME);
    JCheckBox tmLimitBox;
    JCheckBox tmRestrictingBox;
    JCheckBox tmFlashBox;
    JCheckBox tmDistantBox;

    JPanel modeTrailDiv = new JPanel();
    JRadioButton buttonTrailDiv;
    private transient NamedBeanComboBox<Sensor> tdSensorComboBox1 = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SensorManager.class), null, DisplayOptions.DISPLAYNAME);
    private transient NamedBeanComboBox<Sensor> tdSensorComboBox2 = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SensorManager.class), null, DisplayOptions.DISPLAYNAME);
    private transient NamedBeanComboBox<Sensor> tdSensorComboBox3 = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SensorManager.class), null, DisplayOptions.DISPLAYNAME);
    private transient NamedBeanComboBox<Sensor> tdSensorComboBox4 = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SensorManager.class), null, DisplayOptions.DISPLAYNAME);
    private transient NamedBeanComboBox<Sensor> tdSensorComboBox5 = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SensorManager.class), null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<Turnout> tdProtectTurnoutComboBox = new NamedBeanComboBox<>(
            InstanceManager.turnoutManagerInstance(),
            null, DisplayOptions.DISPLAYNAME);
    private transient NamedBeanComboBox<SignalHead> tdNextSignalComboBox1 = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SignalHeadManager.class), null, DisplayOptions.DISPLAYNAME);
    private transient NamedBeanComboBox<SignalHead> tdNextSignalComboBox1Alt = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SignalHeadManager.class), null, DisplayOptions.DISPLAYNAME);
    JCheckBox tdLimitBox;
    JCheckBox tdRestrictingBox;
    JCheckBox tdFlashBox;
    JCheckBox tdDistantBox;

    JPanel modeFacing = new JPanel();
    JRadioButton buttonFacing;

    private transient NamedBeanComboBox<Sensor> fSensorComboBox1 = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SensorManager.class), null, DisplayOptions.DISPLAYNAME);
    private transient NamedBeanComboBox<Sensor> fSensorComboBox2 = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SensorManager.class), null, DisplayOptions.DISPLAYNAME);
    private transient NamedBeanComboBox<Sensor> fSensorComboBox3 = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SensorManager.class), null, DisplayOptions.DISPLAYNAME);
    private transient NamedBeanComboBox<Sensor> fSensorComboBox4 = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SensorManager.class), null, DisplayOptions.DISPLAYNAME);
    private transient NamedBeanComboBox<Sensor> fSensorComboBox5 = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SensorManager.class), null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<Turnout> fProtectTurnoutComboBox = new NamedBeanComboBox<>(
            InstanceManager.turnoutManagerInstance(),
            null, DisplayOptions.DISPLAYNAME);
    private transient NamedBeanComboBox<SignalHead> fNextSignalComboBox1 = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SignalHeadManager.class), null, DisplayOptions.DISPLAYNAME);
    private transient NamedBeanComboBox<SignalHead> fNextSignalComboBox1Alt = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SignalHeadManager.class), null, DisplayOptions.DISPLAYNAME);
    private transient NamedBeanComboBox<SignalHead> fNextSignalComboBox2 = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SignalHeadManager.class), null, DisplayOptions.DISPLAYNAME);
    private transient NamedBeanComboBox<SignalHead> fNextSignalComboBox2Alt = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SignalHeadManager.class), null, DisplayOptions.DISPLAYNAME);
    private transient NamedBeanComboBox<Sensor> fNextSensorComboBox1 = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SensorManager.class), null, DisplayOptions.DISPLAYNAME);
    private transient NamedBeanComboBox<Sensor> fNextSensorComboBox1Alt = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SensorManager.class), null, DisplayOptions.DISPLAYNAME);
    private transient NamedBeanComboBox<Sensor> fNextSensorComboBox2 = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SensorManager.class), null, DisplayOptions.DISPLAYNAME);
    private transient NamedBeanComboBox<Sensor> fNextSensorComboBox2Alt = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SensorManager.class), null, DisplayOptions.DISPLAYNAME);
    JCheckBox fmLimitBox;
    JCheckBox fmRestrictingBox;
    JCheckBox fdLimitBox;
    JCheckBox fdRestrictingBox;
    JCheckBox fFlashBox;
    JCheckBox fDistantBox;

    private transient NamedBeanComboBox<Sensor> approachSensor1ComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SensorManager.class), null, DisplayOptions.DISPLAYNAME);
    private transient NamedBeanComboBox<SignalHead> outSignalHeadComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SignalHeadManager.class), null, DisplayOptions.DISPLAYNAME);
    JLabel statusBar;
    JTextField commentField;
    JButton cancel;

    // ToolTip strings
    String buttonSingleTooltip = Bundle.getMessage("In_direction_of_traffic");
    String buttonTrailMainTooltip = Bundle.getMessage("Signal_head_for_main_track")
            + " " + Bundle.getMessage("through_turnout_in_either_direction");
    String buttonTrailDivTooltip = Bundle.getMessage("Signal_head_for_branching_track")
            + " " + Bundle.getMessage("through_turnout_in_either_direction");
    String buttonFacingTooltip = Bundle.getMessage("Single_signal_head_on_single");
    String outSignalHeadTooltip = "<html>"
            + Bundle.getMessage("Enter_a_new_signal_head_number_or")
            + "<br>" + Bundle.getMessage("then_hit_return_to_load_its_information.")
            + "</html>";
    String approachSensor1Tooltip = "<html>"
            + Bundle.getMessage("Enter_sensor_that_lights_this_signal_or")
            + "</html>";
    String sensorFieldTooltip = Bundle.getMessage("Sensor_active_sets_this_signal_to_Red.");
    String turnoutFieldTooltip = Bundle.getMessage("Enter_protected_turnout_number_here.");
    String flashBoxTooltip = Bundle.getMessage("One_aspect_faster_than_yellow_displays")
            + " " + Bundle.getMessage("flashing_yellow_rather_than_green");
    String limitBoxTooltip = Bundle.getMessage("Limits_the_fastest_aspect_displayed")
            + " " + Bundle.getMessage("to_yellow_rather_than_green");
    String restrictingBoxTooltip = Bundle.getMessage("Limits_the_fastest_aspect_displayed")
            + " " + Bundle.getMessage("to_flashingred_rather_than_green");
    String nextSignalFieldTooltip = Bundle.getMessage("Enter_the_low_speed_signal_head_for_this_track.")
            + " " + Bundle.getMessage("For_dual_head_signals_the_fastest_aspect_is_protected.");
    String highSignalFieldTooltip = Bundle.getMessage("Enter_the_high_speed_signal_head_for_this_track.")
            + " " + Bundle.getMessage("For_dual_head_signals_the_fastest_aspect_is_protected.");
    String distantBoxTooltip = Bundle.getMessage("Mirrors_the_protected_(following)_signals_status")
            + " " + Bundle.getMessage("unless_over_ridden_by_an_intermediate_stop_sensor.");

    /**
     * Ctor for default SSL edit frame.
     */
    public BlockBossFrame() {
        this(Bundle.getMessage("Simple_Signal_Logic"));
    }

    /**
     * Ctor for named SSL edit frame.
     *
     * @param frameName the name to use for this frame
     */
    public BlockBossFrame(String frameName) {

        // create the frame
        super(frameName, false, true);

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // add save menu item
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu(Bundle.getMessage("MenuFile"));
        menuBar.add(fileMenu);
        fileMenu.add(new jmri.configurexml.SaveMenu());
        setJMenuBar(menuBar);
        addHelpMenu("package.jmri.jmrit.blockboss.BlockBossFrame", true);

        // create GUI items
        sLimitBox = new JCheckBox(Bundle.getMessage("Limited_Speed"));
        sRestrictingBox = new JCheckBox(Bundle.getMessage("Restricting_Speed"));
        tmLimitBox = new JCheckBox(Bundle.getMessage("Limited_Speed"));
        tmLimitBox.setModel(sLimitBox.getModel());
        tmRestrictingBox = new JCheckBox(Bundle.getMessage("Restricting_Speed"));
        tmRestrictingBox.setModel(sRestrictingBox.getModel());
        fmLimitBox = new JCheckBox(Bundle.getMessage("Limited_Speed"));
        fmLimitBox.setModel(sLimitBox.getModel());
        fmRestrictingBox = new JCheckBox(Bundle.getMessage("Restricting_Speed"));
        fmRestrictingBox.setModel(sRestrictingBox.getModel());

        tdLimitBox = new JCheckBox(Bundle.getMessage("Limited_Speed"));
        tdRestrictingBox = new JCheckBox(Bundle.getMessage("Restricting_Speed"));
        fdLimitBox = new JCheckBox(Bundle.getMessage("Limited_Speed"));
        fdLimitBox.setModel(tdLimitBox.getModel());
        fdRestrictingBox = new JCheckBox(Bundle.getMessage("Restricting_Speed"));
        fdRestrictingBox.setModel(tdRestrictingBox.getModel());

        sFlashBox = new JCheckBox(Bundle.getMessage("With_Flashing_Yellow"));
        tmFlashBox = new JCheckBox(Bundle.getMessage("With_Flashing_Yellow"));
        tmFlashBox.setModel(sFlashBox.getModel());
        tdFlashBox = new JCheckBox(Bundle.getMessage("With_Flashing_Yellow"));
        tdFlashBox.setModel(sFlashBox.getModel());
        fFlashBox = new JCheckBox(Bundle.getMessage("With_Flashing_Yellow"));
        fFlashBox.setModel(sFlashBox.getModel());

        sDistantBox = new JCheckBox(Bundle.getMessage("Is_Distant_Signal"));
        tmDistantBox = new JCheckBox(Bundle.getMessage("Is_Distant_Signal"));
        tmDistantBox.setModel(sDistantBox.getModel());
        tdDistantBox = new JCheckBox(Bundle.getMessage("Is_Distant_Signal"));
        tdDistantBox.setModel(sDistantBox.getModel());
        fDistantBox = new JCheckBox(Bundle.getMessage("Is_Distant_Signal"));
        fDistantBox.setModel(sDistantBox.getModel());

        buttonSingle = new JRadioButton(Bundle.getMessage("On_Single_Block"));
        buttonTrailMain = new JRadioButton(Bundle.getMessage("Main_Leg_of_Turnout"));
        buttonTrailDiv = new JRadioButton(Bundle.getMessage("Diverging_Leg_of_Turnout"));
        buttonFacing = new JRadioButton(Bundle.getMessage("On_Facing-Point_Turnout"));
        ButtonGroup g = new ButtonGroup();
        g.add(buttonSingle);
        g.add(buttonTrailMain);
        g.add(buttonTrailDiv);
        g.add(buttonFacing);
        ActionListener a = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonClicked();
            }
        };

        buttonSingle.addActionListener(a);
        buttonTrailMain.addActionListener(a);
        buttonTrailDiv.addActionListener(a);
        buttonFacing.addActionListener(a);

        // share sensor data models
        tmSensorComboBox1.setModel(sSensorComboBox1.getModel());
        tdSensorComboBox1.setModel(sSensorComboBox1.getModel());
        fSensorComboBox1.setModel(sSensorComboBox1.getModel());

        tmSensorComboBox2.setModel(sSensorComboBox2.getModel());
        tdSensorComboBox2.setModel(sSensorComboBox2.getModel());
        fSensorComboBox2.setModel(sSensorComboBox2.getModel());

        tmSensorComboBox3.setModel(sSensorComboBox3.getModel());
        tdSensorComboBox3.setModel(sSensorComboBox3.getModel());
        fSensorComboBox3.setModel(sSensorComboBox3.getModel());

        tmSensorComboBox4.setModel(sSensorComboBox4.getModel());
        tdSensorComboBox4.setModel(sSensorComboBox4.getModel());
        fSensorComboBox4.setModel(sSensorComboBox4.getModel());

        tmSensorComboBox5.setModel(sSensorComboBox5.getModel());
        tdSensorComboBox5.setModel(sSensorComboBox5.getModel());
        fSensorComboBox5.setModel(sSensorComboBox5.getModel());
        // share turnout data model
        tmProtectTurnoutComboBox.setModel(tdProtectTurnoutComboBox.getModel());
        fProtectTurnoutComboBox.setModel(tdProtectTurnoutComboBox.getModel());

        tdNextSignalComboBox1.setModel(sNextSignalComboBox1.getModel());
        tdNextSignalComboBox1Alt.setModel(sNextSignalComboBox1Alt.getModel());
        tmNextSignalComboBox1.setModel(sNextSignalComboBox1.getModel());
        tmNextSignalComboBox1Alt.setModel(sNextSignalComboBox1Alt.getModel());
        fNextSignalComboBox1.setModel(sNextSignalComboBox1.getModel());
        fNextSignalComboBox1Alt.setModel(sNextSignalComboBox1Alt.getModel());

        // configure sensor combobox options
        setupComboBox(sSensorComboBox1, false, true, true);
        setupComboBox(sSensorComboBox2, false, true, true);
        setupComboBox(sSensorComboBox3, false, true, true);
        setupComboBox(sSensorComboBox4, false, true, true);
        setupComboBox(sSensorComboBox5, false, true, true);
        setupComboBox(tmSensorComboBox1, false, true, true);
        setupComboBox(tmSensorComboBox2, false, true, true);
        setupComboBox(tmSensorComboBox3, false, true, true);
        setupComboBox(tmSensorComboBox4, false, true, true);
        setupComboBox(tmSensorComboBox5, false, true, true);
        setupComboBox(tdSensorComboBox1, false, true, true);
        setupComboBox(tdSensorComboBox2, false, true, true);
        setupComboBox(tdSensorComboBox3, false, true, true);
        setupComboBox(tdSensorComboBox4, false, true, true);
        setupComboBox(tdSensorComboBox5, false, true, true);
        setupComboBox(fSensorComboBox1, false, true, true);
        setupComboBox(fSensorComboBox2, false, true, true);
        setupComboBox(fSensorComboBox3, false, true, true);
        setupComboBox(fSensorComboBox4, false, true, true);
        setupComboBox(fSensorComboBox5, false, true, true);
        // configure turnout combobox options
        setupComboBox(tdProtectTurnoutComboBox, false, true, true);
        setupComboBox(tmProtectTurnoutComboBox, false, true, true);
        setupComboBox(fProtectTurnoutComboBox, false, true, true);
        // configure next signal combobox options
        setupComboBox(sNextSignalComboBox1, false, true, true);
        setupComboBox(sNextSignalComboBox1Alt, false, true, true);
        setupComboBox(tdNextSignalComboBox1, false, true, true);
        setupComboBox(tdNextSignalComboBox1Alt, false, true, true);
        setupComboBox(tmNextSignalComboBox1, false, true, true);
        setupComboBox(tmNextSignalComboBox1Alt, false, true, true);
        setupComboBox(fNextSignalComboBox1, false, true, true);
        setupComboBox(fNextSignalComboBox1Alt, false, true, true);
        setupComboBox(fNextSignalComboBox2, false, true, true);
        setupComboBox(fNextSignalComboBox2Alt, false, true, true);
        // configure next sensor combobox options
        setupComboBox(fNextSensorComboBox1, false, true, true);
        setupComboBox(fNextSensorComboBox1Alt, false, true, true);
        setupComboBox(fNextSensorComboBox2, false, true, true);
        setupComboBox(fNextSensorComboBox2Alt, false, true, true);

        // add top part of GUI, holds signal head name to drive
        JPanel line = new JPanel();
        line.add(new JLabel(Bundle.getMessage("SSLHeadNamedLabel")));
        setupComboBox(outSignalHeadComboBox, true, true, true);
        line.add(outSignalHeadComboBox);
        outSignalHeadComboBox.setToolTipText(outSignalHeadTooltip);
        outSignalHeadComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // user hit enter, use this name to fill in the rest of the fields
                activate();
            }
        });

        getContentPane().add(line);

        line = new JPanel();
        line.setLayout(new BoxLayout(line, BoxLayout.Y_AXIS));
        buttonSingle.setToolTipText(buttonSingleTooltip);
        line.add(buttonSingle);
        buttonTrailMain.setToolTipText(buttonTrailMainTooltip);
        line.add(buttonTrailMain);
        buttonTrailDiv.setToolTipText(buttonTrailDivTooltip);
        line.add(buttonTrailDiv);
        buttonFacing.setToolTipText(buttonFacingTooltip);
        line.add(buttonFacing);
        line.setAlignmentX(0.5f);
        getContentPane().add(line);

        getContentPane().add(new JSeparator(JSeparator.HORIZONTAL));

        // fill in the specific panels for the modes
        getContentPane().add(fillModeSingle());
        getContentPane().add(fillModeTrailMain());
        getContentPane().add(fillModeTrailDiv());
        getContentPane().add(fillModeFacing());

        line = new JPanel();
        line.add(new JLabel(Bundle.getMessage("ApproachLightingSensorLabel")));
        line.add(approachSensor1ComboBox);
        setupComboBox(approachSensor1ComboBox, false, true, true);
        approachSensor1ComboBox.setToolTipText(approachSensor1Tooltip);
        line.setAlignmentX(0.5f);
        getContentPane().add(line);

        // add comment element
        line = new JPanel();
        line.setLayout(new FlowLayout());
        line.add(new JLabel(Bundle.getMessage("Comment")));
        line.add(commentField = new JTextField(30));
        commentField.setToolTipText(Bundle.getMessage("CommentToolTip"));
        getContentPane().add(line);

        // add status bar above buttons
        line = new JPanel();
        line.setLayout(new FlowLayout());
        statusBar = new JLabel(Bundle.getMessage("StatusSslStart"));
        statusBar.setFont(statusBar.getFont().deriveFont(0.9f * commentField.getFont().getSize())); // a bit smaller
        statusBar.setForeground(Color.gray);
        line.add(statusBar);
        getContentPane().add(line);

        getContentPane().add(new JSeparator(JSeparator.HORIZONTAL));

        JPanel buttons = new JPanel();
        buttons.setLayout(new FlowLayout());
        // add OK button at bottom
        cancel = new JButton(Bundle.getMessage("ButtonCancel"));
        buttons.add(cancel);
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelPressed();
            }
        });
        JButton apply = new JButton(Bundle.getMessage("ButtonApply"));
        apply.setToolTipText(Bundle.getMessage("ApplyToolTip"));
        buttons.add(apply);
        apply.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyPressed();
            }
        });
        getContentPane().add(buttons);

        pack();
        // set a definite mode selection, which also repacks.
        buttonSingle.setSelected(true);
        buttonClicked();
    }

    // Panel arrangements all changed to use GridBagLayout format.

    JPanel fillModeSingle() {
        modeSingle.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.EAST;
        constraints.gridheight = 1;
        constraints.gridwidth = 1;
        constraints.ipadx = 0;
        constraints.ipady = 0;
        Insets insets = new Insets(2, 3, 2, 3); // top, left, bottom, right
        constraints.insets = insets;
        constraints.weightx = 1;
        constraints.weighty = 1;

        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 0;
        constraints.gridy = 0;
        insets.top = 9;
        insets.bottom = 9;

        modeSingle.add(new JLabel(Bundle.getMessage("ProtectsSensor")), constraints);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        sSensorComboBox1.setToolTipText(sensorFieldTooltip);
        modeSingle.add(sSensorComboBox1, constraints);
        constraints.gridx = 2;
        sSensorComboBox2.setToolTipText(sensorFieldTooltip);
        modeSingle.add(sSensorComboBox2, constraints);
        constraints.gridx = 3;
        sSensorComboBox3.setToolTipText(sensorFieldTooltip);
        modeSingle.add(sSensorComboBox3, constraints);
        constraints.gridx = 4;
        sSensorComboBox4.setToolTipText(sensorFieldTooltip);
        modeSingle.add(sSensorComboBox4, constraints);
        constraints.gridx = 5;
        sSensorComboBox5.setToolTipText(sensorFieldTooltip);
        modeSingle.add(sSensorComboBox5, constraints);

        insets.top = 2;
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.NONE;

        modeSingle.add(new JLabel(Bundle.getMessage("Protects_Signal")), constraints);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        sNextSignalComboBox1.setToolTipText(highSignalFieldTooltip);
        modeSingle.add(sNextSignalComboBox1, constraints);
        constraints.gridx = 2;
        sNextSignalComboBox1Alt.setToolTipText(nextSignalFieldTooltip);
        modeSingle.add(sNextSignalComboBox1Alt, constraints);
        constraints.gridx = 3;
        constraints.gridwidth = 2;
        // ??
        JPanel q = new JPanel();
        q.setLayout(new FlowLayout());
        q.add(sLimitBox);
        q.add(sRestrictingBox);
        sLimitBox.setToolTipText(limitBoxTooltip);
        sRestrictingBox.setToolTipText(restrictingBoxTooltip);
        modeSingle.add(q, constraints);

        constraints.gridy = 6;
        constraints.gridx = 1;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.WEST;
        insets.bottom = 9;
        sFlashBox.setToolTipText(flashBoxTooltip);
        modeSingle.add(sFlashBox, constraints);

        constraints.gridx = 3;
        sDistantBox.setToolTipText(distantBoxTooltip);
        modeSingle.add(sDistantBox, constraints);
        return modeSingle;
    }

    JPanel fillModeTrailMain() {
        modeTrailMain.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.EAST;
        constraints.gridheight = 1;
        constraints.gridwidth = 1;
        constraints.ipadx = 0;
        constraints.ipady = 0;
        Insets insets = new Insets(2, 3, 2, 3); // top, left, bottom, right
        constraints.insets = insets;
        constraints.weightx = 1;
        constraints.weighty = 1;

        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 0;
        constraints.gridy = 0;
        insets.top = 9;
        insets.bottom = 9;
        modeTrailMain.add(new JLabel(Bundle.getMessage("ProtectsSensor")), constraints);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        tmSensorComboBox1.setToolTipText(sensorFieldTooltip);
        modeTrailMain.add(tmSensorComboBox1, constraints);
        constraints.gridx = 2;
        tmSensorComboBox2.setToolTipText(sensorFieldTooltip);
        modeTrailMain.add(tmSensorComboBox2, constraints);
        constraints.gridx = 3;
        tmSensorComboBox3.setToolTipText(sensorFieldTooltip);
        modeTrailMain.add(tmSensorComboBox3, constraints);
        constraints.gridx = 4;
        tmSensorComboBox4.setToolTipText(sensorFieldTooltip);
        modeTrailMain.add(tmSensorComboBox4, constraints);
        constraints.gridx = 5;
        tmSensorComboBox5.setToolTipText(sensorFieldTooltip);
        modeTrailMain.add(tmSensorComboBox5, constraints);

        insets.top = 2;
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 0;
        constraints.gridy = 1;
        insets.bottom = 9;
        modeTrailMain.add(new JLabel(Bundle.getMessage("Red_When_Turnout")), constraints);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        tmProtectTurnoutComboBox.setToolTipText(turnoutFieldTooltip);
        modeTrailMain.add(tmProtectTurnoutComboBox, constraints);
        constraints.gridx = 2;
        constraints.gridwidth = 2;
        modeTrailMain.add(new JLabel(Bundle.getMessage("IsState", InstanceManager.turnoutManagerInstance().getThrownText())), constraints);
        constraints.gridwidth = 1;

        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 0;
        constraints.gridy = 4;
        insets.bottom = 2;
        modeTrailMain.add(new JLabel(Bundle.getMessage("Protects_Signal")), constraints);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        tmNextSignalComboBox1.setToolTipText(highSignalFieldTooltip);
        modeTrailMain.add(tmNextSignalComboBox1, constraints);
        constraints.gridx = 2;
        tmNextSignalComboBox1Alt.setToolTipText(nextSignalFieldTooltip);
        modeTrailMain.add(tmNextSignalComboBox1Alt, constraints);
        constraints.gridx = 3;
        constraints.gridwidth = 2;
        // ??
        JPanel q = new JPanel();
        q.setLayout(new FlowLayout());
        q.add(tmLimitBox);
        q.add(tmRestrictingBox);
        tmLimitBox.setToolTipText(limitBoxTooltip);
        tmRestrictingBox.setToolTipText(restrictingBoxTooltip);
        modeTrailMain.add(q, constraints);

        constraints.gridy = 6;
        constraints.gridx = 1;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.WEST;
        insets.bottom = 9;
        tmFlashBox.setToolTipText(flashBoxTooltip);
        modeTrailMain.add(tmFlashBox, constraints);

        constraints.gridx = 3;
        tmDistantBox.setToolTipText(distantBoxTooltip);
        modeTrailMain.add(tmDistantBox, constraints);
        return modeTrailMain;
    }

    JPanel fillModeTrailDiv() {
        modeTrailDiv.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.EAST;
        constraints.gridheight = 1;
        constraints.gridwidth = 1;
        constraints.ipadx = 0;
        constraints.ipady = 0;
        Insets insets = new Insets(2, 3, 2, 3); // top, left, bottom, right
        constraints.insets = insets;
        constraints.weightx = 1;
        constraints.weighty = 1;

        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 0;
        constraints.gridy = 0;
        insets.top = 9;
        insets.bottom = 9;
        modeTrailDiv.add(new JLabel(Bundle.getMessage("ProtectsSensor")), constraints);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        tdSensorComboBox1.setToolTipText(sensorFieldTooltip);
        modeTrailDiv.add(tdSensorComboBox1, constraints);
        constraints.gridx = 2;
        tdSensorComboBox2.setToolTipText(sensorFieldTooltip);
        modeTrailDiv.add(tdSensorComboBox2, constraints);
        constraints.gridx = 3;
        tdSensorComboBox3.setToolTipText(sensorFieldTooltip);
        modeTrailDiv.add(tdSensorComboBox3, constraints);
        constraints.gridx = 4;
        tdSensorComboBox4.setToolTipText(sensorFieldTooltip);
        modeTrailDiv.add(tdSensorComboBox4, constraints);
        constraints.gridx = 5;
        tdSensorComboBox5.setToolTipText(sensorFieldTooltip);
        modeTrailDiv.add(tdSensorComboBox5, constraints);

        insets.top = 2;
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 0;
        constraints.gridy = 1;
        insets.bottom = 9;
        modeTrailDiv.add(new JLabel(Bundle.getMessage("Red_When_Turnout")), constraints);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        tdProtectTurnoutComboBox.setToolTipText(turnoutFieldTooltip);
        modeTrailDiv.add(tdProtectTurnoutComboBox, constraints);
        constraints.gridx = 2;
        constraints.gridwidth = 2;
        modeTrailDiv.add(new JLabel(Bundle.getMessage("IsState", InstanceManager.turnoutManagerInstance().getClosedText())), constraints);
        constraints.gridwidth = 1;

        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 0;
        constraints.gridy = 4;
        insets.bottom = 2;
        modeTrailDiv.add(new JLabel(Bundle.getMessage("Protects_Signal")), constraints);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        tdNextSignalComboBox1.setToolTipText(highSignalFieldTooltip);
        modeTrailDiv.add(tdNextSignalComboBox1, constraints);
        constraints.gridx = 2;
        tdNextSignalComboBox1Alt.setToolTipText(nextSignalFieldTooltip);
        modeTrailDiv.add(tdNextSignalComboBox1Alt, constraints);
        constraints.gridx = 3;
        constraints.gridwidth = 2;
        // ??
        JPanel q = new JPanel();
        q.setLayout(new FlowLayout());
        q.add(tdLimitBox);
        q.add(tdRestrictingBox);
        tdLimitBox.setToolTipText(limitBoxTooltip);
        tdRestrictingBox.setToolTipText(restrictingBoxTooltip);
        modeTrailDiv.add(q, constraints);

        constraints.gridy = 6;
        constraints.gridx = 1;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.WEST;
        insets.bottom = 9;
        tdFlashBox.setToolTipText(flashBoxTooltip);
        modeTrailDiv.add(tdFlashBox, constraints);

        constraints.gridx = 3;
        tdDistantBox.setToolTipText(distantBoxTooltip);
        modeTrailDiv.add(tdDistantBox, constraints);

        return modeTrailDiv;
    }

    JPanel fillModeFacing() {
        modeFacing.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.EAST;
        constraints.gridheight = 1;
        constraints.gridwidth = 1;
        constraints.ipadx = 0;
        constraints.ipady = 0;
        Insets insets = new Insets(2, 3, 2, 3); // top, left, bottom, right
        constraints.insets = insets;
        constraints.weightx = 1;
        constraints.weighty = 1;

        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 0;
        constraints.gridy = 0;
        insets.top = 9;
        insets.bottom = 9;
        modeFacing.add(new JLabel(Bundle.getMessage("ProtectsSensor")), constraints);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        fSensorComboBox1.setToolTipText(sensorFieldTooltip);
        modeFacing.add(fSensorComboBox1, constraints);
        constraints.gridx = 2;
        fSensorComboBox2.setToolTipText(sensorFieldTooltip);
        modeFacing.add(fSensorComboBox2, constraints);
        constraints.gridx = 3;
        fSensorComboBox3.setToolTipText(sensorFieldTooltip);
        modeFacing.add(fSensorComboBox3, constraints);
        constraints.gridx = 4;
        fSensorComboBox4.setToolTipText(sensorFieldTooltip);
        modeFacing.add(fSensorComboBox4, constraints);
        constraints.gridx = 5;
        fSensorComboBox5.setToolTipText(sensorFieldTooltip);
        modeFacing.add(fSensorComboBox5, constraints);

        insets.top = 2;
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 0;
        constraints.gridy = 1;
        insets.bottom = 9;
        modeFacing.add(new JLabel(Bundle.getMessage("WatchesTurnout")), constraints);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        fProtectTurnoutComboBox.setToolTipText(turnoutFieldTooltip);
        modeFacing.add(fProtectTurnoutComboBox, constraints);

        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 0;
        constraints.gridy = 2;
        insets.bottom = 2;
        modeFacing.add(new JLabel(Bundle.getMessage("To_Protect_Signal")), constraints);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        fNextSignalComboBox1.setToolTipText(highSignalFieldTooltip);
        modeFacing.add(fNextSignalComboBox1, constraints);
        constraints.gridx = 2;
        fNextSignalComboBox1Alt.setToolTipText(nextSignalFieldTooltip);
        modeFacing.add(fNextSignalComboBox1Alt, constraints);
        constraints.gridx = 3;
        constraints.gridwidth = 2;
        // ??
        JPanel q = new JPanel();
        q.setLayout(new FlowLayout());
        q.add(fmLimitBox);
        q.add(fmRestrictingBox);
        fmLimitBox.setToolTipText(limitBoxTooltip);
        fmRestrictingBox.setToolTipText(restrictingBoxTooltip);
        modeFacing.add(q, constraints);

        constraints.fill = GridBagConstraints.NONE;
        constraints.gridwidth = 1;
        constraints.gridx = 0;
        constraints.gridy = 3;
        insets.bottom = 9;
        modeFacing.add(new JLabel(Bundle.getMessage("And_Sensors")), constraints);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        fNextSensorComboBox1.setToolTipText(sensorFieldTooltip);
        modeFacing.add(fNextSensorComboBox1, constraints);
        constraints.gridx = 2;
        fNextSensorComboBox1Alt.setToolTipText(sensorFieldTooltip);
        modeFacing.add(fNextSensorComboBox1Alt, constraints);
        constraints.gridx = 3;
        constraints.gridwidth = 2;
        modeFacing.add(new JLabel(Bundle.getMessage("WhenTurnoutIsX", InstanceManager.turnoutManagerInstance().getClosedText())), constraints);
        constraints.gridwidth = 1;

        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 0;
        constraints.gridy = 4;
        insets.bottom = 2;
        modeFacing.add(new JLabel(Bundle.getMessage("And_Protect_Signal")), constraints);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        fNextSignalComboBox2.setToolTipText(highSignalFieldTooltip);
        modeFacing.add(fNextSignalComboBox2, constraints);
        constraints.gridx = 2;
        fNextSignalComboBox2Alt.setToolTipText(nextSignalFieldTooltip);
        modeFacing.add(fNextSignalComboBox2Alt, constraints);
        constraints.gridx = 3;
        constraints.gridwidth = 2;

        q = new JPanel();
        q.setLayout(new FlowLayout());
        q.add(fdLimitBox);
        q.add(fdRestrictingBox);
        fdLimitBox.setToolTipText(limitBoxTooltip);
        fdRestrictingBox.setToolTipText(restrictingBoxTooltip);
        modeFacing.add(q, constraints);

        constraints.fill = GridBagConstraints.NONE;
        constraints.gridwidth = 1;
        constraints.gridx = 0;
        constraints.gridy = 5;
        insets.bottom = 9;
        modeFacing.add(new JLabel(Bundle.getMessage("And_Sensors")), constraints);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        fNextSensorComboBox2.setToolTipText(sensorFieldTooltip);
        modeFacing.add(fNextSensorComboBox2, constraints);
        constraints.gridx = 2;
        fNextSensorComboBox2Alt.setToolTipText(sensorFieldTooltip);
        modeFacing.add(fNextSensorComboBox2Alt, constraints);
        constraints.gridx = 3;
        constraints.gridwidth = 2;
        modeFacing.add(new JLabel(Bundle.getMessage("WhenTurnoutIsX", InstanceManager.turnoutManagerInstance().getThrownText())), constraints);
        constraints.gridwidth = 1;

        constraints.gridy = 6;
        constraints.gridx = 1;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.WEST;
        insets.bottom = 9;
        fFlashBox.setToolTipText(flashBoxTooltip);
        modeFacing.add(fFlashBox, constraints);

        constraints.gridx = 3;
        fDistantBox.setToolTipText(distantBoxTooltip);
        modeFacing.add(fDistantBox, constraints);

        return modeFacing;
    }

    void applyPressed() {
        SignalHead head = sh; // temp used here for SignalHead being operated on

        // check signal head exists
        if (head == null) {
            if (outSignalHeadComboBox.getSelectedItem() == null) {
                setTitle(Bundle.getMessage("Simple_Signal_Logic"));
                statusBar.setText(Bundle.getMessage("HeadXNotDefined", outSignalHeadComboBox.getSelectedItemDisplayName()));
                // JOptionPane.showMessageDialog(this, Bundle.getMessage("HeadXNotDefined", outSignalHeadComboBox.getDisplayName()));
                return;
            } else {
                head = outSignalHeadComboBox.getSelectedItem();
                if (head == null) {
                    // getting selected signal head failed for some reason
                    log.error("Could not create the Simple Signal Logic for {} because SignalHead could not be found", outSignalHeadComboBox.getSelectedItemDisplayName());
                    statusBar.setText(Bundle.getMessage("ApplyErrorDialog"));
                    return;
                }
                statusBar.setText(Bundle.getMessage("StatusSslCreated", outSignalHeadComboBox.getSelectedItemDisplayName()));
            }
        } else {
            statusBar.setText(Bundle.getMessage("StatusSslUpdated", outSignalHeadComboBox.getSelectedItemDisplayName()));
        }

        // it does
        try {
            BlockBossLogic b = BlockBossLogic.getStoppedObject(head);
            b.setApproachSensor1(approachSensor1ComboBox.getSelectedItemDisplayName());
            if (buttonSingle.isSelected()) {
                loadSingle(b);
            } else if (buttonTrailMain.isSelected()) {
                loadTrailMain(b);
            } else if (buttonTrailDiv.isSelected()) {
                loadTrailDiv(b);
            } else if (buttonFacing.isSelected()) {
                loadFacing(b);
            } else {
                log.error("no SSL type radio button was selected"); // NOI18N
                return;
            }
            cancel.setText(Bundle.getMessage("ButtonClose")); // when Apply has been clicked at least once, this is not Cancel
        } catch (Exception e) {
            log.error("An error occurred creating the Simple Signal Logic for" + e.toString());
            statusBar.setText(Bundle.getMessage("ApplyErrorDialog"));
            // JOptionPane.showMessageDialog(this, Bundle.getMessage("ApplyErrorDialog"));
        }
    }

    void cancelPressed() {
        // close pane
        this.setVisible(false);
        statusBar.setText("");
        super.dispose();
    }

    void loadSingle(BlockBossLogic b) {
        b.setSensor1(sSensorComboBox1.getSelectedItemDisplayName());
        b.setSensor2(sSensorComboBox2.getSelectedItemDisplayName());
        b.setSensor3(sSensorComboBox3.getSelectedItemDisplayName());
        b.setSensor4(sSensorComboBox4.getSelectedItemDisplayName());
        b.setSensor5(sSensorComboBox5.getSelectedItemDisplayName());
        b.setMode(BlockBossLogic.SINGLEBLOCK);

        b.setWatchedSignal1(sNextSignalComboBox1.getSelectedItemDisplayName(), sFlashBox.isSelected());
        b.setWatchedSignal1Alt(sNextSignalComboBox1Alt.getSelectedItemDisplayName());
        b.setLimitSpeed1(sLimitBox.isSelected());
        b.setRestrictingSpeed1(sRestrictingBox.isSelected());
        b.setDistantSignal(sDistantBox.isSelected());

        b.setComment(commentField.getText());

        b.retain();
        b.start();
    }

    void loadTrailMain(BlockBossLogic b) {
        b.setSensor1(tmSensorComboBox1.getSelectedItemDisplayName());
        b.setSensor2(tmSensorComboBox2.getSelectedItemDisplayName());
        b.setSensor3(tmSensorComboBox3.getSelectedItemDisplayName());
        b.setSensor4(tmSensorComboBox4.getSelectedItemDisplayName());
        b.setSensor5(tmSensorComboBox5.getSelectedItemDisplayName());
        b.setMode(BlockBossLogic.TRAILINGMAIN);

        b.setTurnout(tmProtectTurnoutComboBox.getSelectedItemDisplayName());

        b.setWatchedSignal1(tmNextSignalComboBox1.getSelectedItemDisplayName(), tmFlashBox.isSelected());
        b.setWatchedSignal1Alt(tmNextSignalComboBox1Alt.getSelectedItemDisplayName());
        b.setLimitSpeed1(tmLimitBox.isSelected());
        b.setRestrictingSpeed1(tmRestrictingBox.isSelected());
        b.setDistantSignal(tmDistantBox.isSelected());

        b.setComment(commentField.getText());

        b.retain();
        b.start();
    }

    void loadTrailDiv(BlockBossLogic b) {
        b.setSensor1(tdSensorComboBox1.getSelectedItemDisplayName());
        b.setSensor2(tdSensorComboBox2.getSelectedItemDisplayName());
        b.setSensor3(tdSensorComboBox3.getSelectedItemDisplayName());
        b.setSensor4(tdSensorComboBox4.getSelectedItemDisplayName());
        b.setSensor5(tdSensorComboBox5.getSelectedItemDisplayName());
        b.setMode(BlockBossLogic.TRAILINGDIVERGING);

        b.setTurnout(tdProtectTurnoutComboBox.getSelectedItemDisplayName());

        b.setWatchedSignal1(tdNextSignalComboBox1.getSelectedItemDisplayName(), tdFlashBox.isSelected());
        b.setWatchedSignal1Alt(tdNextSignalComboBox1Alt.getSelectedItemDisplayName());
        b.setLimitSpeed2(tdLimitBox.isSelected());
        b.setRestrictingSpeed1(tdRestrictingBox.isSelected());
        b.setDistantSignal(tdDistantBox.isSelected());

        b.setComment(commentField.getText());

        b.retain();
        b.start();
    }

    void loadFacing(BlockBossLogic b) {
        b.setSensor1(fSensorComboBox1.getSelectedItemDisplayName());
        b.setSensor2(fSensorComboBox2.getSelectedItemDisplayName());
        b.setSensor3(fSensorComboBox3.getSelectedItemDisplayName());
        b.setSensor4(fSensorComboBox4.getSelectedItemDisplayName());
        b.setSensor5(fSensorComboBox5.getSelectedItemDisplayName());
        b.setMode(BlockBossLogic.FACING);

        b.setTurnout(fProtectTurnoutComboBox.getSelectedItemDisplayName());

        b.setWatchedSignal1(fNextSignalComboBox1.getSelectedItemDisplayName(), fFlashBox.isSelected());
        b.setWatchedSignal1Alt(fNextSignalComboBox1Alt.getSelectedItemDisplayName());
        b.setWatchedSignal2(fNextSignalComboBox2.getSelectedItemDisplayName());
        b.setWatchedSignal2Alt(fNextSignalComboBox2Alt.getSelectedItemDisplayName());
        b.setWatchedSensor1(fNextSensorComboBox1.getSelectedItemDisplayName());
        b.setWatchedSensor1Alt(fNextSensorComboBox1Alt.getSelectedItemDisplayName());
        b.setWatchedSensor2(fNextSensorComboBox2.getSelectedItemDisplayName());
        b.setWatchedSensor2Alt(fNextSensorComboBox2Alt.getSelectedItemDisplayName());
        b.setLimitSpeed1(fmLimitBox.isSelected());
        b.setRestrictingSpeed1(fmRestrictingBox.isSelected());
        b.setLimitSpeed2(fdLimitBox.isSelected());
        b.setRestrictingSpeed2(fdRestrictingBox.isSelected());

        b.setDistantSignal(fDistantBox.isSelected());

        b.setComment(commentField.getText());

        b.retain();
        b.start();
    }

    void activate() {
        // check signal head exists
        if (sh == null && outSignalHeadComboBox.getSelectedItem() == null) {
            // head not exist, just title the window and leave
            setTitle(Bundle.getMessage("Simple_Signal_Logic"));
            return;
        }

        // find existing logic
        BlockBossLogic b;
        if (sh != null) {
            b = BlockBossLogic.getExisting(sh);
        } else {
            b = BlockBossLogic.getExisting(outSignalHeadComboBox.getSelectedItem());
        }

        setTitle(Bundle.getMessage("SignalLogicForX", outSignalHeadComboBox.getSelectedItemDisplayName()));

        approachSensor1ComboBox.setSelectedItemByName(b.getApproachSensor1());

        sSensorComboBox1.setSelectedItemByName(b.getSensor1());
        sSensorComboBox2.setSelectedItemByName(b.getSensor2());
        sSensorComboBox3.setSelectedItemByName(b.getSensor3());
        sSensorComboBox4.setSelectedItemByName(b.getSensor4());
        sSensorComboBox5.setSelectedItemByName(b.getSensor5());

        tmProtectTurnoutComboBox.setSelectedItemByName(b.getTurnout());

        sNextSignalComboBox1.setSelectedItemByName(b.getWatchedSignal1());
        sNextSignalComboBox1Alt.setSelectedItemByName(b.getWatchedSignal1Alt());

        fNextSignalComboBox2.setSelectedItemByName(b.getWatchedSignal2());
        fNextSignalComboBox2Alt.setSelectedItemByName(b.getWatchedSignal2Alt());

        fNextSensorComboBox1.setSelectedItemByName(b.getWatchedSensor1());
        fNextSensorComboBox1Alt.setSelectedItemByName(b.getWatchedSensor1Alt());
        fNextSensorComboBox2.setSelectedItemByName(b.getWatchedSensor2());
        fNextSensorComboBox2Alt.setSelectedItemByName(b.getWatchedSensor2Alt());

        sLimitBox.setSelected(b.getLimitSpeed1());
        sRestrictingBox.setSelected(b.getRestrictingSpeed1());
        tdLimitBox.setSelected(b.getLimitSpeed2());
        tdRestrictingBox.setSelected(b.getRestrictingSpeed2());
        sFlashBox.setSelected(b.getUseFlash());
        sDistantBox.setSelected(b.getDistantSignal());

        commentField.setText(b.getComment());

        int mode = b.getMode();
        if (mode == BlockBossLogic.SINGLEBLOCK) {
            buttonSingle.setSelected(true);
        } else if (mode == BlockBossLogic.TRAILINGMAIN) {
            buttonTrailMain.setSelected(true);
        } else if (mode == BlockBossLogic.TRAILINGDIVERGING) {
            buttonTrailDiv.setSelected(true);
        } else if (mode == BlockBossLogic.FACING) {
            buttonFacing.setSelected(true);
        }

        statusBar.setText(Bundle.getMessage("StatusSslLoaded", Bundle.getMessage("ButtonApply")));
        // do setup of visible panels
        buttonClicked();
    }

    void buttonClicked() {
        modeSingle.setVisible(false);
        modeTrailMain.setVisible(false);
        modeTrailDiv.setVisible(false);
        modeFacing.setVisible(false);
        if (buttonSingle.isSelected()) {
            modeSingle.setVisible(true);
        } else if (buttonTrailMain.isSelected()) {
            modeTrailMain.setVisible(true);
        } else if (buttonTrailDiv.isSelected()) {
            modeTrailDiv.setVisible(true);
        } else if (buttonFacing.isSelected()) {
            modeFacing.setVisible(true);
        } else {
            log.debug("buttonClicked(): no SSL type radio button was selected");
        }
        modeSingle.revalidate();
        modeTrailMain.revalidate();
        modeTrailDiv.revalidate();
        modeFacing.revalidate();
        pack();
        modeSingle.repaint();
        modeTrailMain.repaint();
        modeTrailDiv.repaint();
        modeFacing.repaint();
    }

    SignalHead sh = null;

    /**
     * Programmatically open the frame to edit a specific SSL by signal head.
     *
     * @param sh signal head of which the name should be entered in the Edit pane
     */
    public void setSignal(SignalHead sh) {
        this.sh = sh;
        outSignalHeadComboBox.setSelectedItem(sh);
        outSignalHeadComboBox.setEnabled(false);
        activate();
    }

    /**
     * Programmatically open the frame to edit a specific SSL by signal head system name.
     *
     * @param name system or user name of the signal head to be entered in the Edit pane
     */
    public void setSignal(String name) {
        sh = null;
        outSignalHeadComboBox.setSelectedItemByName(name);
        outSignalHeadComboBox.setEnabled(true);
        activate();
    }

    /**
     * Set up editable NamedBeanComboBoxes for SSL pane.
     * Copied from LayoutEditor
     * @see jmri.jmrit.display.layoutEditor.LayoutEditor#setupComboBox(NamedBeanComboBox, boolean, boolean, boolean)
     * @author G. Warner 2017
     *
     * @param inComboBox     the editable NamedBeanComboBoxes to set up
     * @param inValidateMode boolean: if true, typed in text is validated; if
     *                       false input text is not
     * @param inEnable       boolean to enable / disable the NamedBeanComboBox
     * @param inFirstBlank   boolean to enable / disable the first item being
     *                       blank
     */
    public static void setupComboBox(@Nonnull NamedBeanComboBox<?> inComboBox, boolean inValidateMode, boolean inEnable, boolean inFirstBlank) {
        inComboBox.setEnabled(inEnable);
        inComboBox.setEditable(false);
        inComboBox.setValidatingInput(inValidateMode);
        inComboBox.setSelectedItem(null);
        log.debug("SSL setupComboBox called");
        inComboBox.setMaximumRowCount(15); // no context sensitive calculation
        inComboBox.setAllowNull(inFirstBlank);
        inComboBox.setSelectedIndex(-1);
    }

    private final static Logger log = LoggerFactory.getLogger(BlockBossLogic.class);

}

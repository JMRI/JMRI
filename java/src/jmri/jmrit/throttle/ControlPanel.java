package jmri.jmrit.throttle;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.util.EnumSet;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.MouseInputAdapter;

import jmri.*;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.FileUtil;
import jmri.util.MouseInputAdapterInstaller;
import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * A JInternalFrame that contains a JSlider to control loco speed, and buttons
 * for forward, reverse and STOP.
 * <p>
 *
 * @author glen Copyright (C) 2002
 * @author Bob Jacobsen Copyright (C) 2007
 * @author Ken Cameron Copyright (C) 2008
 */
public class ControlPanel extends JInternalFrame implements java.beans.PropertyChangeListener, ActionListener, AddressListener {
    private DccThrottle throttle;

    private JSlider speedSlider;
    private JSlider speedSliderContinuous;
    private JSpinner speedSpinner;
    private SpinnerNumberModel speedSpinnerModel;
    private JComboBox<SpeedStepMode> speedStepBox;
    private JRadioButton forwardButton, reverseButton;
    private JButton stopButton;
    private JButton idleButton;
    private JPanel buttonPanel;
    private JPanel topButtonPanel;
    private boolean internalAdjust = false; // protecting the speed slider, continuous slider and spinner when doing internal adjust

    private JPopupMenu propertiesPopup;
    private JPanel speedControlPanel;
    private JPanel spinnerPanel;
    private JPanel sliderPanel;
    private JPanel speedSliderContinuousPanel;

    private AddressPanel addressPanel; //for access to roster entry
    /* Constants for speed selection method */
    final public static int SLIDERDISPLAY = 0;
    final public static int STEPDISPLAY = 1;
    final public static int SLIDERDISPLAYCONTINUOUS = 2;

    final public static int BUTTON_SIZE = 40;

    private int _displaySlider = SLIDERDISPLAY;

    /* real time tracking of speed slider - on iff trackSlider==true
     * Min interval for sending commands to the actual throttle can be configured
     * as part of the throttle config but is bounded
     */
    private JPanel mainPanel;

    private boolean trackSlider = false;
    private final boolean trackSliderDefault = false;
    private long trackSliderMinInterval = 200;         // milliseconds
    private final long trackSliderMinIntervalDefault = 200;  // milliseconds
    private final long trackSliderMinIntervalMin = 50;       // milliseconds
    private final long trackSliderMinIntervalMax = 1000;     // milliseconds
    private long lastTrackedSliderMovementTime = 0;

    public int accelerateKey = 107; // numpad +;
    public int decelerateKey = 109; // numpad -;
    public int accelerateKey1 = KeyEvent.VK_LEFT; // Left Arrow
    public int decelerateKey1 = KeyEvent.VK_RIGHT; // Left Arrow
    public int accelerateKey2 = KeyEvent.VK_PAGE_UP; // Left Arrow
    public int decelerateKey2 = KeyEvent.VK_PAGE_DOWN; // Left Arrow
    public int reverseKey = KeyEvent.VK_DOWN;
    public int forwardKey = KeyEvent.VK_UP;
    public int stopKey = 111; // numpad /
    public int idleKey = 106; // numpad *

    // LocoNet really only has 126 speed steps i.e. 0..127 - 1 for em stop
    private int intSpeedSteps = 126;

    private int maxSpeed = 126; //The maximum permissible speed

    private boolean speedControllerEnable = false;

    // Switch to continuous slider on function...
    private String switchSliderFunction = "Fxx";
    private String prevShuntingFn = null;

    /**
     * Constructor.
     */
    public ControlPanel() {
        initGUI();
    }

    /*
     * Set the AddressPanel this throttle control is listenning for new throttle event
     */
    public void setAddressPanel(AddressPanel addressPanel) {
        this.addressPanel = addressPanel;
    }

    /*
     * "Destructor"
     */
    public void destroy() {
        if (addressPanel != null) {
            addressPanel.removeAddressListener(this);
        }
        if (throttle != null) {
            throttle.removePropertyChangeListener(this);
            throttle = null;
        }
    }

    /**
     * Enable/Disable all buttons and slider.
     *
     * @param isEnabled True if the buttons/slider should be enabled, false
     *                  otherwise.
     */
    @Override
    public void setEnabled(boolean isEnabled) {
        forwardButton.setEnabled(isEnabled);
        reverseButton.setEnabled(isEnabled);
        speedStepBox.setEnabled(isEnabled);
        stopButton.setEnabled(isEnabled);
        idleButton.setEnabled(isEnabled);
        speedControllerEnable = isEnabled;
        switch (_displaySlider) {
            case STEPDISPLAY: {
                if (speedSpinner != null) {
                    speedSpinner.setEnabled(isEnabled);
                }
                if (speedSliderContinuous != null) {
                    speedSliderContinuous.setEnabled(false);
                }
                speedSlider.setEnabled(false);
                break;
            }
            case SLIDERDISPLAYCONTINUOUS: {
                if (speedSliderContinuous != null) {
                    speedSliderContinuous.setEnabled(isEnabled);
                }
                if (speedSpinner != null) {
                    speedSpinner.setEnabled(false);
                }
                speedSlider.setEnabled(false);
                break;
            }
            default: {
                if (speedSpinner != null) {
                    speedSpinner.setEnabled(false);
                }
                if (speedSliderContinuous != null) {
                    speedSliderContinuous.setEnabled(false);
                }
                speedSlider.setEnabled(isEnabled);
            }
        }
    }

    /**
     * is this enabled?
     * @return true if enabled
     */
    @Override
    public boolean isEnabled() {
        return speedControllerEnable;
    }
    
    /**
     * Set the GUI to match that the loco is set to forward.
     *
     * @param isForward True if the loco is set to forward, false otherwise.
     */
    private void setIsForward(boolean isForward) {
        forwardButton.setSelected(isForward);
        reverseButton.setSelected(!isForward);
        if (speedSliderContinuous != null) {
            internalAdjust = true;
            if (isForward) {
                speedSliderContinuous.setValue(java.lang.Math.abs(speedSliderContinuous.getValue()));
            } else {
                speedSliderContinuous.setValue(-java.lang.Math.abs(speedSliderContinuous.getValue()));
            }
            internalAdjust = false;
        }
    }

    /**
     * Set the GUI to match the speed steps of the current address. Initialises
     * the speed slider and spinner - including setting their maximums based on
     * the speed step setting and the max speed for the particular loco
     *
     * @param speedStepMode Desired speed step mode. One of:
     *                      SpeedStepMode.NMRA_DCC_128,
     *                      SpeedStepMode.NMRA_DCC_28,
     *                      SpeedStepMode.NMRA_DCC_27,
     *                      SpeedStepMode.NMRA_DCC_14 step mode
     */
    public void setSpeedStepsMode(SpeedStepMode speedStepMode) {
        final ThrottlesPreferences preferences =
                InstanceManager.getDefault(ThrottleFrameManager.class).getThrottlesPreferences();
        internalAdjust = true;
        int maxSpeedPCT = 100;
        if (addressPanel != null && addressPanel.getRosterEntry() != null) {
            maxSpeedPCT = addressPanel.getRosterEntry().getMaxSpeedPCT();
        }

        // Save the old speed as a float
        float oldSpeed = (speedSlider.getValue() / (maxSpeed * 1.0f));

        if (speedStepMode == SpeedStepMode.UNKNOWN) {
            speedStepMode = (SpeedStepMode) speedStepBox.getSelectedItem();
        } else {
            speedStepBox.setSelectedItem(speedStepMode);
        }
        intSpeedSteps = speedStepMode.numSteps;

        /* Set maximum speed based on the max speed stored in the roster as a percentage of the maximum */
        maxSpeed = (int) ((float) intSpeedSteps * ((float) maxSpeedPCT) / 100);

        // rescale the speed slider to match the new speed step mode
        speedSlider.setMaximum(maxSpeed);
        speedSlider.setValue((int) (oldSpeed * maxSpeed));
        speedSlider.setMajorTickSpacing(maxSpeed / 2);
        java.util.Hashtable<Integer, JLabel> labelTable = new java.util.Hashtable<>();
        labelTable.put(maxSpeed / 2, new JLabel("50%"));
        labelTable.put(maxSpeed, new JLabel("100%"));
        labelTable.put(0, new JLabel(Bundle.getMessage("ButtonStop")));
        speedSlider.setLabelTable(labelTable);
        
        if (preferences.isUsingIcons()) {
            speedSlider.setPaintTicks(false);
            speedSlider.setPaintLabels(false);
        } else {
            speedSlider.setPaintTicks(true);
            speedSlider.setPaintLabels(true);
        }

        if (speedSliderContinuous != null) {
            speedSliderContinuous.setMaximum(maxSpeed);
            speedSliderContinuous.setMinimum(-maxSpeed);
            if (forwardButton.isSelected()) {
                speedSliderContinuous.setValue((int) (oldSpeed * maxSpeed));
            } else {
                speedSliderContinuous.setValue(-(int) (oldSpeed * maxSpeed));
            }
            speedSliderContinuous.setMajorTickSpacing(maxSpeed / 2);
            labelTable = new java.util.Hashtable<>();
            labelTable.put(maxSpeed / 2, new JLabel("50%"));
            labelTable.put(maxSpeed, new JLabel("100%"));
            labelTable.put(0, new JLabel(Bundle.getMessage("ButtonStop")));
            labelTable.put(-maxSpeed / 2, new JLabel("-50%"));
            labelTable.put(-maxSpeed, new JLabel("-100%"));
            speedSliderContinuous.setLabelTable(labelTable);
            speedSlider.setLabelTable(labelTable);
            if (preferences.isUsingIcons()) {
                speedSliderContinuous.setPaintTicks(false);
                speedSliderContinuous.setPaintLabels(false);
            } else {
                speedSliderContinuous.setPaintTicks(true);
                speedSliderContinuous.setPaintLabels(true);
            }
        }

        speedSpinnerModel.setMaximum(maxSpeed);
        speedSpinnerModel.setMinimum(0);
        // rescale the speed value to match the new speed step mode
        speedSpinnerModel.setValue(speedSlider.getValue());
        internalAdjust = false;
    }

    /**
     * Is this Speed Control selection method possible?
     *
     * @param displaySlider integer value. possible values: SLIDERDISPLAY = use
     *                      speed slider display STEPDISPLAY = use speed step
     *                      display
     * @return true if speed controller of the selected type is available.
     */
    public boolean isSpeedControllerAvailable(int displaySlider) {
        switch (displaySlider) {
            case STEPDISPLAY:
                return (speedSpinner != null);
            case SLIDERDISPLAY:
                return (speedSlider != null);
            case SLIDERDISPLAYCONTINUOUS:
                return (speedSliderContinuous != null);
            default:
                return false;
        }
    }

    /**
     * Set the Speed Control selection method
     *
     * @param displaySlider integer value. possible values: SLIDERDISPLAY = use
     *                      speed slider display STEPDISPLAY = use speed step
     *                      display
     */
    public void setSpeedController(int displaySlider) {
        _displaySlider = displaySlider;
        switch (displaySlider) {
            case STEPDISPLAY:
                if (speedSpinner != null) {
                    sliderPanel.setVisible(false);
                    speedSlider.setEnabled(false);
                    speedSliderContinuousPanel.setVisible(false);
                    if (speedSliderContinuous != null) {
                        speedSliderContinuous.setEnabled(false);
                    }
                    spinnerPanel.setVisible(true);
                    speedSpinner.setEnabled(speedControllerEnable);
                    return;
                }
                break;
            case SLIDERDISPLAYCONTINUOUS:
                if (speedSliderContinuous != null) {
                    sliderPanel.setVisible(false);
                    speedSlider.setEnabled(false);
                    speedSliderContinuousPanel.setVisible(true);
                    speedSliderContinuous.setEnabled(speedControllerEnable);
                    spinnerPanel.setVisible(false);
                    if (speedSpinner != null) {
                        speedSpinner.setEnabled(false);
                    }
                    return;
                }
                break;
            case SLIDERDISPLAY:
                // normal, drop through
                break;
            default:
                jmri.util.LoggingUtil.warnOnce(log, "Unexpected displaySlider = {}", displaySlider);
                break;    
        }
        sliderPanel.setVisible(true);
        speedSlider.setEnabled(speedControllerEnable);
        spinnerPanel.setVisible(false);
        if (speedSpinner != null) {
            speedSpinner.setEnabled(false);
        }
        speedSliderContinuousPanel.setVisible(false);
        if (speedSliderContinuous != null) {
            speedSliderContinuous.setEnabled(false);
        }
    }

    /**
     * Get the value indicating what speed input we're displaying
     *
     * @return SLIDERDISPLAY, STEPDISPLAY or SLIDERDISPLAYCONTINUOUS
     */
    public int getDisplaySlider() {
        return _displaySlider;
    }

    /**
     * Set real-time tracking of speed slider, or not
     *
     * @param track boolean value, true to track, false to set speed on unclick
     */
    public void setTrackSlider(boolean track) {
        trackSlider = track;
    }

    /**
     * Get status of real-time speed slider tracking
     * 
     * @return true if slider is tracking.
     */
    public boolean getTrackSlider() {
        return trackSlider;
    }

    /**
     * Set the GUI to match that the loco speed.
     *
     *
     * @param speedIncrement The throttle back end's speed increment value - %
     *                       increase for each speed step.
     * @param speed          The speed value of the loco.
     */
    private void setSpeedValues(float speedIncrement, float speed) {
        //This is an internal speed adjustment
        internalAdjust = true;
        //Translate the speed sent in to the max allowed by any set speed limit
        speedSlider.setValue(java.lang.Math.round(speed / speedIncrement));
         log.debug("SpeedSlider value: {}", speedSlider.getValue());
        // Spinner Speed should be the raw integer speed value
        if (speedSpinner != null) {
            speedSpinnerModel.setValue(speedSlider.getValue());
        }
        if (speedSliderContinuous != null) {
            if (forwardButton.isSelected()) {
                speedSliderContinuous.setValue(( speedSlider.getValue()));
            } else {
                speedSliderContinuous.setValue(-( speedSlider.getValue()));
            }
        }
        internalAdjust = false;
    }

    private GridBagConstraints makeDefaultGridBagConstraints() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridheight = 1;
        constraints.gridwidth = 1;
        constraints.ipadx = 0;
        constraints.ipady = 0;
        Insets insets = new Insets(2, 2, 2, 2);
        constraints.insets = insets;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.gridx = 0;
        constraints.gridy = 0;

        return constraints;
    }

    private void layoutTopButtonPanel() {
        GridBagConstraints constraints = makeDefaultGridBagConstraints();

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        topButtonPanel.add(speedStepBox, constraints);
    }

    private void layoutButtonPanel() {
        final ThrottlesPreferences preferences =
                InstanceManager.getDefault(ThrottleFrameManager.class).getThrottlesPreferences();
                
        GridBagConstraints constraints = makeDefaultGridBagConstraints();
        constraints.fill = GridBagConstraints.NONE;

        constraints.gridy = 10;
        if (preferences.isUsingIcons()) {
            constraints.gridx = 2;
        }
        buttonPanel.add(forwardButton, constraints);

        if (preferences.isUsingIcons()) {
            constraints.gridx = 0;
        } else {
            constraints.gridy = 20;
        }
        buttonPanel.add(reverseButton, constraints);

        if (preferences.isUsingIcons()) {
            constraints.gridx = 1;
        } else { 
            constraints.gridy = 30;
        }
        buttonPanel.add(idleButton, constraints);

        if (preferences.isUsingIcons()) {
            constraints.gridx = 1;
        } else {
            constraints.gridx = 0;
        }
        constraints.gridy = 40;
        buttonPanel.add(stopButton, constraints);
    }

    private void layoutSliderPanel() {
        sliderPanel.setLayout(new GridBagLayout());

        GridBagConstraints constraints = makeDefaultGridBagConstraints();

        sliderPanel.add(speedSlider, constraints);
    }

    private void layoutSpeedSliderContinuous() {
        speedSliderContinuousPanel.setLayout(new GridBagLayout());

        GridBagConstraints constraints = makeDefaultGridBagConstraints();

        speedSliderContinuousPanel.add(speedSliderContinuous, constraints);
    }

    private void layoutSpinnerPanel() {
        spinnerPanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = makeDefaultGridBagConstraints();

        spinnerPanel.add(speedSpinner, constraints);
    }

    private void setupButton(AbstractButton button, final ThrottlesPreferences preferences, final String iconPath,
        final String selectedIconPath, final String message) {
        if (preferences.isUsingIcons()) {
            button.setBorderPainted(false);
            button.setContentAreaFilled(false);
            button.setText(null);
            button.setIcon(new ImageIcon(FileUtil.findURL(iconPath)));
            button.setSelectedIcon(new ImageIcon(FileUtil.findURL(selectedIconPath)));
            button.setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
            button.setToolTipText(Bundle.getMessage(message));
        } else {
            button.setText(Bundle.getMessage(message));
        }
    }

    /**
     * Create, initialize and place GUI components.
     */
    private void initGUI() {
        final ThrottlesPreferences preferences =
                InstanceManager.getDefault(ThrottleFrameManager.class).getThrottlesPreferences();
        mainPanel = new JPanel(new BorderLayout());
        this.setContentPane(mainPanel);
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        topButtonPanel = new JPanel();
        topButtonPanel.setLayout(new GridBagLayout());
        mainPanel.add(topButtonPanel, BorderLayout.NORTH);

        speedControlPanel = new JPanel();
        speedControlPanel.setLayout(new BoxLayout(speedControlPanel, BoxLayout.X_AXIS));
        speedControlPanel.setOpaque(false);
        mainPanel.add(speedControlPanel, BorderLayout.CENTER);
        sliderPanel = new JPanel();
        sliderPanel.setOpaque(false);
        
        speedSlider = new JSlider(0, intSpeedSteps);
        if (preferences.isUsingIcons()) {
            speedSlider.setUI(new ControlPanelCustomSliderUI(speedSlider));
        }
        speedSlider.setOpaque(false);
        speedSlider.setValue(0);
        speedSlider.setFocusable(false);

        // add mouse-wheel support
        speedSlider.addMouseWheelListener((MouseWheelEvent e) -> {
            if (e.getWheelRotation() > 0) {
                for (int i = 0; i < e.getScrollAmount(); i++) {
                    decelerate1();
                }
            } else {
                for (int i = 0; i < e.getScrollAmount(); i++) {
                    accelerate1();
                }
            }
        });

        speedSliderContinuous = new JSlider(-intSpeedSteps, intSpeedSteps);
        if (preferences.isUsingIcons()) {
            speedSliderContinuous.setUI(new ControlPanelCustomSliderUI(speedSlider));
        }
        speedSliderContinuous.setValue(0);
        speedSliderContinuous.setOpaque(false);
        speedSliderContinuous.setFocusable(false);

        // add mouse-wheel support
        speedSliderContinuous.addMouseWheelListener((MouseWheelEvent e) -> {
            if (e.getWheelRotation() > 0) {
                for (int i = 0; i < e.getScrollAmount(); i++) {
                    decelerate1();
                }
            } else {
                for (int i = 0; i < e.getScrollAmount(); i++) {
                    accelerate1();
                }
            }
        });

        speedSpinner = new JSpinner();

        speedSpinnerModel = new SpinnerNumberModel(0, 0, intSpeedSteps, 1);
        speedSpinner.setModel(speedSpinnerModel);
        speedSpinner.setFocusable(false);

        EnumSet<SpeedStepMode> speedStepModes = InstanceManager.throttleManagerInstance().supportedSpeedModes();

        speedStepBox = new JComboBox<>(speedStepModes.toArray(new SpeedStepMode[speedStepModes.size()]));

        forwardButton = new JRadioButton();
        setupButton(forwardButton, preferences, "resources/icons/throttles/up-red.png",
            "resources/icons/throttles/up-green.png", "ButtonForward");

        reverseButton = new JRadioButton();
        setupButton(reverseButton, preferences, "resources/icons/throttles/down-red.png",
            "resources/icons/throttles/down-green.png", "ButtonReverse");

        propertiesPopup = new JPopupMenu();

        layoutSliderPanel();
        speedControlPanel.add(sliderPanel);
        speedSlider.setOrientation(JSlider.VERTICAL);
        speedSlider.setMajorTickSpacing(maxSpeed / 2);
        java.util.Hashtable<Integer, JLabel> labelTable = new java.util.Hashtable<>();
        labelTable.put(maxSpeed / 2, new JLabel("50%"));
        labelTable.put(maxSpeed, new JLabel("100%"));
        labelTable.put(0, new JLabel(Bundle.getMessage("ButtonStop")));
        speedSlider.setLabelTable(labelTable);
        if (preferences.isUsingIcons()) {
            speedSlider.setPaintTicks(false);
            speedSlider.setPaintLabels(false);
        } else {
            speedSlider.setPaintTicks(true);
            speedSlider.setPaintLabels(true);
        }
        // remove old actions
        speedSlider.addChangeListener((ChangeEvent e) -> {
            if (!internalAdjust) {
                boolean doIt = false;
                if (!speedSlider.getValueIsAdjusting()) {
                    doIt = true;
                    lastTrackedSliderMovementTime = System.currentTimeMillis() - trackSliderMinInterval;
                } else if (trackSlider
                        && System.currentTimeMillis() - lastTrackedSliderMovementTime >= trackSliderMinInterval) {
                    doIt = true;
                    lastTrackedSliderMovementTime = System.currentTimeMillis();
                }
                if (doIt) {
                    float newSpeed = (speedSlider.getValue() / (intSpeedSteps * 1.0f));
                    if (log.isDebugEnabled()) {
                        log.debug("stateChanged: slider pos: " + speedSlider.getValue() + " speed: " + newSpeed);
                    }
                    if (sliderPanel.isVisible() && throttle != null) {
                        throttle.setSpeedSetting(newSpeed);
                    }
                    if (speedSpinner != null) {
                        speedSpinnerModel.setValue(speedSlider.getValue());
                    }
                    if (speedSliderContinuous != null) {
                        if (forwardButton.isSelected()) {
                            speedSliderContinuous.setValue(( speedSlider.getValue()));
                        } else {
                            speedSliderContinuous.setValue(-( speedSlider.getValue()));
                        }
                    }
                }
            }
        });

        speedSliderContinuousPanel = new JPanel();
        layoutSpeedSliderContinuous();

        speedControlPanel.add(speedSliderContinuousPanel);
        speedSliderContinuous.setOrientation(JSlider.VERTICAL);
        speedSliderContinuous.setMajorTickSpacing(maxSpeed / 2);
        labelTable = new java.util.Hashtable<>();
        labelTable.put(maxSpeed / 2, new JLabel("50%"));
        labelTable.put(maxSpeed, new JLabel("100%"));
        labelTable.put(0, new JLabel(Bundle.getMessage("ButtonStop")));
        labelTable.put(-maxSpeed / 2, new JLabel("-50%"));
        labelTable.put(-maxSpeed, new JLabel("-100%"));
        speedSliderContinuous.setLabelTable(labelTable);
        if (preferences.isUsingIcons()) {
            speedSliderContinuous.setPaintTicks(false);
            speedSliderContinuous.setPaintLabels(false);
        } else {
            speedSliderContinuous.setPaintTicks(true);
            speedSliderContinuous.setPaintLabels(true);
        }
        // remove old actions
        speedSliderContinuous.addChangeListener((ChangeEvent e) -> {
            if (!internalAdjust) {
                boolean doIt = false;
                if (!speedSliderContinuous.getValueIsAdjusting()) {
                    doIt = true;
                    lastTrackedSliderMovementTime = System.currentTimeMillis() - trackSliderMinInterval;
                } else if (trackSlider
                        && System.currentTimeMillis() - lastTrackedSliderMovementTime >= trackSliderMinInterval) {
                    doIt = true;
                    lastTrackedSliderMovementTime = System.currentTimeMillis();
                }
                if (doIt) {
                    float newSpeed = (java.lang.Math.abs(speedSliderContinuous.getValue()) / (intSpeedSteps * 1.0f));
                    boolean newDir = (speedSliderContinuous.getValue() >= 0);
                    if (log.isDebugEnabled()) {
                        log.debug("stateChanged: slider pos: " + speedSliderContinuous.getValue() + " speed: " + newSpeed + " dir: " + newDir);
                    }
                    if (speedSliderContinuousPanel.isVisible() && throttle != null) {
                        throttle.setSpeedSetting(newSpeed);
                        if ((newSpeed > 0) && (newDir != forwardButton.isSelected())) {
                            throttle.setIsForward(newDir);
                        }
                    }
                    if (speedSpinner != null) {
                        speedSpinnerModel.setValue(java.lang.Math.abs(speedSliderContinuous.getValue()));
                    }
                    if (speedSlider != null) {
                        speedSlider.setValue(java.lang.Math.abs(speedSliderContinuous.getValue()));
                    }
                }
            }
        });

        spinnerPanel = new JPanel();
        layoutSpinnerPanel();

        speedControlPanel.add(spinnerPanel);

        // remove old actions
        speedSpinner.addChangeListener((ChangeEvent e) -> {
            if (!internalAdjust) {
                float newSpeed = ((Integer) speedSpinner.getValue()).floatValue() / (intSpeedSteps * 1.0f);
                if (log.isDebugEnabled()) {
                    log.debug("stateChanged: spinner pos: " + speedSpinner.getValue() + " speed: " + newSpeed);
                }
                if (throttle != null) {
                    if (spinnerPanel.isVisible()) {
                        throttle.setSpeedSetting(newSpeed);
                    }
                    speedSlider.setValue(((Integer) speedSpinner.getValue()));
                    if (speedSliderContinuous != null) {
                        if (forwardButton.isSelected()) {
                            speedSliderContinuous.setValue(((Integer) speedSpinner.getValue()));
                        } else {
                            speedSliderContinuous.setValue(-((Integer) speedSpinner.getValue()));
                        }
                    }
                } else {
                    log.warn("no throttle object in stateChanged, ignoring change of speed to {}", newSpeed);
                }
            }
        });


        speedStepBox.addActionListener((ActionEvent e) -> {
            SpeedStepMode s = (SpeedStepMode)speedStepBox.getSelectedItem();
            setSpeedStepsMode(s);
            if (throttle != null) {
              throttle.setSpeedStepMode(s);
            }
        });

        buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        ButtonGroup directionButtons = new ButtonGroup();
        directionButtons.add(forwardButton);
        directionButtons.add(reverseButton);
        forwardButton.setFocusable(false);
        reverseButton.setFocusable(false);

        forwardButton.addActionListener((ActionEvent e) -> {
            if (throttle != null) {
              throttle.setIsForward(true);
            }
            if (speedSliderContinuous != null) {
                speedSliderContinuous.setValue(java.lang.Math.abs(speedSliderContinuous.getValue()));
            }
        });

        reverseButton.addActionListener((ActionEvent e) -> {
            if (throttle != null) {
              throttle.setIsForward(false);
            }
            if (speedSliderContinuous != null) {
                speedSliderContinuous.setValue(-java.lang.Math.abs(speedSliderContinuous.getValue()));
            }
        });

        stopButton = new JButton();
        setupButton(stopButton, preferences, "resources/icons/throttles/estop.png",
            "resources/icons/throttles/estop24.png", "ButtonEStop");

        stopButton.addActionListener((ActionEvent e) -> {
            stop();
        });

        stopButton.addMouseListener(
                new MouseListener() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        stop();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                    }

                    @Override
                    public void mouseClicked(MouseEvent e) {
                    }
                });
        idleButton = new JButton();
        setupButton(idleButton, preferences, "resources/icons/throttles/stop.png",
            "resources/icons/throttles/stop24.png", "ButtonIdle");

        idleButton.addActionListener((ActionEvent e) -> {
            speedSlider.setValue(0);
            if (speedSpinner != null) {
                speedSpinner.setValue(0);
            }
            if (speedSliderContinuous != null) {
                speedSliderContinuous.setValue(0);
            }
            throttle.setSpeedSetting(0);
        });

        addComponentListener(
                new ComponentAdapter() {
                    @Override
                    public void componentResized(ComponentEvent e) {
                        changeOrientation();
                    }
                });

        layoutButtonPanel();
        layoutTopButtonPanel();

        JMenuItem propertiesItem = new JMenuItem(Bundle.getMessage("ControlPanelProperties"));
        propertiesItem.addActionListener(this);
        propertiesPopup.add(propertiesItem);

        // Add a mouse listener all components to trigger the popup menu.
        MouseInputAdapter popupListener = new PopupListener(propertiesPopup, this);
        MouseInputAdapterInstaller.installMouseInputAdapterOnAllComponents(popupListener, this);

        // Install the Key bindings on all Components
        KeyListenerInstaller.installKeyListenerOnAllComponents(new ControlPadKeyListener(), this);

        // set by default which speed selection method is on top
        setSpeedController(_displaySlider);
    }

    /**
     * Perform an emergency stop.
     *
     */
    public void stop() {
        if (this.throttle == null) {
            return;
        }
        internalAdjust = true;
        throttle.setSpeedSetting(-1);
        speedSlider.setValue(0);
        if (speedSpinner != null) {
            speedSpinnerModel.setValue(0);
        }
        if (speedSliderContinuous != null) {
            speedSliderContinuous.setValue(0);
        }
        internalAdjust = false;
    }

    /**
     * The user has resized the Frame. Possibly change from Horizontal to
     * Vertical layout.
     */
    private void changeOrientation() {
        if (mainPanel.getWidth() > mainPanel.getHeight()) {
            speedSlider.setOrientation(JSlider.HORIZONTAL);
            if (speedSliderContinuous != null) {
                speedSliderContinuous.setOrientation(JSlider.HORIZONTAL);
            }
            mainPanel.remove(buttonPanel);
            mainPanel.add(buttonPanel, BorderLayout.EAST);
        } else {
            speedSlider.setOrientation(JSlider.VERTICAL);
            if (speedSliderContinuous != null) {
                speedSliderContinuous.setOrientation(JSlider.VERTICAL);
            }
            mainPanel.remove(buttonPanel);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        }
    }

    /* Accelerate of 1
     *
     */
    private void accelerate1() {
        if (speedSlider.isEnabled()) {
            if (speedSlider.getValue() != speedSlider.getMaximum()) {
                speedSlider.setValue(speedSlider.getValue() + 1);
            }
        } else if (speedSpinner != null && speedSpinner.isEnabled()) {
            if (((Integer) speedSpinner.getValue()) < ((Integer) speedSpinnerModel.getMaximum())
                    && ((Integer) speedSpinner.getValue()) >= ((Integer) speedSpinnerModel.getMinimum())) {
                speedSpinner.setValue(((Integer) speedSpinner.getValue()) + 1);
            }
        } else if (speedSliderContinuous != null && speedSliderContinuous.isEnabled()) {
            if (speedSliderContinuous.getValue() != speedSliderContinuous.getMaximum()) {
                speedSliderContinuous.setValue(speedSliderContinuous.getValue() + 1);
            }
        }
    }

    /* Accelerate of 10
     *
     */
    private void accelerate10() {
        if (speedSlider.isEnabled()) {
            if (speedSlider.getValue() != speedSlider.getMaximum()) {
                speedSlider.setValue(speedSlider.getValue() + 10);
            }
        } else if (speedSpinner != null && speedSpinner.isEnabled()) {
            if (((Integer) speedSpinner.getValue()) < ((Integer) speedSpinnerModel.getMaximum())
                    && ((Integer) speedSpinner.getValue()) >= ((Integer) speedSpinnerModel.getMinimum())) {
                Integer speedvalue = ((Integer) speedSpinner.getValue()) + 10;
                if (speedvalue < ((Integer) speedSpinnerModel.getMaximum())) {
                    speedSpinner.setValue(speedvalue);
                } else {
                    speedSpinner.setValue(speedSpinnerModel.getMaximum());
                }
            }
        } else if (speedSliderContinuous != null && speedSliderContinuous.isEnabled()) {
            if (speedSliderContinuous.getValue() != speedSliderContinuous.getMaximum()) {
                speedSliderContinuous.setValue(speedSliderContinuous.getValue() + 10);
            }
        }
    }

    /* Decelerate of 1
     *
     */
    private void decelerate1() {
        if (speedSlider.isEnabled()) {
            if (speedSlider.getValue() != speedSlider.getMinimum()) {
                speedSlider.setValue(speedSlider.getValue() - 1);
            }
        } else if (speedSpinner != null && speedSpinner.isEnabled()) {
            if (((Integer) speedSpinner.getValue()) <= ((Integer) speedSpinnerModel.getMaximum())
                    && ((Integer) speedSpinner.getValue()) > ((Integer) speedSpinnerModel.getMinimum())) {
                speedSpinner.setValue(((Integer) speedSpinner.getValue()) - 1);
            }
        } else if (speedSliderContinuous != null && speedSliderContinuous.isEnabled()) {
            if (speedSliderContinuous.getValue() != speedSliderContinuous.getMinimum()) {
                speedSliderContinuous.setValue(speedSliderContinuous.getValue() - 1);
            }
        }
    }

    /* Decelerate of 10
     *
     */
    private void decelerate10() {
        if (speedSlider.isEnabled()) {
            if (speedSlider.getValue() != speedSlider.getMinimum()) {
                speedSlider.setValue(speedSlider.getValue() - 10);
            }
        } else if (speedSpinner != null && speedSpinner.isEnabled()) {
            if (((Integer) speedSpinner.getValue()) <= ((Integer) speedSpinnerModel.getMaximum())
                    && ((Integer) speedSpinner.getValue()) > ((Integer) speedSpinnerModel.getMinimum())) {
                Integer speedvalue = ((Integer) speedSpinner.getValue()) - 10;
                if (speedvalue > ((Integer) speedSpinnerModel.getMinimum())) {
                    speedSpinner.setValue(speedvalue);
                } else {
                    speedSpinner.setValue(speedSpinnerModel.getMinimum());
                }
            }
        } else if (speedSliderContinuous != null && speedSliderContinuous.isEnabled()) {
            if (speedSliderContinuous.getValue() != speedSliderContinuous.getMinimum()) {
                speedSliderContinuous.setValue(speedSliderContinuous.getValue() - 10);
            }
        }
    }

    /**
     * A KeyAdapter that listens for the keys that work the control pad buttons
     *
     * @author glen
     */
    class ControlPadKeyListener extends KeyAdapter {

        /**
         * Description of the Method
         *
         * @param e Description of the Parameter
         */
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.isAltDown() || e.isControlDown() || e.isMetaDown() || e.isShiftDown()) {
                return; // we don't want speed change while changing Frame/Panel/Window
            }
            if ((e.getKeyCode() == accelerateKey) || (e.getKeyCode() == accelerateKey1)) {
                accelerate1();
            } else if (e.getKeyCode() == accelerateKey2) {
                accelerate10();
            } else if ((e.getKeyCode() == decelerateKey) || (e.getKeyCode() == decelerateKey1)) {
                decelerate1();
            } else if (e.getKeyCode() == decelerateKey2) {
                decelerate10();
            } else if (e.getKeyCode() == forwardKey) {
                if (forwardButton.isEnabled()) {
                    forwardButton.doClick();
                }
            } else if (e.getKeyCode() == reverseKey) {
                if (reverseButton.isEnabled()) {
                    reverseButton.doClick();
                }
            } else if (e.getKeyCode() == stopKey) {
                if (speedSlider.isEnabled()
                        || (speedSpinner != null && speedSpinner.isEnabled())) {
                    stop();
                }
            } else if (e.getKeyCode() == idleKey) {
                if (speedSlider.isEnabled()
                        || (speedSpinner != null && speedSpinner.isEnabled())) {
                    speedSlider.setValue(0);
                }
            }
        }
    }

    /**
     * Intended for throttle scripting
     * 
     * @param fwd direction: true for forward; false for reverse.
     */
    public void setForwardDirection(boolean fwd) {
        if (fwd) {
            if (forwardButton.isEnabled()) {
                forwardButton.doClick();
            } else {
                log.error("setForwardDirection(true) with forwardButton disabled, failed");
            }
        } else {
            if (reverseButton.isEnabled()) {
                reverseButton.doClick();
            } else {
                log.error("setForwardDirection(false) with reverseButton disabled, failed");
            }
        }
    }

    /**
     * Intended for throttle scripting
     * 
     * @return The speed slider.
     */
    public JSlider getSpeedSlider() {
        return speedSlider;
    }

    /**
     * Intended for throttle scripting and testing.
     *
     * @return The continuous (shunting) speed slider.
     */
    public JSlider getSpeedSliderContinuous() {
        return speedSliderContinuous;
    }

    // update the state of this panel if any of the properties change
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals(Throttle.SPEEDSETTING)) {
            internalAdjust = true;
            float speed = ((Float) e.getNewValue());
            // multiply by MAX_SPEED, and round to find the new
            //slider setting.
            int newSliderSetting = java.lang.Math.round(speed * maxSpeed);
            if (log.isDebugEnabled()) {
                log.debug("propertyChange: new speed float: " + speed + " slider pos: " + newSliderSetting);
            }
            speedSlider.setValue(newSliderSetting);
            if (speedSpinner != null) {
                speedSpinner.setValue(newSliderSetting);
            }
            if (speedSliderContinuous != null) {
                if (forwardButton.isSelected()) {
                    speedSliderContinuous.setValue(( speedSlider.getValue()));
                } else {
                    speedSliderContinuous.setValue(-( speedSlider.getValue()));
                }
            }
            internalAdjust = false;
        } else if (e.getPropertyName().equals(Throttle.SPEEDSTEPS)) {
            SpeedStepMode steps = (SpeedStepMode)e.getNewValue();
            setSpeedStepsMode(steps);
        } else if (e.getPropertyName().equals(Throttle.ISFORWARD)) {
            boolean Forward = ((Boolean) e.getNewValue());
            setIsForward(Forward);
        } else if (e.getPropertyName().equals(switchSliderFunction)) {
            if ((Boolean) e.getNewValue()) { // switch only if displaying sliders
                if (_displaySlider == SLIDERDISPLAY) {
                    setSpeedController(SLIDERDISPLAYCONTINUOUS);
                }
            } else {
                if (_displaySlider == SLIDERDISPLAYCONTINUOUS) {
                    setSpeedController(SLIDERDISPLAY);
                }
            }
        }
        log.debug("Property change event received {} / {}", e.getPropertyName(), e.getNewValue());
    }

    /**
     * Handle the selection from the popup menu.
     *
     * @param e The ActionEvent causing the action.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        ControlPanelPropertyEditor editor
                = new ControlPanelPropertyEditor(this);
        editor.setVisible(true);
    }

    /**
     * A PopupListener to handle mouse clicks and releases. Handles the popup
     * menu.
     */
    static class PopupListener extends MouseInputAdapter {

        private final JPopupMenu _menu;
        private final JInternalFrame parentFrame;

        PopupListener(JPopupMenu menu, JInternalFrame parent) {
            parentFrame = parent;
            _menu = menu;
        }

        /**
         * If the event is the popup trigger, which is dependent on the
         * platform, present the popup menu. Otherwise change the state of the
         * function depending on the locking state of the button.
         *
         * @param e The MouseEvent causing the action.
         */
        @Override
        public void mousePressed(MouseEvent e) {
            if (log.isDebugEnabled()) {
                log.debug("pressed {} {} {}" + (" " + MouseEvent.ALT_DOWN_MASK + "/" + MouseEvent.META_DOWN_MASK + "/" + MouseEvent.CTRL_DOWN_MASK), e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK, e.isPopupTrigger(), e.getModifiersEx() & (MouseEvent.ALT_DOWN_MASK + MouseEvent.META_DOWN_MASK + MouseEvent.CTRL_DOWN_MASK));
            }
            if (e.isPopupTrigger() && parentFrame.isSelected()) {
                try {
                    _menu.show(e.getComponent(),
                            e.getX(), e.getY());
                } catch (java.awt.IllegalComponentStateException cs) {
                    // Message sent to a hidden component, so we need
                }
                e.consume();
            }
        }

        /**
         * If the event is the popup trigger, which is dependent on the
         * platform, present the popup menu. Otherwise change the state of the
         * function depending on the locking state of the button.
         *
         * @param e The MouseEvent causing the action.
         */
        @Override
        public void mouseReleased(MouseEvent e) {
            if (log.isDebugEnabled()) {
                log.debug("released {} {} {}", e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK, e.isPopupTrigger(), e.getModifiersEx() & (MouseEvent.ALT_DOWN_MASK + InputEvent.META_DOWN_MASK + MouseEvent.CTRL_DOWN_MASK));
            }
            if (e.isPopupTrigger()) {
                try {
                    _menu.show(e.getComponent(),
                            e.getX(), e.getY());
                } catch (java.awt.IllegalComponentStateException cs) {
                    // Message sent to a hidden component, so we need
                }

                e.consume();
            }
        }
    }

    /**
     * Collect the prefs of this object into XML Element
     * <ul>
     * <li> Window prefs
     * </ul>
     *
     *
     * @return the XML of this object.
     */
    public Element getXml() {
        Element me = new Element("ControlPanel");
        me.setAttribute("displaySpeedSlider", String.valueOf(this._displaySlider));
        me.setAttribute("trackSlider", String.valueOf(this.trackSlider));
        me.setAttribute("trackSliderMinInterval", String.valueOf(this.trackSliderMinInterval));
        me.setAttribute("switchSliderOnFunction", switchSliderFunction != null ? switchSliderFunction : "Fxx");
        //Element window = new Element("window");
        java.util.ArrayList<Element> children = new java.util.ArrayList<>(1);
        children.add(WindowPreferences.getPreferences(this));
        me.setContent(children);
        return me;
    }

    /**
     * Set the preferences based on the XML Element.
     * <ul>
     * <li> Window prefs
     * </ul>
     *
     *
     * @param e The Element for this object.
     */
    public void setXml(Element e) {
        internalAdjust = true;
        try {
            this.setSpeedController(e.getAttribute("displaySpeedSlider").getIntValue());
        } catch (org.jdom2.DataConversionException ex) {
            log.error("DataConverstionException in setXml: {}", ex);
            // in this case, recover by displaying the speed slider.
            this.setSpeedController(SLIDERDISPLAY);
        }
        Attribute tsAtt = e.getAttribute("trackSlider");
        if (tsAtt != null) {
            try {
                trackSlider = tsAtt.getBooleanValue();
            } catch (org.jdom2.DataConversionException ex) {
                trackSlider = trackSliderDefault;
            }
        } else {
            trackSlider = trackSliderDefault;
        }
        Attribute tsmiAtt = e.getAttribute("trackSliderMinInterval");
        if (tsmiAtt != null) {
            try {
                trackSliderMinInterval = tsmiAtt.getLongValue();
            } catch (org.jdom2.DataConversionException ex) {
                trackSliderMinInterval = trackSliderMinIntervalDefault;
            }
            if (trackSliderMinInterval < trackSliderMinIntervalMin) {
                trackSliderMinInterval = trackSliderMinIntervalMin;
            } else if (trackSliderMinInterval > trackSliderMinIntervalMax) {
                trackSliderMinInterval = trackSliderMinIntervalMax;
            }
        } else {
            trackSliderMinInterval = trackSliderMinIntervalDefault;
        }
        if ((prevShuntingFn == null) && (e.getAttribute("switchSliderOnFunction") != null)) {
            setSwitchSliderFunction(e.getAttribute("switchSliderOnFunction").getValue());
        }
        internalAdjust = false;
        Element window = e.getChild("window");
        WindowPreferences.setPreferences(this, window);
    }

    @Override
    public void notifyAddressChosen(LocoAddress l) {
    }

    @Override
    public void notifyAddressReleased(LocoAddress la) {
        this.setEnabled(false);
        if (throttle != null) {
            throttle.removePropertyChangeListener(this);
        }
        throttle = null;
        if (prevShuntingFn != null) {
            setSwitchSliderFunction(prevShuntingFn);
            prevShuntingFn = null;
        }
    }

    @Override
    public void notifyAddressThrottleFound(DccThrottle t) {
        log.debug("control panel received new throttle");
        this.throttle = t;
        this.setEnabled(true);
        this.setIsForward(throttle.getIsForward());
        this.setSpeedValues(throttle.getSpeedIncrement(),
                throttle.getSpeedSetting());

        // Set speed steps
        this.setSpeedStepsMode(throttle.getSpeedStepMode());

        this.throttle.addPropertyChangeListener(this);
        if (log.isDebugEnabled()) {
            jmri.DccLocoAddress Address = (jmri.DccLocoAddress) throttle.getLocoAddress();
            log.debug("new address is {}", Address.toString());
        }

        if ((addressPanel != null) && (addressPanel.getRosterEntry() != null) && (addressPanel.getRosterEntry().getShuntingFunction() != null)) {
            prevShuntingFn = getSwitchSliderFunction();
            setSwitchSliderFunction(addressPanel.getRosterEntry().getShuntingFunction());
        } else {
            setSwitchSliderFunction(switchSliderFunction); // reset slider
        }
    }

    @Override
    public void notifyConsistAddressChosen(int newAddress, boolean isLong) {
    }

    @Override
    public void notifyConsistAddressReleased(int address, boolean isLong) {
    }

    @Override
    public void notifyConsistAddressThrottleFound(DccThrottle throttle) {
        if (log.isDebugEnabled()) {
            log.debug("control panel received consist throttle");
        }
        notifyAddressThrottleFound(throttle);
    }

    public void setSwitchSliderFunction(String fn) {
        switchSliderFunction = fn;
        if ((switchSliderFunction == null) || (switchSliderFunction.length() == 0)) {
            return;
        }
        if ((throttle != null) && (_displaySlider != STEPDISPLAY)) { // Update UI depending on function state
            try {
                // this uses reflection because the user is allowed to name a 
                // throttle function that triggers this action. 
                java.lang.reflect.Method getter = throttle.getClass().getMethod("get" + switchSliderFunction, (Class[]) null);

                Boolean state = (Boolean) getter.invoke(throttle, (Object[]) null);
                if (state) {
                    setSpeedController(SLIDERDISPLAYCONTINUOUS);
                } else {
                    setSpeedController(SLIDERDISPLAY);
                }

            } catch (IllegalAccessException|NoSuchMethodException|java.lang.reflect.InvocationTargetException ex) {
                log.debug("Exception in setSwitchSliderFunction: {} while looking for function {}", ex, switchSliderFunction);
            }
        }
    }

    public String getSwitchSliderFunction() {
        return switchSliderFunction;
    }

    public void saveToRoster(RosterEntry re) {
        if (re == null) {
            return;
        }
        if ((re.getShuntingFunction() != null) && (re.getShuntingFunction().compareTo(getSwitchSliderFunction()) != 0)) {
            re.setShuntingFunction(getSwitchSliderFunction());
        } else if ((re.getShuntingFunction() == null) && (getSwitchSliderFunction() != null)) {
            re.setShuntingFunction(getSwitchSliderFunction());
        } else {
            return;
        }
        Roster.getDefault().writeRoster();
    }

    // initialize logging
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ControlPanel.class);
}

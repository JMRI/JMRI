package jmri.jmrit.throttle;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;

import jmri.*;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.FileUtil;
import jmri.util.MouseInputAdapterInstaller;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.transcoder.*;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.util.XMLResourceDescriptor;
import org.jdom2.Element;
import org.jdom2.Attribute;
import org.w3c.dom.Document;

/**
 * A JInternalFrame that contains a JSlider to control loco speed, and buttons
 * for forward, reverse and STOP.
 * <p>
 *
 * @author glen Copyright (C) 2002
 * @author Bob Jacobsen Copyright (C) 2007, 2021
 * @author Ken Cameron Copyright (C) 2008
 * @author Lionel Jeanson 2009-2021
 */
public class ControlPanel extends JInternalFrame implements java.beans.PropertyChangeListener, AddressListener {
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

    private Document forwardButtonSvgIcon;
    private Document forwardSelectedButtonSvgIcon;
    private Document forwardRollButtonSvgIcon;
    private ImageIcon forwardButtonImageIcon;
    private ImageIcon forwardSelectedButtonImageIcon;
    private ImageIcon forwardRollButtonImageIcon;

    private Document reverseButtonSvgIcon;
    private Document reverseSelectedButtonSvgIcon;
    private Document reverseRollButtonSvgIcon;
    private ImageIcon reverseButtonImageIcon;
    private ImageIcon reverseSelectedButtonImageIcon;
    private ImageIcon reverseRollButtonImageIcon;

    private Document idleButtonSvgIcon;
    private Document idleSelectedButtonSvgIcon;
    private Document idleRollButtonSvgIcon;
    private ImageIcon idleButtonImageIcon;
    private ImageIcon idleSelectedButtonImageIcon;
    private ImageIcon idleRollButtonImageIcon;

    private Document stopButtonSvgIcon;
    private Document stopSelectedButtonSvgIcon;
    private Document stopRollButtonSvgIcon;
    private ImageIcon stopButtonImageIcon;
    private ImageIcon stopSelectedButtonImageIcon;
    private ImageIcon stopRollButtonImageIcon;


    private boolean internalAdjust = false; // protecting the speed slider, continuous slider and spinner when doing internal adjust

    private JPopupMenu popupMenu;
    private ControlPanelPropertyEditor propertyEditor;
    private JPanel speedControlPanel;
    private JPanel spinnerPanel;
    private JPanel sliderPanel;
    private JPanel speedSliderContinuousPanel;

    private AddressPanel addressPanel; //for access to roster entry
    /* Constants for speed selection method */
    final public static int SLIDERDISPLAY = 0;
    final public static int STEPDISPLAY = 1;
    final public static int SLIDERDISPLAYCONTINUOUS = 2;

    final public static int DEFAULT_BUTTON_SIZE = 24;
    private static final String LONGEST_SS_STRING="999";
    private static final int FONT_SIZE_MIN=12;
    private static final int FONT_INCREMENT = 2;

    private int _displaySlider = SLIDERDISPLAY;

    /* real time tracking of speed slider - on iff trackSlider==true
     * Min interval for sending commands to the actual throttle can be configured
     * as part of the throttle config but is bounded
     */
    private JPanel mainPanel;

    private boolean trackSlider = false;
    private boolean hideSpeedStep = false;
    private final boolean trackSliderDefault = false;
    private long trackSliderMinInterval = 200;         // milliseconds
    private final long trackSliderMinIntervalDefault = 200;  // milliseconds
    private final long trackSliderMinIntervalMin = 50;       // milliseconds
    private final long trackSliderMinIntervalMax = 1000;     // milliseconds
    private long lastTrackedSliderMovementTime = 0;

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
        if (jmri.InstanceManager.getNullableDefault(ThrottlesPreferences.class) == null) {
            log.debug("Creating new ThrottlesPreference Instance");
            jmri.InstanceManager.store(new ThrottlesPreferences(), ThrottlesPreferences.class);
        }
        initGUI();
        applyPreferences();
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

    private void paintSpeedSliderDecorations(JSlider slider, Boolean paint) {
        slider.setPaintTicks(paint);
        slider.setPaintLabels(paint);
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
        final ThrottlesPreferences preferences = InstanceManager.getDefault(ThrottlesPreferences.class);
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
        paintSpeedSliderDecorations(speedSlider, ! (preferences.isUsingExThrottle() && preferences.isUsingLargeSpeedSlider()));

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
            paintSpeedSliderDecorations(speedSliderContinuous, ! (preferences.isUsingExThrottle() && preferences.isUsingLargeSpeedSlider()));
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
     * Provide direct access to speed slider for
     * scripting.
     * @return the speed slider
     */
    public JSlider getSpeedSlider() {
        return speedSlider;
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
     * Set hiding speed step selector (or not)
     *
     * @param hide boolean value, true to hide, false to show
     */
    public void setHideSpeedStep(boolean hide) {
        hideSpeedStep = hide;
        this.speedStepBox.setVisible(! hideSpeedStep);
    }

    /**
     * Get status of hiding  speed step selector
     *
     * @return true if speed step selector is hiden.
     */
    public boolean getHideSpeedStep() {
        return hideSpeedStep;
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
        stopButton.setSelected((speed == -1 ));
        idleButton.setSelected((speed == 0 ));
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
        constraints.insets = new Insets(2, 2, 2, 2);
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
        final ThrottlesPreferences preferences = InstanceManager.getDefault(ThrottlesPreferences.class);
        GridBagConstraints constraints = makeDefaultGridBagConstraints();
        if (preferences.isUsingExThrottle() && preferences.isUsingFunctionIcon()) {
            resizeButtons();
            constraints.insets =  new Insets(0, 0, 0, 0);
            constraints.gridheight = 2;
            constraints.gridwidth = 2;
            constraints.gridy = 0;
            constraints.gridx = 0;
            buttonPanel.add(reverseButton, constraints);
            constraints.gridx = 3;
            buttonPanel.add(forwardButton, constraints);

            constraints.gridheight = 1;
            constraints.gridwidth = 1;
            constraints.gridx = 2;
            constraints.gridy = 0;
            buttonPanel.add(idleButton, constraints);
            constraints.gridy = 1;
            buttonPanel.add(stopButton, constraints);
        } else {
            constraints.fill = GridBagConstraints.NONE;
            constraints.gridy = 1;
            buttonPanel.add(forwardButton, constraints);
            constraints.gridy = 2;
            buttonPanel.add(reverseButton, constraints);
            constraints.gridy = 3;
            buttonPanel.add(idleButton, constraints);
            constraints.gridy = 4;
            buttonPanel.add(stopButton, constraints);
        }
    }

    private void resizeButtons() {
        final ThrottlesPreferences preferences = InstanceManager.getDefault(ThrottlesPreferences.class);
        int w = buttonPanel.getWidth();
        int h = buttonPanel.getHeight();
        if ((buttonPanel.getWidth() == 0 || buttonPanel.getHeight() == 0)
                || !(preferences.isUsingExThrottle() && preferences.isUsingLargeSpeedSlider()) ){
            w = DEFAULT_BUTTON_SIZE * 5;
            h = DEFAULT_BUTTON_SIZE * 2;
        }
        float f = Math.min( Math.floorDiv(w*2,5), h );
        if (forwardButtonSvgIcon != null ) {
            forwardButton.setIcon(scaleTo(forwardButtonSvgIcon, f));
        } else {
            forwardButton.setIcon(scaleTo(forwardButtonImageIcon, (int)f));
        }
        if (forwardSelectedButtonSvgIcon != null) {
            forwardButton.setSelectedIcon(scaleTo(forwardSelectedButtonSvgIcon, f));
        } else {
            forwardButton.setSelectedIcon(scaleTo(forwardSelectedButtonImageIcon, (int)f));
        }
        if (forwardRollButtonSvgIcon!=null) {
            forwardButton.setRolloverIcon(scaleTo(forwardRollButtonSvgIcon, f));
        } else {
            forwardButton.setRolloverIcon(scaleTo(forwardRollButtonImageIcon, (int)f));
        }
        if (reverseButtonSvgIcon!=null) {
            reverseButton.setIcon(scaleTo(reverseButtonSvgIcon, f));
        } else {
            reverseButton.setIcon(scaleTo(reverseButtonImageIcon, (int)f));
        }
        if (reverseSelectedButtonSvgIcon != null) {
            reverseButton.setSelectedIcon(scaleTo(reverseSelectedButtonSvgIcon, f));
        } else {
            reverseButton.setSelectedIcon(scaleTo(reverseSelectedButtonImageIcon, (int)f));
        }
        if (reverseRollButtonSvgIcon!=null) {
            reverseButton.setRolloverIcon(scaleTo(reverseRollButtonSvgIcon, f));
        } else {
            reverseButton.setRolloverIcon(scaleTo(reverseRollButtonImageIcon, (int)f));
        }

        f = Math.min( Math.floorDiv(w,5), h/2 );
        if (idleButtonSvgIcon!=null) {
            idleButton.setIcon(scaleTo(idleButtonSvgIcon, f));
        } else {
            idleButton.setIcon(scaleTo(idleButtonImageIcon, (int)f));
        }
        if (idleSelectedButtonSvgIcon!=null) {
            idleButton.setSelectedIcon(scaleTo(idleSelectedButtonSvgIcon, f));
        } else {
            idleButton.setSelectedIcon(scaleTo(idleSelectedButtonImageIcon, (int)f));
        }
        if (idleRollButtonSvgIcon != null) {
            idleButton.setRolloverIcon(scaleTo(idleRollButtonSvgIcon, f));
        } else {
            idleButton.setRolloverIcon(scaleTo(idleRollButtonImageIcon, (int)f));
        }
        if (stopButtonSvgIcon!=null) {
            stopButton.setIcon(scaleTo(stopButtonSvgIcon, f));
        } else {
            stopButton.setIcon(scaleTo(stopButtonImageIcon, (int)f));
        }
        if (stopSelectedButtonSvgIcon!=null) {
            stopButton.setSelectedIcon(scaleTo(stopSelectedButtonSvgIcon, f));
        } else {
            stopButton.setSelectedIcon(scaleTo(stopSelectedButtonImageIcon, (int)f));
        }
        if (stopRollButtonSvgIcon!=null) {
            stopButton.setRolloverIcon(scaleTo(stopRollButtonSvgIcon, f));
        } else {
            stopButton.setRolloverIcon(scaleTo(stopRollButtonImageIcon, (int)f));
        }
    }

    private ImageIcon scaleTo(ImageIcon imic, int s ) {
        return new ImageIcon(imic.getImage().getScaledInstance(s, s, Image.SCALE_SMOOTH));
    }

    private ImageIcon scaleTo(Document svgImage, Float f ) {
        MyTranscoder transcoder = new MyTranscoder();
        TranscodingHints hints = new TranscodingHints();
        hints.put(ImageTranscoder.KEY_WIDTH, f );
        hints.put(ImageTranscoder.KEY_HEIGHT, f );
        transcoder.setTranscodingHints(hints);
        try {
            transcoder.transcode(new TranscoderInput(svgImage), null);
        } catch (TranscoderException ex) {
            // log it, but continue
            log.debug("Exception while transposing : {}", ex.getMessage());
        }
        return new ImageIcon(transcoder.getImage());
    }

    private void layoutSliderPanel() {
        sliderPanel.setLayout(new GridBagLayout());
        sliderPanel.add(speedSlider, makeDefaultGridBagConstraints());
    }

    private void layoutSpeedSliderContinuous() {
        speedSliderContinuousPanel.setLayout(new GridBagLayout());
        speedSliderContinuousPanel.add(speedSliderContinuous, makeDefaultGridBagConstraints());
    }

    private void layoutSpinnerPanel() {
        spinnerPanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = makeDefaultGridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        spinnerPanel.add(speedSpinner, constraints);
    }

    private void setupButton(AbstractButton button, final ThrottlesPreferences preferences, final String message) {
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setVerticalAlignment(SwingConstants.CENTER);
        button.setToolTipText(Bundle.getMessage(message));
        if (preferences!=null && preferences.isUsingExThrottle() && preferences.isUsingFunctionIcon()) {
            button.setBorder(null);
            button.setBorderPainted(false);
            button.setContentAreaFilled(false);
            button.setText(null);
            button.setRolloverEnabled(true);
        } else {
            button.setBorder((new JButton()).getBorder());
            button.setBorderPainted(true);
            button.setContentAreaFilled(true);
            button.setText(Bundle.getMessage(message));
            button.setIcon(null);
            button.setSelectedIcon(null);
            button.setRolloverIcon(null);
            button.setRolloverEnabled(false);
        }
    }

    /**
     * Create, initialize and place GUI components.
     */
    private void initGUI() {
        mainPanel = new JPanel(new BorderLayout());
        this.setContentPane(mainPanel);
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        JPanel speedPanel = new JPanel();
        speedPanel.setLayout(new BorderLayout());
        speedPanel.setOpaque(false);
        mainPanel.add(speedPanel, BorderLayout.CENTER);

        topButtonPanel = new JPanel();
        topButtonPanel.setLayout(new GridBagLayout());
        speedPanel.add(topButtonPanel, BorderLayout.NORTH);

        speedControlPanel = new JPanel();
        speedControlPanel.setLayout(new BoxLayout(speedControlPanel, BoxLayout.X_AXIS));
        speedControlPanel.setOpaque(false);
        speedPanel.add(speedControlPanel, BorderLayout.CENTER);
        sliderPanel = new JPanel();
        sliderPanel.setOpaque(false);

        speedSlider = new JSlider(0, intSpeedSteps);
        speedSlider.setOpaque(false);
        speedSlider.setValue(0);
        speedSlider.setFocusable(false);

        speedSliderContinuous = new JSlider(-intSpeedSteps, intSpeedSteps);
        speedSliderContinuous.setValue(0);
        speedSliderContinuous.setOpaque(false);
        speedSliderContinuous.setFocusable(false);

        speedSpinner = new JSpinner();
        speedSpinnerModel = new SpinnerNumberModel(0, 0, intSpeedSteps, 1);
        speedSpinner.setModel(speedSpinnerModel);
        speedSpinner.setMinimumSize(new Dimension(20,20));

        // customize speed spinner keyboard and focus interactions to not conflict with throttle keyboard shortcuts
        speedSpinner.getActionMap().put("doNothing", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //do nothing
            }
        });
        speedSpinner.getActionMap().put("giveUpFocus", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
               InstanceManager.getDefault(ThrottleFrameManager.class).getCurrentThrottleFrame().getRootPane().requestFocusInWindow();
            }
        });

        for ( int i : new ArrayList<>(Arrays.asList(
                KeyEvent.VK_0, KeyEvent.VK_1, KeyEvent.VK_2, KeyEvent.VK_3, KeyEvent.VK_4, KeyEvent.VK_5, KeyEvent.VK_6, KeyEvent.VK_7, KeyEvent.VK_8, KeyEvent.VK_9,
                KeyEvent.VK_NUMPAD0, KeyEvent.VK_NUMPAD1, KeyEvent.VK_NUMPAD2, KeyEvent.VK_NUMPAD3, KeyEvent.VK_NUMPAD4, KeyEvent.VK_NUMPAD5, KeyEvent.VK_NUMPAD6, KeyEvent.VK_NUMPAD7, KeyEvent.VK_NUMPAD8, KeyEvent.VK_NUMPAD9,
                KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_UP, KeyEvent.VK_DOWN,
                KeyEvent.VK_DELETE, KeyEvent.VK_BACK_SPACE
        ))) {
            speedSpinner.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(i, 0, true), "doNothing");
            speedSpinner.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(i, 0, false), "doNothing");
        }
        speedSpinner.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "giveUpFocus");
        speedSpinner.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "giveUpFocus");

        EnumSet<SpeedStepMode> speedStepModes = InstanceManager.throttleManagerInstance().supportedSpeedModes();
        speedStepBox = new JComboBox<>(speedStepModes.toArray(new SpeedStepMode[speedStepModes.size()]));

        forwardButton = new JRadioButton();
        reverseButton = new JRadioButton();
        try {
            forwardButtonSvgIcon = createSVGDocument(FileUtil.findURI("resources/icons/throttles/dirFwdOff.svg").toString());
        } catch (Exception ex) {
            log.debug("Issue loading svg icon, reverting to png : {}", ex.getMessage());
            forwardButtonSvgIcon = null;
            forwardButtonImageIcon = new ImageIcon(FileUtil.findURL("resources/icons/throttles/dirFwdOff64.png"));
        }
        try {
            forwardSelectedButtonSvgIcon = createSVGDocument(FileUtil.findURI("resources/icons/throttles/dirFwdOn.svg").toString());
        } catch (Exception ex) {
            log.debug("Issue loading svg icon, reverting to png : {}", ex.getMessage());
            forwardSelectedButtonSvgIcon = null;
            forwardSelectedButtonImageIcon = new ImageIcon(FileUtil.findURL("resources/icons/throttles/dirFwdOn64.png"));
        }
        try {
            forwardRollButtonSvgIcon = createSVGDocument(FileUtil.findURI("resources/icons/throttles/dirFwdRoll.svg").toString());
        } catch (Exception ex) {
            log.debug("Issue loading svg icon, reverting to png : {}", ex.getMessage());
            forwardRollButtonSvgIcon = null;
            forwardRollButtonImageIcon = new ImageIcon(FileUtil.findURL("resources/icons/throttles/dirFwdRoll64.png"));
        }
        try {
            reverseButtonSvgIcon = createSVGDocument(FileUtil.findURI("resources/icons/throttles/dirBckOff.svg").toString());
        } catch (Exception ex) {
            log.debug("Issue loading svg icon, reverting to png : {}", ex.getMessage());
            reverseButtonSvgIcon = null;
            reverseButtonImageIcon = new ImageIcon(FileUtil.findURL("resources/icons/throttles/dirBckOff64.png"));
        }
        try {
            reverseSelectedButtonSvgIcon = createSVGDocument(FileUtil.findURI("resources/icons/throttles/dirBckOn.svg").toString());
        } catch (Exception ex) {
            log.debug("Issue loading svg icon, reverting to png : {}", ex.getMessage());
            reverseSelectedButtonSvgIcon = null;
            reverseSelectedButtonImageIcon = new ImageIcon(FileUtil.findURL("resources/icons/throttles/dirBckOn64.png"));
        }
        try {
            reverseRollButtonSvgIcon = createSVGDocument(FileUtil.findURI("resources/icons/throttles/dirBckRoll.svg").toString());
        } catch (Exception ex) {
            log.debug("Issue loading svg icon, reverting to png : {}", ex.getMessage());
            reverseRollButtonSvgIcon = null;
            reverseRollButtonImageIcon = new ImageIcon(FileUtil.findURL("resources/icons/throttles/dirBckRoll64.png"));
        }

        layoutSliderPanel();
        speedControlPanel.add(sliderPanel);
        speedSlider.setOrientation(JSlider.VERTICAL);
        speedSlider.setMajorTickSpacing(maxSpeed / 2);
        java.util.Hashtable<Integer, JLabel> labelTable = new java.util.Hashtable<>();
        labelTable.put(maxSpeed / 2, new JLabel("50%"));
        labelTable.put(maxSpeed, new JLabel("100%"));
        labelTable.put(0, new JLabel(Bundle.getMessage("ButtonStop")));
        speedSlider.setLabelTable(labelTable);
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
        idleButton = new JButton();
        try {
            stopButtonSvgIcon = createSVGDocument(FileUtil.findURI("resources/icons/throttles/estop.svg").toString());
        } catch (Exception ex) {
            log.debug("Issue loading svg icon, reverting to png : {}", ex.getMessage());
            stopButtonSvgIcon = null;
            stopButtonImageIcon = new ImageIcon(FileUtil.findURL("resources/icons/throttles/estop64.png"));
        }
        try {
            stopSelectedButtonSvgIcon = createSVGDocument(FileUtil.findURI("resources/icons/throttles/estopOn.svg").toString());
        } catch (Exception ex) {
            log.debug("Issue loading svg icon, reverting to png : {}", ex.getMessage());
            stopSelectedButtonSvgIcon = null;
            stopSelectedButtonImageIcon = new ImageIcon(FileUtil.findURL("resources/icons/throttles/estopOn64.png"));
        }
        try {
            stopRollButtonSvgIcon = createSVGDocument(FileUtil.findURI("resources/icons/throttles/estopRoll.svg").toString());
        } catch (Exception ex) {
            log.debug("Issue loading svg icon, reverting to png : {}", ex.getMessage());
            stopRollButtonSvgIcon = null;
            stopRollButtonImageIcon = new ImageIcon(FileUtil.findURL("resources/icons/throttles/estopRoll64.png"));
        }
        try {
            idleButtonSvgIcon = createSVGDocument(FileUtil.findURI("resources/icons/throttles/stop.svg").toString());
        } catch (Exception ex) {
            log.debug("Issue loading svg icon, reverting to png : {}", ex.getMessage());
            idleButtonSvgIcon = null;
            idleButtonImageIcon = new ImageIcon(FileUtil.findURL("resources/icons/throttles/stop64.png"));
        }
        try {
            idleSelectedButtonSvgIcon = createSVGDocument(FileUtil.findURI("resources/icons/throttles/stopOn.svg").toString());
        } catch (Exception ex) {
            log.debug("Issue loading svg icon, reverting to png : {}", ex.getMessage());
            idleSelectedButtonSvgIcon = null;
            idleSelectedButtonImageIcon = new ImageIcon(FileUtil.findURL("resources/icons/throttles/stopOn64.png"));
        }
        try {
            idleRollButtonSvgIcon = createSVGDocument(FileUtil.findURI("resources/icons/throttles/stopRoll.svg").toString());
        } catch (Exception ex) {
            log.debug("Issue loading svg icon, reverting to png : {}", ex.getMessage());
            idleRollButtonSvgIcon = null;
            idleRollButtonImageIcon = new ImageIcon(FileUtil.findURL("resources/icons/throttles/stopRoll64.png"));
        }

        stopButton.addActionListener((ActionEvent e) -> {
            stop();
        });

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

        speedPanel.addComponentListener(
                new ComponentAdapter() {
                    @Override
                    public void componentResized(ComponentEvent e) {
                        changeFontSizes();
                    }
                });

        layoutButtonPanel();
        layoutTopButtonPanel();

        // Add a mouse listener all components to trigger the popup menu.
        MouseInputAdapterInstaller.installMouseListenerOnAllComponents(new PopupListener(), this);

        // set by default which speed selection method is on top
        setSpeedController(_displaySlider);
    }

  /**
   * Use the SAXSVGDocumentFactory to parse the given URI into a DOM.
   *
   * @param uri The path to the SVG file to read.
   * @return A Document instance that represents the SVG file.
   * @throws IOException The file could not be read.
   */
    private Document createSVGDocument( String uri ) throws IOException {
      String parser = XMLResourceDescriptor.getXMLParserClassName();
      SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory( parser );
      return factory.createDocument( uri );
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
        final ThrottlesPreferences preferences = InstanceManager.getDefault(ThrottlesPreferences.class);
        if (mainPanel.getWidth() > mainPanel.getHeight()) {
            speedSlider.setOrientation(JSlider.HORIZONTAL);
            if (speedSliderContinuous != null) {
                speedSliderContinuous.setOrientation(JSlider.HORIZONTAL);
            }
            if ( preferences.isUsingExThrottle() && preferences.isUsingFunctionIcon() && preferences.isUsingLargeSpeedSlider() ) {
                int bpw = mainPanel.getHeight()*5/2;
                if (bpw > mainPanel.getWidth()/2) {
                    bpw = mainPanel.getWidth()/2;
                }
                buttonPanel.setSize(bpw, mainPanel.getHeight());
                resizeButtons();
            }
            mainPanel.remove(buttonPanel);
            mainPanel.add(buttonPanel, BorderLayout.EAST);
        } else {
            speedSlider.setOrientation(JSlider.VERTICAL);
            if (speedSliderContinuous != null) {
                speedSliderContinuous.setOrientation(JSlider.VERTICAL);
            }
            if ( preferences.isUsingExThrottle() && preferences.isUsingFunctionIcon() && preferences.isUsingLargeSpeedSlider() ) {
                int bph = mainPanel.getWidth()*2/5;
                if (bph > mainPanel.getHeight()/2) {
                    bph = mainPanel.getHeight()/2;
                }
                buttonPanel.setSize(mainPanel.getWidth(), bph);
                resizeButtons();
            }
            mainPanel.remove(buttonPanel);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        }
    }

    /**
     * A resizing has occurred, so determine the optimum font size for the speed spinner text font.
     */
    private void changeFontSizes() {
        final ThrottlesPreferences preferences = InstanceManager.getDefault(ThrottlesPreferences.class);
        if ( preferences.isUsingExThrottle() && preferences.isUsingLargeSpeedSlider() ) {
            int fontSize = speedSpinner.getFont().getSize();
            // fit vertically
            int fieldHeight = speedControlPanel.getSize().height;
            int stringHeight = speedSpinner.getFontMetrics(speedSpinner.getFont()).getHeight() + 16;
            if (stringHeight > fieldHeight) { // component has shrunk vertically
                while ((stringHeight > fieldHeight) && (fontSize >= FONT_SIZE_MIN + FONT_INCREMENT)) {
                    fontSize -= FONT_INCREMENT;
                    Font f = new Font("", Font.PLAIN, fontSize);
                    speedSpinner.setFont(f);
                    stringHeight = speedSpinner.getFontMetrics(speedSpinner.getFont()).getHeight() + 16;
                }
            } else { // component has grown vertically
                while (fieldHeight - stringHeight > 10) {
                    fontSize += FONT_INCREMENT;
                    Font f = new Font("", Font.PLAIN, fontSize);
                    speedSpinner.setFont(f);
                    stringHeight = speedSpinner.getFontMetrics(speedSpinner.getFont()).getHeight() + 16 ;
                }
            }
            // fit horizontally
            int fieldWidth = speedControlPanel.getSize().width;
            int stringWidth = speedSpinner.getFontMetrics(speedSpinner.getFont()).stringWidth(LONGEST_SS_STRING) + 24 ;
            while ((stringWidth > fieldWidth) && (fontSize >= FONT_SIZE_MIN + FONT_INCREMENT)) { // component has shrunk horizontally
                fontSize -= FONT_INCREMENT;
                Font f = new Font("", Font.PLAIN, fontSize);
                speedSpinner.setFont(f);
                stringWidth = speedSpinner.getFontMetrics(speedSpinner.getFont()).stringWidth(LONGEST_SS_STRING) + 24 ;
            }
            speedSpinner.setMinimumSize(new Dimension(stringWidth,stringHeight)); //not sure why this helps here, required
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


    // update the state of this panel if any of the properties change
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals(Throttle.SPEEDSETTING)) {
            float speed = ((Float) e.getNewValue());
            setSpeedValues( throttle.getSpeedIncrement(), speed);
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
     * Apply current throttles preferences to this panel
     */
    void applyPreferences() {
        final ThrottlesPreferences preferences = InstanceManager.getDefault(ThrottlesPreferences.class);

        if (preferences.isUsingExThrottle() && preferences.isUsingLargeSpeedSlider()) {
             speedSlider.setUI(new ControlPanelCustomSliderUI(speedSlider));
             speedSliderContinuous.setUI(new ControlPanelCustomSliderUI(speedSliderContinuous));
             changeFontSizes();
        } else {
            speedSlider.setUI((new JSlider()).getUI());
            speedSliderContinuous.setUI((new JSlider()).getUI());
            speedSpinner.setFont(new JSpinner().getFont());
        }
        paintSpeedSliderDecorations(speedSlider, ! (preferences.isUsingExThrottle() && preferences.isUsingLargeSpeedSlider()));
        paintSpeedSliderDecorations(speedSliderContinuous, ! (preferences.isUsingExThrottle() && preferences.isUsingLargeSpeedSlider()));
        hideSpeedStep = ( preferences.isUsingExThrottle() && preferences.isHidingSpeedStepSelector() );
        speedStepBox.setVisible(! hideSpeedStep);

        setupButton(stopButton, preferences, "ButtonEStop");
        setupButton(idleButton, preferences, "ButtonIdle");
        setupButton(forwardButton, preferences, "ButtonForward");
        setupButton(reverseButton, preferences, "ButtonReverse");
        buttonPanel.removeAll();
        layoutButtonPanel();
        if (preferences.isUsingExThrottle() && preferences.isUsingFunctionIcon()) {
            changeOrientation(); // force buttons resizing
        }
    }

    /**
     * A PopupListener to handle mouse clicks and releases. Handles the popup
     * menu.
     */
    private class PopupListener extends MouseAdapter {
        /**
         * If the event is the popup trigger, which is dependent on the
         * platform, present the popup menu.
         * @param e The MouseEvent causing the action.
         */
        @Override
        public void mouseClicked(MouseEvent e) {
            checkTrigger(e);
        }

        /**
         * If the event is the popup trigger, which is dependent on the
         * platform, present the popup menu.
         * @param e The MouseEvent causing the action.
         */
        @Override
        public void mousePressed(MouseEvent e) {
            checkTrigger( e);
        }

        /**
         * If the event is the popup trigger, which is dependent on the
         * platform, present the popup menu.
         * @param e The MouseEvent causing the action.
         */
        @Override
        public void mouseReleased(MouseEvent e) {
            checkTrigger( e);
        }

        private void checkTrigger( MouseEvent e) {
            if (e.isPopupTrigger()) {
                initPopupMenu();
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    private void initPopupMenu() {
        if (popupMenu == null) {
            JMenuItem propertiesMenuItem = new JMenuItem(Bundle.getMessage("ControlPanelProperties"));
            propertiesMenuItem.addActionListener((ActionEvent e) -> {
                if (propertyEditor == null) {
                    propertyEditor = new ControlPanelPropertyEditor(this);
                }
                propertyEditor.setLocation(MouseInfo.getPointerInfo().getLocation());
                propertyEditor.resetProperties();
                propertyEditor.setVisible(true);
            });
            popupMenu = new JPopupMenu();
            popupMenu.add(propertiesMenuItem);
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
        me.setAttribute("hideSpeedStep", String.valueOf(this.hideSpeedStep));
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
        final ThrottlesPreferences preferences = InstanceManager.getDefault(ThrottlesPreferences.class);
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
        Attribute hssAtt = e.getAttribute("hideSpeedStep");
        if (hssAtt != null) {
            try {
                setHideSpeedStep ( hssAtt.getBooleanValue() );
            } catch (org.jdom2.DataConversionException ex) {
                setHideSpeedStep ( preferences.isUsingExThrottle() && preferences.isHidingSpeedStepSelector() );
            }
        } else {
            setHideSpeedStep ( preferences.isUsingExThrottle() && preferences.isHidingSpeedStepSelector() );
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
        throttle = t;
        setEnabled(true);
        setIsForward(throttle.getIsForward());
        setSpeedStepsMode(throttle.getSpeedStepMode());
        setSpeedValues(throttle.getSpeedIncrement(), throttle.getSpeedSetting());

        throttle.addPropertyChangeListener(this);

        if ((addressPanel != null) && (addressPanel.getRosterEntry() != null) && (addressPanel.getRosterEntry().getShuntingFunction() != null)) {
            prevShuntingFn = getSwitchSliderFunction();
            setSwitchSliderFunction(addressPanel.getRosterEntry().getShuntingFunction());
        } else {
            setSwitchSliderFunction(switchSliderFunction); // reset slider
        }

        if (log.isDebugEnabled()) {
            jmri.DccLocoAddress Address = (jmri.DccLocoAddress) throttle.getLocoAddress();
            log.debug("new address is {}", Address.toString());
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
        log.debug("control panel received consist throttle");
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

   private static class MyTranscoder extends ImageTranscoder {
        private BufferedImage image = null;
        @Override
        public BufferedImage createImage(int w, int h) {
            image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            return image;
        }
        public BufferedImage getImage() {
            return image;
        }
        @Override
        public void writeImage(BufferedImage bi, TranscoderOutput to) throws TranscoderException {
            //not required here, do nothing
        }
    }

    // initialize logging
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ControlPanel.class);
}

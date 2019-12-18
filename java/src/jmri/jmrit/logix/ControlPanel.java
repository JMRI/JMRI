package jmri.jmrit.logix;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.EnumSet;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import jmri.DccThrottle;
import jmri.SpeedStepMode;

import jmri.Throttle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JInternalFrame that contains a JSlider to control loco speed, and buttons
 * for forward, reverse and STOP.
 * <p>
 * TODO: fix speed increments (14, 28)
 *
 * @author glen Copyright (C) 2002
 * @author Bob Jacobsen Copyright (C) 2007
 * @author Ken Cameron Copyright (C) 2008
 *
 */
public class ControlPanel extends JInternalFrame implements java.beans.PropertyChangeListener {

    private LearnThrottleFrame _throttleFrame;

    private JSlider speedSlider;
    private JSpinner speedSpinner;
    private SpinnerNumberModel speedSpinnerModel;
    private JRadioButton speedStep128Button;
    private JRadioButton speedStep28Button;
    private JRadioButton speedStep27Button;
    private JRadioButton speedStep14Button;

    private JPanel speedControlPanel;
    private JPanel spinnerPanel;
    private JPanel sliderPanel;

    private boolean _displaySlider = true;
    private boolean speedControllerEnable;
    private boolean _emergencyStop = false;

    private DccThrottle _throttle;
    private boolean internalAdjust = false;

    private long trackSliderMinInterval = 500;          // milliseconds
    private long lastTrackedSliderMovementTime = 0;

    // DCC really only has 126 speed steps i.e. 0..127 - 1 for em stop
    private int MAX_SPEED = 126;

    // Save the speed step mode to aid in storage of the throttle.
    //private int _speedStepMode = SpeedStepMode.NMRA_DCC_128;
    /**
     * Constructor.
     */
    public ControlPanel(LearnThrottleFrame ltf) {
        super("Speed");
        _throttleFrame = ltf;
        speedSlider = new JSlider(0, MAX_SPEED);
        speedSlider.setValue(0);
        speedSlider.setFocusable(false);

        // add mouse-wheel support
        speedSlider.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.getWheelRotation() > 0) {
                    decelerate1();
                } else {
                    accelerate1();
                }
            }
        });

        speedSpinner = new JSpinner();

        speedSpinnerModel = new SpinnerNumberModel(0, 0, MAX_SPEED, 1);
        speedSpinner.setModel(speedSpinnerModel);
        speedSpinner.setFocusable(false);

        speedStep128Button = new JRadioButton(Bundle.getMessage("Button128SS"));
        speedStep28Button = new JRadioButton(Bundle.getMessage("Button28SS"));
        speedStep27Button = new JRadioButton(Bundle.getMessage("Button27SS"));
        speedStep14Button = new JRadioButton(Bundle.getMessage("Button14SS"));

        initGUI();
        pack();
    }

    /**
     * Get notification that a throttle has been found as we requested.
     *
     * @param t An instantiation of the DccThrottle with the address requested.
     */
    public void notifyThrottleFound(DccThrottle t) {
        if (log.isDebugEnabled()) {
            log.debug("control panel received new throttle");
        }
        _throttle = t;
        this.setEnabled(true);
        this.setSpeedValues((int) t.getSpeedIncrement(),
                (int) t.getSpeedSetting());
        this.setSpeedSteps(t.getSpeedStepMode());
        _throttle.addPropertyChangeListener(this);
    }

    @Override
    public void dispose() {
        if (_throttle != null) {
            _throttle.removePropertyChangeListener(this);
            _throttle = null;
        }
        super.dispose();
    }

    // update the state of this panel if any of the properties change
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (log.isDebugEnabled()) {
            log.debug("propertyChange: " + e.getPropertyName() + ", newValue= "
                    + e.getNewValue().toString());
        }
        if (e.getPropertyName().equals(Throttle.SPEEDSETTING)) {
            internalAdjust = true;
            float speed = ((Float) e.getNewValue()).floatValue();
//            speedSetting(speed);
            _throttleFrame.setSpeedSetting(speed);
            if (_emergencyStop && speed < 0.0F) {
                _throttleFrame.stopRunTrain();
            }
            if (speed != 0.0F) {
                _emergencyStop = (speed < 0.0F);
            }
        } else if (e.getPropertyName().equals(Throttle.SPEEDSTEPS)) {
            SpeedStepMode steps = (SpeedStepMode)e.getNewValue();
            setSpeedSteps(steps);
            _throttleFrame.setSpeedStepMode(steps);
        } else if (e.getPropertyName().equals(Throttle.ISFORWARD)) {
            boolean Forward = ((Boolean) e.getNewValue()).booleanValue();
            _throttleFrame.setButtonForward(Forward);
        } else {
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
        speedStep128Button.setEnabled(isEnabled);
        speedStep28Button.setEnabled(isEnabled);
        speedStep27Button.setEnabled(isEnabled);
        speedStep14Button.setEnabled(isEnabled);
        if (isEnabled) {
            configureAvailableSpeedStepModes();
        }
        speedControllerEnable = isEnabled;
        if (_displaySlider) {
            speedSpinner.setEnabled(false);
            speedSlider.setEnabled(isEnabled);
        } else {
            speedSpinner.setEnabled(isEnabled);
            speedSlider.setEnabled(false);
        }
        super.setEnabled(isEnabled);
    }

    /**
     * Set the GUI to match the speed steps of the current address.
     *
     * @param steps Desired number of speed steps. One of 14,27,28,or 128.
     *              Defaults to 128 step mode
     */
    public void setSpeedSteps(SpeedStepMode steps) {
        // Save the old speed as a float
        float oldSpeed = (speedSlider.getValue() / (MAX_SPEED * 1.0f));

        if (steps == SpeedStepMode.NMRA_DCC_14) {
            speedStep14Button.setSelected(true);
            speedStep27Button.setSelected(false);
            speedStep28Button.setSelected(false);
            speedStep128Button.setSelected(false);
            MAX_SPEED = 14;
        } else if (steps == SpeedStepMode.NMRA_DCC_27) {
            speedStep14Button.setSelected(false);
            speedStep27Button.setSelected(true);
            speedStep28Button.setSelected(false);
            speedStep128Button.setSelected(false);
            MAX_SPEED = 27;
        } else if (steps == SpeedStepMode.NMRA_DCC_28) {
            speedStep14Button.setSelected(false);
            speedStep27Button.setSelected(false);
            speedStep28Button.setSelected(true);
            speedStep128Button.setSelected(false);
            MAX_SPEED = 28;
        } else {
            speedStep14Button.setSelected(false);
            speedStep27Button.setSelected(false);
            speedStep28Button.setSelected(false);
            speedStep128Button.setSelected(true);
            MAX_SPEED = 126;
        }
        //_speedStepMode=steps;

        // rescale the speed slider to match the new speed step mode
        internalAdjust = true;
        speedSlider.setMaximum(MAX_SPEED);
        speedSlider.setValue((int) (oldSpeed * MAX_SPEED));
        speedSlider.setMajorTickSpacing(MAX_SPEED / 2);
        java.util.Hashtable<Integer, JLabel> labelTable = new java.util.Hashtable<Integer, JLabel>();
        labelTable.put(Integer.valueOf(MAX_SPEED / 2), new JLabel("50%"));
        labelTable.put(Integer.valueOf(MAX_SPEED), new JLabel("100%"));
        labelTable.put(Integer.valueOf(0), new JLabel(Bundle.getMessage("ButtonStop")));
        speedSlider.setLabelTable(labelTable);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);

        internalAdjust = true;
        speedSpinnerModel.setMaximum(Integer.valueOf(MAX_SPEED));
        speedSpinnerModel.setMinimum(Integer.valueOf(0));
        // rescale the speed value to match the new speed step mode
        speedSpinnerModel.setValue(Integer.valueOf(speedSlider.getValue()));
    }

    /**
     * Set the Speed Control selection method
     *
     * @param displaySlider integer value. possible values: SLIDERDISPLAY = use
     *                      speed slider display STEPDISPLAY = use speed step
     *                      display
     */
    public void setSpeedController(boolean displaySlider) {
        if (displaySlider) {
            sliderPanel.setVisible(true);
            speedSlider.setEnabled(speedControllerEnable);
            spinnerPanel.setVisible(false);
            if (speedSpinner != null) {
                speedSpinner.setEnabled(false);
            }
        } else {
            sliderPanel.setVisible(false);
            speedSlider.setEnabled(false);
            spinnerPanel.setVisible(true);
            speedSpinner.setEnabled(speedControllerEnable);
        }
        _displaySlider = displaySlider;
    }

    /**
     * Set the GUI to match that the loco speed.
     *
     * @param speedIncrement : TODO
     * @param speed          The speed value of the loco.
     */
    public void setSpeedValues(int speedIncrement, int speed) {
        //this.speedIncrement = speedIncrement;
        speedSlider.setValue(speed * speedIncrement);
        // Spinner Speed should be the raw integer speed value
        if (speedSpinner != null) {
            speedSpinnerModel.setValue(Integer.valueOf(speed));
        }
    }

    /**
     * Create, initialize and place GUI components.
     */
    private void initGUI() {
        JPanel mainPanel = new JPanel();
        this.setContentPane(mainPanel);
        mainPanel.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        speedControlPanel = new JPanel();
        speedControlPanel.setLayout(new BoxLayout(speedControlPanel, BoxLayout.X_AXIS));
        this.getContentPane().add(speedControlPanel, BorderLayout.CENTER);
        sliderPanel = new JPanel();
        sliderPanel.setLayout(new GridBagLayout());

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

        sliderPanel.add(speedSlider, constraints);
        //this.getContentPane().add(sliderPanel,BorderLayout.CENTER);
        speedControlPanel.add(sliderPanel);
        speedSlider.setOrientation(JSlider.VERTICAL);
        speedSlider.setMajorTickSpacing(MAX_SPEED / 2);
        java.util.Hashtable<Integer, JLabel> labelTable = new java.util.Hashtable<Integer, JLabel>();
        labelTable.put(Integer.valueOf(MAX_SPEED / 2), new JLabel("50%"));
        labelTable.put(Integer.valueOf(MAX_SPEED), new JLabel("100%"));
        labelTable.put(Integer.valueOf(0), new JLabel(Bundle.getMessage("ButtonStop")));
        speedSlider.setLabelTable(labelTable);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        // remove old actions
        speedSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (!internalAdjust) {
                    boolean doIt = false;
                    if (!speedSlider.getValueIsAdjusting()) {
                        doIt = true;
                    } else if (System.currentTimeMillis() - lastTrackedSliderMovementTime >= trackSliderMinInterval) {
                        doIt = true;
                        lastTrackedSliderMovementTime = System.currentTimeMillis();
                    }
                    if (doIt) {
                        float newSpeed = (speedSlider.getValue() / (MAX_SPEED * 1.0f));
                        if (log.isDebugEnabled()) {
                            log.debug("stateChanged: slider pos: " + speedSlider.getValue() + " speed: " + newSpeed);
                        }
                        _throttle.setSpeedSetting(newSpeed);
                        if (speedSpinner != null) {
                            speedSpinnerModel.setValue(Integer.valueOf(speedSlider.getValue()));
                        }
                    }
                } else {
                    internalAdjust = false;
                }
            }
        });

        spinnerPanel = new JPanel();
        spinnerPanel.setLayout(new GridBagLayout());

        if (speedSpinner != null) {
            spinnerPanel.add(speedSpinner, constraints);
        }
        //this.getContentPane().add(spinnerPanel,BorderLayout.CENTER);
        speedControlPanel.add(spinnerPanel);
        // remove old actions
        if (speedSpinner != null) {
            speedSpinner.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    if (!internalAdjust) {
                        float newSpeed = ((Integer) speedSpinner.getValue()).floatValue() / (MAX_SPEED * 1.0f);
                        if (log.isDebugEnabled()) {
                            log.debug("stateChanged: spinner pos: " + speedSpinner.getValue() + " speed: " + newSpeed);
                        }
                        _throttle.setSpeedSetting(newSpeed);
                        //_throttleFrame.setSpeedSetting( newSpeed );
                        speedSlider.setValue(((Integer) speedSpinner.getValue()).intValue());
                    } else {
                        internalAdjust = false;
                    }
                }
            });
        }

        ButtonGroup speedStepButtons = new ButtonGroup();
        speedStepButtons.add(speedStep128Button);
        speedStepButtons.add(speedStep28Button);
        speedStepButtons.add(speedStep27Button);
        speedStepButtons.add(speedStep14Button);
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridy = 1;
        spinnerPanel.add(speedStep128Button, constraints);
        constraints.gridy = 2;
        spinnerPanel.add(speedStep28Button, constraints);
        constraints.gridy = 3;
        spinnerPanel.add(speedStep27Button, constraints);
        constraints.gridy = 4;
        spinnerPanel.add(speedStep14Button, constraints);

        speedStep14Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setSpeedSteps(SpeedStepMode.NMRA_DCC_14);
                _throttle.setSpeedStepMode(SpeedStepMode.NMRA_DCC_14);
            }
        });

        speedStep27Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setSpeedSteps(SpeedStepMode.NMRA_DCC_27);
                _throttle.setSpeedStepMode(SpeedStepMode.NMRA_DCC_27);
            }
        });

        speedStep28Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setSpeedSteps(SpeedStepMode.NMRA_DCC_28);
                _throttle.setSpeedStepMode(SpeedStepMode.NMRA_DCC_28);
            }
        });

        speedStep128Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setSpeedSteps(SpeedStepMode.NMRA_DCC_128);
                _throttle.setSpeedStepMode(SpeedStepMode.NMRA_DCC_128);
            }
        });
        // set by default which speed selection method is on top
        setSpeedController(_displaySlider);
    }

    public void accelerate1() {
        if (speedSlider.isEnabled()) {
            if (speedSlider.getValue() != speedSlider.getMaximum()) {
                speedSlider.setValue(speedSlider.getValue() + 1);
            }
        } else if (speedSpinner != null && speedSpinner.isEnabled()) {
            if (((Integer) speedSpinner.getValue()).intValue() < ((Integer) speedSpinnerModel.getMaximum()).intValue()
                    && ((Integer) speedSpinner.getValue()).intValue() >= ((Integer) speedSpinnerModel.getMinimum()).intValue()) {
                speedSpinner.setValue(Integer.valueOf(((Integer) speedSpinner.getValue()).intValue() + 1));
            }
        }
    }

    public void accelerate10() {
        if (speedSlider.isEnabled()) {
            if (speedSlider.getValue() != speedSlider.getMaximum()) {
                speedSlider.setValue(speedSlider.getValue() + 10);
            }
        } else if (speedSpinner != null && speedSpinner.isEnabled()) {
            if (((Integer) speedSpinner.getValue()).intValue() < ((Integer) speedSpinnerModel.getMaximum()).intValue()
                    && ((Integer) speedSpinner.getValue()).intValue() >= ((Integer) speedSpinnerModel.getMinimum()).intValue()) {
                Integer speedvalue = Integer.valueOf(((Integer) speedSpinner.getValue()).intValue() + 10);
                if (speedvalue.intValue() < ((Integer) speedSpinnerModel.getMaximum()).intValue()) {
                    speedSpinner.setValue(speedvalue);
                } else {
                    speedSpinner.setValue(speedSpinnerModel.getMaximum());
                }
            }
        }
    }

    public void decelerate1() {
        if (speedSlider.isEnabled()) {
            if (speedSlider.getValue() != speedSlider.getMinimum()) {
                speedSlider.setValue(speedSlider.getValue() - 1);
            }
        } else if (speedSpinner != null && speedSpinner.isEnabled()) {
            if (((Integer) speedSpinner.getValue()).intValue() <= ((Integer) speedSpinnerModel.getMaximum()).intValue()
                    && ((Integer) speedSpinner.getValue()).intValue() > ((Integer) speedSpinnerModel.getMinimum()).intValue()) {
                speedSpinner.setValue(Integer.valueOf(((Integer) speedSpinner.getValue()).intValue() - 1));
            }
        }
    }

    public void decelerate10() {
        if (speedSlider.isEnabled()) {
            if (speedSlider.getValue() != speedSlider.getMinimum()) {
                speedSlider.setValue(speedSlider.getValue() - 10);
            }
        } else if (speedSpinner != null && speedSpinner.isEnabled()) {
            if (((Integer) speedSpinner.getValue()).intValue() <= ((Integer) speedSpinnerModel.getMaximum()).intValue()
                    && ((Integer) speedSpinner.getValue()).intValue() > ((Integer) speedSpinnerModel.getMinimum()).intValue()) {
                Integer speedvalue = Integer.valueOf(((Integer) speedSpinner.getValue()).intValue() - 10);
                if (speedvalue.intValue() > ((Integer) speedSpinnerModel.getMinimum()).intValue()) {
                    speedSpinner.setValue(speedvalue);
                } else {
                    speedSpinner.setValue(speedSpinnerModel.getMinimum());
                }
            }
        }
    }

    /*    private void speedSetting(float speed) {
     // multiply by MAX_SPEED, and round to find the new slider setting.
     int newSliderSetting = java.lang.Math.round(speed * MAX_SPEED) ;
     if (log.isDebugEnabled()) {log.debug( "speedSetting: new speed float: " + speed + " slider pos: " + newSliderSetting ) ;}
     speedSlider.setValue( newSliderSetting );
     speedSlider.repaint();
     if(speedSpinner!=null)
     speedSpinner.setValue(Integer.valueOf(newSliderSetting));
     }*/
    /**
     * Configure the active Speed Step modes based on what is supported by the
     * DCC system
     */
    private void configureAvailableSpeedStepModes() {
        EnumSet<SpeedStepMode> modes = jmri.InstanceManager.throttleManagerInstance().supportedSpeedModes();
        if (modes.contains(SpeedStepMode.NMRA_DCC_128)) {
            speedStep128Button.setEnabled(true);
        } else {
            speedStep128Button.setEnabled(false);
        }
        if (modes.contains(SpeedStepMode.NMRA_DCC_28)) {
            speedStep28Button.setEnabled(true);
        } else {
            speedStep28Button.setEnabled(false);
        }
        if (modes.contains(SpeedStepMode.NMRA_DCC_27)) {
            speedStep27Button.setEnabled(true);
        } else {
            speedStep27Button.setEnabled(false);
        }
        if (modes.contains(SpeedStepMode.NMRA_DCC_14)) {
            speedStep14Button.setEnabled(true);
        } else {
            speedStep14Button.setEnabled(false);
        }
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(ControlPanel.class);
}

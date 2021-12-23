package jmri.jmrit.logix;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.ButtonGroup;
import javax.swing.JDesktopPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.PowerManager;
import jmri.SpeedStepMode;
import jmri.jmrit.throttle.FunctionButton;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JFrame to contain throttle elements such as speed control, function panel.
 * It keeps a record of the throttle commands for playback later.
 * <p>
 *
 * Modeled on package jmri.jmrit.throttle by
 *
 * @author Glen Oberhauser
 * @author Bob Jacobsen Copyright 2008
 *
 * @author Pete Cressman Copyright 2009, 2020
 */
public class LearnThrottleFrame extends JmriJFrame { //implements java.beans.PropertyChangeListener {

    private WarrantFrame _warrantFrame;
    private PowerManager powerMgr = null;
    private LearnControlPanel _controlPanel;
    private LearnFunctionPanel _functionPanel;
    private LearnSpeedPanel _speedPanel;

    /**
     * Default constructor
     * @param warrantFrame caller
     */
    public LearnThrottleFrame(WarrantFrame warrantFrame) {
        super(false, false);
        _warrantFrame = warrantFrame;
        powerMgr = InstanceManager.getNullableDefault(jmri.PowerManager.class);
        if (powerMgr == null) {
            log.info("No power manager instance found, panel not active");
        }
        initGUI();
        setVisible(true);
    }

    /**
     * Get notification that a throttle has been found as you requested.
     *
     * @param t An instantiation of the DccThrottle with the address requested.
     */
    public void notifyThrottleFound(DccThrottle t) {
        if (log.isDebugEnabled()) {
            log.debug("notifyThrottleFound address= {} class= {}",t.getLocoAddress(),t.getClass().getName());
        }
        _controlPanel.notifyAddressThrottleFound(t);
        _functionPanel.notifyAddressThrottleFound(t);
        _speedPanel.notifyAddressThrottleFound(t);
        setSpeedSetting(0.0f);      // be sure loco is stopped.
        setButtonForward(t.getIsForward());
        String name = _warrantFrame.getTrainName();
        if (name == null || name.isEmpty()) {
            jmri.jmrit.roster.RosterEntry re = _warrantFrame._speedUtil.getRosterEntry();
            if (re != null) {
                name = re.getId();
            } else {
                name = t.getLocoAddress().toString();
            }
        } else {
            name =name +" - " + t.getLocoAddress().toString();
        }
        setTitle(name);
    }

    private void initGUI() {
        setTitle("Throttle");
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                _warrantFrame.close();
                dispose();
            }
        });
        initializeMenu();

        _controlPanel = new LearnControlPanel(this);
        _controlPanel.setVisible(true);
        _controlPanel.setEnabled(false);
        _controlPanel.setTitle(Bundle.getMessage("speed"));
        _controlPanel.setSize(_controlPanel.getPreferredSize());

        int width = 3 * (FunctionButton.getButtonWidth()) + 2 * 3 * 5 + 11;   // = 192
        int height = 9 * (FunctionButton.getButtonHeight()) + 2 * 6 * 5 + 20; // FunctionButton.BUT_IMG_SIZE = 45
        _functionPanel = new LearnFunctionPanel(this);
        _functionPanel.setSize(width, height);
        _functionPanel.setVisible(true);
        _functionPanel.setEnabled(false);
        _functionPanel.setTitle(Bundle.getMessage("setFunction"));

        _speedPanel = new LearnSpeedPanel(_warrantFrame.getWarrant());
        _speedPanel.setSize(_functionPanel.getWidth(), _controlPanel.getHeight() - _functionPanel.getHeight());
        _speedPanel.setVisible(true);
        _speedPanel.setClosable(true);
        _speedPanel.setTitle(java.util.ResourceBundle.getBundle("jmri/jmrit/throttle/ThrottleBundle").getString("ThrottleMenuViewSpeedPanel"));

        _controlPanel.setLocation(0, 0);
        _functionPanel.setLocation(_controlPanel.getWidth(), 0);
        _speedPanel.setLocation(_controlPanel.getWidth(), _functionPanel.getHeight());

        JDesktopPane desktop = new JDesktopPane();
        getContentPane().add(desktop);
        desktop.add(_controlPanel);
        desktop.add(_functionPanel);
        desktop.add(_speedPanel);

        desktop.setPreferredSize(new Dimension(
                _controlPanel.getWidth() + _functionPanel.getWidth(), _controlPanel.getHeight()));

        setResizable(false);
        jmri.util.PlaceWindow.getDefault().nextTo(_warrantFrame, null, this);
        pack();
    }

    /**
     * Set up View, Edit and Power Menus
     */
    private void initializeMenu() {
        JMenu speedControl = new JMenu(Bundle.getMessage("SpeedControl"));
        ButtonGroup buttonGroup = new ButtonGroup();
        JRadioButtonMenuItem displaySlider = new JRadioButtonMenuItem(Bundle.getMessage("ButtonDisplaySpeedSlider"));
        displaySlider.addActionListener((ActionEvent e)->_controlPanel.setSpeedController(jmri.jmrit.throttle.ControlPanel.SLIDERDISPLAYCONTINUOUS));
        displaySlider.setSelected(true);
        buttonGroup.add(displaySlider);
        speedControl.add(displaySlider);
        JRadioButtonMenuItem displaySteps = new JRadioButtonMenuItem(Bundle.getMessage("ButtonDisplaySpeedSteps"));
        displaySteps.addActionListener((ActionEvent e)->_controlPanel.setSpeedController(jmri.jmrit.throttle.ControlPanel.STEPDISPLAY));
        buttonGroup.add(displaySteps);
        speedControl.add(displaySteps);
        this.setJMenuBar(new JMenuBar());
        this.getJMenuBar().add(speedControl);

       if (powerMgr != null) {
            JMenu powerMenu = new JMenu(Bundle.getMessage("ThrottleMenuPower"));
            JMenuItem powerOn = new JMenuItem(Bundle.getMessage("ThrottleMenuPowerOn"));
            powerMenu.add(powerOn);
            powerOn.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        powerMgr.setPower(PowerManager.ON);
                    } catch (JmriException e1) {
                        log.error("Error when setting power {}", e1);
                    }
                }
            });

            JMenuItem powerOff = new JMenuItem(Bundle.getMessage("ThrottleMenuPowerOff"));
            powerMenu.add(powerOff);
            powerOff.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        powerMgr.setPower(PowerManager.OFF);
                    } catch (JmriException e1) {
                        log.error("Error when setting power", e1);
                    }
                }
            });

            this.getJMenuBar().add(powerMenu);
        }
        // add help selection
        addHelpMenu("package.jmri.jmrit.throttle.ThrottleFrame", true);
    }

    @Override
    public void dispose() {
        _controlPanel.destroy();
        _functionPanel.destroy();
        _speedPanel.destroy();
        _controlPanel.dispose();
        _functionPanel.dispose();
        super.dispose();
    }

    /* Record throttle commands that have been sent to the throttle from ControlPanel */

    protected void setSpeedSetting(float speed) {
        _warrantFrame.setSpeedCommand(speed);
    }

    protected void setSpeedStepMode(SpeedStepMode speedStep) {
        _warrantFrame.setThrottleCommand("SpeedStep", speedStep.name);
    }

    protected void setFunctionState(String FNum, boolean isSet) {
        _warrantFrame.setThrottleCommand(FNum, Boolean.toString(isSet));
    }

    protected void setFunctionLock(String FMom, boolean isLockable) {
        _warrantFrame.setThrottleCommand(FMom, Boolean.toString(isLockable));
    }

    protected void setButtonForward(boolean isForward) {
        _warrantFrame.setThrottleCommand("Forward", Boolean.toString(isForward));
    }

    private static final Logger log = LoggerFactory.getLogger(LearnThrottleFrame.class);

}

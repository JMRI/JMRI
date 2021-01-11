package jmri.jmrit.logix;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.BoxLayout;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.DccThrottle;
import jmri.Throttle;
import jmri.implementation.SignalSpeedMap;

/**
 * A JInternalFrame that contains a JSlider to control loco speed, and buttons
 * for forward, reverse and STOP.
 *
 * @author Pete Cressman Copyright 2020
 */
public class LearnSpeedPanel extends JInternalFrame implements java.beans.PropertyChangeListener {

    private Warrant _warrant;
    private DccThrottle _throttle;
    private float _currentThrottleValue = 0.0f;
    private JLabel _scaleSpeed;

    LearnSpeedPanel(Warrant w) {
        _warrant = w;
        initGUI();
    }

    private void initGUI() {
        JPanel mainPanel = new JPanel();
        this.setContentPane(mainPanel);
        mainPanel.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setFont(new Font("", Font.PLAIN, 32));
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setOpaque(false);
        OBlock blk = _warrant.getBlockAt(0);
        String name;
        if (blk != null) {
            name = blk.getDisplayName();
        } else {
            name = _warrant.getDisplayName();
        }
        _scaleSpeed = new JLabel(Bundle.getMessage("TrainReady", _warrant.getTrainName(), name));
        panel.add(_scaleSpeed);

        mainPanel.add(panel, BorderLayout.CENTER);
    }

    public void notifyAddressThrottleFound(DccThrottle t) {
        if (log.isDebugEnabled()) {
            log.debug("control panel received new throttle");
        }
        _throttle = t;
        _warrant.getSpeedUtil().setIsForward(_throttle.getIsForward());

        _throttle.addPropertyChangeListener(this);
        if (log.isDebugEnabled()) {
            jmri.DccLocoAddress Address = (jmri.DccLocoAddress) _throttle.getLocoAddress();
            log.debug("new address is {}", Address.toString());
        }
    }

    /**
     * update the state of this panel if direction or speed change
     */
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals(Throttle.SPEEDSETTING)) {
            _currentThrottleValue = ((Float) e.getNewValue()).floatValue();
            _scaleSpeed.setText(setSpeed());
        } else if (e.getPropertyName().equals(Throttle.ISFORWARD)) {
            _warrant.getSpeedUtil().setIsForward((boolean) e.getNewValue());
            _scaleSpeed.setText(setSpeed());
        }
        if (log.isDebugEnabled()) {
            log.debug("Property change event received {} / {}", e.getPropertyName(), e.getNewValue());
        }
    }

    /**
     * @return a string for displaying speed if available
     */
    private String setSpeed() {
        float trackSpeed = _warrant.getSpeedUtil().getTrackSpeed(_currentThrottleValue);
        float speed = 0;
        String units;
        SignalSpeedMap speedMap = jmri.InstanceManager.getDefault(SignalSpeedMap.class);
        switch (speedMap.getInterpretation()) {
            case SignalSpeedMap.PERCENT_NORMAL:
            case SignalSpeedMap.PERCENT_THROTTLE:
                units = Bundle.getMessage("percentThrottle");
                speed = _currentThrottleValue * 100;
                break;
            case SignalSpeedMap.SPEED_MPH:
                units = Bundle.getMessage("speedMph");
                speed = trackSpeed * speedMap.getLayoutScale() * 2.2369363f;
                break;
            case SignalSpeedMap.SPEED_KMPH:
                units = Bundle.getMessage("speedKmph");
                speed = trackSpeed * speedMap.getLayoutScale() * 3.6f;
                break;
            default:
                units = "Error";
                log.error("Unknown speed interpretation {}", speedMap.getInterpretation());
        }
        return Bundle.getMessage("atSpeed", 
                Bundle.getMessage("speedmm", Math.round(trackSpeed*1000)),
                Math.round(speed), units);
    }

    /**
     * "Destructor"
     */
    public void destroy() {
        if (_throttle != null) {
            _throttle.removePropertyChangeListener(this);
            if (log.isDebugEnabled()) {
                jmri.DccLocoAddress Address = (jmri.DccLocoAddress) _throttle.getLocoAddress();
                log.debug("Address {} destroyed", Address.toString());
            }
            _throttle = null;
        }
    }
    

    private static final Logger log = LoggerFactory.getLogger(LearnSpeedPanel.class);
}

package jmri.jmrit.logix;

import java.awt.Font;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import jmri.DccThrottle;
import jmri.DccLocoAddress;
import jmri.Throttle;
import jmri.implementation.SignalSpeedMap;

/**
 * A JInternalFrame that contains a JSlider to control loco speed, and buttons
 * for forward, reverse and STOP.
 *
 * @author Pete Cressman Copyright 2020
 */
public class LearnSpeedPanel extends JInternalFrame implements java.beans.PropertyChangeListener {

    private final Warrant _warrant;
    private JLabel _scaleSpeed;
    private JLabel _direction;

    LearnSpeedPanel(Warrant w) {
        _warrant = w;
        initGUI();
    }

    private void initGUI() {
        JPanel mainPanel = new JPanel();
        this.setContentPane(mainPanel);
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
        _direction = new JLabel(Bundle.getMessage("forward"));
        panel.add(_direction);
        panel.add(Box.createHorizontalStrut(WarrantRoute.STRUT_SIZE));
        _scaleSpeed = new JLabel(Bundle.getMessage("TrainReady", _warrant.getTrainName(), name));
        panel.add(_scaleSpeed);

        mainPanel.add(panel);
    }

    public void notifyAddressThrottleFound(DccThrottle throttle) {
        _warrant.getSpeedUtil().setThrottle(throttle);

        throttle.addPropertyChangeListener(this);
        _scaleSpeed.setText(setSpeed(throttle.getSpeedSetting()));
        if (log.isDebugEnabled()) {
            DccLocoAddress address = (DccLocoAddress) throttle.getLocoAddress();
            log.debug("new address is {}", address );
        }
        setSpeed(0);
    }

    /**
     * Update the state of this panel if direction or speed change.
     * {@inheritDoc }
     */
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals(Throttle.SPEEDSETTING)) {
            _scaleSpeed.setText(setSpeed((Float) e.getNewValue()));
        } else if (e.getPropertyName().equals(Throttle.ISFORWARD)) {
            String direction;
            if ( Boolean.TRUE.equals(e.getNewValue()) ) {
                direction = Bundle.getMessage("forward");
            } else {
                direction = Bundle.getMessage("reverse");
            }
            _direction.setText(direction);
        }
        if (log.isDebugEnabled()) {
            log.debug("Property change event received {} / {}", e.getPropertyName(), e.getNewValue());
        }
    }

    /**
     * @return a string for displaying speed if available
     */
    private String setSpeed(float throttleValue) {
        float trackSpeed = _warrant.getSpeedUtil().getTrackSpeed(throttleValue);
        float speed = 0;
        String units;
        SignalSpeedMap speedMap = jmri.InstanceManager.getDefault(SignalSpeedMap.class);
        switch (speedMap.getInterpretation()) {
            case SignalSpeedMap.PERCENT_NORMAL:
            case SignalSpeedMap.PERCENT_THROTTLE:
                units = Bundle.getMessage("percentThrottle");
                speed = throttleValue * 100;
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
        DccThrottle throttle = _warrant.getSpeedUtil().getThrottle();
        if (throttle != null) {
            throttle.removePropertyChangeListener(this);
            if (log.isDebugEnabled()) {
                DccLocoAddress address = (DccLocoAddress) throttle.getLocoAddress();
                log.debug("Address {} destroyed", address);
            }
//            _throttle = null;
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LearnSpeedPanel.class);

}

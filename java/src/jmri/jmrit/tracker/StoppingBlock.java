package jmri.jmrit.tracker;

import jmri.Block;
import jmri.SignalHead;
import jmri.Throttle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stop a train in a block if required.
 * <p>
 * Watches a Block object that is passing around a Throttle object as its
 * value. When the Block goes OCCUPIED, check whether a signal is telling the
 * train to stop; if so, force the Throttle to zero speed.
 * <p>
 * This contains multiple SignalHead objects, each associated with a Path that
 * contains one or more BeanSettings (e.g. Turnout positions) and directions.
 * When needed, this consults the paths to see which one is active (has its
 * Turnouts set) and corresponds to the current direction of the block. There
 * should be exactly one of these, which will then identify which signal to
 * monitor.
 * <p>
 * Limitations:
 * <ul>
 * <li>Current implementation does not protect against changing direction and
 * backing out of the block
 * <li>Should track speed at time of stop and restore it on restart (or should
 * it not restart? Optional restart?)
 * </ul>
 *
 * @author Bob Jacobsen Copyright (C) 2006
 */
public class StoppingBlock {

    public StoppingBlock(Block b) {
        block = b;

        // set a listener in the block
        block.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                handleBlockChange(e);
            }
        });
    }

    void handleBlockChange(java.beans.PropertyChangeEvent e) {
        // check for going occupied
        if (e.getPropertyName().equals("state") && e.getNewValue().equals(Integer.valueOf(Block.OCCUPIED))) {
            if (sig1 == null) {
                return;
            }

            if (direction != block.getDirection()) {
                return;  // no interesting
            }
            int val = fastestAppearance();
            if (log.isDebugEnabled()) {
                log.debug("Block " + block.getSystemName() + " occupied with " + val);
            }

            if (val == SignalHead.RED) {
                doStop();
            }
            if (val == SignalHead.YELLOW) {
                doSlow();
            }
        }
    }

    void handleSignalChange(java.beans.PropertyChangeEvent e) {
        // if currently have a loco present and stopped,
        // consider changing speed
        if ((block.getValue() != null) && block.getState() == (Block.OCCUPIED)) {

            if (sig1 == null) {
                return;
            }

            if (direction != block.getDirection()) {
                return;  // not interesting
            }
            int val = fastestAppearance();
            if (log.isDebugEnabled()) {
                log.debug("Block " + block.getSystemName() + " signal change to " + val);
            }

            if (val == SignalHead.YELLOW) {
                doSlow();
            }
            if (val == SignalHead.GREEN) {
                doRestart();
            }
        }
    }

    public void addSignal(SignalHead s, int dir) {
        sig1 = s;
        direction = dir;

        sig1.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                handleSignalChange(e);
            }
        });
    }

    public void addSignal(SignalHead s1, SignalHead s2, int dir) {
        addSignal(s1, dir);
        sig2 = s2;
        sig2.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                handleSignalChange(e);
            }
        });
    }

    int fastestAppearance() {
        if (sig1 == null) {
            log.error("Should not get null in fastestAppearance");
            return 0;
        }
        if (sig2 == null) {
            return sig1.getAppearance();
        } else {
            return Math.max(sig1.getAppearance(), sig2.getAppearance());
        }
    }

    /**
     * Perform the stop operation
     */
    void doStop() {
        if (log.isDebugEnabled()) {
            log.debug("Block " + block.getSystemName() + " speed being set to stop");
        }
        setSpeed(0.0f, false, false, false);  // bell on
    }

    void doSlow() {
        if (log.isDebugEnabled()) {
            log.debug("Block " + block.getSystemName() + " speed being set to slow");
        }
        setSpeed(slow, false, false, false);  // bell off
    }

    void doRestart() {
        if (log.isDebugEnabled()) {
            log.debug("Block " + block.getSystemName() + " speed being set to run");
        }
        setSpeed(fast, false, false, false);  // bell off
    }

    void setSpeed(float speed, boolean f1, boolean f2, boolean f3) {
        Object o = block.getValue();
        if (o == null) {
            log.error("Block " + block.getSystemName() + " contained no Throttle object");
            return;
        }
        try {
            Throttle t = (Throttle) block.getValue();
            t.setSpeedSetting(speed);
            t.setF1(f1);
            t.setF2(f2);
        } catch (ClassCastException e) {
            log.error("Block " + block.getSystemName() + " did not contain object of Throttle type: " + e);
        }
    }

    public void setSpeeds(float s, float f) {
        slow = s;
        fast = f;
    }

    // data members
    Block block;
    SignalHead sig1;
    SignalHead sig2;
    int direction;
    float slow = 0.3f;
    float fast = 0.6f;

    private final static Logger log = LoggerFactory.getLogger(StoppingBlock.class);

}

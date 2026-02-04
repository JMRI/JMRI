package jmri.jmrit.tracker;

import jmri.Block;
import jmri.SignalHead;
import jmri.Throttle;

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
        block.addPropertyChangeListener(this::handleBlockChange);
    }

    void handleBlockChange(java.beans.PropertyChangeEvent e) {
        // check for going occupied
        if ( Block.PROPERTY_STATE.equals(e.getPropertyName()) && e.getNewValue().equals(Block.OCCUPIED)) {
            if (sig1 == null) {
                return;
            }

            if (direction != block.getDirection()) {
                return;  // no interesting
            }
            int val = fastestAppearance();
            if (log.isDebugEnabled()) {
                log.debug("Block {} occupied with {}", block.getSystemName(), val);
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
            log.debug("Block {} signal change to {}", block, val);

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

        sig1.addPropertyChangeListener(this::handleSignalChange);
    }

    public void addSignal(SignalHead s1, SignalHead s2, int dir) {
        addSignal(s1, dir);
        sig2 = s2;
        sig2.addPropertyChangeListener(this::handleSignalChange);
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
        log.debug("Block {} speed being set to stop", block.getDisplayName());
        setSpeed(0.0f, false, false, false);  // bell on
    }

    void doSlow() {
        log.debug("Block {} speed being set to slow", block.getDisplayName());
        setSpeed(slow, false, false, false);  // bell off
    }

    void doRestart() {
        log.debug("Block {} speed being set to run", block.getDisplayName());
        setSpeed(fast, false, false, false);  // bell off
    }

    void setSpeed(float speed, boolean f1, boolean f2, boolean f3) {
        Object o = block.getValue();
        if (o == null) {
            log.error("Block {} contained no Throttle object", block.getDisplayName());
            return;
        }
        if ( o instanceof Throttle ) {
            Throttle t = (Throttle) o;
            t.setSpeedSetting(speed);
            t.setFunction(1, f1);
            t.setFunction(2, f2);
        } else {
            log.error("Block {} did not contain object of Throttle type, was {} , a {}",
                block.getDisplayName(), o, o.getClass());
        }
    }

    /**
     * Set speeds.
     * @param s the slow speed, default 0.3
     * @param f the fast speed, default 0.6
     */
    public void setSpeeds(float s, float f) {
        slow = s;
        fast = f;
    }

    // data members
    Block block;
    SignalHead sig1;
    SignalHead sig2;
    int direction;
    private float slow = 0.3f;
    private float fast = 0.6f;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StoppingBlock.class);

}

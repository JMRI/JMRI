package rand;

import jmri.*;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public abstract class TurnoutOp {
    private static final Logger logger = LoggerFactory.getLogger(TurnoutOp.class);
    private final String name;
    private final Turnout turnout;

    public TurnoutOp(String name) {
        this.name = name;
        this.turnout = InstanceManager.getDefault(TurnoutManager.class)
                .getByUserName(name);
        if (this.turnout == null) {
            logger.error("Could not find turnout " + name);
        }
    }

    abstract void perform();

    protected void throwTurnout() {
        try {
            turnout.setState(Turnout.THROWN);
        } catch (JmriException e) {
            logger.error("Failed to throw " + name + ": ", e);
        }
    }

    protected void closeTurnout() {
        try {
            turnout.setState(Turnout.CLOSED);
        } catch (JmriException e) {
            logger.error("Failed to throw " + name + ": ", e);
        }
    }

}

package apps.startup;

import jmri.JmriException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Startup action that causes JMRI to pause before triggering the next startup
 * action.
 *
 * @author Randall Wood (c) 2016
 */
public class StartupPauseModel extends AbstractStartupModel {

    public static final int DEFAULT_DELAY = 10;
    private int delay = -1; // default to invalid duration
    private final static Logger log = LoggerFactory.getLogger(StartupPauseModel.class);

    @Override
    public String getName() {
        return Bundle.getMessage("StartupPauseModel.name", this.getDelay()); // NOI18N
    }

    /**
     * {@inheritDoc}
     *
     * @return true if duration greater than or equal to 0; false otherwise
     */
    @Override
    public boolean isValid() {
        return this.getDelay() >= 0;
    }

    /**
     * Get the delay this action will pause the startup action processing.
     *
     * @return seconds delay
     */
    public int getDelay() {
        return this.delay;
    }

    /**
     * Set the delay this action will pause the startup action processing.
     *
     * @param delay delay in seconds
     */
    public void setDelay(int delay) {
        this.delay = delay;
    }

    @Override
    public void performAction() throws JmriException {
        if (delay > 0) {
            log.info("Pausing startup actions processing for {} seconds.", delay);
            try {
                // delay is in seconds ; sleep takes long, not int
                Thread.sleep(delay * (long) 1000);
            } catch (InterruptedException ex) {
                // warn the user that the pause was not as long as expected
                // this does not throw an error displayed to the user; should it?
                log.warn("Pause in startup actions interrupted.");
            }
        }
    }

}

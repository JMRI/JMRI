package apps.startup;

/**
 * Startup action that causes JMRI to pause before triggering the next startup
 * action.
 *
 * @author Randall Wood (c) 2016
 */
public class StartupPauseModel extends AbstractStartupModel {

    public static final int DEFAULT_DELAY = 10;

    private int delay = -1; // default to invalid duration

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

}

package apps.startup;

/**
 * Startup action that causes JMRI to pause before triggering the next startup
 * action.
 *
 * @author Randall Wood (c) 2016
 */
public class StartupPauseModel extends AbstractStartupModel {

    private int delay = 10; // default to ten seconds

    @Override
    public String getName() {
        return Bundle.getMessage("StartupPauseModel.name", this.getDelay()); // NOI18N
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

package jmri.implementation;

import jmri.Audio;
import jmri.InstanceManager;

/**
 * Base implementation of the Audio class.
 * <p>
 * Specific implementations will extend this base class.
 *
 * @author Matthew Harris copyright (c) 2009
 */
public abstract class AbstractAudio extends AbstractNamedBean implements Audio {

    private int _state = STATE_INITIAL;

    private static final int INT_PRECISION = (int) Math.pow(10, DECIMAL_PLACES);

    /**
     * Abstract constructor for new Audio with system name
     *
     * @param systemName Audio object system name (e.g. IAS1, IAB4)
     */
    public AbstractAudio(String systemName) {
        super(systemName);
    }

    /**
     * Abstract constructor for new Audio with system name and user name
     *
     * @param systemName Audio object system name (e.g. IAS1, IAB4)
     * @param userName   Audio object user name
     */
    public AbstractAudio(String systemName, String userName) {
        super(systemName, userName);
    }

    @Override
    public int getState() {
        return this._state;
    }

    @Override
    public void setState(int newState) {
        Object _old = this._state;
        this._state = newState;
        stateChanged((Integer) _old);
        firePropertyChange("State", _old, _state); //NOI18N
    }

    @Override
    public String toString() {
        return this.getClass().getName() + " (" + this.getSystemName() + ")"; //NOI18N
    }

    /**
     * Abstract method that concrete classes will implement to perform necessary
     * cleanup routines.
     */
    abstract protected void cleanup();

    @Override
    public void dispose() {
        InstanceManager.getDefault(jmri.AudioManager.class).deregister(this);
        super.dispose();
    }

    /**
     * Static method to round a float value to the specified number of decimal
     * places
     *
     * @param value  float value to round
     * @param places number of decimal places to round to
     * @return float value rounded to specified number of decimal places
     */
    public static float roundDecimal(float value, double places) {
        double multiplier = Math.pow(10, places);
        value *= multiplier;
        return (float) (Math.round(value) / multiplier);
    }

    /**
     * Static method to round a float value to the number of decimal places
     * defined by DECIMAL_PLACES.
     *
     * @param value float value to round
     * @return float value rounded to DECIMAL_PLACES decimal places
     */
    public static float roundDecimal(float value) {
        return roundDecimal(value, Math.log10(INT_PRECISION));
    }

    @Override
    public String getBeanType() {
        return Bundle.getMessage("BeanNameAudio");
    }

}

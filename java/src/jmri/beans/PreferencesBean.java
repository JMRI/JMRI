package jmri.beans;

import java.beans.PropertyChangeEvent;
import javax.annotation.Nonnull;
import jmri.profile.Profile;

/**
 * Bean that implements some common code for preferences objects.
 *
 * @author Randall Wood (C) 2017, 2020
 */
public abstract class PreferencesBean extends Bean {

    /**
     * Property indicating preferences item do/do not need to be saved.
     *
     * {@value #DIRTY}
     */
    public static final String DIRTY = "dirty"; // NOI18N
    /**
     * Property indicating preferences item requires restart to be applied.
     *
     * {@value #RESTART_REQUIRED}
     */
    public static final String RESTART_REQUIRED = "restartRequired"; // NOI18N
    private boolean restartRequired = false;
    private boolean isDirty = false;
    private final Profile profile;

    /**
     * Create the PreferencesBean.
     *
     * @param profile the Profile this PreferencesBean is associated with; if
     * null is not associated with a Profile, but applies application wide
     */
    public PreferencesBean(Profile profile) {
        super(false);
        this.profile = profile;
    }

    /**
     * Get the profile associated with this PreferencesBean.
     *
     * @return the profile
     */
    @Nonnull
    public Profile getProfile() {
        return this.profile;
    }

    /**
     * Check if this preferences bean has a state that needs to be saved.
     *
     * @return true if unsaved; false otherwise
     */
    public boolean isDirty() {
        return this.isDirty;
    }

    /**
     * Check if this preferences bean requires the application to be restarted
     * to take effect.
     *
     * @return true if a restart is required; false otherwise
     */
    public boolean isRestartRequired() {
        return this.restartRequired;
    }

    /**
     * Set if restart needs to be required for some preferences to take effect.
     */
    protected void setRestartRequired() {
        if (!this.restartRequired) {
            this.restartRequired = true;
            this.firePropertyChange(RESTART_REQUIRED, false, true);
        }
    }

    /**
     * Set if preferences need to be saved.
     *
     * @param value true to indicate need to save; false otherwise
     */
    protected void setIsDirty(boolean value) {
        boolean old = this.isDirty;
        this.isDirty = value;
        this.firePropertyChange(DIRTY, old, value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * As a side effect, calls to {@link #isDirty} will return {@code true} if
     * oldValue and newValue differ or are null.
     */
    @Override
    public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        if (oldValue == null || newValue == null || !oldValue.equals(newValue)) {
            this.propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
            this.setIsDirty(true);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * As a side effect, calls to {@link #isDirty} will return {@code true} if
     * oldValue and newValue differ and propertyName is not {@value #DIRTY}.
     */
    @Override
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
        if (oldValue != newValue) {
            this.propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
            // don't force dirty to true if we just changed dirty
            if (!DIRTY.equals(propertyName)) {
                this.setIsDirty(true);
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * As a side effect, calls to {@link #isDirty} will return {@code true} if
     * oldValue and newValue differ.
     */
    @Override
    public void firePropertyChange(String propertyName, int oldValue, int newValue) {
        if (oldValue != newValue) {
            this.propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
            this.setIsDirty(true);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * As a side effect, calls to {@link #isDirty} will return {@code true}. To
     * avoid that side effect, call
     * {@link PropertyChangeSupport#firePropertyChange(java.beans.PropertyChangeEvent)}
     * on {@link #propertyChangeSupport} directly.
     */
    @Override
    public void firePropertyChange(PropertyChangeEvent evt) {
        this.propertyChangeSupport.firePropertyChange(evt);
        if (!DIRTY.equals(evt.getPropertyName())) {
            this.setIsDirty(true);
        }
    }
}

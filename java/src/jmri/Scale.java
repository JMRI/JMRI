package jmri;

import java.beans.PropertyVetoException;
import javax.annotation.Nonnull;
import jmri.beans.ConstrainedBean;

/**
 * Define the characteristics of a layout scale.  A scale has four properties.
 * <ul>
 * <li>Name - A fixed string, such N or HO.
 * <li>User name - An alternate name that can be changed.  It defaults to the scale name.
 * <li>Ratio - The ratio for the scale, such as 160 for N scale.
 * <li>Factor - A derived value created by dividing 1 by the scale ratio.
 * </ul>
 * In addition to the standard scales, there is custom entry.  Custom settings
 * are retained in a local copy of ScaleData.xml.
 * <p>
 * Methods are provided to set/get the user name and the scale ratio.  The scale
 * factor is generated from the scale ratio and is read only, as is the scale name.
 * <p>
 * While changing the ratio and user names of the standard scales is not
 * prohibited, doing so is not recommended due to potential conflicts with other
 * applications.
 * <p>
 * Changes to user names and ratios send a <strong>vetoableChange</strong> event.
 * Interested applications can add a <strong>vetoableChange</strong> listener
 * in order to be notified when an event occurs. If the listener determines that
 * the change cannot occur, it can throw a <strong>PropertyVetoException</strong>.
 * <p>
 * See {@link jmri.ScaleManager Scale Manager} for manager details.
 *
 * @author Dave Sand Copyright (C) 2018
 * @since 4.13.6
 */
public class Scale extends ConstrainedBean {

    public Scale() {
        super();
    }

    public Scale(@Nonnull String name, double ratio, String userName) {
        super();
        _name = name;
        _userName = (userName == null) ? name : userName;
        _ratio = ratio;
        _factor = 1.0 / _ratio;
    }

    private String _name = "HO";  // NOI18N
    private String _userName = "HO";  // NOI18N
    private double _ratio = 87.1;
    private double _factor = 1 / 87.1;

    public String getScaleName() {
        return _name;
    }

    public String getUserName() {
        return _userName;
    }

    public double getScaleRatio() {
        return _ratio;
    }

    public double getScaleFactor() {
        return _factor;
    }

    /**
     * Set the user name for the current scale.
     * Registered listeners can veto the change.
     * @param newName The name to be applied if unique.
     * @throws IllegalArgumentException The supplied name is a duplicate.
     * @throws PropertyVetoException The user name change was vetoed.
     */
    public void setUserName(@Nonnull String newName) throws IllegalArgumentException, PropertyVetoException {
        for (Scale scale : ScaleManager.getScales()) {
            if (scale.getUserName().equals(newName)) {
                if (!scale.getScaleName().equals(_name)) {
                    throw new IllegalArgumentException("Duplicate scale user name");  // NOI18N
                }
            }
        }

        String oldName = _userName;
        _userName = newName;

        try {
            fireVetoableChange("ScaleUserName", oldName, newName);  // NOI18N
        } catch (PropertyVetoException ex) {
            // Roll back change
            log.warn("The user name change for {} scale to {} was rejected: Reason: {}",  // NOI18N
                     _name, _userName, ex.getMessage());
            _userName = oldName;
            throw ex;  // Notify caller
        }
        jmri.configurexml.ScaleConfigXML.doStore();
    }

    /**
     * Set the new scale ratio and calculate the scale factor.
     * Registered listeners can veto the change.
     * @param newRatio A double value containing the ratio.
     * @throws IllegalArgumentException The new ratio is less than 1.
     * @throws PropertyVetoException The ratio change was vetoed.
     */
    public void setScaleRatio(double newRatio) throws IllegalArgumentException, PropertyVetoException {
        if (newRatio < 1.0) {
            throw new IllegalArgumentException("The scale ratio is less than 1");  // NOI18N
        }

        double oldRatio = _ratio;
        _ratio = newRatio;
        _factor = 1.0 / _ratio;

        try {
            fireVetoableChange("ScaleRatio", oldRatio, newRatio);  // NOI18N
        } catch (PropertyVetoException ex) {
            // Roll back change
            log.warn("The ratio change for {} scale to {} was rejected: Reason: {}",  // NOI18N
                     _name, _ratio, ex.getMessage());
            _ratio = oldRatio;
            _factor = 1.0 / oldRatio;
            throw ex;  // Notify caller
        }
        jmri.configurexml.ScaleConfigXML.doStore();
    }

    @Override
    public String toString() {
        return String.format("%s (%.1f)", getUserName(), getScaleRatio());
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Scale.class);

}

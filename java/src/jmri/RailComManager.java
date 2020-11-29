package jmri;

/**
 * Locate a RailCom Object representing a specific RailCom Enabled device.<br>
 * RailCom is a registered trademark of Lenz GmbH.
 * <p>
 * RailCom objects are obtained from an RailComManager, which in turn is
 * generally located from the InstanceManager. A typical call sequence might be:
 * <pre>
 * RailCom rc = InstanceManager.getDefault(jmri.RailComManager.class).provideIdTag("23");
 * </pre> The RailCom Manager itself is not installed unless the required
 * hardware that supports RailCom has been installed.
 * <p>
 * Although the RailCom object does extend the NamedBean, it doesn't
 * specifically use the system or user names as each RailCom device should in
 * itself be unique.
 *
 * @author Kevin Dickerson Copyright (C) 2012
 * @since 2.99.4
 */
public interface RailComManager extends IdTagManager {
}

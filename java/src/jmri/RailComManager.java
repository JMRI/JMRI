package jmri;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Locate a RailCom Object representing a specific RailCom Enabled device.<br>
 * RailCom is a registered trademark of Lenz GmbH.
 * <P>
 * RailCom objects are obtained from an RailComManager, which in turn is
 * generally located from the InstanceManager. A typical call sequence might be:
 * <PRE>
 * RailCom rc = InstanceManager.getDefault(jmri.RailComManager.class).provideIdTag("23");
 * </PRE> The RailCom Manager itself is not installed unless the required
 * hardware that supports RailCom has been installed.
 * <p>
 * Although the RailCom object does extend the NamedBean, it doesn't
 * specifically use the system or user names as each RailCom device should in
 * itself be unique.
 * <P>
 * @author Kevin Dickerson Copyright (C) 2012
 * @since 2.99.4
 */
public interface RailComManager extends IdTagManager {

    /**
     * 
     * @throws IllegalArgumentException if requested object doesn't already exist and 
     *                                  the manager cannot create it due to
     *                                  e.g. an illegal name or name that can't
     *                                  be parsed.
     */
    @Override
    public @Nonnull RailCom provideIdTag(@Nonnull String name) throws IllegalArgumentException;

    @Override
    public @CheckForNull RailCom getIdTag(@Nonnull String name);

}

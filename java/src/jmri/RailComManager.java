package jmri;

import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.CheckReturnValue;
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
public interface RailComManager extends Manager<RailCom> {

    /**
     *
     * @param name the name of the tag to provide
     * @return the generated tag
     * @throws IllegalArgumentException if requested object does not already
     *                                  exist and the manager cannot create it
     *                                  due to an illegal name or name that can
     *                                  not be parsed.
     */
    @Nonnull
    public RailCom provideIdTag(@Nonnull String name) throws IllegalArgumentException;

    @CheckForNull
    public RailCom getIdTag(@Nonnull String name);

    /**
     * Locate an instance based on a system name. Returns null if no instance
     * already exists.
     *
     * @param systemName system name being requested
     * @return requested IdTag object or null if none exists
     */
    @CheckReturnValue
    @CheckForNull
    public RailCom getBySystemName(@Nonnull String systemName);

    /**
     * Locate an instance based on a user name. Returns null if no instance
     * already exists.
     *
     * @param userName user name being requested
     * @return requested IdTag object or null if none exists
     */
    @CheckReturnValue
    @CheckForNull
    public RailCom getByUserName(@Nonnull String userName);

    /**
     * Locate an instance based on a tag ID. Returns null if no instance already
     * exists.
     *
     * @param tagID tag ID being requested
     * @return requested IdTag object or null if none exists
     */
    @CheckReturnValue
    @CheckForNull
    public RailCom getByTagID(@Nonnull String tagID);

    /**
     * Return an instance with the specified system and user names. Note that
     * two calls with the same arguments will get the same instance; there is
     * only one IdTag object representing a given physical IdTag and therefore
     * only one with a specific system or user name.
     * <P>
     * This will always return a valid object reference; a new object will be
     * created if necessary. In that case:
     * <UL>
     * <LI>If a null reference is given for user name, no user name will be
     * associated with the IdTag object created; a valid system name must be
     * provided
     * <LI>If both are provided, the system name defines the hardware access of
     * the desired IdTag, and the user address is associated with it. The system
     * name must be valid.
     * </UL>
     * Note that it is possible to make an inconsistent request if both
     * addresses are provided, but the given values are associated with
     * different objects. This is a problem, and we don't have a good solution
     * except to issue warnings. This will mostly happen if you're creating
     * RfidTags when you should be looking them up.
     *
     * @param systemName the system name
     * @param userName   the user name
     * @return requested IdTag object (never null)
     * @throws IllegalArgumentException if cannot create the IdTag due to e.g.
     *                                  an illegal name or name that can't be
     *                                  parsed.
     */
    @Nonnull
    public RailCom newIdTag(@Nonnull String systemName, @CheckForNull String userName);

    /**
     * Get a list of all IdTags seen by a specified Reporter within a specific
     * time threshold from the most recently seen.
     *
     * @param reporter  Reporter to return list for (can be null)
     * @param threshold Time threshold (in ms)
     * @return List of matching IdTags
     */
    @CheckReturnValue
    @Nonnull
    public List<IdTag> getTagsForReporter(@Nonnull Reporter reporter, long threshold);

    /**
     * Define if the manager should persist details of when and where all known
     * IdTags were seen.
     *
     * @param state True to store; False to omit
     */
    public void setStateStored(boolean state);

    /**
     * Determines if the state of known IdTags should be stored.
     *
     * @return True to store state; False to discard state
     */
    @CheckReturnValue
    public boolean isStateStored();

    /**
     * Define if the manager should use the fast clock when setting the times
     * when a given IdTag was last seen.
     *
     * @param fastClock True to use the fast clock; False to use the system
     *                  clock
     */
    public void setFastClockUsed(boolean fastClock);

    /**
     * Determines if fast clock times should be recorded for when a given IdTag
     * was last seen.
     *
     * @return True to use the fast clock; False to use the system clock
     */
    @CheckReturnValue
    public boolean isFastClockUsed();

    /**
     * Perform initialization.
     */
    public void init();

    /**
     * Determines if the manager has been initialized.
     *
     * @return state of initialization
     */
    @CheckReturnValue
    public boolean isInitialised();
}

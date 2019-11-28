package jmri;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import javax.annotation.CheckForNull;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

import jmri.beans.PropertyChangeProvider;

/**
 * Provides common services for classes representing objects on the layout, and
 * allows a common form of access by their Managers.
 * <p>
 * Each object has two types of names:
 * <p>
 * The "system" name is provided by the system-specific implementations, and
 * provides a unique mapping to the layout control system (for example LocoNet
 * or NCE) and address within that system. It must be present and unique across
 * the JMRI instance. Two beans are identical if they have the same system name;
 * if not, not.
 * <p>
 * The "user" name is optional. It's free form text except for two restrictions:
 * <ul>
 * <li>It can't be the empty string "". (A non-existant user name is coded as a
 * null)
 * <li>And eventually, we may insist on normalizing user names to a specific
 * form, e.g. remove leading and trailing white space; see the
 * {@link #normalizeUserName(java.lang.String)} method
 * </ul>
 * <p>
 * Each of these two names must be unique for every NamedBean of the same type
 * on the layout and a single NamedBean cannot have a user name that is the same
 * as the system name of another NamedBean of the same type. (The complex
 * wording is saying that a single NamedBean object is allowed to have its
 * system name and user name be the same, but that's the only non-uniqueness
 * that's allowed within a specific type). Note that the uniqueness restrictions
 * are currently not completely enforced, only warned about; a future version of
 * JMRI will enforce this restriction.
 * <p>
 * For more information, see the
 * <a href="http://jmri.org/help/en/html/doc/Technical/Names.shtml">Names and
 * Naming</a> page in the
 * <a href="http://jmri.org/help/en/html/doc/Technical/index.shtml">Technical
 * Info</a> pages.
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002, 2003, 2004
 * @see jmri.Manager
 */
public interface NamedBean extends Comparable<NamedBean>, PropertyChangeProvider {

    /**
     * Constant representing an "unknown" state, indicating that the object's
     * state is not necessarily that of the actual layout hardware. This is the
     * initial state of a newly created object before communication with the
     * layout.
     */
    public static final int UNKNOWN = 0x01;

    /**
     * Constant representing an "inconsistent" state, indicating that some
     * inconsistency has been detected in the hardware readback.
     */
    public static final int INCONSISTENT = 0x08;

    /**
     * Format used for {@link #getDisplayName(DisplayOptions)} when displaying
     * the user name and system name without quoation marks around the user
     * name.
     */
    public static final String DISPLAY_NAME_FORMAT = "%s (%s)";

    /**
     * Format used for {@link #getDisplayName(DisplayOptions)} when displaying
     * the user name and system name with quoation marks around the user name.
     */
    public static final String QUOTED_NAME_FORMAT = "\"%s\" (%s)";

    /**
     * User's identification for the item. Bound parameter so manager(s) can
     * listen to changes. Any given user name must be unique within the layout.
     * Must not match the system name.
     *
     * @return null if not set
     */
    @CheckReturnValue
    @CheckForNull
    public String getUserName();

    /**
     * Set the user name, normalizing it if needed.
     *
     * @param s the new user name
     * @throws jmri.NamedBean.BadUserNameException if the user name can not be
     *                                                 normalized
     */
    public void setUserName(@CheckForNull String s) throws BadUserNameException;

    /**
     * Get a system-specific name. This encodes the hardware addressing
     * information. Any given system name must be unique within the layout.
     *
     * @return the system-specific name
     */
    @CheckReturnValue
    @Nonnull
    public String getSystemName();

    /**
     * Display the system-specific name.
     *
     * @return the system-specific name
     */
    @Nonnull
    @Override
    public String toString();

    /**
     * Get user name if it exists, otherwise return System name.
     *
     * @return the user name or system-specific name
     */
    @CheckReturnValue
    @Nonnull
    public default String getDisplayName() {
        return getDisplayName(DisplayOptions.DISPLAYNAME);
    }

    /**
     * Get the name to display, formatted per {@link NamedBean.DisplayOptions}.
     * 
     * @param options the DisplayOptions to use
     * @return the display name formatted per options
     */
    @CheckReturnValue
    @Nonnull
    public default String getDisplayName(DisplayOptions options) {
        String userName = getUserName();
        String systemName = getSystemName();
        // since there are two undisplayable states for the user name,
        // empty or null, if user name is empty, make it null to avoid
        // repeatedly checking for both those states later
        if (userName != null && userName.isEmpty()) {
            userName = null;
        }
        switch (options) {
            case USERNAME_SYSTEMNAME:
                return userName != null ? String.format(DISPLAY_NAME_FORMAT, userName, systemName) : systemName;
            case QUOTED_USERNAME_SYSTEMNAME:
                return userName != null ? String.format(QUOTED_NAME_FORMAT, userName, systemName) : getDisplayName(DisplayOptions.QUOTED_SYSTEMNAME);
            case SYSTEMNAME:
                return systemName;
            case QUOTED_SYSTEMNAME:
                return String.format("\"%s\"", systemName);
            case QUOTED_USERNAME:
            case QUOTED_DISPLAYNAME:
                return String.format("\"%s\"", userName != null ? userName : systemName);
            case USERNAME:
            case DISPLAYNAME:
            default:
                return userName != null ? userName : systemName;
        }
    }
    
    /**
     * Get a fully formatted display that includes the SystemName and, if set,
     * the UserName.
     * <p>
     * This is the same as calling {@link #getDisplayName(DisplayOptions)} with
     * the parameter {@link DisplayOptions#USERNAME_SYSTEMNAME}.
     *
     * @return {@code UserName (SystemName)} or {@code SystemName} if the
     *         UserName is null, empty, or matches the SystemName
     * @deprecated since 4.17.2; use {@link #getDisplayName(DisplayOptions)}
     *             with {@link DisplayOptions#USERNAME_SYSTEMNAME} instead
     */
    @CheckReturnValue
    @Nonnull
    @Deprecated
    public default String getFullyFormattedDisplayName() {
        return getDisplayName(DisplayOptions.USERNAME_SYSTEMNAME);
    }

    /**
     * Returns a fully formatted display that includes the SystemName and
     * UserName if set. This uses the format {@value #DISPLAY_NAME_FORMAT}
     *
     * @param userNameFirst ignored; retained for compatibility until removed
     * @return {@code UserName (SystemName)} or {@code SystemName} if the
     *         UserName is null, empty, or matches the SystemName
     * @deprecated since 4.17.2; use {@link #getDisplayName(DisplayOptions)}
     *             with {@link DisplayOptions#USERNAME_SYSTEMNAME} instead
     */
    @CheckReturnValue
    @Nonnull
    @Deprecated
    public default String getFullyFormattedDisplayName(boolean userNameFirst) {
        return getDisplayName(DisplayOptions.USERNAME_SYSTEMNAME);
    }

    /**
     * Request a call-back when a bound property changes. Bound properties are
     * the known state, commanded state, user and system names.
     *
     * @param listener    The listener. This may change in the future to be a
     *                        subclass of NamedProprtyChangeListener that
     *                        carries the name and listenerRef values internally
     * @param name        The name (either system or user) that the listener
     *                        uses for this namedBean, this parameter is used to
     *                        help determine when which listeners should be
     *                        moved when the username is moved from one bean to
     *                        another
     * @param listenerRef A textual reference for the listener, that can be
     *                        presented to the user when a delete is called
     */
    public void addPropertyChangeListener(@Nonnull PropertyChangeListener listener, String name, String listenerRef);

    /**
     * Request a call-back when a bound property changes. Bound properties are
     * the known state, commanded state, user and system names.
     *
     * @param propertyName The name of the property to listen to
     * @param listener     The listener. This may change in the future to be a
     *                         subclass of NamedProprtyChangeListener that
     *                         carries the name and listenerRef values
     *                         internally
     * @param name         The name (either system or user) that the listener
     *                         uses for this namedBean, this parameter is used
     *                         to help determine when which listeners should be
     *                         moved when the username is moved from one bean to
     *                         another
     * @param listenerRef  A textual reference for the listener, that can be
     *                         presented to the user when a delete is called
     */
    public void addPropertyChangeListener(@Nonnull String propertyName, @Nonnull PropertyChangeListener listener,
            String name, String listenerRef);

    public void updateListenerRef(@Nonnull PropertyChangeListener l, String newName);

    public void vetoableChange(@Nonnull PropertyChangeEvent evt) throws PropertyVetoException;

    /**
     * Get the textual reference for the specific listener
     *
     * @param l the listener of interest
     * @return the textual reference
     */
    @CheckReturnValue
    public String getListenerRef(@Nonnull PropertyChangeListener l);

    /**
     * Returns a list of all the listeners references
     *
     * @return a list of textual references
     */
    @CheckReturnValue
    public ArrayList<String> getListenerRefs();

    /**
     * Number of current listeners. May return -1 if the information is not
     * available for some reason.
     *
     * @return the number of listeners.
     */
    @CheckReturnValue
    public int getNumPropertyChangeListeners();

    /**
     * Get a list of all the property change listeners that are registered using
     * a specific name
     *
     * @param name The name (either system or user) that the listener has
     *                 registered as referencing this namedBean
     * @return empty list if none
     */
    @CheckReturnValue
    @Nonnull
    public PropertyChangeListener[] getPropertyChangeListenersByReference(@Nonnull String name);

    /**
     * Deactivate this object, so that it releases as many resources as possible
     * and no longer effects others.
     * <p>
     * For example, if this object has listeners, after a call to this method it
     * should no longer notify those listeners. Any native or system-wide
     * resources it maintains should be released, including threads, files, etc.
     * <p>
     * It is an error to invoke any other methods on this object once dispose()
     * has been called. Note, however, that there is no guarantee about behavior
     * in that case.
     * <p>
     * Afterwards, references to this object may still exist elsewhere,
     * preventing its garbage collection. But it's formally dead, and shouldn't
     * be keeping any other objects alive. Therefore, this method should null
     * out any references to other objects that this NamedBean contained.
     */
    public void dispose(); // remove _all_ connections!

    /**
     * Provide generic access to internal state.
     * <p>
     * This generally shouldn't be used by Java code; use the class-specific
     * form instead (e.g. setCommandedState in Turnout). This is provided to
     * make scripts access easier to read.
     *
     * @param s the state
     * @throws JmriException general error when setting the state fails
     */
    @InvokeOnLayoutThread
    public void setState(int s) throws JmriException;

    /**
     * Provide generic access to internal state.
     * <p>
     * This generally shouldn't be used by Java code; use the class-specific
     * form instead (e.g. getCommandedState in Turnout). This is provided to
     * make scripts easier to read.
     *
     * @return the state
     */
    @CheckReturnValue
    public int getState();

    /**
     * Provide human-readable, localized version of state value.
     * <p>
     * This method is intended for use when presenting to a human operator.
     *
     * @param state the state to describe
     * @return the state in localized form
     */
    @CheckReturnValue
    public String describeState(int state);

    /**
     * Get associated comment text.
     *
     * @return the comment or null
     */
    @CheckReturnValue
    @CheckForNull
    public String getComment();

    /**
     * Set associated comment text.
     * <p>
     * Comments can be any valid text.
     *
     * @param comment the comment or null to remove an existing comment
     */
    public void setComment(@CheckForNull String comment);

    /**
     * Attach a key/value pair to the NamedBean, which can be retrieved later.
     * These are not bound properties as yet, and don't throw events on
     * modification. Key must not be null.
     * <p>
     * Prior to JMRI 4.3, the key was of Object type. It was constrained to
     * String to make these more like normal Java Beans.
     *
     * @param key   the property to set
     * @param value the value of the property
     */
    public void setProperty(@Nonnull String key, Object value);

    /**
     * Retrieve the value associated with a key. If no value has been set for
     * that key, returns null.
     *
     * @param key the property to get
     * @return The value of the property or null.
     */
    @CheckReturnValue
    @CheckForNull
    public Object getProperty(@Nonnull String key);

    /**
     * Remove the key/value pair against the NamedBean.
     *
     * @param key the property to remove
     */
    public void removeProperty(@Nonnull String key);

    /**
     * Retrieve the complete current set of keys.
     *
     * @return empty set if none
     */
    @CheckReturnValue
    @Nonnull
    public java.util.Set<String> getPropertyKeys();

    /**
     * For instances in the code where we are dealing with just a bean and a
     * message needs to be passed to the user or in a log.
     *
     * @return a string of the bean type, eg Turnout, Sensor etc
     */
    @CheckReturnValue
    @Nonnull
    public String getBeanType();

    /**
     * Enforces, and as a user convenience converts to, the standard form for a
     * user name.
     * <p>
     * This implementation just does a trim(), but later versions might e.g. do
     * more extensive things.
     *
     * @param inputName User name to be normalized
     * @throws BadUserNameException If the inputName can't be converted to
     *                                  normalized form
     * @return A user name in standard normalized form or null if inputName was
     *         null
     */
    @CheckReturnValue
    @CheckForNull
    static public String normalizeUserName(@CheckForNull String inputName) throws BadUserNameException {
        String result = inputName;
        if (result != null) {
            result = result.trim();
        }
        return result;
    }

    /**
     * Provide a comparison between the system names of two beans. This provides
     * a implementation for e.g. {@link java.util.Comparator}. Returns 0 if the
     * names are the same, -1 if the first argument orders before the second
     * argument's name, +1 if the first argument's name orders after the second
     * argument's name. The comparison is alphanumeric on the system prefix,
     * then alphabetic on the type letter, then system-specific comparison on
     * the two suffix parts via the {@link #compareSystemNameSuffix} method.
     *
     * @param n2 The second NamedBean in the comparison ("this" is the first
     *               one)
     * @return -1,0,+1 for ordering if the names are well-formed; may not
     *         provide proper ordering if the names are not well-formed.
     */
    @CheckReturnValue
    @Override
    public default int compareTo(NamedBean n2) {
        Objects.requireNonNull(n2);
        jmri.util.AlphanumComparator ac = new jmri.util.AlphanumComparator();
        String o1 = this.getSystemName();
        String o2 = n2.getSystemName();

        int p1len = Manager.getSystemPrefixLength(o1);
        int p2len = Manager.getSystemPrefixLength(o2);

        int comp = ac.compare(o1.substring(0, p1len), o2.substring(0, p2len));
        if (comp != 0)
            return comp;

        char c1 = o1.charAt(p1len);
        char c2 = o2.charAt(p2len);

        if (c1 != c2) {
            return (c1 > c2) ? +1 : -1;
        } else {
            return this.compareSystemNameSuffix(o1.substring(p1len + 1), o2.substring(p2len + 1), n2);
        }
    }

    /**
     * Compare the suffix of this NamedBean's name with the suffix of the
     * argument NamedBean's name for the {@link #compareTo} operation. This is
     * intended to be a system-specific comparison that understands the various
     * formats, etc.
     *
     * @param suffix1 The suffix for the 1st bean in the comparison
     * @param suffix2 The suffix for the 2nd bean in the comparison
     * @param n2      The other (second) NamedBean in the comparison
     * @return -1,0,+1 for ordering if the names are well-formed; may not
     *         provide proper ordering if the names are not well-formed.
     */
    @CheckReturnValue
    public int compareSystemNameSuffix(@Nonnull String suffix1, @Nonnull String suffix2, @Nonnull NamedBean n2);

    /**
     * Parent class for a set of classes that describe if a user name or system
     * name is a bad name.
     */
    public class BadNameException extends IllegalArgumentException {

        private final String localizedMessage;
        
        /**
         * Create an exception with no message to the user or for logging.
         */
        protected BadNameException() {
            super();
            localizedMessage = super.getMessage();
        }

        /**
         * Create a localized exception, suitable for display to the user.This
         * takes the non-localized message followed by the localized message.
         * <p>
         * Use {@link #getLocalizedMessage()} to display the message to the
         * user, and use {@link #getMessage()} to record the message in logs.
         *
         * @param logging the English message for logging
         * @param display the localized message for display
         */
        protected BadNameException(String logging, String display) {
            super(logging);
            localizedMessage = display;
        }
        
        @Override
        public String getLocalizedMessage() {
            return localizedMessage;
        }
        
    }

    public class BadUserNameException extends BadNameException {

        /**
         * Create an exception with no message to the user or for logging. Use
         * only when calling methods likely have alternate mechanism for
         * allowing user to understand why exception was thrown.
         */
        public BadUserNameException() {
            super();
        }

        /**
         * Create a localized exception, suitable for display to the user. This
         * takes the same arguments as
         * {@link jmri.Bundle#getMessage(java.util.Locale, java.lang.String, java.lang.Object...)}
         * as it uses that method to create both the localized and loggable
         * messages.
         * <p>
         * Use {@link #getLocalizedMessage()} to display the message to the
         * user, and use {@link #getMessage()} to record the message in logs.
         * <p>
         * <strong>Note</strong> the message must be accessible by
         * {@link jmri.Bundle}.
         * 
         * @param locale  the locale to be used
         * @param message bundle key to be translated
         * @param subs    One or more objects to be inserted into the message
         */
        public BadUserNameException(Locale locale, String message, Object... subs) {
            super(Bundle.getMessage(Locale.ENGLISH, message, subs),
                    Bundle.getMessage(locale, message, subs));
        }

        /**
         * Create a localized exception, suitable for display to the user. This
         * takes the non-localized message followed by the localized message.
         * <p>
         * Use {@link #getLocalizedMessage()} to display the message to the
         * user, and use {@link #getMessage()} to record the message in logs.
         *
         * @param logging the English message for logging
         * @param display the localized message for display
         */
        public BadUserNameException(String logging, String display) {
            super(logging, display);
        }
    }

    public class BadSystemNameException extends BadNameException {

        /**
         * Create an exception with no message to the user or for logging. Use
         * only when calling methods likely have alternate mechanism for
         * allowing user to understand why exception was thrown.
         */
        public BadSystemNameException() {
            super();
        }

        /**
         * Create a localized exception, suitable for display to the user. This
         * takes the same arguments as
         * {@link jmri.Bundle#getMessage(java.util.Locale, java.lang.String, java.lang.Object...)}
         * as it uses that method to create both the localized and loggable
         * messages.
         * <p>
         * Use {@link #getLocalizedMessage()} to display the message to the
         * user, and use {@link #getMessage()} to record the message in logs.
         * <p>
         * <strong>Note</strong> the message must be accessible by
         * {@link jmri.Bundle}.
         *
         * @param locale  the locale to be used
         * @param message bundle key to be translated
         * @param subs    One or more objects to be inserted into the message
         */
        public BadSystemNameException(Locale locale, String message, Object... subs) {
            this(Bundle.getMessage(Locale.ENGLISH, message, subs),
                    Bundle.getMessage(locale, message, subs));
        }

        /**
         * Create a localized exception, suitable for display to the user. This
         * takes the non-localized message followed by the localized message.
         * <p>
         * Use {@link #getLocalizedMessage()} to display the message to the
         * user, and use {@link #getMessage()} to record the message in logs.
         *
         * @param logging the English message for logging
         * @param display the localized message for display
         */
        public BadSystemNameException(String logging, String display) {
            super(logging, display);
        }
    }

    public class DuplicateSystemNameException extends IllegalArgumentException {

        private final String localizedMessage;
        
        /**
         * Create an exception with no message to the user or for logging. Use
         * only when calling methods likely have alternate mechanism for
         * allowing user to understand why exception was thrown.
         */
        public DuplicateSystemNameException() {
            super();
            localizedMessage = super.getMessage();
        }

        /**
         * Create a exception.
         *
         * @param message bundle key to be translated
         */
        public DuplicateSystemNameException(String message) {
            super(message);
            localizedMessage = super.getMessage();
        }

        /**
         * Create a localized exception, suitable for display to the user. This
         * takes the same arguments as
         * {@link jmri.Bundle#getMessage(java.util.Locale, java.lang.String, java.lang.Object...)}
         * as it uses that method to create both the localized and loggable
         * messages.
         * <p>
         * Use {@link #getLocalizedMessage()} to display the message to the
         * user, and use {@link #getMessage()} to record the message in logs.
         * <p>
         * <strong>Note</strong> the message must be accessible by
         * {@link jmri.Bundle}.
         *
         * @param locale  the locale to be used
         * @param message bundle key to be translated
         * @param subs    One or more objects to be inserted into the message
         */
        public DuplicateSystemNameException(Locale locale, String message, Object... subs) {
            this(Bundle.getMessage(locale, message, subs),
                    Bundle.getMessage(locale, message, subs));
        }

        /**
         * Create a localized exception, suitable for display to the user. This
         * takes the non-localized message followed by the localized message.
         * <p>
         * Use {@link #getLocalizedMessage()} to display the message to the
         * user, and use {@link #getMessage()} to record the message in logs.
         *
         * @param logging the English message for logging
         * @param display the localized message for display
         */
        public DuplicateSystemNameException(String logging, String display) {
            super(logging);
            localizedMessage = display;
        }

        @Override
        public String getLocalizedMessage() {
            return localizedMessage;
        }
    }

    /**
     * Display options for {@link #getDisplayName(DisplayOptions)}. The quoted
     * forms are intended to be used in sentences and messages, while the
     * unquoted forms are intended for use in user interface elements like lists
     * and combo boxes.
     */
    public enum DisplayOptions {
        /**
         * Display the user name; if the user name is null or empty, display the
         * system name.
         */
        DISPLAYNAME,
        /**
         * Display the user name in quotes; if the user name is null or empty,
         * display the system name in quotes.
         */
        QUOTED_DISPLAYNAME,
        /**
         * Display the user name; if the user name is null or empty, display the
         * system name.
         */
        USERNAME,
        /**
         * Display the user name in quotes; if the user name is null or empty,
         * display the system name in quotes.
         */
        QUOTED_USERNAME,
        /**
         * Display the system name. This should be used only when the context
         * would cause displaying the user name to be more confusing than not or
         * in text input fields for editing the system name.
         */
        SYSTEMNAME,
        /**
         * Display the system name in quotes. This should be used only when the
         * context would cause displaying the user name to be more confusing
         * than not or in text input fields for editing the system name.
         */
        QUOTED_SYSTEMNAME,
        /**
         * Display the user name followed by the system name in parenthesis. If
         * the user name is null or empty, display the system name without
         * parenthesis.
         */
        USERNAME_SYSTEMNAME,
        /**
         * Display the user name in quotes followed by the system name in
         * parenthesis. If the user name is null or empty, display the system
         * name in quotes.
         */
        QUOTED_USERNAME_SYSTEMNAME;
    }

}

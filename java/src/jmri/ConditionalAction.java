package jmri;

import java.awt.event.ActionListener;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.Timer;

import jmri.jmrit.Sound;

/**
 * The consequent of the antecedent of the conditional proposition. The data for
 * the action to be taken when a Conditional calculates to True.
 *
 * @author Pete Cressman Copyright (C) 2009
 */
public interface ConditionalAction {

    /**
     * Integer data for action.
     *
     * @return the data
     */
    int getActionData();

    /**
     * Get an I18N String to represent the Action Data.
     * @return human readable String of the data.
     */
    @Nonnull
    String getActionDataString();

    /**
     * String data for action.
     *
     * @return the action String
     */
    @Nonnull
    String getActionString();

    /**
     * Name of the device or element that is effected.
     *
     * @return the name
     */
    String getDeviceName();

    /**
     * Options on when action is taken.
     *
     * @return the option
     */
    int getOption();

    /**
     * Get the Option String in I18N format.
     * @param type true if option is a change; false if option is a trigger.
     * @return String name of the option for this consequent type.
     */
    @Nonnull
    String getOptionString(boolean type);

    /**
     * The consequent device or element type.
     *
     * @return the type
     */
    @Nonnull
    Conditional.Action getType();

    /**
     * @return String name of this consequent type
     */
    @Nonnull
    String getTypeString();

    /**
     * Sets action data from I18N name for it.
     *
     * @param actionData user name
     */
    void setActionData(String actionData);

    void setActionData(int actionData);

    /**
     * Set the Action String.
     * Any String float values ( delayed Sensor ) should use a . decimal separator.
     * @param actionString the action String.
     */
    void setActionString(String actionString);

    void setDeviceName(String deviceName);

    /**
     * Set the Action Option.
     * @param option the action option number.
     */
    void setOption(int option);

    /**
     * Sets type from user's name for it.
     *
     * @param type name of the type
     */
    void setType(String type);

    void setType(Conditional.Action type);

    /**
     * Get an I18N description of the ConditionAction.
     * @param triggerType true if option is a change; false if option is a trigger.
     * @return human readable description.
     */
    @Nonnull
    String description(boolean triggerType);

    /*
     * get timer for delays and other timed events
     */
    @CheckForNull
    Timer getTimer();

    /*
     * set timer for delays and other timed events
     */
    void setTimer(Timer timer);

    boolean isTimerActive();

    void startTimer();

    void stopTimer();

    /*
     * set listener for delays and other timed events
     */
    @CheckForNull
    ActionListener getListener();

    /*
     * set listener for delays and other timed events
     */
    void setListener(ActionListener listener);

    /**
     * Get the Sound.
     *
     * @return the sound
     */
    @CheckForNull
    Sound getSound();

    @CheckForNull
    NamedBeanHandle<?> getNamedBean();

    @CheckForNull
    NamedBean getBean();

    /**
     * Dispose this ConditionalAction.
     */
    void dispose();

}

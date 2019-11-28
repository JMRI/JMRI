package jmri;

import java.awt.event.ActionListener;
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

    String getActionDataString();

    /**
     * String data for action.
     *
     * @return the action String
     */
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
     * @param type the type
     * @return String name of the option for this consequent type
     */
    String getOptionString(boolean type);

    /**
     * The consequent device or element type.
     *
     * @return the type
     */
    Conditional.Action getType();

    /**
     * @return String name of this consequent type
     */
    String getTypeString();

    /**
     * Sets action data from user's name for it
     *
     * @param actionData user name
     */
    void setActionData(String actionData);

    void setActionData(int actionData);

    void setActionString(String actionString);

    void setDeviceName(String deviceName);

    void setOption(int option);

    /**
     * Sets type from user's name for it.
     *
     * @param type name of the type
     */
    void setType(String type);

    void setType(Conditional.Action type);

    public String description(boolean triggerType);

    /*
     * get timer for delays and other timed events
     */
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
    public Sound getSound();

    public NamedBeanHandle<?> getNamedBean();

    public NamedBean getBean();
}

package jmri;

import java.util.ArrayList;
import javax.annotation.Nonnull;

import jmri.implementation.LightControl;

/**
 * Represent a single visible Light on the physical layout.
 * <p>
 * Each Light may have one or more control mechanisms. Control mechanism types
 * are defined here. If a Light has any controls, the information is contained
 * in LightControl objects, which are referenced via that Light.
 * <p>
 * Lights have a state and an intensity.
 * <p>
 * The intensity of the hardware output is represented by the range from 0.0 to
 * 1.0, with 1.0 being brightest.
 * <p>
 * The primary states are:
 * <ul>
 * <li>ON, corresponding to maximum intensity
 * <li>INTERMEDIATE, some value between maximum and minimum
 * <li>OFF, corresponding to minimum intensity
 * </ul>
 * The underlying hardware may provide just the ON/OFF two levels, or have a
 * semi-continuous intensity setting with some number of steps.
 * <p>
 * The light has a TargetIntensity property which can be set directly. In
 * addition, it has a CurrentIntensity property which may differ from
 * TargetIntensity while the Light is being moved from one intensity to another.
 * <p>
 * Intensity is limited by MinIntensity and MaxIntensity parameters. Setting the
 * state to ON sets the TargetIntensity to MinIntensity, and to OFF sets the
 * TargetIntensity to MaxIntensity. Attempting to directly set the
 * TargetIntensity outside the values of MinIntensity and MaxIntensity
 * (inclusive) will result in the TargetIntensity being set to the relevant
 * limit.
 * <p>
 * Because the actual light hardware has only finite resolution, the intensity
 * value is mapped to the nearest setting. For example, in the special case of a
 * two-state (on/off) Light, setting a TargetIntensity of more than 0.5 will
 * turn the Light on, less than 0.5 will turn the light off.
 * <p>
 * Specific implementations will describe how the settings map to the particular
 * hardware commands.
 * <p>
 * The transition rate is absolute; the intensity changes at a constant rate
 * regardless of whether the change is a big one or a small one.
 *
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
 * @author Dave Duchamp Copyright (C) 2004, 2010
 * @author Ken Cameron Copyright (C) 2008
 * @author Bob Jacobsen Copyright (C) 2008
 */
public interface Light extends DigitalIO, AnalogIO {

    /**
     * State value indicating output intensity is less than maxIntensity and
     * more than minIntensity, and no transition is in progress
     */
    public static final int INTERMEDIATE = 0x08;

    /**
     * State value indicating output intensity is currently changing toward
     * higher intensity, and will continue until full ON is reached
     */
    public static final int TRANSITIONINGTOFULLON = 0x310;

    /**
     * State value indicating output intensity is currently changing toward
     * higher intensity. The current transition will stop before full ON is
     * reached.
     */
    public static final int TRANSITIONINGHIGHER = 0x210;

    /**
     * State value indicating output intensity is currently changing toward
     * lower intensity. The current transition will stop before full OFF is
     * reached.
     */
    public static final int TRANSITIONINGLOWER = 0x110;

    /**
     * State value indicating output intensity is currently changing toward
     * lower intensity, and will continue until full OFF is reached
     */
    public static final int TRANSITIONINGTOFULLOFF = 0x010;

    /**
     * State value mask representing status where output is changing due to a
     * request to transition.
     */
    public static final int TRANSITIONING = 0x010;
    
    /** {@inheritDoc} */
    @Override
    default public boolean isConsistentState() {
        return (getState() == DigitalIO.ON)
                || (getState() == DigitalIO.OFF)
                || (getState() == INTERMEDIATE);
    }
    
    /** {@inheritDoc} */
    @Override
    default public boolean isConsistentValue() {
        // Assume that the value is consistent if the state is consistent.
        return isConsistentState();
    }
    
    /** {@inheritDoc} */
    @Override
    @InvokeOnLayoutThread
    default public void setCommandedState(int s) {
        setState(s);
    }
    
    /** {@inheritDoc} */
    @Override
    default public int getCommandedState() {
        return getState();
    }
    
    /** {@inheritDoc} */
    @Override
    default public int getKnownState() {
        return getState();
    }
    
    /** {@inheritDoc} */
    @Override
    @InvokeOnLayoutThread
    default public void requestUpdateFromLayout() {
        // Do nothing
    }

    /**
     * Set the demanded output state. Valid values are ON and OFF. ON
     * corresponds to the maxIntensity setting, and OFF corresponds to
     * minIntensity.
     * <p>
     * Bound parameter.
     * <p>
     * Note that the state may have other values, such as INTERMEDIATE or a form
     * of transitioning, but that these may not be directly set.
     * <p>
     * @param newState the new desired state
     * @throws IllegalArgumentException if invalid newState provided
     */
    @Override
    @InvokeOnLayoutThread
    public void setState(int newState);

    /**
     * Get the current state of the Light's output.
     */
    @Override
    public int getState();

    // control types - types defined
    public static final int NO_CONTROL = 0x00;
    public static final int SENSOR_CONTROL = 0x01;
    public static final int FAST_CLOCK_CONTROL = 0x02;
    public static final int TURNOUT_STATUS_CONTROL = 0x03;
    public static final int TIMED_ON_CONTROL = 0x04;
    public static final int TWO_SENSOR_CONTROL = 0x05;

    /**
     * Check if this object can handle variable intensity.
     * <p>
     * Unbound property.
     *
     * @return false if only ON/OFF is available.
     */
    public boolean isIntensityVariable();

    /**
     * Set the intended new intensity value for the Light. If transitions are in
     * use, they will be applied.
     * <p>
     * Bound property between 0 and 1.
     * <p>
     * A value of 0.0 corresponds to full off, and a value of 1.0 corresponds to
     * full on.
     * <p>
     * Attempting to set a value below the MinIntensity property value will
     * result in MinIntensity being set. Similarly, setting a value above
     * MaxIntensity will result in MaxINtensity being set.
     * <p>
     * Setting the intensity to the value of the MinIntensity property will
     * result in the Light going to the OFF state at the end of the transition.
     * Similarly, setting the intensity to the MaxIntensity value will result in
     * the Light going to the ON state at the end of the transition.
     * <p>
     * All others result in the INTERMEDIATE state.
     * <p>
     * Light implementations with isIntensityVariable false may not have their
     * TargetIntensity set to values between MinIntensity and MaxIntensity,
     * which would result in the INTERMEDIATE state, as that is invalid for
     * them.
     * <p>
     * If a non-zero value is set in the transitionTime property, the state will
     * be one of TRANSITIONTOFULLON, TRANSITIONHIGHER, TRANSITIONLOWER or
     * TRANSITIONTOFULLOFF until the transition is complete.
     *
 * @param intensity the desired brightness
     * @throws IllegalArgumentException when intensity is less than 0.0 or more
     *                                  than 1.0
     * @throws IllegalArgumentException if isIntensityVariable is false and the
     *                                  new value is between MaxIntensity and
     *                                  MinIntensity
     */
    @InvokeOnLayoutThread
    public void setTargetIntensity(double intensity);

    /**
     * Get the current intensity value. If the Light is currently transitioning,
     * this may be either an intermediate or final value.
     * <p>
     * A value of 0.0 corresponds to full off, and a value of 1.0 corresponds to
     * full on.
     *
     * @return the current brightness
     */
    public double getCurrentIntensity();

    /**
     * Get the target intensity value for the current transition, if any. If the
     * Light is not currently transitioning, this is the current intensity
     * value.
     * <p>
     * A value of 0.0 corresponds to full off, and a value of 1.0 corresponds to
     * full on.
     * <p>
     * Bound property
     *
     * @return the desired brightness
     */
    public double getTargetIntensity();

    /**
     * Set the value of the maxIntensity property.
     * <p>
     * Bound property between 0 and 1.
     * <p>
     * A value of 0.0 corresponds to full off, and a value of 1.0 corresponds to
     * full on.
     *
     * @param intensity the maximum brightness
     * @throws IllegalArgumentException when intensity is less than 0.0 or more
     *                                  than 1.0
     * @throws IllegalArgumentException when intensity is not greater than the
     *                                  current value of the minIntensity
     *                                  property
     */
    @InvokeOnLayoutThread
    public void setMaxIntensity(double intensity);

    /**
     * Get the current value of the maxIntensity property.
     * <p>
     * A value of 0.0 corresponds to full off, and a value of 1.0 corresponds to
     * full on.
     *
     * @return the maximum brightness
     */
    public double getMaxIntensity();

    /**
     * Set the value of the minIntensity property.
     * <p>
     * Bound property between 0 and 1.
     * <p>
     * A value of 0.0 corresponds to full off, and a value of 1.0 corresponds to
     * full on.
     *
     * @param intensity the minimum brightness
     * @throws IllegalArgumentException when intensity is less than 0.0 or more
     *                                  than 1.0
     * @throws IllegalArgumentException when intensity is not less than the
     *                                  current value of the maxIntensity
     *                                  property
     */
    @InvokeOnLayoutThread
    public void setMinIntensity(double intensity);

    /**
     * Get the current value of the minIntensity property.
     * <p>
     * A value of 0.0 corresponds to full off, and a value of 1.0 corresponds to
     * full on.
     *
     * @return the minimum brightness
     */
    public double getMinIntensity();

    /**
     * Can the Light change its intensity setting slowly?
     * <p>
     * If true, this Light supports a non-zero value of the transitionTime
     * property, which controls how long the Light will take to change from one
     * intensity level to another.
     * <p>
     * Unbound property
     *
     * @return true if brightness can fade between two states; false otherwise
     */
    public boolean isTransitionAvailable();

    /**
     * Set the fast-clock duration for a transition from full ON to full OFF or
     * vice-versa.
     * <p>
     * Note there is no guarantee of how this scales when other changes in
     * intensity take place. In particular, some Light implementations will
     * change at a constant fraction per fastclock minute and some will take a
     * fixed duration regardless of the size of the intensity change.
     * <p>
     * Bound property
     * <p>
     * @param minutes time to fade
     * @throws IllegalArgumentException if isTransitionAvailable() is false and
     *                                  minutes is not 0.0
     * @throws IllegalArgumentException if minutes is negative
     */
    @InvokeOnLayoutThread
    public void setTransitionTime(double minutes);

    /**
     * Get the number of fastclock minutes taken by a transition from full ON to
     * full OFF or vice versa.
     * <p>
     * @return 0.0 if the output intensity transition is instantaneous
     */
    public double getTransitionTime();

    /**
     * Convenience method for checking if the intensity of the light is
     * currently changing due to a transition.
     * <p>
     * Bound property so that listeners can conveniently learn when the
     * transition is over.
     *
     * @return true if light is between two states; false otherwise
     */
    public boolean isTransitioning();

    // LightControl information management methods
     
    /**
     * Clears (removes) all LightControl objects for this light
     */
    public void clearLightControls();

    /** 
     * Add a LightControl to this Light.
     * <p>
     * Duplicates are considered the same, hence not added
     */
    public void addLightControl(@Nonnull jmri.implementation.LightControl c);

    /**
     * @return a list of all LightControls
     */
    @Nonnull
    public ArrayList<LightControl> getLightControlList();

    /**
     * Set the Enabled property, which determines whether the control logic
     * built in the light object is operating or not. Light objects are usually
     * enabled.
     *
     * @param state true if control logic is enabled; false otherwise
     */
    @InvokeOnLayoutThread
    public void setEnabled(boolean state);

    /**
     * Get the Enabled property, which determines whether the control logic
     * built in the light object is operating or not.
     *
     * @return true if control logic is enabled; false otherwise
     */
    public boolean getEnabled();

    /**
     * Activates a Light. This method activates each LightControl, setting up a
     * control mechanism, appropriate to its control type.
     */
    @InvokeOnLayoutThread
    public void activateLight();

    /**
     * Deactivates a Light. This method deactivates each LightControl, shutting
     * down its control mechanism.
     */
    @InvokeOnLayoutThread
    public void deactivateLight();
}

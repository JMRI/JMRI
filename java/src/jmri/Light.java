package jmri;

import java.util.List;
import javax.annotation.Nonnull;

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
public interface Light extends DigitalIO {

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
                || (getState() == DigitalIO.OFF);
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

    // LightControl information management methods
     
    /**
     * Clears (removes) all LightControl objects for this light
     */
    public void clearLightControls();

    /** 
     * Add a LightControl to this Light.
     * <p>
     * Duplicates are considered the same, hence not added
     * @param c the light control to add.
     */
    public void addLightControl(@Nonnull LightControl c);

    /**
     * @return a list of all LightControls
     */
    @Nonnull
    public List<LightControl> getLightControlList();

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

package jmri.managers;

import jmri.AudioManager;
import jmri.BlockManager;
import jmri.CatalogTreeManager;
import jmri.ClockControl;
import jmri.ConditionalManager;
import jmri.IdTagManager;
import jmri.InstanceManager;
import jmri.LightManager;
import jmri.LogixManager;
import jmri.MemoryManager;
import jmri.ProgrammerManager;
import jmri.ReporterManager;
import jmri.RouteManager;
import jmri.SectionManager;
import jmri.SensorManager;
import jmri.SignalGroupManager;
import jmri.SignalHeadManager;
import jmri.SignalMastLogicManager;
import jmri.SignalMastManager;
import jmri.SignalSystemManager;
import jmri.Timebase;
import jmri.TransitManager;
import jmri.TurnoutManager;
import jmri.implementation.DefaultClockControl;
import jmri.jmrit.audio.DefaultAudioManager;
import jmri.jmrit.catalog.DefaultCatalogTreeManager;
import jmri.jmrit.roster.RosterIconFactory;
import jmri.jmrit.vsdecoder.VSDecoderManager;

/**
 * Provide the usual default implementations for the
 * {@link jmri.InstanceManager}.
 * <P>
 * Not all {@link jmri.InstanceManager} related classes are provided by this
 * class. See the discussion in {@link jmri.InstanceManager} of initilization
 * methods.
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <P>
 * @author	Bob Jacobsen Copyright (C) 2001, 2008, 2014
 * @since 2.9.4
 */
public class DefaultInstanceInitializer implements jmri.InstanceInitializer {

    public <T> Object getDefault(Class<T> type) {

        if (type == AudioManager.class) {
            return DefaultAudioManager.instance();
        }

        // @TODO Should do "implements InstanceManagerAutoDefault" instead
        if (type == BlockManager.class) {
            return new BlockManager();
        }

        if (type == CatalogTreeManager.class) {
            return new DefaultCatalogTreeManager();
        }

        if (type == ClockControl.class) {
            return new DefaultClockControl();
        }

        if (type == ConditionalManager.class) {
            return new DefaultConditionalManager();
        }

        if (type == IdTagManager.class) {
            return new DefaultIdTagManager();
        }

        if (type == LightManager.class) {
            return new jmri.managers.ProxyLightManager();
        }

        if (type == LogixManager.class) {
            return new DefaultLogixManager();
        }

        if (type == MemoryManager.class) {
            return new DefaultMemoryManager();
        }

        if (type == ProgrammerManager.class) {
            return new DeferringProgrammerManager();
        }

        if (type == ReporterManager.class) {
            return new jmri.managers.ProxyReporterManager();
        }

        if (type == RosterIconFactory.class) {
            return RosterIconFactory.instance();
        }

        if (type == RouteManager.class) {
            return new DefaultRouteManager();
        }

        if (type == SensorManager.class) {
            return new jmri.managers.ProxySensorManager();
        }

        // @TODO Should do "implements InstanceManagerAutoDefault" instead
        if (type == SectionManager.class) {
            return new SectionManager();
        }

        if (type == SignalGroupManager.class) {
            // ensure signal mast manager exists first
            InstanceManager.getDefault(jmri.SignalMastManager.class);
            return new DefaultSignalGroupManager();
        }

        if (type == SignalHeadManager.class) {
            return new AbstractSignalHeadManager();
        }

        if (type == SignalMastLogicManager.class) {
            return new DefaultSignalMastLogicManager();
        }

        if (type == SignalMastManager.class) {
            // ensure signal head manager exists first
            InstanceManager.getDefault(jmri.SignalHeadManager.class);
            return new DefaultSignalMastManager();
        }

        if (type == SignalSystemManager.class) {
            return new DefaultSignalSystemManager();
        }

        if (type == Timebase.class) {
            Timebase timebase = new jmri.jmrit.simpleclock.SimpleTimebase();
            if (InstanceManager.getOptionalDefault(jmri.ConfigureManager.class) != null) {
                InstanceManager.getDefault(jmri.ConfigureManager.class).registerConfig(timebase, jmri.Manager.TIMEBASE);
            }
            return timebase;
        }

        // @TODO Should do "implements InstanceManagerAutoDefault" instead
        if (type == TransitManager.class) {
            return new TransitManager();
        }

        if (type == TurnoutManager.class) {
            return new jmri.managers.ProxyTurnoutManager();
        }

        if (type == VSDecoderManager.class) {
            return VSDecoderManager.instance();
        }

        // Nothing found
        return null;
    }

}

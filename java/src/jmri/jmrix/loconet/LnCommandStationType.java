package jmri.jmrix.loconet;

import jmri.ThrottleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enum to carry command-station specific information for LocoNet
 * implementations.
 * <p>
 * Because you can't inherit and extend enums, this will accumulate information
 * from subtypes. We use reflection to deal with that.
 * <p>
 * This is (slowly) centralizing all of the command-station-specific
 * dependencies for startup. It does _not_ handle the connection-specific
 * dependencies for e.g. the connections via networks and Uhlenbrock serial/USB;
 * those are still done via port adapters, special packetizers et al.
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
 * @author Bob Jacobsen Copyright (C) 2014
 * @author B. Milhaupt Copyright (C) 2018
 */
@javax.annotation.concurrent.Immutable
public enum LnCommandStationType {

    //  enum value(name, canRead, progEndOp, ThrottleManager, SlotManager, supportsIdle, supportsMultimeter
    COMMAND_STATION_DCS100("DCS100 (Chief)",  // NOI18N
            ReadsFromServiceModeTrack.CAN_READ_ON_SVC_TRACK,
            ProgDepowersTrack.TRACK_OFF_WHEN_PROGRAMMING,
            "LnThrottleManager", "SlotManager",  // NOI18N
            IdleSupport.SUPPORTS_OPC_IDLE,
            MultiMeterSupport.NO_MULTIMETER_SUPPORT,
            LocoResetSupport.NO_LOCO_RESET_SUPPORT),
    COMMAND_STATION_DCS240("DCS240 (Advanced Command Station)",  // NOI18N
            ReadsFromServiceModeTrack.CAN_READ_ON_SVC_TRACK,
            ProgDepowersTrack.TRACK_OFF_WHEN_PROGRAMMING,
            "LnThrottleManager", "SlotManager",  // NOI18N
            IdleSupport.SUPPORTS_OPC_IDLE,
            MultiMeterSupport.SUPPORTS_MULTIMETER_FUNCTION,
            LocoResetSupport.SUPPORTS_LOCO_RESET_FUNCTION),
    COMMAND_STATION_DCS210("DCS210 (Evolution Command Station)",  // NOI18N
            ReadsFromServiceModeTrack.CAN_READ_ON_SVC_TRACK,
            ProgDepowersTrack.TRACK_OFF_WHEN_PROGRAMMING,
            "LnThrottleManager", "SlotManager",  // NOI18N
            IdleSupport.SUPPORTS_OPC_IDLE,
            MultiMeterSupport.SUPPORTS_MULTIMETER_FUNCTION,
            LocoResetSupport.SUPPORTS_LOCO_RESET_FUNCTION),
    COMMAND_STATION_DCS200("DCS200",  // NOI18N
            ReadsFromServiceModeTrack.CAN_READ_ON_SVC_TRACK,
            ProgDepowersTrack.TRACK_OFF_WHEN_PROGRAMMING,
            "LnThrottleManager", "SlotManager",  // NOI18N
            IdleSupport.SUPPORTS_OPC_IDLE,
            MultiMeterSupport.NO_MULTIMETER_SUPPORT,
            LocoResetSupport.NO_LOCO_RESET_SUPPORT),
    COMMAND_STATION_DCS050("DCS50 (Zephyr)",  // NOI18N
            ReadsFromServiceModeTrack.CAN_READ_ON_SVC_TRACK,
            ProgDepowersTrack.TRACK_OFF_WHEN_PROGRAMMING,
            "LnThrottleManager", "SlotManager",  // NOI18N
            IdleSupport.NO_OPC_IDLE_SUPPORT,
            MultiMeterSupport.NO_MULTIMETER_SUPPORT,
            LocoResetSupport.NO_LOCO_RESET_SUPPORT),
    COMMAND_STATION_DCS051("DCS51 (Zephyr Xtra)",  // NOI18N
            ReadsFromServiceModeTrack.CAN_READ_ON_SVC_TRACK,
            ProgDepowersTrack.TRACK_OFF_WHEN_PROGRAMMING,
            "LnThrottleManager", "SlotManager",  // NOI18N
            IdleSupport.NO_OPC_IDLE_SUPPORT,
            MultiMeterSupport.NO_MULTIMETER_SUPPORT,
            LocoResetSupport.NO_LOCO_RESET_SUPPORT),
    COMMAND_STATION_DCS052("DCS52 (Zephyr Express)", // NOI18N
            ReadsFromServiceModeTrack.CAN_READ_ON_SVC_TRACK,
            ProgDepowersTrack.TRACK_OFF_WHEN_PROGRAMMING,
            "LnThrottleManager", "SlotManager",  // NOI18N
            IdleSupport.NO_OPC_IDLE_SUPPORT,
            MultiMeterSupport.NO_MULTIMETER_SUPPORT,
            LocoResetSupport.SUPPORTS_LOCO_RESET_FUNCTION),
    COMMAND_STATION_DB150("DB150 (Empire Builder)", // NOI18N
            ReadsFromServiceModeTrack.NO_SVC_MODE_READS,
            ProgDepowersTrack.TRACK_ALIVE_WHEN_PROGRAMMING,
            "LnThrottleManager", "SlotManager",  // NOI18N
            IdleSupport.SUPPORTS_OPC_IDLE,
            MultiMeterSupport.NO_MULTIMETER_SUPPORT,
            LocoResetSupport.NO_LOCO_RESET_SUPPORT),

    // the following command stations are assumed to not support "OPC_IDLE"
    COMMAND_STATION_LBPS("LocoBuffer (PS)",  // NOI18N
            ReadsFromServiceModeTrack.CAN_READ_ON_SVC_TRACK,
            ProgDepowersTrack.TRACK_OFF_WHEN_PROGRAMMING,
            "LnThrottleManager", "SlotManager",  // NOI18N
            IdleSupport.NO_OPC_IDLE_SUPPORT,
            MultiMeterSupport.NO_MULTIMETER_SUPPORT,
            LocoResetSupport.NO_LOCO_RESET_SUPPORT),
    COMMAND_STATION_MM("Mix-Master",  // NOI18N
            ReadsFromServiceModeTrack.NO_SVC_MODE_READS,
            ProgDepowersTrack.TRACK_ALIVE_WHEN_PROGRAMMING,
            "LnThrottleManager", "SlotManager",  // NOI18N
            IdleSupport.NO_OPC_IDLE_SUPPORT,
            MultiMeterSupport.NO_MULTIMETER_SUPPORT,
            LocoResetSupport.NO_LOCO_RESET_SUPPORT),
    COMMAND_STATION_IBX_TYPE_1("Intellibox-I",  // NOI18N
            ReadsFromServiceModeTrack.CAN_READ_ON_SVC_TRACK,
            ProgDepowersTrack.TRACK_ALIVE_WHEN_PROGRAMMING,
            "Ib1ThrottleManager", "SlotManager",  // NOI18N
            IdleSupport.NO_OPC_IDLE_SUPPORT,
            MultiMeterSupport.NO_MULTIMETER_SUPPORT,
            LocoResetSupport.NO_LOCO_RESET_SUPPORT),
    COMMAND_STATION_IBX_TYPE_2("Intellibox-II",  // NOI18N
            ReadsFromServiceModeTrack.CAN_READ_ON_SVC_TRACK,
            ProgDepowersTrack.TRACK_ALIVE_WHEN_PROGRAMMING,
            "Ib2ThrottleManager", "UhlenbrockSlotManager",  // NOI18N
            IdleSupport.NO_OPC_IDLE_SUPPORT,
            MultiMeterSupport.NO_MULTIMETER_SUPPORT,
            LocoResetSupport.NO_LOCO_RESET_SUPPORT),

    // the following command stations are known to not support "OPC_IDLE"
    COMMAND_STATION_PR3_ALONE("PR3 standalone programmer",  // NOI18N
            ReadsFromServiceModeTrack.CAN_READ_ON_SVC_TRACK,
            ProgDepowersTrack.TRACK_OFF_WHEN_PROGRAMMING,
            "LnThrottleManager", "SlotManager",  // NOI18N
            IdleSupport.NO_OPC_IDLE_SUPPORT,
            MultiMeterSupport.NO_MULTIMETER_SUPPORT,
            LocoResetSupport.NO_LOCO_RESET_SUPPORT),
    COMMAND_STATION_PR2_ALONE("PR2 standalone programmer",  // NOI18N
            ReadsFromServiceModeTrack.CAN_READ_ON_SVC_TRACK,
            ProgDepowersTrack.TRACK_OFF_WHEN_PROGRAMMING,
            "LnThrottleManager", "SlotManager",  // NOI18N
            IdleSupport.NO_OPC_IDLE_SUPPORT,
            MultiMeterSupport.NO_MULTIMETER_SUPPORT,
            LocoResetSupport.NO_LOCO_RESET_SUPPORT),
    COMMAND_STATION_STANDALONE("Stand-alone LocoNet",  // NOI18N
            ReadsFromServiceModeTrack.NO_SVC_MODE_READS,
            ProgDepowersTrack.TRACK_OFF_WHEN_PROGRAMMING,
            "LnThrottleManager", "SlotManager",  // NOI18N
            IdleSupport.NO_OPC_IDLE_SUPPORT,
            MultiMeterSupport.NO_MULTIMETER_SUPPORT,
            LocoResetSupport.NO_LOCO_RESET_SUPPORT),
    COMMAND_STATION_PR4_ALONE("PR4 standalone programmer",  // NOI18N
            ReadsFromServiceModeTrack.CAN_READ_ON_SVC_TRACK,
            ProgDepowersTrack.TRACK_OFF_WHEN_PROGRAMMING,
            "LnThrottleManager", "SlotManager",  // NOI18N
            IdleSupport.NO_OPC_IDLE_SUPPORT,
            MultiMeterSupport.NO_MULTIMETER_SUPPORT,
            LocoResetSupport.NO_LOCO_RESET_SUPPORT),
    COMMAND_STATION_USB_DCS240_ALONE("DCS240 USB interface as standalone programmer", // NOI18N
            ReadsFromServiceModeTrack.CAN_READ_ON_SVC_TRACK,
            ProgDepowersTrack.TRACK_OFF_WHEN_PROGRAMMING,
            "LnThrottleManager", "SlotManager",  // NOI18N
            IdleSupport.NO_OPC_IDLE_SUPPORT,
            MultiMeterSupport.NO_MULTIMETER_SUPPORT,
            LocoResetSupport.NO_LOCO_RESET_SUPPORT),
    COMMAND_STATION_USB_DCS52_ALONE("DCS52 USB interface as standalone programmer",  // NOI18N
            ReadsFromServiceModeTrack.CAN_READ_ON_SVC_TRACK,
            ProgDepowersTrack.TRACK_OFF_WHEN_PROGRAMMING,
            "LnThrottleManager", "SlotManager",
            IdleSupport.NO_OPC_IDLE_SUPPORT, // NOI18N
            MultiMeterSupport.NO_MULTIMETER_SUPPORT,
            LocoResetSupport.NO_LOCO_RESET_SUPPORT);

    // Note that the convention is that the first word (space-separated token) of the name is the
    // name of a configuration file for loconet.cmdstnconfig
    LnCommandStationType(String name, ReadsFromServiceModeTrack canRead,
            ProgDepowersTrack progEndOp,
            String throttleClassName, String slotManagerClassName,
            IdleSupport supportsIdle, MultiMeterSupport supportMultiMeter,
            LocoResetSupport supportsLocoReset) {
        this.name = name;
        this.canRead = canRead;
        this.progEndOp = progEndOp;
        this.throttleClassName = throttleClassName;
        this.slotManagerClassName = slotManagerClassName;
        this.supportsIdle = supportsIdle;
        this.supportsMultiMeter = supportMultiMeter;
        this.supportsLocoReset = supportsLocoReset;
    }

    final String name;
    final ReadsFromServiceModeTrack canRead;
    final ProgDepowersTrack progEndOp;
    final String throttleClassName;
    final String slotManagerClassName;
    final IdleSupport supportsIdle;
    final MultiMeterSupport supportsMultiMeter;
    final LocoResetSupport supportsLocoReset;

    public String getName() {
        return name;
    }

    /**
     * Can this command station read back from decoders?
     * @return whether the command station can perform CV reads
     */
    public boolean getCanRead() {
        return canRead == ReadsFromServiceModeTrack.CAN_READ_ON_SVC_TRACK;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Does a programming operation turn track power off?
     * @return whether the programming operation turns track power off
     */
    public boolean getProgPowersOff() {
        return progEndOp == ProgDepowersTrack.TRACK_OFF_WHEN_PROGRAMMING;
    }

    static public LnCommandStationType getByName(String name) {
        for (LnCommandStationType p : LnCommandStationType.values()) {
            if (p.name.equals(name)) {
                return p;
            }
        }
        throw new java.lang.IllegalArgumentException("argument value [" + name + "] not valid"); // NOI18N
    }

    /**
     * Get a new ThrottleManager of the right type for this command station.
     *
     * @param memo the LocoNetSystemConnectionMemo object which hosts throttles
     * @return the ThrottleManager object for the connection and the command station
     */
    public ThrottleManager getThrottleManager(LocoNetSystemConnectionMemo memo) {
        try {
            // uses reflection
            String className = "jmri.jmrix.loconet." + throttleClassName; // NOI18N
            log.debug("attempting to create {}", className); // NOI18N
            Class<?> c = Class.forName(className);
            java.lang.reflect.Constructor<?>[] allConstructors = c.getDeclaredConstructors();
            for (java.lang.reflect.Constructor<?> ctor : allConstructors) {
                Class<?>[] pType = ctor.getParameterTypes();
                if (pType.length == 1 && pType[0].equals(LocoNetSystemConnectionMemo.class)) {
                    // this is the right ctor
                    return (ThrottleManager) ctor.newInstance(memo);
                }
            }
            log.error("Did not find a throttle ctor for {}", name);
        } catch (ClassNotFoundException e1) {
            log.error("Could not find class for throttle manager from type {} in enum element {}", throttleClassName, name);
        } catch (InstantiationException e2) {
            log.error("Could not create throttle manager object from type {} in enum element {}", throttleClassName, name, e2);
        } catch (IllegalAccessException e3) {
            log.error("Access error creating throttle manager object from type {} in enum element {}", throttleClassName, name, e3);
        } catch (java.lang.reflect.InvocationTargetException e4) {
            log.error("Invocation error while creating throttle manager object from type {} in enum element {}", throttleClassName, name, e4);
        }
        return null;
    }

    /**
     * Get a new SlotManager of the right type for this command station.
     *
     * @param tc the LnTrafficController object which hosts the slot manager
     * @return the SlogManager object for the connection and the command station
     */
    public SlotManager getSlotManager(LnTrafficController tc) {
        try {
            // uses reflection
            String className = "jmri.jmrix.loconet." + slotManagerClassName; // NOI18N
            log.debug("attempting to create {}", className);
            Class<?> c = Class.forName(className);
            java.lang.reflect.Constructor<?>[] allConstructors = c.getDeclaredConstructors();
            for (java.lang.reflect.Constructor<?> ctor : allConstructors) {
                Class<?>[] pType = ctor.getParameterTypes();
                if (pType.length == 1 && pType[0].equals(LnTrafficController.class)) {
                    // this is the correct ctor
                    return (SlotManager) ctor.newInstance(tc);
                }
            }
            log.error("Did not find a slotmanager ctor for {}", name);
        } catch (ClassNotFoundException e1) {
            log.error("Could not find class for slotmanager from type {} in enum element {}", slotManagerClassName, name);
        } catch (InstantiationException e2) {
            log.error("Could not create slotmanager object from type {} in enum element {}", slotManagerClassName, name, e2);
        } catch (IllegalAccessException e3) {
            log.error("Access error creating slotmanager object from type {} in enum element {}", slotManagerClassName, name, e3);
        } catch (java.lang.reflect.InvocationTargetException e4) {
            log.error("Invocation error while creating slotmanager object from type {} in enum element {}", slotManagerClassName, name, e4);
        }
        return null;
    }

    /**
     * Returns command station's support for OPC_IDLE
     *
     * @return true if OPC_IDLE forces broadcast of "stop", else false
     */
    public boolean getImplementsIdle() {
        return supportsIdle == IdleSupport.SUPPORTS_OPC_IDLE;
    }

    /**
     * Returns whether CS supports a multimeter
     * @return true is Multimeter support
     */
    public boolean getSupportsMultimeter() {
        return supportsMultiMeter == MultiMeterSupport.SUPPORTS_MULTIMETER_FUNCTION;
    }

    /**
     * Returns whether CS supports a Loco Reset feature.
     *
     * For this purpose "supports" means that the command station will send
     * OPC_RE_LOCO_RESET when it clears all slots.
     *
     * @return true if command station supports OPC_RE_LOCO_RESET message
     */
    public boolean getSupportsLocoReset() {

        return supportsLocoReset == LocoResetSupport.SUPPORTS_LOCO_RESET_FUNCTION;
    }

    protected enum ReadsFromServiceModeTrack {
        NO_SVC_MODE_READS, CAN_READ_ON_SVC_TRACK
    }

    protected enum ProgDepowersTrack {
        TRACK_OFF_WHEN_PROGRAMMING, TRACK_ALIVE_WHEN_PROGRAMMING
    }

    protected enum IdleSupport {
        NO_OPC_IDLE_SUPPORT, SUPPORTS_OPC_IDLE
    }

    protected enum MultiMeterSupport {
        NO_MULTIMETER_SUPPORT, SUPPORTS_MULTIMETER_FUNCTION
    }

    protected enum LocoResetSupport {
        NO_LOCO_RESET_SUPPORT, SUPPORTS_LOCO_RESET_FUNCTION
    }


    private final static Logger log = LoggerFactory.getLogger(LnCommandStationType.class);
}

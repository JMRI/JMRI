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
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <P>
 * @author	Bob Jacobsen Copyright (C) 2014
 */
@net.jcip.annotations.Immutable
public enum LnCommandStationType {

    //  enum value                 name                         canRead progEndOp   ThrottleManager      SlotManager
    COMMAND_STATION_DCS100("DCS100 (Chief)", true, false, "LnThrottleManager", "SlotManager"), // NOI18N
    COMMAND_STATION_DCS240("DCS240 (Advanced Command Station)", true, false, "LnThrottleManager", "SlotManager"), // NOI18N
    COMMAND_STATION_DCS200("DCS200", true, false, "LnThrottleManager", "SlotManager"), // NOI18N
    COMMAND_STATION_DCS050("DCS50 (Zephyr)", true, false, "LnThrottleManager", "SlotManager"), // NOI18N
    COMMAND_STATION_DCS051("DCS51 (Zephyr Xtra)", true, false, "LnThrottleManager", "SlotManager"), // NOI18N
    COMMAND_STATION_DB150("DB150 (Empire Builder)", false, true, "LnThrottleManager", "SlotManager"), // NOI18N
    COMMAND_STATION_LBPS("LocoBuffer (PS)", true, false, "LnThrottleManager", "SlotManager"), // NOI18N
    COMMAND_STATION_MM("Mix-Master", false, true, "LnThrottleManager", "SlotManager"), // NOI18N
    COMMAND_STATION_IBX_TYPE_1("Intellibox-I", true, true, "Ib1ThrottleManager", "SlotManager"), // NOI18N
    COMMAND_STATION_IBX_TYPE_2("Intellibox-II", true, true, "Ib2ThrottleManager", "UhlenbrockSlotManager"), // NOI18N

    COMMAND_STATION_PR3_ALONE("PR3 standalone programmer", true, false, "LnThrottleManager", "SlotManager"), // NOI18N
    COMMAND_STATION_PR2_ALONE("PR2 standalone programmer", true, false, "LnThrottleManager", "SlotManager"), // NOI18N
    COMMAND_STATION_STANDALONE("Stand-alone LocoNet", false, false, "LnThrottleManager", "SlotManager");  // NOI18N

    // Note that the convention is that the first word (space-separated token) of the name is the
    // name of a configuration file for loconet.cmdstnconfig
    LnCommandStationType(String name, boolean canRead, boolean progEndOp, String throttleClassName, String slotManagerClassName) {
        this.name = name;
        this.canRead = canRead;
        this.progEndOp = progEndOp;
        this.throttleClassName = throttleClassName;
        this.slotManagerClassName = slotManagerClassName;
    }

    final String name;
    final boolean canRead;
    final boolean progEndOp;
    final String throttleClassName;
    final String slotManagerClassName;

    public String getName() {
        return name;
    }

    /**
     * Can this command station read back from decoders?
     */
    public boolean getCanRead() {
        return canRead;
    }

    public String toString() {
        return name;
    }

    /**
     * Does a programming operation turn track power off?
     */
    public boolean getProgPowersOff() {
        return progEndOp;
    }

    static public LnCommandStationType getByName(String name) {
        for (LnCommandStationType p : LnCommandStationType.values()) {
            if (p.name.equals(name)) {
                return p;
            }
        }
        throw new java.lang.IllegalArgumentException("argument value [" + name + "] not valid");
    }

    /**
     * Get a new ThrottleManager of the right type for this command station.
     */
    public ThrottleManager getThrottleManager(LocoNetSystemConnectionMemo memo) {
        try {
            // uses reflection
            String className = "jmri.jmrix.loconet." + throttleClassName;
            log.debug("attempting to create {}", className);
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
     */
    public SlotManager getSlotManager(LnTrafficController tc) {
        try {
            // uses reflection
            String className = "jmri.jmrix.loconet." + slotManagerClassName;
            log.debug("attempting to create {}", className);
            Class<?> c = Class.forName(className);
            java.lang.reflect.Constructor<?>[] allConstructors = c.getDeclaredConstructors();
            for (java.lang.reflect.Constructor<?> ctor : allConstructors) {
                Class<?>[] pType = ctor.getParameterTypes();
                if (pType.length == 1 && pType[0].equals(LnTrafficController.class)) {
                    // this is the right ctor
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

    private final static Logger log = LoggerFactory.getLogger(LnCommandStationType.class.getName());
}

package jmri;

import java.util.EnumSet;

/**
 * DCC Speed Step Mode.
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
 * @author Austin Hendrix Copyright (C) 2019
  * with edits/additions by
 * @author Timothy Jump Copyright (C) 2025
 */
@javax.annotation.concurrent.Immutable
public enum SpeedStepMode {
    // NOTE: keep these up to date with xml/schema/locomotive-config.xsd
    UNKNOWN("unknown", 1, 0.0f, "SpeedStepUnknown"),
    // NMRA DCC standard speed step modes.
    NMRA_DCC_128("128", 126, "SpeedStep128"), // Remember there are only 126 non-stop values in 128 speed.
    NMRA_DCC_28("28", 28, "SpeedStep28"),
    NMRA_DCC_27("27", 27, "SpeedStep27"),
    NMRA_DCC_14("14", 14, "SpeedStep14"),
    // Non-DCC speed step modes.
    MOTOROLA_28("motorola_28", 28, "SpeedStep28Motorola"), // Motorola 28 speed step mode.
    TMCC1_32("tmcc1_32", 32, "SpeedStep32TMCC1"), // Lionel TMCC 1, 32 speed step mode.
    TMCC2_32("tmcc2_32", 32, "SpeedStep32TMCC2"), // Lionel TMCC 2 Legacy, 32 speed step mode.
    TMCC1_100("tmcc1_100", 100, "SpeedStep100TMCC1"), // Lionel TMCC 1, 100 "relative" speed step mode.
    TMCC2_200("tmcc2_200", 200, "SpeedStep200TMCC2"), // Lionel TMCC 2 Legacy, 200 speed step mode.
    TMCC1TR_32("tmcc1tr_32", 32, "SpeedStep32TMCC1TR"), // Lionel TMCC 1 TR, 32 speed step mode.
    TMCC2TR_32("tmcc2tr_32", 32, "SpeedStep32TMCC2TR"), // Lionel TMCC 2 Legacy TR, 32 speed step mode.
    TMCC1TR_100("tmcc1tr_100", 100, "SpeedStep100TMCC1TR"), // Lionel TMCC 1 TR, 100 "relative" speed step mode.
    TMCC2TR_200("tmcc2tr_200", 200, "SpeedStep200TMCC2TR"), // Lionel TMCC 2 Legacy TR, 200 speed step mode.
    INCREMENTAL("incremental", 1, 1.0f, "SpeedStepIncremental");

    SpeedStepMode(String name, int numSteps, String description) {
        this(name, numSteps, 1.0f / numSteps, description);
    }

    SpeedStepMode(String name, int numSteps, float increment, String description) {
        this.name = name;
        this.numSteps = numSteps;
        this.increment = increment;
        this.description = Bundle.getMessage(description);
    }

    public final String name;

    /**
     * The Number of steps, e.g. 126 for DCC 128
     */
    public final int numSteps;

     /**
     * The increment between steps, e.g. 1 / 126 for DCC 128
     */
    public final float increment;
    public final String description;

    /**
     * Get a locale friendly Step Mode Description.
     * For just "128" use name()
     * @return e.g. "128 SS"
     */
    @Override
    public String toString() {
        return description;
    }

    /**
     * Convert a human-readable string to a DCC speed step mode.
     *
     * @param name string version of speed step mode; example "128"
     * @return matching SpeedStepMode
     * @throws IllegalArgumentException if name does not correspond to a valid speed step mode.
     */
    static public SpeedStepMode getByName(String name) {
        for (SpeedStepMode s : SpeedStepMode.values()) {
            if (s.name.equals(name)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Invalid speed step mode: " + name);
    }

    /**
     * Convert a localized name string to a DCC speed step mode.
     *
     * @param name localized string version of speed step mode; example "128"
     * @return matching SpeedStepMode
     * @throws IllegalArgumentException if name does not correspond to a valid speed step mode.
     */
    static public SpeedStepMode getByDescription(String name) {
        for (SpeedStepMode s : SpeedStepMode.values()) {
            if (s.description.equals(name)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Invalid speed step mode: " + name);
    }

    static public EnumSet<SpeedStepMode> getCompatibleModes(
            EnumSet<SpeedStepMode> command_station_modes,
            EnumSet<SpeedStepMode> decoder_modes) {
        EnumSet<SpeedStepMode> result = command_station_modes.clone();
        result.retainAll(decoder_modes);
        return result;
    }

    static public SpeedStepMode bestCompatibleMode(
            EnumSet<SpeedStepMode> command_station_modes,
            EnumSet<SpeedStepMode> decoder_modes) {
        EnumSet<SpeedStepMode> result = getCompatibleModes(command_station_modes, decoder_modes);
        return bestMode(result);
    }

    static public SpeedStepMode bestMode(EnumSet<SpeedStepMode> modes) {
        if(modes.contains(NMRA_DCC_128)) {
            return NMRA_DCC_128;
        } else if(modes.contains(TMCC1_32)) {
            return TMCC1_32;
        } else if(modes.contains(NMRA_DCC_28)) {
            return NMRA_DCC_28;
        } else if(modes.contains(MOTOROLA_28)) {
            return MOTOROLA_28;
        } else if(modes.contains(NMRA_DCC_27)) {
            return NMRA_DCC_27;
        } else if(modes.contains(NMRA_DCC_14)) {
            return NMRA_DCC_14;
        }
        return UNKNOWN;
    }

    static public EnumSet<SpeedStepMode> getCompatibleModesForProtocol(LocoAddress.Protocol protocol) {
        switch (protocol) {
            case DCC:
            case DCC_LONG:
            case DCC_SHORT:
                return EnumSet.of(
                        // NMRA Speed step modes.
                        SpeedStepMode.NMRA_DCC_128,
                        SpeedStepMode.NMRA_DCC_28,
                        SpeedStepMode.NMRA_DCC_27,
                        SpeedStepMode.NMRA_DCC_14,
                        // Incremental speed step mode, used by LENZ XPA
                        // XpressNet Phone Adapter.
                        SpeedStepMode.INCREMENTAL,
                        // TMCC1 mode, since some NMRA decoder models are used
                        // for TMCC1 locomotives.
                        SpeedStepMode.TMCC1_32);
            case MFX:
                return EnumSet.of(
                        // NMRA Speed step modes.
                        SpeedStepMode.NMRA_DCC_128,
                        SpeedStepMode.NMRA_DCC_28,
                        SpeedStepMode.NMRA_DCC_27,
                        SpeedStepMode.NMRA_DCC_14,
                        // Incremental speed step mode, used by LENZ XPA
                        // XpressNet Phone Adapter.
                        SpeedStepMode.INCREMENTAL,
                        // MFX decoders also support Motorola speed step mode.
                        SpeedStepMode.MOTOROLA_28);
            case MOTOROLA:
                return EnumSet.of(SpeedStepMode.MOTOROLA_28);
            case SELECTRIX:
            case M4:
            case OPENLCB:
            case LGB:
                // No compatible speed step modes for these protocols.
                // NOTE: these protocols only appear to be used in conjunction
                // with ECOS.
                break;
            default:
                // Unhandled case; no compatible speed step mode.
                break;
        }
        return EnumSet.noneOf(SpeedStepMode.class);
    }
}

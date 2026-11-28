package jmri.jmrit.roster;

/**
 * Define default throttles settings for NMRA and NEM. 
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
 * @author Lionel Jeanson Copyright (C) 2026
 * 
 * For consistence defaults defined here have to be keep in sync
 * with the one defined as xml throttles layouts in resources/throttle
 * 
 */

public class RosterFunctionEntriesDefaults {

    // NMRA
    static final String NMRA_SHUNTING_FN = "";  // no convention on a default shunting (slow) function
    static final RosterFunctionEntry[] NMRA_FUNCTION_ENTRIES = {
        new RosterFunctionEntry(0, Bundle.getMessage("F0NMRA"),    // Head lights
                "resources/icons/functionicons/svg/lightsOff.svg", "resources/icons/functionicons/svg/lightsOn.svg", true, true),
        new RosterFunctionEntry(1, Bundle.getMessage("F1NMRA"),   // Bell
                "resources/icons/functionicons/transparent_background/Bell.png", "resources/icons/functionicons/transparent_background/BellOn.png", true, true),
        new RosterFunctionEntry(2, Bundle.getMessage("F2NMRA"),   // Horn
                "resources/icons/functionicons/transparent_background/Horn.png","resources/icons/functionicons/transparent_background/HornOn.png", false, true)
    };

    // NEM as per NEM 608
    static final String NEM_SHUNTING_FN = "F5"; 
    private static final RosterFunctionEntry[] NEM_FUNCTION_ENTRIES = {
        new RosterFunctionEntry(0, Bundle.getMessage("F0NEM"),   // Head lights
                "resources/icons/functionicons/svg/lightsOff.svg", "resources/icons/functionicons/svg/lightsOn.svg", true, true),
        new RosterFunctionEntry(1, Bundle.getMessage("F1NEM"),   // Back lights
                "resources/icons/functionicons/svg/redLightsOff.svg", "resources/icons/functionicons/svg/redLightsOn.svg", true, true),
        new RosterFunctionEntry(2, Bundle.getMessage("F2NEM"),   // Whistle
                "resources/icons/functionicons/transparent_background/Horn.png","resources/icons/functionicons/transparent_background/HornOn.png", false, true),
        new RosterFunctionEntry(3, Bundle.getMessage("F3NEM"),   // Sound
                "resources/icons/functionicons/transparent_background/generic_sound.png","resources/icons/functionicons/transparent_background/mute.png", true, true),
        new RosterFunctionEntry(4, Bundle.getMessage("F4NEM"),   // Coupler
                "resources/icons/functionicons/transparent_background/coupler_sound_off.png","resources/icons/functionicons/transparent_background/coupler_sound_on.png", false, true),
        new RosterFunctionEntry(5, Bundle.getMessage("F5NEM"),    // Shunting
                "resources/icons/functionicons/svg/shuntingOff.svg","resources/icons/functionicons/svg/shuntingOn.svg", true, true),

    };
    static final RosterFunctionEntry[] NEM_STEAM_FUNCTION_ENTRIES = {
        NEM_FUNCTION_ENTRIES[0], // Head lights
        NEM_FUNCTION_ENTRIES[1], // Back lights
        NEM_FUNCTION_ENTRIES[2], // Whistle                
        NEM_FUNCTION_ENTRIES[3], // Sound
        NEM_FUNCTION_ENTRIES[4], // Coupler
        NEM_FUNCTION_ENTRIES[5], // Shunting
        new RosterFunctionEntry(6, Bundle.getMessage("F6NEM"),    // Cab lights
                "resources/icons/functionicons/transparent_background/cab_light_off.png","resources/icons/functionicons/transparent_background/cab_light_on.png", true, true),
        new RosterFunctionEntry(7, Bundle.getMessage("F7NEM_FIREBOX"),    // firebox
                "resources/icons/functionicons/transparent_background/firebox_flicker_off.png","resources/icons/functionicons/transparent_background/firebox_flicker_on.png", true, true), // firebox / engine room light
        new RosterFunctionEntry(8, Bundle.getMessage("F8NEM_SMOKE"),    // smoke
                "resources/icons/functionicons/transparent_background/smoke_steam_off.png","resources/icons/functionicons/transparent_background/smoke_steam_on.png", true, true), // smoke / pantograph
        new RosterFunctionEntry(9, Bundle.getMessage("F9NEM_AIRPUMP"),    // air pump
                "resources/icons/functionicons/transparent_background/air_release_off.png","resources/icons/functionicons/transparent_background/air_release.png", true, true)
    };
    static final RosterFunctionEntry[] NEM_DIESEL_FUNCTION_ENTRIES = {
        NEM_FUNCTION_ENTRIES[0], // Head lights
        NEM_FUNCTION_ENTRIES[1], // Back lights
        NEM_FUNCTION_ENTRIES[2], // Whistle                
        NEM_FUNCTION_ENTRIES[3], // Sound
        NEM_FUNCTION_ENTRIES[4], // Coupler
        NEM_FUNCTION_ENTRIES[5], // Shunting
        new RosterFunctionEntry(6, Bundle.getMessage("F6NEM"),    // Cab lights
                "resources/icons/functionicons/svg/cab_light_diesel_off_europe.svg","resources/icons/functionicons/svg/cab_light_diesel_on_europe.svg", true, true),
        new RosterFunctionEntry(7, Bundle.getMessage("F7NEM_ENGROOMLIGHT"),    // engine room light
                "resources/icons/functionicons/svg/motor_room_light_off.svg","resources/icons/functionicons/svg/motor_room_light_on.svg", true, true), 
        new RosterFunctionEntry(8, Bundle.getMessage("F8NEM_SMOKE"),    // smoke
                "resources/icons/functionicons/svg/diesel_smoke_nosmoke.svg","resources/icons/functionicons/svg/diesel_smoke.svg", true, true),
        new RosterFunctionEntry(9, Bundle.getMessage("F9NEM_COMPRESSOR"),    // compressor
                "resources/icons/functionicons/transparent_background/air_compress_off.png","resources/icons/functionicons/transparent_background/air_compress.png", true, true)
    };
    static final RosterFunctionEntry[] NEM_ELECTRIC_FUNCTION_ENTRIES = {
        NEM_FUNCTION_ENTRIES[0], // Head lights
        NEM_FUNCTION_ENTRIES[1], // Back lights
        NEM_FUNCTION_ENTRIES[2], // Whistle                
        NEM_FUNCTION_ENTRIES[3], // Sound
        NEM_FUNCTION_ENTRIES[4], // Coupler
        NEM_FUNCTION_ENTRIES[5], // Shunting
        new RosterFunctionEntry(6, Bundle.getMessage("F6NEM"),    // Cab lights
                "resources/icons/functionicons/svg/cab_light_diesel_off_europe.svg","resources/icons/functionicons/svg/cab_light_diesel_on_europe.svg", true, true),
        new RosterFunctionEntry(7, Bundle.getMessage("F7NEM_ENGROOMLIGHT"),    // engine room light
                "resources/icons/functionicons/svg/motor_room_light_off.svg","resources/icons/functionicons/svg/motor_room_light_on.svg", true, true),
        new RosterFunctionEntry(8, Bundle.getMessage("F8NEM_PANTOGRAPH"),    // pantograh
                "resources/icons/functionicons/transparent_background/panto_down.png","resources/icons/functionicons/transparent_background/panto_up.png", true, true),
        new RosterFunctionEntry(9, Bundle.getMessage("F9NEM_COMPRESSOR"),    // compressor
                "resources/icons/functionicons/transparent_background/air_compress_off.png","resources/icons/functionicons/transparent_background/air_compress.png", true, true)
    };       
}



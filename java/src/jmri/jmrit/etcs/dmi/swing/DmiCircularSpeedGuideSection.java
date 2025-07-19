package jmri.jmrit.etcs.dmi.swing;

import java.awt.Color;

import org.apiguardian.api.API;

/**
 * Class to represent a section of the DMI Circular Speed Guide.
 * @author Steve Young Copyright (C) 2024
 */
@API(status=API.Status.EXPERIMENTAL)
public class DmiCircularSpeedGuideSection {

    final float start;
    final float stop;
    final int type;
    final Color col;
    final boolean includeHook;
    final boolean includeNegative;

    public static final int CSG_TYPE_NORMAL = 0;
    public static final int CSG_TYPE_HOOK = 1; // width of hook
    public static final int CSG_TYPE_SUPERVISION = 2; // inner edge, yellow with hook
    public static final int CSG_TYPE_RELEASE = 3; // outer edge, grey,

    /**
     * Create a new section of the Circular Speed Guide.
     * @param csgType Type constant, e.g. CSG_TYPE_NORMAL or CSG_TYPE_HOOK
     * @param colour the Colour of the section.
     * @param startSpeed the section Start speed.
     * @param stopSpeed the section End speed.
     * @param hook true to include a hook, else false.
     */
    public DmiCircularSpeedGuideSection(int csgType, Color colour, float startSpeed, float stopSpeed, boolean hook) {
        this( csgType, colour, startSpeed, stopSpeed, hook, false);
    }

    /**
     * Create a new section of the Circular Speed Guide.
     * @param csgType Type constant, e.g. CSG_TYPE_NORMAL or CSG_TYPE_HOOK
     * @param colour the Colour of the section.
     * @param startSpeed the section Start speed.
     * @param stopSpeed the section End speed.
     * @param hook true to include a hook, else false.
     * @param includeNegative true to include the negative section.
     */
    public DmiCircularSpeedGuideSection(int csgType, Color colour, float startSpeed,
        float stopSpeed, boolean hook, boolean includeNegative ) {
        start = startSpeed;
        stop = stopSpeed;
        type = csgType;
        col = colour;
        includeHook = (hook || csgType == CSG_TYPE_HOOK);
        this.includeNegative = includeNegative;
    }

}

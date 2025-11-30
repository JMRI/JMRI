package jmri.jmrit.display.layoutEditor;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * A LayoutXOver corresponds to a crossover (connection between parallel tracks) on the layout. 
 * <p>
 * Three types are supported: double crossover,
 * right-handed single crossover, and left-handed single crossover. 
 * <p>
 * A LayoutXOver has four connection points, designated A, B, C, and
 * D. The A-B and C-D routes are a straight segment (continuing
 * route). A-C and B-D are the diverging routes. B-C and A-D illegal conditions.
 * <br>
 * <pre>
 *           Crossovers
 * Right-hand            left-hand
 * A ==**===== B      A ====**== B
 *      \\                 //
 *       \\               //
 *  D ====**== C     D ==**===== C
 *
 *             Double
 *        A ==**==**== B
 *             \\//
 *              XX
 *             //\\
 *        D ==**==**== C
 * </pre>
 * <p>
 * A LayoutXOver carries Block information. A block border may
 * occur at any connection (A,B,C,D). For a double crossover turnout, up to four
 * blocks may be assigned, one for each connection point, but if only one block
 * is assigned, that block applies to the entire turnout.
 * <p>
 * For drawing purposes, each LayoutXOver carries a center point and
 * displacements for B and C. 
 * For double crossovers, the center point is at the center of the turnout, and
 * the displacement for A = - the displacement for C and the displacement for D
 * = - the displacement for B. The center point and these displacements may be
 * adjusted by the user when in edit mode. For double crossovers, AB and BC are
 * constrained to remain perpendicular. For single crossovers, AB and CD are
 * constrained to remain parallel, and AC and BD are constrained to remain
 * parallel.
 * <p>
 * When LayoutXOvers are first created, a rotation (degrees) is provided. For
 * 0.0 rotation, the turnout lies on the east-west line with A facing east.
 * Rotations are performed in a clockwise direction.
 * <p>
 * When LayoutXOvers are first created, there are no connections. Block
 * information and connections may be added when available.
 * <p>
 * When a LayoutXOvers is first created, it is enabled for control of an
 * assigned actual turnout. Clicking on the turnout center point will toggle the
 * turnout. This can be disabled via the popup menu.
 * <p>
 * Signal Head names are saved here to keep track of where signals are.
 * LayoutTurnout only serves as a storage place for signal head names. The names
 * are placed here by tools, e.g., Set Signals at Turnout, and Set Signals at
 * Double Crossover. Each connection point can have up to three SignalHeads and one SignalMast.
 *
 * @author Dave Duchamp Copyright (c) 2004-2007
 * @author George Warner Copyright (c) 2017-2019
 */
abstract public class LayoutXOver extends LayoutTurnout {

    public LayoutXOver(@Nonnull String id, TurnoutType t, 
            @Nonnull LayoutEditor layoutEditor, 
            int v) {
        super(id, t, layoutEditor, 1);
    }

    @Override
    @Nonnull
    public List<String> getAnchorPoints() {
        return Arrays.asList("A", "B", "C", "D");
    }

    @Override
    @Nonnull
    public List<String> getPathIdentifiers() {
        // All crossovers have AB and CD, plus their specific diagonal path(s)
        List<String> paths = new java.util.ArrayList<>();
        paths.add("AB");
        paths.add("CD");
        paths.addAll(getDiagonalPaths());
        return paths;
    }

    /**
     * Get the diagonal path(s) specific to this crossover type.
     * @return list of diagonal paths
     */
    @Nonnull
    protected abstract List<String> getDiagonalPaths();

    @Override
    @Nonnull
    public List<String> getAllConnectors() {
        return Arrays.asList("A", "B", "C", "D");
    }

    @Override
    protected String findPathForAnchor(@Nonnull String anchor) {
        // Crossovers have multiple possible paths from each anchor
        // Return the first available path for simplicity
        // A more sophisticated implementation would consider turnout state
        switch (anchor) {
            case "A":
                return "AB"; // Could also be "AC" depending on turnout state
            case "B":
                return "AB"; // Could also be "BD" depending on turnout state  
            case "C":
                return "CD"; // Could also be "AC" depending on turnout state
            case "D":
                return "CD"; // Could also be "BD" depending on turnout state
            default:
                return null;
        }
    }

    @Override
    protected Point2D getAnchorCoordinates(@Nonnull String anchor, @Nonnull LayoutEditor layoutEditor) {
        switch (anchor) {
            case "A":
                return layoutEditor.getCoords(getConnectA(), HitPointType.TURNOUT_A);
            case "B":
                return layoutEditor.getCoords(getConnectB(), HitPointType.TURNOUT_B);
            case "C":
                return layoutEditor.getCoords(getConnectC(), HitPointType.TURNOUT_C);
            case "D":
                return layoutEditor.getCoords(getConnectD(), HitPointType.TURNOUT_D);
            default:
                return null;
        }
    }

    @Override
    protected java.awt.geom.Point2D getOtherEndpoint(@Nonnull String pathId, @Nonnull String anchor, @Nonnull LayoutEditor layoutEditor) {
        switch (pathId) {
            case "AB":
                switch (anchor) {
                    case "A":
                        return layoutEditor.getCoords(getConnectB(), HitPointType.TURNOUT_B);
                    case "B":
                        return layoutEditor.getCoords(getConnectA(), HitPointType.TURNOUT_A);
                    default:
                        return null;
                }
            case "CD":
                switch (anchor) {
                    case "C":
                        return layoutEditor.getCoords(getConnectD(), HitPointType.TURNOUT_D);
                    case "D":
                        return layoutEditor.getCoords(getConnectC(), HitPointType.TURNOUT_C);
                    default:
                        return null;
                }
            case "AC":
                switch (anchor) {
                    case "A":
                        return layoutEditor.getCoords(getConnectC(), HitPointType.TURNOUT_C);
                    case "C":
                        return layoutEditor.getCoords(getConnectA(), HitPointType.TURNOUT_A);
                    default:
                        return null;
                }
            case "BD":
                switch (anchor) {
                    case "B":
                        return layoutEditor.getCoords(getConnectD(), HitPointType.TURNOUT_D);
                    case "D":
                        return layoutEditor.getCoords(getConnectB(), HitPointType.TURNOUT_B);
                    default:
                        return null;
                }
            default:
                return null;
        }
    }
    
    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutXOver.class);
}

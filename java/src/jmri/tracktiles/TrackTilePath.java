package jmri.tracktiles;

/**
 * Represents a single path through a track tile.
 * <p>
 * A path connects two points on a track tile and has geometric properties like
 * length for straight paths or radius and arc for curved paths.
 *
 * @author Ralf Lang Copyright (C) 2025
 */
public class TrackTilePath {

    private final String direction; // "straight", "left", "right", "crossing"
    private final String state; // "closed", "thrown" for turnouts
    private final String route; // "ac", "bd", "ad", "bc" for crossovers/slips
    private final double length; // Length in mm (for straight paths)
    private final double radius; // Radius in mm (for curved paths)
    private final double arc; // Arc angle in degrees (for curved paths)
    private final String id; // Optional ID for multi-state turnouts

    /**
     * Create a straight path.
     *
     * @param direction The direction ("straight", "left", "right", etc.)
     * @param state     The turnout state ("closed", "thrown")
     * @param route     The route specification ("ac", "bd", etc.) or null
     * @param length    The length in millimeters
     * @param id        Optional ID for multi-state turnouts
     */
    public TrackTilePath(String direction, String state, String route,
            double length, String id) {
        this.direction = direction;
        this.state = state;
        this.route = route;
        this.length = length;
        this.radius = 0.0;
        this.arc = 0.0;
        this.id = id;
    }

    /**
     * Create a curved path.
     *
     * @param direction The direction ("straight", "left", "right", etc.)
     * @param state     The turnout state ("closed", "thrown")
     * @param route     The route specification ("ac", "bd", etc.) or null
     * @param radius    The radius in millimeters
     * @param arc       The arc angle in degrees
     * @param id        Optional ID for multi-state turnouts
     */
    public TrackTilePath(String direction, String state, String route,
            double radius, double arc, String id) {
        this.direction = direction;
        this.state = state;
        this.route = route;
        this.length = 0.0;
        this.radius = radius;
        this.arc = arc;
        this.id = id;
    }

    /**
     * Create a crossing path with length and angle (for crossings with explicit
     * length).
     *
     * @param direction The direction ("crossing", etc.)
     * @param state     The turnout state (may be null)
     * @param route     The route specification or null
     * @param length    The crossing path length in millimeters
     * @param radius    Must be 0.0 for crossing paths
     * @param angle     The crossing angle in degrees
     * @param id        Optional ID
     */
    public TrackTilePath(String direction, String state, String route,
            double length, double radius, double angle, String id) {
        this.direction = direction;
        this.state = state;
        this.route = route;
        this.length = length;
        this.radius = radius; // Should be 0.0 for crossings
        this.arc = angle;
        this.id = id;
    }

    /**
     * Create an angle-only path (for crossings).
     *
     * @param direction The direction ("crossing", etc.)
     * @param state     The turnout state (may be null)
     * @param route     The route specification or null
     * @param angle     The crossing angle in degrees
     * @param id        Optional ID
     */
    public static TrackTilePath createAngleOnlyPath(String direction, String state,
            String route, double angle, String id) {
        return new TrackTilePath(direction, state, route, 0.0, angle, id);
    }

    /**
     * Create a crossing path with both length and angle.
     *
     * @param direction The direction ("crossing", etc.)
     * @param state     The turnout state (may be null)
     * @param route     The route specification or null
     * @param length    The crossing path length in millimeters
     * @param angle     The crossing angle in degrees
     * @param id        Optional ID
     */
    public static TrackTilePath createCrossingPath(String direction, String state,
            String route, double length, double angle, String id) {
        return new TrackTilePath(direction, state, route, length, 0.0, angle, id);
    }

    public String getDirection() {
        return direction;
    }

    public String getState() {
        return state;
    }

    public String getRoute() {
        return route;
    }

    public double getLength() {
        return length;
    }

    public double getRadius() {
        return radius;
    }

    public double getArc() {
        return arc;
    }

    public String getId() {
        return id;
    }

    /**
     * Check if this is a straight path.
     *
     * @return true if this path has a length value
     */
    public boolean isStraight() {
        return length > 0.0;
    }

    /**
     * Check if this is a curved path.
     *
     * @return true if this path has radius and arc values
     */
    public boolean isCurved() {
        return radius > 0.0 && arc > 0.0;
    }

    /**
     * Check if this is an angle-only path (like crossing paths).
     *
     * @return true if this path has only an angle value (arc) without radius or
     *         length
     */
    public boolean isAngleOnly() {
        return arc > 0.0 && radius == 0.0 && length == 0.0;
    }

    /**
     * Check if this is a crossing path with explicit length.
     *
     * @return true if this path has both length and angle (crossing with
     *         explicit length)
     */
    public boolean isCrossingWithLength() {
        return length > 0.0 && arc > 0.0 && radius == 0.0;
    }

    /**
     * Calculate the actual path length. For straight paths, returns the length.
     * For curved paths, calculates arc length from radius and arc angle.
     *
     * @return the path length in millimeters
     */
    public double calculateLength() {
        if (isStraight()) {
            return length;
        } else if (isCurved()) {
            // Arc length = radius * arc_in_radians
            return radius * Math.toRadians(arc);
        }
        return 0.0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(direction);
        if (state != null) {
            sb.append(" (").append(state).append(")");
        }
        if (route != null) {
            sb.append(" [").append(route).append("]");
        }
        if (isStraight()) {
            sb.append(": ").append(String.format("%.1f mm", length));
        } else if (isCurved()) {
            sb.append(": R").append(String.format("%.1f", radius))
                    .append(" ∠").append(String.format("%.1f°", arc))
                    .append(" (").append(String.format("%.1f mm", calculateLength())).append(")");
        } else if (isCrossingWithLength()) {
            sb.append(": ").append(String.format("%.1f mm", length))
                    .append(" ∠").append(String.format("%.1f°", arc));
        } else if (isAngleOnly()) {
            sb.append(": ∠").append(String.format("%.1f°", arc));
        }
        return sb.toString();
    }
}

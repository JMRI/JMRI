package jmri.jmrit.display.layoutEditor;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import jmri.tracktiles.NotATile;
import jmri.tracktiles.TrackTile;
import jmri.tracktiles.TrackTilePath;
import jmri.tracktiles.UnknownTile;

/**
 * Utility class for calculating turnout path lengths based on track tile geometry.
 *
 * @author Ralf Lang Copyright (C) 2025
 */
public class TurnoutPathCalculator {

    /**
     * Calculate path lengths for a turnout based on its track tile information.
     *
     * @param layoutTurnout The turnout to calculate paths for
     * @return List of formatted path descriptions with lengths
     */
    @Nonnull
    public static List<String> calculatePathLengths(@Nonnull LayoutTurnout layoutTurnout) {
        List<String> pathLengths = new ArrayList<>();

        TrackTile tile = layoutTurnout.getTrackTile();
        
        if (tile instanceof NotATile) {
            pathLengths.add("No track tile assigned");
            return pathLengths;
        }
        
        if (tile instanceof UnknownTile) {
            pathLengths.add("Unknown track tile - no geometry available");
            return pathLengths;
        }
        
        if (!tile.hasPaths()) {
            pathLengths.add("No path geometry available");
            return pathLengths;
        }

        List<TrackTilePath> paths = tile.getPaths();
        
        for (TrackTilePath path : paths) {
            StringBuilder sb = new StringBuilder();
            
            // Determine and display path type information
            String pathType = determinePathType(path, tile.getJmriType());
            if (pathType != null && !pathType.isEmpty()) {
                sb.append("Path ").append(pathType).append(": ");
            } else if (path.getRoute() != null) {
                sb.append("Route ").append(path.getRoute().toUpperCase()).append(": ");
            } else {
                sb.append(path.getDirection()).append(" path");
                if (path.getState() != null) {
                    sb.append(" (").append(path.getState()).append(")");
                }
                sb.append(": ");
            }
            
            // Add length information
            double length = path.calculateLength();
            if (length > 0) {
                sb.append(String.format("%.1f mm", length));
                
                // Add additional details for curved paths
                if (path.isCurved()) {
                    sb.append(String.format(" (R%.1f, ∠%.1f°)", 
                        path.getRadius(), path.getArc()));
                }
            } else {
                sb.append("No geometry data");
            }
            
            pathLengths.add(sb.toString());
        }
        
        if (pathLengths.isEmpty()) {
            pathLengths.add("No valid path geometry found");
        }

        return pathLengths;
    }
    
    /**
     * Determine the path type (connection points) based on direction, state, and turnout type.
     * 
     * @param path The path to analyze
     * @param jmriType The JMRI type of the turnout
     * @return Path type string like "A-B", "A-C", etc., or null if not determinable
     */
    private static String determinePathType(TrackTilePath path, String jmriType) {
        // If route is specified in the path, use it directly
        if (path.getRoute() != null && !path.getRoute().isEmpty()) {
            String route = path.getRoute().toLowerCase();
            // Convert route format (like "ac", "bd") to connection format
            if (route.length() == 2) {
                return route.substring(0,1).toUpperCase() + "-" + route.substring(1).toUpperCase();
            }
            return path.getRoute().toUpperCase();
        }
        
        // For standard turnouts, map direction and state to connection points
        String direction = path.getDirection();
        String state = path.getState();
        
        switch (jmriType) {
            case "turnout-rh":
            case "turnout-lh":
                if ("straight".equals(direction)) {
                    return "A-B";  // Straight through path
                } else if ("right".equals(direction) || "left".equals(direction)) {
                    return "A-C";  // Diverging path
                }
                break;
                
            case "turnout-wye":
                if ("left".equals(direction)) {
                    return "A-B";  // Left branch
                } else if ("right".equals(direction)) {
                    return "A-C";  // Right branch
                }
                break;
                
            case "crossover-single-rh":
            case "crossover-single-lh":
                if ("straight".equals(direction)) {
                    // Could be either main line, need to distinguish
                    if ("closed".equals(state)) {
                        return "A-B";  // Main line
                    } else {
                        return "C-D";  // Siding
                    }
                } else if ("crossing".equals(direction)) {
                    return "A-D";  // Crossover path
                }
                break;
                
            case "crossover-double":
                if ("straight".equals(direction)) {
                    // For double crossover, use route if available, otherwise guess
                    return "A-C/B-D";  // Both straight paths
                } else if ("crossing".equals(direction)) {
                    return "A-D/B-C";  // Both crossing paths
                }
                break;
                
            case "slip-single":
                if ("straight".equals(direction)) {
                    return "A-D";  // Main line through slip
                } else if ("slip".equals(direction)) {
                    return "A-B";  // Slip path
                }
                break;
                
            case "slip-double":
                if ("straight".equals(direction)) {
                    return "A-D/B-C";  // Both straight paths
                } else if ("slip".equals(direction)) {
                    return "A-B/C-D";  // Both slip paths
                }
                break;
                
            default:
                // For unknown types, try to use state information
                if ("closed".equals(state)) {
                    return "A-B";
                } else if ("thrown".equals(state)) {
                    return "A-C";
                }
                break;
        }
        
        // If we can't determine from type, return null to use original formatting
        return null;
    }
}
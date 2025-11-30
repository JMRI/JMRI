package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.layoutEditor.LayoutTrack;
import jmri.util.swing.JmriJOptionPane;

/**
 * Reusable widget for displaying track geometry information (orientations and path lengths)
 * for any LayoutTrack implementation.
 * 
 * @author AI Assistant
 */
public class LayoutTrackGeometryWidget extends JPanel {

    private final LayoutTrack layoutTrack;
    private final LayoutEditor layoutEditor;
    private final Map<String, JLabel> anchorLabels = new HashMap<>();
    private final Map<String, JTextField> orientationFields = new HashMap<>();
    private final Map<String, JLabel> pathLabels = new HashMap<>(); 
    private final Map<String, JTextField> pathLengthFields = new HashMap<>();

    /**
     * Create a geometry widget for the specified track.
     * 
     * @param layoutTrack the track to display geometry for
     * @param layoutEditor the layout editor for coordinate access
     */
    public LayoutTrackGeometryWidget(@Nonnull LayoutTrack layoutTrack, @Nonnull LayoutEditor layoutEditor) {
        this.layoutTrack = layoutTrack;
        this.layoutEditor = layoutEditor;
        initializeWidget();
        updateValues();
    }

    /**
     * Initialize the widget layout and components.
     */
    private void initializeWidget() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5);

        // Create orientation display section
        List<String> anchors = layoutTrack.getAnchorPoints();
        for (int i = 0; i < anchors.size(); i++) {
            String anchor = anchors.get(i);
            
            // Anchor orientation label and field
            JLabel orientationLabel = new JLabel(anchor + " Orientation:");
            JTextField orientationField = new JTextField(12);
            orientationField.setEditable(false);
            
            anchorLabels.put(anchor, orientationLabel);
            orientationFields.put(anchor, orientationField);
            
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.anchor = GridBagConstraints.WEST;
            add(orientationLabel, gbc);
            
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            add(orientationField, gbc);
            gbc.weightx = 0.0;
            gbc.fill = GridBagConstraints.NONE;
        }

        // Create path length display section
        List<String> paths = layoutTrack.getPathIdentifiers();
        for (int i = 0; i < paths.size(); i++) {
            String pathId = paths.get(i);
            
            // Path length label and field
            JLabel pathLabel = new JLabel("Path " + pathId + " Length:");
            JTextField pathLengthField = new JTextField(12);
            pathLengthField.setEditable(false);
            
            pathLabels.put(pathId, pathLabel);
            pathLengthFields.put(pathId, pathLengthField);
            
            int row = anchors.size() + i;
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.anchor = GridBagConstraints.WEST;
            add(pathLabel, gbc);
            
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            add(pathLengthField, gbc);
            gbc.weightx = 0.0;
            gbc.fill = GridBagConstraints.NONE;
        }
    }

    /**
     * Update all displayed values from the track model.
     */
    public void updateValues() {
        updateOrientations();
        updatePathLengths();
    }

    /**
     * Update orientation values for all anchor points.
     */
    private void updateOrientations() {
        for (String anchor : layoutTrack.getAnchorPoints()) {
            try {
                double orientation = layoutTrack.getOrientationAtAnchor(anchor, layoutEditor);
                JTextField field = orientationFields.get(anchor);
                JLabel label = anchorLabels.get(anchor);
                
                if (orientation >= 0) {
                    field.setText(String.format("%.2f°", orientation));
                    
                    // Update label with coordinates if available
                    java.awt.geom.Point2D point = getAnchorCoordinates(anchor);
                    if (point != null) {
                        label.setText(String.format("%s (%.1f, %.1f) Orientation:", 
                            anchor, point.getX(), point.getY()));
                    }
                } else {
                    field.setText("-");
                    label.setText(anchor + " Orientation:");
                }
            } catch (Exception e) {
                orientationFields.get(anchor).setText("Error");
            }
        }
    }

    /**
     * Update path length values for all paths.
     */
    private void updatePathLengths() {
        for (String pathId : layoutTrack.getPathIdentifiers()) {
            try {
                double pathLength = layoutTrack.getPathLength(pathId);
                JTextField field = pathLengthFields.get(pathId);
                
                if (pathLength > 0.0) {
                    field.setText(String.format("%.2f mm", pathLength));
                } else {
                    field.setText("-");
                }
            } catch (Exception e) {
                pathLengthFields.get(pathId).setText("Error");
            }
        }
    }

    /**
     * Get coordinates for an anchor point.
     * This is a helper method that could be moved to LayoutTrack base class.
     */
    private java.awt.geom.Point2D getAnchorCoordinates(String anchor) {
        try {
            // Use reflection to call the protected method
            java.lang.reflect.Method method = LayoutTrack.class.getDeclaredMethod(
                "getAnchorCoordinates", String.class, LayoutEditor.class);
            method.setAccessible(true);
            return (java.awt.geom.Point2D) method.invoke(layoutTrack, anchor, layoutEditor);
        } catch (Exception e) {
            return null; // Coordinates not available
        }
    }

    /**
     * Get the number of anchor orientation fields.
     */
    public int getAnchorCount() {
        return layoutTrack.getAnchorPoints().size();
    }

    /**
     * Get the number of path length fields.
     */
    public int getPathCount() {
        return layoutTrack.getPathIdentifiers().size();
    }
}
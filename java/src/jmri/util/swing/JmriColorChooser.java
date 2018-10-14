package jmri.util.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.colorchooser.AbstractColorChooserPanel;

/**
 * Display the Java color chooser that includes a custom JMRI panel.
 * The custom panel is provided by {@link JmriColorChooserPanel}.
 * <p>
 * Maintain a list of recently used colors.  This includes colors found
 * during loading and subsequent changes.
 * @author Dave Sand Copyright (C) 2018
 * @since 4.13.1
 */
public class JmriColorChooser {

    /**
     * The colors used by the current JMRI session.
     * The XML loading process creates the initial list and
     * subsequent activity will add new colors.
     */
    static private ArrayList<Color> recentColors = new ArrayList<>();

    /**
     * Add a new color to the recent list.
     * Null values and duplicates are not added.
     * @param color The color object to be added.
     */
    static public void addRecentColor(Color color) {
        if (color == null || !color.toString().contains("java.awt.Color")) {
            // Ignore null or default system colors
            return;
        }
        if (!recentColors.contains(color)) {
            recentColors.add(color);
        }
    }

    /**
     * Provide a copy of the recent color list.
     * This is used by {@link JmriColorChooserPanel} to build
     * the recent color list.
     * @return the recent color list.
     */
    static public ArrayList<Color> getRecentColors() {
        return new ArrayList<>(recentColors);
    }

    /**
     * The number of tabs in the color chooser, 5 standard plus the custom JMRI tab
     */
    static Color color;
    
    /**
     * Display the customized color selection dialog.
     * The JMRI custom panel is added before the Java supplied panels so that
     * it will be the default panel.
     * @param comp The calling component.  It is not used but provided to simplify migration.
     * @param dialogTitle The title at the top of the dialog.
     * @param currentColor The color that will be set as the starting value for the dialog.
     * @return the selected color for a OK response, the orignal color for a Cancel response.
     */
    static public Color showDialog(Component comp, String dialogTitle, Color currentColor) {
        color = currentColor == null ? Color.WHITE : currentColor;
        String title = dialogTitle == null ? "" : dialogTitle;
        JColorChooser chooser = extendColorChooser(new JColorChooser(color));
        JDialog d = JColorChooser.createDialog(null, title, true, chooser,
            ((ActionEvent e) -> {
                color = chooser.getColor();
            }),
            null);
        d.setVisible(true);
        return color;
    }

    /**
     * Add or replace the JMRI custom panel at the beginning of the chooser tabs
     * @param chooser The chooser object to be updated.
     * @return the updated chooser object
     */
     static public JColorChooser extendColorChooser(JColorChooser chooser) {

        int colorTabCount ;

        AbstractColorChooserPanel[] currPanels = chooser.getChooserPanels();
        colorTabCount = currPanels.length + 1 ;
        AbstractColorChooserPanel[] newPanels = new AbstractColorChooserPanel[colorTabCount];
        newPanels[0] = new jmri.util.swing.JmriColorChooserPanel();
        int idx = 1;
        for (int i = 0; i < currPanels.length; i++) {
            if (!currPanels[i].getDisplayName().equals("JMRI")) {  // NOI18N
                // Copy non JMRI panels
                newPanels[idx] = currPanels[i];
                idx++;
            }
        }
        chooser.setChooserPanels(newPanels);
        return chooser;
    }
}
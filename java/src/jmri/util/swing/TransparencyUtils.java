package jmri.util.swing;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * Some utilities to turn a recursively component opacity and background transparent
 * 
 * @author Lionel Jeanson 2025
 */
public class TransparencyUtils {

    /**
     * Recursively set opacity of a component and its sub components
     *
     * @param jcomp The component on which to set opacity
     *
     */  
    public static void setOpacityRec(JComponent jcomp) {
        setOpacityRec(jcomp, true);
    }

    public static void setOpacityRec(JComponent jcomp, boolean transparency) {
        if (jcomp instanceof JPanel || jcomp instanceof JTabbedPane) {
            jcomp.setOpaque(!transparency);
        }
        setOpacityRec(jcomp.getComponents(), transparency);
    }

    private static void setOpacityRec(Component[] comps, boolean transparency) {
        for (Component comp : comps) {
            try {
                if (comp instanceof JComponent) {
                    setOpacityRec((JComponent) comp, transparency);
                }
            } catch (Exception e) {
                // Do nothing, just go on
            }
        }
    }

    /**
     * Recursively set a component and its sub components backgroud transparent
     *
     * @param jcomp The component on which to set a transparent background
     *
     */    
    public static void setTransparentBackgroundRec(JComponent jcomp) {
        if (jcomp instanceof JPanel || jcomp instanceof JTabbedPane)
        {
            jcomp.setBackground(new Color(0, 0, 0, 0));
        }
        setTransparentBackgroundRec(jcomp.getComponents());
    }

    /**
     * Recursively set several components backgroud transparent.
     *
     * @param comps The components array to set.
     *
     */
    private static void setTransparentBackgroundRec(Component[] comps) {
        for (Component comp : comps) {
            try {
                if (comp instanceof JComponent) {
                    setTransparentBackgroundRec((JComponent) comp);
                }
            } catch (Exception e) {
                // Do nothing, just go on
            }
        }
    }    
}

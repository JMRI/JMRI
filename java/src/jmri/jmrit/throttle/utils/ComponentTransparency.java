package jmri.jmrit.throttle.utils;

import java.awt.Component;
import java.awt.Color;
import javax.swing.JComponent;
import javax.swing.JPanel;

public class ComponentTransparency {
    // some utilities to turn a component background transparent

    /**
     * Set the transparency of a component.
     *
     * @param jcomp The component to set.
     *
     */
    public static void setTransparentBackground(JComponent jcomp) {
        if (jcomp instanceof JPanel) //OS X: Jpanel components are enough
        {
            jcomp.setBackground(new Color(0, 0, 0, 0));
        }
        setTransparentBackground(jcomp.getComponents());
    }

    /**
     * Set the transparency of sveral components.
     *
     * @param comps The components array to set.
     *
     */
    public static void setTransparentBackground(Component[] comps) {
        for (Component comp : comps) {
            try {
                if (comp instanceof JComponent) {
                    setTransparentBackground((JComponent) comp);
                }
            } catch (Exception e) {
                // Do nothing, just go on
            }
        }
    }

}

package jmri.util.swing;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Some utilities to turn a recursively component background transparent
 * 
 * @author Lionel Jeanson 2025
 */
public class TransparencyUtils {

    public static void setTransparent(JComponent jcomp) {
        setTransparent(jcomp, true);
    }

    public static void setTransparent(JComponent jcomp, boolean transparency) {
        if (jcomp instanceof JPanel) { //OS X: Jpanel components are enough
            jcomp.setOpaque(!transparency);
        }
        setTransparent(jcomp.getComponents(), transparency);
    }

    private static void setTransparent(Component[] comps, boolean transparency) {
        for (Component comp : comps) {
            try {
                if (comp instanceof JComponent) {
                    setTransparent((JComponent) comp, transparency);
                }
            } catch (Exception e) {
                // Do nothing, just go on
            }
        }
    }
    
}

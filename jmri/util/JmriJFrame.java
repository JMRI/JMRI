// JmriJFrame.java

package jmri.util;

import javax.swing.JFrame;
import java.awt.*;

/**
 * JFrame extended for common JMRI use.
 * <P>
 * We needed a place to refactor common JFrame additions in JMRI
 * code, so this class was created.
 * <P>
 * In particular, this is intended to provide Java 2 functionality on a
 * Java 1.1.8 system, or at least try to fake it.
 * <P>
 * Features:
 * <ul>
 * <LI>Size limited to the maximum available on the screen, after
 * removing any menu bars (Mac) and taskbars (Windows)
 * </ul>
 *
 *
 * @author Bob Jacobsen  Copyright 2003
 * @version $Revision: 1.3 $
 */

public class JmriJFrame extends JFrame {

    public JmriJFrame(String name) {
        super(name);
    }

    public Dimension getMaximumSize() {
        // adjust maximum size to full screen minus any toolbars
        try {
            Insets insets = getToolkit().getScreenInsets(this.getGraphicsConfiguration());
            Dimension screen = getToolkit().getScreenSize();
            return new Dimension(screen.width-(insets.right+insets.left),
                screen.height-(insets.top+insets.bottom));
        } catch (NoSuchMethodError e) {
            Dimension screen = getToolkit().getScreenSize();
            return new Dimension(screen.width,
                screen.height-45);  // approximate this...
        }
    }

    public Dimension getPreferredSize() {
        Dimension screen = getMaximumSize();
        int width = Math.min(super.getPreferredSize().width, screen.width);
        int height = Math.min(super.getPreferredSize().height, screen.height);
        return new Dimension(width, height);
    }

}
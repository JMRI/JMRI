package apps;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Rectangle;
import java.awt.Toolkit;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import jmri.util.FileUtil;

/**
 * A splash screen for showing during JMRI startup
 *
 * @author Bob Jacobsen Copyright 2003
 * @author Dennis Miller Copyright 2007
 */
public class SplashWindow extends JFrame {

    Image splashIm;

    public SplashWindow() {
        super("JMRI");
        splashWindowDisplay(null);
    }

    public SplashWindow(JPanel splashMsg) {
        super("JMRI");
        splashWindowDisplay(splashMsg);
    }

    public void splashWindowDisplay(JPanel splashMsg) {
        //super("JMRI");
        this.setUndecorated(true);

        // get the splash image
        MediaTracker mt = new MediaTracker(this);
        splashIm = Toolkit.getDefaultToolkit().getImage(FileUtil.findURL("resources/logo.gif", FileUtil.Location.INSTALLED));
        mt.addImage(splashIm, 0);
        try {
            mt.waitForID(0);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt(); // retain if needed later
        }

        JLabel l = new JLabel(new ImageIcon(splashIm, "JMRI splash screen"));
        l.setOpaque(true);

        if (splashMsg != null) {
            JPanel full = new JPanel();
            full.setLayout(
                    new BoxLayout(full, BoxLayout.Y_AXIS));
            l.setAlignmentX(CENTER_ALIGNMENT);
            splashMsg.setAlignmentX(CENTER_ALIGNMENT);
            full.add(l);
            full.add(splashMsg);
            getContentPane().add(full);
        } else {
            getContentPane().add(l);
        }

        pack();

        /* Center the window and pad the frame size slightly to put some space 
         * between logo and frame border*/
        Dimension screenDim
                = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle winDim = getBounds();
        winDim.height = winDim.height + 10;
        winDim.width = winDim.width + 10;
        setLocation((screenDim.width - winDim.width) / 2,
                (screenDim.height - winDim.height) / 2);
        setSize(winDim.width, winDim.height);

        // and show
        setVisible(true);
    }
}

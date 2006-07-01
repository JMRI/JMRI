// SplashWindow.java

package apps;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * A splash screen for showing during JMRI startup
 * @author	Bob Jacobsen   Copyright 2003
 * @version     $Revision: 1.1 $
 */
public class SplashWindow extends JFrame {
    Image splashIm;

    public SplashWindow() {
        super("JMRI");

        // get the splash image
       MediaTracker mt = new MediaTracker(this);
       splashIm = Toolkit.getDefaultToolkit(
           ).getImage("resources"+File.separator+"logo.gif");
       mt.addImage(splashIm,0);
       try {
          mt.waitForID(0);
       } catch(InterruptedException ie){}

        getContentPane().add(new JLabel(new ImageIcon(splashIm, "JMRI splash screen")));
        pack();

        /* Center the window */
        Dimension screenDim =
             Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle winDim = getBounds();
        setLocation((screenDim.width - winDim.width) / 2,
                (screenDim.height - winDim.height) / 2);

        setVisible(true);
    }

    public void paint(Graphics g) {
       if (splashIm != null) {
           g.drawImage(splashIm,0,0,this);
       }
    }
}



// DecoderPro.java

package apps.DecoderPro;

import java.awt.*;
import java.io.*;

import javax.swing.*;

/**
 * DecoderPro application.
 *
 * @author                      Bob Jacobsen
 * @version                     $Revision: 1.34 $
 */
public class DecoderPro {

    // Main entry point
    public static void main(String s[]) {
        // show splash screen early
        DecoderPro dp = new DecoderPro();
     }

    DecoderPro() {
        super();

        // show splash screen early
        SplashWindow sp = new SplashWindow();

        // start main program, using reflection to
        // reduce startup delay
        try {
            Class c = Class.forName("apps.DecoderPro.DecoderProMain");
            c.getMethod("main", new Class[]{String[].class}).invoke(null, new Object[]{null});
        } catch (Exception e) {
            System.err.println("Exception while trying to start up: "+e);
        }

        // pull splash screen
        sp.setVisible(false);
    }

class SplashWindow extends JFrame {
    Image splashIm;

    SplashWindow() {
        super("JMRI starting");

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

}


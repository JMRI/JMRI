// DecoderPro.java

package apps.DecoderPro;

import java.awt.*;
import java.awt.event.*;

/**
 * DecoderPro application.
 *
 * @author                      Bob Jacobsen
 * @version                     $Revision: 1.32 $
 */
public class DecoderPro extends Frame {

    // Main entry point
    public static void main(String s[]) {
        // show splash screen early
        DecoderPro dp = new DecoderPro();
     }

    DecoderPro() {
        super();
        // get the splash image
       MediaTracker mt = new MediaTracker(this);
       Image splashIm = Toolkit.getDefaultToolkit(
           ).getImage("resources/logo.gif");
       mt.addImage(splashIm,0);
       try {
          mt.waitForID(0);
       } catch(InterruptedException ie){}

        // show splash screen early
        SplashWindow sp = new SplashWindow(this, splashIm);

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

class SplashWindow extends Window {
    Image splashIm;

    SplashWindow(Frame parent, Image splashIm) {
        super(parent);
        this.splashIm = splashIm;
        setSize(new Dimension(splashIm.getHeight(null),splashIm.getWidth(null)));

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


package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import java.awt.event.WindowListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import jmri.jmrix.rps.Measurement;
import jmri.jmrix.rps.Reading;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.JComponentOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Tests for the RpsIcon class.
 *
 * @author	Bob Jacobsen Copyright 2008
 */
public class RpsPositionIconTest extends PositionableTestBase {

    private Editor panel = null;
    private RpsPositionIcon rpsIcon = null;

    @Test
    public void testShow() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JmriJFrame jf = new JmriJFrame("RpsPositionIcon Test");
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        jf.getContentPane().add(rpsIcon);

        // test buttons
        JButton originButton = new JButton("Set 0,0");
        originButton.addActionListener((java.awt.event.ActionEvent e) -> {
            measButtonPushed(0., 0., rpsIcon);
        });
        jf.getContentPane().add(originButton);

        JButton tentenButton = new JButton("Set 10,10");
        tentenButton.addActionListener((java.awt.event.ActionEvent e) -> {
            measButtonPushed(10., 10., rpsIcon);
        });
        jf.getContentPane().add(tentenButton);

        JButton fivetenButton = new JButton("Set 5,10");
        fivetenButton.addActionListener((java.awt.event.ActionEvent e) -> {
            measButtonPushed(5., 10., rpsIcon);
        });
        jf.getContentPane().add(fivetenButton);

        JButton loco21Button = new JButton("Loco 21");
        loco21Button.addActionListener((java.awt.event.ActionEvent e) -> {
            locoButtonPushed("21");
        });
        jf.getContentPane().add(loco21Button);

        JButton loco33Button = new JButton("Loco 33");
        loco33Button.addActionListener((java.awt.event.ActionEvent e) -> {
            locoButtonPushed("33");
        });
        jf.getContentPane().add(loco33Button);

        jf.pack();
        jf.setSize(300, 300);
        jf.setVisible(true);

        JFrame f = jmri.util.JmriJFrame.getFrame("RpsPositionIcon Test");
        Assert.assertTrue("found frame", f != null);
        JUnitUtil.dispose(f);
    }

    String id = "20";

    // animate the visible frame
    public void measButtonPushed(double x, double y, RpsPositionIcon rpsIcon) {
        Reading loco = new Reading(id, null);
        Measurement m = new Measurement(loco, x, y, 0.0, 0.133, 0, "source");
        rpsIcon.notify(m);
    }

    public void locoButtonPushed(String newID) {
        id = newID;
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
        if (!GraphicsEnvironment.isHeadless()) {
            panel = new jmri.jmrit.display.panelEditor.PanelEditor("Test RpsPositionIcon Panel");
            p = rpsIcon = new RpsPositionIcon(panel);
        }
    }

    @After
    public void tearDown() {
        // now close panel window
        if (panel != null) {
            java.awt.event.WindowListener[] listeners = panel.getTargetFrame().getWindowListeners();
            for (WindowListener listener : listeners) {
                panel.getTargetFrame().removeWindowListener(listener);
            }

            // close the panel target frame.
            EditorFrameOperator to = new EditorFrameOperator(panel.getTargetFrame());
            // this panel isn't behaving like others that create a 
            // panelEditor. It does not create dialogs when it closes, so call 
            // requestClose without handling the dialogs as in 
            // to.closeFrameWithConfirmations()
            to.requestClose();
        }
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(RpsPositionIconTest.class);
}

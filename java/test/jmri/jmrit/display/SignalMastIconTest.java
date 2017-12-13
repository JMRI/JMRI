package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import java.awt.event.WindowListener;
import javax.swing.JFrame;
import javax.swing.JLabel;
import jmri.InstanceManager;
import jmri.SignalMast;
import jmri.implementation.DefaultSignalHead;
import jmri.jmrit.display.panelEditor.PanelEditor;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the SignalMastIcon.
 * <p>
 * Description:
 *
 * @author	Bob Jacobsen Copyright 2009
 */
public class SignalMastIconTest extends PositionableIconTest {

    PanelEditor panel = null;
    SignalMast s = null;
    SignalMastIcon to = null;

    @Test
    public void testShowText() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // this one is for Layout editor, which for now
        // is still in text form.
        JFrame jf = new JFrame("SignalMast Icon Text Test");
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        jf.getContentPane().add(new JLabel("Should say Approach: "));
        jf.getContentPane().add(to);

        s.setAspect("Clear");
        s.setAspect("Approach");

        jf.pack();
        jf.setVisible(true);

        // close
        JUnitUtil.dispose(jf);

    }

    @Test
    public void testShowIcon() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JFrame jf = new JFrame("SignalMastIcon Icon Test");
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        SignalMastIcon to = new SignalMastIcon(panel);
        to.setShowAutoText(false);

        jf.getContentPane().add(new JLabel("Should be yellow/red: "));
        jf.getContentPane().add(to);

        s = InstanceManager.getDefault(jmri.SignalMastManager.class)
                .provideSignalMast("IF$shsm:basic:two-searchlight:IH1:IH2");

        s.setAspect("Clear");

        to.setSignalMast(s.getSystemName());

        s.setAspect("Clear");
        s.setAspect("Approach");

        jf.pack();
        jf.setVisible(true);

        // close
        JUnitUtil.dispose(jf);
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            panel = new PanelEditor("Test SignalMastIcon Panel");
            p = new SignalMastIcon(panel);
            to = new SignalMastIcon(panel);
            to.setShowAutoText(true);

            // reset instance manager & create test heads
            InstanceManager.getDefault(jmri.SignalHeadManager.class).register(
                 new DefaultSignalHead("IH1") {
                    @Override
                    protected void updateOutput() {
                    }
                 }
                 );
            InstanceManager.getDefault(jmri.SignalHeadManager.class).register(
                 new DefaultSignalHead("IH2") {
                    @Override
                    protected void updateOutput() {
                 }
            }
            );
            InstanceManager.getDefault(jmri.SignalHeadManager.class).register(
                 new DefaultSignalHead("IH3") {
                    @Override
                    protected void updateOutput() {
                 }
            }
            );

            s = InstanceManager.getDefault(jmri.SignalMastManager.class)
                 .provideSignalMast("IF$shsm:basic:one-searchlight:IH1");

            to.setSignalMast(new jmri.NamedBeanHandle<>(s.getSystemName(), s));
        }
    }

    @After
    @Override
    public void tearDown() {
        // now close panel window
        if (panel != null) {
            WindowListener[] listeners = panel.getTargetFrame().getWindowListeners();
            for (WindowListener listener : listeners) {
                panel.getTargetFrame().removeWindowListener(listener);
            }
            panel.getTargetFrame().dispose();
            JUnitUtil.dispose(panel);
        }
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SignalMastIconTest.class);
}

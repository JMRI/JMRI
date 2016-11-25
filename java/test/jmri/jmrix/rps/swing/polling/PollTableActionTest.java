package jmri.jmrix.rps.swing.polling;

import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Tests for the jmri.jmrix.rps.swing.polling package.
 *
 * @author Bob Jacobsen Copyright 2008
 */
public class PollTableActionTest {

    // Show the window
    @Test
    public void testDisplay() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        new PollTableAction().actionPerformed(null);
        // confirm window was created
        JFrame f = JFrameOperator.waitJFrame("RPS Polling Control", true, true);
        Assert.assertNotNull("found frame", f);
        f.dispose();
    }

}

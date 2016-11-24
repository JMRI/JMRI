package jmri.jmrix.rps;

import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 * JUnit tests for the rps.Reading class.
 *
 * @author	Bob Jacobsen Copyright 2006
  */
public class RpsPositionIconTest {

    @Test
    public void testCtorAndID() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // init test system
        new Engine() {
            void reset() {
                _instance = null;
            }
        }.reset();

        new jmri.configurexml.ConfigXmlManager().load(new java.io.File("java/test/jmri/jmrix/rps/LocationTestPanel.xml"));

        // and push a good measurement
        Reading loco = new Reading("27", null);
        Measurement m = new Measurement(loco, 0.0, 0.0, 0.0, 0.133, 5, "source");
        Distributor.instance().submitMeasurement(m);

        JFrame f = jmri.util.JmriJFrame.getFrame("RPS Location Test");
        Assert.assertNotNull("found frame", f);
        f.dispose();
    }

}

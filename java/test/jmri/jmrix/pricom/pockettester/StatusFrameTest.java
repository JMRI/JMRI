package jmri.jmrix.pricom.pockettester;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * JUnit tests for the StatusFrame class
 *
 * @author	Bob Jacobsen Copyright 2005
 */
public class StatusFrameTest {

    @Test
    public void testCreate() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        StatusFrame statusFrame = new StatusFrame();
        Assert.assertNotNull(statusFrame);
    }

    // create and show, with some data present
    @Test
    public void testShow() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        StatusFrame f = new StatusFrame();
        f.initComponents();
        f.setVisible(true);
        f.setSource(new DataSource() {

            @Override
            void sendBytes(byte[] bytes) {
            }
        });
        f.asciiFormattedMessage(PackageTest.version);
        f.asciiFormattedMessage(PackageTest.speed0003A);
        f.asciiFormattedMessage(PackageTest.idlePacket);
        f.asciiFormattedMessage(PackageTest.status1);
        f.asciiFormattedMessage(PackageTest.status2);
        f.asciiFormattedMessage(PackageTest.status3);
        f.asciiFormattedMessage(PackageTest.status4);
        f.asciiFormattedMessage(PackageTest.status5);

        f.dispose();
    }

    // create and show, with zero address data
    @Test
    public void testZeroAddr() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        StatusFrame f = new StatusFrame();
        f.initComponents();
        f.setVisible(true);
        f.setSource(new DataSource() {
            @Override
            void sendBytes(byte[] bytes) {
            }
        });
        f.asciiFormattedMessage(PackageTest.version);
        f.asciiFormattedMessage(PackageTest.speed0003A);
        f.asciiFormattedMessage(PackageTest.idlePacket);
        f.asciiFormattedMessage(PackageTest.status6);
        f.asciiFormattedMessage(PackageTest.status2);

        f.dispose();
    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }
}

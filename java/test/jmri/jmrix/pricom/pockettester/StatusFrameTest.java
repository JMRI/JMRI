package jmri.jmrix.pricom.pockettester;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * JUnit tests for the StatusFrame class
 *
 * @author Bob Jacobsen Copyright 2005
 */
public class StatusFrameTest {

    @Test
    @DisabledIfHeadless
    public void testCreate() {

        StatusFrame statusFrame = new StatusFrame();
        Assertions.assertNotNull(statusFrame);
    }

    // create and show, with some data present
    @Test
    @DisabledIfHeadless
    public void testShow() {

        StatusFrame f = new StatusFrame();
        f.initComponents();
        f.setVisible(true);
        f.setSource(new DataSource() {

            @Override
            synchronized void sendBytes(byte[] bytes) {
            }
        });
        f.asciiFormattedMessage(TestConstants.version);
        f.asciiFormattedMessage(TestConstants.speed0003A);
        f.asciiFormattedMessage(TestConstants.idlePacket);
        f.asciiFormattedMessage(TestConstants.status1);
        f.asciiFormattedMessage(TestConstants.status2);
        f.asciiFormattedMessage(TestConstants.status3);
        f.asciiFormattedMessage(TestConstants.status4);
        f.asciiFormattedMessage(TestConstants.status5);

        f.dispose();
    }

    // create and show, with zero address data
    @Test
    @DisabledIfHeadless
    public void testZeroAddr() {

        StatusFrame f = new StatusFrame();
        f.initComponents();
        f.setVisible(true);
        f.setSource(new DataSource() {
            @Override
            synchronized void sendBytes(byte[] bytes) {
            }
        });
        f.asciiFormattedMessage(TestConstants.version);
        f.asciiFormattedMessage(TestConstants.speed0003A);
        f.asciiFormattedMessage(TestConstants.idlePacket);
        f.asciiFormattedMessage(TestConstants.status6);
        f.asciiFormattedMessage(TestConstants.status2);

        f.dispose();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}

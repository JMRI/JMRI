package jmri.jmrix.cmri.serial.serialmon;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;

import org.junit.jupiter.api.*;


/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SerialFilterFrameTest extends jmri.util.JmriJFrameTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            frame = new SerialFilterFrame(new CMRISystemConnectionMemo());
        }
    }

    @AfterEach
    @Override
    public void tearDown() {
        jmri.util.JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SerialFilterFrameTest.class);

}

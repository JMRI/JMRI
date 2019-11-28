package jmri.jmrix.openlcb.swing.protocoloptions;

import org.junit.After;
import org.junit.Before;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;
import jmri.jmrix.openlcb.OlcbSystemConnectionMemo;

import static org.junit.Assert.*;

/**
 * @author Balazs Racz, (C) 2018.
 */
public class ProtocolOptionsFrameTest extends jmri.util.JmriJFrameTestBase {

    OlcbSystemConnectionMemo scm;

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        scm = new OlcbSystemConnectionMemo();
        if(!GraphicsEnvironment.isHeadless()){
           frame = new ProtocolOptionsFrame(scm);
        }
    }

    @After
    @Override
    public void tearDown() {
        scm.dispose();
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        super.tearDown();
    }
}

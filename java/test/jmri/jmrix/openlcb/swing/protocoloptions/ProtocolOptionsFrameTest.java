package jmri.jmrix.openlcb.swing.protocoloptions;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;
import jmri.jmrix.openlcb.OlcbSystemConnectionMemo;

import static org.junit.Assert.*;

/**
 * @author Balazs Racz, (C) 2018.
 */
public class ProtocolOptionsFrameTest {

    OlcbSystemConnectionMemo scm;
    ProtocolOptionsFrame f;

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        scm = new OlcbSystemConnectionMemo();
    }

    @After
    public void tearDown() throws Exception {
        scm.dispose();
        JUnitUtil.tearDown();
    }

    @Test
    public void initComponents() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        f = new ProtocolOptionsFrame(scm);
        f.initComponents();
    }
}
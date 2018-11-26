package jmri.jmrix.openlcb.swing.protocoloptions;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.awt.GraphicsEnvironment;

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
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        scm = new OlcbSystemConnectionMemo();
        f = new ProtocolOptionsFrame(scm);
    }

    @After
    public void tearDown() throws Exception {
        scm.dispose();
    }

    @Test
    public void initComponents() throws Exception {
        f.initComponents();
    }
}
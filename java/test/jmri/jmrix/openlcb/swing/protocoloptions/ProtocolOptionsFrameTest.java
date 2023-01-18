package jmri.jmrix.openlcb.swing.protocoloptions;

import org.junit.jupiter.api.*;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;
import jmri.jmrix.openlcb.OlcbSystemConnectionMemoScaffold;


/**
 * @author Balazs Racz, (C) 2018.
 */
public class ProtocolOptionsFrameTest extends jmri.util.JmriJFrameTestBase {

    OlcbSystemConnectionMemoScaffold scm;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        scm = new OlcbSystemConnectionMemoScaffold();
        if(!GraphicsEnvironment.isHeadless()){
           frame = new ProtocolOptionsFrame(scm);
        }
    }

    @AfterEach
    @Override
    public void tearDown() {
        scm.dispose();
        scm = null;
        super.tearDown();
    }
}

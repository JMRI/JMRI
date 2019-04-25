package jmri.jmrix.sprog.update;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.sprog.SprogSystemConnectionMemo;
import jmri.jmrix.sprog.SprogTrafficControlScaffold;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class Sprogv4UpdateFrameTest extends jmri.util.JmriJFrameTestBase {

    private SprogTrafficControlScaffold stcs = null;
    private SprogSystemConnectionMemo m = null;

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        m = new jmri.jmrix.sprog.SprogSystemConnectionMemo(jmri.jmrix.sprog.SprogConstants.SprogMode.OPS);
        stcs = new SprogTrafficControlScaffold(m);
        m.setSprogTrafficController(stcs);
        m.configureCommandStation();
        if(!GraphicsEnvironment.isHeadless()){
           frame = new Sprogv4UpdateFrame(m);
	}
    }

    @After
    @Override
    public void tearDown() {
        m.getSlotThread().interrupt();
        stcs.dispose();
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(Sprogv4UpdateFrameTest.class);

}

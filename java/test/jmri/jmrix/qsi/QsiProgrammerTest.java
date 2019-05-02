package jmri.jmrix.qsi;

import jmri.util.JUnitUtil;
import jmri.ProgrammingMode;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class QsiProgrammerTest extends jmri.jmrix.AbstractProgrammerTest {

    @Test
    @Override
    public void testDefault() {
        Assert.assertEquals("Check Default", ProgrammingMode.PAGEMODE,
                programmer.getMode());        
    }
    
    @Override
    @Test
    public void testDefaultViaBestMode() {
        Assert.assertEquals("Check Default", ProgrammingMode.PAGEMODE,
                ((QsiProgrammer)programmer).getBestMode());        
    }

    @Override
    @Test(expected=java.lang.IllegalArgumentException.class)
    public void testSetGetMode() {
        programmer.setMode(ProgrammingMode.REGISTERMODE);
        Assert.assertEquals("Check mode matches set", ProgrammingMode.REGISTERMODE,
                programmer.getMode());        
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        QsiTrafficController tc = new QsiTrafficControlScaffold();
        QsiSystemConnectionMemo memo = new QsiSystemConnectionMemo(tc);
        programmer = new QsiProgrammer(memo);
    }

    @Override
    @After
    public void tearDown() {
        programmer = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(QsiProgrammerTest.class);

}

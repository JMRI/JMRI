package jmri.jmrix.qsi;

import jmri.util.JUnitUtil;
import jmri.ProgrammingMode;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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
    @Test
    public void testSetGetMode() {
        Assert.assertThrows(IllegalArgumentException.class, () -> programmer.setMode(ProgrammingMode.REGISTERMODE));
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        QsiTrafficController tc = new QsiTrafficControlScaffold();
        QsiSystemConnectionMemo memo = new QsiSystemConnectionMemo(tc);
        programmer = new QsiProgrammer(memo);
    }

    @Override
    @AfterEach
    public void tearDown() {
        programmer = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(QsiProgrammerTest.class);

}

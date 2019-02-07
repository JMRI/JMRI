package jmri.jmrix.zimo;

import jmri.ProgrammingMode;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class Mx1ProgrammerTest extends jmri.jmrix.AbstractProgrammerTest {

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
                ((Mx1Programmer)programmer).getBestMode());        
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
        Mx1TrafficController tc = new Mx1TrafficController(new Mx1CommandStation(),false){
           @Override
           public boolean status(){
              return true;
           }
           @Override
           public void sendMx1Message(Mx1Message m,Mx1Listener reply) {
           }
        };
        programmer = new Mx1Programmer(tc);
    }

    @Override
    @After
    public void tearDown() {
        programmer = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(Mx1ProgrammerTest.class);

}

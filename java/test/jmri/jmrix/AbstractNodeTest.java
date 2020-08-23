package jmri.jmrix;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class AbstractNodeTest {

    @Test
    public void testCTor() {
        AbstractNode t = new AbstractNode(){
           @Override
           protected boolean checkNodeAddress(int address){
              return true;
           }
           @Override
           public AbstractMRMessage createInitPacket(){
              return null;
           }
           @Override
           public AbstractMRMessage createOutPacket(){
              return null;
           }
           @Override
           public boolean getSensorsActive(){
              return true;
           }
           @Override
           public boolean handleTimeout(AbstractMRMessage m, AbstractMRListener l){
              return true;
           }
           @Override
           public void resetTimeout(AbstractMRMessage m){
           }
        };
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(AbstractNodeTest.class);

}

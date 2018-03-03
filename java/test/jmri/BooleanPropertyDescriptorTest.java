package jmri;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2018	
 */
public class BooleanPropertyDescriptorTest {

    @Test
    public void testCTor() {
        BooleanPropertyDescriptor t = new BooleanPropertyDescriptor("test",false){
            @Override
            public String getColumnHeaderText(){
               return "test";
            }
    
            @Override
            public boolean isEditable(NamedBean bean){
               return false;
            }
        };
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TurnoutOperationManagerTest.class);

}

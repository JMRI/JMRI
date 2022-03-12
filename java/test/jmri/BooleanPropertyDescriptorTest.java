package jmri;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TurnoutOperationManagerTest.class);

}

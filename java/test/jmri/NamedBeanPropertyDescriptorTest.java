package jmri;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2018
 */
public class NamedBeanPropertyDescriptorTest {

    @Test
    public void testCTor() {
        NamedBeanPropertyDescriptor<String> t = new NamedBeanPropertyDescriptor<String>("test","test"){
            @Override
            public String getColumnHeaderText(){
               return "test";
            }
    
            @Override
            public boolean isEditable(NamedBean bean){
               return false;
            }
        };
        Assertions.assertNotNull( t, "exists");
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

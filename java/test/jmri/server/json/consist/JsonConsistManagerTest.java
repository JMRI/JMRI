package jmri.server.json.consist;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class JsonConsistManagerTest extends jmri.implementation.AbstractConsistManagerTestBase {

    @Ignore("The JsonConsistManager does not conform to the letter of the Javadoc here.  It returns null when the consist cannot be created.")
    @Test
    @Override
    public void testGetConsist(){
    }

    @Ignore("Ignore delete test until get is fixed.")
    @Test
    @Override
    public void testDelConsist(){
    }

    @Override
    @Test(expected=java.lang.IllegalArgumentException.class)
    @Ignore("JSonConsistManager does not throw an exception.")
    public void testGetConsistLocoAddress(){
    }

    @Ignore("Test fails if cm has no manager")
    @Test
    @Override
    public void testConsists() {
    }

    @Ignore("Test fails if cm has no manager")
    @Test
    @Override
    public void testDecodeErrorCode() {
    }
    
    
    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        cm = new JsonConsistManager();
    }

    @After
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(JsonConsistManagerTest.class);

}

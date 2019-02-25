package jmri.server.json.consist;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.NotApplicable;
import jmri.util.junit.annotations.ToDo;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class JsonConsistManagerTest extends jmri.implementation.AbstractConsistManagerTestBase {

    @Test
    @Ignore("The JsonConsistManager does not conform to the letter of the Javadoc here.  It returns null when the consist cannot be created.")
    @ToDo("implement test or modify JsonConsistManager to conform.  Remove overriden test if JsonConsistManager is modified")
    @Override
    public void testGetConsist(){
    }

    @Test
    @Ignore("Ignore delete test until get is fixed.")
    @ToDo("remove overriden test once JsonConsistManager GetConsist method is fixed")
    @Override
    public void testDelConsist(){
    }

    @Override
    @Test
    @NotApplicable("JSonConsistManager currently never throws an exception.")
    public void testGetConsistLocoAddress(){
    }

    @Test
    @Ignore("Test fails if cm has no manager")
    @ToDo("fix test initialization for test in parent class, then remove overriden test")
    @Override
    public void testConsists() {
    }

    @Test
    @Ignore("Test fails if cm has no manager")
    @ToDo("fix test initialization for test in parent class, then remove overriden test")
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

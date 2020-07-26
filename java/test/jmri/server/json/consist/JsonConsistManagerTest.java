package jmri.server.json.consist;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.NotApplicable;
import jmri.util.junit.annotations.ToDo;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class JsonConsistManagerTest extends jmri.implementation.AbstractConsistManagerTestBase {

    @Test
    @Disabled("The JsonConsistManager does not conform to the letter of the Javadoc here.  It returns null when the consist cannot be created.")
    @ToDo("implement test or modify JsonConsistManager to conform.  Remove overriden test if JsonConsistManager is modified")
    @Override
    public void testGetConsist() {
    }

    @Test
    @Disabled("Ignore delete test until get is fixed.")
    @ToDo("remove overriden test once JsonConsistManager GetConsist method is fixed")
    @Override
    public void testDelConsist() {
    }

    @Override
    @Test
    @NotApplicable("JSonConsistManager currently never throws an exception.")
    public void testGetConsistLocoAddress() {
    }

    @Test
    @Disabled("Test fails if cm has no manager")
    @ToDo("fix test initialization for test in parent class, then remove overriden test")
    @Override
    public void testConsists() {
    }

    @Test
    @Disabled("Test fails if cm has no manager")
    @ToDo("fix test initialization for test in parent class, then remove overriden test")
    @Override
    public void testDecodeErrorCode() {
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        cm = new JsonConsistManager();
    }

    @AfterEach
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(JsonConsistManagerTest.class);
}

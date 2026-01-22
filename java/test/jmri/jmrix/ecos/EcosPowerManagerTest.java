package jmri.jmrix.ecos;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class EcosPowerManagerTest {

    @Test
    public void testCTor() {
        EcosTrafficController tc = new EcosInterfaceScaffold();
        EcosPowerManager t = new EcosPowerManager(tc);
        assertNotNull( t, "exists");

        assertDoesNotThrow( () -> t.dispose() );
        tc.terminateThreads();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(EcosPowerManagerTest.class);

}

package jmri.jmrix.openlcb;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.openlcb.*;

/**
 *
 * @author Bob Jacobsen   (c) 2026
 */
public class OlcbEventNameStoreTest {

    OlcbEventNameStore olcbStore;

    @Test
    public void testLoadAndStoreEventInfo() {
        olcbStore.addMatch(new EventID(1234), "named 1234");
        assertEquals(new EventID(1234), olcbStore.getEventID("named 1234"));
        assertEquals("named 1234", olcbStore.getEventName(new EventID(1234)));
        assertTrue(olcbStore.hasEventName(new EventID(1234)));

        assertFalse(olcbStore.hasEventName(new EventID(4321)));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        olcbStore = new OlcbEventNameStore() {
            @Override 
            public void readDetails() {
                // don't read the eventNames.xml file
            }
            @Override
            public void writeEventNameDetails() {
                // don't write the eventNames.xml file
            }
        };
    }

    @AfterEach
    public void tearDown() {
        if (olcbStore != null) {
            olcbStore.deregisterShutdownTask();
        }
        JUnitUtil.tearDown();
    }

    // private static final Logger log = LoggerFactory.getLogger(OlcbEventNameStoreTest.class);

}

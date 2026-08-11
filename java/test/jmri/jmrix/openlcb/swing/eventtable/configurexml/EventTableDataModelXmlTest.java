package jmri.jmrix.openlcb.swing.eventtable.configurexml;

import jmri.InstanceManager;
import jmri.Light;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.openlcb.OlcbLight;
import jmri.jmrix.openlcb.OlcbLightManager;
import jmri.jmrix.openlcb.OlcbTestInterface;
import jmri.util.JUnitUtil;

import org.jdom2.Element;
import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for EventTable persistence
 *
 * @author   Bob Jacobsen 2026
 */
public class EventTableDataModelXmlTest {

    @Test
    public void testSaveAndRestore() {
    }

    OlcbTestInterface t;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EventTableDataModelXmlTest.class);
}


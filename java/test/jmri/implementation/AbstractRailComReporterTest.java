package jmri.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.IdTag;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class AbstractRailComReporterTest extends AbstractReporterTestBase {

    @Override
    protected Object generateObjectToReport(){
        return new DefaultRailCom("ID1234", "Test Tag");
    }

    // AbstractRailComReporter implements the IdTagListener interface, which
    // includes a notify(IdTag) method.
    @Test
    public void testNotify() {
        Assumptions.assumeTrue(r instanceof AbstractRailComReporter,
            "Not an AbstractRailComReporter"); // EcosReporter
        Assumptions.assumeTrue(generateObjectToReport() instanceof IdTag,
            "generateObjectToReport is not an IdTag");
        assertEquals(IdTag.UNKNOWN,r.getState(), "IdTag not Seen");
        ((AbstractRailComReporter)r).notify((IdTag)generateObjectToReport());
        // Check that both CurrentReport and LastReport are not null
        assertNotNull(r.getCurrentReport(), "CurrentReport Object exists");
        assertNotNull(r.getLastReport(), "LastReport Object exists");
        // Check the value of both CurrentReport and LastReport
        assertEquals(r.getLastReport(), r.getCurrentReport(), "CurrentReport equals LastReport");
        assertEquals(IdTag.SEEN,r.getState(), "IdTag Seen");

        // send a null report.

        ((AbstractRailComReporter)r).notify(null);
        // Check that both CurrentReport and LastReport are not null
        assertNull(r.getCurrentReport(), "CurrentReport Object Null");
        assertNotNull(r.getLastReport(), "LastReport Object exists");
        assertEquals(IdTag.UNSEEN,r.getState(), "IdTag Seen");
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        r = new AbstractRailComReporter("IR1");
    }

    @AfterEach
    @Override
    public void tearDown() {
        r = null;
        if ( InstanceManager.getNullableDefault(jmri.IdTagManager.class) != null ) {
            InstanceManager.getDefault(jmri.IdTagManager.class).dispose();
        }
        // JUnitUtil.clearShutDownManager(); // would be better to check and clean up specifics in tests
        JUnitUtil.tearDown();
    }

}

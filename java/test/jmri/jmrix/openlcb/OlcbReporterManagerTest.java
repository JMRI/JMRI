package jmri.jmrix.openlcb;

import jmri.InstanceManager;
import jmri.RailComManager;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.beans.PropertyVetoException;

/**
 *
 * @author Bob Jacobsen Coyright (C) 2023
 * @author Balazs Racz Coyright (C) 2023
 */
public class OlcbReporterManagerTest extends jmri.managers.AbstractReporterMgrTestBase {

    OlcbTestInterface ti;

    protected String getNameToTest1() {
        return "06.80.0D.11.22.33.00.00";
    }

    protected String getNameToTest2() {
        return "06.80.0D.44.55.66.00.00";
    }

    // For some reason this test hardcodes system name "1" and "2"
    @Override
    public void testRegisterDuplicateSystemName() throws PropertyVetoException, NoSuchFieldException, IllegalAccessException {
        testRegisterDuplicateSystemName((OlcbReporterManager)l,
                l.makeSystemName(getNameToTest1()), l.makeSystemName(getNameToTest2()));
    }

    // OpenLCB can not create a reporter just by number.
    @Override
    public void testReporterProvideByNumber() {}


    @Test
    public void testIdentified() {
        l.provideReporter(getNameToTest2());
        // Upon construction, a consumer range identified message was sent out.
        ti.assertSentMessage(":X194a4c4cN06800D445566FFFF;");
        ti.assertNoSentMessages();
        l.provideReporter(getNameToTest1());
        // Upon construction, a consumer range identified message was sent out.
        ti.assertSentMessage(":X194a4c4cN06800D1122330000;");
        ti.assertNoSentMessages();
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
        // prepare an interface
        ti = new OlcbTestInterface(new OlcbTestInterface.CreateConfigurationManager());
        ti.waitForStartup();
        l = new OlcbReporterManager(ti.systemConnectionMemo);
    }

    @Override
    public String getSystemName(String i) {
        return "MR" + i;
    }

    @AfterEach
    public void tearDown() {
        InstanceManager.getDefault(RailComManager.class).dispose();
        l.dispose();
        l = null;
        ti.dispose();
        ti = null;
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(OlcbReporterManagerTest.class);
}

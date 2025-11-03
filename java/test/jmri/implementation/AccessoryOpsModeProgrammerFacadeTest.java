package jmri.implementation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jmri.CommandStation;
import jmri.InstanceManager;
import jmri.ProgListener;
import jmri.Programmer;
import jmri.progdebugger.ProgDebugger;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.ToDo;

import org.junit.jupiter.api.*;

/**
 * Test the AccessoryOpsModeProgrammerFacade class.
 *
 * @author Bob Jacobsen Copyright 2014
 * @author Dave Heap 2017
 *
 */
@ToDo("test mode handling, test packet contents in each mode, test address handling")
public class AccessoryOpsModeProgrammerFacadeTest {

    @Test
    public void testCvLimit() {
        ProgDebugger dp = new ProgDebugger(true, 123);
        dp.setTestReadLimit(1024);
        dp.setTestWriteLimit(1024);

        Programmer p = new AccessoryOpsModeProgrammerFacade(dp, "", 0, dp);

        assertTrue( p.getCanRead("1024"), "CV limit read OK");
        assertTrue( p.getCanWrite("1024"), "CV limit write OK");
        assertFalse( p.getCanRead("1025"), "CV limit read fail");
        assertFalse( p.getCanWrite("1025"), "CV limit write fail");
    }

    @Test
    public void testWriteAddr123long1cv234val14delay200signal() throws jmri.ProgrammerException, InterruptedException {
        assertMethod();
    }

    @Test
    public void testWriteAddr3long0cv12val0delay500accessory() throws jmri.ProgrammerException, InterruptedException {
        assertMethod();
    }

    @Test
    public void testWriteAddr511long1cv1024val255delay0decoder() throws jmri.ProgrammerException, InterruptedException {
        assertMethod();
    }

    // from here down is testing infrastructure
    // Perform tests with parameters parsed from the name of the calling method.
    synchronized void assertMethod() throws jmri.ProgrammerException, InterruptedException {
        String methodName = "";
        int addr = 0;
        boolean isLong = false;
        String addrType = "";
        String cv = "";
        int value = 0;
        int delay = 0;

        ArrayList<String> items = itemsFromMethodName(3, 5);
        if (!items.isEmpty()) {
            methodName = items.get(0);
            addr = Integer.parseInt(items.get(2));
            isLong = (Integer.parseInt(items.get(4)) != 0);
            cv = items.get(6);
            value = Integer.parseInt(items.get(8));
            delay = Integer.parseInt(items.get(10));
            addrType = items.get(11);
        }
        log.debug(
                "Testing: {}:\nExtracted parameters addr='{}',isLong={},cv='{}', value={}, delay={}, addrType='{}'",
                methodName, addr, isLong, cv, value, delay, addrType);

        // Create an addressed programmer.
        ProgDebugger dp = new ProgDebugger(isLong, addr);
        // Create a facade over the base programmer and also a listener for the facade.
        Programmer p = new AccessoryOpsModeProgrammerFacade(dp, addrType, 0, dp);
        ProgListener l = (int value1, int status) -> {
            log.debug("callback value={} status={}", value1, status);
            setReplied(true);
            readValue = value1;
        };

        // Write to the facade programmer.
        p.writeCV(cv, value, l);
        waitReply();

        Assertions.assertEquals(value, readValue);

        // Check that the write did not through to the base programmer.
        assertFalse( dp.hasBeenWritten(value), "target not directly written");
        assertFalse( dp.hasBeenWritten(81), "index not written");
        // Check that a packet was sent.
        assertNotNull( mockCS.lastPacket, "packet sent");
    }

    // Extract test parameters from test name.
    synchronized ArrayList<String> itemsFromMethodName(int methodOffset, int groupReps) {
        StringBuilder sb = new StringBuilder();
        Pattern pattern;
        Matcher matcher;
        ArrayList<String> retString = new ArrayList<>();

        // Extract test parameters from test name.
        String methodName = Thread.currentThread().getStackTrace()[methodOffset].getMethodName();
        sb.append("^");
        for (int i = 1; i <= groupReps; i++) {
            sb.append("(\\D+)(\\d+)");
        }
        sb.append("(\\D*)$");
        String pat = sb.toString();
        pattern = Pattern.compile(pat);
        matcher = pattern.matcher(methodName);
        log.debug("Test: {} pat=\"{}\", groupCount={}", methodName, pat, matcher.groupCount());
        if (matcher.find()) {
            for (int j = 0; j <= matcher.groupCount(); j++) {
                retString.add(matcher.group(j));
                log.debug("Adding item={}, string=\"{}\"", j, matcher.group(j));
            }
        } else {
            log.error("method=\"{}\" did not match pattern=\"{}\"", methodName, pat);
        }
        return retString;
    }

    private MockCommandStation mockCS;
    private int readValue = -2;
    private boolean replied;

    synchronized void setReplied( boolean newVal) {
        replied = newVal;
    }

    synchronized void waitReply() throws InterruptedException {
        while (!replied) {
            wait(200);
        }
        replied = false;
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        mockCS = new MockCommandStation();
        InstanceManager.setDefault(CommandStation.class, mockCS);
        mockCS.lastPacket = null;
        setReplied(false);
        readValue = -2;
    }

    @AfterEach
    public void tearDown() {
        mockCS = null;
        JUnitUtil.tearDown();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AccessoryOpsModeProgrammerFacadeTest.class);

}

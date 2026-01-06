package jmri.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jmri.ProgListener;
import jmri.Programmer;
import jmri.progdebugger.ProgDebugger;

import org.junit.jupiter.api.*;

/**
 * Test the OpsModeDelayedProgrammerFacade class.
 *
 * @author Bob Jacobsen Copyright 2014
 *
 */
public class OpsModeDelayedProgrammerFacadeTest {

    @Test
    public void testWrite4Val12Delay0() throws jmri.ProgrammerException, InterruptedException {
        testMethod(123, true);
    }

    @Test
    public void testWrite37Val102Delay1000() throws jmri.ProgrammerException, InterruptedException {
        testMethod(123, true);
    }

    @Test
    public void testWrite1024Val255Delay2000() throws jmri.ProgrammerException, InterruptedException {
        testMethod(123, true);
    }

    @Test
    public void testWrite0Val23Delay100() throws jmri.ProgrammerException, InterruptedException {
        testMethod(123, true);
    }

    @Test
    public void testCvLimit() {
        ProgDebugger dp = new ProgDebugger(true, 123);
        dp.setTestReadLimit(1024);
        dp.setTestWriteLimit(1024);

        Programmer p = new OpsModeDelayedProgrammerFacade(dp, 0);

        assertTrue( p.getCanRead("1024"), "CV limit read OK");
        assertTrue( p.getCanWrite("1024"), "CV limit write OK");
        assertFalse( p.getCanRead("1025"), "CV limit read fail");
        assertFalse( p.getCanWrite("1025"), "CV limit write fail");
    }

    private int facProgRetValue = -2;
    private int facProgRetStatus = -2;
    // private int facProgRetDelay = 0;
    private transient volatile boolean facProgReplied = false;

    // Perform tests with parameters parsed from the name of the calling method.
    synchronized void testMethod(int addr, boolean addrType) throws jmri.ProgrammerException, InterruptedException {
        String methodName = "";
        String cv = "";
        int value = 0;
        int delay;

        facProgRetValue = -2;
        facProgRetStatus = -2;
        facProgReplied = false;

        ArrayList<String> items = itemsFromMethodName(3, 3);
        if (!items.isEmpty()) {
            methodName = items.get(0);
            cv = items.get(2);
            value = Integer.parseInt(items.get(4));
            delay = Integer.parseInt(items.get(6));
        } else {
            delay = 0;
        }
        log.debug("Testing: {}:\nExtracted parameters cv='{}', value={}, delay={}", methodName, cv, value, delay);

        // Create a base addressed programmer.
        ProgDebugger baseProg = new ProgDebugger(addrType, addr);

        // Create a facade over the base programmer and also a listener for the facade.
        Programmer facProg = new OpsModeDelayedProgrammerFacade(baseProg, delay);
        ProgListener facProgListnr = new ProgListener() {
            @Override
            public void programmingOpReply(int value, int status) {
                log.debug("facProg callback value={},status={}", value, +status);
                facProgReplied = true;
                facProgRetValue = value;
                facProgRetStatus = status;
            }
        };

        // Write to the facade programmer.
        Instant start = Instant.now();
        facProg.writeCV(cv, value, facProgListnr);
        facProgWaitReply();
        Instant end = Instant.now();
        long elapsed = Duration.between(start, end).toMillis();

        // Check that the write flowed through to the base programmer.
        assertTrue( baseProg.hasBeenWritten(Integer.parseInt(cv)), "Original CV has been written");
        assertEquals( Integer.parseInt(cv), baseProg.lastWriteCv(), "Original CV was last one written");
        assertEquals( value, baseProg.lastWrite(), "Original CV value is as expected");

        log.debug("Notification delay={}, elapsed={}", delay, elapsed);
        assertTrue( elapsed >= delay,
            () -> MessageFormat.format("Elapsed time ({0}) >= delay  ({1})", elapsed, delay));
        assertEquals( value, facProgRetValue, "Facade listener return value OK");
        assertEquals( ProgListener.OK, facProgRetStatus, "Facade listener return status OK");
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

    synchronized void facProgWaitReply() throws InterruptedException {
        while (!facProgReplied) {
            wait(10);
        }
        facProgReplied = false;
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OpsModeDelayedProgrammerFacadeTest.class);

}

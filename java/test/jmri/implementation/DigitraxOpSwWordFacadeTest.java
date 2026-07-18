package jmri.implementation;

import java.util.ArrayList;
import java.util.List;

import jmri.ProgListener;
import jmri.Programmer;
import jmri.ProgrammerException;
import jmri.ProgrammingMode;

import jmri.jmrix.AbstractProgrammer;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * Test the DigitraxOpSwWordFacade class.
 *
 * @author Bob Jacobsen Copyright 2026
 * 
 */
public class DigitraxOpSwWordFacadeTest {


    @Test
    public void testWriteReadDirect() throws jmri.ProgrammerException, InterruptedException {

        OpSwProgrammer dp = new OpSwProgrammer();
        Programmer p = new DigitraxOpSwWordFacade(dp);
        ProgListener l = getNewProgListener();

        replied = false;
        dp.operations = new ArrayList<Record>(List.of(
            new Record("115.4", 12, false)
        ));
        p.writeCV("115.4", 12, l);
        JUnitUtil.waitFor(() -> replied, "Write completed");

        replied = false;
        dp.operations = new ArrayList<Record>(List.of(
            new Record("115.5", 12, true)
        ));
        p.readCV("115.5", l);
        JUnitUtil.waitFor(() -> replied, "Read completed");
        Assert.assertEquals("read back", 12, readValue);
    }

    // from here down is testing infrastructure

    // This represents the result of one read or write operation for checking
    class Record {
        Record(String cv, int value, boolean read) {
            this.cv = cv;
            this.value = value;
            this.read = read;
        }
        String cv;
        int value;
        boolean read;
        @Override
        public boolean equals(Object other) {
            Record c = (Record) other;
            return (this.cv.equals(c.cv) && this.value == c.value);
        }
    }

    // This class checks the intermediate operations that the facade creates
    class OpSwProgrammer extends AbstractProgrammer {
        
        ArrayList<Record> operations = new ArrayList<>();
        
        void clear() { 
            operations = new ArrayList<>(); 
        }
        
        @Override
        protected void timeout() {} // just there for AbstractProgrammer

        @Override
        public void writeCV(String cv, int val, ProgListener p) throws ProgrammerException {
            // check against expected
            var expected = operations.remove(0);
            if (!expected.cv.equals(cv)) Assert.fail("CV did not match");
            if (expected.value != val) Assert.fail("Value did not match");
            if (expected.read != false) Assert.fail("read/write did not match");
            p.programmingOpReply(0,0);
        }
    
        @Override
        public void readCV(String cv, ProgListener p) throws ProgrammerException {
            // check against expected
            var expected = operations.remove(0);
            if (!expected.cv.equals(cv)) Assert.fail("CV did not match");
            if (expected.read != true) Assert.fail("read/write did not match");
            // return designated value with OK status
            p.programmingOpReply(expected.value,0);
        }
    
        @Override
        public void confirmCV(String cv, int val, ProgListener p) throws ProgrammerException {
            // check against expected
            var expected = operations.remove(0);
            if (!expected.cv.equals(cv)) Assert.fail("CV did not match");
            if (expected.read != true) Assert.fail("read/write did not match");
            // return designated value with OK status
            p.programmingOpReply(expected.value,0);
        }

        @Override
        public List<ProgrammingMode> getSupportedModes() {return new ArrayList<ProgrammingMode>();}

    }

    int readValue = 0; // results of the last read operation
    boolean replied;   // the most recent operation has returned a result
    
    // This ProgListener is used to check the overall operation.
    // It's not used to check the intermediate operations.
    ProgListener getNewProgListener() {
        ProgListener l = new ProgListener() {
            @Override
            public void programmingOpReply(int value, int status) {
                log.debug("callback value={} status={}", value, status);
                replied = true;
                readValue = value;
            }
        };
        return l;
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown(){
        jmri.util.JUnitUtil.tearDown();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DigitraxOpSwWordFacadeTest.class);
}

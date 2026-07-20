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

    // test reads and writes that fall through
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

    // test of a full-word write
    @Test
    public void testWriteFullWord() throws jmri.ProgrammerException, InterruptedException {

        OpSwProgrammer dp = new OpSwProgrammer();
        Programmer p = new DigitraxOpSwWordFacade(dp);
        ProgListener l = getNewProgListener();

        replied = false;
        dp.operations = new ArrayList<Record>(List.of(
            new Record("115.25", 1, false),
            new Record("115.26", 1, false),
            new Record("115.27", 0, false),
            new Record("115.28", 0, false),
            new Record("115.29", 1, false),
            new Record("115.30", 0, false),
            new Record("115.31", 0, false),
            new Record("115.32", 1, false),

            new Record("115.46", 1, false),
            new Record("115.47", 0, false),
            new Record("115.48", 1, false),

            new Record("115.33", 1, false),
            new Record("115.34", 1, false),
            new Record("115.35", 0, false),
            new Record("115.36", 0, false),
            new Record("115.37", 0, false),
            new Record("115.38", 1, false),
            new Record("115.39", 0, false),
            new Record("115.40", 0, false),
            new Record("115.41", 1, false),
            new Record("115.42", 0, false),
            new Record("115.43", 0, false),
            new Record("115.44", 0, false),
            new Record("115.45", 0, false),

            new Record("115.62", 0, false),
            new Record("115.63", 1, false),
            new Record("115.64", 0, false),

            new Record("115.49", 0, false),
            new Record("115.50", 0, false),
            new Record("115.51", 1, false),
            new Record("115.52", 1, false),
            new Record("115.53", 0, false),
            new Record("115.54", 1, false),
            new Record("115.55", 1, false),
            new Record("115.56", 1, false),
            new Record("115.57", 0, false),
            new Record("115.58", 1, false),
            new Record("115.59", 0, false),
            new Record("115.60", 0, false),
            new Record("115.61", 0, false)
        ));
        int checkValue = (0x2<<29)+(0x2EC<<16)+(0x5<<13)+(0x123);
        
        p.writeCV("115.25.147", checkValue, l);// 0x93 is 147
        JUnitUtil.waitFor(() -> replied, "Write completed");
        Assert.assertEquals("finished full list", 0, dp.operations.size());
    }
    
    // test of a full-word read
    @Test
    public void testReadFullWord() throws jmri.ProgrammerException, InterruptedException {

        OpSwProgrammer dp = new OpSwProgrammer();
        Programmer p = new DigitraxOpSwWordFacade(dp);
        ProgListener l = getNewProgListener();

        replied = false;
        dp.operations = new ArrayList<Record>(List.of(
            new Record("115.25", 1, false),
            new Record("115.26", 1, false),
            new Record("115.27", 0, false),
            new Record("115.28", 0, false),
            new Record("115.29", 1, false),
            new Record("115.30", 0, false),
            new Record("115.31", 0, false),
            new Record("115.32", 1, false),

            new Record("115.46", 1, true),
            new Record("115.47", 0, true),
            new Record("115.48", 1, true),

            new Record("115.33", 1, true),
            new Record("115.34", 1, true),
            new Record("115.35", 0, true),
            new Record("115.36", 0, true),
            new Record("115.37", 0, true),
            new Record("115.38", 1, true),
            new Record("115.39", 0, true),
            new Record("115.40", 0, true),
            new Record("115.41", 1, true),
            new Record("115.42", 0, true),
            new Record("115.43", 0, true),
            new Record("115.44", 0, true),
            new Record("115.45", 0, true),

            new Record("115.62", 0, true),
            new Record("115.63", 1, true),
            new Record("115.64", 0, true),

            new Record("115.49", 0, true),
            new Record("115.50", 0, true),
            new Record("115.51", 1, true),
            new Record("115.52", 1, true),
            new Record("115.53", 0, true),
            new Record("115.54", 1, true),
            new Record("115.55", 1, true),
            new Record("115.56", 1, true),
            new Record("115.57", 0, true),
            new Record("115.58", 1, true),
            new Record("115.59", 0, true),
            new Record("115.60", 0, true),
            new Record("115.61", 0, true)
        ));
        int checkValue = (0x2<<29)+(0x2EC<<16)+(0x5<<13)+(0x123);
        
        p.readCV("115.25.147", l, 0);// 0x93 is 147
        JUnitUtil.waitFor(() -> replied, "Write completed");
        Assert.assertEquals("finished full list", 0, dp.operations.size());
        Assert.assertEquals("correct result", checkValue, readValue);
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
            log.debug("checking write CV {}", expected.cv);
            Assert.assertEquals("CV did not match", expected.cv, cv);
            Assert.assertEquals("Value did not match", expected.value, val);
            Assert.assertFalse("read/write did not match", expected.read);
            p.programmingOpReply(0,0);
        }
    
        @Override
        public void readCV(String cv, ProgListener p) throws ProgrammerException {
            // check against expected
            var expected = operations.remove(0);
            log.debug("checking read CV {}", expected.cv);
            Assert.assertEquals("CV did not match", expected.cv, cv);
            Assert.assertTrue("read/write did not match", expected.read);
            // return designated value with OK status
            p.programmingOpReply(expected.value,0);
        }
    
        @Override
        public void confirmCV(String cv, int val, ProgListener p) throws ProgrammerException {
            // check against expected
            var expected = operations.remove(0);
            Assert.assertEquals("CV did not match", expected.cv, cv);
            Assert.assertEquals("Value did not match", expected.value, val);
            Assert.assertTrue("read/write did not match", expected.read);
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

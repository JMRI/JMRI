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
        p.writeCV("115.4", 12, l);
        JUnitUtil.waitFor(() -> replied, "Write completed");
        Assert.assertEquals("target CV written", "115.4", dp.written.get(0).cv);
        Assert.assertEquals("target value written", 12, dp.written.get(0).value);

        replied = false;
        dp.read.add(new Record("115.4", 12));
        p.readCV("115.5", l);
        JUnitUtil.waitFor(() -> replied, "Read completed");
        Assert.assertEquals("read back", 12, readValue);
    }

    // from here down is testing infrastructure

    class Record {
        Record(String cv, int value) {
            this.cv = cv;
            this.value = value;
        }
        String cv;
        int value;
        @Override
        public boolean equals(Object other) {
            Record c = (Record) other;
            return (this.cv.equals(c.cv) && this.value == c.value);
        }
    }

    class OpSwProgrammer extends AbstractProgrammer {
        
        ArrayList<Record> written = new ArrayList<>();
        ArrayList<Record> read = new ArrayList<>();
        
        void clear() { 
            written = new ArrayList<>(); 
            read = new ArrayList<>();
        }
        
        @Override
        protected void timeout() {} // just there for AbstractProgrammer

        @Override
        public void writeCV(String cv, int val, ProgListener p) throws ProgrammerException {
            // record for later use
            written.add(new Record(cv, val));
            p.programmingOpReply(0,0);
        }
    
        @Override
        public void readCV(String cv, ProgListener p) throws ProgrammerException {
            // the first element of the read array provides the value,
            // but first check for correct CV read
            if (read.size() == 0) Assert.fail("No read values available");
            var record = read.remove(0);
            if (record.cv.equals(cv)) Assert.fail("Expected cv "+record.cv+" got "+cv);
            
            // return designated value with OK status
            p.programmingOpReply(record.value,0);
        }
    
        @Override
        public void confirmCV(String cv, int val, ProgListener p) throws ProgrammerException {
            p.programmingOpReply(0,0);
        }

        @Override
        public List<ProgrammingMode> getSupportedModes() {return new ArrayList<ProgrammingMode>();}

    }

    int readValue = 0; // results of the last read operation
    boolean replied;   // the most recent operation has returned a result
    
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

package jmri.jmrit.symbolicprog;

import jmri.progdebugger.ProgDebugger;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test CvValue class
 *
 * @author	Bob Jacobsen Copyright 2004, 2006, 2015
 */
public class CvValueTest {

    ProgDebugger p = new ProgDebugger();

    @Test
    public void testStart() {
        new CvValue("12", p);
    }

    // can we create one and manipulate info?
    @Test
    public void testCvValCreate() {
        CvValue cv = new CvValue("19", p);
        Assert.assertTrue(cv.number() == "19");
        cv.setValue(23);
        Assert.assertTrue(cv.getValue() == 23);
    }

    // check a read operation
    @Test
    public void testCvValRead() {

        // create the CV value
        CvValue cv = new CvValue("91", p);
        cv.read(null);
        // wait for reply (normally, done by callback; will check that later)
        JUnitUtil.waitFor(()->{return !cv.isBusy();}, "cv.isBusy");
        
        Assert.assertTrue(cv.getValue() == 123);
        Assert.assertTrue(cv.getState() == CvValue.READ);
    }

    // check a confirm operation
    @Test
    public void testCvValConfirmFail() {

        // create the CV value
        CvValue cv = new CvValue("66", p);
        cv.setValue(91);

        cv.confirm(null);
        // wait for reply (normally, done by callback; will check that later)
        JUnitUtil.waitFor(()->{return !cv.isBusy();}, "cv.isBusy");

        Assert.assertEquals("CV value ", 91, cv.getValue());
        Assert.assertEquals("CV state ", CvValue.UNKNOWN, cv.getState());
    }

    // check a confirm operation
    @Test
    public void testCvValConfirmPass() {

        // create the CV value
        CvValue cv = new CvValue("67", p);
        cv.setValue(123);
        cv.write(null); // force out, so dummy read works
        // release, to ensure
        JUnitUtil.waitFor(()->{return !cv.isBusy();}, "cv.isBusy");

        cv.confirm(null);
        // wait for reply (normally, done by callback; will check that later)
        JUnitUtil.waitFor(()->{return !cv.isBusy();}, "cv.isBusy");

        Assert.assertEquals("CV value ", 123, cv.getValue());
        Assert.assertEquals("CV state ", CvValue.SAME, cv.getState());
    }

    // check a write operation
    @Test
    public void testCvValWrite() {
        // initialize the system
        log.debug("start testCvValWrite");

        // create the CV value
        CvValue cv = new CvValue("91", p);
        cv.setValue(12);
        cv.write(null);
        // wait for reply (normally, done by callback; will check that later)
        JUnitUtil.waitFor(()->{return !cv.isBusy();}, "cv.isBusy");

        Assert.assertEquals("cv value ", 12, cv.getValue());
        Assert.assertEquals("cv state ", CvValue.STORED, cv.getState());
        Assert.assertEquals("last value written ", 12, p.lastWrite());
    }

    // check the state diagram
    @Test
    public void testCvValStates() {
        CvValue cv = new CvValue("21", p);
        Assert.assertTrue(cv.getState() == CvValue.UNKNOWN);
        cv.setValue(23);
        Assert.assertTrue(cv.getState() == CvValue.EDITED);
    }

    // check the initial color
    @Test
    public void testInitialColor() {
        CvValue cv = new CvValue("21", p);
        Assert.assertEquals("initial color", CvValue.COLOR_UNKNOWN, cv.getTableEntry().getBackground());
    }

    // check color update for EDITED
    @Test
    public void testEditedColor() {
        CvValue cv = new CvValue("21", p);
        cv.setValue(23);
        Assert.assertEquals("edited color", CvValue.COLOR_EDITED, cv.getTableEntry().getBackground());
    }

    private final static Logger log = LoggerFactory.getLogger(CvValueTest.class);

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

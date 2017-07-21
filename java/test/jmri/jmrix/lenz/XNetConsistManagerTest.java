package jmri.jmrix.lenz;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XNetConsistManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.XNetConsistManager class
 *
 * @author	Paul Bender Copyright (C) 2012,2017
 */
public class XNetConsistManagerTest extends jmri.implementation.AbstractConsistManagerTestBase {

    // The minimal setup for log4J
<<<<<<< HEAD
    @Override
    protected void setUp() {
=======
    @Before
    @Override
    public void setUp() {
>>>>>>> JMRI/master
        apps.tests.Log4JFixture.setUp();
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());
        cm = new XNetConsistManager(new XNetSystemConnectionMemo(tc));
    }

<<<<<<< HEAD
    @Override
    protected void tearDown() {
=======
    @After
    @Override
    public void tearDown() {
        cm = null;
>>>>>>> JMRI/master
        apps.tests.Log4JFixture.tearDown();
    }

}

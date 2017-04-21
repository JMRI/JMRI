package jmri.jmrix.acela;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class AcelaSignalHeadTest {

    private AcelaSystemConnectionMemo memo = null;

    @Test
    public void testCTor() {
        AcelaSignalHead t = new AcelaSignalHead("AH1",memo);
        Assert.assertNotNull("exists",t);
        jmri.util.JUnitAppender.assertErrorMessage("Can't find new Acela Signal with name 'AH1");
    }

    @Test
    public void test2stringCTor() {
        AcelaSignalHead t = new AcelaSignalHead("AH1","test",memo);
        Assert.assertNotNull("exists",t);
        jmri.util.JUnitAppender.assertErrorMessage("Can't find new Acela Signal with name 'AH1");
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        AcelaTrafficController tc = new AcelaTrafficControlScaffold();
        memo = new AcelaSystemConnectionMemo(tc);
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(AcelaSignalHeadTest.class.getName());

}

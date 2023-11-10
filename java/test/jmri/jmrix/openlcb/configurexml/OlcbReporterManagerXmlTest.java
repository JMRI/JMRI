package jmri.jmrix.openlcb.configurexml;

import jmri.InstanceManager;
import jmri.Reporter;
import jmri.jmrix.openlcb.OlcbReporterManager;
import jmri.jmrix.openlcb.OlcbTestInterface;
import jmri.util.JUnitUtil;
import org.jdom2.Element;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OlcbReporterManagerXmlTest.java
 *
 * Test for the OlcbReporterManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 *           Balazs Racz    (C) 2018, 2023
 */
public class OlcbReporterManagerXmlTest {

    @Test
    public void testSaveAndRestore() throws Exception {
        log.debug("FIRST START");
        t = new OlcbTestInterface(new OlcbTestInterface.CreateConfigurationManager());
        OlcbReporterManager mgr = t.configurationManager.getReporterManager();
        OlcbReporterManagerXml xmlmgr = new OlcbReporterManagerXml();

        mgr.newReporter("MR1.2.3.4.5.6.0.0", "rep1");
        t.flush();
        t.assertSentMessage(":X194a4c4cN010203040506ffff;");
        t.assertNoSentMessages();

        Element stored = xmlmgr.store(mgr);
        Assert.assertNotNull(stored);

        t.dispose();
        InstanceManager.getDefault().clearAll();

        log.debug("SECOND START");

        t = new OlcbTestInterface(new OlcbTestInterface.CreateConfigurationManager());
        mgr = t.configurationManager.getReporterManager();

        xmlmgr.load(stored, null);

        Reporter r2 = mgr.getBySystemName("MR1.2.3.4.5.6.0.0");
        Assert.assertNotNull(r2);
        Assert.assertEquals("rep1", r2.getUserName());
        t.flush();
        t.assertSentMessage(":X194a4c4cN010203040506ffff;");
        t.assertNoSentMessages();

        t.dispose();
    }

    OlcbTestInterface t;
    private final static Logger log = LoggerFactory.getLogger(OlcbReporterManagerXmlTest.class);

    @BeforeAll
    static public void checkSeparate() {
       // this test is run separately because it leaves a lot of threads behind
        org.junit.Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}


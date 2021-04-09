package jmri.jmrix.ecos.networkdriver.configurexml;

import jmri.InstanceManager;
import jmri.ShutDownManager;
import jmri.jmrix.ecos.EcosPreferences;
import jmri.jmrix.ecos.EcosSystemConnectionMemo;
import jmri.jmrix.ecos.networkdriver.ConnectionConfig;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test for the ECoS ConnectionConfigXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 * @author   Egbert Broerse  Copyright (C) 2021
 */
public class ConnectionConfigXmlTest extends jmri.jmrix.configurexml.AbstractNetworkConnectionConfigXmlTestBase {

    private EcosSystemConnectionMemo memo;

    @Test
    @Override
    public void storeTest() {
        super.storeTest();
        // TODO: remove catching java.lang.NullPointerException in ConnectionConfigXml.loadTest()
        jmri.util.JUnitAppender.suppressErrorMessage("Null EcosPrefManager");
        jmri.util.JUnitAppender.suppressErrorMessage("Null EcosPrefManager");
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
        memo = new EcosSystemConnectionMemo(); // takes care of cleaning up the EcosPreferences shutdownTask

        xmlAdapter = new ConnectionConfigXml();
        ((ConnectionConfigXml)xmlAdapter).getInstance();
        /* somehow memo.Preferences is still null after the loadDetails below */
        //((ConnectionConfigXml)xmlAdapter).setSystemConnectionMemo(memo);

        cc = new ConnectionConfig();
        ((ConnectionConfig)cc).setInstance(); // create an adapter assumed to exist in tests
        // at jmri.jmrix.ecos.networkdriver.configurexml.ConnectionConfigXml.extendElement(ConnectionConfigXml.java:40)
        // configxml.extend(e) can't get proper EcosPreferences, but some prefs is running (must be closed by memo in tearDown()
    }

    @AfterEach
    @Override
    public void tearDown() {
        memo.dispose();
        memo = null;
        xmlAdapter = null;
        cc = null;
        JUnitUtil.tearDown();
    }

}

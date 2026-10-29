package jmri.jmrix.dccpp.network.configurexml;

import jmri.jmrix.dccpp.network.ConnectionConfig;
import jmri.jmrix.dccpp.network.DCCppEthernetAdapter;
import jmri.util.JUnitUtil;

import org.jdom2.Element;
import org.junit.jupiter.api.*;

import javax.swing.JPanel;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ConnectionConfigXmlTest.java
 *
 * Test for the ConnectionConfigXml class
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Chad Francis Copyright (C) 2026
 */
public class ConnectionConfigXmlTest extends jmri.jmrix.configurexml.AbstractNetworkConnectionConfigXmlTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        xmlAdapter = new ConnectionConfigXml();
        cc = new ConnectionConfig();
    }

    @AfterEach
    @Override
    public void tearDown() {
        xmlAdapter = null;
        cc = null;
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.tearDown();
    }

    @Override
    protected void validateConnectionDetails(jmri.jmrix.ConnectionConfig cc, Element e) {
        super.validateConnectionDetails(cc, e);
        assertNotNull(e.getAttribute("reconnectEnabled"), "reconnectEnabled attribute should be present");
        DCCppEthernetAdapter adapter = (DCCppEthernetAdapter) cc.getAdapter();
        assertEquals(adapter.getAllowConnectionRecovery() ? "true" : "false",
                e.getAttribute("reconnectEnabled").getValue(),
                "reconnectEnabled should match adapter state");
    }

    @Test
    public void testReconnectEnabledStoredWhenTrue() {
        cc.loadDetails(new JPanel()); // initializes adapter
        DCCppEthernetAdapter adapter = (DCCppEthernetAdapter) cc.getAdapter();
        adapter.setAllowConnectionRecovery(true);
        Element e = xmlAdapter.store(cc);
        assertNotNull(e);
        assertEquals("true", e.getAttribute("reconnectEnabled").getValue());
    }

    @Test
    public void testReconnectEnabledStoredWhenFalse() {
        cc.loadDetails(new JPanel()); // initializes adapter
        DCCppEthernetAdapter adapter = (DCCppEthernetAdapter) cc.getAdapter();
        adapter.setAllowConnectionRecovery(false);
        Element e = xmlAdapter.store(cc);
        assertNotNull(e);
        assertEquals("false", e.getAttribute("reconnectEnabled").getValue());
    }

}

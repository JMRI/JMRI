package jmri.jmrit.logixng.actions.configurexml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.InstanceManager;
import jmri.LocoAddress;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.actions.ActionThrottleFunction;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.jdom2.Element;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test ActionThrottleFunctionXml: the throttle protocol.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class ActionThrottleFunctionXmlTest {

    private static ActionThrottleFunction loaded(String systemName) {
        Base object = InstanceManager.getDefault(DigitalActionManager.class).getBySystemName(systemName);
        while (object instanceof MaleSocket) {
            object = ((MaleSocket) object).getObject();
        }
        return (ActionThrottleFunction) object;
    }

    @Test
    public void testStoreAndLoadProtocol() throws JmriConfigureXmlException {
        ActionThrottleFunction a = new ActionThrottleFunction("IQDA1", null);
        a.setProtocol(LocoAddress.Protocol.MFX);
        Element e = new ActionThrottleFunctionXml().store(a);
        assertEquals( "mfx", e.getChildText("protocol"), "short name stored");

        e.getChild("systemName").setText("IQDA2");
        assertTrue( new ActionThrottleFunctionXml().load(e, null));
        assertEquals( LocoAddress.Protocol.MFX, loaded("IQDA2").getProtocol(), "protocol loaded");
    }

    @Test
    public void testNoProtocolNotStored() throws JmriConfigureXmlException {
        ActionThrottleFunction a = new ActionThrottleFunction("IQDA1", null);
        Element e = new ActionThrottleFunctionXml().store(a);
        assertNull( e.getChild("protocol"), "nothing stored for the connection's default");

        e.getChild("systemName").setText("IQDA2");
        assertTrue( new ActionThrottleFunctionXml().load(e, null));
        assertNull( loaded("IQDA2").getProtocol(), "file without a protocol loads as before");
    }

    @Test
    public void testUnknownProtocol() throws JmriConfigureXmlException {
        ActionThrottleFunction a = new ActionThrottleFunction("IQDA1", null);
        Element e = new ActionThrottleFunctionXml().store(a);
        e.getChild("systemName").setText("IQDA2");
        e.addContent(new Element("protocol").addContent("nosuch"));

        assertTrue( new ActionThrottleFunctionXml().load(e, null));
        JUnitAppender.assertWarnMessage("Unknown throttle protocol \"nosuch\" in IQDA2; the connection's default is used");
        assertNull( loaded("IQDA2").getProtocol());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initLogixNGManager();
    }

    @AfterEach
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}

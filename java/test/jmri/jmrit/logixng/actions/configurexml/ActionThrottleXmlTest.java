package jmri.jmrit.logixng.actions.configurexml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.InstanceManager;
import jmri.LocoAddress;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.actions.ActionThrottle;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.jdom2.Element;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test ActionThrottleXml: the throttle protocol.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class ActionThrottleXmlTest {

    private static ActionThrottle loaded(String systemName) {
        Base object = InstanceManager.getDefault(DigitalActionManager.class).getBySystemName(systemName);
        while (object instanceof MaleSocket) {
            object = ((MaleSocket) object).getObject();
        }
        return (ActionThrottle) object;
    }

    @Test
    public void testStoreAndLoadProtocol() {
        ActionThrottle a = new ActionThrottle("IQDA1", null);
        a.setProtocol(LocoAddress.Protocol.MOTOROLA);
        Element e = new ActionThrottleXml().store(a);
        assertEquals( "motorola", e.getChildText("protocol"), "short name stored");

        e.getChild("systemName").setText("IQDA2");
        assertTrue( new ActionThrottleXml().load(e, null));
        assertEquals( LocoAddress.Protocol.MOTOROLA, loaded("IQDA2").getProtocol(), "protocol loaded");
    }

    @Test
    public void testNoProtocolNotStored() {
        ActionThrottle a = new ActionThrottle("IQDA1", null);
        Element e = new ActionThrottleXml().store(a);
        assertNull( e.getChild("protocol"), "nothing stored for the connection's default");

        e.getChild("systemName").setText("IQDA2");
        assertTrue( new ActionThrottleXml().load(e, null));
        assertNull( loaded("IQDA2").getProtocol(), "file without a protocol loads as before");
    }

    @Test
    public void testUnknownProtocol() {
        ActionThrottle a = new ActionThrottle("IQDA1", null);
        Element e = new ActionThrottleXml().store(a);
        e.getChild("systemName").setText("IQDA2");
        e.addContent(new Element("protocol").addContent("nosuch"));

        assertTrue( new ActionThrottleXml().load(e, null));
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

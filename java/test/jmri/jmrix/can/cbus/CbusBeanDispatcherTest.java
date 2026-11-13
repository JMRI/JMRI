package jmri.jmrix.can.cbus;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import jmri.Light;
import jmri.Sensor;
import jmri.Turnout;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link CbusBeanDispatcher}: CBUS sensors, turnouts and lights get
 * the frames of their own addresses through one listener per traffic
 * controller.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class CbusBeanDispatcherTest {

    private TrafficControllerScaffold tcis;
    private final List<jmri.NamedBean> beans = new ArrayList<>();

    /**
     * A scaffold that notifies its listeners of each frame sent, leaving out
     * the sender, as a real traffic controller does.
     */
    private static class NotifyingScaffold extends TrafficControllerScaffold {
        @Override
        public void sendCanMessage(CanMessage m, CanListener l) {
            super.sendCanMessage(m, l);
            notifyMessage(m, l);
        }

        @Override
        public void forwardMessage(AbstractMRListener l, jmri.jmrix.AbstractMRMessage m) {
            ((CanListener) l).message((CanMessage) m);
        }
    }

    /** A listener filed by the dispatcher, counting what it is offered. */
    private static class Spy implements CanListener {
        final List<CanMessage> messages = new ArrayList<>();
        final List<CanReply> replies = new ArrayList<>();
        final List<String> order;
        final String name;

        Spy(String name, List<String> order) {
            this.name = name;
            this.order = order;
        }

        @Override
        public void message(CanMessage m) {
            messages.add(m);
        }

        @Override
        public void reply(CanReply r) {
            replies.add(r);
            order.add(name);
        }
    }

    private static CanReply reply(int... d) {
        CanReply r = new CanReply(d.length);
        for (int i = 0; i < d.length; i++) {
            r.setElement(i, d[i]);
        }
        return r;
    }

    private static CanMessage message(int... d) {
        CanMessage m = new CanMessage(d.length, 0x12);
        for (int i = 0; i < d.length; i++) {
            m.setElement(i, d[i]);
        }
        return m;
    }

    private <T extends jmri.NamedBean> T keep(T bean) {
        beans.add(bean);
        return bean;
    }

    @Test
    public void testOneListenerForAllBeans() {
        for (int i = 1; i <= 50; i++) {
            keep(new CbusSensor("M", "+N1E" + i, tcis));
            keep(new CbusTurnout("M", "+N2E" + i, tcis));
            keep(new CbusLight("M", "+N3E" + i, tcis));
        }
        assertEquals(1, tcis.numListeners(), "one listener for 150 beans");
        CbusBeanDispatcher d = CbusBeanDispatcher.forTrafficController(tcis);
        assertNotNull(d);
        assertEquals(150, d.numBeans());
    }

    @Test
    public void testFramesReachTheirBeansOnly() {
        CbusSensor s5 = keep(new CbusSensor("M", "+N1E5", tcis));
        CbusSensor s6 = keep(new CbusSensor("M", "+N1E6", tcis));
        CbusSensor shortS = keep(new CbusSensor("M", "+7", tcis));
        CbusSensor rawS = keep(new CbusSensor("M", "X2301;X2302", tcis));
        CbusTurnout t5 = keep(new CbusTurnout("M", "+N1E5", tcis));
        CbusLight l9 = keep(new CbusLight("M", "+N1E9", tcis));

        tcis.sendToListeners(reply(CbusConstants.CBUS_ACON, 0, 1, 0, 5));
        assertEquals(Sensor.ACTIVE, s5.getKnownState());
        assertEquals(Sensor.UNKNOWN, s6.getKnownState());
        assertEquals(Turnout.THROWN, t5.getCommandedState());

        // a short event, from any node
        tcis.sendToListeners(reply(CbusConstants.CBUS_ASON, 0, 99, 0, 7));
        assertEquals(Sensor.ACTIVE, shortS.getKnownState());

        // a response event counts as the event
        tcis.sendToListeners(reply(CbusConstants.CBUS_AROF, 0, 1, 0, 5));
        assertEquals(Sensor.INACTIVE, s5.getKnownState());
        assertEquals(Turnout.CLOSED, t5.getCommandedState());

        // a raw frame address
        tcis.sendToListeners(reply(0x23, 0x02));
        assertEquals(Sensor.INACTIVE, rawS.getKnownState());

        // an outgoing frame from elsewhere
        tcis.sendToListeners(message(CbusConstants.CBUS_ACON, 0, 1, 0, 9));
        assertEquals(Light.ON, l9.getState());

        // a request to the turnout's thrown event is answered
        tcis.outbound.clear();
        tcis.sendToListeners(reply(CbusConstants.CBUS_AREQ, 0, 1, 0, 5));
        assertEquals(1, tcis.outbound.size(), "turnout answers the request");
        assertEquals(CbusConstants.CBUS_AROF, tcis.outbound.get(0).getElement(0));
        assertEquals(Sensor.UNKNOWN, s6.getKnownState(), "never touched");
    }

    /**
     * The dispatcher relies on this: whenever an address accepts a frame, as
     * an event, a response event or a request, the frame has the address's
     * key. Checked over many random frames and addresses, so a bean is never
     * left out.
     */
    @Test
    public void testEveryMatchingFrameHasTheAddressKey() {
        Random r = new Random(15715);
        int[] opcodes = {CbusConstants.CBUS_ACON, CbusConstants.CBUS_ACOF, CbusConstants.CBUS_ASON,
            CbusConstants.CBUS_ASOF, CbusConstants.CBUS_ARON, CbusConstants.CBUS_AROF, CbusConstants.CBUS_ARSON,
            CbusConstants.CBUS_ARSOF, CbusConstants.CBUS_AREQ, CbusConstants.CBUS_ASRQ, 0x23, 0x47, 0xB0};
        String[] forms = {"+N%dE%d", "-N%dE%d", "+%2$d", "-%2$d"};
        int matches = 0;
        for (int i = 0; i < 12000; i++) {
            CbusAddress a;
            if (r.nextInt(5) == 0) {
                // raw frame address, 1 to 5 bytes
                int len = 1 + r.nextInt(5);
                StringBuilder sb = new StringBuilder("X");
                for (int j = 0; j < len; j++) {
                    sb.append(String.format("%02X", j == 0 ? opcodes[r.nextInt(opcodes.length)] : r.nextInt(4)));
                }
                a = new CbusAddress(sb.toString());
            } else {
                a = new CbusAddress(String.format(forms[r.nextInt(forms.length)], 1 + r.nextInt(3), 1 + r.nextInt(3)));
            }
            for (int j = 0; j < 40; j++) {
                int len = r.nextInt(9);
                int[] d = new int[len];
                for (int k = 0; k < len; k++) {
                    d[k] = k == 0 ? opcodes[r.nextInt(opcodes.length)] : r.nextInt(4);
                }
                CanReply f = reply(d);
                CanReply converted = CbusMessage.opcRangeToStl(f);
                if (a.match(f) || a.match(converted) || a.matchRequest(f)) {
                    matches++;
                    assertEquals(a.dispatchKey(), CbusBeanDispatcher.key(f), a + " accepts " + f);
                }
            }
        }
        assertTrue(matches > 1000, "enough matches to mean something: " + matches);
    }

    @Test
    public void testSenderDoesNotHearItsOwnFrame() {
        NotifyingScaffold tc = new NotifyingScaffold();
        List<String> order = new ArrayList<>();
        Spy a = new Spy("a", order);
        Spy b = new Spy("b", order);
        CbusAddress addr = new CbusAddress("+N1E5");
        CbusBeanDispatcher.add(tc, a, addr);
        CbusBeanDispatcher.add(tc, b, addr);

        CanMessage m = addr.makeMessage(tc.getCanid());
        CbusBeanDispatcher.send(tc, m, a);
        assertEquals(0, a.messages.size(), "sender left out");
        assertEquals(1, b.messages.size(), "other bean hears it");

        // the same frame sent by someone else reaches both
        tc.sendCanMessage(m, null);
        assertEquals(1, a.messages.size());
        assertEquals(2, b.messages.size());

        CbusBeanDispatcher.remove(tc, a);
        CbusBeanDispatcher.remove(tc, b);
        assertEquals(0, tc.numListeners());
        tc.terminateThreads();
    }

    @Test
    public void testBeansInTheOrderAdded() {
        List<String> order = new ArrayList<>();
        CbusAddress addr = new CbusAddress("+N1E5");
        Spy a = new Spy("a", order);
        Spy b = new Spy("b", order);
        Spy c = new Spy("c", order);
        CbusBeanDispatcher.add(tcis, b, addr);
        CbusBeanDispatcher.add(tcis, a, addr);
        CbusBeanDispatcher.add(tcis, c, new CbusAddress("+N1E6"));
        CbusBeanDispatcher.add(tcis, b, addr); // added once only
        tcis.sendToListeners(reply(CbusConstants.CBUS_ACON, 0, 1, 0, 5));
        assertEquals(List.of("b", "a"), order);
        CbusBeanDispatcher.remove(tcis, a);
        CbusBeanDispatcher.remove(tcis, b);
        CbusBeanDispatcher.remove(tcis, c);
    }

    @Test
    public void testDisposeRemovesTheListener() {
        CbusSensor s = new CbusSensor("M", "+N1E5", tcis);
        CbusLight l = new CbusLight("M", "+N1E6", tcis);
        assertEquals(1, tcis.numListeners());
        s.dispose();
        assertEquals(1, tcis.numListeners(), "still a bean");
        l.dispose();
        assertEquals(0, tcis.numListeners(), "no beans, no listener");
        CbusSensor again = keep(new CbusSensor("M", "+N1E5", tcis));
        assertEquals(1, tcis.numListeners());
        tcis.sendToListeners(reply(CbusConstants.CBUS_ACON, 0, 1, 0, 5));
        assertEquals(Sensor.ACTIVE, again.getKnownState());
    }

    @Test
    public void testTwoConnectionsKeptApart() {
        TrafficControllerScaffold tc2 = new TrafficControllerScaffold();
        CbusSensor s1 = keep(new CbusSensor("M", "+N1E5", tcis));
        CbusSensor s2 = new CbusSensor("N", "+N1E5", tc2);
        assertEquals(1, tcis.numListeners());
        assertEquals(1, tc2.numListeners());
        tc2.sendToListeners(reply(CbusConstants.CBUS_ACON, 0, 1, 0, 5));
        assertEquals(Sensor.ACTIVE, s2.getKnownState());
        assertEquals(Sensor.UNKNOWN, s1.getKnownState());
        s2.dispose();
        tc2.terminateThreads();
    }

    @Test
    public void testFailingBeanDoesNotStopTheOthers() {
        CbusAddress addr = new CbusAddress("+N1E5");
        CanListener failing = new CanListener() {
            @Override
            public void message(CanMessage m) {
                throw new IllegalStateException("message failed");
            }

            @Override
            public void reply(CanReply r) {
                throw new IllegalStateException("reply failed");
            }
        };
        CbusBeanDispatcher.add(tcis, failing, addr);
        CbusSensor s = keep(new CbusSensor("M", "+N1E5", tcis));
        tcis.sendToListeners(reply(CbusConstants.CBUS_ACON, 0, 1, 0, 5));
        JUnitAppender.assertWarnMessageStartsWith("During reply dispatch to");
        assertEquals(Sensor.ACTIVE, s.getKnownState());
        tcis.sendToListeners(message(CbusConstants.CBUS_ACOF, 0, 1, 0, 5));
        JUnitAppender.assertWarnMessageStartsWith("During message dispatch to");
        assertEquals(Sensor.INACTIVE, s.getKnownState());
        CbusBeanDispatcher.remove(tcis, failing);
    }

    @Test
    public void testExtendedAndRtrFramesIgnored() {
        CbusSensor s = keep(new CbusSensor("M", "+N1E5", tcis));
        CanReply ext = reply(CbusConstants.CBUS_ACON, 0, 1, 0, 5);
        ext.setExtended(true);
        tcis.sendToListeners(ext);
        CanReply rtr = reply(CbusConstants.CBUS_ACON, 0, 1, 0, 5);
        rtr.setRtr(true);
        tcis.sendToListeners(rtr);
        assertEquals(Sensor.UNKNOWN, s.getKnownState());
    }

    @Test
    public void testNoKeyForUnparsedAddress() {
        assertEquals(CbusBeanDispatcher.NO_KEY, CbusBeanDispatcher.key((int[]) null));
        assertEquals(CbusBeanDispatcher.NO_KEY, new CbusAddress("not an address").dispatchKey());
        List<String> order = new ArrayList<>();
        Spy spy = new Spy("x", order);
        CbusBeanDispatcher.add(tcis, spy, new CbusAddress("not an address"));
        assertEquals(0, tcis.numListeners(), "a bean that can match nothing is not filed");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        tcis = new TrafficControllerScaffold();
    }

    @AfterEach
    public void tearDown() {
        for (jmri.NamedBean b : beans) {
            b.dispose();
        }
        beans.clear();
        for (AbstractMRListener l : new ArrayList<>(tcis.getListeners())) {
            fail("listener left: " + l);
        }
        tcis.terminateThreads();
        tcis = null;
        JUnitUtil.tearDown();
    }

}

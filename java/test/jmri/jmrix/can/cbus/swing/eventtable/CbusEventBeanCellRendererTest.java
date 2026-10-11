package jmri.jmrix.can.cbus.swing.eventtable;

import java.awt.Component;
import java.util.HashSet;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

import jmri.Light;
import jmri.Sensor;
import jmri.Turnout;
import jmri.NamedBean;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusLightManager;
import jmri.jmrix.can.cbus.CbusSensorManager;
import jmri.jmrix.can.cbus.CbusTurnoutManager;
import jmri.jmrix.can.cbus.eventtable.CbusEventBeanData;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test simple functioning of CbusEventBeanCellRenderer
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2020
*/
@jmri.util.junit.annotations.DisabledIfHeadless
public class CbusEventBeanCellRendererTest  {

    @Test
    public void testInitComponents() {
        // for now, just makes sure there isn't an exception.
        assertNotNull(new CbusEventBeanCellRenderer(new JTextField(),20));
    }

    @Test
    public void testEmptyRenderer() {

        JTable table = new JTable();
        JTextField filterT = new JTextField();

        CbusEventBeanCellRenderer t = new CbusEventBeanCellRenderer(filterT,20);
        assertNotNull(t);

        Component c = t.getTableCellRendererComponent(table, null, true, true, 0, 0);
        assertNotNull(c);
        assertInstanceOf(JPanel.class, c);
        assertEquals(0,((JPanel) c).getComponentCount());

        CbusEventBeanData evDat = new CbusEventBeanData(new HashSet<>(0),new HashSet<>(0));
        c = t.getTableCellRendererComponent(table, evDat, false, false, 1, 1);
        assertNotNull(c);
        assertInstanceOf(JPanel.class, c);
        assertEquals(0,((JPanel) c).getComponentCount());

    }

    @Test
    public void testRendererWithBeans() {
        CbusLightManager lm = new CbusLightManager(memo);
        CbusSensorManager sm = new CbusSensorManager(memo);
        CbusTurnoutManager tm = new CbusTurnoutManager(memo);

        HashSet<NamedBean> hsA = new HashSet<>();
        HashSet<NamedBean> hsB = new HashSet<>();

        Light lightA = lm.provideLight("+17");
        lightA.setUserName("MyLightA");

        Light lightB = lm.provideLight("+18");
        lightB.setUserName("MyLightB");

        hsA.add(lightA);
        hsB.add(lightB);

        CbusEventBeanData evDat = new CbusEventBeanData(hsA,hsB);

        JTable table = new JTable();
        JTextField filterT = new JTextField();

        CbusEventBeanCellRenderer t = new CbusEventBeanCellRenderer(filterT,20);

        Component c = t.getTableCellRendererComponent(table, evDat, false, false, 1, 1);

        assertEquals(2,((JPanel) c).getComponentCount());

        JTextField d = (JTextField) ((JPanel) c).getComponent(0);
        assertNotNull(d);

        assertTrue(d.getText().contains("Light On: MyLightA"));
        assertTrue(d.getText().contains("Light Off: MyLightB"));

        assertEquals(0,(d.getComponentCount()));

        JPanel dd = (JPanel) ((JPanel) c).getComponent(1);
        assertNotNull(dd);
        assertEquals(2,(dd.getComponentCount()));

        Sensor sa = sm.provide("+20");
        sa.setUserName("MySensorA");
        Sensor sb = sm.provide("+21");
        sb.setUserName("MySensorB");
        hsA.add(sa);
        hsB.add(sb);

        evDat = new CbusEventBeanData(hsA,hsB);
        c = t.getTableCellRendererComponent(table, evDat, false, false, 2, 2);
        assertEquals(2,((JPanel) c).getComponentCount());

        d = (JTextField) ((JPanel) c).getComponent(0);
        assertTrue(d.getText().contains("Light On: MyLightA"));
        assertTrue(d.getText().contains("Light Off: MyLightB"));
        assertTrue(d.getText().contains("Sensor Active: MySensorA"));
        assertTrue(d.getText().contains("Sensor Inactive: MySensorB"));

        dd = (JPanel) ((JPanel) c).getComponent(1);
        assertNotNull(dd);
        assertEquals(4,(dd.getComponentCount()));


        Turnout ta = tm.provide("+30");
        ta.setUserName("MyTurnoutA");
        Turnout tb = tm.provide("+31");
        tb.setUserName("MyTurnoutB");
        hsA.add(ta);
        hsB.add(tb);

        evDat = new CbusEventBeanData(hsA,hsB);
        c = t.getTableCellRendererComponent(table, evDat, false, false, 2, 2);
        assertEquals(2,((JPanel) c).getComponentCount());

        d = (JTextField) ((JPanel) c).getComponent(0);
        assertTrue(d.getText().contains("Light On: MyLightA"));
        assertTrue(d.getText().contains("Light Off: MyLightB"));
        assertTrue(d.getText().contains("Sensor Active: MySensorA"));
        assertTrue(d.getText().contains("Sensor Inactive: MySensorB"));
        assertTrue(d.getText().contains("Turnout Closed: MyTurnoutA"));
        assertTrue(d.getText().contains("Turnout Thrown: MyTurnoutB"));



        dd = (JPanel) ((JPanel) c).getComponent(1);
        assertNotNull(dd);
        assertEquals(6,(dd.getComponentCount()));



        sm.dispose();
        lm.dispose();
        tm.dispose();
    }

    private CanSystemConnectionMemo memo = null;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new CanSystemConnectionMemo();
    }

    @AfterEach
    public void tearDown() {
        assertNotNull(memo);
        memo.dispose();
        memo = null;
        JUnitUtil.tearDown();
    }

}

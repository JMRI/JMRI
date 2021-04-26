package jmri.jmrix.can.cbus.swing.eventtable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.Test;

/**
 * Test simple functioning of CbusEventBeanCellRenderer
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2020
*/
public class CbusEventBeanCellRendererTest  {

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testInitComponents() throws Exception{
        // for now, just makes sure there isn't an exception.
        assertThat(new CbusEventBeanCellRenderer(null,20)).isNotNull();
    }
    
    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testEmptyRenderer() {
        
        JTable table = new JTable();
        JTextField filterT = new JTextField();
        
        CbusEventBeanCellRenderer t = new CbusEventBeanCellRenderer(filterT,20);
        assertThat(t).isNotNull();
        
        Component c = t.getTableCellRendererComponent(table, null, true, true, 0, 0);
        assertThat(c).isNotNull();
        assertThat( c instanceof JPanel ).isTrue();
        assertEquals(0,((JPanel) c).getComponentCount());
        
        CbusEventBeanData evDat = new CbusEventBeanData(new HashSet<>(0),new HashSet<>(0));
        c = t.getTableCellRendererComponent(table, evDat, false, false, 1, 1);
        assertThat(c).isNotNull();
        assertThat( c instanceof JPanel ).isTrue();
        assertEquals(0,((JPanel) c).getComponentCount());
        
    }
    
    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
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
        assertThat(d).isNotNull();
        
        assertThat(d.getText().contains("Light On: MyLightA")).isTrue();
        assertThat(d.getText().contains("Light Off: MyLightB")).isTrue();
        
        assertEquals(0,(d.getComponentCount()));
        
        JPanel dd = (JPanel) ((JPanel) c).getComponent(1);
        assertThat(dd).isNotNull();        
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
        assertThat(d.getText().contains("Light On: MyLightA")).isTrue();
        assertThat(d.getText().contains("Light Off: MyLightB")).isTrue();
        assertThat(d.getText().contains("Sensor Active: MySensorA")).isTrue();
        assertThat(d.getText().contains("Sensor Inactive: MySensorB")).isTrue();
        
        dd = (JPanel) ((JPanel) c).getComponent(1);
        assertThat(dd).isNotNull();        
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
        assertThat(d.getText().contains("Light On: MyLightA")).isTrue();
        assertThat(d.getText().contains("Light Off: MyLightB")).isTrue();
        assertThat(d.getText().contains("Sensor Active: MySensorA")).isTrue();
        assertThat(d.getText().contains("Sensor Inactive: MySensorB")).isTrue();
        assertThat(d.getText().contains("Turnout Closed: MyTurnoutA")).isTrue();
        assertThat(d.getText().contains("Turnout Thrown: MyTurnoutB")).isTrue();
        
        
        
        dd = (JPanel) ((JPanel) c).getComponent(1);
        assertThat(dd).isNotNull();        
        assertEquals(6,(dd.getComponentCount()));
        
        
        
        
        sm.dispose();
        lm.dispose();
        tm.dispose();
    }
    
    
    private CanSystemConnectionMemo memo;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new CanSystemConnectionMemo();
    }

    @AfterEach
    public void tearDown() {
        memo.dispose();
        memo = null;
        JUnitUtil.tearDown();
    }

}

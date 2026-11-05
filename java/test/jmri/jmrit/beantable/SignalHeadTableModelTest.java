package jmri.jmrit.beantable;

import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.SignalHeadManager;
import jmri.Turnout;
import jmri.TurnoutManager;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Unit tests for SignalHeadTableModel.
 * @author Steve Young Copyright (C) 2023
 */
public class SignalHeadTableModelTest extends AbstractBeanTableDataModelBase<SignalHead> {

    @Override
    protected SignalHead createBean(){
        SignalHead s = new jmri.implementation.VirtualSignalHead("IH1", "My Head UserName");
        InstanceManager.getDefault(SignalHeadManager.class).register(s);
        return s;
    }

    @Override
    protected int getModelColumnCount() {
        return 8;
    }

    @Test
    @Override
    public void testGetBaseColumnNames(){
        Assertions.assertEquals(Bundle.getMessage("ColumnSystemName"), t.getColumnName(0));
        Assertions.assertEquals(Bundle.getMessage("ColumnUserName"), t.getColumnName(1));
        Assertions.assertEquals(Bundle.getMessage("SignalMastAppearance"), t.getColumnName(2));
        Assertions.assertEquals(Bundle.getMessage("ColumnComment"), t.getColumnName(3));
        Assertions.assertEquals("", t.getColumnName(4));
        Assertions.assertEquals(Bundle.getMessage("ColumnHeadLit"), t.getColumnName(5));
        Assertions.assertEquals(Bundle.getMessage("ColumnHeadHeld"), t.getColumnName(6));
        Assertions.assertEquals("", t.getColumnName(7));
    }

    @Test
    public void testDeleteClearsOutputComments() {
        SignalHead h = createBean();
        TurnoutManager tm = InstanceManager.getDefault(TurnoutManager.class);
        Turnout a = tm.provideTurnout("IT1");
        Turnout b = tm.provideTurnout("IT2");
        Turnout c = tm.provideTurnout("IT3");
        Turnout d = tm.provideTurnout("IT4");
        a.setComment("SignalHead:IH1:Bit0");
        b.setComment(Bundle.getMessage("BeanNameSignalHead") + ":IH1:" + Bundle.getMessage("InputNum", "3"));
        c.setComment("SignalHead:IH10:Bit0");
        d.setComment("my own note");

        ((SignalHeadTableModel) t).doDelete(h);

        Assertions.assertNull(InstanceManager.getDefault(SignalHeadManager.class).getBySystemName("IH1"), "head gone");
        Assertions.assertEquals(a, tm.getBySystemName("IT1"), "output still in the Turnout table");
        Assertions.assertNull(a.getComment(), "head comment cleared");
        Assertions.assertNull(b.getComment(), "SD2 input comment cleared");
        Assertions.assertEquals("SignalHead:IH10:Bit0", c.getComment(), "IH10 kept");
        Assertions.assertEquals("my own note", d.getComment(), "user comment kept");
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSignalHeadManager();
        t = new SignalHeadTableModel();
        Assertions.assertNotNull(t);
    }

    @Override
    @AfterEach
    public void tearDown() {
        if (t!=null){
            t.dispose();
        }
        t = null;
        JUnitUtil.tearDown();
    }

}

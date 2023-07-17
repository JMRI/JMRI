package jmri.jmrit.beantable;

import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.SignalHeadManager;

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

package jmri.jmrit.beantable.signalmast;

import jmri.*;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SignalMastTableDataModelTest extends jmri.jmrit.beantable.AbstractBeanTableDataModelBase<SignalMast>{
    
    @Test
    public void testCTor() {
        assertNotNull(t, "exists");
    }
    
    @Override
    public int getModelColumnCount(){
        return 9;
    }
    
    @Override
    protected SignalMast createBean(){
        
        Manager<SignalMast> mgr = InstanceManager.getDefault(jmri.SignalMastManager.class);
        assertNotNull(mgr, "Table Bean Manager exists");

        SignalMast b = ((ProvidingManager<SignalMast>) mgr).provide(mgr.getSystemNamePrefix()+"$vsm:basic:one-low($0001)");
        assertNotNull(b, "Bean created");
        return b;
    }

    @Test
    @Override
    public void testGetBaseColumnNames() {
        assertEquals(Bundle.getMessage("ColumnSystemName"), t.getColumnName(0), "Column0 - Bean toString");
        assertEquals(Bundle.getMessage("ColumnUserName"), t.getColumnName(1), "Column1 - UserName");
        assertEquals(Bundle.getMessage("LabelAspectType"), t.getColumnName(2), "Column2 - Bean value");
        assertEquals(Bundle.getMessage("ColumnComment"), t.getColumnName(3), "Column3 - User Comment");
        assertEquals("", t.getColumnName(4), "Column4 - Delete button");
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        t = new SignalMastTableDataModel();
    }

    @AfterEach
    @Override
    public void tearDown() {
        if (t!=null){
            t.dispose();
        }
        t = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SignalMastTableDataModelTest.class);

}

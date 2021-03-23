package jmri.jmrit.beantable.signalmast;

import jmri.*;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SignalMastTableDataModelTest extends jmri.jmrit.beantable.AbstractBeanTableDataModelBase<SignalMast>{
    
    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",t);
    }
    
    @Override
    public int getModelColumnCount(){
        return 9;
    }
    
    @Override
    protected void createBean(){
        
        Manager<?> mgr = InstanceManager.getDefault(jmri.SignalMastManager.class);
        Assert.assertNotNull("Table Bean Manager exists",mgr);
        if (mgr instanceof ProvidingManager){
            NamedBean b = ((ProvidingManager<?>) mgr).provide(mgr.getSystemNamePrefix()+"$vsm:basic:one-low($0001)");
            Assert.assertNotNull("Bean created",b);
        } else {
            Assert.fail("Manager is not a providing manager, this test should be overridden to create a bean");
        }
    }
    
    @Test
    @Override
    public void testGetBaseColumnNames() {
        assertEquals("Column0 - Bean toString",Bundle.getMessage("ColumnSystemName"), t.getColumnName(0));
        assertEquals("Column1 - UserName",Bundle.getMessage("ColumnUserName"), t.getColumnName(1));
        assertEquals("Column2 - Bean value",Bundle.getMessage("LabelAspectType"), t.getColumnName(2));
        assertEquals("Column3 - User Comment",Bundle.getMessage("ColumnComment"), t.getColumnName(3));
        assertEquals("Column4 - Delete button","", t.getColumnName(4));
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

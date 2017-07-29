package jmri.jmrit.beantable;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.netbeans.jemmy.operators.JFrameOperator;
import javax.swing.JFrame;
import java.awt.GraphicsEnvironment;


/**
 * This is an abstract base class for testing bean table action objects
 * derived from AbstractTableAction.  This contains a base level of testing
 * for these objects.  Derived classes should set the "a" variable in their 
 * setUp method.
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public abstract class AbstractTableActionBase {

    protected AbstractTableAction a = null;

    @Test
    public void testGetTableDataModel(){
       Assert.assertNotNull("Table Data Model Exists",a.getTableDataModel());
    }

    @Test
    public void testExecute() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        a.actionPerformed(null);
        JFrame f = JFrameOperator.waitJFrame(getTableFrameName(), true, true);
        Assert.assertNotNull("failed to find frame", f);
        f.dispose();
    }

    /**
     * @return the table name used to create the window, as returned from the 
     * Bundle.
     */
    abstract String getTableFrameName();

    /**
     * Check the return value of getPanel.  If the class under test provides a
     * panel, its test implementation needs to override this method.
     */
    @Test
    public void testGetPanel(){
         Assert.assertNull("Default getPanel returns null",a.getPanel());
    }

    /**
     * Check the return value of getDescription.  If the class under test 
     * provides string descriptor, its test implementation needs to 
     * override this method.
     */
    @Test
    public void testGetClassDescription(){
         Assert.assertEquals("Default class description","Abstract Table Action",a.getClassDescription());
    }

    /**
     * Check the return value of includeAddButton.  If the class under test 
     * includes an add button, its test implementation needs to 
     * override this method.
     */
    @Test
    public void testIncludeAddButton(){
         Assert.assertFalse("Default include add button",a.includeAddButton());
    }
    

    /** 
     * Derived classes should use this method to set a.
     */
    @Before
    abstract public void setUp();

    /** 
     * Derived classes should use this method to clean up after tests.
     */
    @After
    abstract public void tearDown();

    // private final static Logger log = LoggerFactory.getLogger(AbstractTableActionBase.class.getName());

}

package jmri.jmrit.beantable;

import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * This is an abstract base class for testing bean table action objects derived
 * from AbstractTableAction. This contains a base level of testing for these
 * objects. Derived classes should set the "a" variable in their setUp method.
 *
 * @author Paul Bender Copyright (C) 2017
 */
public abstract class AbstractTableActionBase {

    protected AbstractTableAction a = null;
    protected String helpTarget = "index"; // index is default value specified in AbstractTableAction.

    @Test
    public void testGetTableDataModel() {
        Assert.assertNotNull("Table Data Model Exists", a.getTableDataModel());
    }

    @Test
    public void testExecute() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        a.actionPerformed(null);
        JFrame f = JFrameOperator.waitJFrame(getTableFrameName(), true, true);
        Assert.assertNotNull("failed to find frame", f);
        JUnitUtil.dispose(f);
    }

    /**
     * @return the table name used to create the window, as returned from the
     *         Bundle.
     */
    abstract public String getTableFrameName();

    /**
     * Check the return value of getPanel. If the class under test provides a
     * panel, its test implementation needs to override this method.
     */
    @Test
    public void testGetPanel() {
        Assert.assertNull("Default getPanel returns null", a.getPanel());
    }

    /**
     * Check the return value of getDescription. If the class under test
     * provides string descriptor, its test implementation needs to override
     * this method.
     */
    @Test
    public void testGetClassDescription() {
        Assert.assertEquals("Default class description", "Abstract Table Action", a.getClassDescription());
    }

    /**
     * Check the return value of includeAddButton. If the class under test
     * includes an add button, its test implementation needs to override this
     * method.
     */
    @Test
    public void testIncludeAddButton() {
        Assert.assertFalse("Default include add button", a.includeAddButton());
    }

    @Test
    public void testHelpTarget(){
        Assert.assertEquals("help target",helpTarget,a.helpTarget());
    }

    @Test
    public void testAddButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assume.assumeTrue(a.includeAddButton());
        a.actionPerformed(null);
        JFrame f = JFrameOperator.waitJFrame(getTableFrameName(), true, true);

        // find the "Add... " button and press it.
	jmri.util.swing.JemmyUtil.pressButton(new JFrameOperator(f),Bundle.getMessage("ButtonAdd"));
        JFrame f1 = JFrameOperator.waitJFrame(getAddFrameName(), true, true);
	jmri.util.swing.JemmyUtil.pressButton(new JFrameOperator(f1),Bundle.getMessage("ButtonCancel"));
        JUnitUtil.dispose(f1);
        JUnitUtil.dispose(f);
    }

    /**
     * @return the name of the frame resulting from add being pressed, as 
     *         returned from the Bundle.
     */
    abstract public String getAddFrameName();


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

    // private final static Logger log = LoggerFactory.getLogger(AbstractTableActionBase.class);
}

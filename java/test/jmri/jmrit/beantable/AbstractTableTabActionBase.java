package jmri.jmrit.beantable;

import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.*;
import org.junit.*;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * This is an abstract base class for testing bean table action objects derived
 * from AbstractTableTabAction. This contains a base level of testing for these
 * objects. Derived classes should set the "a" variable in their setUp method.
 *
 * @author Paul Bender Copyright (C) 2017
 */
public abstract class AbstractTableTabActionBase {

    protected AbstractTableTabAction a = null;
    protected String helpTarget = "index"; // index is default value specified in AbstractTableTabAction.

    @Test
    @Ignore("test causes an NPE while executing a.actionPerformed")
    @ToDo("The underlying class under test inherits the actionPerformed method from AbstractTableAction, which expects a model to be set by createModel(), which doesn't happen for AbstractTableTabAction classes")
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
     * Check the return value of getPanel. If the class under test does not
     * provide a panel, its test implementation needs to override this method.
     */
    @Test
    public void testGetPanel() {
        Assert.assertNotNull("Default getPanel does not return null", a.getPanel());
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

    // private final static Logger log = LoggerFactory.getLogger(AbstractTableTabActionBase.class);
}

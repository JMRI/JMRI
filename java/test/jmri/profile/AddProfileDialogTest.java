package jmri.profile;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.lang.reflect.Field;
import javax.swing.JTextField;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class AddProfileDialogTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        jmri.util.JmriJFrame jf = new jmri.util.JmriJFrame();
        AddProfileDialog t = new AddProfileDialog(jf,false,false);
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(t);
        JUnitUtil.dispose(jf);
    }

    @Test
    public void testTextFields() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        jmri.util.JmriJFrame jf = new jmri.util.JmriJFrame();
        AddProfileDialog t = new AddProfileDialog(jf,false,false);
        Field field = t.getClass().getDeclaredField("profileName");
        field.setAccessible(true);
        JTextField jtfName = (JTextField) field.get(t);
        field = t.getClass().getDeclaredField("profileLocation");
        field.setAccessible(true);
        JTextField jtfLocation = (JTextField) field.get(t);
        field = t.getClass().getDeclaredField("profileFolder");
        field.setAccessible(true);
        JTextField jtfFolder = (JTextField) field.get(t);
        // test default values
        Assert.assertTrue("Name is empty", jtfName.getText().isEmpty());
        Assert.assertEquals("Location and Folder are the same", jtfLocation.getText(), jtfFolder.getText());
        // test name set to "test"
        jtfName.setText("test");
        Assert.assertEquals("Folder name has .jmri extension",
                new File(jtfLocation.getText(), "test" + Profile.EXTENSION).getPath(),
                jtfFolder.getText());
        // test name erased
        jtfName.setText("");
        Assert.assertTrue("Name is empty", jtfName.getText().isEmpty());
        Assert.assertEquals("Location and Folder are the same", jtfLocation.getText(), jtfFolder.getText());
        // test name set to "test2"
        jtfName.setText("test2");
        Assert.assertEquals("Folder name has .jmri extension",
                new File(jtfLocation.getText(), "test2" + Profile.EXTENSION).getPath(),
                jtfFolder.getText());
        // test name set back to "test"
        jtfName.setText("test");
        Assert.assertEquals("Folder name has .jmri extension",
                new File(jtfLocation.getText(), "test" + Profile.EXTENSION).getPath(),
                jtfFolder.getText());
        JUnitUtil.dispose(t);
        JUnitUtil.dispose(jf);
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(AddProfileDialogTest.class);

}

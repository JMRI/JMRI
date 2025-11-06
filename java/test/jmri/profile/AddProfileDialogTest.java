package jmri.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.lang.reflect.Field;

import javax.swing.JTextField;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
@DisabledIfHeadless
public class AddProfileDialogTest {

    @Test
    public void testCTor() {
        jmri.util.JmriJFrame jf = new jmri.util.JmriJFrame();
        AddProfileDialog t = new AddProfileDialog(jf,false,false);
        assertNotNull(t, "exists");
        JUnitUtil.dispose(t);
        JUnitUtil.dispose(jf);
    }

    @Test
    public void testTextFields() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        jmri.util.JmriJFrame jf = new jmri.util.JmriJFrame();
        AddProfileDialog t = new AddProfileDialog(jf,false,false);
        Field field = t.getClass().getDeclaredField("profileName");
        field.setAccessible(true);
        JTextField jtfName = (JTextField) field.get(t);
        Assertions.assertNotNull(jtfName);
        field = t.getClass().getDeclaredField("profileLocation");
        field.setAccessible(true);
        JTextField jtfLocation = (JTextField) field.get(t);
        field = t.getClass().getDeclaredField("profileFolder");
        field.setAccessible(true);
        JTextField jtfFolder = (JTextField) field.get(t);
        // test default values
        assertTrue( jtfName.getText().isEmpty(), "Name is empty");
        assertEquals( jtfLocation.getText(), jtfFolder.getText(),
            "Location and Folder are the same");
        // test name set to "test"
        jtfName.setText("test");
        assertEquals( new File(jtfLocation.getText(), "test" + Profile.EXTENSION).getPath(),
            jtfFolder.getText(), "Folder name has .jmri extension");
        // test name erased
        jtfName.setText("");
        assertTrue( jtfName.getText().isEmpty(), "Name is empty");
        assertEquals( jtfLocation.getText(), jtfFolder.getText(),
            "Location and Folder are the same");
        // test name set to "test2"
        jtfName.setText("test2");
        assertEquals( new File(jtfLocation.getText(), "test2" + Profile.EXTENSION).getPath(),
            jtfFolder.getText(), "Folder name has .jmri extension");
        // test name set back to "test"
        jtfName.setText("test");
        assertEquals( new File(jtfLocation.getText(), "test" + Profile.EXTENSION).getPath(),
            jtfFolder.getText(), "Folder name has .jmri extension");
        JUnitUtil.dispose(t);
        JUnitUtil.dispose(jf);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(AddProfileDialogTest.class);

}

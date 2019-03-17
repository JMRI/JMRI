package jmri.jmrit.display.controlPanelEditor.shape;

import java.io.File;
import jmri.configurexml.SchemaTestBase;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Checks of JMRI XML Schema
 *
 * @author Bob Jacobsen Copyright 2009
 * @since 2.5.5
 */
@RunWith(Parameterized.class)
public class SchemaTest extends SchemaTestBase {

    @Parameters(name = "{0} (pass={1})")
    public static Iterable<Object[]> data() {
        // the following are just tested for schema pass/fail, not load/store
        return getFiles(new File("java/test/jmri/jmrit/display/controlPanelEditor/shape/valid"), true, true);
    }

    public SchemaTest(File file, boolean pass) {
        super(file, pass);
    }

    public void foo() {

    }
}

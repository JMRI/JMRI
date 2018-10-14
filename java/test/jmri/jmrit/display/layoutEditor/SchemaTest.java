package jmri.jmrit.display.layoutEditor;

import java.io.File;
import java.util.ArrayList;
import jmri.configurexml.SchemaTestBase;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

//import jmri.InstanceManager;
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
        ArrayList<Object[]> files = new ArrayList<>();
        files.addAll(getFiles(new File("java/test/jmri/jmrit/display/layoutEditor/valid"), true, true));
        files.addAll(getFiles(new File("java/test/jmri/jmrit/display/layoutEditor/invalid"), true, false));
        return files;
    }

    public SchemaTest(File file, boolean pass) {
        super(file, pass);
    }
}

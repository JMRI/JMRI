package jmri.jmrit.symbolicprog.tabbedframe;

import java.io.File;
import jmri.configurexml.SchemaTestBase;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Check the names in an XML programmer file against the names.xml definitions
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2007, 2008
 * @see jmri.jmrit.XmlFile
 */
@RunWith(Parameterized.class)
public class SchemaTest extends SchemaTestBase {

    @Parameters(name = "{0} (pass={1})")
    public static Iterable<Object[]> data() {
        return getFiles(new File("xml/programmers"), true, true);
    }

    public SchemaTest(File file, boolean pass) {
        super(file, pass);
    }
}

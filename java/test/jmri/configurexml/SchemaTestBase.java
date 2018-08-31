package jmri.configurexml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import jmri.jmrit.XmlFile;
import jmri.util.JUnitUtil;
import org.jdom2.JDOMException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Base for XML schema testing
 *
 * @author Bob Jacobsen Copyright 2009, 2014
 * @since 3.9.2
 */
@RunWith(Parameterized.class)
public class SchemaTestBase {

    private XmlFile.Validate validate;
    private final File file;
    private final boolean pass;

    public SchemaTestBase(File file, boolean pass) {
        this.file = file;
        this.pass = pass;
    }

    @Test
    public void validate() {
        Assume.assumeFalse("Ignoring schema validation.", Boolean.getBoolean("jmri.skipschematests"));
        XmlFile.setDefaultValidate(XmlFile.Validate.CheckDtdThenSchema);
        XmlFile xf = new XmlFileImpl();
        try {
            xf.rootFromFile(file);
            if (!this.pass) {
                Assert.fail("Validation of \"" + file.getPath() + "\" should have failed");
            }
        } catch (IOException | JDOMException ex) { // throw unexpected errors
            if (this.pass) {
                Assert.fail("Failed to validate \"" + file.getPath() + "\" due to: " + ex);
            }
        }
    }

    /**
     * Get all XML files in a directory and validate them.
     *
     * @param directory the directory containing XML files
     * @param recurse   if true, will recurse into subdirectories
     * @param pass      if true, successful validation will pass; if false,
     *                  successful validation will fail
     * @return a collection of Object arrays, where each array contains the
     *         {@link java.io.File} with a filename ending in {@literal .xml} to
     *         validate and a boolean matching the pass parameter
     */
    public static Collection<Object[]> getFiles(File directory, boolean recurse, boolean pass) {
        ArrayList<Object[]> files = new ArrayList<>();
        if (directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                if (file.isDirectory()) {
                    if (recurse) {
                        files.addAll(getFiles(file, recurse, pass));
                    }
                } else {
                    files.addAll(getFiles(file, recurse, pass));
                }
            }
        } else if (directory.getName().endsWith(".xml")) {
            files.add(new Object[]{directory, pass});
        }
        return files;
    }

    /**
     * Get all XML files in the immediate subdirectories of a directory and
     * validate them.
     *
     * @param directory the directory containing subdirectories containing XML
     *                  files
     * @param recurse   if true, will recurse into subdirectories
     * @param pass      if true, successful validation will pass; if false,
     *                  successful validation will fail
     * @return a collection of Object arrays, where each array contains the
     *         {@link java.io.File} with a filename ending in {@literal .xml} to
     *         validate and a boolean matching the pass parameter
     * @throws IllegalArgumentException if directory is a file
     */
    public static Collection<Object[]> getDirectories(File directory, boolean recurse, boolean pass) throws IllegalArgumentException {
        ArrayList<Object[]> files = new ArrayList<>();
        if (directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                if (file.isDirectory()) {
                    files.addAll(getFiles(file, recurse, pass));
                }
            }
        } else {
            throw new IllegalArgumentException("directory must be a directory, not a file");
        }
        return files;
    }

    @Before
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        this.validate = XmlFile.getDefaultValidate();
    }

    @After
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void tearDown() throws Exception {
        XmlFile.setDefaultValidate(this.validate);
        JUnitUtil.tearDown();
    }

    private static class XmlFileImpl extends XmlFile {
        // empty implementation of abstract class
    }
}

package jmri.configurexml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import jmri.jmrit.XmlFile;
import jmri.util.JUnitUtil;

import org.jdom2.JDOMException;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.provider.Arguments;

/**
 * Base for XML schema testing
 *
 * @author Bob Jacobsen Copyright 2009, 2014
 * @since 3.9.2
 */
public class SchemaTestBase {

    private XmlFile.Validate validate;

    /**
     * Configure for handling I/O checks:
     * <ul>
     * <li> Files in the load subdirectory will be loaded and stored
     * <li> Files in the valid subdirectory will be checked for schema validity
     * <li> Files in the invalid subdirectory will be checked for schema _in_validity
     * </ul>
     * @param path the path to this directory
     * @return the stream of files to process
     */
    static protected Stream<Arguments> setTestFilesBelowThisPath(String path) {
        ArrayList<Arguments> files = new ArrayList<>();
        // the following are just tested for schema pass/fail, not load/store
        files.addAll(getFiles(new File(path+"/valid"), true, true).collect(Collectors.toList()));
        files.addAll(getFiles(new File(path+"/invalid"), true, false).collect(Collectors.toList()));
        // also tested for load/store
        files.addAll(getFiles(new File(path+"/load"), true, true).collect(Collectors.toList()));

        Assert.assertFalse("There should be something here; misconfigured?", files.isEmpty());
        return files.stream();
    }

    public void validate(File file, boolean pass) {
        Assume.assumeFalse("Ignoring schema validation.", Boolean.getBoolean("jmri.skipschematests"));
        XmlFile.setDefaultValidate(XmlFile.Validate.CheckDtdThenSchema);
        XmlFile xf = new XmlFileImpl();
        try {
            xf.rootFromFile(file);
            if (!pass) {
                Assert.fail("Validation of \"" + file.getPath() + "\" should have failed");
            }
        } catch (IOException | JDOMException ex) { // throw unexpected errors
            if (pass) {
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
     * @return a stream of {@link Arguments}, where each Argument contains the
     *         {@link java.io.File} with a filename ending in {@literal .xml} to
     *         validate and a boolean matching the pass parameter
     */
    public static Stream<Arguments> getFiles(File directory, boolean recurse, boolean pass) {
        ArrayList<Arguments> files = new ArrayList<>();
        if (directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                if (file.isDirectory()) {
                    if (recurse) {
                        files.addAll(getFiles(file, recurse, pass).collect(Collectors.toList()));
                    }
                } else {
                    files.addAll(getFiles(file, recurse, pass).collect(Collectors.toList()));
                }
            }
        } else if (directory.getName().endsWith(".xml")) {
            files.add(Arguments.of(directory, pass));
        }
        return files.stream();
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
     * @return a stream of {@link Arguments}, where each Argument contains the
     *         {@link java.io.File} with a filename ending in {@literal .xml} to
     *         validate and a boolean matching the pass parameter
     * @throws IllegalArgumentException if directory is a file
     */
    public static Stream<Arguments> getDirectories(File directory, boolean recurse, boolean pass) throws IllegalArgumentException {
        ArrayList<Arguments> files = new ArrayList<>();
        if (directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                if (file.isDirectory()) {
                    files.addAll(getFiles(file, recurse, pass).collect(Collectors.toList()));
                }
            }
        } else {
            throw new IllegalArgumentException("directory must be a directory, not a file");
        }
        return files.stream();
    }

    @BeforeEach
    @OverridingMethodsMustInvokeSuper
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        this.validate = XmlFile.getDefaultValidate();
    }

    @AfterEach
    @OverridingMethodsMustInvokeSuper
    public void tearDown() throws Exception {
        XmlFile.setDefaultValidate(this.validate);
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    private static class XmlFileImpl extends XmlFile {
        // empty implementation of abstract class
    }
}

package jmri.util;

import apps.tests.Log4JFixture;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Collection;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests that files have correct line endings. The list of file patterns tested
 * should match the list fixed by the ant fixlineends target.
 * <p>
 * Do not include in the jmri package test suite.
 *
 * @author Randall Wood (C) 2017
 */
@RunWith(Parameterized.class)
public class FileLineEndingsTest {

    private final File file;

    private final static Logger log = LoggerFactory.getLogger(FileLineEndingsTest.class);

    @Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return getFiles(new File("."), new String[]{
            "**/*.csh",
            "**/*.css",
            "**/*.df",
            "**/*.dtd",
            "**/*htm",
            "**/*html",
            "**/*.java",
            "**/*.js",
            "**/*.json",
            "**/*.jsp",
            "**/*.jspf",
            "**/*.lcf",
            "**/*.md",
            "**/*.php",
            "**/*.pl",
            "**/*.plist",
            "**/*.policy",
            "**/*.prefs",
            "**/*.properties",
            "**/*.project",
            "**/*.py",
            "**/*.sh",
            "**/*.svg",
            "**/*.tld",
            "**/*.txt",
            "**/*.xml",
            "**/*.xsd",
            "**/*.xsl",
            "**/COPYING",
            "**/Footer",
            "**/Header",
            "**/README*",
            "**/Sidebar",
            "**/TODO",
            "**/.classpath"
        });
    }

    /**
     * Get all files with the given prefixes in a directory and validate them.
     *
     * @param directory the directory containing the files
     * @param patterns  glob patterns of files to match
     * @return a collection of files to validate
     */
    public static Collection<Object[]> getFiles(File directory, String[] patterns) {
        Log4JFixture.setUp(); // setup logging early so this method can log
        ArrayList<Object[]> files = new ArrayList<>();
        try {
            for (String pattern : patterns) {
                PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
                // ignore the build directory if immediately under the passed in directory
                PathMatcher target = FileSystems.getDefault().getPathMatcher("glob:./target/**");
                Files.walk(directory.toPath())
                        .filter(path -> !target.matches(path))
                        .filter(matcher::matches)
                        .forEach((path) -> {
                            if (path.toFile().isFile()) {
                                files.add(new Object[]{path.toFile()});
                            }
                        });
            }
        } catch (IOException ex) {
            log.error("Unable to get files in {}", directory, ex);
        }
        return files;
    }

    public FileLineEndingsTest(File file) {
        this.file = file;
    }

    @Test
    public void lineEndings() {
        try {
            String contents = FileUtils.readFileToString(file);
            Assert.assertFalse("File " + file.getPath() + " has incorrect line endings.", contents.contains("\r\n"));
        } catch (IOException ex) {
            log.error("Unable to get path for {}", this.file, ex);
            Assert.fail("Unable to get get path " + file.getPath() + " for test");
        }
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}

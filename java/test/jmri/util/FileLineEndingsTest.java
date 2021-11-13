package jmri.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.FileUtils;

import org.junit.*;
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
        return getFiles(new File("."),
                new String[]{ // patterns to match
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
                }, new String[]{ // patterns not to match
                    "./target/**", // ignore the build directory if immediately under the passed in directory
                    "**/node_modules/**" // ignore node_modules directories anywhere as those are from external sources
                });
    }

    /**
     * Get all files with the given prefixes in a directory and validate them.
     *
     * @param directory    the directory containing the files
     * @param patterns     glob patterns of files to match
     * @param antiPatterns glob patterns of files not to match
     * @return a collection of files to validate
     */
    public static Collection<Object[]> getFiles(File directory, String[] patterns, String[] antiPatterns) {
        jmri.util.JUnitUtil.setUp(); // setup logging early so this method can log
        ArrayList<Object[]> files = new ArrayList<>();
        ArrayList<PathMatcher> antiMatchers = new ArrayList<>();
        for (String antiPattern : antiPatterns) {
            antiMatchers.add(FileSystems.getDefault().getPathMatcher("glob:" + antiPattern));
        }
        try {
            for (String pattern : patterns) {
                PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
                Files.walk(directory.toPath())
                        .filter(path -> antiMatchers.stream().noneMatch((antiMatcher) -> (antiMatcher.matches(path))))
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

    @BeforeClass  // want to reduce burden
    static public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterClass// want to reduce burden
    static public void tearDown() {
        JUnitUtil.tearDown();
    }
}

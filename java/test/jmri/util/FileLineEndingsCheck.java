package jmri.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.FileUtils;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks that files have correct line endings.
 * The list of file patterns tested
 * should match the list fixed by the ant fixlineends target.
 * <p>
 * Disabled on Windows systems where line endings will fail.
 * <p>
 * Do not include in the jmri package test suite.
 * Cannot have name ending in *Test because it'll get picked up.
 * That used to result in it being run on Windows and failing big time.
 *
 * @author Randall Wood (C) 2017
 */
public class FileLineEndingsCheck {

    private final static Logger log = LoggerFactory.getLogger(FileLineEndingsCheck.class);

    public static Iterable<File> data() {
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
    public static Collection<File> getFiles(File directory, String[] patterns, String[] antiPatterns) {
        setUp(); // setup logging early so this method can log
        ArrayList<File> files = new ArrayList<>(22000); // 19350 as of March 2022
        ArrayList<PathMatcher> antiMatchers = new ArrayList<>(antiPatterns.length);
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
                                files.add( path.toFile());
                            }
                        });
            }
        } catch (IOException ex) {
            log.error("Unable to get files in {}", directory, ex);
        }
        return files;
    }

    @ParameterizedTest(name = "[{index}] {arguments}")
    @MethodSource("data")
    @DisabledOnOs(OS.WINDOWS)
    public void lineEndings(File file) {
        try {
            String contents = FileUtils.readFileToString(file, java.nio.charset.Charset.defaultCharset());
            Assertions.assertFalse(contents.contains("\r\n"), "File " + file.getPath() + " has incorrect line endings.");
        } catch (IOException ex) {
            log.error("Unable to get path for {}", file, ex);
            Assertions.fail("Unable to get get path " + file.getPath() + " for test");
        }
    }

    private static boolean setup = false;

    @BeforeAll // want to reduce burden
    static public void setUp() {
        if (!setup) {
            JUnitUtil.setUp();
            setup = true;
        }
    }

    @AfterAll // want to reduce burden
    static public void tearDown() {
        JUnitUtil.tearDown();
    }
}

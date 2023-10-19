package jmri.util;

import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.*;

/**
 * Check help files for UTF-8 characters.
 * Files that contain &l;tmeta charset="utf-8"&gt; are exempt.
 *
 * @author Daniel Bergqvist Copyright (C) 2022
 */
public class CheckHelpFilesForUTF8Test {

    private final Map<Integer, String> convertChar = new HashMap<>();
    private final Set<Integer> foundChar = new HashSet<>();
    private int numErrors = 0;


    private void searchFolder(String folder) throws IOException {
        Path path = FileSystems.getDefault().getPath(folder);
        Set<String> files = Stream.of(path.toFile().listFiles())
                  .filter(file -> !file.isDirectory())
                  .map(File::getName)
                  .collect(Collectors.toSet());

        for (String file : files) {
            if (file.endsWith(".shtml")) {
                String fileName = folder + file;

                var lines = Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8);
                for (String s : lines) {
                    if (s.contains("<meta charset=\"utf-8\">")) break; // no further testing for UTF
                    s.codePoints().forEach((codePoint) -> {
                        if (codePoint > 127) {
                            numErrors++;
                            foundChar.add(codePoint);
                            String expected = convertChar.get(codePoint);
                            log.error(
                                    "Invalid character. Codepoint: {}, Character: {}, Replace with: {}, File: {}",
                                    codePoint, new String(Character.toChars(codePoint)), expected, fileName);
                        }
                    });
                }
            }
        }

        Set<String> folders = Stream.of(path.toFile().listFiles())
                  .filter(file -> file.isDirectory())
                  .map(File::getName)
                  .collect(Collectors.toSet());

        for (String aFolder : folders) {
            searchFolder(folder + aFolder + "/");
        }

    }

    @Test
    public void testGenerateSearchIndex() throws IOException {
        // See: https://www.w3schools.com/charsets/ref_utf_punctuation.asp
        convertChar.put(169, "&copy;");
        convertChar.put(174, "&reg;");
        convertChar.put(176, "&deg;");
        convertChar.put(200, "&Egrave;");
        convertChar.put(201, "&Eacute;");
        convertChar.put(220, "&Uuml;");
        convertChar.put(223, "&szlig;");
        convertChar.put(224, "&agrave;");
        convertChar.put(225, "&aacute;");
        convertChar.put(226, "&acirc;");
        convertChar.put(228, "&auml;");
        convertChar.put(229, "&aring;");
        convertChar.put(230, "&aelig;");
        convertChar.put(231, "&ccedil;");
        convertChar.put(232, "&egrave;");
        convertChar.put(233, "&eacute;");
        convertChar.put(234, "&ecirc;");
        convertChar.put(237, "&iacute;");
        convertChar.put(241, "&ntilde;");
        convertChar.put(244, "&ocirc;");
        convertChar.put(246, "&ouml;");
        convertChar.put(248, "&oslash;");
        convertChar.put(252, "&uuml;");
        convertChar.put(253, "&yacute;");
        convertChar.put(268, "&Ccaron;");
        convertChar.put(283, "&ecaron;");
        convertChar.put(339, "&oelig;");
        convertChar.put(345, "&rcaron;");
        convertChar.put(352, "&Scaron;");
        convertChar.put(381, "&Zcaron;");
        convertChar.put(8209, "&#8209;");
        convertChar.put(8211, "&ndash;");
        convertChar.put(8212, "&mdash;");
        convertChar.put(8216, "&lsquo;");
        convertChar.put(8217, "&rsquo;");
        convertChar.put(8220, "&ldquo;");
        convertChar.put(8221, "&rdquo;");
        convertChar.put(8226, "&bull;");
        convertChar.put(8230, "&hellip;");
        convertChar.put(8250, "&rsaquo;");
        convertChar.put(8482, "&trade;");
        convertChar.put(8594, "&rarr;");
        convertChar.put(8629, "&crarr;");
        convertChar.put(8658, "&rArr;");
        convertChar.put(9662, "&#9662;");
        convertChar.put(10004, "&#10004;");

        searchFolder("help/en/");

        for (int codePoint : foundChar) {
            String expected = convertChar.get(codePoint);
            log.error("Found UTF-8 Codepoint: {}, Character: {}. Expected: {}",
                    codePoint, new String(Character.toChars(codePoint)), expected);
        }

        if (numErrors > 0) log.error("Num errors: {}", numErrors);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CheckHelpFilesForUTF8Test.class);

}

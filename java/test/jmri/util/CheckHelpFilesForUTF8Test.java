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
 *
 * @author Daniel Bergqvist Copyright (C) 2022
 */
public class CheckHelpFilesForUTF8Test {

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
                    s.codePoints().forEach((codePoint) -> {
                        if (codePoint > 127) {
                            log.error(
                                    "Invalid character. Codepoint: {}, Character: {}, File: {}",
                                    codePoint, new String(Character.toChars(codePoint)), fileName);
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
        searchFolder("help/en/");
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

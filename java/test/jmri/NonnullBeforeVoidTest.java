package jmri;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Check that there is no @Nonnull annotation before a void declaration.
 * Also checks that "static" comes after and not before public/protected/private.
 *
 * @author Daniel Bergqvist (C) 2026
 */
public class NonnullBeforeVoidTest {

    private static final Pattern PATTERN_NULL_BEFORE_VOID = Pattern.compile(".*\\@Nonnull\\s+(\\@\\w+(\\(.*?\\))\\s+)*(|public\\s+|protected\\s+|private\\s+)void.*", Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern PATTERN_STATIC_BEFORE_PUBLIC = Pattern.compile(".*static\\s+(public|protected|private).*", Pattern.MULTILINE | Pattern.DOTALL);
//    private static final Pattern PATTERN_FINAL_BEFORE_PUBLIC_OR_STATIC = Pattern.compile(".*final\\s+(public|protected|private|static).*", Pattern.MULTILINE | Pattern.DOTALL);


    public static Stream<Arguments> data() {
        // Exclude java/graalvm/ since we don't have control over them
        File[] directories = {new File("java/src/"), new File("java/test/")};
        boolean recurse = true;
        boolean pass = true;
        return getFiles(directories, recurse, pass);
    }

    /**
     * Get all Java files in a directory and validate them.
     *
     * @param directories the directories containing Java files
     * @param recurse     if true, will recurse into subdirectories
     * @param pass        if true, successful validation will pass; if false,
     *                    successful validation will fail
     * @return a stream of {@link Arguments}, where each Argument contains the
     *         {@link java.io.File} with a filename ending in {@literal .java} to
     *         validate and a boolean matching the pass parameter
     */
    private static Stream<Arguments> getFiles(File[] directories, boolean recurse, boolean pass) {
        ArrayList<Arguments> files = new ArrayList<>();
        for (File directory : directories) {
            if (directory.isDirectory()) {
                for (File file : directory.listFiles()) {
                    if (file.isDirectory()) {
                        if (recurse) {
                            files.addAll(getFiles(new File[]{file}, recurse, pass).collect(Collectors.toList()));
                        }
                    } else {
                        files.addAll(getFiles(new File[]{file}, recurse, pass).collect(Collectors.toList()));
                    }
                }
            } else if (directory.getName().endsWith(".java")) {
                files.add(Arguments.of(directory, pass));
            }
        }
        files.sort((a,b) -> {
            File aa = (File) a.get()[0];
            File bb = (File) b.get()[0];
            return aa.getAbsolutePath().compareTo(bb.getAbsolutePath());
        });
        return files.stream();
    }

    @ParameterizedTest(name = "{index}: {0} (pass={1})")
    @MethodSource("data")
    public void loadAndStoreTest(File file, boolean pass) throws IOException, JmriException, ParseException {
        checkFile(file);
    }

//    private static int total = 0;
//    private static int fails = 0;

    private void checkFile(File file) throws IOException {

//        total++;

//        System.out.format("File: %s%n", file);
//        log.warn("File: {}", file);
        String data = new String(Files.readAllBytes(file.toPath()));
        if (PATTERN_NULL_BEFORE_VOID.matcher(data).matches()) {
            Assertions.fail(String.format("File %s has @Nonnull annotation for a void declaration", file));
        }
        if (PATTERN_STATIC_BEFORE_PUBLIC.matcher(data).matches()) {
//            fails++;
//            System.out.format("File has static before public/protected/private: %s%n", file);
//            System.out.format("%d, %d: File has static before public/protected/private: %s%n", total, fails, file);
            Assertions.fail(String.format("File %s has static before public/protected/private", file));
        }
/* Ignore for now
        if (PATTERN_FINAL_BEFORE_PUBLIC_OR_STATIC.matcher(data).matches()) {
            fails++;
//            System.out.format("File has static before public/protected/private: %s%n", file);
            System.out.format("%d, %d: File has final before public/protected/private/static: %s%n", total, fails, file);
//            Assertions.fail(String.format("File %s has final before public/protected/private", file));
        }
*/
//        System.out.println(data);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

//    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NonnullBeforeVoidTest.class);
}

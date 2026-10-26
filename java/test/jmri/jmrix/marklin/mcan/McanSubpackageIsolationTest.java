package jmri.jmrix.marklin.mcan;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Enforces the JMRI-independence contract of the {@code jmri.jmrix.marklin.mcan}
 * sub-package: no source file under {@code mcan/} may import anything from
 * {@code jmri.*} (the JMRI application layer).
 * <p>
 * Within-package imports ({@code jmri.jmrix.marklin.mcan.*}) are the only
 * cross-file references permitted inside the library.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de)
 */
public class McanSubpackageIsolationTest {

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    /**
     * Root of the mcan production source tree, relative to the repository root.
     * Resolved against the working directory so the test works from any launch point
     * that has the repo root as cwd (Maven surefire standard behaviour).
     */
    private static final String MCAN_SRC_ROOT =
            "java/src/jmri/jmrix/marklin/mcan";

    @Test
    public void testNoJmriImportsInMcanSubpackage() throws IOException {
        Path root = Paths.get(MCAN_SRC_ROOT);
        assertTrue(Files.isDirectory(root),
                "mcan source root not found at: " + root.toAbsolutePath()
                + "  -  run test from the repository root");

        List<String> violations = new ArrayList<>();

        try (Stream<Path> walk = Files.walk(root)) {
            walk.filter(p -> p.toString().endsWith(".java"))
                .forEach(file -> {
                    try {
                        List<String> lines = Files.readAllLines(file);
                        for (int i = 0; i < lines.size(); i++) {
                            String line = lines.get(i).trim();
                            if (line.startsWith("import jmri.")
                                    && !line.startsWith("import jmri.jmrix.marklin.mcan.")) {
                                violations.add(file + ":" + (i + 1) + "  " + line);
                            }
                        }
                    } catch (IOException e) {
                        violations.add(file + ": could not read  -  " + e.getMessage());
                    }
                });
        }

        assertTrue(violations.isEmpty(),
                "mcan sub-package must not import jmri.* (except within-package).\n"
                + "Violations found:\n" + String.join("\n", violations));
    }
}

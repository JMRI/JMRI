package jmri.web.servlet.directory;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jmri.util.JUnitUtil;

/**
 *
 * @author Paul Bender Copyright 2017
 * @author Randall Wood Copyright 2020
 */
public class DirectoryHandlerTest {

    @Test
    public void testConstructor_String() throws URISyntaxException, IOException {
        DirectoryHandler t = new DirectoryHandler("foo");
        // because getResourceBase() returns a slightly different URI than
        // `new File("foo").toURL().toURI()` does, compare paths of Files from URIs
        assertThat(new File(new URI(t.getResourceBase())).getCanonicalPath()).isEqualTo((new File("foo")).getCanonicalPath());
        assertThat(t.isDirectoriesListed()).isTrue();
        assertThat(t.getWelcomeFiles()).containsExactly("index.html");
    }

    @Test
    public void testDefaultConstructor() {
        DirectoryHandler t = new DirectoryHandler();
        assertThat(t.getResourceBase()).isNull();
        assertThat(t.isDirectoriesListed()).isTrue();
        assertThat(t.getWelcomeFiles()).containsExactly("index.html");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}

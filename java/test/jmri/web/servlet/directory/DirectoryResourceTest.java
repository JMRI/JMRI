package jmri.web.servlet.directory;

import jmri.util.FileUtil;
import jmri.util.JUnitUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Locale;

import org.eclipse.jetty.util.resource.EmptyResource;
import org.eclipse.jetty.util.resource.PathResource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Paul Bender Copyright 2017
 * @author Randall Wood Copyright 2020
 */
public class DirectoryResourceTest {

    DirectoryResource instance;

    @Test
    public void testCTor() throws IOException {
        assertThat(instance).as("exists").isNotNull();
    }

    @Test
    public void testGetListHTML() throws IOException {
        // test File that is directory resource
        assertThat(instance.getListHTML(null, false, "")).isNull();
        assertThat(instance.getListHTML("", false, "")).isNotNull();
        // test File that is not directory resource
        instance = new DirectoryResource(Locale.US, new PathResource(new File(FileUtil.getFile(FileUtil.getProgramPath()), "README.md")));
        assertThat(instance.getListHTML("", false, "")).isNull();
        // test non-File resource
        assertThatCode(() -> new DirectoryResource(Locale.US, EmptyResource.INSTANCE))
            .as("Resource must be for a file path")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    public void testHrefEncodeURI() throws Exception {
        Method method = DirectoryResource.class.getDeclaredMethod("hrefEncodeURI", String.class);
        method.setAccessible(true);
        assertThat(method.invoke(instance, "foobar")).isEqualTo("foobar");
        assertThat(method.invoke(instance, "foo'bar")).isEqualTo("foo%27bar");
        assertThat(method.invoke(instance, "foo\"bar")).isEqualTo("foo%22bar");
        assertThat(method.invoke(instance, "foo<bar>")).isEqualTo("foo%3Cbar%3E");

    }

    @BeforeEach
    public void setUp() throws IOException {
        JUnitUtil.setUp();
        instance = new DirectoryResource(Locale.US, new PathResource(FileUtil.getFile(FileUtil.getProgramPath())));
    }

    @AfterEach
    public void tearDown() {
        instance = null;
        JUnitUtil.tearDown();
    }
}

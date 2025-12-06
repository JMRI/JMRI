package jmri.jmrix.openlcb.configurexml;

import jmri.jmrix.openlcb.OlcbSystemConnectionMemoScaffold;
import jmri.configurexml.LoadAndStoreTestBase;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Stream;

import jmri.util.JUnitAppender;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openlcb.*;

/**
 * Test that configuration files can be read and then stored again consistently.
 * When done across various versions of schema, this checks ability to read
 * older files in newer versions; completeness of reading code; etc.
 * <p>
 * Functional checks, that e.g. check the details of a specific type are being
 * read properly, should go into another type-specific test class.
 * <p>
 * The functionality comes from the common base class, this is just here to
 * insert the test suite into the JUnit hierarchy at the right place.
 *
 * @author Bob Jacobsen Copyright 2009, 2014
 * @since 2.5.5 (renamed & reworked in 3.9 series)
 */
public class LoadAndStoreTest extends LoadAndStoreTestBase {

    public static Stream<Arguments> data() {
        return getFiles(new File("java/test/jmri/jmrix/openlcb/configurexml"), false, true);
    }

    @ParameterizedTest(name = "{index}: {0} (pass={1})")
    @MethodSource("data")
    public void loadAndStoreTest(File file, boolean pass) throws Exception {
        super.loadLoadStoreFileCheck(file);
        // IH1 already suppressed in super test
        JUnitAppender.suppressErrorMessage("systemName is already registered: IH2");
        JUnitAppender.suppressErrorMessage("systemName is already registered: IH3");
        JUnitAppender.suppressErrorMessage("systemName is already registered: IH4");
        JUnitAppender.suppressErrorMessage("systemName is already registered: IH5");

    }

    public LoadAndStoreTest() {
        super(SaveType.Config, false);
        messages = new ArrayList<>();
    }

    // from here down is testing infrastructure
    private OlcbSystemConnectionMemoScaffold memo;
    private Connection connection;
    private NodeID nodeID;
    private ArrayList<Message> messages;

    @BeforeEach
    @SuppressWarnings("deprecation") // OlcbInterface(NodeID, Connection)
    @Override
    public void setUp(@TempDir java.io.File tempDir) throws IOException  {
        super.setUp(tempDir);
        nodeID = new NodeID(new byte[]{1, 0, 0, 0, 0, 0});

        messages = new ArrayList<>();
        connection = new AbstractConnection() {
            @Override
            public void put(Message msg, Connection sender) {
                messages.add(msg);
            }
        };

        memo = new OlcbSystemConnectionMemoScaffold(); // this self-registers as 'M'
        memo.setProtocol(jmri.jmrix.can.ConfigurationManager.OPENLCB);
        memo.setInterface(new OlcbInterface(nodeID, connection) {
            @Override
            public Connection getOutputConnection() {
                return connection;
            }
        });

        jmri.util.JUnitUtil.waitFor(() -> (!messages.isEmpty()), "Initialization Complete message");
    }

    @AfterEach
    @Override
    public void tearDown() {
        if (memo != null && memo.getInterface() != null) {
            memo.getInterface().dispose();
        }
        memo = null;
        connection = null;
        nodeID = null;
        super.tearDown();
    }

}

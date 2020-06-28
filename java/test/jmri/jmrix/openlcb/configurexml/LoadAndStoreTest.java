package jmri.jmrix.openlcb.configurexml;

import jmri.configurexml.LoadAndStoreTestBase;
import jmri.jmrix.openlcb.*;

import java.io.File;
import java.util.ArrayList;
import java.util.stream.Stream;

import org.junit.jupiter.api.*;
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
    }

    public LoadAndStoreTest() {
        super(SaveType.Config, false);
    }

    // from here down is testing infrastructure
    private OlcbSystemConnectionMemo memo;
    private Connection connection;
    private NodeID nodeID;
    private ArrayList<Message> messages;

    @BeforeEach
    public void localSetUp() {
        nodeID = new NodeID(new byte[]{1, 0, 0, 0, 0, 0});

        messages = new ArrayList<>();
        connection = new AbstractConnection() {
            @Override
            public void put(Message msg, Connection sender) {
                messages.add(msg);
            }
        };

        memo = new OlcbSystemConnectionMemo(); // this self-registers as 'M'
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
    public void localTearDown() {
        if (memo != null && memo.getInterface() != null) {
            memo.getInterface().dispose();
        }
        memo = null;
        connection = null;
        nodeID = null;
    }

}

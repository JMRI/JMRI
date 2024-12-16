package jmri.jmrix.openlcb;

import org.junit.jupiter.api.*;

import org.openlcb.NodeID;

/**
 *
 * @author Bob Jacobsen (C) 2024
 */
public class OlcbNodeGroupStoreTest {

    // The minimal setup for log4J
    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        
        store = new OlcbNodeGroupStore() {
            @Override
            protected void load() {  // want tests to be self-contained
            }
            @Override
            protected void initShutdownTask() { // want tests to be self-contained
            }
        };
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

    OlcbNodeGroupStore store;

    @Test
    public void addAndFind() {
        NodeID node = new NodeID("01.02.03.04.05.06");
        String group = "Group A";

        store.addNodeToGroup(node, group);

        Assertions.assertTrue(store.isNodeInGroup(node, group));
    }

    @Test
    public void addARemoveAndNotFind() {
        NodeID node = new NodeID("01.02.03.04.05.06");
        String group = "Group A";

        store.addNodeToGroup(node, group);
        store.removeNodeFromGroup(node, group);

        Assertions.assertFalse(store.isNodeInGroup(node, group));
    }

    @Test
    public void checkListOfGroups() {
        NodeID node = new NodeID("01.02.03.04.05.06");
        String groupB = "Group B";
        String groupA = "Group A";

        store.addNodeToGroup(node, groupB);  // check that adds are kept in right order
        store.addNodeToGroup(node, groupA);

        Assertions.assertEquals("Group A", store.getGroupNames().get(0));
        Assertions.assertEquals("Group B", store.getGroupNames().get(1));
        
        Assertions.assertEquals("Group A", store.getNodesGroups(node).get(0));
        Assertions.assertEquals("Group B", store.getNodesGroups(node).get(1));
    }

    @Test
    public void addAndRemoveGroup() {
        NodeID node = new NodeID("01.02.03.04.05.06");
        String groupB = "Group B";
        String groupA = "Group A";

        store.addNodeToGroup(node, groupB);  // check that adds are kept in right order
        store.addNodeToGroup(node, groupA);

        store.removeGroup(groupA);
        Assertions.assertEquals(1, store.getGroupNames().size());
        Assertions.assertEquals("Group B", store.getGroupNames().get(0));

        Assertions.assertFalse(store.isNodeInGroup(node, groupA));
    }

}

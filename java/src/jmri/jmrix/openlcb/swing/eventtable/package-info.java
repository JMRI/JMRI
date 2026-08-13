/**
 * Provides a table showing relationships among Nodes, Event Producers and Event Consumers.
 * <p>
 * The event name to/from event ID information is taken from the Event Name table, which
 * is maintained across the JMRI LCC code.  If you e.g. add a name to an event in 
 * a configuration window, it will be updated and automatically be persisted by
 * the event table, separately from this package.  That information is stored
 * in an eventnames/eventNames.xml file within the current profile directory
 * by the {@link jmri.jmrix.openlcb.OlcbEventNameStore} class.
 * <p>
 * The Event Table in this package displays the association between event IDs and
 * their producer and consumer nodes.  This information is obtained
 * when the tables "update from network" button is pressed, and automatically updated
 * from traffic on the LCC network.
 * <p>
 * The event table also displays "also known as" information from multiple sources:
 * <ol>
 * <li>The well-known eventID names from the LCC standards documentation
 * <li>Event ID uses identified in open configuration dialogs
 * <li>Event ID uses from configuration dialogs seen in previous runs.
 * </ol>
 * <p>
 * This information  is persisted in an eventnames/eventTable.xml file within the 
 * current profile directory by the {@link EventTableDataModel} class. 
 * Since the information only changes once the Event Table tool
 * is open, it is only stored at shutdown if the Event Table has been opened.
 * <p>
 * The persistence is meant to make the Event Table usable off-line from the main 
 * LCC network.  This raises several issues that require special handling:
 * <ul>
 * <li>A user might have different hardware, hence different node IDs, for the
 *     same effective node at two different locations.  I.e. they've loaded a node
 *     backup from their club into a different board at home.
 *     <p>
 *     To handle this, the table is repopulated using node <u>names</u> if available 
 *     instead of node IDs, if the node names are available.  Priority is given to 
 *     node name to node ID associations on the current LCC network over those 
 *     retrieved from the persisted file.
 * <li>The prior item means that networks containing multiple nodes with the same name
 *     are likely to provide confusing results.
 * </ul>
 *
 * @see jmri.jmrix.openlcb.OlcbEventNameStore
 * @see jmri.jmrix.openlcb.swing.eventtable.configurexml
 *
 * @since JMRI 5.3.4
 */
// include empty DefaultAnnotation to avoid excessive recompilation
@edu.umd.cs.findbugs.annotations.DefaultAnnotation(value={})
package jmri.jmrix.openlcb.swing.eventtable;

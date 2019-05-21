 /**
 * Support for implementing USS CTC systems in JMRI.
 * 
 * <h2>Related Documentation</h2>
 * 
 * For overviews, tutorials, examples, guides, and tool documentation, please see:
 * <ul>
 *   <li><a href="http://jmri.org/help/en//html/tools/uss/index.shtml">JMRI USS CTC overview page</a>
 *   <li><a href="http://jmri.org/resources/icons/USSpanels/">Icons and other useful graphical bits</a>
 * </ul>
 * 
 * Sequence diagrams of how all this goes together:
 * <a href="doc-files/CodeButtonPressTurnout.png"><img src="doc-files/CodeButtonPressTurnout.png" style="text-align: right;" alt="Code press" height="25%" width="25%"></a>
 * <a href="doc-files/TurnoutChangeIndication.png"><img src="doc-files/TurnoutChangeIndication.png" style="text-align: right;" alt="Indication" height="25%" width="25%"></a>
 * <HR>
 *     There are a number of older tools remaining in this package:
 * <ul>
 *     <li>Follower - connects a Turnout to the state of a Sensor. Useful for
 *         e.g. driving a turnout from a switch on the fascia, or to 
 *         drive an indicator light on a physical panel from a senor. 
 *         A veto sensor is optionally available, e.g. to prevent a turnout from
 *         being changed under a train.
 *     <li>OS Indicator - drives the lamp on the panel for a particular OS.
 *         Honors a separate lock/unlocked indication by showing occupied if the
 *         associated turnout has been unlocked.
 * </ul>
 * 
 * <p>
 *     Each tool is made of three parts:
 * <ol>
 *     <li>A class that handles creation and editing of the underlying objects
 *         that implement the tool.  For example, the
 *         OsIndicator
 *         class provides objects that can create the Routes and Logix needed
 *         to implement an OS Indicator.
 *     <li>A class to provide a JPanel for creating and editing the objects.
 *         For example, the OsIndicatorPanel class does this for OsIndicators.
 *     <li>A class to provide a default frame for embedding the panel. In
 *         the case of Os Indicators, this is OsIndicatorFrame. These are children of 
 *         JmriJFrames so they can inherit the features expected ot a JMRI tool frame.
 *     <li>A class to provide an Action that will open the frame, e.g. for use in menus.
 * </ol>
 * 
 * <p>
 *     The underlying implementations for these are based on
 *     Logix and Routes, not on specific classes.
 * <HR>
 * <!-- Put @see and @since tags down here. -->
 * @see jmri.managers
 * @see jmri.implementation 
*/

//@annotations for the entire package go here

package jmri.jmrit.ussctc;

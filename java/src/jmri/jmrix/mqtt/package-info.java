/**
 * Provides JMRI layout objects that use MQTT connections.
 * <p>
 * Currently just Turnouts.
 * <p>
 * Default system letter is "M".
 * <p>
 * This code isn't invoked currently. To make it available, you could e.g. add
 * the following to setCommandStation in the InstanceManager, though that's a
 * pretty brittle design:  <pre><code>
 *
 * // since there is a command station available,
 * // create a DCC turnout manager and make available
 * if (getList(jmri.jmrix.dcc.DccTurnoutManager.class) == null || getList(jmri.jmrix.dcc.DccTurnoutManager.class).size() == 0) {
 * jmri.jmrix.dcc.DccTurnoutManager m = new jmri.jmrix.dcc.DccTurnoutManager();
 * store(m, jmri.jmrix.dcc.DccTurnoutManager.class);
 * setTurnoutManager(m);
 * }
 * </code></pre>
 *
 * <h2>Related Documentation</h2>
 *
 * For overviews, tutorials, examples, guides, and tool documentation, please
 * see:
 * <ul>
 * <li><a href="http://jmri.org/">JMRI project overview page</a>
 * <li><a href="http://jmri.org/help/en/html/doc/Technical/index.shtml">JMRI
 * project technical info</a>
 * </ul>
 *
 * <!-- Put @see and @since tags down here. -->
 *
 * @since 3.9.6
 */
package jmri.jmrix.mqtt;

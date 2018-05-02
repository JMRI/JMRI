/**
 * The JMRI JSON protocol power server.
 *
 * Replies to any request handled by this server include the following
 * properties in the JSON data object:
 * <dl>
 * <dt>name</dt><dd>The user name of the connection supplying power.</dd>
 * <dt>state</dt><dd>One of
 * {@link jmri.server.json.JSON#UNKNOWN}, {@link jmri.server.json.JSON#ON}, or
 * {@link jmri.server.json.JSON#OFF}</dd>
 * <dt>default</dt><dd>True if this message represents the power state for the
 * default power connection; false otherwise.</dd>
 * </dl>
 *
 * If messages requesting or setting power for this server do not contain the
 * user name of a system connection, this server returns the power state for the
 * default system connection, otherwise it returns the power state for the named
 * system connection.
 */
package jmri.server.json.power;

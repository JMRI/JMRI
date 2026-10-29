/**
 * The JMRI JSON protocol power server.
 *
 * Replies to any request handled by this server include the following
 * properties in the JSON data object:
 * <dl>
 * <dt>name</dt><dd>The user name of the connection supplying power.</dd>
 * <dt>prefix</dt><dd>The system prefix of the connection supplying power
 * (e.g. {@code "L"} for LocoNet, {@code "D"} for DCC-EX).</dd>
 * <dt>state</dt><dd>One of
 * {@link jmri.server.json.JSON#UNKNOWN},
 * {@link jmri.server.json.JSON#ON},
 * {@link jmri.server.json.JSON#OFF}, or
 * {@link jmri.server.json.JSON#IDLE}</dd>
 * <dt>default</dt><dd>True if this message represents the power state for the
 * default power connection; false otherwise.</dd>
 * </dl>
 *
 * Requests may include an optional {@code prefix} field in the data object to
 * target a specific system connection. If {@code prefix} is absent, the default
 * connection is used. An unknown prefix returns an HTTP 400 error. WebSocket
 * clients receive state-change notifications only for the connection they
 * subscribed to; use {@code method:"list"} to subscribe to all connections.
 */
// include empty DefaultAnnotation to avoid excessive recompilation
@edu.umd.cs.findbugs.annotations.DefaultAnnotation(value={})
package jmri.server.json.power;

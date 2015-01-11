/**
 * The JMRI JSON Protocol supports the interrogation and control of a JMRI application.
 * <p>
 * The JMRI JSON protocol supports three different network interfaces:
 * <ul>
 * <li>A standard network socket interface, {@link jmri.jmris.json.JsonServer}, usable by other applications.</li>
 * <li>A RESTFUL web interface, {@link jmri.web.servlet.json.JsonServlet}, which provides a channel for interrogation.</li>
 * <li>A WebSocket interface, {@link jmri.web.servlet.json.JsonServlet}, which allows modern web browsers to be fully featured JMRI clients.</li>
 * </ul>
 *
 * All JMRI JSON protocol messages are in one of the following forms:
 * <ul>
 * <li><code>{"type":<i>string</i>, "data":<i>object</i>}}</code> - a control or single item interrogation message and the response</li>
 * <li><code>{"type":"list", "list":<i>string</i>}}</code> - a request for a list of items</li>
 * <li><code>[<i>one or more comma separated objects</i>]</code> - the array response to a request for a list of items</li>
 * <li><code>{"type":"ping"}</code> - the heartbeat message from the client</li>
 * <li><code>{"type":"pong"}</code> - the response to the client heartbeat</li>
 * <li><code>{"type":"goodbye"}</code> - the message sent to cleanly close either a socket or WebSocket connection</li>
 * </ul>
 */
package jmri.jmris.json;

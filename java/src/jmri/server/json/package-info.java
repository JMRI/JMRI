/**
 * The JMRI JSON Services provide access to JMRI via JSON messages via a RESTful
 * interface over HTTP or a socket interface over TCP or WebSockets.
 * 
 * <h2>Schema</h2>
 * 
 * The JMRI JSON protocol has two sets of schema, one for messages from clients to a JMRI
 * server, and one for messages from a JMRI server to clients. The core schema for socket interfaces are
 * the <a href="">client schema</a> and <a href="">server schema</a>. These schema define
 * the current 
 * 
 * <h2>Methods</h2>
 * 
 * The JMRI JSON services accept four methods, all of which directly correspond
 * to HTTP 1.1 methods. The socket services accept one non-HTTP 1.1 method
 * <code>{@value jmri.server.json.JSON#LIST}</code> to allow the listing of
 * objects (in the HTTP service, this is an HTTP GET). In the socket
 * implementations, the method is specified using the
 * <code>{@value jmri.server.json.JSON#METHOD}</code> name in the message object
 * with one of the following values:
 * <dl>
 * <dt>{@value jmri.server.json.JSON#GET}</dt>
 * <dd>Get the requested object. Absent a
 * <code>{@value jmri.server.json.JSON#METHOD}</code> name, this is the default
 * method. <strong>Note to developers:</strong> this should be an idempotent
 * method.</dd>
 * <dt>{@value jmri.server.json.JSON#POST}</dt>
 * <dd>Modify the requested object.</dd>
 * <dt>{@value jmri.server.json.JSON#PUT}</dt>
 * <dd>Create the requested object. <strong>Note to developers:</strong> this
 * should be an idempotent method if the requested object already exists (i.e.,
 * this request should not modify an existing object if a new object cannot be
 * created).</dd>
 * <dt>{@value jmri.server.json.JSON#DELETE}</dt>
 * <dd>Delete the requested object. <strong>Note to developers:</strong> This
 * should be an idempotent method if the requested object does not exist.</dd>
 * <dt>{@value jmri.server.json.JSON#LIST}</dt>
 * <dd>Used in the socket interfaces to get a list of the given
 * <code>type</code>. The equavalent form in the RESTful interface is a
 * <code>GET</code> request to <code>/json/&lt;type&gt;</code>.</dd>
 * </dl>
 *
 * <h2>Messages</h2>
 * <p>
 * Fundamentally, the JSON server passes messages as JSON Objects between a JMRI
 * server and one or more clients. In the TCP Socket and WebSocket interfaces, a
 * message from the client has three properties:</p>
 * <dl>
 * <dt><code>type</code></dt>
 * <dd></dd>
 * <dt><code>method</code></dt>
 * <dd></dd>
 * <dt><code>data</code></dt>
 * <dd></dd>
 * </dl>
 * <p>
 * A message from the server is either a single JSON Object
 * <code>{"type":"<em>type</em>","method":"<em>method</em>","data":{...}}</code>
 * except as noted below. In the RESTful interface, messages from the client in
 * <em>POST</em> and <em>PUT</em> requests may contain a JSON object that
 * cooresponds to the <code>data</code> object in the socket interfaces.
 * </p>
 * <p>
 * Messages from the JSON server to a client will be in the form
 * <code>{"type":"<em>type</em>","data":{...}}</code> or in the form
 * <code>[<em>message</em>,<em>message</em>,...]</code> except as noted below.
 * 
 * <h3>Exceptions</h3>
 * <p>
 * Exceptions to the above form for the socket interfaces are:
 * </p>
 * <dl>
 * <dt>Gets</dt>
 * <dd>When using a get method, specifying the method is optional.</dd>
 * <dt>Lists</dt>
 * <dd>list requests are in the form:
 * <code>{"type":"<em>type</em>","method":"list","data":{...}}</code>
 * (recommended) or <code>{"type":"list","list":"<em>type</em>"}</code> or
 * <code>{"list":"<em>type</em>"}</code> and will either cause an error to be
 * thrown if <em>type</em> is not listable, or cause an array of items to be
 * sent to the client. The <code>data</code> object is not required; some list
 * requests can have additional parameters in the <code>data</code>; however
 * some services ignore that with lists.</dd>
 * <dt>Heartbeats</dt>
 * <dd>Heartbeats messages are sent by the client to assure the JMRI server that
 * a socket connection is still up. A heartbeat from the client is in the form
 * <code>{"type":"ping"}</code> and is answered with a
 * <code>{"type":"pong"}</code> from the JMRI server.</dd>
 * <dt>Closing Sockets</dt>
 * <dd>When a socket is being closed, in a non-erroneous way, the system
 * initiating the close sends a message <code>{"type":"goodbye"}</code> and
 * closes its connection. The receiving system should not respond and close its
 * connection.</dd>
 * </dl>
 * 
 * <h2>Requests</h2>
 * <p>
 * JSON messages in four different forms from the client are handled by the JSON
 * services:
 * <dl>
 * <dt>
 * <dt>list</dt>
 * <dd>list requests are in the form:
 * <code>{"type":"<em>type</em>","method":"list","data":{...}}</code>
 * (recommended) or <code>{"type":"list","list":"<em>type</em>"}</code> or
 * <code>{"list":"<em>type</em>"}</code> and will either cause an error to be
 * thrown if <em>type</em> is not listable, or cause an array of items to be
 * sent to the client. The <code>data</code> object is not required; some list
 * requests can have additional parameters in the <code>data</code>; however
 * some services ignore that with lists.</dd>
 * <dt>
 * <li>messages concerning individual items in the form:
 * <code>{"type":"<em>type</em>","data":{"name":"<em>name</em>"},"method":"<em>method</em>"}</code>.
 * In addition to the initial response, most requests will initiate listeners,
 * which will send updated responses every time the item changes.
 * <ul>
 * <li>an item may be updated if object attributes are included in the message:
 * <code>{"type":"<em>type</em>","data":{"name":"<em>name</em>",...}, "method": "post"}</code>.
 * For historical reasons, the <em>method</em> node may be included in the
 * <em>data</em> node:
 * <code>{"type":"<em>type</em>","data":{"name":"<em>name</em>","method":"post",...}}</code>
 * Note that this is discouraged and not all types support this.</li>
 * <li>individual types can be created if a <strong>method</strong> node with
 * the value <em>put</em> is included in message:
 * <code>{"type":"<em>type</em>","method":"put","data":{"name":"<em>name</em>"}}</code>.</li>
 * </ul>
 * </li>
 * <li>a heartbeat in the form <code>{"type":"ping"}</code>. The heartbeat gets
 * a <code>{"type":"pong"}</code> response.</li>
 * <li>a sign off in the form: <code>{"type":"goodbye"}</code> to which an
 * identical response is sent before the connection gets closed.</li>
 * </ul>
 * <p>
 * <strong>Note</strong> The <em>name</em> property of a data object
 * <strong>must</strong> be the system name, not the user name, of the requested
 * object (usually a {@link jmri.NamedBean}), except when creating an object
 * using a {@code put} method and the {@link jmri.Manager} for that class of
 * NamedBean supports creating a NamedBean without a system name. It is
 * generally safer to always use system names.
 * </p>
 *
 * <h2>Responses</h2>
 * <p>
 * JSON messages sent to the client will be in the form:
 * <ul>
 * <li><code>{"type":"<em>type</em>","data":{"name":"<em>name</em>",...}}</code>
 * an object, either in response to a request or sent because an object
 * requested earlier was updated within JMRI (updates are not available using
 * the HTTP transport)</li>
 * <li><code>{"type":"pong"}</code> in response to a
 * <code>{"type":"ping"}</code> message.</li>
 * <li>a sign off in the form: <code>{"type":"goodbye"}</code> before the
 * connection gets closed.</li>
 * <li><code>[<em>message</em>,<em>message</em>]</code>, an array of object
 * message types. There is no guarantee that an array contains all objects of a
 * single type, or that an array contains all of the objects of a single type,
 * since it is, by design, possible for multiple services, including third-party
 * services to respond to a single request, and the JSON server neither makes
 * nor enforces any guarantees concerning how those services respond. Multiple
 * responses are joined together when using JSON via HTTP protocol, and may or
 * may not be joined together when using JSON in other protocols.</li>
 * </ul>
 *
 * <h2>Version History</h2>
 * <p>
 * Changes to the major number represent a backwards incompatible change in the
 * protocol, while changes to the minor number represent an addition to the
 * protocol.
 * </p>
 * <dl>
 * <dt>5.0 (JMRI 4.15.3)</dt>
 * <dd>Introduces a backwards-incompatible changes of:
 * <ul>
 * <li>Respecting the requirement that a JSON message object from the client
 * include a {@code method} name to not be treated as a
 * {@value jmri.server.json.JSON#GET} introduced in version 4.1.</li>
 * <li>Removes code that creates listeners for objects not requested by the
 * client</li>
 * <li>Removes support for plural tokens for listing types, using the singular
 * token syntax introduced in version 3.0.</li>
 * </ul>
 * </dd>
 * <dt>4.1 (JMRI 4.11.4)</dt>
 * <dd>The RESTful method associated with a JSON message from the client absent
 * a {@code method} name with a value of "{@value jmri.server.json.JSON#GET}",
 * "{@value jmri.server.json.JSON#POST}", "{@value jmri.server.json.JSON#PUT}",
 * or "{@value jmri.server.json.JSON#DELETE}" is assumed to be
 * "{@value jmri.server.json.JSON#GET}".</dd>
 * <dt>4.0 (JMRI 4.3.4)</dt>
 * <dd>Prior to version 4.0, the JSON servers had a single definition for all
 * tokens used in JSON communications. As of version 4.0, the JSON servers use a
 * modular protocol, fixing as constants only the tokens used for the basic
 * protocol structure as well as some tokens used by multiple modules.</dd>
 * <dt>3.0 (JMRI 3.11.2)</dt>
 * <dd>Types no longer need be plural to list. This means that RESTful URLs can
 * be /json/type/id for a single object and /json/type for a list of objects,
 * and /json/types is no longer needed to list (i.e. /json/turnout/IT1 gets
 * turnout IT1, and /json/turnout gets the list of turnouts).</dd>
 * <dt>2.0 (JMRI 3.9.3)</dt>
 * <dd>No reason for version change listed in commit history.</dd>
 * <dt>1.1 (JMRI 3.7.1)</dt>
 * <dd>No reason for version change listed in commit history.</dd>
 * <dt>1.0 (JMRI 3.4)</dt>
 * <dd>Initial release of JMRI JSON Protocol.</dd>
 * </dl>
 * 
 * <h2>Notes</h2>
 * <p>
 * The JMRI JSON services are defined using {@link jmri.spi.JsonServiceFactory}
 * objects which may be loaded as third-party plug-ins to JMRI (see <a href=
 * "http://jmri.org/help/en/html/doc/Technical/plugins.shtml#service">Plug-in
 * mechanisms</a>). Because of this the JSON server can make no guarantees
 * concerning how the JSON services handling a specific type of object behave;
 * specifically the following are not guaranteed:
 * <ul>
 * <li>An array response to a list request contains all items of the requested
 * type.</li>
 * <li>An array response does not contain duplicate items of the same type with
 * different data.</li>
 * <li>The message sent from a JMRI server is in response to the last message
 * received when using sockets.</li>
 * <li>Requests for an object will cause a listener to be created for that
 * object such that the client is automatically updated when the object or
 * object state changes.</li>
 * <li>Requests for a list will cause a listener to be created that
 * automatically updates the client when objects of a type are added or removed
 * within JMRI.</li>
 * <li>A single service will be the only responder to a specific message.</li>
 * </ul>
 *
 * @since 4.3.4
 * @see jmri.web.servlet.json.JsonServlet
 * @see jmri.jmris.json.JsonServer
 * @see jmri.spi.JsonServiceFactory
 */
package jmri.server.json;

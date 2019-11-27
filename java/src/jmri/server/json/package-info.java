/**
 * The JMRI JSON Services provide access to JMRI via JSON data objects via a
 * RESTful interface over HTTP or via JSON messages via a socket interface over
 * TCP or WebSockets.
 * <h2 id="schema">Schema</h2> The JMRI JSON protocol has two sets of schema,
 * one for messages from clients to a JMRI server, and one for messages from a
 * JMRI server to clients. The core schema for socket interfaces are the
 * <a href=
 * "https://raw.githubusercontent.com/JMRI/JMRI/master/java/src/jmri/server/json/schema/json-client.json">client
 * schema</a> and <a href=
 * "https://raw.githubusercontent.com/JMRI/JMRI/master/java/src/jmri/server/json/schema/json-server.json">server
 * schema</a>. These schema define the structure of JSON messages in this
 * protocol. Messages generally contain {@code type}, {@code data}, and
 * {@code method} properties.
 * <p>
 * The data object types supported in a running JMRI instance for both RESTful
 * and socket interfaces can be listed at <a href=
 * "http://localhost:12080/json/type">http://my-jmri-server:12080/json/type</a>
 * and individual schema for each data object type at <a href=
 * "http://localhost:12080/json/schema/type">http://my-jmri-server:12080/json/schema/<em>type</em></a>
 * <h2 id="methods">Methods</h2> The JMRI JSON services accept four methods, all
 * of which directly correspond to HTTP 1.1 methods. The socket services accept
 * one non-HTTP 1.1 method <code>{@value jmri.server.json.JSON#LIST}</code> to
 * allow the listing of objects (in the HTTP service, this is an HTTP GET). In
 * the socket implementations, the method is specified using the
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
 * <code>type</code>. The equivalent form in the RESTful interface is a
 * <code>GET</code> request to <code>/json/&lt;type&gt;</code>.</dd>
 * </dl>
 * <h2 id="messages">Messages</h2>
 * <p>
 * Fundamentally, the JSON server passes messages as JSON Objects between a JMRI
 * server and one or more clients. In the TCP Socket and WebSocket interfaces, a
 * message from the client generally has three properties (full details are in
 * the schema):
 * </p>
 * <dl>
 * <dt><code>type</code></dt>
 * <dd>The type of object in the <em>data</em> property.</dd>
 * <dt><code>method</code></dt>
 * <dd>The RESTful method to be used, defaults to <em>get</em> if not
 * present.</dd>
 * <dt><code>data</code></dt>
 * <dd>The JSON representation of a JMRI object; see the schema for the
 * properties of this object, since the properties vary by <em>type</em>.</dd>
 * <dt><code>id</code></dt>
 * <dd>A message identity as a positive integer that will be returned unmodified
 * to the client in any messages that are direct responses to the message
 * containing an id. Defaults to zero if not present.</dd>
 * </dl>
 * <p>
 * A message from the server is either a single JSON Object
 * <code>{"type":"<em>type</em>","data":{...}}</code> except as noted below or a
 * JSON Array of objects. In the RESTful interface, messages from the client in
 * <em>POST</em> and <em>PUT</em> requests may contain a JSON object that
 * corresponds to the <code>data</code> object in the socket interfaces.
 * </p>
 * <h3 id="exceptions">Exceptions</h3>
 * <p>
 * Exceptions to the above form for the socket interfaces are:
 * </p>
 * <dl>
 * <dt>Get Requests</dt>
 * <dd>When using a get method, specifying the method is optional.</dd>
 * <dt>List Requests</dt>
 * <dd>List requests are in the form:
 * <code>{"type":"<em>type</em>","method":"list","data":{...}}</code>
 * (recommended) or <code>{"type":"list","list":"<em>type</em>"}</code> or
 * <code>{"list":"<em>type</em>"}</code> in the socket interfaces and will
 * either cause an error to be thrown if <em>type</em> is not listable, or cause
 * an array of items to be sent to the client. The <code>data</code> object is
 * not required; some list requests can have additional parameters in the
 * <code>data</code> object; however some services ignore that with lists.</dd>
 * <dt>List Responses</dt>
 * <dd>List responses are in the form:
 * <code>[<em>message</em>,<em>message</em>]</code>, an array of object message
 * types with {@code type} and {@code data} properties. There is no guarantee
 * that an array contains all objects of a single type, or that an array
 * contains all of the objects of a single type, since it is, by design,
 * possible for multiple services, including third-party services to respond to
 * a single request, and the JSON server neither makes nor enforces any
 * guarantees concerning how those services respond. Multiple responses are
 * joined together when using JSON via the RESTful interface, and may or may not
 * be joined together when using JSON in other interfaces.</dd>
 * <dt>Heartbeats</dt>
 * <dd>Heartbeat messages are sent by the client to assure the JMRI server that
 * a socket connection is still up. A heartbeat from the client is in the form
 * <code>{"type":"ping"}</code> and is answered with a
 * <code>{"type":"pong"}</code> from the JMRI server.</dd>
 * <dt>Closing Sockets</dt>
 * <dd>When a socket is being closed in a non-erroneous way, the system
 * initiating the close (either a client or server) sends a message
 * <code>{"type":"goodbye"}</code> and closes its connection. The receiving
 * system should not respond and close its connection.</dd>
 * <dt>Malformed JSON</dt>
 * <dd>When receiving a message with malformed JSON, the JMRI server will not
 * attempt to include an id if present in the malformed message.</dd>
 * </dl>
 * <p>
 * <strong>Note</strong> The <em>name</em> property of a data object
 * <strong>must</strong> be the system name, not the user name, of the requested
 * object (usually a {@link jmri.NamedBean}), except when creating an object
 * using a {@code put} method and the {@link jmri.Manager} for that class of
 * NamedBean supports creating a NamedBean without a system name. It is
 * generally safer to always use system names.
 * </p>
 * <h2 id="history">Version History</h2>
 * <p>
 * Changes to the major number represent a backwards incompatible change in the
 * protocol, while changes to the minor number represent an addition to the
 * protocol, and changes to patch number represent a bug fix within the
 * protocol. The JSON protocol version is available to clients in the
 * {@code hello} message. Within test releases between to two production
 * releases, breaking changes only reset the major number once (for example,
 * breaking changes in JMRI 4.15.4 to JSON protocol version 4.1 followed by
 * breaking changes to 5.0.0 in 4.15.6 will result in JMRI release 4.16
 * including JSON protocol version 5.0.0, not 6.0.0, since 4.15.4 and 4.15.6 are
 * both between JMRI production releases 4.14 and 4.16).
 * <p>
 * Starting with 5.0.0, the JSON protocol version follows semantic version
 * rules, prior to that the version is just a major.minor version.
 * <dl>
 * <dt>5.1.1 (JMRI 4.17.7)</dt>
 * <dd>
 * <ul>
 * <li>for networkService, add userName and change name to mDNS type</li>
 * <li>Add configProfile.isNextProfile</li>
 * <li>several schema fixes</li>
 * <li>reporter.report now supports Reportable data</li>
 * <li>fix systemConnection, configProfile, panel to return single item for name</li>
 * </ul>
 * </dd>
 * <dt>5.1.0 (JMRI 4.17.4)</dt>
 * <dd>
 * <ul>
 * <li>Fixes setting the car type for operations rolling stock</li>
 * <li>Adds the settable property "carSubType" to include car type information
 * that is not shown on manifests or switch lists</li>
 * </ul>
 * </dd>
 * <dt>5.0.0 (JMRI 4.15.4 with further changes in 4.15.6)</dt>
 * <dd>
 * <ul>
 * <li>Adds the ability for a client to specify a message ID that will be
 * included in messages returned in direct response to the identified
 * message</li>
 * <li>Adds a framework for allowing a client to delete objects and handle
 * conflicts (mostly object is in use elsewhere types) when deleting
 * objects</li>
 * <li>Adds schemas that define valid JSON message for all types except
 * consists; <a href="https://github.com/JMRI/JMRI/issues/5002">#5002</a> is
 * being used to track progress on allowing schema validation at all times.</li>
 * </ul>
 * <p>
 * Introduces backwards-incompatible changes of:
 * <ul>
 * <li>Respecting the requirement that a JSON message object from the client
 * include a {@code method} name to not be treated as a
 * {@value jmri.server.json.JSON#GET} introduced in version 4.1.</li>
 * <li>Removes code that creates listeners for objects not requested by the
 * client</li>
 * <li>JSON Operations objects use <code>name</code> instead of <code>id</code>
 * to match behavior of other JSON objects.</li>
 * <li>JSON Throttle objects use <code>name</code> instead of
 * <code>throttle</code> to match behavior of other JSON objects.</li>
 * <li>Deprecates, but does not remove, support for plural tokens for listing
 * types, using the singular token syntax introduced in version 3.0.</li>
 * </ul>
 * </dd>
 * <dt>4.1 (JMRI 4.11.4)</dt>
 * <dd>The RESTful method associated with a JSON message from the client absent
 * a {@code method} name with a value of {@value jmri.server.json.JSON#GET},
 * {@value jmri.server.json.JSON#POST}, {@value jmri.server.json.JSON#PUT}, or
 * {@value jmri.server.json.JSON#DELETE} is assumed to be
 * {@value jmri.server.json.JSON#GET}.</dd>
 * <dt>4.0 (JMRI 4.3.4)</dt>
 * <dd>Prior to version 4.0, the JSON servers had a single definition for all
 * tokens used in JSON communications. As of version 4.0, the JSON servers use a
 * modular protocol, fixing as constants only the tokens used for the basic
 * protocol structure as well as some tokens used by multiple modules.</dd>
 * <dt>3.0 (JMRI 3.11.2)</dt>
 * <dd>Types no longer need be plural to list. This means that RESTful URLs can
 * be /json/type/name for a single object and /json/type for a list of objects,
 * and /json/types is no longer needed to list (i.e. /json/turnout/IT1 gets
 * turnout IT1, and /json/turnout gets the list of turnouts).</dd>
 * <dt>2.0 (JMRI 3.9.3)</dt>
 * <dd>No reason for version change listed in commit history.</dd>
 * <dt>1.1 (JMRI 3.7.1)</dt>
 * <dd>No reason for version change listed in commit history.</dd>
 * <dt>1.0 (JMRI 3.4)</dt>
 * <dd>Initial release of JMRI JSON Protocol.</dd>
 * </dl>
 * <h2 id="notes">Notes</h2>
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

/**
 * The JMRI JSON Services provide access to JMRI via JSON messages via HTTP or a socket. 
 *
 * <h2>Requests</h2>
 * <p>
 * JSON messages in four different forms from the client are handled by the JSON
 * services:
 * <ul>
 * <li>list requests in the form:
 * <code>{"type":"<em>type</em>","method":"list"}</code> or
 * <code>{"type":"list","list":"<em>type</em>"}</code> or
 * <code>{"list":"<em>type</em>"}</code>.</li>
 * <li>individual item state requests in the form:
 * <code>{"type":"<em>type</em>","data":{"name":"<em>name</em>"}}</code>. In
 * addition to the initial response, most requests will initiate listeners,
 * which will send updated responses every time the item's state changes.<ul>
 * <li>an item may be updated if object attributes are included in the message:
 * <code>{"type":"<em>type</em>","data":{"name":"<em>name</em>",...}}</code>.
 * Optionally a <strong>method</strong> node with the value <em>post</em> is
 * included in the message:
 * <code>{"type":"<em>type</em>","method":"post","data":{"name":"<em>name</em>",...}}</code>.
 * For historical reasons, the <em>method</em> node may be included in the
 * <em>data</em> node:
 * <code>{"type":"<em>type</em>","data":{"name":"<em>name</em>","method":"post",...}}</code>
 * Note that this is discouraged and not all types support this.</li>
 * <li>individual types can be created if a <strong>method</strong> node with
 * the value <em>put</em> is included in message:
 * <code>{"type":"<em>type</em>","method":"put","data":{"name":"<em>name</em>"}}</code>.</li>
 * </ul></li>
 * <li>a heartbeat in the form <code>{"type":"ping"}</code>. The heartbeat gets
 * a <code>{"type":"pong"}</code> response.</li>
 * <li>a sign off in the form: <code>{"type":"goodbye"}</code> to which an
 * identical response is sent before the connection gets closed.</li></ul>
 * <p>
 * <strong>Note</strong> The <em>name</em> property of a data object <strong>must</strong>
 * be the system name, not the user name, of the requested object (usually a {@link jmri.NamedBean}),
 * except when creating an object using a {@code put} method and the {@link jmri.Manager}
 * for that class of NamedBean supports creating a NamedBean without a system name. It
 * is generally safer to always use system names.</p>
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
 * single type, or that an array contains all of the objects of a single
 * type, since it is, by design, possible for multiple services, including third-party
 * services to respond to a single request, and the JSON server neither makes nor
 * enforces any guarantees concerning how those services respond. Multiple responses
 * are joined together when using JSON via HTTP protocol, and may or may not
 * be joined together when using JSON in other protocols.</li>
 * </ul>
 *
 * <h2>Notes</h2>
 * <p>
 * The JMRI JSON services are defined using {@link jmri.spi.JsonServiceFactory} objects
 * which may be loaded as third-party plug-ins to JMRI (see
 * <a href="http://jmri.org/help/en/html/doc/Technical/plugins.shtml#service">Plug-in mechanisms</a>).
 * Because of this the JSON server can make no guarantees concerning how the JSON services
 * handling a specific type of object behave; specifically the following are not guaranteed:
 * <ul>
 * <li>An array response to a list request contains all items of the requested type.</li>
 * <li>An array response does not contain duplicate items of the same type with different data.</li>
 * <li>The message sent from a JMRI server is in response to the last message received when using sockets.</li>
 * <li>Requests for an object will cause a listener to be created for that object such that the
 * client is automatically updated when the object or object state changes.</li>
 * <li>Requests for a list will cause a listener to be created that automatically updates the client
 * when objects of a type are added or removed within JMRI.</li>
 * <li>A single service will be the only responder to a specific message.</li>
 * </ul>
 *
 * @since 4.3.4
 * @see jmri.web.servlet.json.JsonServlet
 * @see jmri.jmris.json.JsonServer
 * @see jmri.spi.JsonServiceFactory
 */
package jmri.server.json;

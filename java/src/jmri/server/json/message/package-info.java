/**
 * The JMRI JSON protocol message server.
 *
 * JMRI is capable of sending human readable messages, solicited or unsolicited,
 * to JMRI JSON clients devices. Examples of such messages might status updates
 * for a lengthy process that was initiated through a web service or changes in
 * the status of a connection. This server provides mechanisms for subscribing
 * to, and sending, such messages within the JSON streaming service. JSON HTTP
 * clients are not supported.
 * <p>
 * This server subscribes a client for client-specific messages if the
 * {@code client} key is included in the {@code data} element of a {@code hello}
 * message or {@code client} message with a {@code put} or {@code post} method.
 * A client can cancel its subscription by sending a {@code client} message with
 * the {@code client} data key of the {@code remove} type. A single client can
 * subscribe as many times as it wants to by sending different values for the
 * client key each time (note that such a client will receive multiple messages
 * for every message broadcast to all clients).
 * <p>
 * A client can request that JMRI create a client code on its behalf by sending
 * a {@code client} message with a {@code get} method (no data object is
 * required). If requesting a JMRI-generated code, JMRI provides an existing
 * client-generated code, if one is already subscribed, or generates a UUID on
 * behalf of the requester. JMRI will return the same UUID for a single
 * connection until that UUID is subscription is canceled by the client.
 * <p>
 * Examples:
 * <ul>
 * <li>{@code {"type": "client", "data": {"client": "42"}, "method": "put"}} to
 * subscribe</li>
 * <li>{@code {"type": "client", "data": {"client": "42"}, "method": "delete"}}
 * to cancel a subscription</li>
 * <li>{@code {"type": "client", "method": "get"}} to request a JMRI-generated
 * client identity</li>
 * </ul>
 * <p>
 * Messages sent by this server include the following properties in the JSON
 * data object:
 * <dl>
 * <dt>context</dt><dd>An JSON object, previously sent by the client as part of
 * some other transaction to be included with the message so the client knows
 * why it received the message. This is null if no context was sent by the
 * client, or if the context sent was not a JSON object.</dd>
 * <dt>message</dt><dd>The message text.</dd>
 * <dt>type</dt><dd>One of {@value jmri.server.json.message.JsonMessage#INFO},
 * {@value jmri.server.json.message.JsonMessage#SUCCESS},
 * {@value jmri.server.json.message.JsonMessage#WARNING}, or
 * {@value jmri.server.json.message.JsonMessage#ERROR}</dd>
 * <dt>locale</dt><dd>The message locale, which may differ from both the
 * client's locale and the JMRI server's locale.</dd>
 * </dl>
 */
package jmri.server.json.message;

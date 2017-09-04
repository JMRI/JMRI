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
 * Examples:
 * <ul>
 * <li>{@code {"type": "client", "data": {"client": "42"}, "method": "put"}} to
 * subscribe</li>
 * <li>{@code {"type": "client", "data": {"client": "42"}, "method": "delete"}}
 * to cancel a subscription</li>
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
 * <dt>type</dt><dd>One of {@value JsonMessage#INFO}, {@value JsonMessage#SUCCESS},
 * {@value JsonMessage#WARNING}, or {@value JsonMessage#ERROR}</dd>
 * <dt>locale</dt><dd>The message locale, which may differ from both the
 * client's locale and the JMRI server's locale.</dd>
 * </dl>
 */
package jmri.server.json.message;

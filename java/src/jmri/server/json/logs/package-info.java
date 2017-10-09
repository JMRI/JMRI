/**
 * The JMRI JSON protocol log server.
 *
 * Sends logs in the Logstash JSON format to interested clients. It is expected
 * that the client will perform any desired filtered.
 *
 * This server responds to the following messages:
 * <dl>
 * <dt>{@literal {"type":"logs", "data":{"state":2}} }
 * <dd>Start sending live logs to the requesting client. All log entries from
 * the start of the JMRI application to be sent to the client if logs are not
 * already being sent to the client.
 * <dt>{@literal {"type":"logs", "data":{"state":4}} }
 * <dd>Stop sending live logs to the requesting client.
 * <dt>{@literal {"type":"list", "list":"logs"} }
 * <dd>Send all log entries from the start of the JMRI application without
 * starting live logs.
 * </dl>
 * <p>
 * Note that when listing logs, all logs are sent as individual messages, not in
 * a JSON array.
 */
package jmri.server.json.logs;

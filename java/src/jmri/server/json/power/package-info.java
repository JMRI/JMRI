/**
 * The JMRI JSON protocol power server.
 *
 * If messages requesting or setting power for this server do not contain the
 * user name of a system connection, this server returns the power state for the
 * default system connection, otherwise it returns the power state for the named
 * system connection.
 */
package jmri.server.json.power;

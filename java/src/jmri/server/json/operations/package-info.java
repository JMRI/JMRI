/**
 * JMRI JSON support for Operations.
 * <p>
 * See {@link jmri.server.json} for general information concerning how the JMRI
 * JSON support works.
 * <h2>Notes</h2>
 * <h3>Rolling Stock</h3>
 * <h4>Car Types</h4> JMRI Operations maintains a text property {@code type} for
 * rolling stock; this property is divided into two parts with a hyphen, where
 * only the first part is displayed in manifests and switch lists, and the
 * second part provides additional detail in support of operations automation.
 * <p>
 * In the JSON representation of rolling stock
 * {@value jmri.server.json.operations.JsonOperations#CAR_TYPE} matches just the
 * part of the {@code type} that is displayed;
 * {@value jmri.server.json.operations.JsonOperations#CAR_SUB_TYPE} contains the
 * second {@code type} part or is null if there is no second part. Note that the
 * car types listed when requesting a list of all car types include both parts
 * in a single string.
 * <p>
 * When setting the {@code type} for a piece of rolling stock using a PUT or
 * POST message, include the entire type, both parts separated with a hyphen, in
 * the {@value jmri.server.json.operations.JsonOperations#CAR_TYPE} property.
 * Using {@value jmri.server.json.operations.JsonOperations#CAR_SUB_TYPE} in a
 * message from the client is not schema valid.
 * <p>
 * Note that a future version (likely version 6) of the JMRI JSON protocol will
 * include the entire type in the
 * {@value jmri.server.json.operations.JsonOperations#CAR_TYPE} property, so
 * that the property in a rolling stock entry will match the types listed when
 * getting a list of in use types.
 * 
 * @see jmri.server.json
 */
package jmri.server.json.operations;
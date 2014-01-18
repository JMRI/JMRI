//SimpleOperationsServer.java
package jmri.jmris.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.ArrayList;
import javax.management.Attribute;
import jmri.JmriException;
import jmri.jmris.AbstractOperationsServer;
import jmri.jmris.JmriConnection;
import static jmri.jmris.json.JSON.CABOOSE;
import static jmri.jmris.json.JSON.CARS;
import static jmri.jmris.json.JSON.CODE;
import static jmri.jmris.json.JSON.DATA;
import static jmri.jmris.json.JSON.ERROR;
import static jmri.jmris.json.JSON.LEAD_ENGINE;
import static jmri.jmris.json.JSON.LENGTH;
import static jmri.jmris.json.JSON.LOCATION;
import static jmri.jmris.json.JSON.MESSAGE;
import static jmri.jmris.json.JSON.OPERATIONS;
import static jmri.jmris.json.JSON.STATUS;
import static jmri.jmris.json.JSON.TERMINATE;
import static jmri.jmris.json.JSON.TRAIN;
import static jmri.jmris.json.JSON.TYPE;
import static jmri.jmris.json.JSON.WEIGHT;
import jmri.jmrit.operations.trains.Train;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple interface between the JMRI operations and a network connection
 *
 * @author Paul Bender Copyright (C) 2010
 * @author Dan Boudreau Copyright (C) 2012 (Documented the code, changed reply
 * format, and some minor refactoring)
 * @version $Revision: 21313 $
 */
public class JsonOperationsServer extends AbstractOperationsServer {

    private final JmriConnection connection;
    private final ObjectMapper mapper;
    static Logger log = LoggerFactory.getLogger(JsonOperationsServer.class);

    public JsonOperationsServer(JmriConnection connection) {
        super();
        this.connection = connection;
        this.mapper = new ObjectMapper();
    }

    /*
     * Protocol Specific Simple Functions
     */
    /**
     * Send a JSON object to the connection. The object is built from a set of
     * attributes.
     *
     * @param contents is the ArrayList of Attributes to be sent.
     * @throws java.io.IOException
     */
    @Override
    public void sendMessage(ArrayList<Attribute> contents) throws IOException {
        ObjectNode root = this.mapper.createObjectNode();
        root.put(TYPE, OPERATIONS);
        ObjectNode data = root.putObject(DATA);
        for (Attribute attr : contents) {
            if (attr.getValue() != null) {
                data.put(attr.getName(), attr.getValue().toString());
            } else {
                data.putNull(attr.getName());
            }
        }
        this.connection.sendMessage(this.mapper.writeValueAsString(root));
    }

    /**
     * Constructs an error message and sends it to the client as a JSON message
     *
     * @param errorStatus is the error message. It need not include any padding
     * - this method will add it. It should be plain text.
     * @throws IOException if there is a problem sending the error message
     */
    @Override
    public void sendErrorStatus(String errorStatus) throws IOException {
        ObjectNode root = this.mapper.createObjectNode();
        root.put(TYPE, ERROR);
        ObjectNode data = root.putObject(ERROR);
        data.put(CODE, -1);
        data.put(MESSAGE, errorStatus);
        this.connection.sendMessage(this.mapper.writeValueAsString(root));
    }

    /**
     * Parse a string into a JSON structure and then parse the data node for
     * operations commands
     *
     * @param statusString
     * @throws jmri.JmriException
     * @throws java.io.IOException
     */
    @Override
    public void parseStatus(String statusString) throws JmriException, IOException {
        this.parseRequest(this.mapper.readTree(statusString).path(DATA));
    }

    /**
     * Respond to an operations request.
     *
     * Note that unlike the SimpleOperationsServer, this server will not list
     * anything, but relies on the JsonClientHandler to handle requests for
     * lists of operations data on its behalf.
     *
     * @param data
     * @throws JmriException
     * @throws IOException
     */
    public void parseRequest(JsonNode data) throws JmriException, IOException {
        ArrayList<Attribute> response = new ArrayList<Attribute>();
        if (!data.path(TRAIN).isMissingNode() && !data.path(TRAIN).isNull()) {
            String train = data.path(TRAIN).asText();
            response.add(new Attribute(TRAIN, train));
            if (!data.path(LENGTH).isMissingNode()) {
                response.add(new Attribute(LENGTH, this.constructTrainLength(train)));
            }
            if (!data.path(WEIGHT).isMissingNode()) {
                response.add(new Attribute(WEIGHT, this.constructTrainWeight(train)));
            }
            if (!data.path(CARS).isMissingNode()) {
                response.add(new Attribute(CARS, this.constructTrainNumberOfCars(train)));
            }
            if (!data.path(LEAD_ENGINE).isMissingNode()) {
                response.add(new Attribute(LEAD_ENGINE, this.constructTrainLeadLoco(train)));
            }
            if (!data.path(CABOOSE).isMissingNode()) {
                response.add(new Attribute(CABOOSE, this.constructTrainCaboose(train)));
            }
            if (!data.path(STATUS).isMissingNode()) {
                response.add(new Attribute(STATUS, this.constructTrainStatus(train)));
            }
            if (!data.path(TERMINATE).isMissingNode()) {
                response.add(new Attribute(TERMINATE, this.terminateTrain(train)));
            }
            if (!data.path(LOCATION).isMissingNode()) {
                if (data.path(LOCATION).isNull()) {
                    response.add(new Attribute(LOCATION, this.constructTrainLocation(train)));
                } else {
                    response.add(new Attribute(LOCATION, this.setTrainLocation(train, data.path(LOCATION).asText())));
                }
            }
            if (response.size() > 1) {
                this.sendMessage(response);
            }
        } else {
            this.sendErrorStatus(Bundle.getMessage("ErrorTrainAttribute"));
        }
    }

    @Override
    public void sendTrainList() {
        try {
            try {
                this.connection.sendMessage(this.mapper.writeValueAsString(JsonUtil.getTrains()));
            } catch (JsonException ex) {
                this.connection.sendMessage(this.mapper.writeValueAsString(ex.getJsonMessage()));
            }
        } catch (IOException ex) {
            try {
                this.connection.close();
            } catch (IOException e1) {
                log.warn("Unable to close connection.", e1);
            }
            log.warn("Unable to send message, closing connection.", ex);
        }
    }

    @Override
    public void sendLocationList() {
        throw new UnsupportedOperationException("Overridden but unsupported method"); // NOI18N
    }

    @Override
    public void sendFullStatus(Train train) throws IOException {
        try {
            this.connection.sendMessage(this.mapper.writeValueAsString(JsonUtil.getTrain(train.getId())));
        } catch (JsonException ex) {
            this.connection.sendMessage(this.mapper.writeValueAsString(ex.getJsonMessage()));
        }
    }
}

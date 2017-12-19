package jmri.server.json.consist;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import jmri.ConsistListListener;
import jmri.ConsistListener;
import jmri.LocoAddress;
import jmri.DccLocoAddress;
import jmri.JmriException;
import jmri.jmrit.consisttool.ConsistFile;
import jmri.server.json.JSON;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonException;
import jmri.server.json.JsonSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Randall Wood Copyright (C) 2016
 */
public class JsonConsistSocketService extends JsonSocketService {

    private final JsonConsistHttpService service;
    private final HashSet<DccLocoAddress> consists = new HashSet<>();
    private Locale locale;
    private final JsonConsistListener consistListener = new JsonConsistListener();
    private final JsonConsistListListener consistListListener = new JsonConsistListListener();
    private final static Logger log = LoggerFactory.getLogger(JsonConsistSocketService.class);

    public JsonConsistSocketService(JsonConnection connection) {
        super(connection);
        this.service = new JsonConsistHttpService(connection.getObjectMapper());
        this.service.manager.addConsistListListener(this.consistListListener);
    }

    @Override
    public void onMessage(String type, JsonNode data, Locale locale) throws IOException, JmriException, JsonException {
        this.locale = locale;
        if (JsonConsist.CONSISTS.equals(type)) {
            this.connection.sendMessage(this.service.doGetList(type, locale));
        } else {
            DccLocoAddress address = new DccLocoAddress(data.path(JSON.ADDRESS).asInt(), data.path(JSON.IS_LONG_ADDRESS).asBoolean());
            String name = address.getNumber() + (address.isLongAddress() ? "L" : "");
            if (data.path(JSON.METHOD).asText().equals(JSON.PUT)) {
                this.connection.sendMessage(this.service.doPut(type, name, data, locale));
            } else {
                this.connection.sendMessage(this.service.doPost(type, name, data, locale));
            }
            if (!this.consists.contains(address)) {
                this.service.manager.getConsist(address).addConsistListener(this.consistListener);
                this.consists.add(address);
            }
        }
    }

    @Override
    public void onList(String type, JsonNode data, Locale locale) throws IOException, JmriException, JsonException {
        this.locale = locale;
        this.connection.sendMessage(this.service.doGetList(type, locale));
    }

    @Override
    public void onClose() {
        this.consists.stream().forEach((address) -> {
            this.service.manager.getConsist(address).removeConsistListener(this.consistListener);
        });
        this.consists.clear();
        this.service.manager.removeConsistListListener(this.consistListListener);
    }

    private class JsonConsistListener implements ConsistListener {

        @Override
        public void consistReply(LocoAddress locoaddress, int status) {
            try {
                try {
                    connection.sendMessage(service.getConsist(locale, (DccLocoAddress) locoaddress));
                } catch (JsonException ex) {
                    connection.sendMessage(ex.getJsonMessage());
                }
            } catch (IOException ex) {
                // this IO execption caused by broken comms with client
                service.manager.getConsist(locoaddress).removeConsistListener(this);
                consists.remove((DccLocoAddress) locoaddress);
            }
            try {
                (new ConsistFile()).writeFile(service.manager.getConsistList());
            } catch (IOException ex) {
                // this IO execption caused by unable to write file
                log.error("Unable to write consist file \"{}\": {}", ConsistFile.defaultConsistFilename(), ex);
            }
        }
    }

    private class JsonConsistListListener implements ConsistListListener {

        @Override
        public void notifyConsistListChanged() {
            try {
                try {
                    connection.sendMessage(service.doGetList(JsonConsist.CONSISTS, locale));
                } catch (JsonException ex) {
                    connection.sendMessage(ex.getJsonMessage());
                }
            } catch (IOException ex) {
                // this IO execption caused by broken comms with client
                service.manager.removeConsistListListener(this);
            }
            try {
                (new ConsistFile()).writeFile(service.manager.getConsistList());
            } catch (IOException ex) {
                // this IO execption caused by unable to write file
                log.error("Unable to write consist file \"{}\": {}", ConsistFile.defaultConsistFilename(), ex);
            }
        }
    }
}

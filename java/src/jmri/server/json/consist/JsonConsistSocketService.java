package jmri.server.json.consist;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import jmri.ConsistListListener;
import jmri.ConsistListener;
import jmri.DccLocoAddress;
import jmri.JmriException;
import jmri.LocoAddress;
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
public class JsonConsistSocketService extends JsonSocketService<JsonConsistHttpService> {

    private final HashSet<LocoAddress> consists = new HashSet<>();
    private final JsonConsistListener consistListener = new JsonConsistListener();
    private final JsonConsistListListener consistListListener = new JsonConsistListListener();
    private final static Logger log = LoggerFactory.getLogger(JsonConsistSocketService.class);

    public JsonConsistSocketService(JsonConnection connection) {
        super(connection, new JsonConsistHttpService(connection.getObjectMapper()));
        this.service.manager.addConsistListListener(this.consistListListener);
    }

    @Override
    public void onMessage(String type, JsonNode data, String method, Locale locale, int id) throws IOException, JmriException, JsonException {
        this.setLocale(locale);
        if (JsonConsist.CONSISTS.equals(type)) {
            this.connection.sendMessage(this.service.doGetList(type, data, locale, id), id);
        } else {
            DccLocoAddress address = new DccLocoAddress(data.path(JSON.ADDRESS).asInt(), data.path(JSON.IS_LONG_ADDRESS).asBoolean());
            String name = address.getNumber() + (address.isLongAddress() ? "L" : "");
            if (method.equals(JSON.PUT)) {
                this.connection.sendMessage(this.service.doPut(type, name, data, locale, id), id);
            } else {
                this.connection.sendMessage(this.service.doPost(type, name, data, locale, id), id);
            }
            if (!this.consists.contains(address)) {
                this.service.manager.getConsist(address).addConsistListener(this.consistListener);
                this.consists.add(address);
            }
        }
    }

    @Override
    public void onList(String type, JsonNode data, Locale locale, int id) throws IOException, JmriException, JsonException {
        this.setLocale(locale);
        this.connection.sendMessage(this.service.doGetList(type, data, locale, id), id);
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
                    connection.sendMessage(service.getConsist(getLocale(), locoaddress, 0), 0);
                } catch (JsonException ex) {
                    connection.sendMessage(ex.getJsonMessage(), 0);
                }
            } catch (IOException ex) {
                // this IO execption caused by broken comms with client
                service.manager.getConsist(locoaddress).removeConsistListener(this);
                consists.remove(locoaddress);
            }
            try {
                (new ConsistFile()).writeFile(service.manager.getConsistList());
            } catch (IOException ex) {
                // this IO execption caused by unable to write file
                log.error("Unable to write consist file \"{}\"", ConsistFile.defaultConsistFilename(), ex);
            }
        }
    }

    private class JsonConsistListListener implements ConsistListListener {

        @Override
        public void notifyConsistListChanged() {
            try {
                try {
                    connection.sendMessage(service.doGetList(JsonConsist.CONSISTS,
                            service.getObjectMapper().createObjectNode(), getLocale(), 0), 0);
                } catch (JsonException ex) {
                    connection.sendMessage(ex.getJsonMessage(), 0);
                }
            } catch (IOException ex) {
                // this IO exception caused by broken communications with client
                service.manager.removeConsistListListener(this);
            }
            try {
                (new ConsistFile()).writeFile(service.manager.getConsistList());
            } catch (IOException ex) {
                // this IO exception caused by unable to write file
                log.error("Unable to write consist file \"{}\"", ConsistFile.defaultConsistFilename(), ex);
            }
        }
    }
}

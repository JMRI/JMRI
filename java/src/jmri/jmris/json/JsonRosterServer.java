package jmri.jmris.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Locale;
import jmri.jmris.JmriConnection;
import static jmri.jmris.json.JSON.NAME;
import static jmri.jmris.json.JSON.ROSTER;
import static jmri.jmris.json.JSON.TYPE;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listen for changes in the roster and notify subscribed clients of the change.
 *
 * @author Randall Wood Copyright (C) 2014
 */
public class JsonRosterServer {

    private final JmriConnection connection;
    private final ObjectMapper mapper;
    private final static Logger log = LoggerFactory.getLogger(JsonRosterServer.class);
    private final JsonRosterListener rosterListener = new JsonRosterListener();

    public JsonRosterServer(JmriConnection connection) {
        this.connection = connection;
        this.mapper = new ObjectMapper();
    }

    public void listen() {
        Roster.instance().addPropertyChangeListener(this.rosterListener);
    }

    public void parseRosterEntryRequest(Locale locale, JsonNode data) throws IOException, JsonException {
        this.connection.sendMessage(this.mapper.writeValueAsString(JsonUtil.getRosterEntry(locale, data.path(NAME).asText())));
    }

    public void dispose() {
        Roster.instance().removePropertyChangeListener(this.rosterListener);
    }

    private class JsonRosterListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            ObjectNode root = mapper.createObjectNode().put(TYPE, ROSTER);
            try {
                // two events not explicitly handled: REMOVE (remove entry), CHANGE (entry id changed)
                if (evt.getPropertyName().equals(Roster.ADD)) {
                    connection.sendMessage(mapper.writeValueAsString(JsonUtil.getRosterEntry(connection.getLocale(),
                            ((RosterEntry) evt.getNewValue()).getId())));
                } else if (evt.getPropertyName().equals(Roster.ROSTER_GROUP_ADDED)
                        || evt.getPropertyName().equals(Roster.ROSTER_GROUP_REMOVED)
                        || evt.getPropertyName().equals(Roster.ROSTER_GROUP_RENAMED)) {
                    connection.sendMessage(mapper.writeValueAsString(JsonUtil.getRosterGroups(connection.getLocale())));
                } else if (!evt.getPropertyName().equals(Roster.SAVED)) {
                    // catch all events other than SAVED
                    connection.sendMessage(mapper.writeValueAsString(JsonUtil.getRoster(connection.getLocale(), root)));
                }
            } catch (IOException ex) {
                Roster.instance().removePropertyChangeListener(this);
            }
        }
    }

}

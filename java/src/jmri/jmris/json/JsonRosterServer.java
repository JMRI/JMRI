package jmri.jmris.json;

import static jmri.jmris.json.JSON.ADD;
import static jmri.jmris.json.JSON.DATA;
import static jmri.jmris.json.JSON.NAME;
import static jmri.jmris.json.JSON.ROSTER;
import static jmri.jmris.json.JSON.TYPE;
import static jmri.jmrit.roster.Roster.REMOVE;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Locale;
import jmri.jmris.JmriConnection;
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
    private final ObjectMapper mapper = new ObjectMapper();
    private final static Logger log = LoggerFactory.getLogger(JsonRosterServer.class);
    private final JsonRosterListener rosterListener = new JsonRosterListener();
    private final JsonRosterEntryListener rosterEntryListener = new JsonRosterEntryListener();
    private boolean listening = false;

    public JsonRosterServer(JmriConnection connection) {
        this.connection = connection;
    }

    public void listen() {
        if (!this.listening) {
            Roster.instance().addPropertyChangeListener(this.rosterListener);
            for (RosterEntry re : Roster.instance().getEntriesInGroup(Roster.ALLENTRIES)) {
                re.addPropertyChangeListener(this.rosterEntryListener);
            }
            this.listening = true;
        }
    }

    public void parseRosterEntryRequest(Locale locale, JsonNode data) throws IOException, JsonException {
        this.connection.sendMessage(this.mapper.writeValueAsString(JsonUtil.getRosterEntry(locale, data.path(NAME).asText())));
        this.listen();
    }

    public void parseRosterGroupRequest(Locale locale, JsonNode data) throws IOException, JsonException {
        this.connection.sendMessage(this.mapper.writeValueAsString(JsonUtil.getRosterGroup(locale, data.path(NAME).asText())));
        this.listen();
    }

    public void dispose() {
        Roster.instance().removePropertyChangeListener(this.rosterListener);
        for (RosterEntry re : Roster.instance().getEntriesInGroup(Roster.ALLENTRIES)) {
            re.removePropertyChangeListener(this.rosterEntryListener);
        }
        this.listening = false;
    }

    private class JsonRosterEntryListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            try {
                if (evt.getPropertyName().equals(RosterEntry.ID)) {
                    // send old roster entry and new roster entry to client as roster changes
                    ObjectNode root = mapper.createObjectNode().put(TYPE, ROSTER);
                    ObjectNode data = root.putObject(DATA);
                    RosterEntry old = new RosterEntry((RosterEntry) evt.getSource(), (String) evt.getOldValue());
                    data.put(ADD, JsonUtil.getRosterEntry(connection.getLocale(), (String) evt.getNewValue()));
                    data.put(REMOVE, JsonUtil.getRosterEntry(connection.getLocale(), old));
                    connection.sendMessage(mapper.writeValueAsString(root));
                } else if (!evt.getPropertyName().equals(RosterEntry.DATE_UPDATED)
                        && !evt.getPropertyName().equals(RosterEntry.FILENAME)) {
                    log.debug("Triggering change on {} ({} => {})", evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                    connection.sendMessage(mapper.writeValueAsString(JsonUtil.getRosterEntry(connection.getLocale(), (RosterEntry) evt.getSource())));
                }
            } catch (IOException ex) {
                dispose();
            }
        }
    }

    private class JsonRosterListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            ObjectNode root = mapper.createObjectNode().put(TYPE, ROSTER);
            try {
                if (evt.getPropertyName().equals(Roster.ROSTER_GROUP_ADDED)
                        || evt.getPropertyName().equals(Roster.ROSTER_GROUP_REMOVED)
                        || evt.getPropertyName().equals(Roster.ROSTER_GROUP_RENAMED)) {
                    connection.sendMessage(mapper.writeValueAsString(JsonUtil.getRosterGroups(connection.getLocale())));
                } else if (evt.getPropertyName().equals(Roster.ADD)) {
                    root.putObject(DATA).put(ADD, JsonUtil.getRosterEntry(connection.getLocale(), (RosterEntry) evt.getNewValue()));
                    ((RosterEntry) evt.getNewValue()).addPropertyChangeListener(rosterEntryListener);
                    connection.sendMessage(mapper.writeValueAsString(root));
                } else if (evt.getPropertyName().equals(Roster.REMOVE)) {
                    root.putObject(DATA).put(REMOVE, JsonUtil.getRosterEntry(connection.getLocale(), (RosterEntry) evt.getOldValue()));
                    connection.sendMessage(mapper.writeValueAsString(root));
                } else if (!evt.getPropertyName().equals(Roster.SAVED)
                        || evt.getPropertyName().equals(Roster.CHANGE)) {
                    // catch all events other than SAVED
                    connection.sendMessage(mapper.writeValueAsString(JsonUtil.getRoster(connection.getLocale(), root)));
                }
            } catch (IOException ex) {
                dispose();
            }
        }
    }

}

package jmri.server.json.roster;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import jmri.JmriException;
import jmri.beans.PropertyChangeProvider;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import static jmri.server.json.JSON.ADD;
import static jmri.server.json.JSON.DATA;
import static jmri.server.json.JSON.DELETE;
import static jmri.server.json.JSON.GET;
import static jmri.server.json.JSON.METHOD;
import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.POST;
import static jmri.server.json.JSON.PUT;
import static jmri.server.json.JSON.REMOVE;
import static jmri.server.json.JSON.TYPE;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonException;
import jmri.server.json.JsonSocketService;
import static jmri.server.json.roster.JsonRosterServiceFactory.ROSTER;
import static jmri.server.json.roster.JsonRosterServiceFactory.ROSTER_ENTRY;
import static jmri.server.json.roster.JsonRosterServiceFactory.ROSTER_GROUP;
import static jmri.server.json.roster.JsonRosterServiceFactory.ROSTER_GROUPS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listen for changes in the roster and notify subscribed clients of the change.
 *
 * @author Randall Wood Copyright (C) 2014, 2016
 */
public class JsonRosterSocketService extends JsonSocketService {

    private final static Logger log = LoggerFactory.getLogger(JsonRosterSocketService.class);
    private final JsonRosterListener rosterListener = new JsonRosterListener();
    private final JsonRosterEntryListener rosterEntryListener = new JsonRosterEntryListener();
    private final JsonRosterHttpService service;
    private boolean listening = false;

    public JsonRosterSocketService(JsonConnection connection) {
        super(connection);
        this.service = new JsonRosterHttpService(connection.getObjectMapper());
    }

    public void listen() {
        if (!this.listening) {
            Roster.getDefault().addPropertyChangeListener(this.rosterListener);
            Roster.getDefault().getEntriesInGroup(Roster.ALLENTRIES).stream().forEach((re) -> {
                re.addPropertyChangeListener(this.rosterEntryListener);
            });
            this.listening = true;
        }
    }

    @Override
    public void onMessage(String type, JsonNode data, Locale locale) throws IOException, JmriException, JsonException {
        String method = data.path(METHOD).asText();
        switch (method) {
            case DELETE:
                throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, Bundle.getMessage("DeleteNotAllowed", type));
            case POST:
            case PUT:
                throw new JsonException(HttpServletResponse.SC_NOT_IMPLEMENTED, Bundle.getMessage("MethodNotImplemented", method, type));
            case GET:
            default:
                switch (type) {
                    case ROSTER:
                        this.connection.sendMessage(this.service.getRoster(locale, data));
                        break;
                    case ROSTER_ENTRY:
                        this.connection.sendMessage(this.service.getRosterEntry(locale, data.path(NAME).asText()));
                        break;
                    case ROSTER_GROUP:
                        this.connection.sendMessage(this.service.getRosterGroup(locale, data.path(NAME).asText()));
                        break;
                    case ROSTER_GROUPS:
                        this.connection.sendMessage(this.service.getRosterGroups(locale));
                        break;
                    default:
                        throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Bundle.getMessage("ErrorUnknownType", type));
                }
                break;
        }
        this.listen();
    }

    @Override
    public void onList(String type, JsonNode data, Locale locale) throws IOException, JmriException, JsonException {
        this.connection.sendMessage(service.doGetList(type, locale));
        this.listen();
    }

    @Override
    public void onClose() {
        Roster.getDefault().removePropertyChangeListener(this.rosterListener);
        Roster.getDefault().getEntriesInGroup(Roster.ALLENTRIES).stream().forEach((re) -> {
            re.removePropertyChangeListener(this.rosterEntryListener);
        });
        this.listening = false;
    }

    private class JsonRosterEntryListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            try {
                if (evt.getPropertyName().equals(RosterEntry.ID)) {
                    // send old roster entry and new roster entry to client as roster changes
                    ObjectNode root = connection.getObjectMapper().createObjectNode().put(TYPE, ROSTER);
                    ObjectNode data = root.putObject(DATA);
                    RosterEntry old = new RosterEntry((RosterEntry) evt.getSource(), (String) evt.getOldValue());
                    data.put(ADD, service.getRosterEntry(connection.getLocale(), (RosterEntry) evt.getSource()));
                    data.put(REMOVE, service.getRosterEntry(connection.getLocale(), old));
                    connection.sendMessage(root);
                } else if (!evt.getPropertyName().equals(RosterEntry.DATE_UPDATED)
                        && !evt.getPropertyName().equals(RosterEntry.FILENAME)) {
                    log.debug("Triggering change on {} ({} => {})", evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                    connection.sendMessage(service.getRosterEntry(connection.getLocale(), (RosterEntry) evt.getSource()));
                }
            } catch (IOException ex) {
                onClose();
            }
        }
    }

    private class JsonRosterListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            ObjectNode root = connection.getObjectMapper().createObjectNode().put(TYPE, ROSTER);
            try {
                if (evt.getPropertyName().equals(Roster.ROSTER_GROUP_ADDED)
                        || evt.getPropertyName().equals(Roster.ROSTER_GROUP_REMOVED)
                        || evt.getPropertyName().equals(Roster.ROSTER_GROUP_RENAMED)) {
                    try {
                        connection.sendMessage(service.getRosterGroups(connection.getLocale()));
                    } catch (JsonException ex) {
                        connection.sendMessage(ex.getJsonMessage());
                    }
                } else if (evt.getPropertyName().equals(Roster.ADD)) {
                    root.putObject(DATA).put(ADD, service.getRosterEntry(connection.getLocale(), (RosterEntry) evt.getNewValue()));
                    ((PropertyChangeProvider) evt.getNewValue()).addPropertyChangeListener(rosterEntryListener);
                    connection.sendMessage(root);
                } else if (evt.getPropertyName().equals(Roster.REMOVE)) {
                    root.putObject(DATA).put(REMOVE, service.getRosterEntry(connection.getLocale(), (RosterEntry) evt.getOldValue()));
                    connection.sendMessage(root);
                } else if (!evt.getPropertyName().equals(Roster.SAVED)
                        || evt.getPropertyName().equals(Roster.CHANGE)) {
                    // catch all events other than SAVED
                    connection.sendMessage(service.getRoster(connection.getLocale(), root));
                }
            } catch (IOException ex) {
                onClose();
            }
        }
    }

}

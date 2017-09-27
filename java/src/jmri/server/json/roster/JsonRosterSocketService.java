package jmri.server.json.roster;

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
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonException;
import jmri.server.json.JsonSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listen for changes in the roster and notify subscribed clients of changes to
 * the roster, including roster groups
 *
 * @author Randall Wood Copyright (C) 2014, 2016
 */
public class JsonRosterSocketService extends JsonSocketService {

    private final static Logger log = LoggerFactory.getLogger(JsonRosterSocketService.class);
    private final JsonRosterListener rosterListener = new JsonRosterListener();
    private final JsonRosterEntryListener rosterEntryListener = new JsonRosterEntryListener();
    private final JsonRosterGroupsListener rosterGroupsListener = new JsonRosterGroupsListener();
    private final JsonRosterHttpService service;
    private boolean listening = false;

    public JsonRosterSocketService(JsonConnection connection) {
        super(connection);
        this.service = new JsonRosterHttpService(connection.getObjectMapper());
    }

    public void listen() {
        if (!this.listening) {
            Roster.getDefault().addPropertyChangeListener(this.rosterListener);
            Roster.getDefault().addPropertyChangeListener(this.rosterGroupsListener);
            Roster.getDefault().getEntriesInGroup(Roster.ALLENTRIES).stream().forEach((re) -> {
                re.addPropertyChangeListener(this.rosterEntryListener);
                re.addPropertyChangeListener(this.rosterGroupsListener);
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
                    case JsonRoster.ROSTER:
                        this.connection.sendMessage(this.service.getRoster(locale, data));
                        break;
                    case JsonRoster.ROSTER_ENTRY:
                        this.connection.sendMessage(this.service.getRosterEntry(locale, data.path(NAME).asText()));
                        break;
                    case JsonRoster.ROSTER_GROUP:
                        this.connection.sendMessage(this.service.getRosterGroup(locale, data.path(NAME).asText()));
                        break;
                    case JsonRoster.ROSTER_GROUPS:
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
        Roster.getDefault().removePropertyChangeListener(this.rosterGroupsListener);

        Roster.getDefault().getEntriesInGroup(Roster.ALLENTRIES).stream().forEach((re) -> {
            re.removePropertyChangeListener(this.rosterEntryListener);
            re.removePropertyChangeListener(this.rosterGroupsListener);
        });
        this.listening = false;
    }

    private class JsonRosterEntryListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            try {
                try {
                    if (evt.getPropertyName().equals(RosterEntry.ID)) {
                        // send old roster entry and new roster entry to client as roster changes
                        ObjectNode root = connection.getObjectMapper().createObjectNode().put(TYPE, JsonRoster.ROSTER);
                        ObjectNode data = root.putObject(DATA);
                        RosterEntry old = new RosterEntry((RosterEntry) evt.getSource(), (String) evt.getOldValue());
                        data.set(ADD, service.getRosterEntry(connection.getLocale(), (RosterEntry) evt.getSource()));
                        data.set(REMOVE, service.getRosterEntry(connection.getLocale(), old));
                        log.debug("Sending add and remove rosterEntry for {} ({} => {})", evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                        connection.sendMessage(root);
                    } else if (!evt.getPropertyName().equals(RosterEntry.DATE_UPDATED)
                            && !evt.getPropertyName().equals(RosterEntry.FILENAME)
                            && !evt.getPropertyName().equals(RosterEntry.COMMENT)) {  //don't send comment changes
                        log.debug("Sending updated rosterEntry for {} ({} => {})", evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                        connection.sendMessage(service.getRosterEntry(connection.getLocale(), (RosterEntry) evt.getSource()));
                    }
                } catch (JsonException ex) {
                    connection.sendMessage(ex.getJsonMessage());
                }
            } catch (IOException ex) {
                onClose();
            }
        }
    }

    private class JsonRosterListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            ObjectNode root = connection.getObjectMapper().createObjectNode().put(TYPE, JsonRoster.ROSTER);
            try {
                try {
                    if (evt.getPropertyName().equals(Roster.ADD)) {
                        root.putObject(DATA).put(ADD, service.getRosterEntry(connection.getLocale(), (RosterEntry) evt.getNewValue()));
                        ((PropertyChangeProvider) evt.getNewValue()).addPropertyChangeListener(rosterEntryListener);
                        connection.sendMessage(root);
                    } else if (evt.getPropertyName().equals(Roster.REMOVE)) {
                        root.putObject(DATA).put(REMOVE, service.getRosterEntry(connection.getLocale(), (RosterEntry) evt.getOldValue()));
                        connection.sendMessage(root);
                    } else if (!evt.getPropertyName().equals(Roster.SAVED)
                            && !evt.getPropertyName().equals(Roster.ROSTER_GROUP_ADDED)
                            && !evt.getPropertyName().equals(Roster.ROSTER_GROUP_REMOVED)
                            && !evt.getPropertyName().equals(Roster.ROSTER_GROUP_RENAMED)) {
                        // catch all events other than SAVED, and group stuff (handled elsewhere)
                        connection.sendMessage(service.getRoster(connection.getLocale(), root));
                    }
                } catch (JsonException ex) {
                    connection.sendMessage(ex.getJsonMessage());
                }
            } catch (IOException ex) {
                onClose();
            }
        }
    }

    private class JsonRosterGroupsListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            try {
                //handle direct roster change events
                if (evt.getPropertyName().equals(Roster.ROSTER_GROUP_ADDED)
                        || evt.getPropertyName().equals(Roster.ROSTER_GROUP_REMOVED)
                        || evt.getPropertyName().equals(Roster.ROSTER_GROUP_RENAMED)) {
                    try {
                        connection.sendMessage(service.getRosterGroups(connection.getLocale()));
                    } catch (JsonException ex) {
                        connection.sendMessage(ex.getJsonMessage());
                    }
                    //handle event names of format "attributeUpdated:RosterGroup:GROUPNAME"
                } else if (evt.getPropertyName().startsWith(RosterEntry.ATTRIBUTE_UPDATED)) {
                    String attrName = evt.getPropertyName().substring(RosterEntry.ATTRIBUTE_UPDATED.length());
                    if (attrName.startsWith(Roster.ROSTER_GROUP_PREFIX)) {
                        String groupName = attrName.substring(Roster.ROSTER_GROUP_PREFIX.length());
                        if (Roster.getDefault().getRosterGroups().containsKey(groupName)) {
                            try {
                                log.debug("sending changed rosterGroup {} and updated group array", groupName);
                                connection.sendMessage(service.getRosterGroup(connection.getLocale(), groupName));
                                connection.sendMessage(service.getRosterGroups(connection.getLocale()));
                            } catch (JsonException ex) {
                                connection.sendMessage(ex.getJsonMessage());
                            }
                        }
                    }
                    //handle attribute deleted, old value is of form "RosterGroup:GROUPNAME"
                } else if (evt.getPropertyName().startsWith(RosterEntry.ATTRIBUTE_DELETED)) {
                    if (((String) evt.getOldValue()).startsWith(Roster.ROSTER_GROUP_PREFIX)) {
                        String groupName = ((String) evt.getOldValue()).substring(Roster.ROSTER_GROUP_PREFIX.length());
                        if (Roster.getDefault().getRosterGroups().containsKey(groupName)) {
                            try {
                                log.debug("sending changed rosterGroup {} and updated group array", groupName);
                                connection.sendMessage(service.getRosterGroup(connection.getLocale(), groupName));
                                connection.sendMessage(service.getRosterGroups(connection.getLocale()));
                            } catch (JsonException ex) {
                                connection.sendMessage(ex.getJsonMessage());
                            }
                        }
                    }

                }

            } catch (IOException ex) {
                onClose();
            }
        }
    }

}

package jmri.server.json.roster;

import static jmri.server.json.JSON.ADD;
import static jmri.server.json.JSON.DELETE;
import static jmri.server.json.JSON.GET;
import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.POST;
import static jmri.server.json.JSON.PUT;
import static jmri.server.json.JSON.REMOVE;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import jmri.JmriException;
import jmri.beans.PropertyChangeProvider;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonException;
import jmri.server.json.JsonRequest;
import jmri.server.json.JsonSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listen for changes in the roster and notify subscribed clients of changes to
 * the roster, including roster groups
 *
 * @author Randall Wood Copyright (C) 2014, 2016
 */
public class JsonRosterSocketService extends JsonSocketService<JsonRosterHttpService> {

    private static final Logger log = LoggerFactory.getLogger(JsonRosterSocketService.class);
    private final JsonRosterListener rosterListener = new JsonRosterListener();
    private final JsonRosterEntryListener rosterEntryListener = new JsonRosterEntryListener();
    private final JsonRosterGroupsListener rosterGroupsListener = new JsonRosterGroupsListener();
    private boolean listening = false;

    public JsonRosterSocketService(JsonConnection connection) {
        super(connection, new JsonRosterHttpService(connection.getObjectMapper()));
    }

    public void listen() {
        if (!listening) {
            Roster.getDefault().addPropertyChangeListener(rosterListener);
            Roster.getDefault().addPropertyChangeListener(rosterGroupsListener);
            Roster.getDefault().getEntriesInGroup(Roster.ALLENTRIES).stream().forEach(re -> {
                re.addPropertyChangeListener(rosterEntryListener);
                re.addPropertyChangeListener(rosterGroupsListener);
            });
            listening = true;
        }
    }

    @Override
    public void onMessage(String type, JsonNode data, JsonRequest request)
            throws IOException, JmriException, JsonException {
        switch (request.method) {
            case DELETE:
                throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                        Bundle.getMessage(request.locale, "DeleteNotAllowed", type), request.id);
            case POST:
                if (JsonRoster.ROSTER_ENTRY.equals(type)) {
                    connection
                            .sendMessage(service.postRosterEntry(request.locale, data.path(NAME).asText(), data, request.id), request.id);
                } else {
                    throw new JsonException(HttpServletResponse.SC_NOT_IMPLEMENTED,
                            Bundle.getMessage(request.locale, "MethodNotImplemented", request.method, type), request.id);
                }
                break;
            case PUT:
                throw new JsonException(HttpServletResponse.SC_NOT_IMPLEMENTED,
                        Bundle.getMessage(request.locale, "MethodNotImplemented", request.method, type), request.id);
            case GET:
                switch (type) {
                    case JsonRoster.ROSTER:
                        connection.sendMessage(service.getRoster(request.locale, data, request.id), request.id);
                        break;
                    case JsonRoster.ROSTER_ENTRY:
                        connection.sendMessage(service.getRosterEntry(request.locale, data.path(NAME).asText(), request.id),
                                request.id);
                        break;
                    case JsonRoster.ROSTER_GROUP:
                        connection.sendMessage(service.getRosterGroup(request.locale, data.path(NAME).asText(), request.id),
                                request.id);
                        break;
                    case JsonRoster.ROSTER_GROUPS:
                        connection.sendMessage(service.getRosterGroups(request), request.id);
                        break;
                    default:
                        throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                Bundle.getMessage(request.locale, JsonException.ERROR_UNKNOWN_TYPE, type), request.id);
                }
                break;
            default:
                throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                        Bundle.getMessage(request.locale, "UnknownMethod", request.method), request.id);
        }
        listen();
    }

    @Override
    public void onList(String type, JsonNode data, JsonRequest request)
            throws IOException, JmriException, JsonException {
        connection.sendMessage(service.doGetList(type, data, request), request.id);
        listen();
    }

    @Override
    public void onClose() {
        Roster.getDefault().removePropertyChangeListener(rosterListener);
        Roster.getDefault().removePropertyChangeListener(rosterGroupsListener);

        Roster.getDefault().getEntriesInGroup(Roster.ALLENTRIES).stream().forEach(re -> {
            re.removePropertyChangeListener(rosterEntryListener);
            re.removePropertyChangeListener(rosterGroupsListener);
        });
        listening = false;
    }

    private class JsonRosterEntryListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            try {
                sendRosterUpdate(evt);
            } catch (IOException ex) {
                onClose();
            }
        }

        private void sendRosterUpdate(PropertyChangeEvent evt) throws IOException {
            try {
                if (evt.getPropertyName().equals(RosterEntry.ID)) {
                    // send old roster entry and new roster entry to client
                    // as roster changes
                    ObjectNode data = connection.getObjectMapper().createObjectNode();
                    RosterEntry old = new RosterEntry((RosterEntry) evt.getSource(), (String) evt.getOldValue());
                    data.set(ADD, service.getRosterEntry(connection.getLocale(), (RosterEntry) evt.getSource(), 0));
                    data.set(REMOVE, service.getRosterEntry(connection.getLocale(), old, 0));
                    log.debug("Sending add and remove rosterEntry for {} ({} => {})", evt.getPropertyName(),
                            evt.getOldValue(), evt.getNewValue());
                    connection.sendMessage(service.message(JsonRoster.ROSTER, data, 0), 0);
                } else if (!evt.getPropertyName().equals(RosterEntry.DATE_UPDATED) &&
                        !evt.getPropertyName().equals(RosterEntry.FILENAME) &&
                        !evt.getPropertyName().equals(RosterEntry.COMMENT)) {
                    // don't send comment changes
                    log.debug("Sending updated rosterEntry for {} ({} => {})", evt.getPropertyName(),
                            evt.getOldValue(), evt.getNewValue());
                    connection.sendMessage(
                            service.getRosterEntry(connection.getLocale(), (RosterEntry) evt.getSource(), 0), 0);
                }
            } catch (JsonException ex) {
                connection.sendMessage(ex.getJsonMessage(), 0);
            }
        }
    }

    private class JsonRosterListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            try {
                sendRosterUpdate(evt);
            } catch (IOException ex) {
                onClose();
            }
        }

        private void sendRosterUpdate(PropertyChangeEvent evt) throws IOException {
            try {
                ObjectNode data = connection.getObjectMapper().createObjectNode();
                if (evt.getPropertyName().equals(Roster.ADD)) {
                    data.set(ADD,
                            service.getRosterEntry(connection.getLocale(), (RosterEntry) evt.getNewValue(), 0));
                    ((PropertyChangeProvider) evt.getNewValue()).addPropertyChangeListener(rosterEntryListener);
                    connection.sendMessage(service.message(JsonRoster.ROSTER, data, 0), 0);
                } else if (evt.getPropertyName().equals(Roster.REMOVE)) {
                    data.set(REMOVE,
                            service.getRosterEntry(connection.getLocale(), (RosterEntry) evt.getOldValue(), 0));
                    connection.sendMessage(service.message(JsonRoster.ROSTER, data, 0), 0);
                } else if (!evt.getPropertyName().equals(Roster.SAVED) &&
                        !evt.getPropertyName().equals(Roster.ROSTER_GROUP_ADDED) &&
                        !evt.getPropertyName().equals(Roster.ROSTER_GROUP_REMOVED) &&
                        !evt.getPropertyName().equals(Roster.ROSTER_GROUP_RENAMED)) {
                    // catch all events other than SAVED and ROSTER_GROUP_*
                    // (handled elsewhere)
                    connection.sendMessage(service.getRoster(connection.getLocale(), NullNode.getInstance(), 0), 0);
                }
            } catch (JsonException ex) {
                connection.sendMessage(ex.getJsonMessage(), 0);
            }
        }
    }

    private class JsonRosterGroupsListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            try {
                // handle direct roster change events
                if (evt.getPropertyName().equals(Roster.ROSTER_GROUP_ADDED) ||
                        evt.getPropertyName().equals(Roster.ROSTER_GROUP_REMOVED) ||
                        evt.getPropertyName().equals(Roster.ROSTER_GROUP_RENAMED)) {
                    sendGroupsUpdate();
                    // handle event names of format
                    // "attributeUpdated:RosterGroup:GROUPNAME"
                } else if (evt.getPropertyName().startsWith(RosterEntry.ATTRIBUTE_UPDATED)) {
                    String attrName = evt.getPropertyName().substring(RosterEntry.ATTRIBUTE_UPDATED.length());
                    if (attrName.startsWith(Roster.ROSTER_GROUP_PREFIX)) {
                        String groupName = attrName.substring(Roster.ROSTER_GROUP_PREFIX.length());
                        if (Roster.getDefault().getRosterGroups().containsKey(groupName)) {
                            sendGroupNameUpdate(groupName);
                        }
                    }
                    // handle attribute deleted, old value is of form
                    // "RosterGroup:GROUPNAME"
                } else if (evt.getPropertyName().startsWith(RosterEntry.ATTRIBUTE_DELETED) &&
                        ((String) evt.getOldValue()).startsWith(Roster.ROSTER_GROUP_PREFIX)) {
                    String groupName = ((String) evt.getOldValue()).substring(Roster.ROSTER_GROUP_PREFIX.length());
                    if (Roster.getDefault().getRosterGroups().containsKey(groupName)) {
                        sendGroupNameUpdate(groupName);
                    }
                }
            } catch (IOException ex) {
                onClose();
            }
        }

        private void sendGroupsUpdate() throws IOException {
            try {
                connection.sendMessage(service.getRosterGroups(new JsonRequest(getLocale(), getVersion(), GET, 0)), 0);
            } catch (JsonException ex) {
                connection.sendMessage(ex.getJsonMessage(), 0);
            }
        }

        private void sendGroupNameUpdate(String groupName) throws IOException {
            try {
                log.debug("sending changed rosterGroup {} and updated group array", groupName);
                connection.sendMessage(service.getRosterGroup(getLocale(), groupName, 0), 0);
                sendGroupsUpdate();
            } catch (JsonException ex) {
                connection.sendMessage(ex.getJsonMessage(), 0);
            }
        }
    }

}

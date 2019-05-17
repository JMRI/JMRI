package jmri.server.json.message;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jmri.InstanceManagerAutoDefault;
import jmri.server.json.JsonConnection;

/**
 * Manager for JSON streaming clients that are subscribing to messages triggered
 * by out-of-channel events.
 *
 * @author Randall Wood Copyright 2017
 */
public class JsonMessageClientManager implements InstanceManagerAutoDefault {

    final ObjectMapper mapper = new ObjectMapper();
    HashMap<String, JsonConnection> clients = new HashMap<>();

    /**
     * Subscribe to the message service.
     *
     * @param client     the client identifier to use for the subscription
     * @param connection the connection associated with client
     * @throws IllegalArgumentException if client is already in use for a
     *                                      different connection
     */
    public void subscribe(@Nonnull String client, @Nonnull JsonConnection connection) throws IllegalArgumentException {
        if (this.clients.containsKey(client) && !connection.equals(this.clients.get(client))) {
            throw new IllegalArgumentException("client in use with different connection");
        }
        this.clients.putIfAbsent(client, connection);
    }

    /**
     * Cancel the subscription for a single client.
     *
     * @param client the client canceling the subscription
     */
    public void unsubscribe(@Nullable String client) {
        this.clients.remove(client);
    }

    /**
     * Cancel the subscription for all clients on a given connection.
     *
     * @param connection the connection canceling the subscription
     */
    public void unsubscribe(@Nullable JsonConnection connection) {
        List<String> keys = new ArrayList<>();
        this.clients.entrySet().stream()
                .filter((entry) -> (entry.getValue().equals(connection)))
                .forEachOrdered((entry) -> {
                    keys.add(entry.getKey());
                });
        keys.forEach((client) -> {
            this.unsubscribe(client);
        });
    }

    /**
     * Send a message to a client or clients. The determination of a single
     * client or all clients is made using {@link JsonMessage#getClient()}.
     *
     * @param message the message to send
     */
    public void send(@Nonnull JsonMessage message) {
        JsonNode node = this.getJsonMessage(message);
        if (message.getClient() == null) {
            new HashMap<>(this.clients).entrySet().forEach((client) -> {
                try {
                    client.getValue().sendMessage(node, 0);
                } catch (IOException ex) {
                    this.unsubscribe(client.getKey());
                }
            });
        } else {
            JsonConnection connection = this.clients.get(message.getClient());
            if (connection != null) {
                try {
                    connection.sendMessage(node, 0);
                } catch (IOException ex) {
                    this.unsubscribe(message.getClient());
                }
            }
        }
    }

    private JsonNode getJsonMessage(JsonMessage message) {
        return message.toJSON(this.mapper);
    }

    /**
     * Get the first client name associated with a connection.
     *
     * @param connection the connection to get a client for
     * @return the client or null if the connection is not subscribed
     */
    @CheckForNull
    public synchronized String getClient(@Nonnull JsonConnection connection) {
        for (Entry<String, JsonConnection> entry : this.clients.entrySet()) {
            if (entry.getValue().equals(connection)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Get all client names associated with a connection.
     * 
     * @param connection the connection to get clients for
     * @return a set of clients or an empty set if the connection is not
     *         subscribed
     */
    public synchronized Set<String> getClients(@Nonnull JsonConnection connection) {
        Set<String> set = new HashSet<>();
        for (Entry<String, JsonConnection> entry : this.clients.entrySet()) {
            if (entry.getValue().equals(connection)) {
                set.add(entry.getKey());
            }
        }
        return set;
    }
}

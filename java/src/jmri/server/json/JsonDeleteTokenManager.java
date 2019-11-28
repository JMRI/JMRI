package jmri.server.json;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.CheckForNull;

import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;

/**
 * Manager for deletion tokens in the JSON protocols. This is a separate manager
 * to be able to support the RESTful API where the connection where a token is
 * generated may be broken before the client's deletion request containing the
 * token is sent.
 * <p>
 * Note this is <em>package private</em> and is not part of a committed to API.
 * 
 * @author Randall Wood Copyright 2019
 * @since 4.15.6
 */
class JsonDeleteTokenManager {

    private final Map<String, String> tokens = new HashMap<>();

    /**
     * Use this method to access the default instance. This ensures that public
     * API does not need to be exposed for {@link InstanceManagerAutoDefault} to
     * function.
     * 
     * @return the default instance
     */
    static JsonDeleteTokenManager getDefault() {
        if (InstanceManager.getNullableDefault(JsonDeleteTokenManager.class) == null) {
            InstanceManager.setDefault(JsonDeleteTokenManager.class, new JsonDeleteTokenManager());
        }
        return InstanceManager.getDefault(JsonDeleteTokenManager.class);
    }

    JsonDeleteTokenManager() {
        // nothing to do
    }

    /**
     * Accept a token. If the token is not valid, any valid token is also invalidated.
     * 
     * @param type the type of the object to delete
     * @param name the name of the object to delete
     * @param token the token to test
     * @return true if the token was accepted; false otherwise
     */
    boolean acceptToken(@Nonnull String type, @Nonnull String name, @CheckForNull String token) {
        // generate a random token so that a fixed string cannot be discovered and used to
        // bypass this check
        String value = tokens.getOrDefault(getKey(type, name), UUID.randomUUID().toString());
        return value.equals(token);
    }

    /**
     * Generate a token to allow deletion following the rejection of a deletion
     * request.
     * 
     * @param type the type of the object to delete
     * @param name the name of the object to delete
     * @return the token to use to confirm a deletion should be accepted
     */
    String getToken(@Nonnull String type, @Nonnull String name) {
        String key = getKey(type, name);
        tokens.put(key, UUID.randomUUID().toString());
        return tokens.get(key);
    }

    private String getKey(@Nonnull String type, @Nonnull String name) {
        return type + name;
    }
}

package jmri.server.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletResponse;
import jmri.NamedBean;
import jmri.ProvidingManager;

/**
 * Abstract implementation of JsonHttpService with specific support for
 * {@link jmri.NamedBean} objects.
 * <p>
 * <strong>Note:</strong> services requiring support
 * for multiple classes of NamedBean cannot extend this class.
 * <p>
 * <strong>Note:</strong> NamedBeans must be managed by a
 * {@link jmri.ProvidingManager} for this class to be used.
 *
 * @author Randall Wood (C) 2016, 2019
 * @param <T> the type supported by this service
 */
public abstract class JsonNamedBeanHttpService<T extends NamedBean> extends JsonNonProvidedNamedBeanHttpService<T> {

    public JsonNamedBeanHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public final JsonNode doGet(@Nonnull String type, @Nonnull String name, @Nonnull Locale locale) throws JsonException {
        if (type != getType()) {
            throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Bundle.getMessage(locale, "LoggedError"));
        }
        return this.doGet(this.getManager().getBeanBySystemName(name), name, type, locale);
    }

    /**
     * {@inheritDoc}
     * 
     * Override if the implementing class needs to prevent PUT methods
     * from functioning or need to perform additional validation prior to
     * creating the NamedBean. 
     */
    @Override
    public JsonNode doPut(@Nonnull String type, @Nonnull String name, @Nonnull JsonNode data, @Nonnull Locale locale) throws JsonException {
        try {
            getManager().provide(name);
        } catch (IllegalArgumentException ex) {
            throw new JsonException(HttpServletResponse.SC_BAD_REQUEST, Bundle.getMessage(locale, "ErrorInvalidSystemName", name, getType()));
        } catch (Exception ex) {
            throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Bundle.getMessage(locale, "ErrorCreatingObject", getType(), name));
        }
        return this.doPost(type, name, data, locale);
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public final ArrayNode doGetList(String type, Locale locale) throws JsonException {
        return doGetList(getManager(), type, locale);
    }

    /**
     * Respond to an HTTP GET request for the requested name.
     * <p>
     * If name is null, return a list of all objects for the given type, if
     * appropriate.
     * <p>
     * This method should throw a 500 Internal Server Error if type is not
     * recognized.
     *
     * @param bean   the requested object
     * @param name   the name of the requested object
     * @param type   the type of the requested object
     * @param locale the requesting client's Locale
     * @return a JSON description of the requested object
     * @throws JsonException if the named object does not exist or other error
     *                       occurs
     */
    @Override
    @Nonnull
    protected abstract ObjectNode doGet(T bean, @Nonnull String name, @Nonnull String type, @Nonnull Locale locale) throws JsonException;
    
    /**
     * Get the JSON type supported by this service.
     * 
     * @return the JSON type
     */
    @Nonnull
    protected abstract String getType();

    /**
     * Get the expected manager for the supported JSON type. This should normally be
     * the default manager.
     * 
     * @return the manager
     */
    @Nonnull
    protected abstract ProvidingManager<T> getManager();
}

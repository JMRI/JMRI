package jmri.server.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.CheckForNull;
import javax.servlet.http.HttpServletResponse;
import jmri.NamedBean;
import jmri.ProvidingManager;

/**
 * Abstract implementation of JsonHttpService with specific support for
 * {@link jmri.NamedBean} objects.
 * <p>
 * <strong>Note:</strong> services requiring support for multiple classes of
 * NamedBean cannot extend this class.
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
    public final JsonNode doGet(@Nonnull String type, @Nonnull String name, @Nonnull JsonNode data,
            @Nonnull Locale locale, int id) throws JsonException {
        if (!type.equals(getType())) {
            throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    Bundle.getMessage(locale, JsonException.LOGGED_ERROR), id);
        }
        // NOTE: although allowing a user name to be used, a system name is recommended as it is
        // less likely to suffer errors in translation between the allowed name and URL conversion
        return this.doGet(this.getManager().getNamedBean(name), name, type, locale, id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public final JsonNode doPost(@Nonnull String type, @Nonnull String name, @Nonnull JsonNode data, @Nonnull Locale locale, int id) throws JsonException {
        if (!type.equals(getType())) {
            throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    Bundle.getMessage(locale, JsonException.LOGGED_ERROR), id);
        }
        // NOTE: although allowing a user name to be used, a system name is recommended as it is
        // less likely to suffer errors in translation between the allowed name and URL conversion
        T bean = this.postNamedBean(getManager().getNamedBean(name), data, name, type, locale, id);
        return this.doPost(bean, name, type, data, locale, id);
    }

    /**
     * {@inheritDoc}
     * 
     * Override if the implementing class needs to prevent PUT methods from
     * functioning or need to perform additional validation prior to creating
     * the NamedBean.
     */
    @Override
    public JsonNode doPut(@Nonnull String type, @Nonnull String name, @Nonnull JsonNode data, @Nonnull Locale locale, int id)
            throws JsonException {
        try {
            getManager().provide(name);
        } catch (IllegalArgumentException ex) {
            throw new JsonException(HttpServletResponse.SC_BAD_REQUEST,
                    Bundle.getMessage(locale, "ErrorInvalidSystemName", name, getType()), id);
        } catch (Exception ex) {
            throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    Bundle.getMessage(locale, "ErrorCreatingObject", getType(), name), id);
        }
        return this.doPost(type, name, data, locale, id);
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public final JsonNode doGetList(String type, JsonNode data, Locale locale, int id) throws JsonException {
        return doGetList(getManager(), type, data, locale, id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doDelete(String type, String name, JsonNode data, Locale locale, int id) throws JsonException {
        if (!type.equals(getType())) {
            throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    Bundle.getMessage(locale, JsonException.LOGGED_ERROR), id);
        }
        // NOTE: although allowing a user name to be used, a system name is recommended as it is
        // less likely to suffer errors in translation between the allowed name and URL conversion
        this.doDelete(getManager().getNamedBean(name), name, type, data, locale, id);
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
     * @param id     the message id set by the client
     * @return a JSON description of the requested object
     * @throws JsonException if the named object does not exist or other error
     *                           occurs
     */
    @Override
    @Nonnull
    protected abstract ObjectNode doGet(T bean, @Nonnull String name, @Nonnull String type, @Nonnull Locale locale, int id)
            throws JsonException;

    /**
     * Respond to an HTTP POST request for the requested name.
     *
     * @param bean   the requested object
     * @param name   the name of the requested object
     * @param type   the type of the requested object
     * @param data   data describing the requested object
     * @param locale the requesting client's Locale
     * @param id     the message id set by the client
     * @return a JSON description of the requested object
     * @throws JsonException if an error occurs
     */
    @Nonnull
    protected abstract ObjectNode doPost(T bean, @Nonnull String name, @Nonnull String type, @Nonnull JsonNode data, @Nonnull Locale locale, int id)
            throws JsonException;

    /**
     * Delete the requested bean.
     * <p>
     * This method must be overridden to allow a bean to be deleted. The
     * simplest overriding method body is:
     * {@code deleteBean(bean, name, type, data, locale, id); }
     * 
     * @param bean   the bean to delete
     * @param name   the named of the bean to delete
     * @param type   the type of the bean to delete
     * @param data   data describing the named bean
     * @param locale the requesting client's Locale
     * @param id     the message id set by the client
     * @throws JsonException if an error occurs
     */
    protected void doDelete(@CheckForNull T bean, @Nonnull String name, @Nonnull String type, @Nonnull JsonNode data, @Nonnull Locale locale, int id) throws JsonException {
        super.doDelete(type, name, data, locale, id);
    }

    /**
     * Delete the requested bean. This is the simplest method to delete a bean,
     * and is likely to become the default implementation of
     * {@link #doDelete(NamedBean, String, String, JsonNode, Locale, int)} in an
     * upcoming release of JMRI.
     * 
     * @param bean   the bean to delete
     * @param name   the named of the bean to delete
     * @param type   the type of the bean to delete
     * @param data   data describing the named bean
     * @param locale the requesting client's Locale
     * @param id     the message id set by the client
     * @throws JsonException if an error occurs
     */
    protected final void deleteBean(@CheckForNull T bean, @Nonnull String name, @Nonnull String type, @Nonnull JsonNode data, @Nonnull Locale locale, int id) throws JsonException {
        if (bean == null) {
            throw new JsonException(HttpServletResponse.SC_NOT_FOUND,
                    Bundle.getMessage(locale, JsonException.ERROR_NOT_FOUND, type, name), id);
        }
        List<String> listeners = bean.getListenerRefs();
        if (!listeners.isEmpty() && !acceptForceDeleteToken(type, name, data.path(JSON.FORCE_DELETE).asText())) {
            ArrayNode conflicts = mapper.createArrayNode();
            listeners.forEach(conflicts::add);
            throwDeleteConflictException(type, name, conflicts, locale, id);
        } else {
            getManager().deregister(bean);
        }
    }

    /**
     * Get the JSON type supported by this service.
     * 
     * @return the JSON type
     */
    @Nonnull
    protected abstract String getType();

    /**
     * Get the expected manager for the supported JSON type. This should
     * normally be the default manager.
     * 
     * @return the manager
     */
    @Nonnull
    protected abstract ProvidingManager<T> getManager();
}

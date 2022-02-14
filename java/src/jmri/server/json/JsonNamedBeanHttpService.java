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
            @Nonnull JsonRequest request) throws JsonException {
        if (!type.equals(getType())) {
            throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    Bundle.getMessage(request.locale, JsonException.LOGGED_ERROR), request.id);
        }
        // NOTE: although allowing a user name to be used, a system name is recommended as it is
        // less likely to suffer errors in translation between the allowed name and URL conversion
        return doGet(this.getManager().getNamedBean(name), name, type, request);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public final JsonNode doPost(@Nonnull String type, @Nonnull String name, @Nonnull JsonNode data, @Nonnull JsonRequest request) throws JsonException {
        if (!type.equals(getType())) {
            throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    Bundle.getMessage(request.locale, JsonException.LOGGED_ERROR), request.id);
        }
        // NOTE: although allowing a user name to be used, a system name is recommended as it is
        // less likely to suffer errors in translation between the allowed name and URL conversion
        T bean = postNamedBean(getManager().getNamedBean(name), data, name, type, request);
        return doPost(bean, name, type, data, request);
    }

    /**
     * {@inheritDoc}
     *
     * Override if the implementing class needs to prevent PUT methods from
     * functioning or need to perform additional validation prior to creating
     * the NamedBean.
     */
    @Override
    public JsonNode doPut(@Nonnull String type, @Nonnull String name, @Nonnull JsonNode data, @Nonnull JsonRequest request)
            throws JsonException {
        try {
            getManager().provide(name);
        } catch (IllegalArgumentException ex) {
            throw new JsonException(HttpServletResponse.SC_BAD_REQUEST,
                    Bundle.getMessage(request.locale, "ErrorInvalidSystemName", name, getType()), request.id);
        } catch (Exception ex) {
            throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    Bundle.getMessage(request.locale, "ErrorCreatingObject", getType(), name), request.id);
        }
        return doPost(type, name, data, request);
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public final JsonNode doGetList(String type, JsonNode data, JsonRequest request) throws JsonException {
        return doGetList(getManager(), type, data, request);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doDelete(String type, String name, JsonNode data, JsonRequest request) throws JsonException {
        if (!type.equals(getType())) {
            throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    Bundle.getMessage(request.locale, JsonException.LOGGED_ERROR), request.id);
        }
        // NOTE: although allowing a user name to be used, a system name is recommended as it is
        // less likely to suffer errors in translation between the allowed name and URL conversion
        doDelete(getManager().getNamedBean(name), name, type, data, request);
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
     * @param request the JSON request
     * @return a JSON description of the requested object
     * @throws JsonException if the named object does not exist or other error
     *                           occurs
     */
    @Override
    @Nonnull
    protected abstract ObjectNode doGet(T bean, @Nonnull String name, @Nonnull String type, @Nonnull JsonRequest request)
            throws JsonException;

    /**
     * {@inheritDoc}
     */
    @Override
    @CheckForNull
    public T getNamedBean(@Nonnull String type, @Nonnull String name, @Nonnull JsonNode data, @Nonnull JsonRequest request) throws JsonException {
        try {
            if (!data.isEmpty() && !data.isNull()) {
                if (JSON.PUT.equals(request.method)) {
                    doPut(type, name, data, request);
                } else if (JSON.POST.equals(request.method)) {
                    doPost(type, name, data, request);
                }
            }
            return getManager().getBySystemName(name);
        } catch (IllegalArgumentException ex) {
            throw new JsonException(HttpServletResponse.SC_BAD_REQUEST, Bundle.getMessage(request.locale, "ErrorInvalidSystemName", name, type), request.id);
        }
    }

    /**
     * Respond to an HTTP POST request for the requested name.
     *
     * @param bean   the requested object
     * @param name   the name of the requested object
     * @param type   the type of the requested object
     * @param data   data describing the requested object
     * @param request the JSON request
     * @return a JSON description of the requested object
     * @throws JsonException if an error occurs
     */
    @Nonnull
    protected abstract ObjectNode doPost(T bean, @Nonnull String name, @Nonnull String type, @Nonnull JsonNode data, @Nonnull JsonRequest request)
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
     * @param request the JSON request
     * @throws JsonException if an error occurs
     */
    protected void doDelete(@CheckForNull T bean, @Nonnull String name, @Nonnull String type, @Nonnull JsonNode data, @Nonnull JsonRequest request) throws JsonException {
        super.doDelete(type, name, data, request);
    }

    /**
     * Delete the requested bean. This is the simplest method to delete a bean,
     * and is likely to become the default implementation of
     * {@link #doDelete} in an
     * upcoming release of JMRI.
     *
     * @param bean   the bean to delete
     * @param name   the named of the bean to delete
     * @param type   the type of the bean to delete
     * @param data   data describing the named bean
     * @param request the JSON request
     * @throws JsonException if an error occurs
     */
    protected final void deleteBean(@CheckForNull T bean, @Nonnull String name, @Nonnull String type, @Nonnull JsonNode data, @Nonnull JsonRequest request) throws JsonException {
        if (bean == null) {
            throw new JsonException(HttpServletResponse.SC_NOT_FOUND,
                    Bundle.getMessage(request.locale, JsonException.ERROR_NOT_FOUND, type, name), request.id);
        }
        List<String> listeners = bean.getListenerRefs();
        if (!listeners.isEmpty() && !acceptForceDeleteToken(type, name, data.path(JSON.FORCE_DELETE).asText())) {
            ArrayNode conflicts = mapper.createArrayNode();
            listeners.forEach(conflicts::add);
            throwDeleteConflictException(type, name, conflicts, request);
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

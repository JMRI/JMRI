package jmri.server.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Locale;
import javax.annotation.Nonnull;
import jmri.NamedBean;

/**
 * Abstract implementation of JsonHttpService with specific support for
 * {@link jmri.NamedBean} objects.
 *
 * @author Randall Wood (C) 2016
 */
public abstract class JsonNamedBeanHttpService extends JsonHttpService {

    public JsonNamedBeanHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    /**
     * Create the JsonNode for a {@link jmri.NamedBean} object.
     *
     * @param bean   the bean to create the node for
     * @param name   the name of the bean; used only if the bean is null
     * @param type   the JSON type of the bean
     * @param locale the locale used for any error messages
     * @return a JSON node
     * @throws JsonException if the bean is null
     */
    @Nonnull
    protected ObjectNode getNamedBean(NamedBean bean, @Nonnull String name, @Nonnull String type, @Nonnull Locale locale) throws JsonException {
        if (bean == null) {
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", type, name));
        }
        ObjectNode root = mapper.createObjectNode();
        root.put(JSON.TYPE, type);
        ObjectNode data = root.putObject(JSON.DATA);
        data.put(JSON.NAME, bean.getSystemName());
        data.put(JSON.USERNAME, bean.getUserName());
        data.put(JSON.COMMENT, bean.getComment());
        ArrayNode properties = root.putArray(JSON.PROPERTIES);
        bean.getPropertyKeys().stream().forEach((key) -> {
            Object value = bean.getProperty(key);
            if (value != null) {
                properties.add(mapper.createObjectNode().put(key, value.toString()));
            } else {
                properties.add(mapper.createObjectNode().putNull(key));
            }
        });
        return data;
    }

    /**
     * Handle the common elements of a NamedBean that can be changed in an POST
     * message.
     * <p>
     * <strong>Note:</strong> the system name of a NamedBean cannot be changed
     * using this method.
     *
     * @param bean   the bean to modify
     * @param data   the JsonNode containing the JSON representation of bean
     * @param name   the system name of the bean
     * @param type   the JSON type of the bean
     * @param locale the locale used for any error messages
     * @throws JsonException if the bean is null
     */
    protected void postNamedBean(NamedBean bean, @Nonnull JsonNode data, @Nonnull String name, @Nonnull String type, @Nonnull Locale locale) throws JsonException {
        if (bean == null) {
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", type, name));
        }
        if (data.path(JSON.USERNAME).isTextual()) {
            bean.setUserName(data.path(JSON.USERNAME).asText());
        }
        if (!data.path(JSON.COMMENT).isMissingNode()) {
            JsonNode comment = data.path(JSON.COMMENT);
            bean.setComment(comment.isTextual() ? comment.asText() : null);
        }
    }
}

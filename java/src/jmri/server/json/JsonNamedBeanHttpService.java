package jmri.server.json;

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
                properties.add(mapper.createObjectNode().put(key, bean.getProperty(key).toString()));
            } else {
                properties.add(mapper.createObjectNode().putNull(key));
            }
        });
        return data;
    }

}

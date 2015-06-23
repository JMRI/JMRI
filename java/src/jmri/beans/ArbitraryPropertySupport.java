package jmri.beans;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Provide support for converging the Arbitrary* classes with the non-arbitrary
 * versions of those classes so that the Arbitrary* version can extend the
 * non-arbitrary class.
 */
public class ArbitraryPropertySupport implements BeanInterface {

    private final HashMap<String, Object> properties = new HashMap<>();
    private final UnboundBean bean;

    public ArbitraryPropertySupport(UnboundBean bean) {
        this.bean = bean;
    }

    @Override
    public void setIndexedProperty(String key, int index, Object value) {
        if (Beans.hasIntrospectedProperty(this.bean, key)) {
            Beans.setIntrospectedIndexedProperty(this.bean, key, index, value);
        } else {
            if (!this.properties.containsKey(key)) {
                this.properties.put(key, new Object[1]);
            }
            Object[] array = (Object[]) this.properties.get(key);
            if (index < array.length) {
                array[index] = value;
            } else {
                Object[] grown = Arrays.copyOf(array, index + 1);
                grown[index] = value;
                this.properties.put(key, grown);
            }
        }
    }

    @Override
    public Object getIndexedProperty(String key, int index) {
        if (this.properties.containsKey(key) && this.properties.get(key).getClass().isArray()) {
            try {
                return ((Object[]) this.properties.get(key))[index];
            } catch (ArrayIndexOutOfBoundsException ex) {
                return null;
            }
        }
        return Beans.getIntrospectedIndexedProperty(this.bean, key, index);
    }

    @Override
    public void setProperty(String key, Object value) {
        // use write method for property if it exists
        if (Beans.hasIntrospectedProperty(this.bean, key)) {
            Beans.setIntrospectedProperty(this.bean, key, value);
        } else {
            this.properties.put(key, value);
        }
    }

    @Override
    public Object getProperty(String key) {
        if (this.properties.containsKey(key)) {
            return this.properties.get(key);
        }
        return Beans.getIntrospectedProperty(this.bean, key);
    }

    @Override
    public boolean hasProperty(String key) {
        return (this.properties.containsKey(key) || Beans.hasIntrospectedProperty(this.bean, key));
    }

    @Override
    public boolean hasIndexedProperty(String key) {
        return ((this.properties.containsKey(key) && this.properties.get(key).getClass().isArray())
                || Beans.hasIntrospectedIndexedProperty(this.bean, key));
    }

    @Override
    public Set<String> getPropertyNames() {
        HashSet<String> names = new HashSet<>();
        names.addAll(this.properties.keySet());
        names.addAll(Beans.getIntrospectedPropertyNames(this.bean));
        return names;
    }

}

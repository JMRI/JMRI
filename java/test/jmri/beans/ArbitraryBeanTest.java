package jmri.beans;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jmri.util.JUnitUtil;

/**
 * @author Randall Wood Copyright 2020
 */
public class ArbitraryBeanTest {

    private ArbitraryBeanImpl bean;
    private final String ap = "arbitraryProperty";
    private final String dp = "definedProperty";
    private final String aip = "arbitraryIndexedProperty";
    private final String dip = "definedIndexedProperty";

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        bean = new ArbitraryBeanImpl();
    }

    @AfterEach
    public void tearDown() {
        bean = null;
        JUnitUtil.tearDown();
    }

    @Test
    public void testSetProperty() {
        assertThat(bean).hasFieldOrProperty(dp);
        assertThat(bean.hasProperty(ap)).isFalse();
        assertThat(bean.getDefinedProperty()).isNull();
        assertThat(bean.getProperty(ap)).isNull();
        assertThat(bean.getProperty(dp)).isNull();
        bean.setProperty(dp, "set");
        assertThat(bean.getDefinedProperty()).isEqualTo("set");
        assertThat(bean.getProperty(dp)).isEqualTo("set");
        bean.setProperty(ap, null);
        assertThat(bean.hasProperty(ap)).isTrue();
        assertThat(bean.getProperty(ap)).isNull();
        bean.setProperty(ap, "set");
        assertThat(bean.getProperty(ap)).isEqualTo("set");
    }

    @Test
    public void testSetIndexedProperty() {
        assertThat(bean.hasIndexedProperty(dip)).isTrue();
        assertThat(bean.getDefinedIndexedProperty()).isEmpty();
        assertThat(bean.hasIndexedProperty(aip)).isFalse();
        bean.setIndexedProperty(dip, 0, "set");
        assertThat(bean.getDefinedIndexedProperty(0)).isEqualTo("set");
        assertThat(bean.getIndexedProperty(dip, 0)).isEqualTo("set");
        assertThat(bean.hasIndexedProperty(aip)).isFalse();
        bean.setIndexedProperty(aip, 0, null);
        assertThat(bean.hasIndexedProperty(aip)).isTrue();
        assertThat(bean.getIndexedProperty(aip, 0)).isNull();
        bean.setIndexedProperty(aip, 0, "set");
        assertThat(bean.getIndexedProperty(aip, 0)).isEqualTo("set");
    }

    @Test
    public void testGetPropertyNames() {
        assertThat(bean.getPropertyNames())
                .contains(dp, dip)
                .doesNotContain(ap, aip);
        bean.setProperty(ap, null);
        assertThat(bean.getPropertyNames())
                .contains(dp, dip, ap)
                .doesNotContain(aip);
        bean.setIndexedProperty(aip, 0, null);
        assertThat(bean.getPropertyNames())
                .contains(dp, dip, ap, aip);
    }

    public class ArbitraryBeanImpl extends ArbitraryBean {

        public String definedProperty;
        public final List<String> definedIndexedProperty = new ArrayList<>();

        public String getDefinedProperty() {
            return definedProperty;
        }

        public void setDefinedProperty(String value) {
            definedProperty = value;
        }

        public String[] getDefinedIndexedProperty() {
            return definedIndexedProperty.toArray(new String[definedIndexedProperty.size()]);
        }

        public String getDefinedIndexedProperty(int i) {
            return definedIndexedProperty.get(i);
        }

        public void setDefinedIndexedProperty(String[] values) {
            definedIndexedProperty.clear();
            definedIndexedProperty.addAll(Arrays.asList(values));
        }

        /*
         * Throws IndexOutOfBoundsException if index > size.
         */
        public void setDefinedIndexedProperty(int i, String value) {
            if (i < definedIndexedProperty.size()) {
                definedIndexedProperty.set(i, value);
            } else {
                definedIndexedProperty.add(i, value);
            }
        }
    }

}

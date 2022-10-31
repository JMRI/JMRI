package jmri.beans;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.*;

import jmri.util.JUnitUtil;

/**
 * @author Randall Wood Copyright 2020
 */
public class ArbitraryBeanTest {

    private ArbitraryBeanImpl bean = null;
    private final static String AP = "arbitraryProperty";
    private final static String DP = "definedProperty";
    private final static String AIP = "arbitraryIndexedProperty";
    private final static String DIP = "definedIndexedProperty";

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
        Assertions.assertNotNull(bean);
        assertThat(bean).hasFieldOrProperty(DP);
        assertThat(bean.hasProperty(AP)).isFalse();
        assertThat(bean.getDefinedProperty()).isNull();
        assertThat(bean.getProperty(AP)).isNull();
        assertThat(bean.getProperty(DP)).isNull();
        bean.setProperty(DP, "set");
        assertThat(bean.getDefinedProperty()).isEqualTo("set");
        assertThat(bean.getProperty(DP)).isEqualTo("set");
        bean.setProperty(AP, null);
        assertThat(bean.hasProperty(DP)).isTrue();
        assertThat(bean.getProperty(AP)).isNull();
        bean.setProperty(AP, "set");
        assertThat(bean.getProperty(DP)).isEqualTo("set");
    }

    @Test
    public void testSetIndexedProperty() {
        Assertions.assertNotNull(bean);
        assertThat(bean.hasIndexedProperty(DIP)).isTrue();
        assertThat(bean.getDefinedIndexedProperty()).isEmpty();
        assertThat(bean.hasIndexedProperty(AIP)).isFalse();
        bean.setIndexedProperty(DIP, 0, "set");
        assertThat(bean.getDefinedIndexedProperty(0)).isEqualTo("set");
        assertThat(bean.getIndexedProperty(DIP, 0)).isEqualTo("set");
        assertThat(bean.hasIndexedProperty(AIP)).isFalse();
        bean.setIndexedProperty(AIP, 0, null);
        assertThat(bean.hasIndexedProperty(AIP)).isTrue();
        assertThat(bean.getIndexedProperty(AIP, 0)).isNull();
        bean.setIndexedProperty(AIP, 0, "set");
        assertThat(bean.getIndexedProperty(AIP, 0)).isEqualTo("set");
    }

    @Test
    public void testGetPropertyNames() {
        Assertions.assertNotNull(bean);
        assertThat(bean.getPropertyNames())
                .contains(DP, DIP)
                .doesNotContain(AP, AIP);
        bean.setProperty(AP, null);
        assertThat(bean.getPropertyNames())
                .contains(DP, DIP, AP)
                .doesNotContain(AIP);
        bean.setIndexedProperty(AIP, 0, null);
        assertThat(bean.getPropertyNames())
                .contains(DP, DIP, AP, AIP);
    }

    protected static class ArbitraryBeanImpl extends ArbitraryBean {

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

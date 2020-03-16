package jmri.beans;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Randall Wood Copyright 2020
 */
public class ConstrainedArbitraryBeanTest {

    private ConstrainedArbitraryBeanImpl bean;
    private VetoableChangeListener vetoer;
    private boolean changed;
    private boolean veto;
    private final String ap = "arbitraryProperty";
    private final String dp = "definedProperty";
    private final String aip = "arbitraryIndexedProperty";
    private final String dip = "definedIndexedProperty";

    @BeforeEach
    public void setUp() {
        bean = new ConstrainedArbitraryBeanImpl();
        changed = false;
        vetoer = (e -> {
            if (!veto) {
                changed = true;
            } else {
                veto = false;
                throw new PropertyVetoException("veto", e);
            }
        });
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    /**
     * Test of setProperty method, of class ConstrainedArbitraryBean.
     */
    @Test
    public void testSetProperty() {
        bean.addVetoableChangeListener(vetoer);
        assertThat(changed).isFalse();
        assertThat(bean.hasProperty(ap)).isFalse();
        assertThat(bean.hasProperty(dp)).isTrue();
        assertThat(bean.getProperty(ap)).isEqualTo(null);
        assertThat(bean.getProperty(dp)).isEqualTo(null);
        veto = false;
        assertThatCode(() -> bean.setProperty(ap, ap)).doesNotThrowAnyException();
        assertThat(bean.hasProperty(ap)).isTrue();
        assertThat(bean.getProperty(ap)).isEqualTo(ap);
        assertThat(changed).isTrue();
        assertThat(veto).isFalse();
        changed = false;
        veto = true;
        assertThatCode(() -> bean.setProperty(ap, null)).doesNotThrowAnyException();
        assertThat(bean.getProperty(ap)).isEqualTo(ap);
        assertThat(changed).isFalse();
        assertThat(veto).isFalse();
        changed = false;
        veto = false;
        assertThatCode(() -> bean.setProperty(dp, dp)).doesNotThrowAnyException();
        assertThat(bean.getProperty(dp)).isEqualTo(dp);
        assertThat(changed).isTrue();
        assertThat(veto).isFalse();
        changed = false;
        veto = true;
        assertThatCode(() -> bean.setProperty(dp, null)).doesNotThrowAnyException();
        assertThat(bean.getProperty(dp)).isEqualTo(dp);
        assertThat(changed).isFalse();
        assertThat(veto).isFalse();
    }

    /**
     * Test of setIndexedProperty method, of class ConstrainedArbitraryBean.
     */
    @Test
    public void testSetIndexedProperty() {
        bean.addVetoableChangeListener(vetoer);
        assertThat(changed).isFalse();
        assertThat(bean.hasIndexedProperty(aip)).isFalse();
        assertThat(bean.hasIndexedProperty(dip)).isTrue();
        assertThat(bean.getIndexedProperty(aip, 0)).isEqualTo(null);
        assertThatThrownBy(() -> bean.getIndexedProperty(dip, 0)).isExactlyInstanceOf(IndexOutOfBoundsException.class);
        veto = false;
        assertThatCode(() -> bean.setIndexedProperty(aip, 0, aip)).doesNotThrowAnyException();
        assertThat(bean.hasIndexedProperty(aip)).isTrue();
        assertThat(bean.getIndexedProperty(aip, 0)).isEqualTo(aip);
        assertThat(changed).isTrue();
        assertThat(veto).isFalse();
        changed = false;
        veto = true;
        assertThatCode(() -> bean.setIndexedProperty(aip, 0, null)).doesNotThrowAnyException();
        assertThat(bean.getIndexedProperty(aip, 0)).isEqualTo(aip);
        assertThat(changed).isFalse();
        assertThat(veto).isFalse();
        changed = false;
        veto = false;
        assertThatCode(() -> bean.setIndexedProperty(dip, 0, dip)).doesNotThrowAnyException();
        assertThat(bean.getIndexedProperty(dip, 0)).isEqualTo(dip);
        assertThat(changed).isTrue();
        assertThat(veto).isFalse();
        changed = false;
        veto = true;
        assertThatCode(() -> bean.setIndexedProperty(dip, 0, null)).doesNotThrowAnyException();
        assertThat(bean.getIndexedProperty(dip, 0)).isEqualTo(dip);
        assertThat(changed).isFalse();
        assertThat(veto).isFalse();
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

    public class ConstrainedArbitraryBeanImpl extends ConstrainedArbitraryBean {

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

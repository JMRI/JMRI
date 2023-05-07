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

import org.junit.jupiter.api.*;

/**
 * @author Randall Wood Copyright 2020
 */
public class ConstrainedArbitraryBeanTest {

    private ConstrainedArbitraryBeanImpl bean;
    private VetoableChangeListener vetoer;
    private boolean changed;
    private boolean veto;
    private final static String AP = "arbitraryProperty";
    private final static String DP = "definedProperty";
    private final static String AIP = "arbitraryIndexedProperty";
    private final static String DIP = "definedIndexedProperty";

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
        bean = null;
        JUnitUtil.tearDown();
    }

    /**
     * Test of setProperty method, of class ConstrainedArbitraryBean.
     */
    @Test
    public void testSetProperty() {
        bean.addVetoableChangeListener(vetoer);
        assertThat(changed).isFalse();
        assertThat(bean.hasProperty(AP)).isFalse();
        assertThat(bean.hasProperty(DP)).isTrue();
        assertThat(bean.getProperty(AP)).isEqualTo(null);
        assertThat(bean.getProperty(DP)).isEqualTo(null);
        veto = false;
        assertThatCode(() -> bean.setProperty(AP, AP)).doesNotThrowAnyException();
        assertThat(bean.hasProperty(AP)).isTrue();
        assertThat(bean.getProperty(AP)).isEqualTo(AP);
        assertThat(changed).isTrue();
        assertThat(veto).isFalse();
        changed = false;
        veto = true;
        assertThatCode(() -> bean.setProperty(AP, null)).doesNotThrowAnyException();
        assertThat(bean.getProperty(AP)).isEqualTo(AP);
        assertThat(changed).isFalse();
        assertThat(veto).isFalse();
        changed = false;
        veto = false;
        assertThatCode(() -> bean.setProperty(DP, DP)).doesNotThrowAnyException();
        assertThat(bean.getProperty(DP)).isEqualTo(DP);
        assertThat(changed).isTrue();
        assertThat(veto).isFalse();
        changed = false;
        veto = true;
        assertThatCode(() -> bean.setProperty(DP, null)).doesNotThrowAnyException();
        assertThat(bean.getProperty(DP)).isEqualTo(DP);
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
        assertThat(bean.hasIndexedProperty(AIP)).isFalse();
        assertThat(bean.hasIndexedProperty(DIP)).isTrue();
        assertThat(bean.getIndexedProperty(AIP, 0)).isEqualTo(null);
        assertThatThrownBy(() -> bean.getIndexedProperty(DIP, 0)).isExactlyInstanceOf(IndexOutOfBoundsException.class);
        veto = false;
        assertThatCode(() -> bean.setIndexedProperty(AIP, 0, AIP)).doesNotThrowAnyException();
        assertThat(bean.hasIndexedProperty(AIP)).isTrue();
        assertThat(bean.getIndexedProperty(AIP, 0)).isEqualTo(AIP);
        assertThat(changed).isTrue();
        assertThat(veto).isFalse();
        changed = false;
        veto = true;
        assertThatCode(() -> bean.setIndexedProperty(AIP, 0, null)).doesNotThrowAnyException();
        assertThat(bean.getIndexedProperty(AIP, 0)).isEqualTo(AIP);
        assertThat(changed).isFalse();
        assertThat(veto).isFalse();
        changed = false;
        veto = false;
        assertThatCode(() -> bean.setIndexedProperty(DIP, 0, DIP)).doesNotThrowAnyException();
        assertThat(bean.getIndexedProperty(DIP, 0)).isEqualTo(DIP);
        assertThat(changed).isTrue();
        assertThat(veto).isFalse();
        changed = false;
        veto = true;
        assertThatCode(() -> bean.setIndexedProperty(DIP, 0, null)).doesNotThrowAnyException();
        assertThat(bean.getIndexedProperty(DIP, 0)).isEqualTo(DIP);
        assertThat(changed).isFalse();
        assertThat(veto).isFalse();
    }

    @Test
    public void testGetPropertyNames() {
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

    @Test
    public void testBeanImpl() {
        bean.setDefinedProperty(AIP);
        Assertions.assertEquals(AIP, bean.getDefinedProperty() );
        
        String[] A_STRING_ARRAY = new String[]{AP, DP, AIP, DIP};
        bean.setDefinedIndexedProperty(A_STRING_ARRAY);
        Assertions.assertArrayEquals(A_STRING_ARRAY, bean.getDefinedIndexedProperty());
        Assertions.assertEquals(AIP, bean.getDefinedIndexedProperty(2));
        
        bean.setDefinedIndexedProperty(2, DIP);
        Assertions.assertEquals(DIP, bean.getDefinedIndexedProperty(2));
    }

    private static class ConstrainedArbitraryBeanImpl extends ConstrainedArbitraryBean {

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

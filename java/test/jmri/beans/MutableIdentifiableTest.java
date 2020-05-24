package jmri.beans;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jmri.util.JUnitUtil;

/**
 * @author Randall Wood Copyright 2020
 */
public class MutableIdentifiableTest {

    private MutableIdentifiable bean;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        bean = new MutableIdentifiable() {

            private String id = "bean";
            @Override
            public String getId() {
                return id;
            }

            @Override
            public void setId(String id) {
                this.id = id;
            }
        };
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    @Test
    public void testSetId() {
        assertThat(bean.getId()).isEqualTo("bean");
        bean.setId("changed");
        assertThat(bean.getId()).isEqualTo("changed");
    }

}

package jmri.beans;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jmri.util.JUnitUtil;

/**
 * @author Randall Wood Copyright 2020
 */
public class IdentifiedBeanTest {
    
    private IdentifiedBean bean;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        bean = new IdentifiedBean() {

            @Override
            public String getId() {
                return "bean";
            }};
    }
    
    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    @Test
    public void testGetId() {
        assertThat(bean.getId()).isEqualTo("bean");
    }
    
}

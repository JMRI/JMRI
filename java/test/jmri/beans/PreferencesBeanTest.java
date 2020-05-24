package jmri.beans;

import static org.assertj.core.api.Assertions.assertThat;

import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.nio.file.Path;

import jmri.profile.NullProfile;
import jmri.profile.Profile;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * @author Randall Wood Copyright 2020
 */
public class PreferencesBeanTest {

    @TempDir
    Path profilePath;
    private Profile profile;
    private PreferencesBeanImpl bean;
    private boolean changed;

    @BeforeEach
    public void setUp() throws IOException {
        JUnitUtil.setUp();
        changed = false;
        profile = new NullProfile(profilePath.toFile());
        bean = new PreferencesBeanImpl(profile);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    @Test
    public void testGetProfile() {
        assertThat(bean.getProfile()).isEqualTo(profile);
    }

    @Test
    public void testSetRestartRequired() {
        bean.addPropertyChangeListener(PreferencesBean.RESTART_REQUIRED, (e) -> changed = true);
        assertThat(changed).isFalse();
        assertThat(bean.isRestartRequired()).isFalse();
        bean.setRestartRequired();
        assertThat(changed).isTrue();
        assertThat(bean.isRestartRequired()).isTrue();
        changed = false;
        bean.setRestartRequired();
        assertThat(changed).isFalse();
        assertThat(bean.isRestartRequired()).isTrue();
    }

    @Test
    public void testSetIsDirty() {
        bean.addPropertyChangeListener(PreferencesBean.DIRTY, (e) -> changed = true);
        assertThat(changed).isFalse();
        assertThat(bean.isDirty()).isFalse();
        bean.setIsDirty(true);
        assertThat(changed).isTrue();
        assertThat(bean.isDirty()).isTrue();
        changed = false;
        bean.setIsDirty(false);
        assertThat(changed).isTrue();
        assertThat(bean.isDirty()).isFalse();
        changed = false;
        bean.setIsDirty(false);
        assertThat(changed).isFalse();
        assertThat(bean.isDirty()).isFalse();
    }

    @Test
    public void testFirePropertyChange_String_boolean_boolean() {
        bean.addPropertyChangeListener("boolean", (e) -> changed = true);
        assertThat(changed).isFalse();
        assertThat(bean.b).isFalse();
        assertThat(bean.isDirty()).isFalse();
        bean.setBoolean(true);
        assertThat(changed).isTrue();
        assertThat(bean.b).isTrue();
        assertThat(bean.isDirty()).isTrue();
        changed = false;
        bean.setIsDirty(false);
        bean.setBoolean(true);
        assertThat(changed).isFalse();
        assertThat(bean.b).isTrue();
        assertThat(bean.isDirty()).isFalse();
        changed = false;
        bean.setBoolean(false);
        assertThat(changed).isTrue();
        assertThat(bean.b).isFalse();
    }

    @Test
    public void testFirePropertyChange_String_int_int() {
        bean.addPropertyChangeListener("int", (e) -> changed = true);
        assertThat(changed).isFalse();
        assertThat(bean.i).isEqualTo(0);
        assertThat(bean.isDirty()).isFalse();
        bean.setInt(1);
        assertThat(bean.isDirty()).isTrue();
        assertThat(changed).isTrue();
        assertThat(bean.i).isEqualTo(1);
        changed = false;
        bean.setIsDirty(false);
        bean.setInt(1);
        assertThat(changed).isFalse();
        assertThat(bean.i).isEqualTo(1);
        assertThat(bean.isDirty()).isFalse();
        changed = false;
        bean.setInt(0);
        assertThat(changed).isTrue();
        assertThat(bean.i).isEqualTo(0);
    }

    @Test
    public void testFirePropertyChange_String_Object_Object() {
        bean.addPropertyChangeListener("Object", (e) -> changed = true);
        assertThat(changed).isFalse();
        assertThat(bean.o).isNull();
        assertThat(bean.isDirty()).isFalse();
        bean.setObject(profilePath);
        assertThat(bean.isDirty()).isTrue();
        assertThat(changed).isTrue();
        assertThat(bean.o).isEqualTo(profilePath);
        changed = false;
        bean.setIsDirty(false);
        bean.setObject(profilePath);
        assertThat(changed).isFalse();
        assertThat(bean.o).isEqualTo(profilePath);
        assertThat(bean.isDirty()).isFalse();
        changed = false;
        bean.setObject(null);
        assertThat(changed).isTrue();
        assertThat(bean.o).isNull();
    }

    @Test
    public void testFirePropertyChange_PropertyChangeEvent() {
        bean.addPropertyChangeListener("Event", e -> changed = true);
        assertThat(changed).isFalse();
        assertThat(bean.isDirty()).isFalse();
        bean.firePropertyChange(new PropertyChangeEvent(bean, "Event", null, null));
        assertThat(changed).isTrue();
        assertThat(bean.isDirty()).isTrue();
    }

    private class PreferencesBeanImpl extends PreferencesBean {

        public PreferencesBeanImpl(Profile prfl) {
            super(prfl);
        }

        boolean b = false;
        int i = 0;
        Object o = null;

        public void setBoolean(boolean b) {
            boolean old = this.b;
            this.b = b;
            firePropertyChange("boolean", old, b);
        }

        public void setInt(int i) {
            int old = this.i;
            this.i = i;
            firePropertyChange("int", old, i);
        }

        public void setObject(Object o) {
            Object old = this.o;
            this.o = o;
            firePropertyChange("Object", old, o);
        }
    }

}

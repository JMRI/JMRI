package jmri.swing;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;

import com.alexandriasoftware.swing.JInputValidator;
import com.alexandriasoftware.swing.Validation;

import org.assertj.swing.edt.GuiActionRunner;
import org.junit.jupiter.api.*;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.NamedBean.DisplayOptions;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 *
 * @author Bob Jacobsen Copyright (C) 2017
 * @author Randall Wood Copyright (C) 2019
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class NamedBeanComboBoxTest {

    @Test
    public void testSensorSimpleCtor() {
        Manager<Sensor> m = InstanceManager.getDefault(SensorManager.class);
        NamedBeanComboBox<Sensor> t = GuiActionRunner.execute(() -> new NamedBeanComboBox<>(m));
        assertThat(t).as("exists").isNotNull();
    }

    @Test
    public void testSensorFullCtor() {
        SensorManager m = InstanceManager.getDefault(SensorManager.class);
        m.provideSensor("IS1").setUserName("Sensor 1");
        Sensor s = m.provideSensor("IS2");
        s.setUserName("Sensor 2");
        m.provideSensor("IS3").setUserName("Sensor 3");

        NamedBeanComboBox<Sensor> t = GuiActionRunner.execute(() -> new NamedBeanComboBox<>(m, s, DisplayOptions.DISPLAYNAME));

        Assertions.assertNotNull(t);
        assertThat(t.getSelectedItem()).isEqualTo(s);
        assertThat(t.getSelectedItemUserName()).isEqualTo("Sensor 2");
        assertThat(t.getSelectedItemSystemName()).isEqualTo("IS2");
        // Display name is user name if present
        assertThat(t.getSelectedItemDisplayName()).isEqualTo("Sensor 2");

        GuiActionRunner.execute(() -> t.setSelectedItem(null));
        assertThat(t.getSelectedItemUserName()).isNull();
        assertThat(t.getSelectedItemSystemName()).isNull();
        assertThat(t.getSelectedItemDisplayName()).isNull();
    }

    @Test
    public void testSensorSelectEntry() {
        SensorManager m = InstanceManager.getDefault(SensorManager.class);
        Sensor s1 = m.provideSensor("IS1");
        s1.setUserName("Sensor 1");
        Sensor s2 = m.provideSensor("IS2");
        s2.setUserName("Sensor 2");
        Sensor s3 = m.provideSensor("IS3");
        s3.setUserName("Sensor 3");

        NamedBeanComboBox<Sensor> t = GuiActionRunner.execute(() -> {
            NamedBeanComboBox<Sensor> b = new NamedBeanComboBox<>(m, s2, DisplayOptions.DISPLAYNAME);
            assertThat(b.getSelectedItem()).isEqualTo(s2);
            b.setSelectedItem(s3);
            return b;
        });
        Assertions.assertNotNull(t);

        assertThat(t.getSelectedItem()).isEqualTo(s3);
        assertThat(t.getSelectedItemUserName()).isEqualTo("Sensor 3");
        assertThat(t.getSelectedItemSystemName()).isEqualTo("IS3");
        // Display name is user name if present
        assertThat(t.getSelectedItemDisplayName()).isEqualTo("Sensor 3");
    }

    @Test
    public void testSensorUserNameMixThatCausedProblems() {
        // add an LS manager
        ((jmri.managers.ProxySensorManager) InstanceManager.getDefault(SensorManager.class)).getDefaultManager();
        var lsm = new jmri.jmrix.internal.InternalSensorManager(
                    new jmri.jmrix.internal.InternalSystemConnectionMemo("L", "LocoNet"));
        ((jmri.managers.ProxySensorManager) InstanceManager.getDefault(SensorManager.class)).addManager(lsm);
        SensorManager m = InstanceManager.getDefault(jmri.SensorManager.class);

        Sensor is101 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS101");
        is101.setUserName("IS 101");
        Sensor is102 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS102");
        is102.setUserName("IS102");

        Sensor clock = InstanceManager.getDefault(SensorManager.class).provideSensor ("ISCLOCKRUNNING");

        Sensor ls101 = InstanceManager.getDefault(SensorManager.class).provideSensor("LS101");
        ls101.setUserName("LS 101");
        Sensor ls102 = InstanceManager.getDefault(SensorManager.class).provideSensor("LS102");
        ls102.setUserName("LS102");

        NamedBeanComboBox<Sensor> t = GuiActionRunner.execute(() -> {
            NamedBeanComboBox<Sensor> b = new NamedBeanComboBox<>(m, is101, DisplayOptions.DISPLAYNAME);
            assertThat(b.getSelectedItem()).isEqualTo(is101);
            b.setSelectedItem(is102);
            return b;
        });
        Assertions.assertNotNull(t);

        assertThat(t.getSelectedItem()).isEqualTo(is102);
        assertThat(t.getSelectedItemUserName()).isEqualTo("IS102");
        assertThat(t.getSelectedItemSystemName()).isEqualTo("IS102");
        // Display name is user name if present
        assertThat(t.getSelectedItemDisplayName()).isEqualTo("IS102");

        // check that they're all there
        assertThat(t.getItemCount()).isEqualTo(5);

        GuiActionRunner.execute(() -> {
            t.setSelectedIndex(0);

        });
        assertThat(t.getSelectedItem()).isEqualTo(is102);

        GuiActionRunner.execute(() -> {
            t.setSelectedIndex(1);

        });
        assertThat(t.getSelectedItem()).isEqualTo(is101);

        GuiActionRunner.execute(() -> {
            t.setSelectedIndex(4);

        });
        assertThat(t.getSelectedItem()).isEqualTo(clock);
    }

    @Test
    public void testSensorExcludeSome() {
        SensorManager m = InstanceManager.getDefault(SensorManager.class);
        Sensor s1 = m.provideSensor("IS1");
        s1.setUserName("Sensor 1");
        Sensor s2 = m.provideSensor("IS2");
        s2.setUserName("Sensor 2");
        Sensor s3 = m.provideSensor("IS3");
        s3.setUserName("Sensor 3");
        Sensor s4 = m.provideSensor("IS4");
        s4.setUserName("Sensor 4");

        GuiActionRunner.execute(() -> {
            NamedBeanComboBox<Sensor> t = new NamedBeanComboBox<>(m, s2, DisplayOptions.DISPLAYNAME);

            assertThat(t.getItemCount()).isEqualTo(4);
            assertThat(t.getSelectedItem()).isEqualTo(s2);

            t.setExcludedItems(new HashSet<>(Arrays.asList(new Sensor[]{s4})));
            assertThat(t.getExcludedItems()).isNotNull();

            assertThat(t.getItemCount()).isEqualTo(3);
            assertThat(t.getSelectedItem()).isEqualTo(s2);

            t.setExcludedItems(new HashSet<>(Arrays.asList(new Sensor[]{s2, s4})));

            assertThat(t.getItemCount()).isEqualTo(2);
            // confirm selection changed from s2
            assertThat(t.getSelectedItem()).isNotEqualTo(s2);
        });
    }

    @Test
    public void testSensorChangeDisplayMode() {
        SensorManager m = InstanceManager.getDefault(SensorManager.class);
        Sensor s1 = m.provideSensor("IS1");
        s1.setUserName("Sensor 1");

        GuiActionRunner.execute(() -> {
            NamedBeanComboBox<Sensor> t = new NamedBeanComboBox<>(m, s1, DisplayOptions.DISPLAYNAME);
            assertThat(t).isNotNull();
            JList<Sensor> l = new JList<>(t.getModel());
            assertThat(t).as("exists").isNotNull();
            assertThat(t.getDisplayOrder()).isEqualTo(DisplayOptions.DISPLAYNAME);
            assertThat(((JLabel) t.getRenderer().getListCellRendererComponent(l, s1, 0, false, false)).getText()).isEqualTo("Sensor 1");
            t.setDisplayOrder(DisplayOptions.SYSTEMNAME);
            assertThat(t.getDisplayOrder()).isEqualTo(DisplayOptions.SYSTEMNAME);
            assertThat(((JLabel) t.getRenderer().getListCellRendererComponent(l, s1, 0, false, false)).getText()).isEqualTo("IS1");
            t.setDisplayOrder(DisplayOptions.USERNAME_SYSTEMNAME);
            assertThat(t.getDisplayOrder()).isEqualTo(DisplayOptions.USERNAME_SYSTEMNAME);
            assertThat(((JLabel) t.getRenderer().getListCellRendererComponent(l, s1, 0, false, false)).getText()).isEqualTo("Sensor 1 (IS1)");
        });
    }

    @Test
    public void testSensorSetAndDefaultValidate() {
        Manager<Sensor> m = InstanceManager.getDefault(SensorManager.class);
        NamedBeanComboBox<Sensor> t = GuiActionRunner.execute(() -> new NamedBeanComboBox<>(m));
        Assertions.assertNotNull(t);

        assertThat(t.isValidatingInput()).isTrue();

        t.setValidatingInput(false);
        assertThat(t.isValidatingInput()).isFalse();

        t.setValidatingInput(true);
        assertThat(t.isValidatingInput()).isTrue();

    }

    private int countContents;
    private int countAdded;
    private int countRemoved;
    private Manager.ManagerDataEvent<Sensor> lastEvent;

    @Test
    public void testDataUpdatesForNewDataModel() {
        SensorManager m = InstanceManager.getDefault(SensorManager.class);

        Manager.ManagerDataListener<Sensor> listener = new Manager.ManagerDataListener<Sensor>() {
            @Override
            public void contentsChanged(Manager.ManagerDataEvent<Sensor> e) {
                countContents++;
                lastEvent = e;
            }

            @Override
            public void intervalAdded(Manager.ManagerDataEvent<Sensor> e) {
                countAdded++;
                lastEvent = e;
            }

            @Override
            public void intervalRemoved(Manager.ManagerDataEvent<Sensor> e) {
                countRemoved++;
                lastEvent = e;
            }
        };
        m.addDataListener(listener);
        countContents = countAdded = countRemoved = 0;
        lastEvent = null;

        GuiActionRunner.execute(() -> m.provideSensor("IS2"));

        assertThat(countContents).isEqualTo(0);
        assertThat(countAdded).isEqualTo(1);
        assertThat(countRemoved).isEqualTo(0);

        Assertions.assertNotNull(lastEvent);
        assertThat(lastEvent.getIndex0()).isEqualTo(0);  // new element 0
        assertThat(lastEvent.getIndex1()).isEqualTo(0);

        countContents = countAdded = countRemoved = 0;
        lastEvent = null;

        GuiActionRunner.execute(() -> m.provideSensor("IS3"));

        assertThat(countContents).isEqualTo(0);
        assertThat(countAdded).isEqualTo(1);
        assertThat(countRemoved).isEqualTo(0);

        Assertions.assertNotNull(lastEvent);
        assertThat(lastEvent.getIndex0()).isEqualTo(1);  // new element 1
        assertThat(lastEvent.getIndex1()).isEqualTo(1);

        countContents = countAdded = countRemoved = 0;
        lastEvent = null;

        GuiActionRunner.execute(() -> m.provideSensor("IS1"));

        assertThat(countContents).isEqualTo(0);
        assertThat(countAdded).isEqualTo(1);
        assertThat(countRemoved).isEqualTo(0);

        Assertions.assertNotNull(lastEvent);
        assertThat(lastEvent.getIndex0()).isEqualTo(0);  // new element 0
        assertThat(lastEvent.getIndex1()).isEqualTo(0);
    }

    @Test
    public void testSensorAllowEdit() {
        SensorManager m = InstanceManager.getDefault(jmri.SensorManager.class);
        assertThat(m.getNamedBeanSet().isEmpty()).isTrue();
        NamedBeanComboBox<Sensor> t = GuiActionRunner.execute(() -> new NamedBeanComboBox<>(m));
        Assertions.assertNotNull(t);
        assertThat(t.isAllowNull()).isFalse();
        assertThat(t.getModel().getSize()).isEqualTo(0);
        GuiActionRunner.execute(() -> {
            t.setDisplayOrder(DisplayOptions.SYSTEMNAME);
            t.setAllowNull(true);
        });
        assertThat(t.isAllowNull()).isTrue();
        assertThat(t.getModel().getSize()).isEqualTo(0);
        Sensor s1 = GuiActionRunner.execute(() -> m.provideSensor("IS1"));
        assertThat(t.isAllowNull()).isTrue();
        assertThat(t.getModel().getSize()).isEqualTo(2);
        assertThat(t.getItemAt(0)).isNull();
        assertThat(t.getItemAt(1)).isEqualTo(s1);
        GuiActionRunner.execute(() -> t.setAllowNull(false));
        assertThat(t.isAllowNull()).isFalse();
        assertThat(t.getModel().getSize()).isEqualTo(1);
        assertThat(t.getItemAt(0)).isEqualTo(s1);
        GuiActionRunner.execute(() -> t.setAllowNull(true));
        assertThat(t.isAllowNull()).isTrue();
        assertThat(t.getModel().getSize()).isEqualTo(2);
        assertThat(t.getItemAt(0)).isNull();
        assertThat(t.getItemAt(1)).isEqualTo(s1);
    }

    @Test
    public void testSensorEditText()
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
            SecurityException {
        SensorManager m = InstanceManager.getDefault(jmri.SensorManager.class);
        Sensor s1 = m.provideSensor("IS1");
        s1.setUserName("Sensor 1");
        Sensor s2 = m.provideSensor("IS2");
        s2.setUserName("Sensor 2");
        Sensor s3 = m.provideSensor("IS3");
        s3.setUserName("Sensor 3");

        GuiActionRunner.execute(() -> {
            NamedBeanComboBox<Sensor> t = new NamedBeanComboBox<>(m);
            t.setDisplayOrder(DisplayOptions.SYSTEMNAME);
            t.setAllowNull(true);
            t.setEditable(true);
            JTextField c = ((JTextField) t.getEditor().getEditorComponent());
            assertThat(c.getText()).isEqualTo("");
            c.setText("IS2");
            assertThat(c.getText()).isEqualTo("IS2");
            assertThat(t.getSelectedItem()).isEqualTo(s2);
        });
    }

    @Test
    public void testSensorTestProvidingValidity()
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
            SecurityException {
        SensorManager m = InstanceManager.getDefault(jmri.SensorManager.class);
        NamedBeanComboBox<Sensor> t = GuiActionRunner.execute(() -> {
            NamedBeanComboBox<Sensor> b = new NamedBeanComboBox<>(m);
            b.setDisplayOrder(DisplayOptions.USERNAME_SYSTEMNAME);
            b.setAllowNull(true);
            b.setEditable(true);
            b.setProviding(true);
            return b;
        });
        Assertions.assertNotNull(t);
        JTextField c = GuiActionRunner.execute(() -> (JTextField) t.getEditor().getEditorComponent());
        Assertions.assertNotNull(c);

        // test with no matching bean and isValidatingInput() == false
        // should always match NONE
        t.setValidatingInput(false);

        GuiActionRunner.execute(() -> {
            c.setText("");
            c.getInputVerifier().verify(c);
        });
        assertThat(c.getText()).isEqualTo("");
        assertThat(((JInputValidator) c.getInputVerifier()).getValidation().getType()).isEqualTo(Validation.Type.NONE);
        assertThat(t.getSelectedItem()).isNull();

        GuiActionRunner.execute(() -> {
            c.setText("IS1");
            c.getInputVerifier().verify(c);
        });
        assertThat(c.getText()).isEqualTo("IS1");
        assertThat(((JInputValidator) c.getInputVerifier()).getValidation().getType()).isEqualTo(Validation.Type.NONE);
        Sensor s1 = GuiActionRunner.execute(() -> t.getSelectedItem());
        Assertions.assertNotNull(s1);
        assertThat(m.getBySystemName("IS1")).isEqualTo(s1);

        GuiActionRunner.execute(() -> {
            c.setText("K ");
            c.getInputVerifier().verify(c);
        });
        assertThat(c.getText()).isEqualTo("K ");
        assertThat(((JInputValidator) c.getInputVerifier()).getValidation().getType()).isEqualTo(Validation.Type.NONE);
        assertThat(t.getSelectedItem()).isEqualTo(s1); // selection did not change because of invalid input

        // clear manager
        m.deregister(s1);

        // test with no matching bean and isValidatingInput() == true
        // should match NONE when empty and DANGER otherwise
        t.setValidatingInput(true);

        GuiActionRunner.execute(() -> {
            c.setText("");
            c.getInputVerifier().verify(c);
        });
        assertThat(c.getText()).isEqualTo("");
        assertThat(((JInputValidator) c.getInputVerifier()).getValidation().getType()).isEqualTo(Validation.Type.NONE);
        assertThat(t.getSelectedItem()).isNull();

        GuiActionRunner.execute(() -> {
            c.setText("IS1");
            c.getInputVerifier().verify(c);
        });
        assertThat(c.getText()).isEqualTo("IS1");
        assertThat(((JInputValidator) c.getInputVerifier()).getValidation().getType()).isEqualTo(Validation.Type.INFORMATION);
        s1 = GuiActionRunner.execute(() -> t.getSelectedItem());
        Assertions.assertNotNull(s1);
        assertThat(m.getBySystemName("IS1")).isEqualTo(s1);

        GuiActionRunner.execute(() -> {
            c.setText("K ");
            c.getInputVerifier().verify(c);
        });
        assertThat(c.getText()).isEqualTo("K ");
        assertThat(((JInputValidator) c.getInputVerifier()).getValidation().getType()).isEqualTo(Validation.Type.DANGER);
        assertThat(t.getSelectedItem()).isEqualTo(s1); // selection did not change because of invalid input

        // clear manager
        m.deregister(s1);

        // test with a matching bean and isValidatingInput() == false
        // should always match NONE
        t.setValidatingInput(false);

        GuiActionRunner.execute(() -> {
            c.setText("");
            c.getInputVerifier().verify(c);
        });
        assertThat(c.getText()).isEqualTo("");
        assertThat(((JInputValidator) c.getInputVerifier()).getValidation().getType()).isEqualTo(Validation.Type.NONE);
        assertThat(t.getSelectedItem()).isNull();

        GuiActionRunner.execute(() -> {
            c.setText("IS1");
            c.getInputVerifier().verify(c);
        });
        assertThat(c.getText()).isEqualTo("IS1");
        assertThat(((JInputValidator) c.getInputVerifier()).getValidation().getType()).isEqualTo(Validation.Type.NONE);
        s1 = GuiActionRunner.execute(() -> t.getSelectedItem());
        Assertions.assertNotNull(s1);
        assertThat(m.getBySystemName("IS1")).isEqualTo(s1);

        GuiActionRunner.execute(() -> {
            c.setText("K ");
            c.getInputVerifier().verify(c);
        });
        assertThat(c.getText()).isEqualTo("K ");
        assertThat(((JInputValidator) c.getInputVerifier()).getValidation().getType()).isEqualTo(Validation.Type.NONE);
        assertThat(t.getSelectedItem()).isEqualTo(s1); // selection did not change because of invalid input

        // clear manager
        m.deregister(s1);

        // test with a matching bean and isValidatingInput() == true
        // should match DANGER with text "K " and NONE otherwise
        t.setValidatingInput(true);

        GuiActionRunner.execute(() -> {
            c.setText("");
            c.getInputVerifier().verify(c);
        });
        assertThat(c.getText()).isEqualTo("");
        assertThat(((JInputValidator) c.getInputVerifier()).getValidation().getType()).isEqualTo(Validation.Type.NONE);
        assertThat(t.getSelectedItem()).isNull();

        t.setSelectedItem(null); // change selection to verify selection changes
        assertThat(t.getSelectedItem()).isNull();
        GuiActionRunner.execute(() -> {
            c.setText("IS1");
            c.getInputVerifier().verify(c);
        });
        assertThat(c.getText()).isEqualTo("IS1");
        assertThat(((JInputValidator) c.getInputVerifier()).getValidation().getType()).isEqualTo(Validation.Type.INFORMATION);
        s1 = GuiActionRunner.execute(() -> t.getSelectedItem());
        assertThat(m.getBySystemName("IS1")).isEqualTo(s1);

        GuiActionRunner.execute(() -> {
            c.setText("K ");
            c.getInputVerifier().verify(c);
        });
        assertThat(c.getText()).isEqualTo("K ");
        assertThat(((JInputValidator) c.getInputVerifier()).getValidation().getType()).isEqualTo(Validation.Type.DANGER);
        assertThat(t.getSelectedItem()).isEqualTo(s1); // selection did not change because of invalid input
    }

    @Test
    public void testSensorTestNonProvidingValidity()
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
            SecurityException {
        SensorManager m = InstanceManager.getDefault(jmri.SensorManager.class);
        NamedBeanComboBox<Sensor> t = GuiActionRunner.execute(() -> {
            NamedBeanComboBox<Sensor> b = new NamedBeanComboBox<>(m);
            b.setDisplayOrder(DisplayOptions.USERNAME_SYSTEMNAME);
            b.setAllowNull(true);
            b.setEditable(true);
            b.setProviding(false);
            return b;
        });
        Assertions.assertNotNull(t);
        JTextField c = GuiActionRunner.execute(() -> ((JTextField) t.getEditor().getEditorComponent()));
        Assertions.assertNotNull(c);

        // test with no matching bean and isValidatingInput() == false
        // should always match NONE
        t.setValidatingInput(false);

        boolean v = GuiActionRunner.execute(() -> {
            c.setText("");
            return c.getInputVerifier().verify(c);
        });
        assertThat(c.getText()).as("Empty text").isEqualTo("");
        assertThat(((JInputValidator) c.getInputVerifier()).getValidation().getType()).as("Empty validates to NONE").isEqualTo(Validation.Type.NONE);
        assertThat(t.getSelectedItem()).as("Empty is not selected").isNull();
        assertThat(v).as("Empty is valid").isTrue();

        v = GuiActionRunner.execute(() -> {
            c.setText("IS1");
            return c.getInputVerifier().verify(c);
        });
        assertThat(c.getText()).as("IS1 text").isEqualTo("IS1");
        assertThat(((JInputValidator) c.getInputVerifier()).getValidation().getType()).as("IS1 validation is NONE (not existing/non-validating)").isEqualTo(Validation.Type.NONE);
        assertThat(t.getSelectedItem()).as("IS1 is not selected (not existing/non-validating)").isNull();
        assertThat(v).as("IS1 is valid (not existing/non-validating)").isTrue();

        v = GuiActionRunner.execute(() -> {
            c.setText("K ");
            return c.getInputVerifier().verify(c);
        });
        assertThat(c.getText()).as("K text").isEqualTo("K ");
        assertThat(((JInputValidator) c.getInputVerifier()).getValidation().getType()).as("K validation is NONE (non-validating)").isEqualTo(Validation.Type.NONE);
        assertThat(t.getSelectedItem()).as("No selection (no existing selection)").isNull();
        assertThat(v).as("K is valid (non-validating)").isTrue();

        // test with no matching bean and isValidatingInput() == true
        // should match NONE when empty and WARNING otherwise
        t.setValidatingInput(true);

        v = GuiActionRunner.execute(() -> {
            c.setText("");
            return c.getInputVerifier().verify(c);
        });
        assertThat(c.getText()).as("Empty text").isEqualTo("");
        assertThat(((JInputValidator) c.getInputVerifier()).getValidation().getType()).as("Empty validates to NONE").isEqualTo(Validation.Type.NONE);
        assertThat(t.getSelectedItem()).as("Empty is not selected").isNull();
        assertThat(v).as("Empty is valid").isTrue();

        v = GuiActionRunner.execute(() -> {
            c.setText("IS1");
            return c.getInputVerifier().verify(c);
        });
        assertThat(c.getText()).as("IS1 text").isEqualTo("IS1");
        assertThat(((JInputValidator) c.getInputVerifier()).getValidation().getType()).as("IS1 validation is WARNING (not existing/validating)").isEqualTo(Validation.Type.WARNING);
        assertThat(t.getSelectedItem()).as("IS1 is not selected (not existing/validating)").isNull();
        assertThat(v).as("IS1 is valid (not existing/validating)").isTrue();

        v = GuiActionRunner.execute(() -> {
            c.setText("K ");
            return c.getInputVerifier().verify(c);
        });
        assertThat(c.getText()).as("K text").isEqualTo("K ");
        assertThat(((JInputValidator) c.getInputVerifier()).getValidation().getType()).as("K validation is WARNING (validating)").isEqualTo(Validation.Type.WARNING);
        assertThat(t.getSelectedItem()).as("No selection (no existing selection)").isNull();
        assertThat(v).as("K is valid (validating)").isTrue();

        // test with a matching bean and isValidatingInput() == false
        // should always match NONE
        t.setValidatingInput(false);
        Sensor s = m.provide("IS1");

        v = GuiActionRunner.execute(() -> {
            c.setText("");
            return c.getInputVerifier().verify(c);
        });
        assertThat(c.getText()).as("Empty text").isEqualTo("");
        assertThat(((JInputValidator) c.getInputVerifier()).getValidation().getType()).as("Empty validates to NONE").isEqualTo(Validation.Type.NONE);
        assertThat(t.getSelectedItem()).as("Empty is not selected").isNull();
        assertThat(v).as("Empty is valid").isTrue();

        v = GuiActionRunner.execute(() -> {
            c.setText("IS1");
            return c.getInputVerifier().verify(c);
        });
        assertThat(c.getText()).as("IS1 text").isEqualTo("IS1");
        assertThat(((JInputValidator) c.getInputVerifier()).getValidation().getType()).as("IS1 validation is NONE (pre-existing/non-validating)").isEqualTo(Validation.Type.NONE);
        assertThat(t.getSelectedItem()).as("IS1 is selected (pre-existing)").isEqualTo(s);
        assertThat(v).as("IS1 is valid (pre-existing)").isTrue();

        v = GuiActionRunner.execute(() -> {
            c.setText("K ");
            return c.getInputVerifier().verify(c);
        });
        assertThat(c.getText()).as("K text").isEqualTo("K ");
        assertThat(((JInputValidator) c.getInputVerifier()).getValidation().getType()).as("K validation is NONE (non-validating)").isEqualTo(Validation.Type.NONE);
        assertThat(t.getSelectedItem()).as("IS1 is selected for K (pre-selected/non-validating)").isEqualTo(s); // selection did not change because of invalid input
        assertThat(v).as("K is valid (non-validating)").isTrue();

        // test with a matching bean and isValidatingInput() == true
        // should match WARNING with text "K " and NONE otherwise
        t.setValidatingInput(true);

        v = GuiActionRunner.execute(() -> {
            c.setText("");
            return c.getInputVerifier().verify(c);
        });
        assertThat(c.getText()).as("Empty text").isEqualTo("");
        assertThat(((JInputValidator) c.getInputVerifier()).getValidation().getType()).as("Empty validates to NONE").isEqualTo(Validation.Type.NONE);
        // selection did not change because of invalid input
        assertThat(t.getSelectedItem()).as("Empty is not selected").isEqualTo(s);
        assertThat(v).as("Empty is valid").isTrue();
        // change selection to verify selection changes
        GuiActionRunner.execute(() -> t.setSelectedItem(null));
        assertThat(t.getSelectedItem()).isNull();
        v = GuiActionRunner.execute(() -> {
            c.setText("IS1");
            return c.getInputVerifier().verify(c);
        });
        assertThat(c.getText()).as("IS1 text").isEqualTo("IS1");
        assertThat(((JInputValidator) c.getInputVerifier()).getValidation().getType()).as("IS1 validation is NONE (pre-existing/validating)").isEqualTo(Validation.Type.NONE);
        assertThat(t.getSelectedItem()).as("IS1 is selected (pre-existing)").isEqualTo(s);
        assertThat(v).as("IS1 is valid (pre-existing)").isTrue();

        v = GuiActionRunner.execute(() -> {
            c.setText("K ");
            return c.getInputVerifier().verify(c);
        });
        assertThat(c.getText()).as("K text").isEqualTo("K ");
        assertThat(((JInputValidator) c.getInputVerifier()).getValidation().getType()).as("K validation is WARNING (validating)").isEqualTo(Validation.Type.WARNING);
        assertThat(t.getSelectedItem()).as("IS1 is selected for K (pre-selected/validating)").isEqualTo(s); // selection did not change because of invalid input
        assertThat(v).as("K is valid (validating)").isTrue();
    }

    @Test
    public void testSensorSetBean() {
        SensorManager m = InstanceManager.getDefault(jmri.SensorManager.class);
        Sensor s1 = m.provideSensor("IS1");
        s1.setUserName("Sensor 1");
        Sensor s2 = m.provideSensor("IS2");
        s2.setUserName("Sensor 2");
        Sensor s3 = m.provideSensor("IS3");
        s3.setUserName("Sensor 3");
        Sensor s4 = m.provideSensor("IS4");
        s4.setUserName("Sensor 4");

        GuiActionRunner.execute(() -> {
            NamedBeanComboBox<Sensor> t = new NamedBeanComboBox<>(m, s1, DisplayOptions.DISPLAYNAME);

            assertThat(t.getSelectedItemDisplayName()).isEqualTo("Sensor 1");

            t.setSelectedItem(s2);
            assertThat(t.getSelectedItem()).isEqualTo(s2);

            t.setDisplayOrder(DisplayOptions.SYSTEMNAME);
            t.setSelectedItem(s3);
            assertThat(t.getSelectedItem()).isEqualTo(s3);

            t.setDisplayOrder(DisplayOptions.USERNAME_SYSTEMNAME);
            t.setSelectedItem(s4);
            assertThat(t.getSelectedItem()).isEqualTo(s4);

            t.setDisplayOrder(DisplayOptions.USERNAME);
            t.setSelectedItem(s2);
            assertThat(t.getSelectedItem()).isEqualTo(s2);
        });
    }

    @Test
    public void testSensorNameChange() {
        SensorManager m = InstanceManager.getDefault(jmri.SensorManager.class);
        Sensor s1 = m.provideSensor("IS1");

        NamedBeanComboBox<Sensor> t = GuiActionRunner.execute(() -> new NamedBeanComboBox<>(m, s1, DisplayOptions.DISPLAYNAME));
        Assertions.assertNotNull(t);
        assertThat(t.getSelectedItemDisplayName()).isEqualTo("IS1");

        s1.setUserName("Sensor 1");
        assertThat(t.getSelectedItemDisplayName()).isEqualTo("Sensor 1");

        s1.setUserName("new name");
        assertThat(t.getSelectedItemDisplayName()).isEqualTo("new name");
    }

    @Test
    public void testSensorAddTracking() {
        SensorManager m = InstanceManager.getDefault(jmri.SensorManager.class);
        Sensor s1 = m.provideSensor("IS1");
        s1.setUserName("Sensor 1");

        NamedBeanComboBox<Sensor> t = GuiActionRunner.execute(() -> new NamedBeanComboBox<>(m, s1, DisplayOptions.DISPLAYNAME));
        Assertions.assertNotNull(t);
        assertThat(t.getItemCount()).isEqualTo(1);

        GuiActionRunner.execute(() -> {
            Sensor s2 = m.provideSensor("IS2");
            s2.setUserName(null);
        });
        assertThat(t.getItemCount()).isEqualTo(2);

        GuiActionRunner.execute(() -> {
            Sensor s3 = m.provideSensor("IS3");
            s3.setUserName("Sensor 3");
        });
        assertThat(t.getItemCount()).isEqualTo(3);
    }

    @Test
    public void testIsProviding() {
        SensorManager m = InstanceManager.getDefault(jmri.SensorManager.class);
        NamedBeanComboBox<Sensor> t = GuiActionRunner.execute(() -> new NamedBeanComboBox<>(m));
        Assertions.assertNotNull(t);
        assertThat(t.isProviding()).isTrue();
        t.setProviding(false);
        assertThat(t.isProviding()).isFalse();
        t.setProviding(true);
        assertThat(t.isProviding()).isTrue();
    }

    @Test
    public void testGetManager() {
        SensorManager m = InstanceManager.getDefault(jmri.SensorManager.class);
        NamedBeanComboBox<Sensor> t = GuiActionRunner.execute(() -> new NamedBeanComboBox<>(m));
        Assertions.assertNotNull(t);
        assertThat(t.getManager()).as("Manager is as expected").isEqualTo(m);
    }

    @Test
    public void testDispose() {
        SensorManager m = InstanceManager.getDefault(jmri.SensorManager.class);
        assertThat(m.getPropertyChangeListeners().length).as("Manager has no listeners").isEqualTo(0);
        GuiActionRunner.execute(() -> {
            NamedBeanComboBox<Sensor> t = new NamedBeanComboBox<>(m);
            assertThat(m.getPropertyChangeListeners().length).as("Manager has two listeners").isEqualTo(2);
            t.dispose();
        });
        assertThat(m.getPropertyChangeListeners().length).as("Manager has no listeners").isEqualTo(0);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initInternalSensorManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(NamedBeanComboBoxTest.class);

}

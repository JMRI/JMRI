package jmri.implementation;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.profile.Profile;
import jmri.spi.PreferencesManager;
import jmri.util.JUnitUtil;
import jmri.util.prefs.AbstractPreferencesManager;
import jmri.util.prefs.InitializationException;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class JmriConfigurationManagerTest {

    private List<PreferencesManager> initialized;
    private JmriConfigurationManager jcm;

    @Test
    public void testCTor() {
        assertThat(jcm).as("exists").isNotNull();
    }

    /**
     * Test that the JmriConfigurationManager initializes in the expected order
     * and does not suffer a stack overflow given the five PreferencesManagers
     * below. PreferencesManager A & B should be the first two initialized,
     * followed by C & D, and E should be last. The order of A or B is
     * non-deterministic, as is the order of C or D.
     *
     * @throws jmri.JmriException in unexpected circumstances
     */
    @Test
    public void testInitializeProviders() throws JmriException {
        InstanceManager.getDefault().clear(PreferencesManager.class);
        PMA pma = new PMA();
        PMB pmb = new PMB();
        PMC pmc = new PMC();
        PMD pmd = new PMD();
        PME pme = new PME();
        // emulate using the service provider
        // note: storing not in execution order
        InstanceManager.store(pme, PreferencesManager.class);
        InstanceManager.store(pme, PME.class);
        InstanceManager.store(pmd, PreferencesManager.class);
        InstanceManager.store(pmd, PMD.class);
        InstanceManager.store(pma, PreferencesManager.class);
        InstanceManager.store(pma, PMA.class);
        InstanceManager.store(pmb, PreferencesManager.class);
        InstanceManager.store(pmb, PMB.class);
        InstanceManager.store(pmc, PreferencesManager.class);
        InstanceManager.store(pmc, PMC.class);
        assertThat(InstanceManager.getList(PreferencesManager.class)).containsExactly(pme, pmd, pma, pmb, pmc);
        jcm.load((URL) null, false);
        assertThat(initialized.size()).isEqualTo(5);
        assertThat(initialized.get(0)).isIn(pma, pmb);
        assertThat(initialized.get(1)).isIn(pma, pmb);
        assertThat(initialized.get(2)).isIn(pmc, pmd);
        assertThat(initialized.get(3)).isIn(pmc, pmd);
        assertThat(initialized.get(4)).isEqualTo(pme);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        initialized = new ArrayList<>();
        jcm = new JmriConfigurationManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    public class PMA extends AbstractPreferencesManager {

        @Override
        public void initialize(Profile profile) throws InitializationException {
            initialized.add(this);
            setInitialized(profile, true);
        }

        @Override
        public void savePreferences(Profile profile) {
            // nothing to do
        }

        @Override
        public Set<Class<? extends PreferencesManager>> getRequires() {
            return new HashSet<>();
        }
    }

    public class PMB extends AbstractPreferencesManager {

        @Override
        public void initialize(Profile profile) throws InitializationException {
            initialized.add(this);
            setInitialized(profile, true);
        }

        @Override
        public void savePreferences(Profile profile) {
            // nothing to do
        }

        @Override
        public Set<Class<? extends PreferencesManager>> getRequires() {
            return new HashSet<>();
        }
    }

    public class PMC extends AbstractPreferencesManager {

        @Override
        public void initialize(Profile profile) throws InitializationException {
            initialized.add(this);
            setInitialized(profile, true);
        }

        @Override
        public void savePreferences(Profile profile) {
            // nothing to do
        }

        @Override
        public Set<Class<? extends PreferencesManager>> getRequires() {
            return requireAllOther();
        }
    }

    public class PMD extends AbstractPreferencesManager {

        @Override
        public void initialize(Profile profile) throws InitializationException {
            initialized.add(this);
            setInitialized(profile, true);
        }

        @Override
        public void savePreferences(Profile profile) {
            // nothing to do
        }

        @Override
        public Set<Class<? extends PreferencesManager>> getRequires() {
            return requireAllOther();
        }
    }

    public class PME extends AbstractPreferencesManager {

        @Override
        public void initialize(Profile profile) throws InitializationException {
            initialized.add(this);
            setInitialized(profile, true);
        }

        @Override
        public void savePreferences(Profile profile) {
            // nothing to do
        }

        @Override
        public Set<Class<? extends PreferencesManager>> getRequires() {
            Set<Class<? extends PreferencesManager>> set = new HashSet<>();
            set.add(PMC.class);
            set.add(PMD.class);
            return set;
        }
    }
}

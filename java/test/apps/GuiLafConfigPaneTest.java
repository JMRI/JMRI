package apps;

import jmri.swing.PreferencesPanelTestBase;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class GuiLafConfigPaneTest extends PreferencesPanelTestBase<GuiLafConfigPane> {

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        prefsPanel = new GuiLafConfigPane();
    }

    @Override
    @Test
    public void isPersistant() {
        assertThat(prefsPanel.isPersistant()).isTrue();
    }

    // private final static Logger log = LoggerFactory.getLogger(GuiLafConfigPaneTest.class);

}

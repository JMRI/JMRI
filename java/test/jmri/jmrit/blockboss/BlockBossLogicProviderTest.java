package jmri.jmrit.blockboss;

import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.SignalHeadManager;
import jmri.implementation.VirtualSignalHead;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BlockBossLogicProviderTest {

    private BlockBossLogicProvider provider;
    private SignalHeadManager signalHeadManager;

    @BeforeEach
    public void setUp(){
        JUnitUtil.setUp();
        JUnitUtil.initInternalSignalHeadManager();
        provider = new BlockBossLogicProvider();
        signalHeadManager = InstanceManager.getDefault(SignalHeadManager.class);
        signalHeadManager.register(new VirtualSignalHead("IH1","signal head"));
    }

    @AfterEach
    public void tearDown(){
        provider = null;
        JUnitUtil.tearDown();
    }

    @Test
    public void GivenANewProvider_WhenGetEexistingByValidSignalHeadString_ThenABlockBossLogicObjectIsCreated_(){
        BlockBossLogic blockBossLogic = provider.provide("IH1");
        assertThat(blockBossLogic).isNotNull();
    }

    @Test
    public void GivenANewProvider_WhenGetEexistingByValidSignalHead_ThenABlockBossLogicObjectIsCreated_(){
        SignalHead signalHead = signalHeadManager.getSignalHead("IH1");
        BlockBossLogic blockBossLogic = provider.provide(signalHead);
        assertThat(blockBossLogic).isNotNull();
    }

}

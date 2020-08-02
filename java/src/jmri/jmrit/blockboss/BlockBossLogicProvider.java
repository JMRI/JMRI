package jmri.jmrit.blockboss;

import jmri.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Provider for {@link BlockBossLogic} objects
 *
 * @author Paul Bender Copyright (C) 2020
 */
public class BlockBossLogicProvider implements Disposable, InstanceManagerAutoDefault {

    private final SignalHeadManager signalHeadManager;
    private final Map<SignalHead,BlockBossLogic> headToBlockBossLogicMap;

    public BlockBossLogicProvider() {
        signalHeadManager = InstanceManager.getDefault(SignalHeadManager.class);
        headToBlockBossLogicMap = new HashMap<>();
        ConfigureManager cm = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        if (cm != null) {
            cm.registerConfig(this, jmri.Manager.BLOCKBOSS);
        }
    }

    public BlockBossLogic provide(@Nonnull String signalName) {
        SignalHead signalHead = signalHeadManager.getSignalHead(signalName);
        if (signalHead == null) {
            log.error("SignalHead {} doesn't exist, BlockBossLogic.getExisting(\"{}\") cannot continue", signalName, signalName);
            throw new IllegalArgumentException("Requested signal head doesn't exist");
        }
        return provide(signalHead);
    }

    public BlockBossLogic provide(@Nonnull SignalHead signalHead){
        if (signalHead == null) {
            log.error("BlockBossLogic requested for null signal head.");
            throw new IllegalArgumentException("BlockBossLogic Requested for null signal head.");
        }
        return headToBlockBossLogicMap.computeIfAbsent(signalHead,o -> new BlockBossLogic(o.getDisplayName()));
    }

    public void register(BlockBossLogic blockBossLogic){
        headToBlockBossLogicMap.put(blockBossLogic.driveSignal.getBean(),blockBossLogic);
    }

    public void remove(BlockBossLogic blockBossLogic){
        headToBlockBossLogicMap.remove(blockBossLogic.driveSignal.getBean(),blockBossLogic);
    }

    public Collection<BlockBossLogic> provideAll(){
        return headToBlockBossLogicMap.values();
    }

    public void dispose(){
        for (BlockBossLogic b : headToBlockBossLogicMap.values()) {
            b.stop();
        }
        headToBlockBossLogicMap.clear();
    }

    private static final Logger log = LoggerFactory.getLogger(BlockBossLogicProvider.class);

}

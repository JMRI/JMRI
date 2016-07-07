package jmri.jmris;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import jmri.Block;
import jmri.InstanceManager;
import jmri.JmriException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract interface between a JMRI block and a network connection
 *
 * @author mstevetodd Copyright (C) 2016 (copied from AbstractMemoryServer)
 * @author Randall Wood Copyright (C) 2013, 2014
 */
abstract public class AbstractBlockServer {

    private final HashMap<String, BlockListener> blocks;
    private static final Logger log = LoggerFactory.getLogger(AbstractBlockServer.class);

    public AbstractBlockServer() {
        blocks = new HashMap<String, BlockListener>();
    }

    /*
     * Protocol Specific Abstract Functions
     */
    abstract public void sendStatus(String block, String Status) throws IOException;

    abstract public void sendErrorStatus(String block) throws IOException;

    abstract public void parseStatus(String statusString) throws JmriException, IOException;

    synchronized protected void addBlockToList(String blockName) {
        if (!blocks.containsKey(blockName)) {
            blocks.put(blockName, new BlockListener(blockName));
            InstanceManager.blockManagerInstance().getBlock(blockName).addPropertyChangeListener(blocks.get(blockName));
        }
    }

    synchronized protected void removeBlockFromList(String blockName) {
        if (blocks.containsKey(blockName)) {
            InstanceManager.blockManagerInstance().getBlock(blockName).removePropertyChangeListener(blocks.get(blockName));
            blocks.remove(blockName);
        }
    }

    public Block initBlock(String blockName) throws IllegalArgumentException {
        Block block = InstanceManager.blockManagerInstance().provideBlock(blockName);
        this.addBlockToList(blockName);
        return block;
    }

    public void setBlockValue(String blockName, String blockValue) {
        Block block;
        try {
            addBlockToList(blockName);
            block = InstanceManager.blockManagerInstance().getBlock(blockName);
            if (block == null) {
                log.error("Block {} is not available", blockName);
            } else {
                if (!(block.getValue().equals(blockValue))) {
                    block.setValue(blockValue);
                } else {
                    try {
                        sendStatus(blockName, blockValue);
                    } catch (IOException ex) {
                        log.error("Error sending appearance", ex);
                    }
                }
            }
        } catch (Exception ex) {
            log.error("error setting block value", ex);
        }
    }

    public void dispose() {
        for (Map.Entry<String, BlockListener> block : this.blocks.entrySet()) {
            InstanceManager.blockManagerInstance().getBlock(block.getKey()).removePropertyChangeListener(block.getValue());
        }
        this.blocks.clear();
    }

    class BlockListener implements PropertyChangeListener {

        BlockListener(String blockName) {
            name = blockName;
            block = InstanceManager.blockManagerInstance().getBlock(blockName);
        }

        // update state as state of block changes
        @Override
        public void propertyChange(PropertyChangeEvent e) {
            if (e.getPropertyName().equals("value")) {
                String state = (String) e.getNewValue();
                try {
                    sendStatus(name, state);
                } catch (IOException ie) {
                    log.debug("Error sending status, removing listener from block {}", name);
                    // if we get an error, de-register
                    block.removePropertyChangeListener(this);
                    removeBlockFromList(name);
                }
            }
        }
        String name = null;
        Block block = null;
    }
}

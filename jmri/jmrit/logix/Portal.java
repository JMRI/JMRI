package jmri.jmrit.logix;

import java.util.ArrayList;
import java.util.List;

import jmri.Block;
import jmri.SignalHead;
//import jmri.Path;

/**
 * An Portal is a boundary between two Blocks.
 * 
 * <P>
 * A Portal has Lists of the OPaths that connect through it.
 *
 * @author	Pete Cressman  Copyright (C) 2009
 */
public class Portal  {

    private ArrayList <OPath> _fromPaths = new ArrayList <OPath>();
    private OBlock _fromBlock;
    private SignalHead _fromSignal;
    private ArrayList <OPath> _toPaths = new ArrayList <OPath>();
    private OBlock _toBlock;
    private SignalHead _toSignal;
    private String _portalName;
    /*
    public Portal(String name) {
        _portalName = name;
    } */
    public Portal(OBlock fromBlock, String portalName, OBlock toBlock) {
        _fromBlock = fromBlock;
        _portalName = portalName;
        _toBlock = toBlock;
        if (_fromBlock!=null) _fromBlock.addPortal(this);
        if (_toBlock!=null) _toBlock.addPortal(this);
        if (log.isDebugEnabled()) log.debug("Ctor: name= "+_portalName+", fromBlock= "+
                   getFromBlockName()+", toBlock= "+getToBlockName()); 
    }

    public Portal(SignalHead fromSig, OBlock fromBlock, String portalName, 
                  OBlock toBlock, SignalHead toSig) {
        this(fromBlock, portalName, toBlock);
        _fromSignal = fromSig;
        _toSignal = toSig;
    }

    /**
    * Determine which list the Path belongs to and add it to the list
    * @return false if Path does not have a matching block for this Portal
    */
    public boolean addPath(OPath path) {
        Block block = path.getBlock();
        if (block==null) {
            log.error("Path \""+path.getName()+"\" has no block.");
            return false;
        }
        if (!_portalName.equals(path.getFromPortalName()) &&
                !_portalName.equals(path.getToPortalName()) ){
            log.error("Path \""+path.getName()+"\" in block \""+block.getSystemName()+
                "\" does not pass through Portal \""+_portalName+"\".");
            return false;
        }
        if (_fromBlock != null && _fromBlock.equals(block)) {
            if (!_fromPaths.contains(path))  {
                return addPath(_fromPaths, path);
            }
        } else if (_toBlock != null && _toBlock.equals(block)) {
            if (!_toPaths.contains(path))  {
                return addPath(_toPaths, path);
            }
        } else {
            log.error("Path \""+path.getName()+"\" in block \""+block.getSystemName()+
                "\" is not in either of the blocks of Portal \""+_portalName+"\".");
        }
        return false;
    }

    /**
    *  Utility for both path lists
    */
    private boolean addPath(List <OPath> list, OPath path) {
        String pName =path.getName();
        for (int i=0; i<list.size(); i++) {
            if (pName.equals(list.get(i).getName())) { return false; }
        }
        list.add(path);
        return true;
    }

    public void setName(String name) {
        if (name == null || name.length()==0) { return; }
        if (_portalName.equals(name)) { return; }

        String oldName = _portalName;
        _portalName = name;

        changePathPortalName(_fromPaths, _portalName, oldName);
        changePathPortalName(_toPaths, _portalName, oldName);
        changeBlockPortalName(_fromBlock, _portalName, oldName);
        changeBlockPortalName(_toBlock, _portalName, oldName);
    }

    /**
    *  Utility for both path lists
    */
    private void changePathPortalName(List <OPath> pathList, 
                                         String newName, String oldName) {
        for (int i=0; i<pathList.size(); i++) {
            OPath path = pathList.get(i);
            if (oldName.equals(path.getFromPortalName())) {
                path.setFromPortalName(newName);
            }
            if (oldName.equals(path.getToPortalName())) {
                path.setToPortalName(newName);
            }
            changeBlockPortalName((OBlock)path.getBlock(), newName, oldName);
        }
    }

    /**
    * should not be necessary, but just in case portal
    * has more than one object representing it
    */
    private void changeBlockPortalName(OBlock block, 
                                       String newName, String oldName) {
        if (block!=null) {
            Portal portal = block.getPortalByName(oldName);
            if (portal!=null) { portal.setName(newName); }            
        }
    }

    public String getName() { return _portalName; }

    /**
    * Set block name. Verify that all toPaths are contained in the block.
    * @return false if paths are not in the block
    */
    public boolean setToBlock(OBlock block, boolean changePaths) {
        if ((block!=null && block.equals(_toBlock)) || (block==null && _toBlock==null)) {
            return true;
        }
        if (changePaths) {
            //Switch paths to new block.  User will need to verify connections
            for (int i=0; i<_toPaths.size(); i++) {
                    _toPaths.get(i).setBlock(block);
            }
        } else if (!verify(_toPaths, block)) {
            return false;
        }
        log.debug("setToBlock: oldBlock= \""+getToBlockName()
                  +"\" newBlock \""+(block!=null ? block.getDisplayName() : null)+"\".");
        if (_toBlock!=null) { _toBlock.removePortal(this); }
        _toBlock = block;
        if (_toBlock!=null) { _toBlock.addPortal(this); }
        return true;
    }
    public OBlock getToBlock() { return _toBlock;  }
    public String getToBlockName() { return (_toBlock!=null ? _toBlock.getDisplayName() : null); }
    public List <OPath> getToPaths() { return _toPaths; }

    /**
    * Set block name. Verify that all toPaths are contained in the block.
    * @return false if paths are not in the block
    */
    public boolean setFromBlock(OBlock block, boolean changePaths) {
        if ((block!=null && block.equals(_fromBlock)) || (block==null && _fromBlock==null)) {
            return true;
        }
        if (changePaths) {
            //Switch paths to new block.  User will need to verify connections
            for (int i=0; i<_fromPaths.size(); i++) {
                    _fromPaths.get(i).setBlock(block);
            }
        }
        if (!verify(_fromPaths, block)) {
            return false;
        }
        log.debug("setFromBlock: oldBlock= \""+getFromBlockName()+
                  "\" newBlock \""+(block!=null ? block.getDisplayName() : null)+"\".");
        if (_fromBlock!=null) { _fromBlock.removePortal(this); }
        _fromBlock = block;
        if (_fromBlock!=null) { _fromBlock.addPortal(this); }
        return true;
    }
    public OBlock getFromBlock() { return _fromBlock;  }
    public String getFromBlockName() { return (_fromBlock!=null ? _fromBlock.getDisplayName() : null);  }
    public List <OPath> getFromPaths() { return _fromPaths;  }

    public SignalHead getFromSignal() {  return _fromSignal; }
    public String getFromSignalName() {  return (_fromSignal!=null ? _fromSignal.getDisplayName() : null);  }
    public void setFromSignal(SignalHead signal) {  _fromSignal = signal;  }

    public SignalHead getToSignal() { return _toSignal; }
    public String getToSignalName() { return (_toSignal!=null ? _toSignal.getDisplayName() : null);  }
    public void setToSignal(SignalHead signal) { _toSignal = signal; }

    /**
    * Get the paths to the portal within the connected Block
    * i.e. the paths in this (the param) block through the Portal
    * @param block 
    * @return null if portal does not connect to block
    */
    public List <OPath> getPathsWithinBlock(OBlock block) { 
        if (block == null) { return null; }
        if (block.equals(_fromBlock)) {
            return _fromPaths;
        } else if (block.equals(_toBlock)) {
            return _toPaths;
        }
        return null; 
    }

    /**
    * Return the block on the other side of the portal
    * from this (the param) block
    */
    public OBlock getOpposingBlock(OBlock block) {
        if (block.equals(_fromBlock)) {
            return _toBlock;
        } else if (block.equals(_toBlock)) {
            return _fromBlock;
        }
        return null; 
    }
    
    /**
    * Get the paths from the portal in the next connected Block
    * i.e. paths in the block on the other side of the portal
    * from this (the param) block
    * @param block 
    * @return null if portal does not connect to block
    */
    public List <OPath> getPathsFromOpposingBlock(OBlock block) { 
        if (block.equals(_fromBlock)) {
            return _toPaths;
        } else if (block.equals(_toBlock)) {
            return _fromPaths;
        }
        return null; 
    }

    public void setSignal(OBlock block, int appearance) {
        if (block.equals(_fromBlock)) {
            if (_fromSignal!=null) {
                _fromSignal.setAppearance(appearance);
                if (log.isDebugEnabled()) log.debug(block.getDisplayName()+" set _fromSignal= "+appearance);
            }
        } else if (block.equals(_toBlock)) {
            if (_toSignal!=null) {
                _toSignal.setAppearance(appearance);
                if (log.isDebugEnabled()) log.debug(block.getDisplayName()+" set _toSignal= "+appearance);
            }
        }
    }
    
    public int getSignalAppearance(OBlock block) {
        int appearance = SignalHead.DARK;
        if (block.equals(_fromBlock)) {
            if (_fromSignal!=null) {
                appearance =_fromSignal.getAppearance();
                if (log.isDebugEnabled()) log.debug(block.getDisplayName()+" get _fromSignal= "+appearance);
            }
        } else if (block.equals(_toBlock)) {
            if (_toSignal!=null) {
                appearance = _toSignal.getAppearance();
                if (log.isDebugEnabled()) log.debug(block.getDisplayName()+" get _toSignal= "+appearance);
            }
        }
        return appearance;
    }
    
    public void setOpposingSignal(OBlock block, int appearance) {
        if (block.equals(_fromBlock)) {
            if (_toSignal!=null) {
                _toSignal.setAppearance(appearance);
                if (log.isDebugEnabled()) log.debug(block.getDisplayName()+" set Opposing _toSignal= "+appearance);
            }
        } else if (block.equals(_toBlock)) {
            if (_fromSignal!=null) {
                _fromSignal.setAppearance(appearance);
                if (log.isDebugEnabled()) log.debug(block.getDisplayName()+" set Opposing _fromSignal= "+appearance);
            }
        }
    }
    
    public int getOpposingSignalAppearance(OBlock block) {
        int appearance = SignalHead.DARK;
        if (block.equals(_fromBlock)) {
            if (_toSignal!=null) {
                appearance = _toSignal.getAppearance();
                if (log.isDebugEnabled()) log.debug(block.getDisplayName()+" get Opposing _toSignal= "+appearance);
            }
        } else if (block.equals(_toBlock)) {
            if (_fromSignal!=null) {
                appearance = _fromSignal.getAppearance();
                if (log.isDebugEnabled()) log.debug(block.getDisplayName()+" get Opposing _fromSignal= "+appearance);
            }
        }
        return appearance;
    }
    
    private boolean verify(List <OPath> paths, OBlock block) {
        String name = block.getSystemName();
        for (int i=0; i<paths.size(); i++) {
            String pathName = paths.get(i).getBlock().getSystemName();
            if (!pathName.equals(name)) {
                return false;
            }
        }
        return true;
    }

    public boolean isValid() {
        return (_fromBlock!=null && _toBlock!=null);
    }

    public void dispose() {
        if (_fromBlock!=null) _fromBlock.removePortal(this);
        if (_toBlock!=null) _toBlock.removePortal(this);
    }
    
    public String toString() {
        return ("Portal \""+_portalName+"\" from block \""+getFromBlockName()+"\" to block \""+getToBlockName()+"\""); 
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Portal.class.getName());
}

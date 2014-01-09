
package jmri.jmrit.logix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
//import java.util.Enumeration;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Iterator;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class RouteFinder implements Runnable {
	WarrantRoute _caller;
    BlockOrder _originBlockOrder;
    BlockOrder _destBlockOrder;
    BlockOrder _viaBlockOrder;
    BlockOrder _avoidBlockOrder;
    ArrayList <DefaultMutableTreeNode> _destNodes;
    DefaultTreeModel _tree;

    OBlock _destBlock;
    String _dPathName;
    String _dEntryName;
    OBlock _viaBlock;
    String _vPathName;
    OBlock _avoidBlock;
    String _aPathName;

    int _maxBlocks;
    boolean _quit = false;
//    java.beans.PropertyChangeSupport _pcs = new java.beans.PropertyChangeSupport(this);

    protected RouteFinder(WarrantRoute f, BlockOrder origin, BlockOrder dest, 
                          BlockOrder via, BlockOrder avoid, int maxB) {
        _caller = f;
//        _pcs.addPropertyChangeListener(_caller);
        _originBlockOrder = origin;
        _destBlockOrder = dest;
        _viaBlockOrder = via;
        _avoidBlockOrder = avoid;
        _maxBlocks = maxB;
    }

    protected void quit() {
        _quit = true;
    }

    static class RouteNode extends DefaultMutableTreeNode {

        boolean _needsViaAncestor = false;

        RouteNode(Object userObject) {
            super(userObject);
        }

        RouteNode(Object userObject, boolean needsViaAncestor) {
            super(userObject);
            _needsViaAncestor = needsViaAncestor;
        }

        void hasViaAncestor(boolean hasViaAncestor) {
            _needsViaAncestor = !hasViaAncestor;
        }

        boolean needsViaAncestor() {
            return _needsViaAncestor;
        }

    }

    public void run() {
        _destBlock = _destBlockOrder.getBlock();
        _dPathName = _destBlockOrder.getPathName();
        _dEntryName = _destBlockOrder.getEntryName();
        _viaBlock = null;
        _vPathName = null;
        if (_viaBlockOrder!=null) {
            _vPathName = _viaBlockOrder.getPathName();
            _viaBlock = _viaBlockOrder.getBlock();
        }
        _avoidBlock = null;
        _aPathName = null;
        if (_avoidBlockOrder!=null) {
            _aPathName = _avoidBlockOrder.getPathName();
            _avoidBlock = _avoidBlockOrder.getBlock();
        }

        _destNodes = new ArrayList <DefaultMutableTreeNode>();
        _quit = false;
        int level = 0;
        RouteNode root = new RouteNode(_originBlockOrder, (_viaBlockOrder!=null));
        _tree = new DefaultTreeModel(root);
        ArrayList <RouteNode> nodes = new ArrayList <RouteNode>();
        nodes.add(root);
        while (level < _maxBlocks && !_quit) {
            nodes = makeLevel(nodes, level);
            level++;
//            _pcs.firePropertyChange("RouteSearch", Integer.valueOf(level), Integer.valueOf(_destNodes.size()));
        }
        if (_destNodes.size()==0) {
            _caller.debugRoute(_tree, _originBlockOrder, _destBlockOrder, _maxBlocks);
        } else {
            _caller.pickRoute(_destNodes, _tree);
        }
//        _pcs.removePropertyChangeListener(_caller);
    }

    /**
    * Examines list of nodes at a given level for the destination node and makes a list
    * of nodes of the next level.
    */
    ArrayList <RouteNode> makeLevel(ArrayList <RouteNode> nodes, int level) {

        ArrayList <RouteNode> children = new ArrayList <RouteNode>();
        for (int i=0; i<nodes.size(); i++) {
            RouteNode node = nodes.get(i);
            BlockOrder pOrder = (BlockOrder)node.getUserObject();
            OBlock pBlock = pOrder.getBlock();
            String pName = pOrder.getExitName();    // is entryName of next block
            Portal exitPortal = pBlock.getPortalByName(pName);
            if (exitPortal != null) {
                OBlock nextBlock = exitPortal.getOpposingBlock(pBlock);
                List <OPath> paths = exitPortal.getPathsFromOpposingBlock(pBlock);
                if (log.isDebugEnabled()) log.debug("makeLevel "+level+" block= "+pBlock.getDisplayName()
                                                +", path= "+pOrder.getPathName()+" meets "+paths.size()+" portal paths");
                if (paths.size()==0) {
                    log.error("Portal \""+pName+"\" "+(exitPortal.getOpposingBlock(pBlock)==null ? 
                    		"is malformed! Only one block!" : "does not have any paths into the next block!"));
                }
                // walk all paths
                for (int k=0; k<paths.size(); k++) {
                    OPath path = paths.get(k);
                    if (_avoidBlock!=null && _avoidBlock.equals(nextBlock) ) {
                        if (_aPathName.equals(path.getName())) {
                            continue;
                        }
                    }
                    String exitName = path.getOppositePortalName(pName);
                    BlockOrder nOrder = new BlockOrder((OBlock)path.getBlock(), path.getName(), pName, exitName);
                    RouteNode child = new RouteNode(nOrder, node.needsViaAncestor());
                    _tree.insertNodeInto(child, node, node.getChildCount());
                    if (_viaBlock!=null && _viaBlock.equals(nextBlock) ) {
                        if (_vPathName.equals(path.getName())) {
                            child.hasViaAncestor(true);
                        }
                    }
                    if (!node.needsViaAncestor()) {
                        if (_destBlock == nOrder.getBlock() && _dPathName.equals(path.getName())
                            && _dEntryName.equals(pName) ) {
                            _destNodes.add(child);
                        } else {
                            children.add(child);
                        }
                    } else {
                        children.add(child);
                    }
                }
//                _pcs.firePropertyChange("RouteSearch", Integer.valueOf(level), Integer.valueOf(_destNodes.size()));
            } else {
                if (log.isDebugEnabled()) log.debug("Dead branch: block= "+pBlock.getDisplayName()+
                                                    " has no exit portal");
            }
            if (_quit) { break; }
        }
        return children;
    }

    static Logger log = LoggerFactory.getLogger(RouteFinder.class.getName());
}

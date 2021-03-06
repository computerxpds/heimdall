/**
 * 
 */
package org.morganm.heimdall.blockhistory;

import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.morganm.heimdall.Heimdall;
import org.morganm.heimdall.util.Debug;

import de.diddiz.LogBlock.LogBlock;
import de.diddiz.LogBlock.QueryParams;
import de.diddiz.LogBlock.QueryParams.BlockChangeType;

/**
 * @author morganm
 *
 */
public class BlockHistoryLogBlock implements BlockHistoryManager {
	private final static HashSet<String> ignoredOwners = new HashSet<String>(15);
	private final Heimdall plugin;
	private final Debug debug;
	private final BlockHistoryCache bhCache;
	private LogBlock logBlock;
	
	static {
		ignoredOwners.add("WaterFlow");
		ignoredOwners.add("LavaFlow");
		ignoredOwners.add("TNT");
		ignoredOwners.add("Creeper");
		ignoredOwners.add("Fire");
		ignoredOwners.add("Ghast");
		ignoredOwners.add("Environment");
		ignoredOwners.add("Enderman");
		ignoredOwners.add("LeavesDecay");
	}
	
	public BlockHistoryLogBlock(final Heimdall plugin, final BlockHistoryCache bhCache) {
		this.plugin = plugin;
		this.debug = Debug.getInstance();
		this.bhCache = bhCache;
		
		Plugin p = this.plugin.getServer().getPluginManager().getPlugin("LogBlock");
		if( p instanceof LogBlock )
			this.logBlock = (LogBlock) p;
		else
			this.logBlock = null;
	}
	
	@Override
	public void pluginLoaded(final Plugin p) {
		if( p instanceof LogBlock )
			this.logBlock = (LogBlock) p;
	}
	
	@Override
	public void pluginUnloaded(final Plugin p) {
		if( p instanceof LogBlock )
			this.logBlock = null;
	}

	@Override
	public BlockHistory getBlockHistory(final Location l) {
		if( l == null )
			return null;
		
		// check the cache to see if we already have the history for this location
		BlockHistory bh = bhCache.getCacheObject(l);
		if( bh != null )
			return bh;
		
		// don't run a lookup if this world is disabled
		if( plugin.isDisabledWorld(l.getWorld().getName()) )
			return null;
		
		// if it's a broken block and we have logBlock, lookup the owner
		if( logBlock != null ) {
			debug.debug("running logBlock query");
			QueryParams params = new QueryParams(logBlock);
			params.bct = BlockChangeType.CREATED;
//			params.since = 43200;		// 30 days
			params.since = 107373;		// roughly 3 months
			params.loc = l;
			params.world = l.getWorld();
			params.silent = true;
//			params.needDate = true;
			params.needType = true;
			params.needPlayer = true;
			params.radius = 0;
			// order descending and limit 1, we just want the most recent blockChange
			params.limit = 1;
			params.order = QueryParams.Order.DESC;
			try {
				if( debug.isDevDebug() ) {
					debug.devDebug("logBlock query = ",params.getQuery());
				}
				for (de.diddiz.LogBlock.BlockChange lbChange : logBlock.getBlockChanges(params)) {
					// skip ignored owners
					if( ignoredOwners.contains(lbChange.playerName) )
						continue;
					
					bh = new BlockHistory(lbChange.playerName, lbChange.type, l);
					debug.debug("got logBlock result, lbOwner=",lbChange.playerName);
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		// store it in the cache
		if( bh != null )
			bhCache.storeCacheObject(bh);
		
		return bh;
	}

}

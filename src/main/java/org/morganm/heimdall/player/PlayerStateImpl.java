/**
 * 
 */
package org.morganm.heimdall.player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.morganm.heimdall.Heimdall;
import org.morganm.heimdall.log.GriefLog;
import org.morganm.heimdall.util.Debug;
import org.morganm.heimdall.util.PermissionSystem;

/**
 * @author morganm
 *
 */
public class PlayerStateImpl implements PlayerState {
	private final transient Heimdall plugin;  
	private final transient PermissionSystem permSystem;  
	private final String name;
	private float griefPoints=0;
	/* We track pointsByOwner so that if a player friends another player (after they've accumulated
	 * some grief as a result of breaking others players blocks, for example), we can subtract the
	 * points from the player that are owned by the new friend.
	 * 
	 */
	private Map<String, Float> pointsByOwner;
	
	private final transient File dataFile;
	private final transient PlayerStateManager playerStateManager;
	private transient GriefLog griefLog;
	private transient YamlConfiguration dataStore;
	
	public PlayerStateImpl(final Heimdall plugin, final String name, final PlayerStateManager playerStateManager) {
		this.plugin = plugin;
		this.permSystem = this.plugin.getPermissionSystem();
		this.name = name;
		this.playerStateManager = playerStateManager;
		this.dataFile = new File("plugins/Heimdall/playerData/"+name+".yml");
	}
	
	@Override
	public String getName() { return name; }

	@Override
	public float incrementGriefPoints(final float f, final String owner) {
		griefPoints += f;
		
		Debug.getInstance().debug("incrementGriefPoints(player = "+name+") points="+f+", owner="+owner);
		
		// track owner points, if owner was given
		if( owner != null ) {
			if( pointsByOwner == null )
				pointsByOwner = new HashMap<String, Float>();
			
			Float ownerPoints = pointsByOwner.get(owner);
			if( ownerPoints == null )
				ownerPoints = Float.valueOf(f);
			else
				ownerPoints += f;
			pointsByOwner.put(owner, ownerPoints);
		}
		
		return griefPoints;
	}

	@Override
	public float getGriefPoints() {
		return griefPoints;
	}

	@Override
	public boolean isExemptFromChecks() {
		List<String> exemptPerms = plugin.getConfig().getStringList("exemptPermissions");
		if( exemptPerms != null ) {
			for(String perm : exemptPerms) {
				if( permSystem.has(name, perm) )
					return true;
			}
		}
		
		return false;
	}

	@Override
	public boolean isFriend(PlayerState p) {
		// TODO: something intelligent later (with playerStateManager)
		return false;
	}

	@Override
	public float getPointsByOwner(PlayerState p) {
		return pointsByOwner.get(p.getName());
	}

	@Override
	public GriefLog getGriefLog() {
		if( griefLog == null ) {
			File logFile = new File("plugins/Heimdall/griefLog/"+name.toLowerCase()+".dat");
			griefLog = new GriefLog(plugin, logFile);
		}
		
		return griefLog;
	}
	
	public void save() throws IOException {
		// make parent directories if necessary
		if( !dataFile.exists() ) {
			File path = new File(dataFile.getParent());
			if( !path.exists() )
				path.mkdirs();
		}
		
		// don't do anything if nothing to record
		if( griefPoints == 0 && pointsByOwner == null )
			return;
		
		if( dataStore == null )
			dataStore = new YamlConfiguration();
		
		dataStore.set("griefPoints", griefPoints);
		
		if( pointsByOwner != null ) {
			for(Map.Entry<String, Float> entry : pointsByOwner.entrySet()) {
				dataStore.set("pointsByOwner."+entry.getKey(), entry.getValue());
			}
		}

		dataStore.save(dataFile);
	}
	
	public void load() throws IOException, InvalidConfigurationException {
		// if there's no data file, do nothing
		if( !dataFile.exists() )
			return;
		
		if( dataStore == null )
			dataStore = YamlConfiguration.loadConfiguration(dataFile);
		else
			dataStore.load(dataFile);
		
		griefPoints = (float) dataStore.getDouble("griefPoints");
		
		if( pointsByOwner == null )
			pointsByOwner = new HashMap<String, Float>();
		ConfigurationSection section = dataStore.getConfigurationSection("pointsByOwner");
		if( section != null ) {
			Set<String> owners = section.getKeys(false);
			if( owners != null ) {
				for(String owner : owners) {
					pointsByOwner.put(owner, Float.valueOf((float) section.getDouble(owner)));
				}
			}
		}
	}
}
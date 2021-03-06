/**
 * 
 */
package org.morganm.heimdall.engine;

import java.util.HashMap;

import org.bukkit.Location;
import org.morganm.heimdall.Heimdall;
import org.morganm.heimdall.event.BlockChangeEvent;
import org.morganm.heimdall.event.Event;
import org.morganm.heimdall.event.InventoryChangeEvent;

/** Engine that keeps track of the most recent grief event per-player.
 * 
 * @author morganm
 *
 */
public class LastGriefTrackingEngine extends AbstractEngine {
	@SuppressWarnings("unused")
	private final Heimdall plugin;
	private final HashMap<String, Location> lastGriefLocation = new HashMap<String, Location>(10);
	private String lastGriefPlayerName = null;

	public LastGriefTrackingEngine(final Heimdall plugin) {
		this.plugin = plugin;
	}

	@Override
	public Event.Type[] getRegisteredEventTypes() {
		return new Event.Type[] { Event.Type.BLOCK_CHANGE, Event.Type.INVENTORY_CHANGE };
	}

	/* Return the name of the most recent person to get a grief alert.
	 * 
	 */
	public String getLastGriefPlayerName() {
		return lastGriefPlayerName;
	}
	
	public Location getLastGriefLocation(final String playerName) {
		return lastGriefLocation.get(playerName);
	}
	
	private void processEvent(final Event event) {
		String playerName = event.getPlayerName();
		Location location = event.getLocation();
		if( playerName != null && location != null ) {
			lastGriefLocation.put(playerName, location);
			lastGriefPlayerName = playerName;
		}
	}
	
	@Override
	public void processBlockChange(final BlockChangeEvent event) {
		processEvent(event);
	}

	@Override
	public void processInventoryChange(final InventoryChangeEvent event) {
		processEvent(event);
	}

}

/**
 * 
 */
package org.morganm.heimdall.engine;

import java.util.HashMap;

import org.morganm.heimdall.Heimdall;
import org.morganm.heimdall.HeimdallPersonality;
import org.morganm.heimdall.event.BlockChangeEvent;
import org.morganm.heimdall.event.Event;
import org.morganm.heimdall.event.InventoryChangeEvent;
import org.morganm.heimdall.player.PlayerState;
import org.morganm.heimdall.player.PlayerStateManager;

/**
 * @author morganm
 *
 */
public class HeimdallPersonalityEngine extends AbstractEngine {
	@SuppressWarnings("unused")
	private final Heimdall plugin;
	private final PlayerStateManager playerStateManager;
	private final HeimdallPersonality personality;
	private final HashMap<String, Integer> announceLevel = new HashMap<String, Integer>(10);
	
	public HeimdallPersonalityEngine(final Heimdall plugin) {
		this.plugin = plugin;
		this.playerStateManager = plugin.getPlayerStateManager();
		this.personality = new HeimdallPersonality(plugin);
	}
	
	@Override
	public Event.Type[] getRegisteredEventTypes() {
		return new Event.Type[] { Event.Type.BLOCK_CHANGE, Event.Type.INVENTORY_CHANGE };
	}
	
	private void processEvent(final Event event) {
		if( !personality.isEnabled() )
			return;
		
		final String playerName = event.getPlayerName();
		final PlayerState ps = playerStateManager.getPlayerState(playerName);
		
		int level = 0;
		Integer intLevel = announceLevel.get(playerName);
		if( intLevel != null )
			level = intLevel;
		
		// keep track of the grief level we announce at, so we don't do the same
		// announcements multiple times
		if( ps.getGriefPoints() > 25 && level < 25 ) {
			announceLevel.put(playerName, 25);
			personality.announcePossibleGriefer(playerName);
		}
	}
	
	@Override
	public void processBlockChange(final BlockChangeEvent event) {
		processEvent(event);
	}
	@Override
	public void processInventoryChange(InventoryChangeEvent event) {
		processEvent(event);
	}
}

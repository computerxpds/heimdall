/**
 * 
 */
package org.morganm.heimdall.event;

import org.bukkit.Location;
import org.morganm.heimdall.event.handlers.EventHandler;

/** General event interface that all Heimdall Event types will implement.
 * 
 * @author morganm
 *
 */
public interface Event {
	public enum Type {
		BLOCK_CHANGE,
		INVENTORY_CHANGE,
		CHAT_MESSAGE,
		PLAYER_EVENT,
		HEIMDALL_FRIEND_EVENT,
		HEIMDALL_FRIEND_INVITE_SENT,
	}
	
	/* Bukkit has deprecated their event types. While the new event system is
	 * much better, event types are still useful for a plugin like Heimdall
	 * where event data is stored and processed asynchronously. Heimdall's
	 * event types were designed to be less granular, so this enum is used
	 * to track the original Bukkit event type for places where that might
	 * be useful (such as logging).
	 */
	public enum BukkitType {
		BLOCK_PLACE,
		BLOCK_BREAK,
		SIGN_CHANGE
	}
	
	/** Clear the event object of any data.
	 * 
	 */
	public void clear();
	
	public boolean isCleared();
	
	public Type getType();
	
	/** Not specifically related to getType(): this method should return a human-readable
	 * String that explains the event type. For example this might be "BLOCK_PLACE" or
	 * "BLOCK_DESTROY" or "ITEM_CRAFTED", etc.
	 * 
	 * @return
	 */
	public String getEventTypeString();
	
	/** Return the player whose actions caused this event to be generated.
	 * 
	 * @return
	 */
	public String getPlayerName();

	/** Return the time that the event took place.
	 * 
	 * @return
	 */
	public long getTime();
	
	/** For events that have a location.
	 * 
	 * @return the location of the event, or null if no location
	 */
	public Location getLocation();
	
	/** Visitor pattern.
	 * 
	 */
	public void accept(EventHandler visitor);
}

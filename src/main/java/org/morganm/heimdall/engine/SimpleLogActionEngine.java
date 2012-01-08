/**
 * 
 */
package org.morganm.heimdall.engine;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

import org.morganm.heimdall.Heimdall;
import org.morganm.heimdall.event.BlockChangeEvent;
import org.morganm.heimdall.event.Event;
import org.morganm.heimdall.event.InventoryChangeEvent;
import org.morganm.heimdall.player.PlayerState;
import org.morganm.heimdall.player.PlayerStateManager;

/** Simple engine to log griefer actions.
 * 
 * @author morganm
 *
 */
public class SimpleLogActionEngine implements Engine {
	private static final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
//	private static long TIME_BETWEEN_FLUSH = 5000;	// 5 seconds
	
	private final Heimdall plugin;
	private EngineLog log;
	private boolean flushScheduled = false;
	private final LogFlusher logFlusher = new LogFlusher();
	private final PlayerStateManager playerStateManager;
	
	public SimpleLogActionEngine(final Heimdall plugin, final PlayerStateManager playerStateManager) {
		this.plugin = plugin;
		this.playerStateManager = playerStateManager;
		
		// TODO: drive file location from config file
		this.log = new EngineLog(plugin, new File("plugins/Heimdall/logs/simpleLogActionEngine.log"));
		try {
			log.init();
		}
		catch(IOException e) {
			log = null;
			e.printStackTrace();
		}
	}

	@Override
	public void processBlockChange(BlockChangeEvent event) {
		logEvent(event, event.griefValue);
	}

	@Override
	public void processInventoryChange(InventoryChangeEvent event) {
		logEvent(event, event.griefValue);
	}

	@Override
	public void processChatMessage(String message) {
		// TODO Auto-generated method stub
		
	}
	
	private void logEvent(final Event event, final float griefValue) {
//		Debug.getInstance().debug("SimpleLogActionEngine:processGriefValue(): playerName=",event.getPlayerName(),", griefvalue=",griefValue);
		if( griefValue == 0 )
			return;

		PlayerState ps = playerStateManager.getPlayerState(event.getPlayerName());
		if( ps.isExemptFromChecks() )
			return;

		if( log != null ) {
			try {
				StringBuilder sb = new StringBuilder(160);
				sb.append("[");
				sb.append(dateFormat.format(new Date()));
				sb.append("] ");
				sb.append(event.getPlayerName());
				sb.append(" event grief points ");
				sb.append(griefValue);
				sb.append(", total grief now is ");
				sb.append(ps.getGriefPoints());
				log.log(sb.toString());
				
				if( !flushScheduled ) {
					flushScheduled = true;
					plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, logFlusher, 100);
				}
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private class LogFlusher implements Runnable {
		public void run() {
			log.flush();
			flushScheduled = false;
		}
	}

}

package net.slipcor.pvparena.events;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.managers.Spawns;

import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * 
 * PVP Arena End Event
 * 
 * -
 * 
 * is thrown when an arena match ends
 * 
 * @version v0.7.8
 * 
 * @author slipcor
 * 
 */

public class PAEndEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private Arena arena;

	/**
	 * create an end event instance
	 * 
	 * @param a
	 *            the ending arena
	 */
	public PAEndEvent(Arena a) {
		arena = a;
	}

	/**
	 * hand over the arena instance
	 * 
	 * @return the ending arena
	 */
	public Arena getArena() {
		return arena;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	/**
	 * return the battlefield center
	 * 
	 * @return the battlefield center location
	 */
	public Location getRegionCenter() {
		return Spawns.getRegionCenter(arena);
	}
}

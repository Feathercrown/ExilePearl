package com.devotedmc.ExilePearl.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.PearlPlayer;

public class ExilePearlEvent extends Event {
	public enum Type { NEW, HELD, DROPPED, FREED }
	
	private final ExilePearl pearl;
	private final Type type;
	private final PearlPlayer imprisoner;
	
	private boolean cancelled;
	
	// Handler list for spigot events
	private static final HandlerList handlers = new HandlerList();
	
	public ExilePearlEvent(final ExilePearl pearl, final Type type, final PearlPlayer imprisoner) {
		this.pearl = pearl;
		this.type = type;
		this.imprisoner = imprisoner;
	}
	
	public ExilePearlEvent(ExilePearl pearl, Type type) {
		this(pearl, type, null);
	}
	
	/**
	 * Gets the exile pearl
	 * @return The exile pearl
	 */
	public ExilePearl getExilePearl() {
		return pearl;
	}
	
	/**
	 * Gets the event type
	 * @return The event type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Gets the imprisoner if one exists, otherwise null
	 * @return The imprisoner
	 */
	public PearlPlayer getImprisoner() {
		return imprisoner;
	}
	
	/**
	 * Gets whether the event is cancelled
	 * @return true if the event is cancelled
	 */
	public boolean isCancelled() {
		return cancelled;
	}
	
	/**
	 * Sets whether the event is cancelled
	 * @param cancelled whether the event is cancelled
	 */
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
	
	@Override
	public HandlerList getHandlers() {
	    return handlers;
	}
	
	public static HandlerList getHandlerList() {
	    return handlers;
	}
}
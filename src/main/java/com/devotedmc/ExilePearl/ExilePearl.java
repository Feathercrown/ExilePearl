package com.devotedmc.ExilePearl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import com.devotedmc.ExilePearl.holder.BlockHolder;
import com.devotedmc.ExilePearl.holder.LocationHolder;
import com.devotedmc.ExilePearl.holder.PearlHolder;
import com.devotedmc.ExilePearl.holder.HolderVerifyResult;
import com.devotedmc.ExilePearl.holder.PlayerHolder;
import com.devotedmc.ExilePearl.util.TextUtil;

/**
 * Instance of a player who is imprisoned in an exile pearl
 * @author Gordon
 *
 */
public class ExilePearl {
	public static final int HOLDER_COUNT = 5;
	public static String ITEM_NAME = "Prison Pearl";

	private final ExilePearlPlugin plugin;
	private final UUID playerId;
	private PearlPlayer player;
	private PearlHolder holder;
	private Date pearledOn;
	private LinkedBlockingQueue<PearlHolder> holders;
	private long lastMoved;
	private boolean freedOffline;

	/**
	 * Creates a new prison pearl instance
	 * @param playerId The pearled player id
	 * @param holder The holder instance
	 */
	public ExilePearl(ExilePearlPlugin plugin, UUID playerId, PearlHolder holder) {
		this.plugin = plugin;
		this.playerId = playerId;
		this.pearledOn = new Date();
		this.holders = new LinkedBlockingQueue<PearlHolder>();
		this.lastMoved = pearledOn.getTime();
		this.setHolder(holder);
	}


	/**
	 * Creates a new prison pearl instance
	 * @param playerId The pearled player id
	 * @param holder The holder instance
	 */
	public ExilePearl(ExilePearlPlugin plugin, PearlPlayer player, PearlHolder holder) {
		this(plugin, player.getUniqueId(), holder);
		this.player = player;
	}

	/**
	 * Creates a new prison pearl instance
	 * @param playerId The pearled player id
	 * @param imprisoner The imprisoner
	 */
	public ExilePearl(ExilePearlPlugin plugin, PearlPlayer player, Player imprisoner) {
		this(plugin, player.getUniqueId(), new PlayerHolder(imprisoner));
		this.player = player;
	}


	/**
	 * Gets the imprisoned player ID
	 * @return The player ID
	 */
	public UUID getPlayerID() {
		return playerId;
	}


	/**
	 * Gets the imprisoned player
	 * @return The player instance
	 */
	public PearlPlayer getPlayer() {
		if (player == null) {
			player = plugin.getPearlPlayer(playerId);
		}
		return player;
	}


	/**
	 * Gets when the player was pearled
	 * @return The time the player was pearled
	 */
	public Date getPearledOn() {
		return this.pearledOn;
	}


	/**
	 * Sets when the player was pearled
	 * @param pearledOn The time the player was pearled
	 */
	public void setPearledOn(Date pearledOn) {
		this.pearledOn = pearledOn;
	}


	/**
	 * Gets the imprisoned name
	 * @return The player name
	 */
	public String getName() {
		return this.getPlayer().getName();
	}


	/**
	 * Gets the pearl holder
	 * @return The pearl holder
	 */
	public PearlHolder getHolder() {
		return this.holder;
	}


	/**
	 * Sets the pearl holder
	 * @param holder The new pearl holder
	 */
	public void setHolder(PearlHolder holder) {
		if (holder == null) {
			throw new RuntimeException("Prisonpearl holder cannot be null.");
		}

		this.holder = holder;
		this.holders.add(holder);

		if (holders.size() > HOLDER_COUNT) {
			holders.poll();
		}
	}


	/**
	 * Sets the pearl holder to a player
	 * @param holder The new pearl holder
	 */
	public void setHolder(PearlPlayer p) {
		this.setHolder(new PlayerHolder(p.getBukkitPlayer()));
	}


	/**
	 * Sets the pearl holder to a block
	 * @param holder The new pearl block
	 */
	public void setHolder(Block b) {
		this.setHolder(new BlockHolder(b));
	}


	/**
	 * Sets the pearl holder to a location
	 * @param holder The new pearl location
	 */
	public void setHolder(Location l) {
		this.setHolder(new LocationHolder(l));
	}


	/**
	 * Gets the name of the current location
	 * @return The string of the current location
	 */
	public String getLocationName() {
		final Location loc = holder.getLocation();
		final Vector vec = loc.toVector();
		final String str = loc.getWorld().getName() + " " + vec.getBlockX() + " " + vec.getBlockY() + " " + vec.getBlockZ();
		return "held by " + holder.getName() + " at " + str;
	}


	/**
	 * Marks when the pearl was moved last
	 */
	public void markMove() {
		this.lastMoved = System.currentTimeMillis();
	}



	/**
	 * Gets whether the player was freed offline
	 * @return true if the player was freed offline
	 */
	public boolean getFreedOffline() {
		return this.freedOffline;
	}

	/**
	 * Gets whether the player was freed offline
	 * @return true if the player was freed offline
	 */
	public void setFreedOffline(boolean freedOffline) {
		this.freedOffline = freedOffline;
	}



	/**
	 * Creates an item stack for the pearl
	 * @return The new item stack
	 */
	public ItemStack createItemStack() {
		List<String> lore = generateLore();
		ItemStack is = new ItemStack(Material.ENDER_PEARL, 1);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(this.getName());
		im.setLore(lore);
		is.setItemMeta(im);
		return is;
	}

	/**
	 * Generates the lore for the pearl
	 * @return The pearl lore
	 */
	public List<String> generateLore() {
		List<String> lore = new ArrayList<String>();
		lore.add(parse("<l>%s", ITEM_NAME));
		lore.add(parse("<a>Player: <n>%s", this.getName()));
		lore.add(parse("<a>UUID: <n>%s", playerId.toString()));
		lore.add(parse("<a>Imprisoned on: <n>%s", new SimpleDateFormat("yyyy-MM-dd").format(pearledOn)));
		lore.add(parse(""));
		lore.add(parse("<l>Commands:"));
		//lore.add(parse(CmdPearl.getInstance().cmdFree.getUsageTemplate(true)));
		//lore.add(parse(CmdPearl.getInstance().cmdSummon.getUsageTemplate(true)));
		//lore.add(parse(CmdPearl.getInstance().cmdReturn.getUsageTemplate(true)));
		//lore.add(parse(CmdPearl.getInstance().cmdKill.getUsageTemplate(true)));
		return lore;
	}

	// For parsing the UUID out of the pearl lore
	private static Pattern idPattern = Pattern.compile(parse("<a>UUID: <n>(.+)"));


	/**
	 * Gets the UUID from a prison pearl
	 * @param is The item stack
	 * @return The player UUID, or null if it can't parse
	 */
	public static UUID getIDFromItemStack(ItemStack is) {
		if (is == null) {
			return null;
		}

		if (!is.getType().equals(Material.ENDER_PEARL)) {
			return null;
		}

		ItemMeta im = is.getItemMeta();
		if (im == null) {
			return null;
		}

		List<String> lore = im.getLore();
		if (lore == null) {
			return null;
		}

		String idLore  = lore.get(2);
		Matcher match = idPattern.matcher(idLore);
		if (match.find()) {
			UUID id = UUID.fromString(match.group(1));
			return id;
		}

		return null;
	}


	/**
	 * Validates that an item stack is the prison pearl
	 * @param is The item stack
	 * @return true if it checks out
	 */
	public boolean validateItemStack(ItemStack is) {

		UUID id = getIDFromItemStack(is);
		if (id != null && id.equals(this.playerId)) {

			// re-create the item stack to update the values
			ItemMeta im = is.getItemMeta();
			im.setLore(this.generateLore());
			is.setItemMeta(im);
			return true;
		}

		return false;
	}


	/**
	 * Verifies the pearl location
	 * @return
	 */
	public boolean verifyLocation() {
		StringBuilder sb = new StringBuilder();

		StringBuilder verifier_log = new StringBuilder();
		StringBuilder failure_reason_log = new StringBuilder();

		for (final PearlHolder holder : this.holders) {
			HolderVerifyResult reason = this.verifyHolder(holder, verifier_log);
			if (reason.isValid()) {
				sb.append(String.format("PP (%s, %s) passed verification for reason '%s': %s",
						playerId.toString(), this.getName(), reason.toString(), verifier_log.toString()));
				plugin.log(sb.toString());

				return true;
			} else {
				failure_reason_log.append(reason.toString()).append(", ");
			}
			verifier_log.append(", ");
		}
		sb.append(String.format("PP (%s, %s) failed verification for reason %s: %s",
				playerId.toString(), this.getName(), failure_reason_log.toString(), verifier_log.toString()));

		plugin.log(sb.toString());
		return false;
	}


	/**
	 * Verifies the holder of a pearl
	 * @param holder The holder to check
	 * @param feedback The feedback string
	 * @return true if the pearl was found in a valid location
	 */
	private HolderVerifyResult verifyHolder(PearlHolder holder, StringBuilder feedback) {

		if (System.currentTimeMillis() - this.lastMoved < 2000) {
			// The pearl was recently moved. Due to a race condition, this exists to
			//  prevent players from spamming /ppl to get free when a pearl is moved.
			return HolderVerifyResult.TIME;
		}

		return holder.validate(this, feedback);
	}

	public Location getLocation() {
		return this.holders.peek().getLocation();
	}


	protected static String parse(String str) {
		return TextUtil.instance().parse(str);
	}

	protected static String parse(String str, Object... args) {
		return String.format(parse(str), args);
	}


	/**
	 * Gets the item stack from an inventory if it exists
	 * @param inv The inventory to search
	 * @return The pearl item
	 */
	public ItemStack getItemFromInventory(Inventory inv) {

		for (ItemStack item : inv.all(Material.ENDER_PEARL).values()) {
			if (this.validateItemStack(item)) {
				return item;
			}
		}

		return null;
	}
}
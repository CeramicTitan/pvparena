package net.slipcor.pvparena;

import java.io.File;
import java.io.IOException;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.command.PAA_Command;
import net.slipcor.pvparena.command.PA_Command;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.core.Tracker;
import net.slipcor.pvparena.core.Update;
import net.slipcor.pvparena.listeners.BlockListener;
import net.slipcor.pvparena.listeners.InventoryListener;
import net.slipcor.pvparena.listeners.EntityListener;
import net.slipcor.pvparena.listeners.PlayerListener;
import net.slipcor.pvparena.managers.Arenas;
import net.slipcor.pvparena.metrics.Metrics;
import net.slipcor.pvparena.neworder.ArenaModule;
import net.slipcor.pvparena.neworder.ArenaModuleManager;
import net.slipcor.pvparena.neworder.ArenaRegion;
import net.slipcor.pvparena.neworder.ArenaRegionManager;
import net.slipcor.pvparena.neworder.ArenaType;
import net.slipcor.pvparena.neworder.ArenaTypeManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * main class
 * 
 * -
 * 
 * contains central elements like plugin handlers and listeners
 * 
 * @author slipcor
 * 
 * @version v0.8.12
 * 
 */

public class PVPArena extends JavaPlugin {

	public static final EntityListener entityListener = new EntityListener();
	public static PVPArena instance = null;

	private final BlockListener blockListener = new BlockListener();
	private final PlayerListener playerListener = new PlayerListener();
	private final InventoryListener customListener = new InventoryListener();
	private final static Debug db = new Debug(1);

	private ArenaRegionManager arm = null;
	private ArenaTypeManager atm = null;
	private ArenaModuleManager amm = null;

	/**
	 * Command handling
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd,
			String commandLabel, String[] args) {

		if (args == null || args.length < 1) {
			return false;
		}

		db.i("onCommand: player " + sender.getName() + ": /" + commandLabel
				+ StringParser.parseArray(args));

		PA_Command command = PA_Command.parseCommand(args[0]);
		if (command != null) {
			db.i("command: " + command.getName());
			command.commit(sender, args);
			return true;
		}

		String sName = args[0];

		Arena arena = Arenas.getArenaByName(sName);
		if (arena == null) {
			db.i("arena not found, searching...");
			if (sender instanceof Player) {
				arena = Arenas.getArenaByPlayer((Player) sender);
			}
			if (arena != null) {
				db.i("found arena by player: " + arena.name);
			} else if (Arenas.count() == 1) {
				arena = Arenas.getFirst();
				db.i("found 1 arena: " + arena.name);
			} else if (Arenas.getArenaByName("default") != null) {
				arena = Arenas.getArenaByName("default");
				db.i("found default arena!");
			} else {
				if (args.length > 1 && args[1].equals("create")) {
					Arenas.tellPlayer(sender, "�c/pa create [name] {type}");
					return true;
				}
				Arenas.tellPlayer(sender,
						Language.parse("arenanotexists", sName));
				return true;
			}

		} else {

			String[] newArgs = new String[args.length - 1];
			System.arraycopy(args, 1, newArgs, 0, args.length - 1);
			args = newArgs;
		}

		PAA_Command arenaCommand;

		if (args.length < 1) {
			arenaCommand = PAA_Command.parseCommand(null, arena);
		} else {
			arenaCommand = PAA_Command.parseCommand(args[0], arena);
		}
		if (arenaCommand != null) {
			db.i("arena command: " + arenaCommand.getName());
			if (!arena.cfg.getBoolean("general.enabled")
					&& !PVPArena.hasAdminPerms(sender)
					&& !(PVPArena.hasCreatePerms(sender, arena))) {
				Arenas.tellPlayer(sender, Language.parse("arenadisabled"),
						arena);
				return true;
			}
			db.i("committing arena command: " + db.formatStringArray(args)
					+ " in arena " + arena.name);
			arenaCommand.commit(arena, sender, args);
			return true;
		}
		return false;
	}

	/**
	 * Plugin disabling method - Reset all arenas, cancel tasks
	 */
	@Override
	public void onDisable() {
		Arenas.reset(true);
		Tracker.stop();
		Language.log_info("disabled", getDescription().getFullName());
	}

	/**
	 * Plugin enabling method - Register events and load the configs
	 */
	@Override
	public void onEnable() {
		instance = this;

		getDataFolder().mkdir();
		new File(getDataFolder().getPath() + "/arenas").mkdir();
		new File(getDataFolder().getPath() + "/modules").mkdir();
		new File(getDataFolder().getPath() + "/regions").mkdir();
		new File(getDataFolder().getPath() + "/dumps").mkdir();
		new File(getDataFolder().getPath() + "/files").mkdir();

		atm = new ArenaTypeManager(this);
		amm = new ArenaModuleManager(this);
		arm = new ArenaRegionManager(this);

		Language.init(getConfig().getString("language", "en"));

		getServer().getPluginManager().registerEvents(blockListener, this);
		getServer().getPluginManager().registerEvents(entityListener, this);
		getServer().getPluginManager().registerEvents(playerListener, this);
		getServer().getPluginManager().registerEvents(customListener, this);

		if (getConfig().get("language") != null
				&& getConfig().get("onlyPVPinArena") == null) {
			getConfig().set("debug", "none"); // 0.3.15 correction
			getServer().getLogger().info("[PA-debug] 0.3.15 correction");
		}

		getConfig().options().copyDefaults(true);
		saveConfig();

		File players = new File(getDataFolder(), "players.yml");
		if (!players.exists()) {
			try {
				players.createNewFile();
				db.i("players.yml created successfully");
			} catch (IOException e) {
				getServer()
						.getLogger()
						.severe("Could not create players.yml! More errors will be happening!");
				e.printStackTrace();
			}
		}

		Debug.load(this, Bukkit.getConsoleSender());
		Arenas.load_arenas();
		Update u = new Update(this);
		u.init();
		u.start();

		if (Arenas.count() > 0) {

			Tracker trackMe = new Tracker(this);
			trackMe.start();

			Metrics metrics;
			try {
				metrics = new Metrics(this);
				Metrics.Graph atg = metrics.createGraph("Game modes installed");
				for (ArenaType at : atm.getTypes()) {
					atg.addPlotter(new WrapPlotter(at.getName()));
				}
				Metrics.Graph amg = metrics
						.createGraph("Enhancement modules installed");
				for (ArenaModule am : amm.getModules()) {
					amg.addPlotter(new WrapPlotter(am.getName()));
				}
				Metrics.Graph arg = metrics
						.createGraph("Region shapes installed");
				for (ArenaRegion ar : arm.getRegions()) {
					arg.addPlotter(new WrapPlotter(ar.getName()));
				}
				metrics.start();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		amm.onEnable();

		Language.log_info("enabled", getDescription().getFullName());
	}

	/**
	 * Check if the player has admin permissions
	 * 
	 * @param player
	 *            the player to check
	 * @return true if the player has admin permissions, false otherwise
	 */
	public static boolean hasAdminPerms(CommandSender player) {
		return hasPerms(player, "pvparena.admin");
	}

	/**
	 * Check if the player has creation permissions
	 * 
	 * @param player
	 *            the player to check
	 * @param arena
	 *            the arena to check
	 * @return true if the player has creation permissions, false otherwise
	 */
	public static boolean hasCreatePerms(CommandSender player, Arena arena) {
		return (hasPerms(player, "pvparena.create") && (arena == null || arena.owner
				.equals(player.getName())));
	}

	/**
	 * Check if the player has permission for an arena
	 * 
	 * @param player
	 *            the player to check
	 * @param arena
	 *            the arena to check
	 * @return true if explicit permission not needed or granted, false
	 *         otherwise
	 */
	public static boolean hasPerms(CommandSender player, Arena arena) {
		db.i("perm check.");
		if (arena.cfg.getBoolean("join.explicitPermission")) {
			db.i(" - explicit: "
					+ String.valueOf(hasPerms(player, "pvparena.join."
							+ arena.name.toLowerCase())));
		} else {
			db.i(String.valueOf(hasPerms(player, "pvparena.user")));
		}

		return arena.cfg.getBoolean("join.explicitPermission") ? hasPerms(
				player, "pvparena.join." + arena.name.toLowerCase())
				: hasPerms(player, "pvparena.user");
	}

	/**
	 * Check if a player has a permission
	 * 
	 * @param player
	 *            the player to check
	 * @param perms
	 *            a permission node to check
	 * @return true if the player has the permission, false otherwise
	 */
	public static boolean hasPerms(CommandSender player, String perms) {
		return instance.amm.hasPerms(player, perms);
	}

	/**
	 * Hand over the ArenaRegionManager instance
	 * 
	 * @return the ArenaRegionManager instance
	 */
	public ArenaRegionManager getArm() {
		return arm;
	}

	/**
	 * Hand over the ArenaTypeManager instance
	 * 
	 * @return the ArenaTypeManager instance
	 */
	public ArenaTypeManager getAtm() {
		return atm;
	}

	/**
	 * Hand over the ArenaModuleManager instance
	 * 
	 * @return the ArenaModuleManager instance
	 */
	public ArenaModuleManager getAmm() {
		return amm;
	}

	private class WrapPlotter extends Metrics.Plotter {
		public WrapPlotter(String name) {
			super(name);
		}

		public int getValue() {
			return 1;
		}
	}
}

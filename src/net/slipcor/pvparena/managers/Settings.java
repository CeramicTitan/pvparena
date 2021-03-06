package net.slipcor.pvparena.managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.StringParser;

/**
 * setting manager class
 * 
 * -
 * 
 * provides access to the arena settings
 * 
 * @author slipcor
 * 
 * @version v0.8.11
 * 
 */

public class Settings {
	private final Arena arena;
	private static HashMap<String, String> types = new HashMap<String, String>();

	private Debug db = new Debug(34);

	static {

		types.put("tp.win", "tp");
		types.put("tp.lose", "tp");
		types.put("tp.exit", "tp");
		types.put("tp.death", "tp");

		types.put("setup.wand", "int");

		types.put("game.allowDrops", "boolean");
		types.put("game.dropSpawn", "boolean");
		types.put("game.lives", "int");
		types.put("game.preventDeath", "boolean");
		types.put("game.powerups", "string");
		types.put("game.refillInventory", "boolean");
		types.put("game.teamKill", "boolean");
		types.put("game.weaponDamage", "boolean");
		types.put("game.mustbesafe", "boolean");
		types.put("game.woolFlagHead", "boolean");

		types.put("messages.chat", "boolean");
		types.put("messages.colorNick", "boolean");
		types.put("messages.defaultChat", "boolean");
		types.put("messages.onlyChat", "boolean");
		types.put("messages.language", "lang");

		types.put("general.classperms", "boolean");
		types.put("general.cmdfailjoin", "boolean");
		types.put("general.enabled", "boolean");
		types.put("general.owner", "string");
		types.put("general.world", "string");
		types.put("general.item-rewards", "items");
		types.put("general.random-reward", "boolean");
		types.put("general.restoreChests", "boolean");
		types.put("general.signs", "boolean");
		types.put("general.tpnodamageseconds", "int");

		types.put("region.spawncampdamage", "int");
		types.put("region.timer", "int");

		types.put("arenatype.randomSpawn", "boolean");

		types.put("goal.timed", "int");
		types.put("goal.endtimer", "int");

		types.put("join.explicitPermission", "boolean");
		types.put("join.forceEven", "boolean");
		types.put("join.inbattle", "boolean");
		types.put("join.manual", "boolean");
		types.put("join.onCountdown", "boolean");
		types.put("join.random", "boolean");
		types.put("join.range", "int");
		types.put("join.warmup", "int");

		types.put("periphery.checkRegions", "boolean");

		types.put("protection.spawn", "int");
		types.put("protection.restore", "boolean");
		types.put("protection.enabled", "boolean");
		
		types.put("protection.blockplace", "boolean");
		types.put("protection.blockdamage", "boolean");
		types.put("protection.blocktntdamage", "boolean");
		types.put("protection.decay", "boolean");
		types.put("protection.fade", "boolean");
		types.put("protection.form", "boolean");
		types.put("protection.fluids", "boolean");
		types.put("protection.firespread", "boolean");
		types.put("protection.grow", "boolean");
		types.put("protection.lavafirespread", "boolean");
		types.put("protection.lighter", "boolean");
		types.put("protection.painting", "boolean");
		types.put("protection.punish", "boolean");
		types.put("protection.piston", "boolean");
		types.put("protection.tnt", "boolean");
		
		types.put("protection.checkExit", "boolean");
		types.put("protection.checkSpectator", "boolean");
		types.put("protection.checkLounges", "boolean");
		types.put("protection.inventory", "boolean");

		types.put("start.countdown", "int");
		types.put("start.health", "int");
		types.put("start.foodLevel", "int");
		types.put("start.saturation", "int");
		types.put("start.exhaustion", "double");

		types.put("ready.block", "item");
		types.put("ready.checkEach", "boolean");
		types.put("ready.checkEachTeam", "boolean");
		types.put("ready.min", "int");
		types.put("ready.max", "int");
		types.put("ready.minTeam", "int");
		types.put("ready.maxTeam", "int");
		types.put("ready.autoclass", "string");
		types.put("ready.startRatio", "double");
	}

	/**
	 * create a settings instance
	 * 
	 * @param a
	 *            the arena to link to
	 */
	public Settings(Arena a) {
		arena = a;
		PVPArena.instance.getAmm().addSettings(types);
		a.type().addSettings(types);
	}

	/**
	 * hand over a config node
	 * 
	 * @param node
	 *            the key to search
	 * @return the full path to the node
	 */
	private String getNode(String node) {
		for (String s : arena.cfg.getYamlConfiguration().getKeys(true)) {

			if (types.get(s) == null) {
				continue;
			}

			if (s.endsWith("." + node)) {
				return s;
			}
		}
		return "null";
	}

	/**
	 * spam a list of settings to a player
	 * 
	 * @param player
	 *            the player to send to
	 * @param page
	 *            the settings page to send
	 */
	public void list(CommandSender player, int page) {
		if (page < 1) {
			page = 1;
		}
		Set<String> keys = new HashSet<String>();

		int i = 0;

		for (String node : arena.cfg.getYamlConfiguration().getKeys(true)) {
			if (types.get(node) == null) {
				continue;
			}
			if (i++ >= (page - 1) * 10) {
				String[] s = node.split("\\.");
				keys.add(s[s.length - 1]);
			}
			if (keys.size() >= 10) {
				break;
			}
		}
		Arenas.tellPlayer(player, ChatColor.GRAY + "------ config list ["
				+ page + "] ------", arena);
		for (String node : keys) {
			Arenas.tellPlayer(player, node + " => " + types.get(getNode(node)),
					arena);
		}

	}

	/**
	 * set a setting
	 * 
	 * @param player
	 *            the player committing the setting
	 * @param node
	 *            the node to set
	 * @param value
	 *            the value to set
	 */
	public void set(CommandSender player, String node, String value) {
		db.i("player '" + player.getName() + "' tries to set node '" + node
				+ "' to '" + value + "'");

		if (!PVPArena.hasAdminPerms(player)
				&& !PVPArena.hasCreatePerms(player, arena)) {

			Arenas.tellPlayer(player,
					Language.parse("nopermto", Language.parse("set")), arena);
			return;
		}

		for (String s : arena.cfg.getYamlConfiguration().getKeys(true)) {
			if (s.endsWith("." + node)) {
				set(player, s, value);
				return;
			}
		}

		String type = types.get(node);

		if (type == null) {
			type = "";
		}

		if (type.equals("boolean")) {
			if (value.equalsIgnoreCase("true")) {
				arena.cfg.set(node, Boolean.valueOf(true));
				Arenas.tellPlayer(
						player,
						node
								+ " set to "
								+ String.valueOf(value.equalsIgnoreCase("true")),
						arena);
			} else if (value.equalsIgnoreCase("false")) {
				arena.cfg.set(node, Boolean.valueOf(false));
				Arenas.tellPlayer(
						player,
						node
								+ " set to "
								+ String.valueOf(value.equalsIgnoreCase("true")),
						arena);
			} else {
				Arenas.tellPlayer(player, "No valid boolean '" + value + "'!",
						arena);
				Arenas.tellPlayer(player, "Valid values: true | false", arena);
				return;
			}
		} else if (type.equals("string")) {
			arena.cfg.set(node, String.valueOf(value));
			Arenas.tellPlayer(player,
					node + " set to " + String.valueOf(value), arena);
		} else if (type.equals("int")) {
			int i = 0;

			try {
				i = Integer.parseInt(value);
			} catch (Exception e) {
				Arenas.tellPlayer(player, "No valid int '" + value
						+ "'! Use numbers without decimals!", arena);
				return;
			}
			arena.cfg.set(node, i);
			Arenas.tellPlayer(player, node + " set to " + String.valueOf(i),
					arena);
		} else if (type.equals("double")) {
			double d = 0;

			try {
				d = Double.parseDouble(value);
			} catch (Exception e) {
				Arenas.tellPlayer(player, "No valid double '" + value
						+ "'! Use numbers with period (.)!", arena);
				return;
			}
			arena.cfg.set(node, d);
			Arenas.tellPlayer(player, node + " set to " + String.valueOf(d),
					arena);
		} else if (type.equals("tp")) {
			if (!value.equals("exit") && !value.equals("old")
					&& !value.equals("spectator")) {
				Arenas.tellPlayer(player, "No valid tp '" + value + "'!", arena);
				Arenas.tellPlayer(player,
						"Valid values: exit | old | spectator", arena);
				return;
			}
			arena.cfg.set(node, String.valueOf(value));
			Arenas.tellPlayer(player,
					node + " set to " + String.valueOf(value), arena);
		} else if (type.equals("item")) {
			try {
				try {
					Material mat = Material.valueOf(value);
					if (!mat.equals(Material.AIR)) {
						arena.cfg.set(node, mat.name());
						Arenas.tellPlayer(player,
								node + " set to " + String.valueOf(mat.name()),
								arena);
					}
				} catch (Exception e2) {
					Material mat = Material
							.getMaterial(Integer.parseInt(value));
					arena.cfg.set(node, mat.name());
					Arenas.tellPlayer(player,
							node + " set to " + String.valueOf(mat.name()),
							arena);
				}
				arena.cfg.save();
				return;
			} catch (Exception e) {
				// nothing
			}
			Arenas.tellPlayer(player, "No valid item '" + value
					+ "'! Use valid ENUM or item id", arena);
			return;
		} else if (type.equals("items")) {
			String[] ss = value.split(",");
			ItemStack[] items = new ItemStack[ss.length];

			for (int i = 0; i < ss.length; i++) {
				items[i] = StringParser.getItemStackFromString(ss[i]);
				if (items[i] == null) {
					Arenas.tellPlayer(player, "unrecognized item: " + items[i],
							arena);
					return;
				}
			}

			arena.cfg.set(node, String.valueOf(value));
			Arenas.tellPlayer(player,
					node + " set to " + String.valueOf(value), arena);
		} else {
			Arenas.tellPlayer(player, "Unknown node: " + node, arena);
			Arenas.tellPlayer(player,
					"use /pa [name] set [page] to get a node list", arena);
			return;
		}
		arena.cfg.save();
	}

}

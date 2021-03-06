package net.slipcor.pvparena.command;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.managers.Arenas;
import net.slipcor.pvparena.neworder.ArenaRegion;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PAABorders extends PAA_Command {

	@Override
	public void commit(Arena arena, CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			Arenas.tellPlayer(sender, Language.parse("onlyplayers"));
			return;
		}
		
		if (!checkArgs(sender, args, 2)) {
			return;
		}
		
		Player player = (Player) sender;
		
		if (!PVPArena.hasAdminPerms(player)
				&& !(PVPArena.hasCreatePerms(player, arena))) {
			Arenas.tellPlayer(player,
					Language.parse("nopermto", Language.parse("admin")));
			return;
		}
		ArenaRegion region = arena.regions.get(args[1]);
		if (region == null) {
			Arenas.tellPlayer(player, "Region unknown: " + args[1], arena);
			return;
		}
		
		region.showBorder(player);
	}

	@Override
	public String getName() {
		return "PAABorders";
	}
}

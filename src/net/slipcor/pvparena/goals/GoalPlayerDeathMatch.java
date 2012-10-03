package net.slipcor.pvparena.goals;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.arena.PlayerState;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.classes.PACheckResult;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaGoal;
import net.slipcor.pvparena.managers.ArenaManager;
import net.slipcor.pvparena.runnables.EndRunnable;
import net.slipcor.pvparena.runnables.InventoryRefillRunnable;

/**
 * <pre>Arena Goal class "PlayerDeathMatch"</pre>
 * 
 * The first Arena Goal. Players have lives. When every life is lost, the player
 * is teleported to the spectator spawn to watch the rest of the fight.
 * 
 * @author slipcor
 * 
 * @version v0.9.0
 */

public class GoalPlayerDeathMatch extends ArenaGoal {
	public GoalPlayerDeathMatch(Arena arena) {
		super(arena, "PlayerDeathMatch");
		db = new Debug(101);
	}
	
	EndRunnable er = null;

	HashMap<String, Integer> lives = new HashMap<String, Integer>();

	@Override
	public String version() {
		return "v0.9.0.0";
	}

	int priority = 2;
	
	@Override
	public PACheckResult checkEnd(PACheckResult res) {
		if (res.getPriority() > priority) {
			return res;
		}

		int count = lives.size();

		if (count == 1) {
			res.setModName(getName());
			res.setPriority(priority); // yep. only one player left. go!
		} else if (count == 0) {
			res.setError(MSG.ERROR_NOPLAYERFOUND.toString());
		}

		return res;
	}

	@Override
	public String checkForMissingSpawns(Set<String> list) {
		if (!arena.isFreeForAll()) {
			return null; // teams are handled somewhere else
		}
		int count = 0;
		for (String s : list) {
			if (s.startsWith("spawn")) {
				count++;
			}
		}
		return count > 3 ? null : "need more spawns! ("+count+"/4)";
	}

	@Override
	public PACheckResult checkPlayerDeath(PACheckResult res, Player player) {
		if (res.getPriority() <= priority) {
			res.setModName(getName());
			res.setPriority(priority);
		}
		return res;
	}
	
	@Override
	public GoalPlayerDeathMatch clone() {
		return new GoalPlayerDeathMatch(arena);
	}

	@Override
	public void commitEnd() {
		if (er != null) {
			return;
		}
		for (ArenaTeam team : arena.getTeams()) {
			for (ArenaPlayer ap : team.getTeamMembers()) {
				if (!ap.getStatus().equals(Status.FIGHT))
					continue;
				
				PVPArena.instance.getAmm().announce(arena, Language.parse(MSG.PLAYER_HAS_WON, ap.getName()), "WINNER");

				arena.broadcast(Language.parse(MSG.PLAYER_HAS_WON, ap.getName()));
			}
			if (PVPArena.instance.getAmm().commitEnd(arena, team)) {
				return;
			}
		}
		
		er = new EndRunnable(arena, arena.getArenaConfig().getInt(CFG.TIME_ENDCOUNTDOWN));
	}

	@Override
	public void commitPlayerDeath(Player killer,
			boolean doesRespawn, String error, PlayerDeathEvent event) {
		
		Player ex = killer;
		
		if (ex.getKiller() == null && !lives.containsKey(ex.getKiller().getName())) {
			return;
		}
		killer = ex.getKiller();
		
		int i = lives.get(killer.getName());
		db.i("kills to go: " + i);
		if (i <= 1) {
			// player has won!
			HashSet<ArenaPlayer> plrs = new HashSet<ArenaPlayer>();
			for (ArenaPlayer ap : arena.getFighters()) {
				if (ap.getName().equals(killer.getName())) {
					continue;
				}
				plrs.add(ap);
			}
			for (ArenaPlayer ap : plrs) {
				lives.remove(ap.getName());
				db.i("faking player death");
				arena.removePlayer(ap.get(), CFG.TP_LOSE.toString(), true);
				
				ap.setStatus(Status.LOST);
				ap.addLosses();
				
				PlayerState.fullReset(arena, ap.get());
				
				//new PlayerResetRunnable(ap,0, ap.get().getLocation());
				//TODO - timer is inactive - if this works, timer can just ... die
				
				if (ArenaManager.checkAndCommit(arena))
					return;
			}
			
			PVPArena.instance.getAgm().checkAndCommit(arena);
		} else {
			i--;
			lives.put(killer.getName(), i);

			new InventoryRefillRunnable(arena, ex, event.getDrops(), 0);
			
			ArenaTeam respawnTeam = ArenaPlayer.parsePlayer(ex.getName()).getArenaTeam();

			arena.broadcast(Language.parse(MSG.FIGHT_KILLED_BY_REMAINING_FRAGS,
					respawnTeam.colorizePlayer(killer) + ChatColor.YELLOW,
					arena.parseDeathCause(ex, event.getEntity()
							.getLastDamageCause().getCause(), killer),
					String.valueOf(i)));
			
			arena.tpPlayerToCoordName(ex, (arena.isFreeForAll()?"":respawnTeam.getName())
					+ "spawn");
			
			arena.unKillPlayer(ex, event.getEntity()
					.getLastDamageCause().getCause(), ex.getKiller());
		}
	}

	@Override
	public void displayInfo(CommandSender sender) {
		sender.sendMessage("lives: " + arena.getArenaConfig().getInt(CFG.GOAL_PDM_LIVES));
	}

	@Override
	public PACheckResult getLives(PACheckResult res, ArenaPlayer ap) {
		if (!res.hasError() && res.getPriority() <= priority) {
			res.setError("" + (arena.getArenaConfig().getInt(CFG.GOAL_PDM_LIVES)-(lives.containsKey(ap.getName())?lives.get(ap.getName()):0)));
		}
		return res;
	}

	@Override
	public boolean hasSpawn(String string) {
		return (arena.isFreeForAll() && string.toLowerCase().startsWith("spawn"));
	}

	@Override
	public void initate(Player player) {
		lives.put(player.getName(), arena.getArenaConfig().getInt(CFG.GOAL_PDM_LIVES));
	}
	
	@Override
	public void reset(boolean force) {
		er = null;
		lives.clear();
	}

	@Override
	public void teleportAllToSpawn() {
		for (ArenaTeam team : arena.getTeams()) {
			for (ArenaPlayer ap : team.getTeamMembers()) {
				this.lives
						.put(ap.getName(), arena.getArenaConfig().getInt(CFG.GOAL_PDM_LIVES));
			}
		}
	}
}
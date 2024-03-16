package org.crystaltine.advancementrace;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.*;

import java.util.HashMap;

public class AdvRaceScoreboard {

    private final HashMap<Player, Integer> points;
    private final ScoreboardManager manager;
    private final Scoreboard board;
    private final Objective objective;

    private final Plugin plugin;

    /**
     * Creates a new instance of the scoreboard object.
     *
     * @param points Pointer to the points hashmap
     *
     * @throws NullPointerException if the scoreboard manager is null (error while creating the scoreboard)
     */
    public AdvRaceScoreboard(HashMap<Player, Integer> points, Plugin plugin) {
        this.points = points;
        this.manager = Bukkit.getScoreboardManager();
        this.plugin = plugin;

        if (manager == null) {
            System.out.println("Scoreboard manager is null");
            throw new NullPointerException();
        }

        this.board = manager.getNewScoreboard();
        this.objective = board.registerNewObjective("AdvancementRace", Criteria.DUMMY, "Advancement");
    }

    @EventHandler
    public void PlayerJoin(PlayerJoinEvent e) {
        final Player p = e.getPlayer();
        final Scoreboard board = this.board;
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, new Runnable() {
            public void run() {
                p.setScoreboard(board);
            }
        },0, 20 * 10);
    }

    public void displayForPlayers() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setScoreboard(board);
        }
    }

    public void updateScoreboard() {
        for (Player p : points.keySet()) {
            objective.getScore(p.getName()).setScore(points.get(p));
        }
    }
}
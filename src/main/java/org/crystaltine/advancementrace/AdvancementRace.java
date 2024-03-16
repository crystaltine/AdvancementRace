package org.crystaltine.advancementrace;

import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.advancement.Advancement;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.HashMap;
import java.util.HashSet;

public final class AdvancementRace extends JavaPlugin implements Listener {

    HashSet<Advancement> completedAdvs = new HashSet<Advancement>();
    int winThreshold;

    HashMap<Player, Integer> points = new HashMap<Player, Integer>();

    boolean gameStarted = false;

    @Override
    public void onEnable() {
        // Plugin startup logic
        PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvents(this, this);

        System.out.println("AdvancementRace has been enabled!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        System.out.println("AdvancedRace has been disabled!");
    }

    @EventHandler
    public void onAdvancementComplete(PlayerAdvancementDoneEvent e) {
        // Ignore recipe and root advancements (not real!!!)
        if (e.getAdvancement().getKey().getKey().contains("recipe") || e.getAdvancement().getKey().getKey().contains("root")) {
            return;
        }
        if (!gameStarted) {
            e.getPlayer().sendMessage("AdvancementRace is installed but the challenge wasn't started. Revoking advancement. Run /advrace-start to start the challenge");
            for (String s : e.getPlayer().getAdvancementProgress(e.getAdvancement()).getAwardedCriteria()) {
                e.getPlayer().getAdvancementProgress(e.getAdvancement()).revokeCriteria(s);
            }
        }

        // If the advancement has already been completed
        if (completedAdvs.contains(e.getAdvancement())) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(e.getPlayer().getDisplayName() + " has completed the already-obtained advancement [" + ChatColor.GREEN + e.getAdvancement().getKey().getKey() + "]");
            }
            return;
        }

        // Else, that means the advancement has not been completed
        completedAdvs.add(e.getAdvancement());
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage( e.getPlayer().getDisplayName() + " has become the first to complete the advancement [" + ChatColor.LIGHT_PURPLE + e.getAdvancement().getKey().getKey() + "]");
        }

        // Add points to the player
        points.put(e.getPlayer(), points.get(e.getPlayer()) + 1);

        // Check if the player has reached the win threshold
        if (points.get(e.getPlayer()) >= winThreshold) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendTitle(ChatColor.AQUA + e.getPlayer().getDisplayName() + " wins!", "", 10, 70, 20);
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            }
            gameStarted = false;
        }
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (label.equals("advrace-start") && !gameStarted) {
            if (args.length == 1) {
                try {
                    winThreshold = Integer.parseInt(args[0]);
                } catch (Exception e) {
                    sender.sendMessage(ChatColor.RED + "Advancement Race win threshold must be an integer.");
                    return false;
                }
            } else {
                winThreshold = 15;
            }

            // Turn AnnounceAdvancements off since we are using a custom message
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "gamerule announceAdvancements false");

            for (Player p : Bukkit.getOnlinePlayers()) {
                points.put(p, 0);
                // revoke all advancements
                for (Advancement adv : completedAdvs) {
                    for (String s : p.getAdvancementProgress(adv).getAwardedCriteria()) {
                        p.getAdvancementProgress(adv).revokeCriteria(s);
                    }
                }

                p.sendMessage(ChatColor.GREEN + "[AdvancementRace] The game has started");
            }
            gameStarted = true;
            return true;
        }
        return false;
    }
}

class AdvRaceScoreboard {

    private final HashMap<Player, Integer> points;
    private final Objective objective;

    /**
     * Creates a new instance of the scoreboard object.
     *
     * @param points Pointer to the points hashmap
     *
     * @throws NullPointerException if the scoreboard manager is null (error while creating the scoreboard)
     */
    public AdvRaceScoreboard(HashMap<Player, Integer> points) {
        this.points = points;
        ScoreboardManager manager = Bukkit.getScoreboardManager();

        if (manager == null) {
            System.out.println("Scoreboard manager is null");
            throw new NullPointerException();
        }

        final Scoreboard board = manager.getNewScoreboard();

        Criteria criteria = Criteria.DUMMY;

        this.objective = board.registerNewObjective("AdvancementRace", criteria, "Advancement");
    }

    // TODO
    @EventHandler
    public void PlayerJoin(PlayerJoinEvent e) {
        final Player p = e.getPlayer();
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.plugin, new Runnable() {
            public void run() {
                ScoreboardManager manager = Bukkit.getScoreboardManager();
                final Scoreboard board = manager.getNewScoreboard();
                final Objective objective = board.registerNewObjective("test", "dummy");
                objective.setDisplaySlot(DisplaySlot.SIDEBAR);
                objective.setDisplayName(ChatColor.RED + "YourScoreboardTitle");
                Score score = objective.getScore("Score10");
                score.setScore(10);
                Score score1 = objective.getScore("Score9");
                score1.setScore(9);
                Score score2 = objective.getScore("Score8");
                score2.setScore(8);
                Score score3 = objective.getScore("§6Colors");
                score3.setScore(7);
                p.setScoreboard(board);
            }
        },0, 20 * 10);

    }
}
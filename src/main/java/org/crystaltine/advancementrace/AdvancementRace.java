package org.crystaltine.advancementrace;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

public final class AdvancementRace extends JavaPlugin implements Listener {

    HashSet<Advancement> completedAdvs = new HashSet<Advancement>();
    int winThreshold;

    HashMap<Player, Integer> points = new HashMap<Player, Integer>();

    boolean gameStarted = false;

    AdvRaceScoreboard scoreboardThingy;

    @Override
    public void onEnable() {
        // Plugin startup logic
        PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvents(this, this);
        scoreboardThingy = new AdvRaceScoreboard(points);

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
            return;
        }

        TextComponent advancement = new TextComponent("[" + Objects.requireNonNull(e.getAdvancement().getDisplay()).getTitle() + "]");
        BaseComponent[] hover = new ComponentBuilder(e.getAdvancement().getDisplay().getTitle() + "\n" + e.getAdvancement().getDisplay().getDescription()).create();
        advancement.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover));

        // If the advancement has already been completed
        if (completedAdvs.contains(e.getAdvancement())) {
            TextComponent alreadyCompleted = new TextComponent(e.getPlayer().getDisplayName() + " has completed the already-obtained advancement ");
            advancement.setColor(net.md_5.bungee.api.ChatColor.GREEN);
            for(BaseComponent b : hover){
                b.setColor(net.md_5.bungee.api.ChatColor.GREEN);
            }
            alreadyCompleted.addExtra(advancement);
            getServer().spigot().broadcast(alreadyCompleted);
            return;
        }

        // Else, that means the advancement has not been completed
        completedAdvs.add(e.getAdvancement());
        TextComponent firstCompleted = new TextComponent(e.getPlayer().getDisplayName() + " has become the first to complete the advancement ");
        advancement.setColor(net.md_5.bungee.api.ChatColor.LIGHT_PURPLE);
        for(BaseComponent b : hover){
            b.setColor(net.md_5.bungee.api.ChatColor.LIGHT_PURPLE);
        }
        firstCompleted.addExtra(advancement);
        getServer().spigot().broadcast(firstCompleted);

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

        //Update scoreboard
        scoreboardThingy.updateScoreboard();
        scoreboardThingy.displayForPlayers();
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

            scoreboardThingy.updateScoreboard();
            scoreboardThingy.displayForPlayers();

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
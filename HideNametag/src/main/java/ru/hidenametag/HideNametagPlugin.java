package ru.hidenametag;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public final class HideNametagPlugin extends JavaPlugin implements Listener {

    private static final String TEAM_NAME = "lhs_hn_hidden";
    private boolean enabled;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        enabled = getConfig().getBoolean("hide-nametags", true);

        getServer().getPluginManager().registerEvents(this, this);
        applyToAll();

        getLogger().info("HideNametag включен. Скрытие ников: " + (enabled ? "ВКЛ" : "ВЫКЛ"));
    }

    @Override
    public void onDisable() {
        removeTeam();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (enabled) {
            addPlayerToTeam(event.getPlayer());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("hidenametag")) {
            return false;
        }

        if (!sender.hasPermission("hidenametag.admin")) {
            sender.sendMessage(ChatColor.RED + "У вас нет прав на использование этой команды.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(ChatColor.YELLOW + "Использование: /hidenametag <on|off|status>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "on" -> {
                enabled = true;
                getConfig().set("hide-nametags", true);
                saveConfig();
                applyToAll();
                sender.sendMessage(ChatColor.GREEN + "Ники над головой теперь скрыты.");
            }
            case "off" -> {
                enabled = false;
                getConfig().set("hide-nametags", false);
                saveConfig();
                removeTeam();
                sender.sendMessage(ChatColor.GREEN + "Ники над головой теперь видны.");
            }
            case "status" -> sender.sendMessage(ChatColor.AQUA + "Скрытие ников сейчас: "
                    + (enabled ? ChatColor.GREEN + "включено" : ChatColor.RED + "выключено"));
            default -> sender.sendMessage(ChatColor.YELLOW + "Использование: /hidenametag <on|off|status>");
        }

        return true;
    }

    private void applyToAll() {
        if (!enabled) {
            removeTeam();
            return;
        }

        Team team = getOrCreateTeam();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!team.hasEntry(player.getName())) {
                team.addEntry(player.getName());
            }
        }
    }

    private Team getOrCreateTeam() {
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = board.getTeam(TEAM_NAME);

        if (team == null) {
            team = board.registerNewTeam(TEAM_NAME);
        }

        team.setNameTagVisibility(NameTagVisibility.NEVER);
        return team;
    }

    private void addPlayerToTeam(Player player) {
        if (!enabled) {
            return;
        }

        Team team = getOrCreateTeam();
        if (!team.hasEntry(player.getName())) {
            team.addEntry(player.getName());
        }
    }

    private void removeTeam() {
        ScoreboardManagerSafe.removeTeam(TEAM_NAME);
    }

    /**
     * Keeps scoreboard access in one place and safely handles shutdown/reload.
     */
    private static final class ScoreboardManagerSafe {
        private ScoreboardManagerSafe() {
        }

        private static void removeTeam(String name) {
            if (Bukkit.getScoreboardManager() == null) {
                return;
            }

            Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
            Team team = board.getTeam(name);
            if (team != null) {
                team.unregister();
            }
        }
    }
}

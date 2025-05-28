package dev.lisomandiy.murGreeting;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MurGreeting extends JavaPlugin implements Listener, TabCompleter {
    private String joinMessage = "§aДобро пожаловать!";
    private String titleMessage = "";
    private String subtitleMessage = "";
    private Sound joinSound = null;

    private boolean showJoinQuit = true;
    private boolean enableGreeting = true;
    private boolean enableTitle = true;
    private boolean enableSound = true;
    private boolean enableChat = true;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        FileConfiguration config = getConfig();
        showJoinQuit = config.getBoolean("show-default-join-message", true);

        joinMessage = parse(config.getString("join-message", "&aДобро пожаловать!"));
        titleMessage = parse(config.getString("title-message", ""));
        subtitleMessage = parse(config.getString("subtitle-message", ""));

        String soundName = config.getString("sound", "ENTITY_PLAYER_LEVELUP");
        if (soundName.equalsIgnoreCase("none") || soundName.equalsIgnoreCase("false")) {
            joinSound = null;
        } else {
            try {
                joinSound = Sound.valueOf(soundName.toUpperCase());
            } catch (IllegalArgumentException e) {
                getLogger().warning("Неверное имя звука в config.yml: " + soundName);
                joinSound = null;
            }
        }

        getServer().getPluginManager().registerEvents(this, this);
        getCommand("murgreeting").setTabCompleter(this);

        getLogger().info("Плагин murGreeting успешно включён.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Плагин murGreeting выключен.");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String name = player.getName();

        if (!showJoinQuit) event.setJoinMessage(null);

        if (enableGreeting) {
            String msg = applyPlaceholders(player, joinMessage.replace("$player", name));
            if (enableChat) player.sendMessage(msg);
        }

        if (enableTitle) {
            String title = applyPlaceholders(player, titleMessage.replace("$player", name));
            String subtitle = applyPlaceholders(player, subtitleMessage.replace("$player", name));
            player.sendTitle(title, subtitle, 10, 60, 10);
        }

        if (enableSound && joinSound != null) {
            player.playSound(player.getLocation(), joinSound, 1.0f, 1.0f);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("murgreeting")) {

            Player player = null;
            if (sender instanceof Player) {
                player = (Player) sender;
                if (!hasPermission(player, "murgreeting.use")) {
                    player.sendMessage("§cУ Вас нет прав на эту команду.");
                    return true;
                }
            }

            if (args.length == 0) {
                player.sendMessage("§eИспользование: /murgreeting reload | editconfig <ключ> <значение>");
                return true;
            }

            if (args[0].equalsIgnoreCase("reload")) {
                reloadConfig();
                FileConfiguration config = getConfig();

                joinMessage = applyHexColors(config.getString("join-message", "&aДобро пожаловать!"))
                        .replace("&", "§").replace("\\n", "\n");
                titleMessage = applyHexColors(config.getString("title-message", ""))
                        .replace("&", "§").replace("\\n", "\n");
                subtitleMessage = applyHexColors(config.getString("subtitle-message", ""))
                        .replace("&", "§").replace("\\n", "\n");
                showJoinQuit = config.getBoolean("show-default-join-message", true);

                String soundName = config.getString("sound", "ENTITY_PLAYER_LEVELUP");
                if (soundName.equalsIgnoreCase("none") || soundName.equalsIgnoreCase("false")) {
                    joinSound = null;
                } else {
                    try {
                        joinSound = Sound.valueOf(soundName.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        player.sendMessage("§cНеверный звук в конфиге: " + soundName);
                        joinSound = null;
                    }
                }

                player.sendMessage("§aКонфигурация перезагружена.");
                return true;
            }

            if (args[0].equalsIgnoreCase("editconfig")) {
                if (args.length < 3) {
                    player.sendMessage("§cИспользование: /murgreeting editconfig <ключ> <значение>");
                    return true;
                }

                String key = args[1];
                String value = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                FileConfiguration config = getConfig();

                List<String> validKeys = Arrays.asList("join-message", "title-message", "subtitle-message",
                        "show-default-join-message", "sound");

                if (!validKeys.contains(key)) {
                    player.sendMessage("§cНедопустимый ключ. Возможные: " + String.join(", ", validKeys));
                    return true;
                }

                if (key.equals("show-default-join-message")) {
                    boolean boolValue = value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes");
                    config.set(key, boolValue);
                } else {
                    config.set(key, value);
                }

                saveConfig();
                player.sendMessage("§aКлюч '" + key + "' обновлён. Не забудьте /murgreeting reload.");
                return true;
            }

            player.sendMessage("§cНеизвестный аргумент.");
            return true;
        }

        if (command.getName().equalsIgnoreCase("gmessages")) {
            Player player = null;
            if (sender instanceof Player) {
                player = (Player) sender;
                if (!hasPermission(player, "murgreeting.use")) {
                    player.sendMessage("§cУ Вас нет прав на эту команду.");
                    return true;
                }
            }

            showJoinQuit = !showJoinQuit;
            getConfig().set("show-default-join-message", showJoinQuit);
            saveConfig();
            player.sendMessage("§7Системное сообщение входа теперь: " +
                    (showJoinQuit ? "§aвключено" : "§cотключено"));
            return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!command.getName().equalsIgnoreCase("murgreeting")) return Collections.emptyList();

        if (args.length == 1) {
            return Arrays.asList("reload", "editconfig");
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("editconfig")) {
            return Arrays.asList("join-message", "title-message", "subtitle-message",
                    "show-default-join-message", "sound");
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("editconfig") && args[1].equalsIgnoreCase("sound")) {
            return Arrays.stream(Sound.values())
                    .map(Enum::name)
                    .filter(name -> name.startsWith(args[2].toUpperCase()))
                    .limit(20)
                    .toList();
        }

        return Collections.emptyList();
    }

    private String parse(String message) {
        if (message == null) return "";
        return applyHexColors(message)
                .replace("&", "§")
                .replace("\\n", "\n");
    }

    private String applyHexColors(String message) {
        if (message == null) return "";
        Pattern pattern = Pattern.compile("(?i)#([A-Fa-f0-9]{6})");
        Matcher matcher = pattern.matcher(message);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : hex.toCharArray()) {
                replacement.append('§').append(c);
            }
            matcher.appendReplacement(buffer, replacement.toString());
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private String applyPlaceholders(Player player, String text) {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return PlaceholderAPI.setPlaceholders(player, text);
        }
        return text;
    }

    private boolean hasPermission(Player player, String permission) {
        if (Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
            return player.hasPermission(permission);
        } return player.isOp();
    }
}

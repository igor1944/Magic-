package ru.magicplus.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.magicplus.MagicPlusPlugin;
import ru.magicplus.config.MessageService;
import ru.magicplus.mana.ManaManager;
import ru.magicplus.mana.ManaProfile;
import ru.magicplus.spell.Spell;
import ru.magicplus.spell.SpellManager;
import ru.magicplus.util.ColorUtil;
import ru.magicplus.wand.WandService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Команда /magic. Все подсказки и ответы пользователю даны на русском языке. */
public final class MagicCommand implements CommandExecutor, TabCompleter {
    private final MagicPlusPlugin plugin;
    private final ManaManager manaManager;
    private final SpellManager spellManager;
    private final WandService wandService;
    private final MessageService messages;

    public MagicCommand(MagicPlusPlugin plugin, ManaManager manaManager, SpellManager spellManager,
                        WandService wandService, MessageService messages) {
        this.plugin = plugin;
        this.manaManager = manaManager;
        this.spellManager = spellManager;
        this.wandService = wandService;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("magicplus.use")) {
            messages.send(sender, "no-permission");
            return true;
        }
        if (args.length == 0 || args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("помощь")) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "mana", "мана" -> showMana(sender, args);
            case "spells", "заклинания" -> showSpells(sender);
            case "select", "выбрать" -> selectSpell(sender, args);
            case "cast", "применить" -> castSpell(sender, args);
            case "wand", "жезл" -> giveWand(sender, args);
            case "reload", "перезагрузка" -> reload(sender);
            case "setmana" -> changeMana(sender, args, ChangeMode.SET);
            case "addmana" -> changeMana(sender, args, ChangeMode.ADD);
            case "setmax" -> changeMana(sender, args, ChangeMode.SET_MAX);
            default -> messages.send(sender, "unknown-command");
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ColorUtil.color("&8&m---------------- &d&lMagic+ &8&m----------------"));
        sender.sendMessage(ColorUtil.color("&d/magic mana &8— &7посмотреть свою ману"));
        sender.sendMessage(ColorUtil.color("&d/magic spells &8— &7список доступных заклинаний"));
        sender.sendMessage(ColorUtil.color("&d/magic select <id> &8— &7выбрать заклинание"));
        sender.sendMessage(ColorUtil.color("&d/magic cast <id> [игрок] &8— &7применить заклинание"));
        sender.sendMessage(ColorUtil.color("&d/magic wand &8— &7получить волшебный жезл"));
        sender.sendMessage(ColorUtil.color("&7Жезл: &fЛКМ &8— &7сменить магию, &fПКМ &8— &7применить."));
        if (sender.hasPermission("magicplus.admin")) {
            sender.sendMessage(ColorUtil.color("&c/magic reload &8— &7перезагрузить настройки"));
            sender.sendMessage(ColorUtil.color("&c/magic setmana <игрок> <число>"));
            sender.sendMessage(ColorUtil.color("&c/magic addmana <игрок> <число>"));
            sender.sendMessage(ColorUtil.color("&c/magic setmax <игрок> <число>"));
            sender.sendMessage(ColorUtil.color("&c/magic wand [игрок] &8— &7выдать жезл"));
        }
        sender.sendMessage(ColorUtil.color("&8&m----------------------------------------"));
    }

    private void showMana(CommandSender sender, String[] args) {
        if (args.length >= 2) {
            if (!sender.hasPermission("magicplus.admin")) {
                messages.send(sender, "no-permission");
                return;
            }
            Player target = findPlayer(sender, args[1]);
            if (target == null) {
                return;
            }
            ManaProfile profile = manaManager.get(target);
            messages.send(sender, "mana-other", Map.of(
                    "player", target.getName(),
                    "mana", ColorUtil.number(profile.getMana()),
                    "max", ColorUtil.number(profile.getMaxMana())
            ));
            return;
        }
        Player player = requirePlayer(sender);
        if (player == null) {
            return;
        }
        ManaProfile profile = manaManager.get(player);
        messages.send(sender, "mana-self", Map.of(
                "mana", ColorUtil.number(profile.getMana()),
                "max", ColorUtil.number(profile.getMaxMana())
        ));
    }

    private void showSpells(CommandSender sender) {
        Player player = requirePlayer(sender);
        if (player == null) {
            return;
        }
        sender.sendMessage(ColorUtil.color("&8&m------------- &dДоступные заклинания &8&m-------------"));
        for (Spell spell : spellManager.getAvailable(player)) {
            sender.sendMessage(ColorUtil.color(" &d" + spell.id() + " &8— ") + spell.displayName()
                    + ColorUtil.color(" &7| &5" + ColorUtil.number(spell.manaCost()) + " маны"
                    + " &7| &f" + ColorUtil.number(spell.cooldownMillis() / 1000.0) + " сек."));
            sender.sendMessage(ColorUtil.color("   &8" + spell.description()));
        }
    }

    private void selectSpell(CommandSender sender, String[] args) {
        Player player = requirePlayer(sender);
        if (player == null) {
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(ColorUtil.color("&cИспользование: /magic select <id>"));
            return;
        }
        spellManager.select(player, args[1]);
    }

    private void castSpell(CommandSender sender, String[] args) {
        Player player = requirePlayer(sender);
        if (player == null) {
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(ColorUtil.color("&cИспользование: /magic cast <id> [игрок]"));
            return;
        }
        String explicitTarget = args.length >= 3 ? args[2] : null;
        spellManager.cast(player, args[1], explicitTarget);
    }

    private void giveWand(CommandSender sender, String[] args) {
        Player target;
        if (args.length >= 2) {
            if (!sender.hasPermission("magicplus.admin")) {
                messages.send(sender, "no-permission");
                return;
            }
            target = findPlayer(sender, args[1]);
        } else {
            target = requirePlayer(sender);
        }
        if (target != null) {
            wandService.give(target);
        }
    }

    private void reload(CommandSender sender) {
        if (!sender.hasPermission("magicplus.admin")) {
            messages.send(sender, "no-permission");
            return;
        }
        plugin.reloadPlugin();
        messages.send(sender, "reload");
    }

    private void changeMana(CommandSender sender, String[] args, ChangeMode mode) {
        if (!sender.hasPermission("magicplus.admin")) {
            messages.send(sender, "no-permission");
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(ColorUtil.color("&cИспользование: /magic " + args[0] + " <игрок> <число>"));
            return;
        }
        Player target = findPlayer(sender, args[1]);
        if (target == null) {
            return;
        }
        Double value = parseNumber(sender, args[2], mode == ChangeMode.SET_MAX);
        if (value == null) {
            return;
        }

        switch (mode) {
            case SET -> {
                manaManager.set(target, value);
                messages.send(sender, "mana-set", Map.of(
                        "player", target.getName(), "mana", ColorUtil.number(manaManager.get(target).getMana())));
            }
            case ADD -> {
                manaManager.add(target, value);
                messages.send(sender, "mana-added", Map.of(
                        "player", target.getName(),
                        "amount", ColorUtil.number(value),
                        "mana", ColorUtil.number(manaManager.get(target).getMana())));
            }
            case SET_MAX -> {
                manaManager.setMax(target, value);
                messages.send(sender, "max-mana-set", Map.of(
                        "player", target.getName(), "max", ColorUtil.number(manaManager.get(target).getMaxMana())));
            }
        }
    }

    private Double parseNumber(CommandSender sender, String input, boolean mustBePositive) {
        try {
            double value = Double.parseDouble(input);
            if (!Double.isFinite(value) || value < 0.0 || (mustBePositive && value <= 0.0)) {
                throw new NumberFormatException();
            }
            return value;
        } catch (NumberFormatException exception) {
            messages.send(sender, "invalid-number", Map.of("value", input));
            return null;
        }
    }

    private Player requirePlayer(CommandSender sender) {
        if (sender instanceof Player player) {
            return player;
        }
        messages.send(sender, "players-only");
        return null;
    }

    private Player findPlayer(CommandSender sender, String name) {
        Player player = Bukkit.getPlayerExact(name);
        if (player == null) {
            messages.send(sender, "player-not-found", Map.of("player", name));
        }
        return player;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> commands = new ArrayList<>(Arrays.asList(
                    "help", "mana", "spells", "select", "cast", "wand"));
            if (sender.hasPermission("magicplus.admin")) {
                commands.addAll(Arrays.asList("reload", "setmana", "addmana", "setmax"));
            }
            return filter(commands, args[0]);
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("select") || args[0].equalsIgnoreCase("cast"))) {
            return filter(spellManager.getSpellIds(), args[1]);
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("wand") || args[0].equalsIgnoreCase("mana")
                || args[0].equalsIgnoreCase("setmana") || args[0].equalsIgnoreCase("addmana")
                || args[0].equalsIgnoreCase("setmax"))) {
            return filter(onlinePlayerNames(), args[1]);
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("cast")) {
            return filter(onlinePlayerNames(), args[2]);
        }
        return Collections.emptyList();
    }

    private List<String> onlinePlayerNames() {
        List<String> names = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            names.add(player.getName());
        }
        return names;
    }

    private List<String> filter(List<String> values, String input) {
        String lower = input.toLowerCase(Locale.ROOT);
        return values.stream().filter(value -> value.toLowerCase(Locale.ROOT).startsWith(lower)).toList();
    }

    private enum ChangeMode {
        SET,
        ADD,
        SET_MAX
    }
}

package com.rabbit_company.msm.commands;

import com.rabbit_company.msm.MinecraftServerMonitor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Reload implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if((sender instanceof Player player) && !player.isOp()) {
            player.sendMessage(Component.text("You do not have permission to execute this command!").color(TextColor.color(0xFF0000)));
            return true;
        }

        MinecraftServerMonitor.getInstance().reloadConfig();

        MinecraftServerMonitor.metricRegistry.stop();
        MinecraftServerMonitor.httpServer.stop();

        MinecraftServerMonitor.metricRegistry.start();
        MinecraftServerMonitor.httpServer.start();
        sender.sendMessage(Component.text("MinecraftServerMonitor plugin has been reloaded").color(TextColor.color(0x00FF00)));
        return true;
    }
}

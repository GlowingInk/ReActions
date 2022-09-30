package me.fromgate.reactions.commands;

import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.util.math.NumberUtils;
import me.fromgate.reactions.util.message.Msg;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@CmdDefine(command = "reactions", description = Msg.CMD_CHECK, permission = "reactions.config",
        subCommands = {"check"}, shortDescription = "&3/react check [radius]")
public class CmdCheck extends Cmd {

    @Override
    public boolean execute(Player player, String[] args) {
        int radius = args.length > 1 && NumberUtils.isNonzeroInteger(args[1]) ? Integer.parseInt(args[1]) : 8;
        printActivatorsAround(player, radius);
        return true;
    }

    private void printActivatorsAround(Player player, int radius) {
        int playerX = player.getLocation().getBlockX();
        int playerY = player.getLocation().getBlockY();
        int playerZ = player.getLocation().getBlockZ();
        World world = player.getWorld();
        Bukkit.getScheduler().runTaskAsynchronously(ReActions.getPlugin(), () -> {
            List<String> total = new ArrayList<>();
            for (int x = playerX - radius; x <= playerX + radius; x++) {
                for (int y = playerY - radius; y <= playerY + radius; y++) {
                    for (int z = playerZ - radius; z <= playerZ + radius; z++) {
                        Collection<Activator> found = ReActions.getActivators().search().byRawLocation(world, x, y, z);
                        if (found.isEmpty()) continue;
                        found.forEach(a -> total.add(a.toString()));
                    }
                }
            }
            Bukkit.getScheduler().runTask(ReActions.getPlugin(), () -> Msg.printPage(player, total, Msg.MSG_CHECK, 1, 100, true));
        });
    }
}
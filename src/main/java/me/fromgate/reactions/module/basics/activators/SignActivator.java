/*
 *  ReActions, Minecraft bukkit plugin
 *  (c)2012-2017, fromgate, fromgate@gmail.com
 *  http://dev.bukkit.org/server-mods/reactions/
 *
 *  This file is part of ReActions.
 *
 *  ReActions is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  ReActions is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with ReActions.  If not, see <http://www.gnorg/licenses/>.
 *
 */

package me.fromgate.reactions.module.basics.activators;

import me.fromgate.reactions.logic.ActivatorLogic;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Storage;
import me.fromgate.reactions.module.basics.storages.SignStorage;
import me.fromgate.reactions.util.BlockUtils;
import me.fromgate.reactions.util.enums.ClickType;
import me.fromgate.reactions.util.parameter.BlockParameters;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class SignActivator extends Activator {

    private final List<String> maskLines;
    private final ClickType click;

    private SignActivator(ActivatorLogic base, ClickType click, List<String> maskLines) {
        super(base);
        this.click = click;
        this.maskLines = maskLines;
    }

    public static SignActivator create(ActivatorLogic base, Parameters p) {
        if (!(p instanceof BlockParameters param)) return null;
        Block targetBlock = param.getBlock();
        Sign sign = null;
        if (targetBlock != null && BlockUtils.isSign(targetBlock))
            sign = (Sign) targetBlock.getState();
        ClickType click = ClickType.getByName(param.getString("click", "RIGHT"));
        List<String> maskLines = new ArrayList<>();
        if (sign == null) {
            maskLines.add(param.getString("line1", ""));
            maskLines.add(param.getString("line2", ""));
            maskLines.add(param.getString("line3", ""));
            maskLines.add(param.getString("line4", ""));
        } else {
            maskLines.add(param.getString("line1", sign.getLine(0)));
            maskLines.add(param.getString("line2", sign.getLine(1)));
            maskLines.add(param.getString("line3", sign.getLine(2)));
            maskLines.add(param.getString("line4", sign.getLine(3)));
        }
        return new SignActivator(base, click, maskLines);
    }

    public static SignActivator load(ActivatorLogic base, ConfigurationSection cfg) {
        ClickType click = ClickType.getByName(cfg.getString("click-type", "RIGHT"));
        List<String> maskLines = cfg.getStringList("sign-mask");
        return new SignActivator(base, click, maskLines);
    }

    public boolean checkMask(String[] sign) {
        if (maskLines.isEmpty()) return false;
        int emptyLines = 0;
        for (int i = 0; i < Math.min(4, maskLines.size()); i++) {
            if (maskLines.get(i).isEmpty()) {
                emptyLines++;
                continue;
            }
            if (!ChatColor.translateAlternateColorCodes('&', maskLines.get(i))
                    .equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', sign[i]))) {
                return false;
            }
        }
        return emptyLines < 4;
    }

    @Override
    public boolean checkStorage(Storage event) {
        SignStorage signEvent = (SignStorage) event;
        if (click.checkRight(signEvent.isLeftClick())) return false;
        return checkMask(signEvent.getSignLines());
    }

    @Override
    public void saveOptions(ConfigurationSection cfg) {
        cfg.set("sign-mask", maskLines);
        cfg.set("click-type", click.name());
    }

    @Override
    public boolean isValid() {
        if (maskLines == null || maskLines.isEmpty()) {
            return false;
        }
        int emptyLines = 0;
        for (int i = 0; i < Math.min(4, maskLines.size()); i++) {
            if (maskLines.get(i).isEmpty()) {
                emptyLines++;
            }
        }
        return emptyLines > 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append(" (");
        sb.append("click:").append(this.click.name());
        sb.append(" sign:");
        if (this.maskLines.isEmpty()) sb.append("[][][][]");
        else {
            for (String s : maskLines)
                sb.append("[").append(s).append("]");
        }
        sb.append(")");
        return sb.toString();
    }
}

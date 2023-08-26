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

package fun.reactions.module.basic.activators;

import fun.reactions.model.Logic;
import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.environment.Variable;
import fun.reactions.util.BlockUtils;
import fun.reactions.util.enums.ClickType;
import fun.reactions.util.location.LocationUtils;
import fun.reactions.util.parameter.BlockParameters;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fun.reactions.model.environment.Variable.simple;

// TODO Add Hand
public class SignActivator extends Activator {

    private final List<String> maskLines;
    private final ClickType click;

    private SignActivator(Logic base, ClickType click, List<String> maskLines) {
        super(base);
        this.click = click;
        this.maskLines = maskLines;
    }

    public static SignActivator create(Logic base, Parameters p) {
        if (!(p instanceof BlockParameters param)) return null;
        Block targetBlock = param.getBlock();
        Sign sign = null;
        if (targetBlock != null && BlockUtils.isSign(targetBlock))
            sign = (Sign) targetBlock.getState();
        ClickType click = ClickType.getByName(param.getString("click", "ANY"));
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

    public static SignActivator load(Logic base, ConfigurationSection cfg) {
        ClickType click = ClickType.getByName(cfg.getString("click-type", "ANY"));
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
    public boolean checkContext(@NotNull ActivationContext context) {
        SignContext signEvent = (SignContext) context;
        if (click.checkRight(signEvent.leftClick)) return false;
        return checkMask(signEvent.signLines);
    }

    @Override
    public void saveOptions(@NotNull ConfigurationSection cfg) {
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

    public static class SignContext extends ActivationContext {
        private final boolean leftClick;
        private final Location location;
        private final String[] signLines;

        public SignContext(Player player, String[] signLines, Location loc, boolean leftClick) { // TODO Hand?
            super(player);
            this.signLines = signLines;
            this.location = loc;
            this.leftClick = leftClick;
        }

        @Override
        public @NotNull Class<? extends Activator> getType() {
            return SignActivator.class;
        }

        @Override
        protected @NotNull Map<String, Variable> prepareVariables() {
            Map<String, Variable> vars = new HashMap<>();
            for (int i = 0; i < signLines.length; i++) {
                vars.put("sign_line" + (i + 1), Variable.simple(signLines[i]));
            }
            vars.put("sign_loc", simple(LocationUtils.locationToString(location)));
            vars.put("click", Variable.simple(leftClick ? "left" : "right"));
            return vars;
        }
    }
}

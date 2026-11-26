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
import fun.reactions.model.activators.ActivatorsManager;
import fun.reactions.model.activators.type.ActivatorType;
import fun.reactions.model.environment.Variable;
import fun.reactions.model.environment.variables.LocationVariable;
import fun.reactions.util.enums.ClickType;
import fun.reactions.util.location.LocationUtils;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

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
        ClickType click = ClickType.getByName(p.getString("click-type", "ANY"));
        List<String> maskLines = new ArrayList<>();
        for (String key : p.keyedList("sign-mask")) {
            maskLines.add(p.getString(key, ""));
        }
        return new SignActivator(base, click, maskLines);
    }

    private boolean checkMask(String[] sign) {
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
        SignContext sign = (SignContext) context;
        return matches(sign.signLines, sign.leftClick);
    }

    private boolean matches(String[] lines, boolean leftClick) {
        return !click.checkRight(leftClick) && checkMask(lines);
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

    public static final class Type implements ActivatorType {
        private final Set<SignActivator> activators = new LinkedHashSet<>();

        @Override
        public @NotNull String getName() {
            return "SIGN";
        }

        @Override
        public @NotNull Class<SignActivator> getActivatorClass() {
            return SignActivator.class;
        }

        @Override
        public @NotNull Activator createActivator(@NotNull Logic logic, @NotNull Parameters params) {
            return create(logic, params);
        }

        @Override
        public boolean isNeedBlock() {
            return true;
        }

        @Override
        public void include(@NotNull Activator activator) {
            activators.add((SignActivator) activator);
        }

        @Override
        public void discard(@NotNull Activator activator) {
            activators.remove((SignActivator) activator);
        }

        public boolean trigger(
                @NotNull Player player,
                @NotNull String[] lines,
                @NotNull Location location,
                boolean leftClick
        ) {
            List<SignActivator> matched = new ArrayList<>();
            for (SignActivator activator : activators) {
                if (activator.matches(lines, leftClick)) matched.add(activator);
            }
            if (matched.isEmpty()) return false;
            SignContext context = new SignContext(player, lines, location, leftClick);
            ActivatorsManager.executeLogic(context, matched);
            return context.isCancelled();
        }
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
                vars.put("sign_line" + (i + 1), Variable.value(signLines[i]));
            }
            vars.put("sign_loc", Variable.value(LocationUtils.locationToString(location)));
            vars.put("sign_location", new LocationVariable(location));
            vars.put("click", Variable.value(leftClick ? "left" : "right"));
            return vars;
        }
    }
}

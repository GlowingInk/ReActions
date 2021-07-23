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

package me.fromgate.reactions.module.basics.flags;

import me.fromgate.reactions.logic.activity.flags.OldFlag;
import me.fromgate.reactions.util.data.RaContext;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FlagFlagSet implements OldFlag {

    private static final Pattern BRACES = Pattern.compile("(^\\{\\s*)|(\\s*}$)");
    private static final Pattern BRACES_GROUP = Pattern.compile("\\S+:\\{[^\\{\\}]*\\}|\\S+");

    private static String hideBkts(String s) {
        int count = 0;
        StringBuilder r = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            String a = String.valueOf(c);
            if (c == '{') {
                count++;
                if (count != 1) a = "#BKT1#";
            } else if (c == '}') {
                if (count != 1) a = "#BKT2#";
                count--;
            }
            r.append(a);
        }
        return r.toString();
    }

    @Override
    public boolean checkFlag(RaContext context, String param) {
        List<String> flagList = parseParamsList(param);
        if (flagList.isEmpty()) return false;
        for (String flagStr : flagList) {
            boolean negative = flagStr.startsWith("!");
            if (negative) flagStr = flagStr.replaceFirst("!", "");
            String[] fnv = flagStr.split(":", 2);
            if (fnv.length != 2) continue;
            if (Flags.checkFlag(context, fnv[0], BRACES.matcher(fnv[1]).replaceAll(""), negative)) {
                return true;
            }
        }
        return false;
    }

    private List<String> parseParamsList(String param) {
        List<String> paramList = new ArrayList<>();
        Matcher matcher = BRACES_GROUP.matcher(hideBkts(param));
        while (matcher.find()) {
            paramList.add(matcher.group().trim().replace("#BKT1#", "{").replace("#BKT2#", "}"));
        }
        return paramList;
    }
}

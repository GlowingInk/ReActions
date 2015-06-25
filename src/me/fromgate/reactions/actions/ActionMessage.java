/*  
 *  ReActions, Minecraft bukkit plugin
 *  (c)2012-2014, fromgate, fromgate@gmail.com
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

package me.fromgate.reactions.actions;

import me.fromgate.reactions.util.Param;
import me.fromgate.reactions.util.Util;

import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Set;

public class ActionMessage extends Action {

    @Override
    public boolean execute(Player p, Param params) {
        sendMessage (p,params);
        return true;
    }
    
    private String [] keys = {"region","rgplayer","player","world","faction","group","perm"};
    public boolean isKeyword(String key){
    	for (String keyStr : keys)
    		if (keyStr.equalsIgnoreCase(key)) return true;
    	return false;
    }
    
    private void sendMessage(Player player, Param params){
        Set<Player> players = Util.getPlayerList(params,player);
        String message = removeParams (params);
        if (message.isEmpty()) return;
        
        for (Player p : players){
            String key = "reactions-msg-"+this.getActivatorName()+(this.isAction() ? "act" : "react");    
            boolean showmsg = false;
            if (this.getActivator().isAnnoying()){
                if (!p.hasMetadata(key)) {
                    showmsg = true;
                    p.setMetadata(key, new FixedMetadataValue(plg(),System.currentTimeMillis()));
                } else {
                    Long before = p.getMetadata(key).get(0).asLong();
                    Long now = System.currentTimeMillis();
                    if ((now-before)>(plg().sameMessagesDelay*1000)){
                        showmsg = true;
                        p.setMetadata(key, new FixedMetadataValue(plg(),now));
                    }
                }
            } else showmsg = true;
            if (showmsg) u().printMsg(p, message);
        }
    }
    
	private String removeParams(Param params){
		String message = params.getParam("param-line", "");
		if (message.isEmpty()) return message;
		if (params.size()<=1) return message;
		String [] msgArray = message.split(" ");
		for (String key : params.keySet()){
			if (!isKeyword(key)) continue;			 
			for (int i = 0; i<msgArray.length; i++){
				String msgPart = msgArray[i].toLowerCase();
				if (msgPart.startsWith(key.toLowerCase()+":")) msgArray[i]="";
			}
		}
		String newMessage = "";
		for (int i = 0; i<msgArray.length; i++){
			if (msgArray[i].isEmpty()) continue;
			newMessage = newMessage.isEmpty() ? msgArray[i] : newMessage+(msgArray[i].isEmpty() ? "" : " "+msgArray[i]);
		}
		return newMessage;
	}
	
	


}

package me.fromgate.reactions.externals.placeholderapi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.fromgate.reactions.placeholders.Placeholders;
import me.fromgate.reactions.util.Variables;
import org.bukkit.OfflinePlayer;

import java.util.regex.Pattern;

public class PapiResolver extends PlaceholderExpansion {
	private final static String IDENTIFIER = "reactions";
	private final static String AUTHOR = "fromgate";
	private final static String VERSION = "0.0.3";
	private final static Pattern VARP = Pattern.compile("(?i)varp?:\\S+");
	private final static String EMPTY = "";

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public String getAuthor() {
		return AUTHOR;
	}

	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public String onRequest(OfflinePlayer player, String s) {
		if (Placeholders.countPlaceholder() && VARP.matcher(s).find()) {
			String placeholder = "%" + s + "%";
			String result = Variables.replacePlaceholders(player, placeholder);
			if (!placeholder.equals(result))
				return result;
		}
		return EMPTY;
	}
}
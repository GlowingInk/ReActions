package me.fromgate.reactions.placeholders;

import me.fromgate.reactions.util.Util;
import org.bukkit.entity.Player;

import java.util.regex.Pattern;

@PlaceholderDefine(id = "Random", keys = {"RANDOM", "rnd"})
public class PlaceholderRandom extends Placeholder {

	private final static Pattern WORD_LIST = Pattern.compile("[\\S,]*[\\S]");

	@Override
	public String processPlaceholder(Player player, String key, String param) {
		return random(param);
	}


	private String random(String rndStr) {
		if (Util.INT_POSITIVE.matcher(rndStr).matches())
			return Integer.toString(Util.getRandomInt(Integer.parseInt(rndStr)));

		if (Util.INT_MIN_MAX.matcher(rndStr).matches())
			return Integer.toString(Util.getMinMaxRandom(rndStr));

		if (WORD_LIST.matcher(rndStr).matches()) {
			String[] ln = rndStr.split(",");
			if (ln.length == 0) return rndStr;
			return ln[Util.getRandomInt(ln.length)];
		}
		return rndStr;
	}
}

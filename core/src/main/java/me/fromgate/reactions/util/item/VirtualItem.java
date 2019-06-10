/*
 * Copyright 2015 fromgate. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. You cannot use this file (or part of this file) in commercial projects.
 * 
 * 2. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 3. Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and contributors and should not be interpreted as representing official policies,
 * either expressed or implied, of anybody else.
 */

package me.fromgate.reactions.util.item;

import com.google.common.base.Joiner;
import me.fromgate.reactions.util.message.Msg;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VirtualItem extends ItemStack {

    private static boolean ALLOW_RANDOM = true;
    private static boolean TRY_OLD_ITEM_PARSE = true;
    static boolean ADD_REGEX = true;
    private static ThreadLocalRandom random = ThreadLocalRandom.current();
    private final static String DIVIDER = "\\n";
    private final static Pattern AMOUNT_RANDOM = Pattern.compile("<\\d+|>\\d+|<=\\d+|>=\\d+");
    private final static Pattern BYTES_RGB = Pattern.compile("^[0-9]{1,3},[0-9]{1,3},[0-9]{1,3}$");
    private final static Pattern BYTE = Pattern.compile("[0-9]{1,3}");
    private final static Pattern INT_MIN_MAX = Pattern.compile("\\d+(-\\d+)?");
    private final static Pattern INT = Pattern.compile("\\d+");
    private final static Pattern TIME_HH_MM = Pattern.compile("^[0-5][0-9]:[0-5][0-9]$");
    private final static Pattern TIME_HH_MM_SS = Pattern.compile("^([0-9]|0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]$");
    private final static Pattern PARAM_PATTERN = Pattern.compile("\\S+:\\{[^\\{\\}]*\\}|\\S+:\\S+");
    private final static Pattern PARAM_BRACKET = Pattern.compile("\\{.*\\}");

    /**
     * Constructor Create new VirtualItem object
     *
     * @param type - Item type
     */
    public VirtualItem(Material type) {
        super(type);
    }

    /**
     * Constructor Create new VirtualItem object
     *
     * @param type   - Item material
     * @param damage - Durability
     * @param amount - Amount
     */
    public VirtualItem(Material type, int damage, int amount) {
        super(type);
        this.setDamage(damage);
        this.setAmount(amount);
    }

    /**
     * Constructor Create new VirtualItem object based on ItemStack
     *
     * @param item   - Base ItemStack
     */
    public VirtualItem(ItemStack item) {
        super(item);
    }

    public static VirtualItem fromItemStack(ItemStack item) {
        if (item == null || item.getType() == Material.AIR)
            return null;
        return new VirtualItem(item);
    }

    /**
     * Create VirtualItem object based on parameter-string
     *
     * @param itemStr - String. Format: type:<Type> data:<Data> amount:<Amount> [AnotherParameters]
     *                item:<Type>:<Data>*<Amount> [AnotherParameters]
     * @return - New VirtualItem object or null (if parse failed)
     */
    public static VirtualItem fromString(String itemStr) {
        Map<String, String> params = parseParams(itemStr);
        VirtualItem vi = fromMap(params);
        if (vi != null) return vi;
        ItemStack item = parseOldItemStack(itemStr);
        if (item != null) return new VirtualItem(item);
        return null;
    }

    /**
     * Create VirtualItem object (deserialize from Map)
     *
     * @param params - Map of parameters and values
     * @return - VirtualItem object
     */

    public static VirtualItem fromMap(Map<String, String> params) {
        if (params == null || params.isEmpty())
            return null;
        Material type;
        int data;
        int amount;
        if (params.containsKey("item") || params.containsKey("default-param")) {
            String itemStr = params.containsKey("item") ? params.get("item")
                    : params.get("default-param");
            String amountStr = "1";
            if (itemStr.contains("*")) {
                itemStr = itemStr.substring(0, itemStr.indexOf("*"));
                amountStr = itemStr.substring(itemStr.indexOf("*") + 1);
            }
            if (itemStr.contains(":")) {
                itemStr = itemStr.substring(0, itemStr.indexOf(":"));
            }
            type = Material.getMaterial(itemStr.toUpperCase());
            amount = getNumber(amountStr);
            if (amount == 0) return null;
        } else if (params.containsKey("type")) {
            String typeStr = getParam(params, "type", "");
            type = Material.getMaterial(typeStr.toUpperCase());
        } else
            return null;
        if (type == null)
            return null;
        data = getNumber(getParam(params, "data", "0"));
        amount = getNumber(getParam(params, "amount", "1"));
        VirtualItem vi = new VirtualItem(type, data, amount);

        vi.setName(getParam(params, "name"));
        vi.setLore(getParam(params, "lore"));
        vi.setEnchantments(getParam(params, "enchantments"));
        vi.setBook(getParam(params, "book-author"), getParam(params, "book-title"), getParam(params, "book-pages"));
        vi.setFireworks(getNumber(getParam(params, "firework-power", "0")), getParam(params, "firework-effects"));
        vi.setColor(getParam(params, "color"));
        vi.setSkull(getParam(params, "skull-owner"));
        vi.setPotionMeta(getParam(params, "potion-effects"));
        vi.setMap(getParam(params, "map-scale", "false").equalsIgnoreCase("true"));
        vi.setEnchantStorage(getParam(params, "stored-enchants"));
        vi.setFireworkEffect(getParam(params, "firework-effects"));
        return vi;
    }

    /**
     * Serialize VirtualItem to Map<String,String>
     *
     * @return - Map of parameters to recreate item
     */
    public Map<String, String> toMap() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("type", this.getType().name());
        params.put("data", Integer.toString(this.getDamage()));
        params.put("amount", Integer.toString(this.getAmount()));
        putEnchants(params, "enchantments", this.getEnchantments());
        putItemMeta(params, this.getItemMeta());
        if (ADD_REGEX) params.put("regex", "false");
        return params;
    }

    public void putItemMeta18(Map<String, String> params) {
    }

    /**
     * Serialize item to JSON-string
     *
     * @return JSON-string generated from ItemStack
     */
    @SuppressWarnings("unchecked")
    public String toJSON() {
        Map<String, Object> itemS = this.serialize();
        JSONObject json = new JSONObject();
        for (String i : itemS.keySet())
            json.put(i, itemS.get(i));
        return json.toJSONString();
    }

    /**
     * Deserialize item from JSON-string
     *
     * @param itemJSON - JSON-string with item parameters
     * @return - VirtualItem generated from JSON-string
     */
    public static VirtualItem fromJSONString(String itemJSON) {
        if (itemJSON == null || itemJSON.isEmpty())
            return null;
        JSONParser parser = new JSONParser();
        Object object = null;
        try {
            object = parser.parse(itemJSON);
        } catch (Exception ignore) {
        }
        if (object == null)
            return null;
        JSONObject json = (JSONObject) object;
        Map<String, Object> map = new HashMap<>();
        for (Object key : json.keySet()) {
            if (key instanceof String) {
                map.put((String) key, json.get(key));
            }
        }
        if (map.isEmpty())
            return null;
        ItemStack item = ItemStack.deserialize(map);
        if (item == null)
            return null;
        return new VirtualItem(item);
    }

    // ///////////////////////////////////////////////////////
    public void giveItemOrDrop(Player player) {
        for (ItemStack i : player.getInventory().addItem(this.clone()).values())
            player.getWorld().dropItemNaturally(player.getLocation(), i);
    }

    // ////////////////////////////////////////////////////
    protected void setEnchantStorage(String enchStr) {
        if (enchStr == null || enchStr.isEmpty()) return;
        if (!(this.getItemMeta() instanceof EnchantmentStorageMeta)) return;
        EnchantmentStorageMeta esm = (EnchantmentStorageMeta) this.getItemMeta();
        String[] enchLn = enchStr.split(";");
        for (String e : enchLn) {
            String eType = e;
            int power = 0;
            if (eType.contains(":")) {
                String powerStr = eType.substring(eType.indexOf(":") + 1);
                eType = eType.substring(0, eType.indexOf(":"));
                power = INT.matcher(powerStr).matches() ? Integer.valueOf(powerStr) : 0;
            }
            Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(eType.toUpperCase()));
            if (enchantment == null) continue;
            esm.addStoredEnchant(enchantment, power, true);
        }
        this.setItemMeta(esm);
    }

    protected void setMap(boolean scale) {
        if (this.getItemMeta() instanceof MapMeta) {
            MapMeta mm = (MapMeta) this.getItemMeta();
            mm.setScaling(scale);
            this.setItemMeta(mm);
        }
    }


    protected void setPotionMeta(String potions) {
        if (potions == null || potions.isEmpty())
            return;
        if (!(this.getItemMeta() instanceof PotionMeta))
            return;
        String[] potLn = potions.split(";");
        PotionMeta pm = (PotionMeta) this.getItemMeta();
        pm.clearCustomEffects();
        for (String pStr : potLn) {
            String[] ln = pStr.trim().split(":");
            if (ln.length == 0)
                continue;
            PotionEffectType pType = PotionEffectType.getByName(ln[0]
                    .toUpperCase());
            if (pType == null)
                continue;
            int amplifier = (ln.length > 1 && INT_MIN_MAX.matcher(ln[1]).matches()) ? getNumber(ln[1]) : 0;
            int duration = (ln.length > 2) ? parseTimeTicks(ln[2]) : Integer.MAX_VALUE;
            pm.addCustomEffect(new PotionEffect(pType, duration, amplifier, true), true);
        }
        this.setItemMeta(pm);
    }

    @SuppressWarnings("deprecation")
    protected void setSkull(String owner) {
        if (owner == null || owner.isEmpty())
            return;
        if (this.getItemMeta() instanceof SkullMeta) {
            SkullMeta sm = (SkullMeta) this.getItemMeta();
            sm.setOwner(owner);
            this.setItemMeta(sm);
        }
    }

    /**
     * Configure leather armor color
     *
     * @param colorStr
     */
    protected void setColor(String colorStr) {
        if (colorStr == null || colorStr.isEmpty()) return;

        if (this.getItemMeta() instanceof LeatherArmorMeta) {
            Color c = parseColor(colorStr);
            if (c == null) return;
            LeatherArmorMeta lm = (LeatherArmorMeta) this.getItemMeta();
            lm.setColor(c);
            this.setItemMeta(lm);
        } else {
        /*
            DyeColor dc = parseDyeColor(colorStr);
            if (dc == null) return;
            if(this.getData() instanceof Colorable)
                ((Colorable)this).setColor(dc);
         */
        }
    }

    protected void putEnchants(Map<String, String> params, String key, Map<Enchantment, Integer> enchantments) {
        if (enchantments == null || enchantments.isEmpty())
            return;
        StringBuilder sb = new StringBuilder();
        for (Enchantment e : enchantments.keySet()) {
            if (sb.length() > 0)
                sb.append(";");
            sb.append(e.getKey().getKey()).append(":").append(enchantments.get(e));
        }
        params.put(key, sb.toString());
    }

    public boolean hasDisplayName() {
        if (!this.hasItemMeta()) return false;
        return this.getItemMeta().hasDisplayName();
    }

    public boolean hasLore() {
        if (!this.hasItemMeta()) return false;
        return this.getItemMeta().hasLore();
    }

    public String getDisplayName() {
        if (!this.hasItemMeta()) return null;
        ItemMeta im = this.getItemMeta();
        if (im.hasDisplayName()) return im.getDisplayName();
        return null;
    }

    public List<String> getLore() {
        if (!this.hasItemMeta()) return null;
        ItemMeta im = this.getItemMeta();
        if (im.hasLore()) return im.getLore();
        return null;
    }


    protected void putItemMeta(Map<String, String> params, ItemMeta itemMeta) {
        if (itemMeta == null)
            return;
        if (itemMeta.hasDisplayName())
            put(params, "name", itemMeta.getDisplayName().replace('§', '&'));
        if (itemMeta.hasLore())
            put(params, "lore", itemMeta.getLore());
        if (itemMeta instanceof BookMeta) {
            BookMeta bm = (BookMeta) itemMeta;
            put(params, "book-author", bm.getAuthor().replace('§', '&'));
            put(params, "book-title", bm.getTitle().replace('§', '&'));
            if (!bm.getPages().isEmpty()) {
                List<String> pages = new ArrayList<>();
                for (String page : bm.getPages()) {
                    String newPage = page.replaceAll("§0\n", "&z");
                    newPage = newPage.replace('§', '&');
                    pages.add(newPage);
                }
                put(params, "book-pages", pages);
            }
        }
        if (itemMeta instanceof FireworkMeta) {
            FireworkMeta fm = (FireworkMeta) itemMeta;
            put(params, "firework-power", fm.getPower());
            put(params, "firework-effects", fireworksToList(fm.getEffects()));
        }

        if (itemMeta instanceof LeatherArmorMeta) {
            LeatherArmorMeta lm = (LeatherArmorMeta) itemMeta;
            put(params, "color", colorToString(lm.getColor(), true));
        }
        if (itemMeta instanceof SkullMeta) {
            SkullMeta sm = (SkullMeta) itemMeta;
            if (sm.hasOwner())
                put(params, "skull-owner", sm.getOwningPlayer().getName());
        }
        if (itemMeta instanceof PotionMeta) {
            PotionMeta pm = (PotionMeta) itemMeta;
            if (pm.hasCustomEffects())
                putEffects(params, pm.getCustomEffects());
        }
        if (itemMeta instanceof MapMeta) {
            MapMeta mm = (MapMeta) itemMeta;
            if (mm.isScaling())
                put(params, "map-scale", "true");
        }

        if (itemMeta instanceof EnchantmentStorageMeta) {
            EnchantmentStorageMeta esm = (EnchantmentStorageMeta) itemMeta;
            if (esm.hasStoredEnchants())
                putEnchants(params, "stored-enchants", esm.getStoredEnchants());
        }

        if (itemMeta instanceof FireworkEffectMeta)
            putFireworkEffectMeta(params, (FireworkEffectMeta) itemMeta);
    }

    protected String fireworksToString(FireworkEffect fe) {
        StringBuilder sb = new StringBuilder();
        sb.append("type:").append(fe.getType().name());
        sb.append(" flicker:").append(fe.hasFlicker());
        sb.append(" trail:").append(fe.hasTrail());
        if (!fe.getColors().isEmpty()) {
            sb.append(" colors:");
            for (int i = 0; i < fe.getColors().size(); i++) {
                Color c = fe.getColors().get(i);
                if (i > 0)
                    sb.append(";");
                sb.append(colorToString(c, true));
            }
        }
        if (!fe.getFadeColors().isEmpty()) {
            sb.append(" fade-colors:");
            for (int i = 0; i < fe.getFadeColors().size(); i++) {
                Color c = fe.getColors().get(i);
                if (i > 0) sb.append(";");
                sb.append(colorToString(c, true));
            }
        }
        return sb.toString();
    }

    protected void putFireworkEffectMeta(Map<String, String> params,
                                         FireworkEffectMeta fwm) {
        if (!fwm.hasEffect())
            return;
        put(params, "firework-effects", fireworksToString(fwm.getEffect()));

    }

    protected void putEffects(Map<String, String> params, List<PotionEffect> customEffects) {
        StringBuilder sb = new StringBuilder();
        for (PotionEffect pef : customEffects) {
            if (sb.length() > 0)
                sb.append(";");
            sb.append(pef.getType().getName()).append(":");
            sb.append(pef.getAmplifier()).append(":").append(pef.getDuration());
        }
        put(params, "potion-effects", sb.toString());
    }

    protected static List<Color> parseColors(String colorStr) {
        List<Color> colors = new ArrayList<>();
        String[] clrs = colorStr.split(";");
        for (String cStr : clrs) {
            Color c = parseColor(cStr.trim());
            if (c == null)
                continue;
            colors.add(c);
        }
        return colors;
    }

    /**
     * Parse bukkit colors. Name and RGB values supported
     *
     * @param colorStr - Color name, or RGB values (Example: 10,15,20)
     * @return - Color
     */
    protected static Color parseColor(String colorStr) {
        if (BYTES_RGB.matcher(colorStr).matches()) {
            String[] rgb = colorStr.split(",");
            int red = Integer.parseInt(rgb[0]);
            int green = Integer.parseInt(rgb[1]);
            int blue = Integer.parseInt(rgb[2]);
            return Color.fromRGB(red, green, blue);
        } else if (BYTE.matcher(colorStr).matches()) {
            int num = Integer.parseInt(colorStr);
            if (num > 15)
                num = 15;
            @SuppressWarnings("deprecation")
            DyeColor c = DyeColor.getByDyeData((byte) num);
            return c == null ? null : c.getColor();
        } else {
        	/*
            for (DyeColor dc : DyeColor.values())
                if (dc.name().equalsIgnoreCase(colorStr))
                    return dc.getColor();
        	 */
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    protected static DyeColor parseDyeColor(String colorStr) {
        if (BYTES_RGB.matcher(colorStr).matches()) {
            return getClosestColor(parseColor(colorStr));
        } else if (BYTE.matcher(colorStr).matches()) {
            int num = Integer.parseInt(colorStr);
            if (num > 15) {
                num = 15;
            }
            return DyeColor.getByDyeData((byte) num);
        } else {
            for (DyeColor dc : DyeColor.values()) {
                if (dc.name().equalsIgnoreCase(colorStr)) {
                    return dc;
                }
            }
        }
        return null;
    }

    protected static double getColorDistance(Color c1, Color c2) {
        double rmean = (c1.getRed() + c2.getRed()) / 2.0;
        double r = c1.getRed() - c2.getRed();
        double g = c1.getGreen() - c2.getGreen();
        int b = c1.getBlue() - c2.getBlue();
        double weightR = 2 + rmean / 256.0;
        double weightG = 4.0;
        double weightB = 2 + (255 - rmean) / 256.0;
        return weightR * r * r + weightG * g * g + weightB * b * b;
    }

    protected static DyeColor getClosestColor(Color color) {
        int index = 0;
        double best = -1;
        for (int i = 0; i < DyeColor.values().length; i++) {
            double distance = getColorDistance(color,
                    DyeColor.values()[i].getColor());
            if (distance < best || best == -1) {
                best = distance;
                index = i;
            }
        }
        return DyeColor.values()[index];
    }

    protected String colorToString(Color c, boolean useRGB) {
        for (DyeColor dc : DyeColor.values())
            if (dc.getColor().equals(c))
                return dc.name();
        if (!useRGB)
            getClosestColor(c).name();
        String sb = c.getRed() + "," +
                c.getGreen() + "," +
                c.getBlue();
        return sb;
    }

    public List<String> toStringList() {
        List<String> list = new ArrayList<>();
        Map<String, String> map = toMap();
        for (String key : map.keySet()) {
            String value = map.get(key);
            StringBuilder line = new StringBuilder(key).append(":");
            if (value.contains(" "))
                line.append("{").append(value).append("}");
            else
                line.append(value);
            list.add(line.toString());
        }
        return list;
    }

    @Override
    public String toString() {
        Map<String, String> params = toMap();
        StringBuilder sb = new StringBuilder();
        for (String key : params.keySet()) {
            if (sb.length() > 0)
                sb.append(" ");
            sb.append(key).append(":");
            String value = params.get(key);
            if (value.contains(" "))
                sb.append("{").append(value).append("}");
            else
                sb.append(value);
        }
        return sb.toString();
    }

    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();
        if (this.getItemMeta().hasDisplayName()) sb.append(this.getItemMeta().getDisplayName());
        else {
            sb.append(this.getType().name());
            if (this.getDamage() > 0) sb.append(":").append(this.getDamage());
        }
        if (this.getAmount() > 1) sb.append("*").append(this.getAmount());
        return ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', sb.toString()));
    }

    public String getDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getItemMeta().hasDisplayName() ? this.getItemMeta()
                .getDisplayName() : this.getType().name());
        if (this.getAmount() > 1)
            sb.append("*").append(this.getAmount());
        return ChatColor.stripColor(sb.toString());
    }

    public void setEnchantments(String enchStr) {
        clearEnchantments();
        Map<Enchantment, Integer> enchantments = parseEnchantmentsString(enchStr);
        if (enchantments.isEmpty()) return;
        this.addUnsafeEnchantments(enchantments);
    }

    protected Map<Enchantment, Integer> parseEnchantmentsString(String enchStr) {
        Map<Enchantment, Integer> ench = new HashMap<>();
        if (enchStr == null || enchStr.isEmpty()) return ench;
        String[] ln = enchStr.split(";");
        for (String e : ln) {
            String eType = e;
            int power = 0;
            if (eType.contains(":")) {
                String powerStr = eType.substring(eType.indexOf(":") + 1);
                eType = eType.substring(0, eType.indexOf(":"));
                power = INT_MIN_MAX.matcher(powerStr).matches() ? getNumber(powerStr) : 0;
            }
            Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(eType.toUpperCase()));
            if (enchantment == null)
                continue;
            ench.put(enchantment, power);
        }
        return ench;

    }

    public void clearEnchantments() {
        for (Enchantment e : this.getEnchantments().keySet())
            this.removeEnchantment(e);
    }

    public void setBook(String author, String title, String pagesStr) {
        if (!(this.getItemMeta() instanceof BookMeta))
            return;
        BookMeta bm = (BookMeta) this.getItemMeta();
        if (pagesStr != null) {
            String[] ln = pagesStr.split(Pattern.quote(DIVIDER));
            List<String> pages = new ArrayList<>();
            for (String page : ln)
                pages.add(ChatColor.translateAlternateColorCodes('&',
                        page.replace("&z", "§0\n")));
            bm.setPages(pages);
        }
        if (author != null && !author.isEmpty())
            bm.setAuthor(ChatColor.translateAlternateColorCodes('&', author));
        if (title != null && !title.isEmpty())
            bm.setTitle(ChatColor.translateAlternateColorCodes('&', title));
        this.setItemMeta(bm);
    }

    public void setLore(String loreStr) {
        List<String> lore = new ArrayList<>();
        if (loreStr != null) {
            String[] ln = ChatColor.translateAlternateColorCodes('&', loreStr).split(Pattern.quote(DIVIDER));
            lore = Arrays.asList(ln);
        } // else this.lore = null;
        ItemMeta im = this.getItemMeta();
        im.setLore(lore);
        this.setItemMeta(im);
    }

    public void setName(String name) {
        if (name == null) return;
        ItemMeta im = this.getItemMeta();
        im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        this.setItemMeta(im);
    }


    protected int parseTimeTicks(String time) {
        int hh = 0; // часы
        int mm = 0; // минуты
        int ss = 0; // секунды
        int tt = 0; // тики
        int ms = 0; // миллисекунды
        if (INT.matcher(time).matches()) {
            tt = Integer.parseInt(time);
        } else if (TIME_HH_MM.matcher(time).matches()) {
            String[] ln = time.split(":");
            mm = Integer.parseInt(ln[0]);
            ss = Integer.parseInt(ln[1]);
        } else if (TIME_HH_MM_SS.matcher(time).matches()) {
            String[] ln = time.split(":");
            hh = Integer.parseInt(ln[0]);
            mm = Integer.parseInt(ln[1]);
            ss = Integer.parseInt(ln[2]);
        } else if (time.endsWith("ms")) {
            String s = time.replace("ms", "");
            if (INT.matcher(s).matches())
                ms = Integer.parseInt(s);
        } else if (time.endsWith("h")) {
            String s = time.replace("h", "");
            if (INT.matcher(s).matches())
                hh = Integer.parseInt(s);
        } else if (time.endsWith("m")) {
            String s = time.replace("m", "");
            if (INT.matcher(s).matches())
                mm = Integer.parseInt(s);
        } else if (time.endsWith("s")) {
            String s = time.replace("s", "");
            if (INT.matcher(s).matches())
                ss = Integer.parseInt(s);
        } else if (time.endsWith("t")) {
            String s = time.replace("t", "");
            if (INT.matcher(s).matches())
                tt = Integer.parseInt(s);
        } else
            return Integer.MAX_VALUE;
        return Math.max(1, ((hh * 3600000) + (mm * 60000) + (ss * 1000)
                + (tt * 50) + ms) / 50);
    }

    /**
     * Parse one-string parameter and create a map based on params(keys) and
     * values.
     *
     * @param param - String. Example: param1:{value1} param2:{value2}
     *              paramN:{valueN}
     * @return - parsed Map
     */
    protected static Map<String, String> parseParams(String param) {
        Map<String, String> params = new HashMap<>();

        Matcher matcher = PARAM_PATTERN.matcher(hideBkts(param));
        while (matcher.find()) {
            String paramPart = matcher.group().trim().replace("#BKT1#", "{").replace("#BKT2#", "}");
            String key;
            String value;
            if (paramPart.contains(":")) {
                key = paramPart.substring(0, paramPart.indexOf(":"));
                value = paramPart.substring(paramPart.indexOf(":") + 1);
            } else {
                key = "default-param"; // это для упрощенного формата
                value = paramPart;
            }

            if (key.isEmpty()) continue;
            if (PARAM_BRACKET.matcher(value).matches()) value = value.substring(1, value.length() - 1);
            params.put(key, value);
        }
        return params;
    }

    protected static String hideBkts(String s) {
        int count = 0;
        StringBuilder r = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            String a = String.valueOf(c);
            if (c == '{') {
                count++;
                if (count != 1)
                    a = "#BKT1#";
            } else if (c == '}') {
                if (count != 1)
                    a = "#BKT2#";
                count--;
            }
            r.append(a);
        }
        return r.toString();
    }

    protected static String getParam(Map<String, String> params, String key,
                                     String defaultValue) {
        if (!params.containsKey(key))
            return defaultValue;
        return params.get(key);
    }

    /**
     * Get value of parameter. If parameter is not exist, null value will
     * returned
     *
     * @param params - Map contained parameter (key) and it's values
     * @param key    - Parameter id to get value
     * @return - Value of parameter
     */
    protected static String getParam(Map<String, String> params, String key) {
        if (!params.containsKey(key)) return null;
        return params.get(key);
    }

    protected void setFireworkEffect(String fireworkStr) {
        if (fireworkStr == null || fireworkStr.isEmpty()) return;
        if (!(this.getItemMeta() instanceof FireworkEffectMeta)) return;
        FireworkEffectMeta fm = (FireworkEffectMeta) this.getItemMeta();
        Map<String, String> params = parseParams(fireworkStr);
        FireworkEffect.Type fType;
        List<Color> colors;
        List<Color> fadeColors;
        boolean flicker;
        boolean trail;
        fType = FireworkEffect.Type.valueOf(getParam(params, "type", "")
                .toUpperCase());
        flicker = "true".equalsIgnoreCase(getParam(params, "flicker", "false"));
        trail = "true".equalsIgnoreCase(getParam(params, "trail", "false"));
        colors = parseColors(getParam(params, "colors", ""));
        fadeColors = parseColors(getParam(params, "fade-colors", ""));
        Builder b = FireworkEffect.builder().with(fType);
        if (flicker)
            b = b.withFlicker();
        if (trail)
            b = b.withTrail();
        for (Color c : colors)
            b = b.withColor(c);
        for (Color c : fadeColors)
            b = b.withFade(c);
        fm.setEffect(b.build());
        this.setItemMeta(fm);
    }

    protected void setFireworks(int power, String fireworkStr) {
        if (!(this.getItemMeta() instanceof FireworkMeta))
            return;
        FireworkMeta fm = (FireworkMeta) this.getItemMeta();
        fm.clearEffects();
        fm.setPower(power);
        if (fireworkStr != null && !fireworkStr.isEmpty()) {
            String[] fireworks = fireworkStr.split(";");
            List<FireworkEffect> fe = new ArrayList<>();
            for (String fStr : fireworks) {
                Map<String, String> params = parseParams(fStr);
                FireworkEffect.Type fType = null;
                List<Color> colors;
                List<Color> fadeColors;
                boolean flicker;
                boolean trail;
                for (FireworkEffect.Type ft : FireworkEffect.Type.values()) {
                    if (ft.name()
                            .equalsIgnoreCase(getParam(params, "type", "")))
                        fType = ft;
                }
                flicker = "true".equalsIgnoreCase(getParam(params, "flicker",
                        "false"));
                trail = "true".equalsIgnoreCase(getParam(params, "trail",
                        "false"));
                colors = parseColors(getParam(params, "colors", ""));
                fadeColors = parseColors(getParam(params, "fade-colors", ""));
                if (fType == null)
                    continue;
                Builder b = FireworkEffect.builder().with(fType);
                if (flicker)
                    b = b.withFlicker();
                if (trail)
                    b = b.withTrail();
                for (Color c : colors)
                    b = b.withColor(c);
                for (Color c : fadeColors)
                    b = b.withFade(c);
                fe.add(b.build());
            }
            if (!fe.isEmpty())
                fm.addEffects(fe);
        }
        this.setItemMeta(fm);
    }

    protected List<String> fireworksToList(List<FireworkEffect> fireworks) {
        if (fireworks == null || fireworks.isEmpty())
            return null;
        List<String> fireList = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < fireworks.size(); j++) {
            // if (j>0) sb.append("\\\\n");
            if (j > 0)
                sb.append(";");
            FireworkEffect fe = fireworks.get(j);
            sb.append(fireworksToString(fe));
        }
        fireList.add(sb.toString());
        return fireList;
    }

    protected void put(Map<String, String> params, String key, int value) {
        params.put(key, Integer.toString(value));
    }

    protected void put(Map<String, String> params, String key, String value) {
        if (value == null)
            return;
        if (value.isEmpty())
            return;
        params.put(key, value);
    }

    private String listToString(List<String> valueList) {
        if (valueList == null) return null;
        if (valueList.isEmpty()) return null;
        StringBuilder sb = new StringBuilder(valueList.get(0));
        if (valueList.size() > 1)
            for (int i = 1; i < valueList.size(); i++)
                sb.append(DIVIDER).append(valueList.get(i));
        return sb.toString();
    }

    protected void put(Map<String, String> params, String key, List<String> valueList) {
        String str = listToString(valueList);
        if (str == null) return;
        params.put(key, str.replace('§', '&'));
    }

    protected static int getNumber(String numMinMaxStr) {
        if (INT.matcher(numMinMaxStr).matches())
            return Integer.parseInt(numMinMaxStr);
        int min = 0;
        int max;
        String strMin = numMinMaxStr;
        String strMax = numMinMaxStr;
        if (numMinMaxStr.contains("-")) {
            strMin = numMinMaxStr.substring(0, numMinMaxStr.indexOf("-"));
            strMax = numMinMaxStr.substring(numMinMaxStr.indexOf("-") + 1);
        }
        if (INT.matcher(strMin).matches())
            min = Integer.parseInt(strMin);
        if (!ALLOW_RANDOM)
            return min;
        max = min;
        if (INT.matcher(strMax).matches())
            max = Integer.parseInt(strMax);
        if (max > min)
            return min + random.nextInt(1 + max - min);
        else
            return min;
    }

    /**
     * Old format algorithm. Implemented for compatibility.
     *
     * @param itemStr - old item format
     * @return - ItemStack
     */
    protected static ItemStack parseOldItemStack(String itemStr) {
        if (!TRY_OLD_ITEM_PARSE)
            return null;
        if (itemStr.isEmpty())
            return null;
        String iStr = itemStr;
        String enchant = "";
        String name = "";
        String loreStr = "";
        if (iStr.contains("$")) {
            name = iStr.substring(0, iStr.indexOf("$"));
            iStr = iStr.substring(name.length() + 1);
            if (name.contains("@")) {
                loreStr = name.substring(name.indexOf("@") + 1);
                name = name.substring(0, name.indexOf("@"));
            }

        }
        if (iStr.contains("@")) {
            enchant = iStr.substring(iStr.indexOf("@") + 1);
            iStr = iStr.substring(0, iStr.indexOf("@"));
        }
        Material id;
        int amount = 1;
        short data = 0;
        String[] si = iStr.split("\\*");
        if (si.length > 0) {
            if (si.length == 2)
                amount = Math.max(getNumber(si[1]), 1);
            String[] ti = si[0].split(":");
            if (ti.length > 0) {
                Material m = Material.getMaterial(ti[0].toUpperCase());
                if (m == null)
                    return null;
                id = m;
                if ((ti.length == 2) && (INT.matcher(ti[1]).matches()))
                    data = Short.parseShort(ti[1]);
                ItemStack item = new ItemStack(id, amount);
                ItemUtil.setDurability(item, data);
                if (!enchant.isEmpty()) {

                    String[] ln = enchant.split(",");
                    for (String ec : ln) {
                        if (ec.isEmpty())
                            continue;

                        Color clr = parseColor(ec);
                        if (clr != null) {
                            if (item.hasItemMeta()
                                    && (item.getItemMeta() instanceof LeatherArmorMeta)) {
                                LeatherArmorMeta meta = (LeatherArmorMeta) item
                                        .getItemMeta();
                                meta.setColor(clr);
                                item.setItemMeta(meta);
                            }
                        } else {
                            String ench = ec;
                            int level = 1;
                            if (ec.contains(":")) {
                                ench = ec.substring(0, ec.indexOf(":"));
                                level = Math.max(1, getNumber(ec.substring(ench
                                        .length() + 1)));
                            }
                            Enchantment e = Enchantment.getByKey(NamespacedKey.minecraft(ench.toUpperCase()));
                            if (e == null)
                                continue;
                            item.addUnsafeEnchantment(e, level);
                        }
                    }
                }
                if (!name.isEmpty()) {
                    ItemMeta im = item.getItemMeta();
                    im.setDisplayName(ChatColor.translateAlternateColorCodes(
                            '&', name.replace("_", " ")));
                    item.setItemMeta(im);
                }
                if (!loreStr.isEmpty()) {
                    ItemMeta im = item.getItemMeta();
                    String[] ln = loreStr.split("@");
                    List<String> lore = new ArrayList<>();
                    for (String loreLine : ln)
                        lore.add(loreLine.replace("_", " "));
                    im.setLore(lore);
                    item.setItemMeta(im);
                }
                return item;
            }
        }
        return null;
    }

    public boolean compare(ItemStack item, int amount) {
        int amountToRemove = amount > 0 ? amount : item.getAmount();
        if (this.getAmount() < amountToRemove)
            return false; // Сравниваем ТЕКУЩИЙ предмет с целевым. Т.е. текущего должно быть столько же (или больше чем) того с которым сравниваем.
        if (this.getType() != item.getType()) return false;
        if (this.getDamage() != ItemUtil.getDurability(item)) return false;
        return Bukkit.getItemFactory().equals(this.getItemMeta(), item.getItemMeta());
    }

    public boolean compare(String itemStr) {
        return compare(itemStr, -1);
    }

    public boolean compare(String itemStr, int amount) {
        Map<String, String> params = parseParams(itemStr);
        if (amount > 0) params.put("amount", Integer.toString(amount));
        return compare(params, amount);
    }

    public boolean compare(Map<String, String> itemMap) {
        return compare(itemMap, -1);
    }

    public boolean compare(Map<String, String> itemMap, int amount) {
		if (itemMap == null || itemMap.isEmpty()) return false;
		
        boolean regex = !itemMap.containsKey("regex") || itemMap.get("regex").equalsIgnoreCase("true");

        ItemMeta thisMeta = this.getItemMeta();
        if (itemMap.containsKey("item") || itemMap.containsKey("default-param")) {
            String itemStr = itemMap.containsKey("item") ? itemMap.get("item") : itemMap.get("default-param");
            String dataStr = "";
            String amountStr = "";
            if (itemStr.contains("*")) {
                itemStr = itemStr.substring(0, itemStr.indexOf("*"));
                amountStr = itemStr.substring(itemStr.indexOf("*") + 1);
            }
            if (itemStr.contains(":")) {
                itemStr = itemStr.substring(0, itemStr.indexOf(":"));
                dataStr = itemStr.substring(itemStr.indexOf(":") + 1);
            }
            itemMap.put("type", Material.getMaterial(itemStr.toUpperCase()).name());

            if (INT.matcher(dataStr).matches()) itemMap.put("data", dataStr);
            if (INT.matcher(amountStr).matches()) itemMap.put("amount", amountStr);
			itemMap.remove("item");
			itemMap.remove("default-param");
        }
        if (amount > 0) itemMap.put("amount", Integer.toString(amount));
        if (this.hasDisplayName() && !itemMap.containsKey("name")) return false;
        if (this.hasLore() && !itemMap.containsKey("lore")) return false;
        if (itemMap.containsKey("type")) {
            String typeStr = itemMap.get("type").toUpperCase();
            Material m = null;
            try {
                m = Material.getMaterial(typeStr);
            } catch (Exception ignored) {
            }
            if (m == null) return false;
            typeStr = m.name();
            if (!compareOrMatch(this.getType().name(), typeStr.toUpperCase(), regex)) return false;

            if (itemMap.containsKey("color")) {
            	/*
                DyeColor dyeColor = parseDyeColor(itemMap.get("color"));
                if(this.getItemMeta() instanceof Colorable)
                	itemMap.put("data", String.valueOf(dyeColor.getWoolData()));

            	*/
            }
        }

        if (itemMap.containsKey("data")) {
            String dataStr = itemMap.get("data");
            int reqData = INT.matcher(dataStr).matches() ? Integer.parseInt(dataStr) : -1;
            if (reqData != this.getDamage()) return false;
        }
        if (itemMap.containsKey("amount")) {
            String amountStr = itemMap.get("amount");
            if (INT.matcher(amountStr).matches() && this.getAmount() < Integer.parseInt(amountStr))
                return false;//this.getAmount()>=Integer.parseInt(amountStr);
            else if (AMOUNT_RANDOM.matcher(amountStr).matches()) {
                boolean greater = amountStr.startsWith(">");
                boolean equal = amountStr.contains("=");
                int reqAmount = Integer.parseInt(amountStr.replaceAll("\\D+", ""));
                reqAmount = equal ? (greater ? reqAmount++ : reqAmount--) : reqAmount;
                if (greater && this.getAmount() < reqAmount) return false;
                if (!greater && this.getAmount() > reqAmount) return false;
            }
        }
        if (itemMap.containsKey("name")) {
            String thisName = thisMeta.hasDisplayName() ? thisMeta.getDisplayName() : "";
            if (!compareOrMatch(thisName, ChatColor.translateAlternateColorCodes('&', itemMap.get("name")), regex))
                return false;
        }

        if (itemMap.containsKey("lore")) {
            List<String> thisLore = thisMeta.hasLore() ? thisMeta.getLore() : new ArrayList<>();
            String thisLoreStr = Joiner.on(DIVIDER).join(thisLore);
            String loreStr = ChatColor.translateAlternateColorCodes('&', itemMap.get("lore")); //Joiner.on(regex ? Pattern.quote(DIVIDER) : DIVIDER).join(thisLore);
            return compareOrMatch(thisLoreStr, loreStr, regex);
        }
        return true;
    }

    private boolean compareOrMatch(String str, String toStr, boolean useRegex) {
        if (useRegex) {
            try {
                return str.matches(toStr);
            } catch (Exception e) {
                Msg.logOnce(toStr + "0", "Failed to check items matches:");
                Msg.logOnce(toStr + "1", "Item 1: " + str);
                Msg.logOnce(toStr + "2", "Item 2: " + toStr);
                return false;
            }
        }
        return str.equalsIgnoreCase(toStr);
    }

    /**
     * Actually {@link #getDurability()} but for 1.13+
     *
     * @return Durability of item
     */
    public int getDamage() {
        ItemMeta meta = this.getItemMeta();
        if(meta instanceof Damageable) {
            return ((Damageable)meta).getDamage();
        }
        return 0;
    }
    
    /**
     * Actually {@link #setDurability(short)} but for 1.13+
     */
    public void setDamage(int damage) {
        ItemMeta meta = this.getItemMeta();
        if(meta instanceof Damageable) {
            ((Damageable)meta).setDamage(damage);
            this.setItemMeta(meta);
        }
    }
}

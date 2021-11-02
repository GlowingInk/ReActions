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

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import me.fromgate.reactions.util.TimeUtils;
import me.fromgate.reactions.util.Utils;
import me.fromgate.reactions.util.math.NumberUtils;
import me.fromgate.reactions.util.math.Rng;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.Material;
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
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

// TODO: Recode from scratch; don't extend ItemStack
public class VirtualItem extends ItemStack {

    private static final String DIVIDER = "\\n";
    private static final Pattern AMOUNT_RANDOM = Pattern.compile("<\\d+|>\\d+|<=\\d+|>=\\d+");

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
     * @param type   Item material
     * @param damage Durability
     * @param amount Amount
     */
    public VirtualItem(Material type, int damage, int amount) {
        super(type);
        this.setDamage(damage);
        this.setAmount(amount);
    }

    /**
     * Constructor Create new VirtualItem object based on ItemStack
     *
     * @param item Base ItemStack
     */
    public VirtualItem(ItemStack item) {
        super(item);
    }

    public static VirtualItem fromItemStack(ItemStack item) {
        if (!ItemUtils.isExist(item))
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
        Map<String, String> params = Parameters.parametersMap(itemStr);
        VirtualItem vi = fromMap(params);
        if (vi != null) return vi;
        ItemStack item = ItemUtils.parseOldItemStack(itemStr);
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
            type = Material.getMaterial(itemStr.toUpperCase(Locale.ENGLISH), false);
            if (type == null)
                type = Material.getMaterial(itemStr.toUpperCase(Locale.ENGLISH), true);
            amount = Rng.nextIntRanged(amountStr);
            if (amount == 0) return null;
        } else if (params.containsKey("type")) {
            String typeStr = params.getOrDefault("type", "");
            type = Material.getMaterial(typeStr.toUpperCase(Locale.ENGLISH));
        } else
            return null;
        if (type == null)
            return null;
        data = Rng.nextIntRanged(params.getOrDefault("data", "0"));
        amount = Rng.nextIntRanged(params.getOrDefault("amount", "1"));
        VirtualItem vi = new VirtualItem(type, data, amount);

        vi.setName(params.get("name"));
        vi.setLore(params.get("lore"));
        vi.setEnchantments(params.get("enchantments"));
        vi.setBook(params.get("book-author"), params.get("book-title"), params.get("book-pages"));
        vi.setFireworks(Rng.nextIntRanged(params.getOrDefault("firework-power", "0")), params.get("firework-effects"));
        vi.setColor(params.get("color"));
        vi.setSkull(params.get("skull-owner"));
        vi.setPotionMeta(params.get("potion-effects"));
        vi.setMap(params.getOrDefault("map-scale", "false").equalsIgnoreCase("true"));
        vi.setEnchantStorage(params.get("stored-enchants"));
        vi.setFireworkEffect(params.get("firework-effects"));
        return vi;
    }

    /**
     * Deserialize item from JSON-string
     *
     * @param itemJSON JSON-string with item parameters
     * @return VirtualItem generated from JSON-string
     */
    public static VirtualItem fromJSONString(String itemJSON) {
        if (Utils.isStringEmpty(itemJSON))
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
        return new VirtualItem(item);
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
        // private static boolean ALLOW_RANDOM = true;
        params.put("regex", "false");
        return params;
    }

    /**
     * Serialize item to JSON-string
     *
     * @return JSON-string generated from ItemStack
     */
    @SuppressWarnings({"unchecked"})
    public String toJSON() {
        Map<String, Object> itemS = this.serialize();
        JSONObject json = new JSONObject();
        for (String i : itemS.keySet())
            json.put(i, itemS.get(i));
        return json.toJSONString();
    }

    public void giveItem(Player player) {
        for (ItemStack i : player.getInventory().addItem(this.clone()).values())
            player.getWorld().dropItemNaturally(player.getLocation(), i);
    }

    public void setEnchantStorage(String enchStr) {
        if (Utils.isStringEmpty(enchStr)) return;
        if (!(this.getItemMeta() instanceof EnchantmentStorageMeta esm)) return;
        String[] enchLn = enchStr.split(";");
        for (String e : enchLn) {
            String eType = e;
            int power = 0;
            if (eType.contains(":")) {
                String powerStr = eType.substring(eType.indexOf(":") + 1);
                eType = eType.substring(0, eType.indexOf(":"));
                power = NumberUtils.INT_POSITIVE.matcher(powerStr).matches() ? Integer.parseInt(powerStr) : 0;
            }
            Enchantment enchantment = ItemUtils.getEnchantmentByName(eType);
            if (enchantment == null) continue;
            esm.addStoredEnchant(enchantment, power, true);
        }
        this.setItemMeta(esm);
    }

    public void setMap(boolean scale) {
        if (this.getItemMeta() instanceof MapMeta mm) {
            mm.setScaling(scale);
            this.setItemMeta(mm);
        }
    }


    public void setPotionMeta(String potions) {
        if (Utils.isStringEmpty(potions))
            return;
        if (!(this.getItemMeta() instanceof PotionMeta pm))
            return;
        String[] potLn = potions.split(";");
        pm.clearCustomEffects();
        for (String pStr : potLn) {
            String[] ln = pStr.trim().split(":");
            if (ln.length == 0)
                continue;
            PotionEffectType pType = PotionEffectType.getByName(ln[0]
                    .toUpperCase(Locale.ENGLISH));
            if (pType == null)
                continue;
            int amplifier = (ln.length > 1) ? Rng.nextIntRanged(ln[1]) : 0;
            int duration = (ln.length > 2) ? (int) (TimeUtils.parseTime(ln[2]) / 50) : Integer.MAX_VALUE;
            pm.addCustomEffect(new PotionEffect(pType, duration, amplifier, true), true);
        }
        this.setItemMeta(pm);
    }

    @SuppressWarnings("deprecation")
    private void setSkull(String owner) {
        if (Utils.isStringEmpty(owner))
            return;
        if (this.getItemMeta() instanceof SkullMeta sm) {
            sm.setOwner(owner);
            this.setItemMeta(sm);
        }
    }

    /**
     * Configure leather armor color
     *
     * @param colorStr
     */
    private void setColor(String colorStr) {
        if (Utils.isStringEmpty(colorStr)) return;

        if (this.getItemMeta() instanceof LeatherArmorMeta lm) {
            Color c = ItemUtils.parseColor(colorStr);
            if (c == null) return;
            lm.setColor(c);
            this.setItemMeta(lm);
        }
    }

    private void putEnchants(Map<String, String> params, String key, Map<Enchantment, Integer> enchantments) {
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
        return this.hasItemMeta() && this.getItemMeta().hasDisplayName();
    }

    public boolean hasLore() {
        return this.hasItemMeta() && this.getItemMeta().hasLore();
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

    private void putItemMeta(Map<String, String> params, ItemMeta itemMeta) {
        if (itemMeta == null)
            return;
        if (itemMeta.hasDisplayName())
            put(params, "name", itemMeta.getDisplayName().replace('§', '&'));
        if (itemMeta.hasLore())
            put(params, "lore", itemMeta.getLore());
        if (itemMeta instanceof BookMeta bm) {
            if (bm.hasAuthor())
                put(params, "book-author", bm.getAuthor().replace('§', '&'));
            if (bm.hasTitle())
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
        if (itemMeta instanceof FireworkMeta fm) {
            put(params, "firework-power", fm.getPower());
            put(params, "firework-effects", fireworksToList(fm.getEffects()));
        }

        if (itemMeta instanceof LeatherArmorMeta lm) {
            put(params, "color", ItemUtils.colorToString(lm.getColor(), true));
        }
        if (itemMeta instanceof SkullMeta sm) {
            if (sm.hasOwner())
                put(params, "skull-owner", sm.getOwningPlayer().getName());
        }
        if (itemMeta instanceof PotionMeta pm) {
            if (pm.hasCustomEffects())
                putEffects(params, pm.getCustomEffects());
        }
        if (itemMeta instanceof MapMeta mm) {
            if (mm.isScaling())
                put(params, "map-scale", "true");
        }

        if (itemMeta instanceof EnchantmentStorageMeta esm) {
            if (esm.hasStoredEnchants())
                putEnchants(params, "stored-enchants", esm.getStoredEnchants());
        }

        if (itemMeta instanceof FireworkEffectMeta)
            putFireworkEffectMeta(params, (FireworkEffectMeta) itemMeta);
    }

    private void putFireworkEffectMeta(Map<String, String> params, FireworkEffectMeta fwm) {
        if (fwm.hasEffect())
            put(params, "firework-effects", ItemUtils.fireworksToString(fwm.getEffect()));
    }

    private void putEffects(Map<String, String> params, List<PotionEffect> customEffects) {
        StringBuilder sb = new StringBuilder();
        for (PotionEffect pef : customEffects) {
            if (sb.length() > 0)
                sb.append(";");
            sb.append(pef.getType().getName()).append(":");
            sb.append(pef.getAmplifier()).append(":").append(pef.getDuration());
        }
        put(params, "potion-effects", sb.toString());
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
        ItemMeta meta = getItemMeta();
        if (meta.hasDisplayName()) sb.append(meta.getDisplayName());
        else {
            sb.append(getType().name());
            if (getDamage() > 0) sb.append(":").append(getDamage());
        }
        if (getAmount() > 1) sb.append("*").append(getAmount());
        return ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', sb.toString()));
    }

    public String getDescription() {
        StringBuilder sb = new StringBuilder();
        ItemMeta meta = this.getItemMeta();
        sb.append(meta.hasDisplayName() ? meta : getType().name());
        if (this.getAmount() > 1)
            sb.append("*").append(this.getAmount());
        return ChatColor.stripColor(sb.toString());
    }

    private void setEnchantments(String enchStr) {
        clearEnchantments();
        Object2IntMap<Enchantment> enchantments = ItemUtils.parseEnchantmentsString(enchStr);
        if (enchantments.isEmpty()) return;
        this.addUnsafeEnchantments(enchantments);
    }

    public void clearEnchantments() {
        for (Enchantment e : this.getEnchantments().keySet())
            this.removeEnchantment(e);
    }

    public void setBook(String author, String title, String pagesStr) {
        ItemMeta meta = this.getItemMeta();
        if (!(meta instanceof BookMeta bm))
            return;
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

    public void setName(String name) {
        if (name == null) return;
        ItemMeta im = this.getItemMeta();
        im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        this.setItemMeta(im);
    }

    private void setFireworkEffect(String fireworkStr) {
        if (fireworkStr == null || fireworkStr.isEmpty()) return;
        if (!(this.getItemMeta() instanceof FireworkEffectMeta fm)) return;
        Map<String, String> params = Parameters.parametersMap(fireworkStr);
        FireworkEffect.Type fType;
        List<Color> colors;
        List<Color> fadeColors;
        boolean flicker;
        boolean trail;
        fType = FireworkEffect.Type.valueOf(params.getOrDefault("type", "")
                .toUpperCase(Locale.ENGLISH));
        flicker = "true".equalsIgnoreCase(params.getOrDefault("flicker", "false"));
        trail = "true".equalsIgnoreCase(params.getOrDefault("trail", "false"));
        colors = ItemUtils.parseColors(params.getOrDefault("colors", ""));
        fadeColors = ItemUtils.parseColors(params.getOrDefault("fade-colors", ""));
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

    private void setFireworks(int power, String fireworkStr) {
        if (!(this.getItemMeta() instanceof FireworkMeta fm))
            return;
        fm.clearEffects();
        fm.setPower(power);
        if (fireworkStr != null && !fireworkStr.isEmpty()) {
            String[] fireworks = fireworkStr.split(";");
            List<FireworkEffect> fe = new ArrayList<>();
            for (String fStr : fireworks) {
                Map<String, String> params = Parameters.parametersMap(fStr);
                FireworkEffect.Type fType = null;
                List<Color> colors;
                List<Color> fadeColors;
                boolean flicker;
                boolean trail;
                for (FireworkEffect.Type ft : FireworkEffect.Type.values()) {
                    if (ft.name().equalsIgnoreCase(params.getOrDefault("type", "")))
                        fType = ft;
                }
                flicker = "true".equalsIgnoreCase(params.getOrDefault("flicker",
                        "false"));
                trail = "true".equalsIgnoreCase(params.getOrDefault("trail",
                        "false"));
                colors = ItemUtils.parseColors(params.getOrDefault("colors", ""));
                fadeColors = ItemUtils.parseColors(params.getOrDefault("fade-colors", ""));
                if (fType == null)
                    continue;
                FireworkEffect.Builder b = FireworkEffect.builder().with(fType);
                if (flicker)
                    b.withFlicker();
                if (trail)
                    b.withTrail();
                for (Color c : colors)
                    b.withColor(c);
                for (Color c : fadeColors)
                    b.withFade(c);
                fe.add(b.build());
            }
            if (!fe.isEmpty())
                fm.addEffects(fe);
        }
        this.setItemMeta(fm);
    }

    private List<String> fireworksToList(List<FireworkEffect> fireworks) {
        if (fireworks == null || fireworks.isEmpty())
            return null;
        List<String> fireList = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < fireworks.size(); j++) {
            // if (j>0) sb.append("\\\\n");
            if (j > 0)
                sb.append(";");
            FireworkEffect fe = fireworks.get(j);
            sb.append(ItemUtils.fireworksToString(fe));
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

    public boolean compare(ItemStack item, int amount) {
        int amountToRemove = amount > 0 ? amount : item.getAmount();
        if (this.getAmount() < amountToRemove)
            return false; // Сравниваем ТЕКУЩИЙ предмет с целевым. Т.е. текущего должно быть столько же или больше
        if (this.getType() != item.getType()) return false;
        if (this.getDamage() != ItemUtils.getDurability(item)) return false;
        return Bukkit.getItemFactory().equals(this.getItemMeta(), item.getItemMeta());
    }

    public boolean compare(String itemStr) {
        return compare(itemStr, -1);
    }

    public boolean compare(String itemStr, int amount) {
        Map<String, String> params = Parameters.parametersMap(itemStr);
        if (amount > 0) params.put("amount", Integer.toString(amount));
        return compare(params, amount);
    }

    @SuppressWarnings("unused")
    public boolean compare(Map<String, String> itemMap) {
        return compare(itemMap, -1);
    }

    public boolean compare(Map<String, String> itemMap, int amount) {
        if (itemMap == null || itemMap.isEmpty()) return false;

        boolean regex = !itemMap.containsKey("regex") || itemMap.get("regex").equalsIgnoreCase("true");

        if (itemMap.containsKey("type")) {
            String typeStr = itemMap.get("type").toUpperCase(Locale.ENGLISH);
            Material m = Material.getMaterial(typeStr);
            if (m == null) return false;
            typeStr = m.toString();
            if (!compareOrMatch(this.getType().toString(), typeStr, regex)) return false;
        }
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
            itemMap.put("type", Material.getMaterial(itemStr.toUpperCase(Locale.ENGLISH)).name());

            if (NumberUtils.INT_POSITIVE.matcher(dataStr).matches()) itemMap.put("data", dataStr);
            if (NumberUtils.INT_POSITIVE.matcher(amountStr).matches()) itemMap.put("amount", amountStr);
            itemMap.remove("item");
            itemMap.remove("default-param");
        }
        if (amount > 0) itemMap.put("amount", Integer.toString(amount));
        if (this.hasDisplayName() && !itemMap.containsKey("name")) return false;
        if (this.hasLore() && !itemMap.containsKey("lore")) return false;

        if (itemMap.containsKey("data")) {
            String dataStr = itemMap.get("data");
            int reqData = NumberUtils.INT_POSITIVE.matcher(dataStr).matches() ? Integer.parseInt(dataStr) : -1;
            if (reqData != this.getDamage()) return false;
        }
        if (itemMap.containsKey("amount")) {
            String amountStr = itemMap.get("amount");
            if (NumberUtils.INT_POSITIVE.matcher(amountStr).matches() && this.getAmount() < Integer.parseInt(amountStr))
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
            String thisLoreStr = String.join(DIVIDER, thisLore);
            String loreStr = ChatColor.translateAlternateColorCodes('&', itemMap.get("lore")); //Joiner.on(regex ? Pattern.quote(DIVIDER) : DIVIDER).join(thisLore);
            return compareOrMatch(thisLoreStr, loreStr, regex);
        }
        return true;
    }

    private boolean compareOrMatch(String str, String toStr, boolean useRegex) {
        if (useRegex) {
            //try {
            return str.matches(toStr);
            //} catch (Exception e) {
            //	Msg.logOnce(toStr + "0", "Failed to check items matches:");
            //	Msg.logOnce(toStr + "1", "Item 1: " + str);
            //	Msg.logOnce(toStr + "2", "Item 2: " + toStr);
            //	return false;
            //}
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
        if (meta instanceof Damageable) {
            return ((Damageable) meta).getDamage();
        }
        return 0;
    }

    /**
     * Actually {@link #setDurability(short)} but for 1.13+
     */
    public void setDamage(int damage) {
        ItemMeta meta = this.getItemMeta();
        if (meta instanceof Damageable) {
            ((Damageable) meta).setDamage(damage);
            this.setItemMeta(meta);
        }
    }
}

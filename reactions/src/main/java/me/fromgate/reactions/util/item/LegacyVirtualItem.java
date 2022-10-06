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
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import me.fromgate.reactions.util.NumberUtils;
import me.fromgate.reactions.util.Rng;
import me.fromgate.reactions.util.TimeUtils;
import me.fromgate.reactions.util.Utils;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@Deprecated
public class LegacyVirtualItem extends ItemStack {

    private static final Pattern BYTES_RGB = Pattern.compile("^\\d{1,3},\\d{1,3},\\d{1,3}$");

    private static final String DIVIDER = "\\n";

    /**
     * Constructor Create new VirtualItem object
     *
     * @param type - Item type
     */
    public LegacyVirtualItem(Material type) {
        super(type);
    }

    /**
     * Constructor Create new VirtualItem object
     *
     * @param type   Item material
     * @param damage Durability
     * @param amount Amount
     */
    public LegacyVirtualItem(Material type, int damage, int amount) {
        super(type);
        this.setDamage(damage);
        this.setAmount(amount);
    }

    /**
     * Constructor Create new VirtualItem object based on ItemStack
     *
     * @param item Base ItemStack
     */
    public LegacyVirtualItem(ItemStack item) {
        super(item);
    }

    public static LegacyVirtualItem fromItemStack(ItemStack item) {
        if (!ItemUtils.isExist(item))
            return null;
        return new LegacyVirtualItem(item);
    }

    /**
     * Create VirtualItem object based on parameter-string
     *
     * @param itemStr - String. Format: type:<Type> data:<Data> amount:<Amount> [AnotherParameters]
     *                item:<Type>:<Data>*<Amount> [AnotherParameters]
     * @return - New VirtualItem object or null (if parse failed)
     */
    public static LegacyVirtualItem fromString(String itemStr) {
        Map<String, String> params = Parameters.fromString(itemStr).getMap();
        LegacyVirtualItem vi = fromMap(params);
        if (vi != null) return vi;
        ItemStack item = parseOldItemStack(itemStr);
        if (item != null) return new LegacyVirtualItem(item);
        return null;
    }

    /**
     * Create VirtualItem object (deserialize from Map)
     *
     * @param params - Map of parameters and values
     * @return - VirtualItem object
     */

    public static LegacyVirtualItem fromMap(Map<String, String> params) {
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
            type = Material.getMaterial(itemStr.toUpperCase(Locale.ROOT), false);
            if (type == null)
                type = Material.getMaterial(itemStr.toUpperCase(Locale.ROOT), true);
            amount = Rng.nextIntRanged(amountStr);
            if (amount == 0) return null;
        } else if (params.containsKey("type")) {
            String typeStr = params.getOrDefault("type", "");
            type = Material.getMaterial(typeStr.toUpperCase(Locale.ROOT));
        } else
            return null;
        if (type == null)
            return null;
        data = Rng.nextIntRanged(params.getOrDefault("data", "0"));
        amount = Rng.nextIntRanged(params.getOrDefault("amount", "1"));
        LegacyVirtualItem vi = new LegacyVirtualItem(type, data, amount);

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

    private static Enchantment getEnchantmentByName(String name) {
        if (!Utils.isStringEmpty(name))
            try {
                return Enchantment.getByKey(NamespacedKey.minecraft(name.toLowerCase(Locale.ROOT)));
            } catch (IllegalArgumentException ignore) {
            }
        return null;
    }

    private static String fireworksToString(FireworkEffect fe) {
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

    private static List<Color> parseColors(String colorStr) {
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
    private static Color parseColor(String colorStr) {
        if (BYTES_RGB.matcher(colorStr).matches()) {
            String[] rgb = colorStr.split(",");
            int red = Integer.parseInt(rgb[0]);
            int green = Integer.parseInt(rgb[1]);
            int blue = Integer.parseInt(rgb[2]);
            return Color.fromRGB(red, green, blue);
        } else if (NumberUtils.BYTE.matcher(colorStr).matches()) {
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

    private static double getColorDistance(Color c1, Color c2) {
        double rmean = (c1.getRed() + c2.getRed()) / 2.0;
        double r = c1.getRed() - c2.getRed();
        double g = c1.getGreen() - c2.getGreen();
        int b = c1.getBlue() - c2.getBlue();
        double weightR = 2 + rmean / 256.0;
        double weightG = 4.0;
        double weightB = 2 + (255 - rmean) / 256.0;
        return weightR * r * r + weightG * g * g + weightB * b * b;
    }

    private static DyeColor getClosestColor(Color color) {
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

    private static String colorToString(Color c, boolean useRGB) {
        for (DyeColor dc : DyeColor.values())
            if (dc.getColor().equals(c))
                return dc.name();
        if (!useRGB)
            getClosestColor(c).name();
        return c.getRed() + "," +
                c.getGreen() + "," +
                c.getBlue();
    }

    private static Object2IntMap<Enchantment> parseEnchantmentsString(String enchStr) {
        Object2IntMap<Enchantment> ench = new Object2IntOpenHashMap<>();
        if (enchStr == null || enchStr.isEmpty()) return ench;
        String[] ln = enchStr.split(";");
        for (String e : ln) {
            String eType = e;
            int power = 0;
            if (eType.contains(":")) {
                String powerStr = eType.substring(eType.indexOf(":") + 1);
                eType = eType.substring(0, eType.indexOf(":"));
                power = Rng.nextIntRanged(powerStr);
            }
            Enchantment enchantment = getEnchantmentByName(eType);
            if (enchantment == null)
                continue;
            ench.put(enchantment, power);
        }
        return ench;

    }

    /**
     * Old format algorithm. Implemented for compatibility.
     *
     * @param itemStr - old item format
     * @return - ItemStack
     */
    private static ItemStack parseOldItemStack(String itemStr) {
        if (Utils.isStringEmpty(itemStr))
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
                amount = Math.max(Rng.nextIntRanged(si[1]), 1);
            String[] ti = si[0].split(":");
            if (ti.length > 0) {
                Material m = Material.getMaterial(ti[0].toUpperCase(Locale.ROOT));
                if (m == null)
                    return null;
                id = m;
                if ((ti.length == 2) && (NumberUtils.INT_POSITIVE.matcher(ti[1]).matches()))
                    data = Short.parseShort(ti[1]);
                ItemStack item = new ItemStack(id, amount);
                ItemUtils.setDurability(item, data);
                if (!enchant.isEmpty()) {

                    String[] ln = enchant.split(",");
                    for (String ec : ln) {
                        if (ec.isEmpty())
                            continue;

                        Color clr = parseColor(ec);
                        if (clr != null) {
                            if (item.hasItemMeta()
                                    && (item.getItemMeta() instanceof LeatherArmorMeta meta)) {
                                meta.setColor(clr);
                                item.setItemMeta(meta);
                            }
                        } else {
                            String ench = ec;
                            int level = 1;
                            if (ec.contains(":")) {
                                ench = ec.substring(0, ec.indexOf(":"));
                                level = Math.max(1, Rng.nextIntRanged(ec.substring(ench
                                        .length() + 1)));
                            }
                            Enchantment e = getEnchantmentByName(ench);
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
            Enchantment enchantment = getEnchantmentByName(eType);
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
                    .toUpperCase(Locale.ROOT));
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
            Color c = parseColor(colorStr);
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
            put(params, "color", colorToString(lm.getColor(), true));
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
            put(params, "firework-effects", fireworksToString(fwm.getEffect()));
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
        Object2IntMap<Enchantment> enchantments = parseEnchantmentsString(enchStr);
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
        Map<String, String> params = Parameters.fromString(fireworkStr).getMap();
        FireworkEffect.Type fType;
        List<Color> colors;
        List<Color> fadeColors;
        boolean flicker;
        boolean trail;
        fType = FireworkEffect.Type.valueOf(params.getOrDefault("type", "")
                .toUpperCase(Locale.ROOT));
        flicker = "true".equalsIgnoreCase(params.getOrDefault("flicker", "false"));
        trail = "true".equalsIgnoreCase(params.getOrDefault("trail", "false"));
        colors = parseColors(params.getOrDefault("colors", ""));
        fadeColors = parseColors(params.getOrDefault("fade-colors", ""));
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
                Map<String, String> params = Parameters.fromString(fStr).getMap();
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
                colors = parseColors(params.getOrDefault("colors", ""));
                fadeColors = parseColors(params.getOrDefault("fade-colors", ""));
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

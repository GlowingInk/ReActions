package me.fromgate.reactions.activators;

import me.fromgate.reactions.actions.Actions;
import me.fromgate.reactions.event.EntityClickEvent;
import me.fromgate.reactions.event.RAEvent;
import me.fromgate.reactions.util.Param;
import me.fromgate.reactions.util.Variables;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;

/**
 * Created by MaxDikiy on 2017-05-14.
 */
public class EntityClickActivator extends Activator {
	private String entityType;

	public EntityClickActivator(String name, String param) {
		super(name, "activators");
		Param params = new Param(param);
		this.entityType = params.getParam("type", "");
	}

	public EntityClickActivator(String name, String group, YamlConfiguration cfg) {
		super(name, group, cfg);
	}

	@Override
	public boolean activate(RAEvent event) {
		if (!(event instanceof EntityClickEvent)) return false;
		EntityClickEvent ece = (EntityClickEvent) event;
		if (ece.getEntity() == null) return false;
		if (!isActivatorEntity(ece.getEntity())) return false;
		Variables.setTempVar("entitytype", ece.getEntity().getType().name());
		return Actions.executeActivator(ece.getPlayer(), this);
	}

	private boolean isActivatorEntity(Entity entity) {
		return this.entityType.isEmpty() || entity.getType().toString().equalsIgnoreCase(this.entityType);
	}

	@Override
	public boolean isLocatedAt(Location loc) {
		return false;
	}

	@Override
	public void save(ConfigurationSection cfg) {
		cfg.set("entity-type", this.entityType);
	}

	@Override
	public void load(ConfigurationSection cfg) {
		this.entityType = cfg.getString("entity-type", "");
	}

	@Override
	public ActivatorType getType() {
		return ActivatorType.ENTITY_CLICK;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(name).append(" [").append(getType()).append("]");
		if (!getFlags().isEmpty()) sb.append(" F:").append(getFlags().size());
		if (!getActions().isEmpty()) sb.append(" A:").append(getActions().size());
		if (!getReactions().isEmpty()) sb.append(" R:").append(getReactions().size());
		sb.append(" (");
		sb.append("type:").append(entityType.isEmpty() ? "-" : entityType.toUpperCase());
		sb.append(")");
		return sb.toString();
	}

	@Override
	public boolean isValid() {
		return true;
	}
}

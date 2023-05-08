package lol.pyr.znpcsplus.hologram;

import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import lol.pyr.znpcsplus.api.entity.EntityProperty;
import lol.pyr.znpcsplus.api.entity.PropertyHolder;
import lol.pyr.znpcsplus.entity.EntityPropertyImpl;
import lol.pyr.znpcsplus.entity.PacketEntity;
import lol.pyr.znpcsplus.util.ZLocation;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.Collection;

public class HologramLine implements PropertyHolder {
    private Component text;
    private final PacketEntity armorStand;

    public HologramLine(ZLocation location, Component text) {
        this.text = text;
        armorStand = new PacketEntity(this, EntityTypes.ARMOR_STAND, location);
    }

    public Component getText() {
        return text;
    }

    public void setText(Component text) {
        this.text = text;
    }

    protected void show(Player player) {
        armorStand.spawn(player);
    }

    protected void hide(Player player) {
        armorStand.despawn(player);
    }

    public void setLocation(ZLocation location, Collection<Player> viewers) {
        armorStand.setLocation(location, viewers);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getProperty(EntityProperty<T> key) {
        if (key == EntityPropertyImpl.INVISIBLE) return (T) Boolean.TRUE;
        if (key == EntityPropertyImpl.NAME) return (T) text;
        return key.getDefaultValue();
    }

    @Override
    public boolean hasProperty(EntityProperty<?> key) {
        return key == EntityPropertyImpl.NAME || key == EntityPropertyImpl.INVISIBLE;
    }
}

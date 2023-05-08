package lol.pyr.znpcsplus.skin.descriptor;

import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import lol.pyr.znpcsplus.api.skin.SkinDescriptor;
import lol.pyr.znpcsplus.skin.BaseSkinDescriptor;
import lol.pyr.znpcsplus.skin.Skin;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public class PrefetchedDescriptor implements BaseSkinDescriptor, SkinDescriptor {
    private final Skin skin;

    public PrefetchedDescriptor(Skin skin) {
        this.skin = skin;
    }

    @Override
    public CompletableFuture<Skin> fetch(Player player) {
        return CompletableFuture.completedFuture(skin);
    }

    @Override
    public Skin fetchInstant(Player player) {
        return skin;
    }

    @Override
    public boolean supportsInstant(Player player) {
        return true;
    }

    public Skin getSkin() {
        return skin;
    }

    @Override
    public String serialize() {
        StringBuilder sb = new StringBuilder();
        sb.append("prefetched;");
        for (TextureProperty property : skin.getProperties()) {
            sb.append(property.getName()).append(";");
            sb.append(property.getValue()).append(";");
            sb.append(property.getSignature()).append(";");
        }
        return sb.toString();
    }
}

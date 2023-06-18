package lol.pyr.znpcsplus.conversion.znpcs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import lol.pyr.znpcsplus.api.interaction.InteractionType;
import lol.pyr.znpcsplus.config.ConfigManager;
import lol.pyr.znpcsplus.conversion.DataImporter;
import lol.pyr.znpcsplus.conversion.znpcs.model.ZNpcsAction;
import lol.pyr.znpcsplus.conversion.znpcs.model.ZNpcsLocation;
import lol.pyr.znpcsplus.conversion.znpcs.model.ZNpcsModel;
import lol.pyr.znpcsplus.interaction.InteractionAction;
import lol.pyr.znpcsplus.interaction.consolecommand.ConsoleCommandAction;
import lol.pyr.znpcsplus.interaction.message.MessageAction;
import lol.pyr.znpcsplus.interaction.playerchat.PlayerChatAction;
import lol.pyr.znpcsplus.interaction.playercommand.PlayerCommandAction;
import lol.pyr.znpcsplus.interaction.switchserver.SwitchServerAction;
import lol.pyr.znpcsplus.npc.NpcEntryImpl;
import lol.pyr.znpcsplus.npc.NpcImpl;
import lol.pyr.znpcsplus.npc.NpcTypeRegistryImpl;
import lol.pyr.znpcsplus.packets.PacketFactory;
import lol.pyr.znpcsplus.scheduling.TaskScheduler;
import lol.pyr.znpcsplus.util.BungeeConnector;
import lol.pyr.znpcsplus.util.NpcLocation;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class ZNpcImporter implements DataImporter {
    private final ConfigManager configManager;
    private final BukkitAudiences adventure;
    private final BungeeConnector bungeeConnector;
    private final TaskScheduler taskScheduler;
    private final PacketFactory packetFactory;
    private final LegacyComponentSerializer textSerializer;
    private final NpcTypeRegistryImpl typeRegistry;
    private final File dataFile;
    private final Gson gson;

    public ZNpcImporter(ConfigManager configManager, BukkitAudiences adventure, BungeeConnector bungeeConnector,
                        TaskScheduler taskScheduler, PacketFactory packetFactory, LegacyComponentSerializer textSerializer,
                        NpcTypeRegistryImpl typeRegistry, File dataFile) {

        this.configManager = configManager;
        this.adventure = adventure;
        this.bungeeConnector = bungeeConnector;
        this.taskScheduler = taskScheduler;
        this.packetFactory = packetFactory;
        this.textSerializer = textSerializer;
        this.typeRegistry = typeRegistry;
        this.dataFile = dataFile;
        gson = new GsonBuilder()
                .create();
    }

    @Override
    public Collection<NpcEntryImpl> importData() {
        ZNpcsModel[] models;
        try (BufferedReader fileReader = Files.newBufferedReader(dataFile.toPath())) {
            JsonElement element = JsonParser.parseReader(fileReader);
            models = gson.fromJson(element, ZNpcsModel[].class);
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
        if (models == null) return Collections.emptyList();
        ArrayList<NpcEntryImpl> entries = new ArrayList<>();
        for (ZNpcsModel model : models) {
            String type = model.getNpcType();

            switch (type.toLowerCase()) {
                case "mushroom_cow":
                    type = "mooshroom";
                    break;
                case "snowman":
                    type = "snow_golem";
                    break;
            }

            ZNpcsLocation oldLoc = model.getLocation();
            NpcLocation location = new NpcLocation(oldLoc.getX(), oldLoc.getY(), oldLoc.getZ(), oldLoc.getYaw(), oldLoc.getPitch());
            NpcImpl npc = new NpcImpl(configManager, packetFactory, textSerializer, oldLoc.getWorld(), typeRegistry.getByName(type), location);

            for (String raw : model.getHologramLines()) {
                Component line = textSerializer.deserialize(raw);
                npc.getHologram().addLineComponent(line);
            }

            for (ZNpcsAction action : model.getClickActions()) {
                InteractionType t = adaptClickType(action.getClickType());
                npc.addAction(adaptAction(action.getActionType(), t, action.getAction(), action.getDelay()));
            }

            NpcEntryImpl entry = new NpcEntryImpl(String.valueOf(model.getId()), npc);
            entry.enableEverything();
            entries.add(entry);
        }
        return entries;
    }

    @Override
    public boolean isValid() {
        return dataFile.isFile();
    }

    private InteractionType adaptClickType(String clickType) {
        switch (clickType.toLowerCase()) {
            case "default":
                return InteractionType.ANY_CLICK;
            case "left":
                return InteractionType.LEFT_CLICK;
            case "right":
                return InteractionType.RIGHT_CLICK;
        }
        throw new IllegalArgumentException("Couldn't adapt znpcs click type: " + clickType);
    }

    private InteractionAction adaptAction(String type, InteractionType clickType, String parameter, int delay) {
        switch (type.toLowerCase()) {
            case "cmd":
                return new PlayerCommandAction(taskScheduler, parameter, clickType, delay * 1000L);
            case "console":
                return new ConsoleCommandAction(taskScheduler, parameter, clickType, delay * 1000L);
            case "chat":
                return new PlayerChatAction(taskScheduler, parameter, clickType, delay * 1000L);
            case "message":
                return new MessageAction(adventure, parameter, clickType, textSerializer, delay * 1000L);
            case "server":
                return new SwitchServerAction(bungeeConnector, parameter, clickType, delay * 1000L);
        }
        throw new IllegalArgumentException("Couldn't adapt znpcs click action: " + type);
    }
}
package net.onyx.client.modules.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.onyx.client.OnyxClient;
import net.onyx.client.config.settings.Setting;
import net.onyx.client.events.Event;
import net.onyx.client.events.client.ClientTickEvent;
import net.onyx.client.events.client.DisconnectEvent;
import net.onyx.client.events.packet.OnEntityStatusEvent;
import net.onyx.client.events.screen.DeathEvent;
import net.onyx.client.modules.Module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class TotemPopCount extends Module {
    public TotemPopCount() {
        super("TotemPopCount");

        this.setDescription("This counts the total number of totems used by a player before death.");

        // Default One min
        this.addSetting(new Setting("CountDuration", 60d) {{
            this.setDescription("The duration in seconds to count the totems before resetting.");

            this.setMax(3600d);
            this.setMin(1d);
        }});
        this.addSetting(new Setting("DeathMessage", true) {{
            this.setDescription("Show how many totems someone has used before death.");
        }});
        
        this.setCategory("Combat");
    }

    private Integer localPopCount = 0;

    public Integer getTotalPops() {
        return this.localPopCount / 2;
    }

    private static class PlayerEntry {
        private Integer popPackets = 0;
        public Double lastValid;

        public PlayerEntry() {
            this.valid();
        }

        public Integer onPopPacket() {
            this.valid();
            return ++this.popPackets;
        }

        public Boolean isPop() {
            return this.popPackets % 2 == 0;
        }

        public Integer getPops() {
            return this.popPackets / 2;
        }

        public Double getLastValid() {
            return this.lastValid;
        }

        public void valid() {
            this.lastValid = OnyxClient.getCurrentTime();
        }
    }

    private final HashMap<UUID, PlayerEntry> entries = new HashMap<>();

    @Override
    public String listOption() {
        return (this.getTotalPops()).toString();
    }

    @Override
    public void activate() {
        this.addListen(ClientTickEvent.class);
        this.addListen(DeathEvent.class);
        this.addListen(OnEntityStatusEvent.class);
        this.addListen(DisconnectEvent.class);
    }

    @Override
    public void deactivate() {
        this.removeListen(DisconnectEvent.class);
        this.removeListen(DeathEvent.class);
        this.removeListen(ClientTickEvent.class);
        this.removeListen(OnEntityStatusEvent.class);
    }

    public PlayerEntry playerPop(PlayerEntity player) {
        if (player == OnyxClient.me()) {
            this.localPopCount++;

            return null;
        }

        UUID uuid = player.getUuid();

        if (!this.entries.containsKey(uuid)) {
            this.entries.put(uuid, new PlayerEntry());
        }
        PlayerEntry entry = this.entries.get(uuid);

        entry.onPopPacket();
        return entry;
    }

    private void resetLocal() {
        this.localPopCount = 0;
    }

    private void resetEntries() {
        this.entries.clear();
    }

    private void resetAll() {
        this.resetLocal();
        this.resetEntries();
    }

    @Override
    public void fireEvent(Event event) {
        switch (event.getClass().getSimpleName()) {
            case "DeathEvent": {
                if (this.getBoolSetting("DeathMessage") && this.getTotalPops() > 0) this.displayMessage(String.format("You used %d totems before you died.", this.getTotalPops()));

                this.resetLocal();
                break;
            }

            case "DisconnectEvent": {
                this.resetAll();
                break;
            }

            case "ClientTickEvent": {
                for (PlayerEntity player : OnyxClient.getClient().world.getPlayers()) {
                    UUID uuid = player.getUuid();

                    if (!this.entries.containsKey(uuid)) continue;
                    PlayerEntry entry = this.entries.get(uuid);

                    if (!player.isAlive() && entry.isPop()) {
                        if (this.getBoolSetting("DeathMessage")) this.displayMessage(String.format("%s died after popping a total of %d totems.", player.getEntityName(), entry.getPops()));
                        this.entries.remove(uuid);
                        continue;
                    }

                    this.entries.get(player.getUuid()).valid();
                }

                Double duration = this.getDoubleSetting("CountDuration");

                List<UUID> expiredUUIDs = new ArrayList<>();
                for (UUID uuid : this.entries.keySet()) {
                    PlayerEntry entry = this.entries.get(uuid);

                    Double lastValid = entry.getLastValid();

                    if (OnyxClient.getCurrentTime() - lastValid >= duration) {
                        expiredUUIDs.add(uuid);
                    }
                }

                // Remove them separately
                for (UUID uuid : expiredUUIDs) {
                    this.entries.remove(uuid);
                }

                break;
            }

            case "OnEntityStatusEvent": {
                OnEntityStatusEvent e = (OnEntityStatusEvent)event;

                if (e.packet.getStatus() != 35) break;

                Entity entity = e.packet.getEntity(OnyxClient.me().world);
                
                // I mean I have no idea how that would work but like whatever.
                if (!(entity instanceof PlayerEntity player)) break;

                PlayerEntry entry = this.playerPop(player);

                // MC is a bit wacky when handling totem popping.
                if (entry == null || !entry.isPop()) break;

                this.displayMessage(String.format("%s has popped %d totems so far.", player.getEntityName(), entry.getPops()));

                break;
            }
        }
    }
}

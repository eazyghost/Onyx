package net.onyx.client.modules.render;

import net.onyx.client.events.Event;
import net.onyx.client.events.render.BossBarHudRenderEvent;
import net.onyx.client.events.render.BossBarHudSkyEffectsEvent;
import net.onyx.client.modules.Module;

public class NoBoss extends Module {
    public NoBoss() {
        super("NoBossBar");

        this.setDescription("Hide annoying boss bars and their effects.");
        this.setCategory("Render");
    }
  
    @Override
    public void activate() {
        this.addListen(BossBarHudRenderEvent.class);
        this.addListen(BossBarHudSkyEffectsEvent.class);
    }

    @Override
    public void deactivate() {
        this.removeListen(BossBarHudRenderEvent.class);
        this.removeListen(BossBarHudSkyEffectsEvent.class);
    }

    @Override
    public void fireEvent(Event event) {
        switch (event.getClass().getSimpleName()) {
            case "BossBarHudRenderEvent": {
                ((BossBarHudRenderEvent)event).ci.cancel();
                break;
            }
            case "BossBarHudSkyEffectsEvent": {
                ((BossBarHudSkyEffectsEvent)event).cir.setReturnValue(false);
                break;
            }
        }
    }
}

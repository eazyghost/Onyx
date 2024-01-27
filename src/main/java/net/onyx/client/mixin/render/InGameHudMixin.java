package net.onyx.client.mixin.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Matrix4f;
import net.onyx.client.OnyxClient;
import net.onyx.client.events.render.InGameHudRenderEvent;
import net.onyx.client.events.render.RenderHealthBarEvent;
import net.onyx.client.events.render.RenderPortalOverlayEvent;
import net.onyx.client.onyxevent.EventManager;
import net.onyx.client.onyxevent.events.GUIRenderListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Shadow private int scaledWidth;

    @Shadow private int scaledHeight;

    @Inject(method = {"render(Lnet/minecraft/client/util/math/MatrixStack;F)V"}, at = {@At("HEAD")})
    public void render(MatrixStack mStack, float tickDelta, CallbackInfo ci) {
        OnyxClient.getClient().getProfiler().push("walksy-client_render_2d");

        OnyxClient.getInstance().emitter.triggerEvent(new InGameHudRenderEvent(mStack, tickDelta, ci));
        
        OnyxClient.getClient().getProfiler().pop();

        RenderSystem.setProjectionMatrix(Matrix4f.projectionMatrix(0, OnyxClient.getClient().getWindow().getFramebufferWidth(), 0, OnyxClient.getClient().getWindow().getFramebufferHeight(), 1000, 3000));

        GUIRenderListener.GUIRenderEvent event = new GUIRenderListener.GUIRenderEvent(mStack, tickDelta);
        EventManager.fire(event);

        RenderSystem.setProjectionMatrix(Matrix4f.projectionMatrix(0, (float) (OnyxClient.getClient().getWindow().getFramebufferWidth() / OnyxClient.getClient().getWindow().getScaleFactor()), 0, (float) (OnyxClient.getClient().getWindow().getFramebufferHeight() / OnyxClient.getClient().getWindow().getScaleFactor()), 1000, 3000));
        RenderSystem.applyModelViewMatrix();
    }

    @Inject(at = @At("HEAD"), method = {"renderPortalOverlay(F)V"}, cancellable = true)
    public void onRenderPortalOverlay(float nauseaStrength, CallbackInfo ci) {
        OnyxClient.getInstance().emitter.triggerEvent(new RenderPortalOverlayEvent(nauseaStrength, ci));
    }

    @Inject(at = @At("HEAD"), method = "renderHealthBar(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/entity/player/PlayerEntity;IIIIFIIIZ)V", cancellable = true)
    public void onRenderHealthBar(MatrixStack matrices, PlayerEntity player, int x, int y, int lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking, CallbackInfo ci) {
        OnyxClient.getInstance().emitter.triggerEvent(
            new RenderHealthBarEvent(matrices, player, x, y, lines, regeneratingHeartIndex, maxHealth, lastHealth, health, absorption, blinking, ci)
        );
    }
}

package net.onyx.client.mixin.botch;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import net.onyx.client.OnyxClient;
import net.onyx.client.components.ProjectionUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(GameRenderer.class)
public abstract class ProjectionUtilsMixin {
    @Inject(method = "renderWorld", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = { "ldc=hand" }), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void onRenderWorld(float tickDelta, long limitTime, MatrixStack matrices, CallbackInfo info, boolean bl, Camera camera, MatrixStack matrixStack, double d, Matrix4f matrix4f) {
        MinecraftClient client = OnyxClient.getClient();
        if ((client == null || client.world == null || OnyxClient.me() == null)) return;

        client.getProfiler().push("walksy-client_render");
        
        ProjectionUtils.getInstance().update(matrices, matrix4f);

        RenderSystem.applyModelViewMatrix();
        client.getProfiler().pop();
    }
}

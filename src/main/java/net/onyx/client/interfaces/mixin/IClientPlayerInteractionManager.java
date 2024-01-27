package net.onyx.client.interfaces.mixin;

public interface IClientPlayerInteractionManager {
    void setBlockBreakProgress(float var1);

    void setBlockBreakingCooldown(int var1);

    float getBlockBreakProgress();

    void syncSelected();
}

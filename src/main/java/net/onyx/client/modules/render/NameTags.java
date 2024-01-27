package net.onyx.client.modules.render;


import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.onyx.client.OnyxClient;
import net.onyx.client.components.ProjectionUtils;
import net.onyx.client.config.settings.Setting;
import net.onyx.client.events.Event;
import net.onyx.client.events.render.InGameHudRenderEvent;
import net.onyx.client.events.render.renderLabelIfPresentEvent;
import net.onyx.client.misc.Colour;
import net.onyx.client.misc.attributes.Attribute;
import net.onyx.client.misc.attributes.entity.HealthAttribute;
import net.onyx.client.misc.attributes.entity.NameAttribute;
import net.onyx.client.misc.attributes.player.PingAttribute;
import net.onyx.client.misc.maths.Vec3;
import net.onyx.client.modules.Module;
import net.onyx.client.utils.ClientUtils;


public class NameTags extends Module {
    public NameTags() {
        super("NameTags");

        this.addSetting(new Setting("Scale", 3.0f));
        this.addSetting(new Setting("OutlineAlpha", 125));

        this.setDescription("Renders a different kind of name-tag above nearby players.");

        this.setCategory("Render");
    }



    @Override
    public void activate() {
        this.addListen(InGameHudRenderEvent.class);
        this.addListen(renderLabelIfPresentEvent.class);
    }

    @Override
    public void deactivate() {
        this.removeListen(InGameHudRenderEvent.class);
        this.removeListen(renderLabelIfPresentEvent.class);
    }

    private void displayNameTag(PlayerEntity player, MatrixStack mStack, float tickDelta) {
        // Get position
        Vec3d pos = player.getLerpedPos(tickDelta).add(0, player.getBoundingBox().maxY - player.getPos().y + 0.25, 0);

        // Text renderer
        TextRenderer r = OnyxClient.getInstance().textRenderer;

        // Get the different attributes
        float textOffsets = r.getWidth(" ")/2;
        Attribute[] attributes = {
                new NameAttribute(player),
                new HealthAttribute(player),
                new PingAttribute(player),
        };

        // Calculate length
        int len = 0;
        for (Attribute attribute : attributes) {
            len += r.getWidth(attribute.getText());
        }
        len += (attributes.length - 1) * textOffsets;

        // Calculate scale
        float scale = (Float)(this.getSetting("Scale").value);
        float absoluteScale = scale*(float) ProjectionUtils.getScale(pos, tickDelta);

        // WorldToScreen
        Vec3 playerPos = new Vec3(pos);
        if (!ProjectionUtils.getInstance().to2D(playerPos, absoluteScale)) return;
        ProjectionUtils.scaleProjection(absoluteScale);

        // Positions
        float x = -len/2 + (float)playerPos.x;
        float y = -10 + (float)playerPos.y;
        int outlineAlpha = (int)this.getSetting("OutlineAlpha").value;

        Matrix4f matrix4f = mStack.peek().getPositionMatrix();
        for (Attribute attribute : attributes) {
            VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());

            Colour c = new Colour(0, 0, 0, outlineAlpha + 5);

            r.drawWithOutline(attribute.getText().asOrderedText(), x, y, attribute.getColour(), c.toARGB(), matrix4f, immediate, 255);
            x += r.getWidth(attribute.getText()) + textOffsets;

            immediate.draw();
        }
    }

    @Override
    public void fireEvent(Event event) {
        switch (event.getClass().getSimpleName()) {
            case "InGameHudRenderEvent": {
                InGameHudRenderEvent e = (InGameHudRenderEvent)event;
                Iterable<Entity> ents = OnyxClient.getClient().world.getEntities();

                for (Entity entity : ents) {
                    // No render myself.
                    if (!(entity instanceof PlayerEntity player) || (entity == OnyxClient.me() && !ClientUtils.isThirdperson())) {
                        continue;
                    }

                    this.displayNameTag(player, e.mStack, e.tickDelta);
                    ProjectionUtils.resetProjection();
                }

                break;
            }
            case "renderLabelIfPresentEvent": {
                renderLabelIfPresentEvent<Entity> e = (renderLabelIfPresentEvent<Entity>)event;
                if (!(e.entity instanceof PlayerEntity)) break;

                e.ci.cancel();

                break;
            }
        }
    }
}
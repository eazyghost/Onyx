package net.onyx.client.misc.attributes.entity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.onyx.client.misc.Colour;
import net.onyx.client.misc.attributes.Attribute;
import net.onyx.client.utils.ClientUtils;

public class HealthAttribute extends Attribute {
    public HealthAttribute(LivingEntity entity) {
        super(entity);
    }

    private int getHealth() {
        return ClientUtils.getHealth(this.getEntity());
    }

    @Override
    public Text getText() {
        return Text.of(
            String.valueOf(this.getHealth())
        );
    }

    @Override
    public int getColour() {
        float f = (this.getHealth() / this.getEntity().getMaxHealth()) * 255 * 2;
        Colour c = new Colour((int)(255*2 - f), (int)(f), 0, 255);

        return c.toARGB();
    }
}

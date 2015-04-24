package mcjty.rftools.blocks.environmental.modules;

import mcjty.rftools.PlayerBuff;
import net.minecraft.potion.Potion;

public class WaterBreathingEModule extends PotionEffectModule {
    public static final float RFPERTICK = 0.001f;

    public WaterBreathingEModule() {
        super(Potion.waterBreathing.getId(), 0);
    }

    @Override
    public float getRfPerTick() {
        return RFPERTICK;
    }

    @Override
    protected PlayerBuff getBuff() {
        return PlayerBuff.BUFF_WATERBREATHING;
    }
}

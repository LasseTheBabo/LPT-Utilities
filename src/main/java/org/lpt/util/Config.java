package org.lpt.util;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Mod.EventBusSubscriber(modid = Util.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    public static final boolean DEBUG = true;
    public static final int S2C_BYTES = 4110;
    public static final int C2S_BYTES = 1460;
    public static final Charset CHARSET = StandardCharsets.US_ASCII;

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    static final ForgeConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {

    }
}

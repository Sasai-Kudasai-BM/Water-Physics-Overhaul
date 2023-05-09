package net.skds.wpo.config;

import java.util.function.Function;

import net.minecraftforge.common.ForgeConfigSpec;
import net.skds.wpo.WPO;
import net.skds.wpo.WPOConfig;

public class Main {

    //public final ForgeConfigSpec.BooleanValue slide;
    public final ForgeConfigSpec.IntValue maxSlideDist, maxEqDist, maxBucketDist;

    // public final ForgeConfigSpec.ConfigValue<ArrayList<String>> ss;
    // private final ForgeConfigSpec.IntValue maxFluidLevel;

    public Main(ForgeConfigSpec.Builder innerBuilder) {
        Function<String, ForgeConfigSpec.Builder> builder = name -> innerBuilder .translation(WPO.MOD_ID + ".config." + name);

        innerBuilder.push("General");

        // slide = builder.apply("setSlide").comment("Will fluids slide down from hills").define("setSlide", true);
        maxEqDist = builder.apply("setMaxEqualizeDistance")
                .comment("the distance over which water levels will equalize")
                .defineInRange("setMaxEqualizeDistance", 16, 0, 256);
        maxSlideDist = builder.apply("setMaxSlidingDistance")
                .comment("the maximum distance water will slide to reach lower ground")
                .defineInRange("setMaxSlidingDistance", 5, 0, 256);
        maxBucketDist = builder.apply("setMaxBucketDistance")
                .comment("Maximum horizontal bucket reach from click location (for water packet pickup)")
                .defineInRange("setMaxBucketDistance", 8, 0, WPOConfig.MAX_FLUID_LEVEL);

        innerBuilder.pop();
    }
}
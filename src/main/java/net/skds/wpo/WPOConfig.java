package net.skds.wpo;

import java.nio.file.Paths;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.skds.wpo.config.Main;

public class WPOConfig {

    public static final Main COMMON;
    //public static final Waterlogged WATERLOGGED;
    private static final ForgeConfigSpec SPEC;//, SPEC_WL;


    public static final int MAX_FLUID_LEVEL = 8;

    static {
        Pair<Main, ForgeConfigSpec> cm = new ForgeConfigSpec.Builder().configure(Main::new);
        COMMON = cm.getLeft();
        SPEC = cm.getRight();

        //Pair<Waterlogged, ForgeConfigSpec> wl = new ForgeConfigSpec.Builder().configure(Waterlogged::new);
        ///WATERLOGGED = wl.getLeft();
        //SPEC_WL = wl.getRight();

        // FINITE_WATER = COMMON.finiteWater.get();
        // MAX_EQ_DIST = COMMON.maxEqDist.get();
    }

    public static void init() {
        Paths.get(System.getProperty("user.dir"), "config", WPO.MOD_ID).toFile().mkdir();
        ModLoadingContext.get().registerConfig(Type.COMMON, SPEC, Paths.get(WPO.MOD_ID, "common.toml").toString());
        //ModLoadingContext.get().registerConfig(Type.COMMON, SPEC_WL, PhysEX.MOD_ID + "/waterlogged.toml");
    }
}

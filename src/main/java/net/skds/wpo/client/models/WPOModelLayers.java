package net.skds.wpo.client.models;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.skds.wpo.WPO;

public class WPOModelLayers {
    // inspired from net.minecraft.client.model.geom.ModelLayer
    private static final String DEFAULT_LAYER = "main";
    public static final ModelLayerLocation PIPE = create("pipe");
//    public static final ModelLayerLocation PIPE = create("textures/block/pipe.png");
    public static final ModelLayerLocation PIPE_PUMP = create("pump_te2");
//    public static final ModelLayerLocation PIPE_PUMP = create("textures/block/pump_te2.png");
    public static final ModelLayerLocation PUMP = create("pump");
//    public static final ModelLayerLocation PUMP = create("textures/block/pump_te.png");

    private static ModelLayerLocation create(String name) {
        return new ModelLayerLocation(new ResourceLocation(WPO.MOD_ID, name), "main");
    }
}

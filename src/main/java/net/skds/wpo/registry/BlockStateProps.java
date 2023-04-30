package net.skds.wpo.registry;

import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.core.Direction;
import net.skds.wpo.WPOConfig;

public class BlockStateProps {

	public static final IntegerProperty FFLUID_LEVEL = IntegerProperty.create("ffluid_level", 0, WPOConfig.MAX_FLUID_LEVEL);
	public static final EnumProperty<Direction> ROTATION = EnumProperty.create("frotation", Direction.class);

}
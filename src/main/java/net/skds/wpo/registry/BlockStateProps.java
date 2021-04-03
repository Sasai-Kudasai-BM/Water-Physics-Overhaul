package net.skds.wpo.registry;

import net.minecraft.state.EnumProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.util.Direction;
import net.skds.wpo.WPOConfig;

public class BlockStateProps {

	public static final IntegerProperty FFLUID_LEVEL = IntegerProperty.create("ffluid_level", 0, WPOConfig.MAX_FLUID_LEVEL);
	public static final EnumProperty<Direction> ROTATION = EnumProperty.create("frotation", Direction.class);

}
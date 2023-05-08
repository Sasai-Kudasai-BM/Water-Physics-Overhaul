package net.skds.wpo.mixins.block;

import net.skds.core.api.IBlockExtraStates;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.level.block.BaseCoralPlantTypeBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.ConduitBlock;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.TrappedChestBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.skds.wpo.registry.BlockStateProps;
import net.skds.wpo.util.interfaces.IBaseWL;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

// TODO why mix into block here (instead of in sdks core)
@Mixin(value = { SlabBlock.class, CrossCollisionBlock.class, ChainBlock.class, TrapDoorBlock.class, CampfireBlock.class,
		LanternBlock.class, ChainBlock.class, StairBlock.class, WallSignBlock.class, StandingSignBlock.class,
		LadderBlock.class, BaseCoralPlantTypeBlock.class, SeaPickleBlock.class, ChestBlock.class,
		TrappedChestBlock.class, EnderChestBlock.class, ScaffoldingBlock.class, ConduitBlock.class })
public class BaseWLMixin extends Block implements IBaseWL, IBlockExtraStates {

	public BaseWLMixin(Properties properties) {
		super(properties);
	}

	@Override
	public void customStatesRegister(Block b, StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(BlockStateProps.FFLUID_LEVEL);
	}

	@Override
	public void fixDS() {
		this.registerDefaultState(this.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(false)));
	}
}
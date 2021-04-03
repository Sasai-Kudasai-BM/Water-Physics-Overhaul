package net.skds.wpo.mixins.block;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.block.AbstractCoralPlantBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.ChainBlock;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.ConduitBlock;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.block.FourWayBlock;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.LanternBlock;
import net.minecraft.block.ScaffoldingBlock;
import net.minecraft.block.SeaPickleBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.StandingSignBlock;
import net.minecraft.block.TrapDoorBlock;
import net.minecraft.block.TrappedChestBlock;
import net.minecraft.block.WallSignBlock;
import net.minecraft.state.StateContainer;
import net.skds.wpo.registry.BlockStateProps;
import net.skds.wpo.util.interfaces.IBaseWL;

@Mixin(value = { SlabBlock.class, FourWayBlock.class, ChainBlock.class, TrapDoorBlock.class, CampfireBlock.class,
		LanternBlock.class, ChainBlock.class, StairsBlock.class, WallSignBlock.class, StandingSignBlock.class,
		LadderBlock.class, AbstractCoralPlantBlock.class, SeaPickleBlock.class, ChestBlock.class,
		TrappedChestBlock.class, EnderChestBlock.class, ScaffoldingBlock.class, ConduitBlock.class })
public class BaseWLMixin implements IBaseWL {

	protected void customStatesRegister(Block b, StateContainer.Builder<Block, BlockState> builder) {
		builder.add(BlockStateProps.FFLUID_LEVEL);
	}
}
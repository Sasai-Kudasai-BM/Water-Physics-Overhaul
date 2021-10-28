package net.skds.wpo.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.skds.wpo.commands.arguments.FluidStateArgument;
import net.skds.wpo.util.FUtils;

public class SetFluidCommand {

	public static LiteralArgumentBuilder<CommandSource> create() {
		return Commands.literal("setfluid").requires((s) -> s.hasPermissionLevel(2))
				.then(Commands.argument("pos", BlockPosArgument.blockPos())
				.then(Commands.argument("fluid", new FluidStateArgument())
				.executes(context -> setFluidState(BlockPosArgument.getLoadedBlockPos(context, "pos"), FluidStateArgument.getFluidState(context, "fluid"), context.getSource().getWorld()))));
	}

	public static int setFluidState(BlockPos pos, FluidState state, ServerWorld w) {
		try {
			FUtils.setFluidState(state, w, pos);
			FUtils.markUpdated(w, pos);
			return 1;
		} catch (Exception e) {
			return 0;
		}
	}
}

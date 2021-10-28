package net.skds.wpo.commands.arguments;

import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.fluid.FluidState;
import net.minecraftforge.registries.ForgeRegistries;
import net.skds.wpo.util.FUtils;

public class FluidStateArgument implements ArgumentType<FluidState> {

	@Override
	public FluidState parse(StringReader reader) throws CommandSyntaxException {
		return FUtils.parse(reader);
	}

	public static FluidState getFluidState(CommandContext<CommandSource> context, String name) {
		return context.getArgument(name, FluidState.class);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		
		ISuggestionProvider.suggestIterable(ForgeRegistries.FLUIDS.getKeys(), builder);
		return builder.buildFuture();
	}
}

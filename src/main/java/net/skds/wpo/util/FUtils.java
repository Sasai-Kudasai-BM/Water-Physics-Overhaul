package net.skds.wpo.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraftforge.registries.ForgeRegistries;
import net.skds.core.util.blockupdate.WWSGlobal;
import net.skds.core.util.data.capability.ChunkCapabilityData;
import net.skds.wpo.data.WPOChunkSectionData;
import net.skds.wpo.fluidphysics.FluidCallback;
import net.skds.wpo.fluidphysics.WorldWorkSet;

public class FUtils {
	public static final DynamicCommandExceptionType BAD_STATE = new DynamicCommandExceptionType((p_208687_0_) -> {
		return new TranslationTextComponent("argument.fluidstate.invalid", p_208687_0_);
	});

	public static FluidCallback setFluidState(FluidState fs, World w, BlockPos pos) {
		Chunk c = w.getChunkProvider().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
		if (c == null || c.isEmpty()) {
			return FluidCallback.EMPTY;
		}
		return setFluidState(fs, c, pos.getX(), pos.getY(), pos.getZ());
	}

	public static FluidCallback setFluidState(FluidState fs, Chunk c, BlockPos pos) {
		return setFluidState(fs, c, pos.getX(), pos.getY(), pos.getZ());
	}

	public static FluidCallback setFluidState(FluidState fs, Chunk c, int x, int y, int z) {
		int cy = y >> 4;
		WPOChunkSectionData data = ChunkCapabilityData.getCap(c).get().getCSAD(cy, true).getData(WPOChunkSectionData.class);
		if (data == null) {
			return FluidCallback.EMPTY;
		}
		return setFluidState(fs, c, data, x, y, z);
	}

	public static FluidCallback setFluidState(FluidState fs, Chunk c, WPOChunkSectionData data, int x, int y, int z) {
		int cy = y >> 4;
		x &= 15;
		y &= 15;
		z &= 15;
		ChunkSection[] sections = c.getSections();
		ChunkSection section = sections[cy];
		if (ChunkSection.isEmpty(section)) {
			section = new ChunkSection(cy << 4);
			sections[cy] = section;
		}
		FluidState ofs = data.setFS(x, y, z, fs);
		BlockState oldState = section.getBlockState(x, y, z);
		BlockState newState = oldState;
		if (oldState.getFluidState() != fs) {
			if (fs.getFluid().isEquivalentTo(Fluids.WATER) && oldState.hasProperty(BlockStateProperties.WATERLOGGED)) {
				newState = oldState.with(BlockStateProperties.WATERLOGGED, !fs.isEmpty());
			} else if (oldState.getMaterial() == Material.AIR || oldState.getMaterial().isLiquid()) {	
				newState = fs.getBlockState();
			}
			if (oldState != newState) {
				section.setBlockState(x, y, z, newState);
			}
		}

		return new FluidCallback(ofs, fs, oldState, newState);
	}

	public static void markUpdated(World w, BlockPos pos) {
		WorldWorkSet wws = WWSGlobal.get(w).getTyped(WorldWorkSet.class);
		wws.updatedChunks.add(ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4));
	}

	@SuppressWarnings("unchecked")
	public static <T extends Comparable<T>, V extends T> FluidState parse(StringReader reader) throws CommandSyntaxException {
		
		ResourceLocation id = ResourceLocation.read(reader);
		Fluid fluid = ForgeRegistries.FLUIDS.getValue(id);
		if (fluid == null) {
			throw BAD_STATE.createWithContext(reader, id.toString());
		}

		StateContainer<Fluid, FluidState> container = fluid.getStateContainer();
		FluidState state = fluid.getDefaultState();
		if (reader.canRead() && reader.peek() == '[') {
			reader.skip();
			reader.skipWhitespace();

			Map<Property<T>, Comparable<T>> properties = new HashMap<>();

			while (true) {
				if (reader.canRead() && reader.peek() != ']') {
					reader.skipWhitespace();
					int i = reader.getCursor();
					String s = reader.readString();
					Property<T> property = (Property<T>) container.getProperty(s);
					if (property == null) {
						reader.setCursor(i);
						throw BAD_STATE.createWithContext(reader, id.toString() + " " + s);
					}

					if (properties.containsKey(property)) {
						reader.setCursor(i);
						throw BAD_STATE.createWithContext(reader, id.toString() + " " + s);
					}

					reader.skipWhitespace();
					if (!reader.canRead() || reader.peek() != '=') {
						throw BAD_STATE.createWithContext(reader, id.toString() + " " + s);
					}

					reader.skip();
					reader.skipWhitespace();

					Optional<V> optional = (Optional<V>) property.parseValue(s);
					if (optional.isPresent()) {
					   state = state.with(property, optional.get());
					   properties.put(property, optional.get());
					} else {
						throw BAD_STATE.createWithContext(reader, id.toString() + " " + s);
					}

					reader.skipWhitespace();
					if (!reader.canRead()) {
						continue;
					}

					if (reader.peek() == ',') {
						reader.skip();
						continue;
					}

					if (reader.peek() != ']') {
						throw BAD_STATE.createWithContext(reader, id.toString() + " " + s);
					}
				}

				if (reader.canRead()) {
					reader.skip();
					continue;
				}

				throw BAD_STATE.createWithContext(reader, id.toString());
			}
		}

		return state;
	}

}

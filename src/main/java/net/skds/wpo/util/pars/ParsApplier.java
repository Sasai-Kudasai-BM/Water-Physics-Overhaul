package net.skds.wpo.util.pars;

import static net.skds.wpo.WPO.LOGGER;

import java.util.Set;

import net.minecraft.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.skds.core.api.IBlockExtended;
import net.skds.core.util.CustomBlockPars;

public class ParsApplier {

	public static void applyFluidPars(ParsGroup<FluidPars> FG) {
		for (Block b : FG.blocks) {
			CustomBlockPars pars = ((IBlockExtended) b).getCustomBlockPars();
			pars.put(FG.param);
		}
	}

	public static void refresh() {
		
		JsonConfigReader reader = new JsonConfigReader();
		reader.run();

		long t0 = System.currentTimeMillis();
		LOGGER.info("Cleaning blocks...");

		ForgeRegistries.BLOCKS.getValues().forEach(block -> {
			((IBlockExtended) block).getCustomBlockPars().clear(FluidPars.class);
		});

		LOGGER.info("Reading fluid configs...");

		reader.FP.forEach((name, pars) -> {
			applyFluidPars(pars);
		});

		LOGGER.info("Configs reloaded in " + (System.currentTimeMillis() - t0) + "ms");

		//System.out.println(reader.BFP);
	}

	public static class ParsGroup<A> {
		public final Set<Block> blocks;
		public final A param;

		ParsGroup(A p, Set<Block> blockList) {
			param = p;
			blocks = blockList;
		}
	}
}
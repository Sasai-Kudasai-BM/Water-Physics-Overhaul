package net.skds.wpo.fluidphysics;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.ILiquidContainer;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.block.PistonBlockStructureHelper;
import net.minecraft.block.SpongeBlock;
import net.minecraft.block.WetSpongeBlock;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.WaterFluid;
import net.minecraft.item.FishBucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.PistonEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.skds.wpo.WPOConfig;
import net.skds.wpo.registry.BlockStateProps;
import net.skds.wpo.util.ExtendedFHIS;
import net.skds.wpo.util.interfaces.IBaseWL;
import net.skds.core.api.IBlockExtended;
import net.skds.core.api.IWorldExtended;
import net.skds.core.util.blockupdate.WWSGlobal;
import net.skds.wpo.util.pars.FluidPars;

public class FFluidStatic {

	public final static int FCONST = 1000 / WPOConfig.MAX_FLUID_LEVEL;

	// ================ UTIL ================== //

	public static Direction[] getRandomizedDirections(Random r, boolean addVertical) {

		Direction[] dirs = new Direction[4];

		if (addVertical) {
			dirs = new Direction[6];
			dirs[4] = Direction.DOWN;
			dirs[5] = Direction.UP;
		}
		int i0 = r.nextInt(4);
		for (int index = 0; index < 4; ++index) {
			Direction dir = Direction.byHorizontalIndex((index + i0) % 4);
			dirs[index] = dir;
		}

		return dirs;
	}

	public static Direction[] getAllRandomizedDirections(Random r) {

		Direction[] dirs = new Direction[6];

		int i0 = r.nextInt(6);
		for (int index = 0; index < 6; ++index) {
			Direction dir = Direction.byIndex((index + i0) % 6);
			dirs[index] = dir;
		}

		return dirs;
	}

	public static BlockState getUpdatedState(BlockState state0, int newlevel, Fluid fluid) {
		if ((newlevel < 0) || (newlevel > WPOConfig.MAX_FLUID_LEVEL)) {
			throw new RuntimeException("Incorrect fluid level!!!");
		}
		if (FFluidStatic.canOnlyFullCube(state0) && fluid instanceof WaterFluid) {
			if (newlevel >= 1) {
				return state0.with(BlockStateProperties.WATERLOGGED, true);
			} else {
				return state0.with(BlockStateProperties.WATERLOGGED, false);
			}
		}
		if (state0.getBlock() instanceof IBaseWL && fluid instanceof WaterFluid) {
			if (newlevel >= 1) {
				return state0.with(BlockStateProperties.WATERLOGGED, true).with(BlockStateProps.FFLUID_LEVEL, newlevel);
			} else {
				return state0.with(BlockStateProperties.WATERLOGGED, false).with(BlockStateProps.FFLUID_LEVEL,
						newlevel);
			}
		}
		FluidState fs2;
		if (newlevel >= WPOConfig.MAX_FLUID_LEVEL) {
			fs2 = ((FlowingFluid) fluid).getStillFluidState(false);
		} else if (newlevel <= 0) {
			fs2 = Fluids.EMPTY.getDefaultState();
		} else {
			fs2 = ((FlowingFluid) fluid).getFlowingFluidState(newlevel, false);
		}
		return fs2.getBlockState();
	}

	public static float getHeight(int level) {
		float h = ((float) level / WPOConfig.MAX_FLUID_LEVEL) * 0.9375F;
		switch (level) {
			case 3:
				return h * 0.9F;
			case 2:
				return h * 0.75F;
			case 1:
				return h * 0.4F;
			default:
				return h;
		}
	}

	public static PushReaction getPushReaction(BlockState state) {
		return PushReaction.PUSH_ONLY;
	}

	public static boolean isSameFluid(Fluid f1, Fluid f2) {
		if (f1 == Fluids.EMPTY)
			return false;
		if (f2 == Fluids.EMPTY)
			return false;
		return f1.isEquivalentTo(f2);
	}

	public static int getTickRate(FlowingFluid fluid, IWorldReader w) {
		int rate = fluid.getTickRate(w);
		rate /= 2;
		//System.out.println(rate);
		return rate > 0 ? rate : 1;
	}

	public static Direction dirFromVec(BlockPos pos, BlockPos pos2) {
		return Direction.getFacingFromVector(pos2.getX() - pos.getX(), pos2.getY() - pos.getY(),
				pos2.getZ() - pos.getZ());
	}

	// ================ OTHER ================== //

	public static Vector3d getVel2(IBlockReader w, BlockPos posV, FluidState state) {

		Vector3d vel = new Vector3d(0, 0, 0);
		int level = state.getLevel();
		Iterator<Direction> iter = Direction.Plane.HORIZONTAL.iterator();

		while (iter.hasNext()) {
			Direction dir = (Direction) iter.next();
			BlockPos pos2 = posV.offset(dir);

			BlockState st = w.getBlockState(pos2);
			FluidState fluidState = st.getFluidState();
			if (!fluidState.isEmpty() && canReach(w, posV, dir.getOpposite())) {
				int lvl0 = fluidState.getLevel();
				FluidState f2 = w.getFluidState(pos2.up());
				if (isSameFluid(state.getFluid(), f2.getFluid())) {
					lvl0 += f2.getLevel();
				}
				int delta = level - lvl0;
				if (delta > 1 || delta < -1) {
					Vector3i v3i = dir.getDirectionVec();
					vel = vel.add(v3i.getX() * delta, 0, v3i.getZ() * delta);
				}
			}
			// vel.multiply((double) 1D/n);
		}
		return vel.normalize();
	}

	public static Vector3d getVel(IBlockReader w, BlockPos pos, FluidState fs) {

		Vector3d vel = new Vector3d(0, 0, 0);
		int level = fs.getLevel();
		BlockState state = fs.getBlockState();
		Fluid fluid = fs.getFluid();
		BlockPos posu = pos.up();

		boolean flag = false;

		BlockState stateu = w.getBlockState(posu);

		if (canReach(pos, posu, state, stateu, fluid, w) && !stateu.getFluidState().isEmpty()) {
			level += stateu.getFluidState().getLevel();
			flag = true;
		}

		for (Direction dir : Direction.Plane.HORIZONTAL) {
			BlockPos pos2 = pos.offset(dir);

			BlockState state2 = w.getBlockState(pos2);
			FluidState fs2 = state2.getFluidState();

			if (!fs2.isEmpty() && canReach(pos, pos2, state, state2, fluid, w)) {
				int lvl2 = fs2.getLevel();
				if (flag) {
					FluidState fs2u = w.getFluidState(pos2.up());
					if (isSameFluid(fluid, fs2u.getFluid())) {
						lvl2 += fs2u.getLevel();
					}
				}
				int delta = level - lvl2;
				if (delta > 1 || delta < -1) {
					Vector3i v3i = dir.getDirectionVec();
					vel = vel.add(v3i.getX() * delta, 0, v3i.getZ() * delta);
				}
			}
			// vel.multiply((double) 1D/n);
		}
		return vel.normalize();
	}

	// ================ RENDERER ================== //

	public static float[] getConH(IBlockReader w, BlockPos pos, Fluid fluid) {
		int[] count = new int[] { 1, 1, 1, 1 };
		boolean[] conner = new boolean[4];
		boolean[] setconner = new boolean[4];
		float[] setconnervl = new float[4];
		// boolean downtry = false;
		boolean downsuc = false;

		float offset = 0.0036F;
		float offset2 = 0.99999F;

		BlockPos posd = null;
		BlockState stated = null;

		BlockState state = w.getBlockState(pos);
		float level = state.getFluidState().getHeight();
		float[] sum = new float[] { level, level, level, level };

		BlockPos posu = pos.up();
		BlockState statu = w.getBlockState(posu);
		FluidState ufs = w.getFluidState(posu);

		boolean posus = canReach(pos, posu, state, statu, fluid, w);

		if (fluid.isEquivalentTo(ufs.getFluid()) && posus) {
			return new float[] { 1.0F, 1.0F, 1.0F, 1.0F };
		}

		posd = pos.down();
		stated = w.getBlockState(posd);
		downsuc = (stated.getFluidState().getFluid().isEquivalentTo(fluid));

		if (posus) {
			offset2 = 1.0F;
		}

		if (downsuc) {
			offset = 0.0F;
		}

		// int n = -1;
		Direction dir = Direction.EAST;
		for (int n = 0; n < 4; n++) {
			dir = dir.rotateYCCW();
			// ++n;
			int n2 = n > 0 ? n - 1 : 3;
			BlockPos pos2 = pos.offset(dir);
			BlockState state2 = w.getBlockState(pos2);

			boolean reach2 = canReach(pos, pos2, state, state2, fluid, w);
			boolean same2 = state2.getFluidState().getFluid().isEquivalentTo(fluid);
			if (same2 && reach2) {

				BlockPos pos2u = pos2.up();
				BlockState state2u = w.getBlockState(pos2u);
				if (state2u.getFluidState().getFluid().isEquivalentTo(fluid)
						&& canReach(pos2, pos2u, state2, state2u, fluid, w)) {
					conner[n] = true;
					conner[n2] = true;
					setconner[n] = true;
					setconner[n2] = true;
					setconnervl[n] = offset2;
					setconnervl[n2] = offset2;
				} else {
					float level2 = state2.getFluidState().getHeight();
					sum[n] += level2;
					sum[n2] += level2;
					count[n]++;
					count[n2]++;
				}
				Direction[] dirside = new Direction[2];
				dirside[0] = dir.rotateY();
				dirside[1] = dir.rotateYCCW();

				for (int i = 0; i < 2; i++) {
					if (i == 0 && (conner[n2])) {
						continue;
					}
					if (i == 1 && (conner[n])) {
						continue;
					}
					BlockPos pos2dir = pos2.offset(dirside[i]);
					BlockState state2dir = w.getBlockState(pos2dir);
					if (canReach(pos2, pos2dir, state2, state2dir, fluid, w)) {

						if (state2dir.getFluidState().getFluid().isEquivalentTo(fluid)) {

							BlockPos pos2diru = pos2dir.up();
							BlockState state2diru = w.getBlockState(pos2diru);
							if (state2diru.getFluidState().getFluid().isEquivalentTo(fluid)
									&& canReach(pos2dir, pos2diru, state2dir, state2diru, fluid, w)) {
								if (i == 0) {
									setconnervl[n2] = offset2;
									setconner[n2] = true;
									conner[n2] = true;
								} else {
									setconnervl[n] = offset2;
									setconner[n] = true;
									conner[n] = true;
								}
							} else {
								float level2dir = state2dir.getFluidState().getHeight();
								if (i == 0) {
									sum[n2] += level2dir;
									count[n2]++;
									conner[n2] = true;
								} else {
									sum[n] += level2dir;
									count[n]++;
									conner[n] = true;
								}
							}

						} else if (state2dir.getFluidState().isEmpty()) {
							BlockPos pos2dird = pos2dir.down();
							BlockState state2dird = w.getBlockState(pos2dird);
							if (state2dird.getFluidState().getFluid().isEquivalentTo(fluid)
									&& canReach(pos2dir, pos2dird, state2dir, state2dird, fluid, w)) {
								if (i == 0) {
									if (!setconner[n2])
										setconnervl[n2] = offset;
									setconner[n2] = true;
									conner[n2] = true;
								} else {
									if (!setconner[n2])
										setconnervl[n] = offset;
									setconner[n] = true;
									conner[n] = true;
								}
							}
						}
					}
				}
			} else {

				if (reach2) {
					BlockPos pos2d = pos2.down();
					BlockState state2d = w.getBlockState(pos2d);
					if (state2d.getFluidState().getFluid().isEquivalentTo(fluid)
							&& canReach(pos2, pos2d, state2, state2d, fluid, w)) {
						if (!setconner[n]) {
							setconner[n] = true;
							setconnervl[n] = offset;
						}
						if (!setconner[n2]) {
							setconner[n2] = true;
							setconnervl[n2] = offset;
						}
					}
				}
			}
		}

		float[] ch = new float[4];
		for (int i = 0; i < 4; i++) {
			if (setconner[i]) {
				ch[i] = setconnervl[i];
			} else {
				ch[i] = (float) sum[i] / count[i];
			}
		}
		return ch;
	}

	public static float getConH(IBlockReader w, BlockPos p, Fluid f, BlockPos dir) {
		// p = p.add(-dir.getX(), 0, -dir.getZ());
		// Blockreader w = (Blockreader) wi;
		BlockPos pu = p.up();
		FluidState ufs = w.getFluidState(pu);
		if (!ufs.isEmpty() && isSameFluid(ufs.getFluid(), f)) {
			return 1.0f;
		}
		FluidState fsm = w.getFluidState(p);

		float sl = fsm.getHeight();
		int i = 1;
		BlockPos dp = p.add(dir.getX(), 0, 0);
		BlockPos dp2 = p.add(0, 0, dir.getZ());
		FluidState dfs = w.getFluidState(dp);
		FluidState dfs2 = w.getFluidState(dp2);

		boolean s = false;

		if (!dfs.isEmpty() && isSameFluid(dfs.getFluid(), f)) {
			pu = dp.up();
			ufs = w.getFluidState(pu);
			if (!ufs.isEmpty() && isSameFluid(ufs.getFluid(), f)) {
				return 1.0f;
			}

			sl += dfs.getHeight();
			i++;
			s = true;
		} else if (dfs.isEmpty() && canReach(w, p, Direction.getFacingFromVector(dir.getX(), 0, 0))) {
			BlockPos downp = dp.down();
			FluidState downfs = w.getFluidState(downp);
			if (!downfs.isEmpty() && isSameFluid(downfs.getFluid(), f) && downfs.getHeight() == 1.0F) {
				return 0.0F;
			}
		}

		if (!dfs2.isEmpty() && isSameFluid(dfs2.getFluid(), f)) {
			pu = dp2.up();
			ufs = w.getFluidState(pu);
			if (!ufs.isEmpty() && isSameFluid(ufs.getFluid(), f)) {
				return 1.0f;
			}

			sl += dfs2.getHeight();
			i++;
			s = true;
		} else if (dfs2.isEmpty() && canReach(w, p, Direction.getFacingFromVector(0, 0, dir.getZ()))) {
			BlockPos downp = dp2.down();
			FluidState downfs = w.getFluidState(downp);
			if (!downfs.isEmpty() && isSameFluid(downfs.getFluid(), f) && downfs.getHeight() == 1.0F) {
				return 0.0F;
			}
		}

		if (s) {
			BlockPos dp3 = p.add(dir);
			FluidState dfs3 = w.getFluidState(dp3);

			if (!dfs3.isEmpty() && isSameFluid(dfs3.getFluid(), f)) {
				pu = dp3.up();
				ufs = w.getFluidState(pu);
				if (!ufs.isEmpty() && isSameFluid(ufs.getFluid(), f)) {
					return 1.0f;
				}

				sl += dfs3.getHeight();
				i++;
			} else if (dfs3.isEmpty()) {
				BlockPos downp = dp3.down();
				FluidState downfs = w.getFluidState(downp);
				if (!downfs.isEmpty() && isSameFluid(downfs.getFluid(), f) && downfs.getHeight() == 1.0F
						&& canReach(w, dp3, Direction.getFacingFromVector(0, 1, 0))) {
					return 0.0F;
				}
			}
		}
		return sl /= i;
	}

	// ================= UTIL ================== //
	private static boolean canReach(IBlockReader world, BlockPos pos, Direction direction) {
		BlockState state1 = world.getBlockState(pos);
		BlockState state2 = world.getBlockState(pos.offset(direction));
		if (state2.isSolid() && !(state2.getBlock() instanceof IWaterLoggable)) {
			return false;
		}
		VoxelShape voxelShape2 = state2.getCollisionShape(world, pos.offset(direction));
		VoxelShape voxelShape1 = state1.getCollisionShape(world, pos);
		if (voxelShape1.isEmpty() && voxelShape2.isEmpty()) {
			return true;
		}
		return !VoxelShapes.doAdjacentCubeSidesFillSquare(voxelShape1, voxelShape2, direction);
	}

	public static boolean canReach(BlockPos pos1, BlockPos pos2, BlockState state1, BlockState state2, Fluid fluid,
			IBlockReader w) {

		Fluid f2 = state2.getFluidState().getFluid();
		if (f2.isEquivalentTo(fluid) && state1.getBlock() instanceof FlowingFluidBlock
				&& state2.getBlock() instanceof FlowingFluidBlock) {
			return true;
		}

		FluidPars fp2 = (FluidPars) ((IBlockExtended) state2.getBlock()).getCustomBlockPars().get(FluidPars.class);
		FluidPars fp1 = (FluidPars) ((IBlockExtended) state1.getBlock()).getCustomBlockPars().get(FluidPars.class);
		boolean posos = false;
		if (fp1 != null) {
			if (fp1.isPassable == 1) {
				posos = true;
			}
		}
		Direction dir = dirFromVec(pos1, pos2);
		if (fp2 != null) {
			if (fp2.isPassable == 1) {
				// System.out.println(state2);
				return true;
			} else if (fp2.isPassable == -1) {
				return false;
			}
			if ((state2.getFluidState().isEmpty() || state1.getFluidState().canDisplace(w, pos1, f2, dir))
					&& fp2.isDestroyableBy(fluid))
				return true;
		}

		if (state2.isSolid() && !posos && !(state2.getBlock() instanceof IWaterLoggable)) {
			return false;
		}
		if (!(fluid instanceof WaterFluid)
				&& (state1.getBlock() instanceof IWaterLoggable || state2.getBlock() instanceof IWaterLoggable)) {
			return false;
		}
		VoxelShape voxelShape2 = state2.getCollisionShape(w, pos2);
		VoxelShape voxelShape1 = state1.getCollisionShape(w, pos1);
		if ((voxelShape1.isEmpty() || posos) && voxelShape2.isEmpty()) {
			return true;
		}
		return !VoxelShapes.doAdjacentCubeSidesFillSquare(voxelShape1, voxelShape2, dir);
	}

	public static boolean canOnlyFullCube(BlockState bs) {
		return canOnlyFullCube(bs.getBlock());
	}

	public static boolean canOnlyFullCube(Block b) {
		return (b instanceof IWaterLoggable) ? !(b instanceof IBaseWL) : false;
	}

	// ================= ITEMS ==================//

	public static void onBucketEvent(FillBucketEvent e) {

		FishBucketItem fishItem = null;
		ItemStack bucket = e.getEmptyBucket();
		Item bu = bucket.getItem();
		if (bu instanceof FishBucketItem) {
			fishItem = (FishBucketItem) bu;
		}
		Optional<IFluidHandlerItem> op = bucket.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY)
				.resolve();
		IFluidHandlerItem bh;
		if (op.isPresent()) {
			bh = op.get();
		} else {
			bh = new ExtendedFHIS(bucket, 1000);
			// System.out.println("l;hhhhhhh " + bh);
		}
		Fluid f = bh.getFluidInTank(0).getFluid();
		if (!(f instanceof FlowingFluid) && f != Fluids.EMPTY) {
			return;
		}
		PlayerEntity p = e.getPlayer();
		World w = e.getWorld();
		RayTraceResult targ0 = e.getTarget();
		RayTraceResult targ = rayTrace(w, p,
				f == Fluids.EMPTY ? RayTraceContext.FluidMode.ANY : RayTraceContext.FluidMode.NONE);
		targ0 = targ;
		if (targ.getType() != RayTraceResult.Type.BLOCK) {
			return;
		}
		BlockRayTraceResult targB = (BlockRayTraceResult) targ;
		BlockPos pos = targB.getPos();
		BlockState bs = w.getBlockState(pos);
		FluidState fs = bs.getFluidState();
		if (fs.isEmpty() && f != Fluids.EMPTY && !(bs.getBlock() instanceof IWaterLoggable)) {
			pos = pos.offset(targB.getFace());
			bs = w.getBlockState(pos);
			fs = bs.getFluidState();
		} 
		if (!w.isRemote && f != Fluids.EMPTY && bs.getBlock() instanceof IWaterLoggable) {
			FluidTasksManager.addFluidTask((ServerWorld) w, pos, bs);
		}
		Fluid fluid = fs.getFluid();
		if ((!f.isEquivalentTo(Fluids.WATER) && f != Fluids.EMPTY) && bs.getBlock() instanceof IWaterLoggable) {

			return;
		}

		if (!(w.isBlockModifiable(p, pos) && p.canPlayerEdit(pos, targB.getFace(), bh.getContainer()))) {
			return;
		}

		if (f == Fluids.EMPTY) {
			if (!(fluid instanceof FlowingFluid)) {
				return;
			}
			if (targ0.getType() == RayTraceResult.Type.BLOCK) {
				BlockRayTraceResult targB0 = (BlockRayTraceResult) targ0;
				FluidState fs0 = w.getFluidState(targB0.getPos());
				if (fs0.isSource()) {
					return;
				}
			}
			BucketFiller filler = new BucketFiller(w, fluid, bh, e);
			iterateFluidWay(WPOConfig.COMMON.maxBucketDist.get(), pos, filler);

		} else {
			if (!f.isEquivalentTo(fluid) && fluid != Fluids.EMPTY) {
				e.setCanceled(true);
				return;
			}
			BucketFlusher flusher = new BucketFlusher(w, f, bh, e);
			if (iterateFluidWay(WPOConfig.COMMON.maxBucketDist.get(), pos, flusher) && fishItem != null) {
				fishItem.onLiquidPlaced(w, bucket, pos);
			}			
		}
	}

	public static BlockRayTraceResult rayTrace(World worldIn, PlayerEntity player,
			RayTraceContext.FluidMode fluidMode) {
		float f = player.rotationPitch;
		float f1 = player.rotationYaw;
		Vector3d vector3d = player.getEyePosition(1.0F);
		float f2 = MathHelper.cos(-f1 * ((float) Math.PI / 180F) - (float) Math.PI);
		float f3 = MathHelper.sin(-f1 * ((float) Math.PI / 180F) - (float) Math.PI);
		float f4 = -MathHelper.cos(-f * ((float) Math.PI / 180F));
		float f5 = MathHelper.sin(-f * ((float) Math.PI / 180F));
		float f6 = f3 * f4;
		float f7 = f2 * f4;
		double d0 = player.getAttribute(net.minecraftforge.common.ForgeMod.REACH_DISTANCE.get()).getValue();
		Vector3d vector3d1 = vector3d.add((double) f6 * d0, (double) f5 * d0, (double) f7 * d0);
		return worldIn.rayTraceBlocks(
				new RayTraceContext(vector3d, vector3d1, RayTraceContext.BlockMode.OUTLINE, fluidMode, player));
	}

	public static boolean iterateFluidWay(int maxRange, BlockPos pos, IFluidActionIteratable actioner) {
		boolean frst = true;
		boolean client = false;
		World w = actioner.getWorld();
		WWSGlobal wws = ((IWorldExtended) w).getWWS();
		Set<BlockPos> setBan = new HashSet<>();
		Set<BlockPos> setAll = new HashSet<>();
		client = wws == null;
		if (!client && setBan.add(pos) && !wws.banPos(pos.toLong())) {
			setBan.forEach(p -> wws.banPos(p.toLong()));
			///wws.unbanPoses(setBan);
			return false;
		}
		setAll.add(pos);
		Set<BlockPos> setLocal = new HashSet<>();
		actioner.addZero(setLocal, pos);
		int n = maxRange;
		while (n > 0 && !actioner.isComplete() && !setLocal.isEmpty()) {
			--n;
			Set<BlockPos> setLocal2 = new HashSet<>();
			for (BlockPos posn : setLocal) {
				if (frst) {
					frst = false;
					setAll.add(posn);
					BlockState bs = w.getBlockState(posn);
					if (!client && setBan.add(posn) && !wws.banPos(posn.toLong())) {
						//wws.unbanPoses(setBan);
						setBan.forEach(p -> wws.unbanPos(p.toLong()));
						return false;
					}
					actioner.run(posn, bs);
					// w.addParticle(ParticleTypes.CLOUD, posn.getX() + 0.5, posn.getY() + 0.5,
					// posn.getZ() + 0.5, 0, 0, 0);
				}
				if (actioner.isComplete()) {
					break;
				}
				for (Direction dir : getRandomizedDirections(w.getRandom(), true)) {
					BlockPos pos2 = posn.offset(dir);
					if (setAll.contains(pos2)) {
						continue;
					}
					BlockState bs2 = w.getBlockState(pos2);
					boolean cr = canReach(w, posn, dir);
					boolean eq = actioner.isValidState(bs2);
					if (cr && eq) {
						setLocal2.add(pos2);
						if (actioner.isValidPos(pos2)) {
							if (!client && setBan.add(pos2) && !wws.banPos(pos2.toLong())) {
								//wws.unbanPoses(setBan);
								setBan.forEach(p -> wws.unbanPos(p.toLong()));
								return false;
							}
							actioner.run(pos2, bs2);
						}
						// w.addParticle(ParticleTypes.CLOUD, pos2.getX() + 0.5, pos2.getY() + 0.5,
						// pos2.getZ() + 0.5, 0, 0, 0);
					}
					// if (!eq) {

					setAll.add(pos2);
					// }
					if (actioner.isComplete()) {
						break;
					}
				}
			}
			setLocal = setLocal2;
		}
		// if (!client) {
		// for (BlockPos p : setAll) {
		// if (!wws.banPos(p)) {
		// wws.unbanPoses(setAll);
		// System.out.println(p);
		// return false;
		// }
		// }
		//
		// }
		if (actioner.isComplete()) {
			actioner.finish();
			if (!client)
				//wws.unbanPoses(setBan);
				setBan.forEach(p -> wws.unbanPos(p.toLong()));
			return true;
		} else {
			actioner.fail();
			if (!client)
				//wws.unbanPoses(setBan);
				setBan.forEach(p -> wws.unbanPos(p.toLong()));
			return false;
		}
	}

	private static class BucketFiller implements IFluidActionIteratable {

		int bucketLevels = WPOConfig.MAX_FLUID_LEVEL;
		int sl = 0;
		boolean complete = false;
		World world;
		Fluid fluid;
		FillBucketEvent event;
		IFluidHandlerItem bucket;
		Long2ObjectLinkedOpenHashMap<BlockState> states = new Long2ObjectLinkedOpenHashMap<>();

		BucketFiller(World w, Fluid f, IFluidHandlerItem b, FillBucketEvent e) {
			world = w;
			fluid = f;
			bucket = b;
			event = e;
		}

		@Override
		public boolean isComplete() {
			return complete;
		}

		@Override
		public void run(BlockPos pos, BlockState state) {
			// world.addParticle(ParticleTypes.CLOUD, pos.getX() + 0.5, pos.getY() + 0.5,
			// pos.getZ() + 0.5, 0, 0, 0);

			if (canOnlyFullCube(state) && state.get(BlockStateProperties.WATERLOGGED)) {
				states.clear();
				states.put(pos.toLong(), getUpdatedState(state, 0, fluid));
				complete = true;
				return;
			}
			FluidState fs = state.getFluidState();
			int l = fs.getLevel();
			sl += l;
			int nl = 0;
			if (sl >= bucketLevels) {
				nl = sl - bucketLevels;
				complete = true;
			}
			states.put(pos.toLong(), getUpdatedState(state, nl, fluid));
		}

		@Override
		public World getWorld() {
			return world;
		}

		@Override
		public boolean isValidState(BlockState state) {
			return fluid.isEquivalentTo(state.getFluidState().getFluid());
		}

		@Override
		public void fail() {
			// System.out.println(sl);

			fillStates(states, world);
			event.setResult(Result.ALLOW);
			PlayerEntity p = event.getPlayer();
			Item item = bucket.getContainer().getItem();
			p.addStat(Stats.ITEM_USED.get(item));
			SoundEvent soundevent = fluid.getAttributes().getFillSound();
			if (soundevent == null)
				soundevent = fluid.isIn(FluidTags.LAVA) ? SoundEvents.ITEM_BUCKET_FILL_LAVA
						: SoundEvents.ITEM_BUCKET_FILL;
			p.playSound(soundevent, 1.0F, 1.0F);

			if (!p.abilities.isCreativeMode) {
				ItemStack stack = new ItemStack(net.skds.wpo.registry.Items.ADVANCED_BUCKET.get());
				ExtendedFHIS st2 = new ExtendedFHIS(stack, 1000);
				Fluid f2 = fluid instanceof FlowingFluid ? ((FlowingFluid) fluid).getStillFluid() : fluid;
				FluidStack fluidStack = new FluidStack(f2, sl * FFluidStatic.FCONST);
				st2.fill(fluidStack, FluidAction.EXECUTE);

				stack = st2.getContainer();
				event.setFilledBucket(stack);
			}
		}

		@Override
		public void finish() {
			fillStates(states, world);

			event.setResult(Result.ALLOW);
			PlayerEntity p = event.getPlayer();
			Item item = bucket.getContainer().getItem();
			p.addStat(Stats.ITEM_USED.get(item));
			SoundEvent soundevent = fluid.getAttributes().getFillSound();
			if (soundevent == null)
				soundevent = fluid.isIn(FluidTags.LAVA) ? SoundEvents.ITEM_BUCKET_FILL_LAVA
						: SoundEvents.ITEM_BUCKET_FILL;
			p.playSound(soundevent, 1.0F, 1.0F);
			if (!p.abilities.isCreativeMode) {
				// bucket.fill(new FluidStack(fluid, 1000), FluidAction.EXECUTE);
				event.setFilledBucket(new ItemStack(fluid.getFilledBucket()));
			}
		}
	}

	private static class BottleFiller implements IFluidActionIteratable {

		int bucketLevels = 3;
		int sl = 0;
		boolean complete = false;
		World world;
		ItemStack bottle;
		Fluid fluid;
		CallbackInfoReturnable<ActionResult<ItemStack>> ci;
		Long2ObjectLinkedOpenHashMap<BlockState> states = new Long2ObjectLinkedOpenHashMap<>();

		BottleFiller(World w, Fluid f, CallbackInfoReturnable<ActionResult<ItemStack>> ci, ItemStack stack) {
			world = w;
			fluid = f;
			bottle = stack;
			this.ci = ci;
		}

		@Override
		public boolean isComplete() {
			return complete;
		}

		@Override
		public void run(BlockPos pos, BlockState state) {
			// world.addParticle(ParticleTypes.CLOUD, pos.getX() + 0.5, pos.getY() + 0.5,
			// pos.getZ() + 0.5, 0, 0, 0);

			if (canOnlyFullCube(state) && state.get(BlockStateProperties.WATERLOGGED)) {
				states.clear();
				states.put(pos.toLong(), getUpdatedState(state, 0, fluid));
				complete = true;
				return;
			}
			FluidState fs = state.getFluidState();
			int l = fs.getLevel();
			int osl = sl;
			sl += l;
			int nl = 0;
			if (sl >= bucketLevels) {
				nl = sl - bucketLevels;
				complete = true;
			}
			if (osl != sl)
				states.put(pos.toLong(), getUpdatedState(state, nl, fluid));
		}

		@Override
		public World getWorld() {
			return world;
		}

		@Override
		public boolean isValidState(BlockState state) {
			return fluid.isEquivalentTo(state.getFluidState().getFluid());
		}

		@Override
		public void finish() {
			fillStates(states, world);
		}

		@Override
		public void fail() {
			ci.setReturnValue(ActionResult.resultFail(bottle));
		}
	}

	private static class BucketFlusher implements IFluidActionIteratable {

		int mfl = WPOConfig.MAX_FLUID_LEVEL;
		int bucketLevels = WPOConfig.MAX_FLUID_LEVEL;
		int sl = bucketLevels;
		boolean complete = false;
		World world;
		Fluid fluid;
		FillBucketEvent event;
		IFluidHandlerItem bucket;
		Long2ObjectLinkedOpenHashMap<BlockState> states = new Long2ObjectLinkedOpenHashMap<>();

		BucketFlusher(World w, Fluid f, IFluidHandlerItem b, FillBucketEvent e) {
			world = w;
			fluid = f;
			bucket = b;
			event = e;
			sl = bucket.getFluidInTank(0).getAmount() / FFluidStatic.FCONST;
			fluid = bucket.getFluidInTank(0).getFluid();
		}

		@Override
		public boolean isComplete() {
			return complete;
		}

		@Override
		public void run(BlockPos pos, BlockState state) {
			// world.addParticle(ParticleTypes.CLOUD, pos.getX() + 0.5, pos.getY() + 0.5,
			// pos.getZ() + 0.5, 0, 0, 0);

			if (canOnlyFullCube(state) && state.hasProperty(BlockStateProperties.WATERLOGGED) && !state.get(BlockStateProperties.WATERLOGGED)) {
				states.clear();
				states.put(pos.toLong(), getUpdatedState(state, mfl, fluid));
				complete = true;
				return;
			}
			FluidState fs = state.getFluidState();
			int el = mfl - fs.getLevel();
			int osl = sl;
			sl -= el;
			int nl = mfl;
			if (sl <= 0) {
				nl = mfl + sl;
				complete = true;
			}
			if (osl != sl)
				states.put(pos.toLong(), getUpdatedState(state, nl, fluid));
		}

		@Override
		public World getWorld() {
			return world;
		}

		@Override
		public boolean isValidState(BlockState state) {
			return fluid.isEquivalentTo(state.getFluidState().getFluid()) || state.getFluidState().isEmpty();
		}

		@Override
		public void finish() {
			fillStates(states, world);

			event.setResult(Result.ALLOW);
			PlayerEntity p = event.getPlayer();
			Item item = bucket.getContainer().getItem();
			p.addStat(Stats.ITEM_USED.get(item));
			SoundEvent soundevent = fluid.getAttributes().getEmptySound();
			if (soundevent == null)
				soundevent = fluid.isIn(FluidTags.LAVA) ? SoundEvents.ITEM_BUCKET_EMPTY_LAVA
						: SoundEvents.ITEM_BUCKET_EMPTY;
			p.playSound(soundevent, 1.0F, 1.0F);
			if (!p.abilities.isCreativeMode) {
				// bucket.fill(FluidStack.EMPTY, FluidAction.EXECUTE);
				// event.setFilledBucket(bucket.getContainer());
				event.setFilledBucket(new ItemStack(Items.BUCKET));
			}
		}
	}

	public static class FluidDisplacer implements IFluidActionIteratable {

		int mfl = WPOConfig.MAX_FLUID_LEVEL;
		// int bucketLevels = PhysEXConfig.MAX_FLUID_LEVEL;
		int sl;
		boolean complete = false;
		World world;
		Fluid fluid;
		BlockEvent.EntityPlaceEvent event;
		Long2ObjectLinkedOpenHashMap<BlockState> states = new Long2ObjectLinkedOpenHashMap<>();
		BlockState obs;

		FluidDisplacer(World w, BlockEvent.EntityPlaceEvent e) {
			obs = e.getBlockSnapshot().getReplacedBlock();
			FluidState ofs = obs.getFluidState();

			fluid = ofs.getFluid();
			sl = ofs.getLevel();
			world = w;
			event = e;
		}

		@Override
		public boolean isComplete() {
			return complete;
		}

		@Override
		public void addZero(Set<BlockPos> set, BlockPos p0) {
			for (Direction d : getRandomizedDirections(world.getRandom(), true)) {
				BlockPos pos2 = p0.offset(d);
				BlockState state2 = world.getBlockState(pos2);
				if (isValidState(state2) && canReach(p0, pos2, obs, state2, fluid, world)) {
					set.add(pos2);
				}
			}
		}

		@Override
		public void run(BlockPos pos, BlockState state) {
			// world.addParticle(ParticleTypes.CLOUD, pos.getX() + 0.5, pos.getY() + 0.5,
			// pos.getZ() + 0.5, 0, 0, 0);

			// if (fb) {
			// fb = false;
			// return;
			// }

			if (canOnlyFullCube(state) && state.hasProperty(BlockStateProperties.WATERLOGGED) && !state.get(BlockStateProperties.WATERLOGGED)) {
				states.clear();
				states.put(pos.toLong(), getUpdatedState(state, mfl, fluid));
				complete = true;
				return;
			}
			FluidState fs = state.getFluidState();
			int el = mfl - fs.getLevel();
			int osl = sl;
			sl -= el;
			int nl = mfl;
			if (sl <= 0) {
				nl = mfl + sl;
				complete = true;
			}
			if (osl != sl)
				states.put(pos.toLong(), getUpdatedState(state, nl, fluid));
		}

		@Override
		public World getWorld() {
			return world;
		}

		@Override
		public boolean isValidState(BlockState state) {
			return fluid.isEquivalentTo(state.getFluidState().getFluid()) || state.getFluidState().isEmpty();
		}

		@Override
		public void finish() {
			fillStates(states, world);
			event.setResult(Result.ALLOW);
		}

		@Override
		public void fail() {
			// event.setCanceled(true);
		}
	}

	public static class FluidDisplacer2 implements IFluidActionIteratable {

		int mfl = WPOConfig.MAX_FLUID_LEVEL;
		// int bucketLevels = PhysEXConfig.MAX_FLUID_LEVEL;
		int sl;
		boolean complete = false;
		World world;
		Fluid fluid;
		Long2ObjectLinkedOpenHashMap<BlockState> states = new Long2ObjectLinkedOpenHashMap<>();
		BlockState obs;

		FluidDisplacer2(World w, BlockState obs) {
			FluidState ofs = obs.getFluidState();
			this.obs = obs;
			fluid = ofs.getFluid();
			sl = ofs.getLevel();
			world = w;
		}

		@Override
		public boolean isComplete() {
			return complete;
		}

		@Override
		public void addZero(Set<BlockPos> set, BlockPos p0) {
			for (Direction d : getRandomizedDirections(world.getRandom(), true)) {
				BlockPos pos2 = p0.offset(d);
				BlockState state2 = world.getBlockState(pos2);
				if (isValidState(state2) && canReach(p0, pos2, obs, state2, fluid, world)) {
					set.add(pos2);
				}
			}
		}

		@Override
		public void run(BlockPos pos, BlockState state) {
			// world.addParticle(ParticleTypes.CLOUD, pos.getX() + 0.5, pos.getY() + 0.5,
			// pos.getZ() + 0.5, 0, 0, 0);

			// if (fb) {
			// fb = false;
			// return;
			// }

			if (canOnlyFullCube(state) && state.hasProperty(BlockStateProperties.WATERLOGGED) && !state.get(BlockStateProperties.WATERLOGGED)) {
				states.clear();
				states.put(pos.toLong(), getUpdatedState(state, mfl, fluid));
				complete = true;
				return;
			}
			FluidState fs = state.getFluidState();
			int el = mfl - fs.getLevel();
			int osl = sl;
			sl -= el;
			int nl = mfl;
			if (sl <= 0) {
				nl = mfl + sl;
				complete = true;
			}
			if (osl != sl)
				states.put(pos.toLong(), getUpdatedState(state, nl, fluid));
		}

		@Override
		public World getWorld() {
			return world;
		}

		@Override
		public boolean isValidState(BlockState state) {
			return fluid.isEquivalentTo(state.getFluidState().getFluid()) || state.getFluidState().isEmpty();
		}

		@Override
		public void finish() {
			fillStates(states, world);
		}

		@Override
		public void fail() {
			// event.setCanceled(true);
		}
	}

	private static class PistonDisplacer implements IFluidActionIteratable {

		int mfl = WPOConfig.MAX_FLUID_LEVEL;
		// int bucketLevels = PhysEXConfig.MAX_FLUID_LEVEL;
		int sl;
		boolean complete = false;
		World world;
		Fluid fluid;
		// PistonBlockStructureHelper ps;
		Set<BlockPos> movepos = new HashSet<>();
		PistonEvent.Pre event;
		Long2ObjectLinkedOpenHashMap<BlockState> states = new Long2ObjectLinkedOpenHashMap<>();
		BlockState obs;

		PistonDisplacer(World w, PistonEvent.Pre e, BlockState os, PistonBlockStructureHelper ps) {
			this.obs = os;
			FluidState ofs = obs.getFluidState();
			// this.ps = ps;
			this.fluid = ofs.getFluid();
			this.sl = ofs.getLevel();
			this.world = w;
			this.event = e;
			movepos.addAll(ps.getBlocksToDestroy());
			movepos.addAll(ps.getBlocksToMove());
			for (BlockPos p : ps.getBlocksToMove()) {
				movepos.add(p.offset(event.getDirection()));
				// System.out.println(p.offset(event.getDirection()));
			}
		}

		@Override
		public boolean isComplete() {
			return complete;
		}

		@Override
		public void addZero(Set<BlockPos> set, BlockPos p0) {
			for (Direction d : getRandomizedDirections(world.getRandom(), true)) {
				BlockPos pos2 = p0.offset(d);
				BlockState state2 = world.getBlockState(pos2);
				if (isValidState(state2) && canReach(p0, pos2, obs, state2, fluid, world)) {
					set.add(pos2);
				}
			}
		}

		@Override
		public void run(BlockPos pos, BlockState state) {
			// world.addParticle(ParticleTypes.CLOUD, pos.getX() + 0.5, pos.getY() + 0.5,
			// pos.getZ() + 0.5, 0, 0, 0);

			// if (fb) {
			// fb = false;
			// return;
			// }

			if (canOnlyFullCube(state) && state.hasProperty(BlockStateProperties.WATERLOGGED) && !state.get(BlockStateProperties.WATERLOGGED)) {
				states.clear();
				states.put(pos.toLong(), getUpdatedState(state, mfl, fluid));
				complete = true;
				return;
			}
			FluidState fs = state.getFluidState();
			int el = mfl - fs.getLevel();
			int osl = sl;
			sl -= el;
			int nl = mfl;
			if (sl <= 0) {
				nl = mfl + sl;
				complete = true;
			}
			if (osl != sl)
				states.put(pos.toLong(), getUpdatedState(state, nl, fluid));
		}

		@Override
		public World getWorld() {
			return world;
		}

		@Override
		public boolean isValidState(BlockState state) {
			return fluid.isEquivalentTo(state.getFluidState().getFluid()) || state.getFluidState().isEmpty();
		}

		@Override
		public boolean isValidPos(BlockPos pos) {
			return !movepos.contains(pos);
		}

		@Override
		public void finish() {
			fillStates(states, world);
			event.setResult(Result.ALLOW);
			// System.out.println("u");
		}

		@Override
		public void fail() {
			event.setCanceled(true);
			// event.setResult(Result.DENY);
			// System.out.println("x");
		}
	}

	public static void fillStates(Long2ObjectLinkedOpenHashMap<BlockState> states, World world) {
		if (!world.isRemote) {
			states.forEach((lpos, state) -> {
				world.setBlockState(BlockPos.fromLong(lpos), state);
			});
		}
	}

	public interface IFluidActionIteratable {
		default void addZero(Set<BlockPos> set, BlockPos p0) {
			set.add(p0);
		}

		boolean isComplete();

		void run(BlockPos pos, BlockState state);

		World getWorld();

		boolean isValidState(BlockState state);

		default boolean isValidPos(BlockPos pos) {
			return true;
		}

		void finish();

		default void fail() {
		}
	}

	public static void onBottleUse(World w, PlayerEntity p, Hand hand,
			CallbackInfoReturnable<ActionResult<ItemStack>> ci, ItemStack stack) {
		BlockRayTraceResult rt = rayTrace(w, p, RayTraceContext.FluidMode.ANY);
		BlockPos pos = rt.getPos();

		BottleFiller filler = new BottleFiller(w, Fluids.WATER, ci, stack);
		iterateFluidWay(WPOConfig.COMMON.maxBucketDist.get(), pos, filler);
	}

	public static void onBlockPlace(BlockEvent.EntityPlaceEvent e) {
		World w = (World) e.getWorld();
		BlockPos pos = e.getPos();
		BlockState oldState = e.getBlockSnapshot().getReplacedBlock();
		FluidState fs = oldState.getFluidState();
		Fluid f = fs.getFluid();
		BlockState newState = e.getPlacedBlock();
		Block nb = newState.getBlock();
		if (fs.isEmpty() || nb instanceof SpongeBlock || nb instanceof WetSpongeBlock) {
			return;
		}
		if (nb instanceof ILiquidContainer && !(nb instanceof IWaterLoggable)) {
			return;
		}
		if (nb instanceof IWaterLoggable && newState.get(BlockStateProperties.WATERLOGGED)) {
			return;
		}
		if (!canOnlyFullCube(newState) && nb instanceof IBaseWL && f.isEquivalentTo(Fluids.WATER)) {
			newState = getUpdatedState(newState, fs.getLevel(), Fluids.WATER);
			w.setBlockState(pos, newState);
			return;
		}

		FluidDisplacer displacer = new FluidDisplacer(w, e);
		iterateFluidWay(10, e.getPos(), displacer);
	}

	// ======================= PISTONS ======================= //

	public static void onPistonPre(PistonEvent.Pre e) {
		World w = (World) e.getWorld();
		if (w.isRemote || e.isCanceled()) {
			return;
		}
		PistonBlockStructureHelper ps = e.getStructureHelper();

		if (!ps.canMove()) {
			return;
		}
		List<BlockPos> poslist = ps.getBlocksToDestroy();

		for (BlockPos pos : poslist) {
			BlockState state = w.getBlockState(pos);
			FluidState fs = state.getFluidState();
			// System.out.println(state);

			if (!fs.isEmpty()) {

				// System.out.println("jjj");

				PistonDisplacer displacer = new PistonDisplacer(w, e, state, ps);
				if (!iterateFluidWay(12, pos, displacer)) {
					e.setCanceled(true);
				}
			}
		}
	}
}
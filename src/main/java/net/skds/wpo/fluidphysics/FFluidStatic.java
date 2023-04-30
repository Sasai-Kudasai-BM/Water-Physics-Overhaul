package net.skds.wpo.fluidphysics;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.SpongeBlock;
import net.minecraft.world.level.block.WetSpongeBlock;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.WaterFluid;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
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
import net.skds.core.api.IWWSG;
import net.skds.core.api.IWorldExtended;
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
			Direction dir = Direction.from2DDataValue((index + i0) % 4);
			dirs[index] = dir;
		}

		return dirs;
	}

	public static Direction[] getAllRandomizedDirections(Random r) {

		Direction[] dirs = new Direction[6];

		int i0 = r.nextInt(6);
		for (int index = 0; index < 6; ++index) {
			Direction dir = Direction.from3DDataValue((index + i0) % 6);
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
				return state0.setValue(BlockStateProperties.WATERLOGGED, true);
			} else {
				return state0.setValue(BlockStateProperties.WATERLOGGED, false);
			}
		}
		if (state0.getBlock() instanceof IBaseWL && fluid instanceof WaterFluid) {
			if (newlevel >= 1) {
				return state0.setValue(BlockStateProperties.WATERLOGGED, true).setValue(BlockStateProps.FFLUID_LEVEL, newlevel);
			} else {
				return state0.setValue(BlockStateProperties.WATERLOGGED, false).setValue(BlockStateProps.FFLUID_LEVEL,
						newlevel);
			}
		}
		FluidState fs2;
		if (newlevel >= WPOConfig.MAX_FLUID_LEVEL) {
			fs2 = ((FlowingFluid) fluid).getSource(false);
		} else if (newlevel <= 0) {
			fs2 = Fluids.EMPTY.defaultFluidState();
		} else {
			fs2 = ((FlowingFluid) fluid).getFlowing(newlevel, false);
		}
		return fs2.createLegacyBlock();
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
		return f1.isSame(f2);
	}

	public static int getTickRate(FlowingFluid fluid, LevelReader w) {
		int rate = fluid.getTickDelay(w);
		rate /= 2;
		//System.out.println(rate);
		return rate > 0 ? rate : 1;
	}

	public static Direction dirFromVec(BlockPos pos, BlockPos pos2) {
		return Direction.getNearest(pos2.getX() - pos.getX(), pos2.getY() - pos.getY(),
				pos2.getZ() - pos.getZ());
	}

	// ================ OTHER ================== //

	public static Vec3 getVel2(BlockGetter w, BlockPos posV, FluidState state) {

		Vec3 vel = new Vec3(0, 0, 0);
		int level = state.getAmount();
		Iterator<Direction> iter = Direction.Plane.HORIZONTAL.iterator();

		while (iter.hasNext()) {
			Direction dir = (Direction) iter.next();
			BlockPos pos2 = posV.relative(dir);

			BlockState st = w.getBlockState(pos2);
			FluidState fluidState = st.getFluidState();
			if (!fluidState.isEmpty() && canReach(w, posV, dir.getOpposite())) {
				int lvl0 = fluidState.getAmount();
				FluidState f2 = w.getFluidState(pos2.above());
				if (isSameFluid(state.getType(), f2.getType())) {
					lvl0 += f2.getAmount();
				}
				int delta = level - lvl0;
				if (delta > 1 || delta < -1) {
					Vec3i v3i = dir.getNormal();
					vel = vel.add(v3i.getX() * delta, 0, v3i.getZ() * delta);
				}
			}
			// vel.multiply((double) 1D/n);
		}
		return vel.normalize();
	}

	public static Vec3 getVel(BlockGetter w, BlockPos pos, FluidState fs) {

		Vec3 vel = new Vec3(0, 0, 0);
		int level = fs.getAmount();
		BlockState state = fs.createLegacyBlock();
		Fluid fluid = fs.getType();
		BlockPos posu = pos.above();

		boolean flag = false;

		BlockState stateu = w.getBlockState(posu);

		if (canReach(pos, posu, state, stateu, fluid, w) && !stateu.getFluidState().isEmpty()) {
			level += stateu.getFluidState().getAmount();
			flag = true;
		}

		for (Direction dir : Direction.Plane.HORIZONTAL) {
			BlockPos pos2 = pos.relative(dir);

			BlockState state2 = w.getBlockState(pos2);
			FluidState fs2 = state2.getFluidState();

			if (!fs2.isEmpty() && canReach(pos, pos2, state, state2, fluid, w)) {
				int lvl2 = fs2.getAmount();
				if (flag) {
					FluidState fs2u = w.getFluidState(pos2.above());
					if (isSameFluid(fluid, fs2u.getType())) {
						lvl2 += fs2u.getAmount();
					}
				}
				int delta = level - lvl2;
				if (delta > 1 || delta < -1) {
					Vec3i v3i = dir.getNormal();
					vel = vel.add(v3i.getX() * delta, 0, v3i.getZ() * delta);
				}
			}
			// vel.multiply((double) 1D/n);
		}
		return vel.normalize();
	}

	// ================ RENDERER ================== //

	public static float[] getConH(BlockGetter w, BlockPos pos, Fluid fluid) {
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
		float level = state.getFluidState().getOwnHeight();
		float[] sum = new float[] { level, level, level, level };

		BlockPos posu = pos.above();
		BlockState statu = w.getBlockState(posu);
		FluidState ufs = w.getFluidState(posu);

		boolean posus = canReach(pos, posu, state, statu, fluid, w);

		if (fluid.isSame(ufs.getType()) && posus) {
			return new float[] { 1.0F, 1.0F, 1.0F, 1.0F };
		}

		posd = pos.below();
		stated = w.getBlockState(posd);
		downsuc = (stated.getFluidState().getType().isSame(fluid));

		if (posus) {
			offset2 = 1.0F;
		}

		if (downsuc) {
			offset = 0.0F;
		}

		// int n = -1;
		Direction dir = Direction.EAST;
		for (int n = 0; n < 4; n++) {
			dir = dir.getCounterClockWise();
			// ++n;
			int n2 = n > 0 ? n - 1 : 3;
			BlockPos pos2 = pos.relative(dir);
			BlockState state2 = w.getBlockState(pos2);

			boolean reach2 = canReach(pos, pos2, state, state2, fluid, w);
			boolean same2 = state2.getFluidState().getType().isSame(fluid);
			if (same2 && reach2) {

				BlockPos pos2u = pos2.above();
				BlockState state2u = w.getBlockState(pos2u);
				if (state2u.getFluidState().getType().isSame(fluid)
						&& canReach(pos2, pos2u, state2, state2u, fluid, w)) {
					conner[n] = true;
					conner[n2] = true;
					setconner[n] = true;
					setconner[n2] = true;
					setconnervl[n] = offset2;
					setconnervl[n2] = offset2;
				} else {
					float level2 = state2.getFluidState().getOwnHeight();
					sum[n] += level2;
					sum[n2] += level2;
					count[n]++;
					count[n2]++;
				}
				Direction[] dirside = new Direction[2];
				dirside[0] = dir.getClockWise();
				dirside[1] = dir.getCounterClockWise();

				for (int i = 0; i < 2; i++) {
					if (i == 0 && (conner[n2])) {
						continue;
					}
					if (i == 1 && (conner[n])) {
						continue;
					}
					BlockPos pos2dir = pos2.relative(dirside[i]);
					BlockState state2dir = w.getBlockState(pos2dir);
					if (canReach(pos2, pos2dir, state2, state2dir, fluid, w)) {

						if (state2dir.getFluidState().getType().isSame(fluid)) {

							BlockPos pos2diru = pos2dir.above();
							BlockState state2diru = w.getBlockState(pos2diru);
							if (state2diru.getFluidState().getType().isSame(fluid)
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
								float level2dir = state2dir.getFluidState().getOwnHeight();
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
							BlockPos pos2dird = pos2dir.below();
							BlockState state2dird = w.getBlockState(pos2dird);
							if (state2dird.getFluidState().getType().isSame(fluid)
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
					BlockPos pos2d = pos2.below();
					BlockState state2d = w.getBlockState(pos2d);
					if (state2d.getFluidState().getType().isSame(fluid)
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

	public static float getConH(BlockGetter w, BlockPos p, Fluid f, BlockPos dir) {
		// p = p.add(-dir.getX(), 0, -dir.getZ());
		// Blockreader w = (Blockreader) wi;
		BlockPos pu = p.above();
		FluidState ufs = w.getFluidState(pu);
		if (!ufs.isEmpty() && isSameFluid(ufs.getType(), f)) {
			return 1.0f;
		}
		FluidState fsm = w.getFluidState(p);

		float sl = fsm.getOwnHeight();
		int i = 1;
		BlockPos dp = p.offset(dir.getX(), 0, 0);
		BlockPos dp2 = p.offset(0, 0, dir.getZ());
		FluidState dfs = w.getFluidState(dp);
		FluidState dfs2 = w.getFluidState(dp2);

		boolean s = false;

		if (!dfs.isEmpty() && isSameFluid(dfs.getType(), f)) {
			pu = dp.above();
			ufs = w.getFluidState(pu);
			if (!ufs.isEmpty() && isSameFluid(ufs.getType(), f)) {
				return 1.0f;
			}

			sl += dfs.getOwnHeight();
			i++;
			s = true;
		} else if (dfs.isEmpty() && canReach(w, p, Direction.getNearest(dir.getX(), 0, 0))) {
			BlockPos downp = dp.below();
			FluidState downfs = w.getFluidState(downp);
			if (!downfs.isEmpty() && isSameFluid(downfs.getType(), f) && downfs.getOwnHeight() == 1.0F) {
				return 0.0F;
			}
		}

		if (!dfs2.isEmpty() && isSameFluid(dfs2.getType(), f)) {
			pu = dp2.above();
			ufs = w.getFluidState(pu);
			if (!ufs.isEmpty() && isSameFluid(ufs.getType(), f)) {
				return 1.0f;
			}

			sl += dfs2.getOwnHeight();
			i++;
			s = true;
		} else if (dfs2.isEmpty() && canReach(w, p, Direction.getNearest(0, 0, dir.getZ()))) {
			BlockPos downp = dp2.below();
			FluidState downfs = w.getFluidState(downp);
			if (!downfs.isEmpty() && isSameFluid(downfs.getType(), f) && downfs.getOwnHeight() == 1.0F) {
				return 0.0F;
			}
		}

		if (s) {
			BlockPos dp3 = p.offset(dir);
			FluidState dfs3 = w.getFluidState(dp3);

			if (!dfs3.isEmpty() && isSameFluid(dfs3.getType(), f)) {
				pu = dp3.above();
				ufs = w.getFluidState(pu);
				if (!ufs.isEmpty() && isSameFluid(ufs.getType(), f)) {
					return 1.0f;
				}

				sl += dfs3.getOwnHeight();
				i++;
			} else if (dfs3.isEmpty()) {
				BlockPos downp = dp3.below();
				FluidState downfs = w.getFluidState(downp);
				if (!downfs.isEmpty() && isSameFluid(downfs.getType(), f) && downfs.getOwnHeight() == 1.0F
						&& canReach(w, dp3, Direction.getNearest(0, 1, 0))) {
					return 0.0F;
				}
			}
		}
		return sl /= i;
	}

	// ================= UTIL ================== //
	private static boolean canReach(BlockGetter world, BlockPos pos, Direction direction) {
		BlockState state1 = world.getBlockState(pos);
		BlockState state2 = world.getBlockState(pos.relative(direction));
		if (state2.canOcclude() && !(state2.getBlock() instanceof SimpleWaterloggedBlock)) {
			return false;
		}
		VoxelShape voxelShape2 = state2.getCollisionShape(world, pos.relative(direction));
		VoxelShape voxelShape1 = state1.getCollisionShape(world, pos);
		if (voxelShape1.isEmpty() && voxelShape2.isEmpty()) {
			return true;
		}
		return !Shapes.mergedFaceOccludes(voxelShape1, voxelShape2, direction);
	}

	public static boolean canReach(BlockPos pos1, BlockPos pos2, BlockState state1, BlockState state2, Fluid fluid,
			BlockGetter w) {

		Fluid f2 = state2.getFluidState().getType();
		if (f2.isSame(fluid) && state1.getBlock() instanceof LiquidBlock
				&& state2.getBlock() instanceof LiquidBlock) {
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
			if ((state2.getFluidState().isEmpty() || state1.getFluidState().canBeReplacedWith(w, pos1, f2, dir))
					&& fp2.isDestroyableBy(fluid))
				return true;
		}

		if (state2.canOcclude() && !posos && !(state2.getBlock() instanceof SimpleWaterloggedBlock)) {
			return false;
		}
		if (!(fluid instanceof WaterFluid)
				&& (state1.getBlock() instanceof SimpleWaterloggedBlock || state2.getBlock() instanceof SimpleWaterloggedBlock)) {
			return false;
		}
		VoxelShape voxelShape2 = state2.getCollisionShape(w, pos2);
		VoxelShape voxelShape1 = state1.getCollisionShape(w, pos1);
		if ((voxelShape1.isEmpty() || posos) && voxelShape2.isEmpty()) {
			return true;
		}
		return !Shapes.mergedFaceOccludes(voxelShape1, voxelShape2, dir);
	}

	public static boolean canOnlyFullCube(BlockState bs) {
		return canOnlyFullCube(bs.getBlock());
	}

	public static boolean canOnlyFullCube(Block b) {
		return (b instanceof SimpleWaterloggedBlock) ? !(b instanceof IBaseWL) : false;
	}

	// ================= ITEMS ==================//

	public static void onBucketEvent(FillBucketEvent e) {

		MobBucketItem mobBucketItem = null;
		ItemStack bucket = e.getEmptyBucket();
		Item bu = bucket.getItem();
		if (bu instanceof MobBucketItem) {
			mobBucketItem = (MobBucketItem) bu;
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
		Player p = e.getPlayer();
		Level w = e.getWorld();
		HitResult targ0 = e.getTarget();
		HitResult targ = rayTrace(w, p,
				f == Fluids.EMPTY ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE);
		targ0 = targ;
		if (targ.getType() != HitResult.Type.BLOCK) {
			return;
		}
		BlockHitResult targB = (BlockHitResult) targ;
		BlockPos pos = targB.getBlockPos();
		BlockState bs = w.getBlockState(pos);
		FluidState fs = bs.getFluidState();
		if (fs.isEmpty() && f != Fluids.EMPTY && !(bs.getBlock() instanceof SimpleWaterloggedBlock)) {
			pos = pos.relative(targB.getDirection());
			bs = w.getBlockState(pos);
			fs = bs.getFluidState();
		} 
		if (!w.isClientSide && f != Fluids.EMPTY && bs.getBlock() instanceof SimpleWaterloggedBlock) {
			FluidTasksManager.addFluidTask((ServerLevel) w, pos, bs);
		}
		Fluid fluid = fs.getType();
		if ((!f.isSame(Fluids.WATER) && f != Fluids.EMPTY) && bs.getBlock() instanceof SimpleWaterloggedBlock) {

			return;
		}

		if (!(w.mayInteract(p, pos) && p.mayUseItemAt(pos, targB.getDirection(), bh.getContainer()))) {
			return;
		}

		if (f == Fluids.EMPTY) {
			if (!(fluid instanceof FlowingFluid)) {
				return;
			}
			if (targ0.getType() == HitResult.Type.BLOCK) {
				BlockHitResult targB0 = (BlockHitResult) targ0;
				FluidState fs0 = w.getFluidState(targB0.getBlockPos());
				if (fs0.isSource()) {
					return;
				}
			}
			BucketFiller filler = new BucketFiller(w, fluid, bh, e);
			iterateFluidWay(WPOConfig.COMMON.maxBucketDist.get(), pos, filler);

		} else {
			if (!f.isSame(fluid) && fluid != Fluids.EMPTY) {
				e.setCanceled(true);
				return;
			}
			BucketFlusher flusher = new BucketFlusher(w, f, bh, e);
			if (iterateFluidWay(WPOConfig.COMMON.maxBucketDist.get(), pos, flusher) && mobBucketItem != null) {
				mobBucketItem.checkExtraContent(e.getPlayer(), w, bucket, pos);
			}			
		}
	}

	public static BlockHitResult rayTrace(Level worldIn, Player player,
			ClipContext.Fluid fluidMode) {
		float f = player.getXRot();
		float f1 = player.getYRot();
		Vec3 vector3d = player.getEyePosition(1.0F);
		float f2 = Mth.cos(-f1 * ((float) Math.PI / 180F) - (float) Math.PI);
		float f3 = Mth.sin(-f1 * ((float) Math.PI / 180F) - (float) Math.PI);
		float f4 = -Mth.cos(-f * ((float) Math.PI / 180F));
		float f5 = Mth.sin(-f * ((float) Math.PI / 180F));
		float f6 = f3 * f4;
		float f7 = f2 * f4;
		double d0 = player.getAttribute(net.minecraftforge.common.ForgeMod.REACH_DISTANCE.get()).getValue();
		Vec3 vector3d1 = vector3d.add((double) f6 * d0, (double) f5 * d0, (double) f7 * d0);
		return worldIn.clip(
				new ClipContext(vector3d, vector3d1, ClipContext.Block.OUTLINE, fluidMode, player));
	}

	public static boolean iterateFluidWay(int maxRange, BlockPos pos, IFluidActionIteratable actioner) {
		boolean frst = true;
		boolean client = false;
		Level w = actioner.getWorld();
		IWWSG wws = ((IWorldExtended) w).getWWS();
		Set<BlockPos> setBan = new HashSet<>();
		Set<BlockPos> setAll = new HashSet<>();
		client = wws == null;
		if (!client && setBan.add(pos) && !wws.banPos(pos.asLong())) {
			setBan.forEach(p -> wws.banPos(p.asLong()));
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
					if (!client && setBan.add(posn) && !wws.banPos(posn.asLong())) {
						//wws.unbanPoses(setBan);
						setBan.forEach(p -> wws.unbanPos(p.asLong()));
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
					BlockPos pos2 = posn.relative(dir);
					if (setAll.contains(pos2)) {
						continue;
					}
					BlockState bs2 = w.getBlockState(pos2);
					boolean cr = canReach(w, posn, dir);
					boolean eq = actioner.isValidState(bs2);
					if (cr && eq) {
						setLocal2.add(pos2);
						if (actioner.isValidPos(pos2)) {
							if (!client && setBan.add(pos2) && !wws.banPos(pos2.asLong())) {
								//wws.unbanPoses(setBan);
								setBan.forEach(p -> wws.unbanPos(p.asLong()));
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
				setBan.forEach(p -> wws.unbanPos(p.asLong()));
			return true;
		} else {
			actioner.fail();
			if (!client)
				//wws.unbanPoses(setBan);
				setBan.forEach(p -> wws.unbanPos(p.asLong()));
			return false;
		}
	}

	private static class BucketFiller implements IFluidActionIteratable {

		int bucketLevels = WPOConfig.MAX_FLUID_LEVEL;
		int sl = 0;
		boolean complete = false;
		Level world;
		Fluid fluid;
		FillBucketEvent event;
		IFluidHandlerItem bucket;
		Long2ObjectLinkedOpenHashMap<BlockState> states = new Long2ObjectLinkedOpenHashMap<>();

		BucketFiller(Level w, Fluid f, IFluidHandlerItem b, FillBucketEvent e) {
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

			if (canOnlyFullCube(state) && state.getValue(BlockStateProperties.WATERLOGGED)) {
				states.clear();
				states.put(pos.asLong(), getUpdatedState(state, 0, fluid));
				complete = true;
				return;
			}
			FluidState fs = state.getFluidState();
			int l = fs.getAmount();
			sl += l;
			int nl = 0;
			if (sl >= bucketLevels) {
				nl = sl - bucketLevels;
				complete = true;
			}
			states.put(pos.asLong(), getUpdatedState(state, nl, fluid));
		}

		@Override
		public Level getWorld() {
			return world;
		}

		@Override
		public boolean isValidState(BlockState state) {
			return fluid.isSame(state.getFluidState().getType());
		}

		@Override
		public void fail() {
			// System.out.println(sl);

			fillStates(states, world);
			event.setResult(Result.ALLOW);
			Player p = event.getPlayer();
			Item item = bucket.getContainer().getItem();
			p.awardStat(Stats.ITEM_USED.get(item));
			SoundEvent soundevent = fluid.getAttributes().getFillSound();
			if (soundevent == null)
				soundevent = fluid.is(FluidTags.LAVA) ? SoundEvents.BUCKET_FILL_LAVA
						: SoundEvents.BUCKET_FILL;
			p.playSound(soundevent, 1.0F, 1.0F);

			if (!p.getAbilities().instabuild) {
				ItemStack stack = new ItemStack(net.skds.wpo.registry.Items.ADVANCED_BUCKET.get());
				ExtendedFHIS st2 = new ExtendedFHIS(stack, 1000);
				Fluid f2 = fluid instanceof FlowingFluid ? ((FlowingFluid) fluid).getSource() : fluid;
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
			Player p = event.getPlayer();
			Item item = bucket.getContainer().getItem();
			p.awardStat(Stats.ITEM_USED.get(item));
			SoundEvent soundevent = fluid.getAttributes().getFillSound();
			if (soundevent == null)
				soundevent = fluid.is(FluidTags.LAVA) ? SoundEvents.BUCKET_FILL_LAVA
						: SoundEvents.BUCKET_FILL;
			p.playSound(soundevent, 1.0F, 1.0F);
			if (!p.getAbilities().instabuild) {
				// bucket.fill(new FluidStack(fluid, 1000), FluidAction.EXECUTE);
				event.setFilledBucket(new ItemStack(fluid.getBucket()));
			}
		}
	}

	private static class BottleFiller implements IFluidActionIteratable {

		int bucketLevels = 3;
		int sl = 0;
		boolean complete = false;
		Level world;
		ItemStack bottle;
		Fluid fluid;
		CallbackInfoReturnable<InteractionResultHolder<ItemStack>> ci;
		Long2ObjectLinkedOpenHashMap<BlockState> states = new Long2ObjectLinkedOpenHashMap<>();

		BottleFiller(Level w, Fluid f, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> ci, ItemStack stack) {
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

			if (canOnlyFullCube(state) && state.getValue(BlockStateProperties.WATERLOGGED)) {
				states.clear();
				states.put(pos.asLong(), getUpdatedState(state, 0, fluid));
				complete = true;
				return;
			}
			FluidState fs = state.getFluidState();
			int l = fs.getAmount();
			int osl = sl;
			sl += l;
			int nl = 0;
			if (sl >= bucketLevels) {
				nl = sl - bucketLevels;
				complete = true;
			}
			if (osl != sl)
				states.put(pos.asLong(), getUpdatedState(state, nl, fluid));
		}

		@Override
		public Level getWorld() {
			return world;
		}

		@Override
		public boolean isValidState(BlockState state) {
			return fluid.isSame(state.getFluidState().getType());
		}

		@Override
		public void finish() {
			fillStates(states, world);
		}

		@Override
		public void fail() {
			ci.setReturnValue(InteractionResultHolder.fail(bottle));
		}
	}

	private static class BucketFlusher implements IFluidActionIteratable {

		int mfl = WPOConfig.MAX_FLUID_LEVEL;
		int bucketLevels = WPOConfig.MAX_FLUID_LEVEL;
		int sl = bucketLevels;
		boolean complete = false;
		Level world;
		Fluid fluid;
		FillBucketEvent event;
		IFluidHandlerItem bucket;
		Long2ObjectLinkedOpenHashMap<BlockState> states = new Long2ObjectLinkedOpenHashMap<>();

		BucketFlusher(Level w, Fluid f, IFluidHandlerItem b, FillBucketEvent e) {
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

			if (canOnlyFullCube(state) && state.hasProperty(BlockStateProperties.WATERLOGGED) && !state.getValue(BlockStateProperties.WATERLOGGED)) {
				states.clear();
				states.put(pos.asLong(), getUpdatedState(state, mfl, fluid));
				complete = true;
				return;
			}
			FluidState fs = state.getFluidState();
			int el = mfl - fs.getAmount();
			int osl = sl;
			sl -= el;
			int nl = mfl;
			if (sl <= 0) {
				nl = mfl + sl;
				complete = true;
			}
			if (osl != sl)
				states.put(pos.asLong(), getUpdatedState(state, nl, fluid));
		}

		@Override
		public Level getWorld() {
			return world;
		}

		@Override
		public boolean isValidState(BlockState state) {
			return fluid.isSame(state.getFluidState().getType()) || state.getFluidState().isEmpty();
		}

		@Override
		public void finish() {
			fillStates(states, world);

			event.setResult(Result.ALLOW);
			Player p = event.getPlayer();
			Item item = bucket.getContainer().getItem();
			p.awardStat(Stats.ITEM_USED.get(item));
			SoundEvent soundevent = fluid.getAttributes().getEmptySound();
			if (soundevent == null)
				soundevent = fluid.is(FluidTags.LAVA) ? SoundEvents.BUCKET_EMPTY_LAVA
						: SoundEvents.BUCKET_EMPTY;
			p.playSound(soundevent, 1.0F, 1.0F);
			if (!p.getAbilities().instabuild) {
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
		Level world;
		Fluid fluid;
		BlockEvent.EntityPlaceEvent event;
		Long2ObjectLinkedOpenHashMap<BlockState> states = new Long2ObjectLinkedOpenHashMap<>();
		BlockState obs;

		FluidDisplacer(Level w, BlockEvent.EntityPlaceEvent e) {
			obs = e.getBlockSnapshot().getReplacedBlock();
			FluidState ofs = obs.getFluidState();

			fluid = ofs.getType();
			sl = ofs.getAmount();
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
				BlockPos pos2 = p0.relative(d);
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

			if (canOnlyFullCube(state) && state.hasProperty(BlockStateProperties.WATERLOGGED) && !state.getValue(BlockStateProperties.WATERLOGGED)) {
				states.clear();
				states.put(pos.asLong(), getUpdatedState(state, mfl, fluid));
				complete = true;
				return;
			}
			FluidState fs = state.getFluidState();
			int el = mfl - fs.getAmount();
			int osl = sl;
			sl -= el;
			int nl = mfl;
			if (sl <= 0) {
				nl = mfl + sl;
				complete = true;
			}
			if (osl != sl)
				states.put(pos.asLong(), getUpdatedState(state, nl, fluid));
		}

		@Override
		public Level getWorld() {
			return world;
		}

		@Override
		public boolean isValidState(BlockState state) {
			return fluid.isSame(state.getFluidState().getType()) || state.getFluidState().isEmpty();
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
		Level world;
		Fluid fluid;
		Long2ObjectLinkedOpenHashMap<BlockState> states = new Long2ObjectLinkedOpenHashMap<>();
		BlockState obs;

		FluidDisplacer2(Level w, BlockState obs) {
			FluidState ofs = obs.getFluidState();
			this.obs = obs;
			fluid = ofs.getType();
			sl = ofs.getAmount();
			world = w;
		}

		@Override
		public boolean isComplete() {
			return complete;
		}

		@Override
		public void addZero(Set<BlockPos> set, BlockPos p0) {
			for (Direction d : getRandomizedDirections(world.getRandom(), true)) {
				BlockPos pos2 = p0.relative(d);
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

			if (canOnlyFullCube(state) && state.hasProperty(BlockStateProperties.WATERLOGGED) && !state.getValue(BlockStateProperties.WATERLOGGED)) {
				states.clear();
				states.put(pos.asLong(), getUpdatedState(state, mfl, fluid));
				complete = true;
				return;
			}
			FluidState fs = state.getFluidState();
			int el = mfl - fs.getAmount();
			int osl = sl;
			sl -= el;
			int nl = mfl;
			if (sl <= 0) {
				nl = mfl + sl;
				complete = true;
			}
			if (osl != sl)
				states.put(pos.asLong(), getUpdatedState(state, nl, fluid));
		}

		@Override
		public Level getWorld() {
			return world;
		}

		@Override
		public boolean isValidState(BlockState state) {
			return fluid.isSame(state.getFluidState().getType()) || state.getFluidState().isEmpty();
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
		Level world;
		Fluid fluid;
		// PistonBlockStructureHelper ps;
		Set<BlockPos> movepos = new HashSet<>();
		PistonEvent.Pre event;
		Long2ObjectLinkedOpenHashMap<BlockState> states = new Long2ObjectLinkedOpenHashMap<>();
		BlockState obs;

		PistonDisplacer(Level w, PistonEvent.Pre e, BlockState os, PistonStructureResolver ps) {
			this.obs = os;
			FluidState ofs = obs.getFluidState();
			// this.ps = ps;
			this.fluid = ofs.getType();
			this.sl = ofs.getAmount();
			this.world = w;
			this.event = e;
			movepos.addAll(ps.getToDestroy());
			movepos.addAll(ps.getToPush());
			for (BlockPos p : ps.getToPush()) {
				movepos.add(p.relative(event.getDirection()));
				// System.out.println(p.relative(event.getDirection()));
			}
		}

		@Override
		public boolean isComplete() {
			return complete;
		}

		@Override
		public void addZero(Set<BlockPos> set, BlockPos p0) {
			for (Direction d : getRandomizedDirections(world.getRandom(), true)) {
				BlockPos pos2 = p0.relative(d);
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

			if (canOnlyFullCube(state) && state.hasProperty(BlockStateProperties.WATERLOGGED) && !state.getValue(BlockStateProperties.WATERLOGGED)) {
				states.clear();
				states.put(pos.asLong(), getUpdatedState(state, mfl, fluid));
				complete = true;
				return;
			}
			FluidState fs = state.getFluidState();
			int el = mfl - fs.getAmount();
			int osl = sl;
			sl -= el;
			int nl = mfl;
			if (sl <= 0) {
				nl = mfl + sl;
				complete = true;
			}
			if (osl != sl)
				states.put(pos.asLong(), getUpdatedState(state, nl, fluid));
		}

		@Override
		public Level getWorld() {
			return world;
		}

		@Override
		public boolean isValidState(BlockState state) {
			return fluid.isSame(state.getFluidState().getType()) || state.getFluidState().isEmpty();
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

	public static void fillStates(Long2ObjectLinkedOpenHashMap<BlockState> states, Level world) {
		if (!world.isClientSide) {
			states.forEach((lpos, state) -> {
				world.setBlockAndUpdate(BlockPos.of(lpos), state);
			});
		}
	}

	public interface IFluidActionIteratable {
		default void addZero(Set<BlockPos> set, BlockPos p0) {
			set.add(p0);
		}

		boolean isComplete();

		void run(BlockPos pos, BlockState state);

		Level getWorld();

		boolean isValidState(BlockState state);

		default boolean isValidPos(BlockPos pos) {
			return true;
		}

		void finish();

		default void fail() {
		}
	}

	public static void onBottleUse(Level w, Player p, InteractionHand hand,
			CallbackInfoReturnable<InteractionResultHolder<ItemStack>> ci, ItemStack stack) {
		BlockHitResult rt = rayTrace(w, p, ClipContext.Fluid.ANY);
		BlockPos pos = rt.getBlockPos();

		BottleFiller filler = new BottleFiller(w, Fluids.WATER, ci, stack);
		iterateFluidWay(WPOConfig.COMMON.maxBucketDist.get(), pos, filler);
	}

	public static void onBlockPlace(BlockEvent.EntityPlaceEvent e) {
		Level w = (Level) e.getWorld();
		BlockPos pos = e.getPos();
		BlockState oldState = e.getBlockSnapshot().getReplacedBlock();
		FluidState fs = oldState.getFluidState();
		Fluid f = fs.getType();
		BlockState newState = e.getPlacedBlock();
		Block nb = newState.getBlock();
		if (fs.isEmpty() || nb instanceof SpongeBlock || nb instanceof WetSpongeBlock) {
			return;
		}
		if (nb instanceof LiquidBlockContainer && !(nb instanceof SimpleWaterloggedBlock)) {
			return;
		}
		if (nb instanceof SimpleWaterloggedBlock && newState.getValue(BlockStateProperties.WATERLOGGED)) {
			return;
		}
		if (!canOnlyFullCube(newState) && nb instanceof IBaseWL && f.isSame(Fluids.WATER)) {
			newState = getUpdatedState(newState, fs.getAmount(), Fluids.WATER);
			w.setBlockAndUpdate(pos, newState);
			return;
		}

		FluidDisplacer displacer = new FluidDisplacer(w, e);
		iterateFluidWay(10, e.getPos(), displacer);
	}

	// ======================= PISTONS ======================= //

	public static void onPistonPre(PistonEvent.Pre e) {
		Level w = (Level) e.getWorld();
		if (w.isClientSide || e.isCanceled()) {
			return;
		}
		PistonStructureResolver ps = e.getStructureHelper();

		if (!ps.resolve()) {
			return;
		}
		List<BlockPos> poslist = ps.getToDestroy();

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
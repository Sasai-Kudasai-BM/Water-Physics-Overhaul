package net.skds.wpo.tileentity;

import java.util.Optional;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.skds.wpo.fluidphysics.FFluidStatic;
import net.skds.wpo.network.PacketHandler;
import net.skds.wpo.network.PipeUpdatePacket;
import net.skds.wpo.registry.Entities;
import net.skds.wpo.util.api.IConnectionSides;
import net.skds.wpo.util.api.IPressuredTank;

public class PipeTileEntity extends BasicTankEntity {

	private static final float G = 9.81f;
	private static final float ATM_PRESSURE = 1f;
	private static final double PIX = 1D / 16D;
	public static final AxisAlignedBB MID_AABB = new AxisAlignedBB(3 * PIX, 3 * PIX, 3 * PIX, 13 * PIX, 13 * PIX,
			13 * PIX);
	public static final VoxelShape MID_SHAPE = VoxelShapes.create(MID_AABB);

	public float[] flow = new float[6];
	public TileEntity[] connections = new TileEntity[6];
	public boolean[] boolConnections = new boolean[6];
	public float pressure = ATM_PRESSURE;

	// public int max = 500;

	private boolean firstTick = true;
	//private FluidTank tank = new FluidTank(500);
	private final LazyOptional<IFluidHandler> holder = LazyOptional.of(() -> tank);

	public PipeTileEntity() {
		super(Entities.PIPE.get());
	}

	@Override
	public void onLoad() {
		super.onLoad();
		// Fucking deadlock
		// updateConntections();
	}

	@Override
	public void tick() {
		if (pressure < 0F) {
			pressure = 0F;
		}
		// System.out.println(pressure);

		if (firstTick) {
			updateConntections();
			firstTick = false;
		}


		if (!world.isRemote) {
			tickFluid();
			//sendUpdatePacket();
		}

	}

	private void tickConnection(TileEntity connected, Direction direction) {
		IFluidHandler fh2 = getFluidHandler(connected, direction);
		if (fh2 != null) {

			int i = direction.getIndex();
			FluidStack fst1 = tank.getFluid();
			FluidStack fst2 = fh2.getFluidInTank(0);
			if (fst1.isEmpty() && fst2.isEmpty()) {
				float pz1 = getZeroPressure(null);
				float pz2 = getZeroPressureInHandler(connected, direction.getOpposite());

				float dp2 = (pz1 - pz2) * 0.6f;

				if (Math.abs(dp2) > 1E-5F) {
					pz2 += dp2;
					pz1 -= dp2;
					setPressure(pz1, null);
					setPressure(connected, pz2, direction.getOpposite());
				}
				return;
			}
			if (!(fst1.isEmpty() || fst2.isEmpty()) && !fst1.isFluidEqual(fst2)) {
				return;
			}
			//if (connected instanceof FluidGateTileEntity) {
			//	System.out.println("x");
			//}
			if (!fh2.isFluidValid(0, fst1) || !tank.isFluidValid(0, fst2)) {
				return;
			}
			Fluid f = fst1.isEmpty() ? fst2.getFluid() : fst1.getFluid();
			FluidAttributes fa = f.getAttributes();

			float p1 = getPressure(null);
			float p2 = getPressureInHandler(connected, direction.getOpposite());

			boolean b = true;
			boolean b2 = false;

			float dp0 = 0;

			float dp = getPressurePerH(f, 1000);
			float z = getZeroPressureInHandler(connected, direction.getOpposite());
			float z0 = getZeroPressure(null);
			if (direction == Direction.UP) {
				//return;
				float pp = getPressurePerStack(fst2) * 2;
				dp0 = -pp;
				b2 = true;
				if (z0 > p2) {
					//setPressure(p1 -= pp);
					p1 -= dp;
				} else {
					//setPressure(p2);
				p2 += dp;
				}
			} else if (direction == Direction.DOWN) {
				float pp = getPressurePerStack(fst1) * 2;
				dp0 = pp;
				b2 = true;
				if (z > p1) {
					//setPressure(connected, z -= pp);
					p1 += dp;
				} else {
					//setPressure(connected, p1);
					p1 += dp;
				}

			}
			if (b) {
				float pz1 = getZeroPressure(null);
				float pz2 = getZeroPressureInHandler(connected, direction.getOpposite());

				float dp2 = (pz1 + dp0 - pz2) / 2f;

				if (b2) {
					//dp2 *= 0.6f;
				}

				if (Math.abs(dp2) > 1E-5F) {
					pz2 += dp2;
					pz1 -= dp2;
					setPressure(pz1, null);
					setPressure(connected, pz2, direction.getOpposite());
				}
			}

			float c = 4E5F;
			float df = (p1 - p2) * c / fa.getDensity();
			flow[i] += df;
			int maxFlow = Math.round(flow[i]);

			int am1 = fst1.getAmount();
			int am2 = fst2.getAmount();
			if (maxFlow == 0) {
				if (am1 > 0 && am1 < 3 && am1 != am2) {
					maxFlow = flow[i] > 0 ? 1 : -1;
				} else {
					return;
				}
			}
			IFluidHandler toFillH;
			IFluidHandler toDrainH;
			if (maxFlow > 0) {
				toFillH = fh2;
				toDrainH = tank;
			} else {
				toFillH = tank;
				toDrainH = fh2;
			}
			FluidStack toFill = toFillH.getFluidInTank(0);
			FluidStack toDrain = toDrainH.getFluidInTank(0);
			maxFlow = Math.abs(maxFlow);
			int amd = toDrain.getAmount();
			int amf = toFill.getAmount();

			maxFlow = Math.min(maxFlow, amd);
			maxFlow = Math.min(maxFlow, toFillH.getTankCapacity(0) - amf);

			// float dp = (float) df * fa.getDensity() / c;
			// p1 += dp;
			// p2 -= dp;
			// float dp2 = (p1 - p2) /4f;
			// p2 += dp2;
			// p1 -= dp2;

			// setPressure(p1);
			// setPressure(connected, p2);

			FluidStack cfs = toDrainH.drain(maxFlow, FluidAction.EXECUTE);
			//System.out.println(maxFlow);
			toFillH.fill(cfs, FluidAction.EXECUTE);
		}
	}

	private void tickFluid() {
		for (Direction dir : FFluidStatic.getAllRandomizedDirections(world.rand)) {			
		//for (Direction dir : Direction.values()) {
			int i = dir.getIndex();
			if (connections[i] != null) {
				tickConnection(connections[i], dir);
			}

			flow[i] *= 0.8f;
			if (flow[i] < 0.01f) {
				flow[i] = 0;
			}
		}
	}

	private static void setPressure(TileEntity te, float pressure, Direction side) {
		if (te instanceof IPressuredTank) {
			((IPressuredTank) te).setPressure(pressure, side);
		}
	}

	private static IFluidHandler getFluidHandler(TileEntity tile, Direction dir) {
		if (tile != null) {
			Optional<IFluidHandler> op = tile
					.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, dir.getOpposite()).resolve();
			if (op.isPresent()) {
				return op.get();
			}
		}
		return null;
	}

	private static float getZeroPressureInHandler(TileEntity tile, Direction side) {
		if (tile != null) {
			if (tile instanceof IPressuredTank) {
				return ((IPressuredTank) tile).getZeroPressure(side);
			}
		}
		return ATM_PRESSURE;
	}

	private static float getPressureInHandler(TileEntity tile, Direction side) {
		if (tile != null) {
			if (tile instanceof IPressuredTank) {
				return ((IPressuredTank) tile).getPressure(side);
			}
		}
		return ATM_PRESSURE;
	}

	public void updateBoolConnections() {
		boolean[] bl = new boolean[6];
		int i = 0;
		for (TileEntity fth : connections) {
			bl[i] = fth != null;
			i++;
		}
		boolConnections = bl;
	}

	public void updateConntections() {
		for (Direction dir : Direction.values()) {
			BlockPos pos2 = pos.offset(dir);
			BlockState state2 = world.getBlockState(pos2);
			if (state2.hasTileEntity()) {
				TileEntity te = world.getTileEntity(pos2);
				if (te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).resolve().isPresent()) {
					if (te instanceof IConnectionSides && !((IConnectionSides) te).canBeConnected(dir)) {
						removeConnection(dir);
						return;
					}
					addConnection(te, dir);
					if (te instanceof PipeTileEntity) {
						((PipeTileEntity) te).addConnection(this, dir.getOpposite());
					}
				} else {
					removeConnection(dir);
				}
			} else {
				removeConnection(dir);
			}
		}
	}

	@Override
	public void remove() {
		super.remove();
		for (TileEntity c : connections) {
			if (c != null && c instanceof PipeTileEntity) {
				((PipeTileEntity) c).updateConntections();
			}
		}
	}

	public void addConnection(TileEntity handler, Direction direction) {
		connections[direction.getIndex()] = handler;
		updateBoolConnections();
	}

	public void removeConnection(Direction direction) {
		connections[direction.getIndex()] = null;
		updateBoolConnections();
	}

	public VoxelShape getShape() {
		VoxelShape shape = MID_SHAPE;
		for (int i = 0; i < 6; i++) {
			if (connections[i] != null) {
				Vector3i dirvec = Direction.byIndex(i).getDirectionVec();
				Vector3d vec = new Vector3d(dirvec.getX(), dirvec.getY(), dirvec.getZ()).scale(3 * PIX);
				VoxelShape shape2 = VoxelShapes.create(MID_AABB.offset(vec));
				shape = VoxelShapes.combine(shape, shape2, IBooleanFunction.OR);
			}
		}
		return shape.simplify();
	}

	public static float getPressurePerStack(FluidStack stack) {
		if (stack.isEmpty()) {
			return 0.0f;
		}
		return (stack.getAmount() * stack.getFluid().getFluid().getAttributes().getDensity() * 1E-8F * G);
	}

	public static float getPressurePerH(Fluid fluid, int mb) {
		return (mb * fluid.getFluid().getAttributes().getDensity() * 1E-8F * G);
	}

	@Override
	public float getPressure(Direction side) {
		return pressure + (getPressurePerStack(tank.getFluid()) * 2);
	}

	@Override
	public float getZeroPressure(Direction side) {
		return pressure;
	}

	@Override
	public void setPressure(float pressure, Direction side) {
		this.pressure = pressure;
	}

	@Override
	public void read(BlockState state, CompoundNBT tag) {
		super.read(state, tag);
		tank.readFromNBT(tag);
		if (tag.contains("Pressure")) {
			pressure = tag.getFloat("Pressure");
		} else {
			pressure = ATM_PRESSURE;
		}

		ListNBT flowListNBT = tag.getList("Flow", 5);
		for (int i = 0; i < 6; i++) {
			flow[i] = flowListNBT.getFloat(i);
		}
	}

	@Override
	public CompoundNBT write(CompoundNBT tag) {
		tag = super.write(tag);
		tank.writeToNBT(tag);
		tag.putFloat("Pressure", pressure);
		ListNBT flowListNBT = new ListNBT();
		for (float f : flow) {
			flowListNBT.add(FloatNBT.valueOf(f));
		}
		tag.put("Flow", flowListNBT);
		return tag;
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
			return holder.cast();
		return super.getCapability(capability, facing);
	}

	@Override
	public int getTanks() {
		return 1;
	}

	@Override
	public FluidStack getFluidInTank(int tank) {
		return this.tank.getFluid();
	}

	@Override
	public int getTankCapacity(int tank) {
		return this.tank.getCapacity();
	}

	@Override
	public boolean isFluidValid(int tank, FluidStack stack) {
		return this.tank.isFluidValid(stack);
	}

	@Override
	public int fill(FluidStack resource, FluidAction action) {
		return this.tank.fill(resource, action);
	}

	@Override
	public FluidStack drain(FluidStack resource, FluidAction action) {
		return this.tank.drain(resource, action);
	}

	@Override
	public FluidStack drain(int maxDrain, FluidAction action) {
		return this.tank.drain(maxDrain, action);
	}

	@SuppressWarnings("unused")
	private void sendUpdatePacket() {
		for (PlayerEntity p : world.getPlayers()) {
			PacketHandler.send(p, new PipeUpdatePacket(write(new CompoundNBT())));
		}
	}

	@Override
	public void updateContainingBlockInfo() {
		super.updateContainingBlockInfo();
		updateConntections();
	}
}
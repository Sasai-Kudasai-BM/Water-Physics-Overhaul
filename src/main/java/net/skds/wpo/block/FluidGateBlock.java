package net.skds.wpo.block;

import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.skds.wpo.block.entity.FluidGateBlockEntity;
import net.skds.wpo.registry.Entities;

import javax.annotation.Nullable;

public class FluidGateBlock extends BaseEntityBlock {

	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	public static final DirectionProperty FACING = BlockStateProperties.FACING;

	public FluidGateBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, Boolean.valueOf(false)));
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new FluidGateBlockEntity(pos, state);
	}

	@Override
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
//		return level.isClientSide() ? null : createTickerHelper(type, Entities.GATE.get(), FluidGateBlockEntity::tick);
		// TODO check if possible to split tickers for client and server side (especially if client side not ticking -> null)
		return createTickerHelper(type, Entities.GATE.get(), FluidGateBlockEntity::tick);
	}

	public static FluidGateBlock getForReg() {
		Properties prop = Properties.of(Material.METAL).requiresCorrectToolForDrops().isRedstoneConductor(opa).noOcclusion().strength(0.5F);
		return new FluidGateBlock(prop);
	}

	private static final StatePredicate opa = new StatePredicate() {
		@Override
		public boolean test(BlockState s, BlockGetter w, BlockPos p) {
			return false;
		}
	};

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		// builder.add(FFL, WATERLOGGED, FACING);
		builder.add(FACING, POWERED);
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
		boolean flag = worldIn.hasNeighborSignal(pos);
		boolean flag1 = state.getValue(POWERED);
		if (flag && !flag1) {
			worldIn.setBlock(pos, state.setValue(POWERED, Boolean.valueOf(true)), 6);
		} else if (!flag && flag1) {
			worldIn.setBlock(pos, state.setValue(POWERED, Boolean.valueOf(false)), 6);
		}

	}

	// @Override
	// public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random
	// rand) {
	// ProxyBlockSource proxyblocksource = new ProxyBlockSource(worldIn, pos);
	// PumpTileEntity pump = proxyblocksource.getBlockTileEntity();
	// }

	@Override
	public VoxelShape getBlockSupportShape(BlockState state, BlockGetter reader, BlockPos pos) {
		return Shapes.block();
		// switch (state.get(FACING)) {
		// case UP:
		// return U_AABB;
		// case DOWN:
		// return D_AABB;
		// case NORTH:
		// return N_AABB;
		// case SOUTH:
		// return S_AABB;
		// case WEST:
		// return W_AABB;
		// case EAST:
		// return E_AABB;
		// default:
		// return U_AABB;
		// }
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return getBlockSupportShape(state, worldIn, pos);
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos,
			CollisionContext context) {
		return getBlockSupportShape(state, worldIn, pos);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		boolean sneak = context.getPlayer().isShiftKeyDown();
		Direction dir = context.getNearestLookingDirection();
		return this.defaultBlockState().setValue(FACING, sneak ? dir.getOpposite() : dir);
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState state) {
		return true;
	}
}
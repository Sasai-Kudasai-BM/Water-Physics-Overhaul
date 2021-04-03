package net.skds.wpo.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.skds.wpo.tileentity.PumpTileEntity;

public class PumpBlock extends DirectionalBlock {

	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	public PumpBlock(Properties properties) {
		super(properties);
		this.setDefaultState(this.stateContainer.getBaseState().with(POWERED, Boolean.valueOf(false)));
	}

	public static PumpBlock getForReg() {
		Properties prop = Properties.create(Material.IRON).setRequiresTool().setOpaque(opa).notSolid().hardnessAndResistance(0.5F);
		return new PumpBlock(prop);
	}

	private static final IPositionPredicate opa = new IPositionPredicate() {
		@Override
		public boolean test(BlockState s, IBlockReader w, BlockPos p) {
			return false;
		}
	};

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		// builder.add(FFL, WATERLOGGED, FACING);
		builder.add(FACING, POWERED);
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
		boolean flag = worldIn.isBlockPowered(pos);
		boolean flag1 = state.get(POWERED);
		if (flag && !flag1) {
			worldIn.setBlockState(pos, state.with(POWERED, Boolean.valueOf(true)), 6);
		} else if (!flag && flag1) {
			worldIn.setBlockState(pos, state.with(POWERED, Boolean.valueOf(false)), 6);
		}

	}

	// @Override
	// public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random
	// rand) {
	// ProxyBlockSource proxyblocksource = new ProxyBlockSource(worldIn, pos);
	// PumpTileEntity pump = proxyblocksource.getBlockTileEntity();
	// }

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new PumpTileEntity(state);
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader reader, BlockPos pos) {
		return VoxelShapes.fullCube();
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
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return getCollisionShape(state, worldIn, pos);
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos,
			ISelectionContext context) {
		return getCollisionShape(state, worldIn, pos);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		boolean sneak = context.getPlayer().isSneaking();
		Direction dir = context.getNearestLookingDirection();
		return this.getDefaultState().with(FACING, sneak ? dir.getOpposite() : dir);
	}

	@Override
	public boolean isTransparent(BlockState state) {
		return true;
	}
}
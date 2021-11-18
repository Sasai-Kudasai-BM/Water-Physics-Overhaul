package net.skds.wpo.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.skds.wpo.registry.BlockStateProps;
import net.skds.wpo.tileentity.PipeTileEntity;
import net.skds.wpo.util.interfaces.IBaseWL;

public class PipeBlock extends Block implements IWaterLoggable, IBaseWL {

	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	public static final IntegerProperty FFL = BlockStateProps.FFLUID_LEVEL;

	public PipeBlock(Properties properties) {
		super(properties);
		this.setDefaultState(this.stateContainer.getBaseState().with(FFL, 0).with(WATERLOGGED, Boolean.valueOf(false)));
	}

	public static PipeBlock getForReg() {
		Properties prop = Properties.create(Material.IRON).setRequiresTool().setOpaque(PipeBlock::opa).notSolid()
				.hardnessAndResistance(0.5F);
		return new PipeBlock(prop);
	}

	public static boolean opa(BlockState s, IBlockReader w, BlockPos p) {
		return false;
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(FFL, WATERLOGGED);
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new PipeTileEntity();
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader reader, BlockPos pos) {
		PipeTileEntity te = (PipeTileEntity) reader.getTileEntity(pos);
		VoxelShape shape;
		if (te == null) {
			shape = VoxelShapes.create(0.01, 0.01, 0.01, 0.99, 0.99, 0.99);
		} else {
			shape = te.getShape();
		}
		return shape;
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
		return this.getDefaultState();
	}

	@Override
	public boolean isTransparent(BlockState state) {
		return true;
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
		updateConntections(worldIn, pos);
	}

	@Override
	public BlockState updatePostPlacement(BlockState state, Direction direction, BlockState neighbourState,
			IWorld world, BlockPos pos, BlockPos neighbourPos) {
		updateConntections(world, pos);
		return state;
	}

	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		updateConntections(worldIn, pos);
	}

	public void updateConntections(IWorld world, BlockPos pos) {
		PipeTileEntity thisEntity = (PipeTileEntity) world.getTileEntity(pos);
		if (thisEntity != null) {
			thisEntity.updateConntections();
		}
	}
}
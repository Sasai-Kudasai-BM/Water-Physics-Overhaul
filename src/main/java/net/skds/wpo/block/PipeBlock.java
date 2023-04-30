package net.skds.wpo.block;

import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.skds.wpo.block.entity.FluidGateBlockEntity;
import net.skds.wpo.registry.BlockStateProps;
import net.skds.wpo.block.entity.PipeBlockEntity;
import net.skds.wpo.registry.Entities;
import net.skds.wpo.util.interfaces.IBaseWL;

import javax.annotation.Nullable;

public class PipeBlock extends BaseEntityBlock implements SimpleWaterloggedBlock, IBaseWL {

	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	public static final IntegerProperty FFL = BlockStateProps.FFLUID_LEVEL;

	public PipeBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FFL, 0).setValue(WATERLOGGED, Boolean.valueOf(false)));
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new PipeBlockEntity(pos, state);
	}

	@Override
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
//		return level.isClientSide() ? null : createTickerHelper(type, Entities.PIPE.get(), PipeBlockEntity::tick);
		return createTickerHelper(type, Entities.PIPE.get(), PipeBlockEntity::tick);
	}

	public static PipeBlock getForReg() {
		Properties prop = Properties.of(Material.METAL).requiresCorrectToolForDrops().isRedstoneConductor(opa).noOcclusion().strength(0.5F);
		return new PipeBlock(prop);
	}

	private static final StatePredicate opa = new StatePredicate() {
		@Override
		public boolean test(BlockState s, BlockGetter w, BlockPos p) {
			return false;
		}
	};

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FFL, WATERLOGGED);
	}

	@Override
	public VoxelShape getBlockSupportShape(BlockState state, BlockGetter reader, BlockPos pos) {
		PipeBlockEntity te = (PipeBlockEntity) reader.getBlockEntity(pos);
		VoxelShape shape;
		if (te == null) {
			shape = Shapes.box(0.01, 0.01, 0.01, 0.99, 0.99, 0.99);
		} else {
			shape = te.getShape();
		}
		return shape;
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
		return this.defaultBlockState();
	}
	
	@Override
	public boolean useShapeForLightOcclusion(BlockState state) {
		return true;
	}    

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
		updateConntections(worldIn, pos);
	}
	
	@Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState, LevelAccessor world, BlockPos pos, BlockPos neighbourPos) {

		updateConntections(world, pos);
        return state;
    }

	@Override
	public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		updateConntections(worldIn, pos);
	}

	public void updateConntections(LevelAccessor world, BlockPos pos) {		
		PipeBlockEntity thisEntity = (PipeBlockEntity) world.getBlockEntity(pos);
		if (thisEntity != null) {
			thisEntity.updateConntections();
		}
	}
}
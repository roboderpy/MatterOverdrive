package matteroverdrive.blocks;

import com.astro.clib.util.AABBUtils;
import com.google.common.collect.ImmutableList;
import matteroverdrive.blocks.includes.MOBlockContainer;
import matteroverdrive.tile.pipes.TileEntityPipe;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public abstract class BlockPipe<TE extends TileEntity> extends MOBlockContainer<TE> {

    public static final ImmutableList<PropertyBool> CONNECTED_PROPERTIES = ImmutableList.of(PropertyBool.create(EnumFacing.DOWN.getName()), PropertyBool.create(EnumFacing.UP.getName()), PropertyBool.create(EnumFacing.NORTH.getName()), PropertyBool.create(EnumFacing.SOUTH.getName()), PropertyBool.create(EnumFacing.WEST.getName()), PropertyBool.create(EnumFacing.EAST.getName()));

    private static final AxisAlignedBB CENTER = new AxisAlignedBB(13 / 32d, 13 / 32d, 13 / 32d, 19 / 32d, 19 / 32d, 19 / 32d);
    private static final AxisAlignedBB DOWN = new AxisAlignedBB(13 / 32d, 13 / 64d, 13 / 32d, 19 / 32d, 0, 19 / 32d);

    public BlockPipe(Material material, String name) {
        super(material, name);
        this.useNeighborBrightness = true;
        this.setRotationType(-1);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, CONNECTED_PROPERTIES.toArray(new IProperty[CONNECTED_PROPERTIES.size()]));
    }

    @Nonnull
    @Override
    @Deprecated
    public IBlockState getActualState(@Nonnull IBlockState state, IBlockAccess world, BlockPos pos) {
        for (EnumFacing facing : EnumFacing.VALUES) {
            state = state.withProperty(CONNECTED_PROPERTIES.get(facing.getIndex()), isConnectableSide(facing, world, pos));
        }
        return state;
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB mask, List<AxisAlignedBB> list, @Nullable Entity entityIn, boolean isActualState) {
        AxisAlignedBB center = CENTER.offset(pos);

        if (mask.intersects(center)) {
            list.add(center);
        }

        for (EnumFacing side : EnumFacing.VALUES) {
            if (isConnectableSide(side, worldIn, pos)) {
                AxisAlignedBB sideBox = AABBUtils.rotateFace(DOWN, side).offset(pos);
                if (mask.intersects(sideBox)) {
                    list.add(sideBox);
                }
            }
        }
    }


    public boolean isConnectableSide(EnumFacing dir, IBlockAccess world, BlockPos pos) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityPipe) {
            return ((TileEntityPipe) tileEntity).isConnectableSide(dir);
        }
        return false;
    }

    @Override
    @Deprecated
    public boolean isOpaqueCube(IBlockState blockState) {
        return false;
    }

    @Override
    @Deprecated
    public boolean isFullCube(IBlockState blockState) {
        return false;
    }

}

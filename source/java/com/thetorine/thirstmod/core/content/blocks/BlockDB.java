package com.thetorine.thirstmod.core.content.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.thetorine.thirstmod.core.content.packs.DrinkLists;
import com.thetorine.thirstmod.core.main.ThirstMod;
import com.thetorine.thirstmod.core.utils.Constants;

public class BlockDB extends BlockContainer {
	private static boolean keepInventory;
	public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

	public BlockDB() {
		super(Material.rock);
		setResistance(5f);
		setHardness(4f);
		setCreativeTab(ThirstMod.thirst);
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
	}

	@Override
	public TileEntity createNewTileEntity(World world, int i) {
		return new TileEntityDB();
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ) {
		if(DrinkLists.LOADED_DRINKS.size() > 0) {
			player.openGui(ThirstMod.instance, Constants.DRINKS_BREWER_ID, world, pos.getX(), pos.getY(), pos.getZ());
		} else {
			if(player.worldObj.isRemote) {
				player.addChatComponentMessage(new ChatComponentText("There are no drink packs installed!"));
				player.addChatComponentMessage(new ChatComponentText("Download from Thirst Mod post on Minecraft Forums and place in [minecraft-dir]/thirstmod/content"));
			}
		}
		return true;
	}
	
	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		super.onBlockAdded(world, pos, state);
		setDefaultDirection(world, pos, state);
	}
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		worldIn.setBlockState(pos, state.withProperty(FACING, placer.func_174811_aO().getOpposite()), 2);
	}
	
	public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return this.getDefaultState().withProperty(FACING, placer.func_174811_aO().getOpposite());
	}

	private void setDefaultDirection(World worldIn, BlockPos p_176445_2_, IBlockState p_176445_3_) {
		if (!worldIn.isRemote) {
			Block block = worldIn.getBlockState(p_176445_2_.offsetNorth()).getBlock();
			Block block1 = worldIn.getBlockState(p_176445_2_.offsetSouth()).getBlock();
			Block block2 = worldIn.getBlockState(p_176445_2_.offsetWest()).getBlock();
			Block block3 = worldIn.getBlockState(p_176445_2_.offsetEast()).getBlock();
			EnumFacing enumfacing = (EnumFacing) p_176445_3_.getValue(FACING);

			if (enumfacing == EnumFacing.NORTH && block.isFullBlock() && !block1.isFullBlock()) {
				enumfacing = EnumFacing.SOUTH;
			} else if (enumfacing == EnumFacing.SOUTH && block1.isFullBlock() && !block.isFullBlock()) {
				enumfacing = EnumFacing.NORTH;
			} else if (enumfacing == EnumFacing.WEST && block2.isFullBlock() && !block3.isFullBlock()) {
				enumfacing = EnumFacing.EAST;
			} else if (enumfacing == EnumFacing.EAST && block3.isFullBlock() && !block2.isFullBlock()) {
				enumfacing = EnumFacing.WEST;
			}
			worldIn.setBlockState(p_176445_2_,p_176445_3_.withProperty(FACING, enumfacing), 2);
		}
	}
	
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		if (!keepInventory) {
			TileEntity tileentity = worldIn.getTileEntity(pos);
			if (tileentity instanceof TileEntityFurnace) {
				InventoryHelper.dropInventoryItems(worldIn, pos, (TileEntityFurnace) tileentity);
				worldIn.updateComparatorOutputLevel(pos, this);
			}
		}
		super.breakBlock(worldIn, pos, state);
	}
	
	@Override
	public int getRenderType() {
		return 3;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IBlockState getStateForEntityRender(IBlockState state) {
		return this.getDefaultState().withProperty(FACING, EnumFacing.SOUTH);
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		EnumFacing enumfacing = EnumFacing.getFront(meta);

		if (enumfacing.getAxis() == EnumFacing.Axis.Y) {
			enumfacing = EnumFacing.NORTH;
		}

		return this.getDefaultState().withProperty(FACING, enumfacing);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return ((EnumFacing) state.getValue(FACING)).getIndex();
	}

	@Override
	protected BlockState createBlockState() {
		return new BlockState(this, new IProperty[] { FACING });
	}
}

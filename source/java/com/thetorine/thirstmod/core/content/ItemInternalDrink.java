package com.thetorine.thirstmod.core.content;

import java.util.List;
import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenJungle;
import net.minecraftforge.fml.common.FMLCommonHandler;

import com.thetorine.thirstmod.core.client.player.ClientStats;
import com.thetorine.thirstmod.core.main.ThirstMod;
import com.thetorine.thirstmod.core.player.PlayerContainer;
import com.thetorine.thirstmod.core.utils.Constants;

public class ItemInternalDrink extends Item {
	public boolean addItem;
	public int id;
	
	public int thirstHeal;
	public float thirstSaturation;
	public float thirstPoison;
	
	public Item returnItem;
	
	public ItemInternalDrink(int replenish, float saturation, float poison, String texture, int stacksize) {
		this.id = 1;
		this.thirstHeal = replenish;
		this.thirstSaturation = saturation;
		this.thirstPoison = poison;
		this.setMaxStackSize(stacksize > 0 ? stacksize : Constants.DRINKS_STACKSIZE);
		this.returnItem = ItemLoader.cup;
		register(texture);
	}
	
	public ItemInternalDrink(String texture) {
		super();
		this.id = 0;
		register(texture);
	}
	
	public void register(String texture) {
		this.setCreativeTab(ThirstMod.thirst); 
	}
	
	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if(id == 0) {
			MovingObjectPosition mop = this.getMovingObjectPositionFromPlayer(world, player, true);
			if (mop != null) {
				if(mop.typeOfHit == MovingObjectType.BLOCK) {
					if (world.getBlockState(mop.getBlockPos()).getBlock().getMaterial() == Material.water) {
						returnItem = ItemLoader.water_cup;
						addItem = true;
					} else if (world.getBlockState(mop.getBlockPos()).getBlock() == Blocks.leaves) {
						Random random = new Random();
						if (world.getBiomeGenForCoords(mop.getBlockPos()) instanceof BiomeGenJungle) {
							world.setBlockToAir(mop.getBlockPos());
							if (random.nextFloat() < 0.3f) {
								addItem = true;
							}
						}
					}
					
					if(addItem) {
						 --stack.stackSize;
						 if(stack.stackSize <= 0) {
							 return new ItemStack(returnItem);
						 }
						 if(!player.inventory.addItemStackToInventory(new ItemStack(ItemLoader.water_cup))) {
							 world.spawnEntityInWorld(new EntityItem(world, player.posX, player.posY, player.posZ, new ItemStack(ItemLoader.water_cup)));
				         }
					}
					addItem = false;
				}
			}
		} else if(canDrink(player) || player.capabilities.isCreativeMode) {
			player.setItemInUse(stack, getMaxItemUseDuration(stack));
		}
		return stack;
	}
	
	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World world, EntityPlayer player) {
		if(!world.isRemote) {
			stack.stackSize--;
			
			PlayerContainer playerCon = PlayerContainer.getPlayer(player);
			playerCon.addStats(thirstHeal, thirstSaturation);
			if ((thirstPoison > 0) && ThirstMod.config.POISON_ON) {
				Random rand = new Random();
				if (rand.nextFloat() < thirstPoison) {
					playerCon.getStats().poisonLogic.poisonPlayer();
				}
			}
			player.inventory.addItemStackToInventory(new ItemStack(returnItem));
		}
		return stack;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean flag) {
		super.addInformation(stack, player, list, flag);
		
		float f = Float.parseFloat(Integer.toString(thirstHeal)) / 2;
		String s2 = Float.toString(f);
		list.add("Heals " + (s2.endsWith(".0") ? s2.replace(".0", "") : s2) + " Droplets");
	}
	
	public ItemInternalDrink setReturnItem(Item returnItem) {
		this.returnItem = returnItem;
		return this;
	}
	
	@Override
	public EnumAction getItemUseAction(ItemStack itemstack) {
		return EnumAction.DRINK;
	}
	
	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 32;
	}
	
	public boolean canDrink(EntityPlayer player) {
		switch(FMLCommonHandler.instance().getEffectiveSide()) {
			case CLIENT: return ClientStats.getInstance().level < 20;
			case SERVER: return PlayerContainer.getPlayer(player).stats.thirstLevel < 20;
		}
		return false;
	}
}

package com.thetorine.thirstmod.core.content.blocks;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotFurnaceOutput;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ContainerDB extends Container {
	private TileEntityDB drinksBrewer;
	private int lastFuelLevel;
	private int lastBrewTime;
	private int lastMaxFuelLevel;

	public ContainerDB(InventoryPlayer inv, TileEntityDB tile) {
		this.drinksBrewer = tile;
		this.addSlotToContainer(new Slot(tile, 0, 58, 24)); // brewing item
		this.addSlotToContainer(new Slot(tile, 1, 30, 24)); // glass
		this.addSlotToContainer(new Slot(tile, 2, 44, 47)); // fuel
		this.addSlotToContainer(new SlotFurnaceOutput(inv.player, tile, 3, 116, 35)); // return
		int i;

		for (i = 0; i < 3; ++i) {
			for (int var4 = 0; var4 < 9; ++var4) {
				this.addSlotToContainer(new Slot(inv, var4 + (i * 9) + 9, 8 + (var4 * 18), 84 + (i * 18)));
			}
		}

		for (i = 0; i < 9; ++i) {
			this.addSlotToContainer(new Slot(inv, i, 8 + (i * 18), 142));
		}
	}

	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();
		for (int i = 0; i < this.crafters.size(); ++i) {
			ICrafting craft = (ICrafting) this.crafters.get(i);
			if(lastFuelLevel != drinksBrewer.getField(0)) {
				craft.sendProgressBarUpdate(this, drinksBrewer.getField(0), 0);
			}
			if(lastBrewTime != drinksBrewer.getField(1)) {
				craft.sendProgressBarUpdate(this, drinksBrewer.getField(1), 1);
			}
			if(lastMaxFuelLevel != drinksBrewer.maxFuelLevel) {
				craft.sendProgressBarUpdate(this, drinksBrewer.getField(2), 2);
			}
		}
		lastFuelLevel = drinksBrewer.getField(0);
		lastBrewTime = drinksBrewer.getField(1);
		lastMaxFuelLevel = drinksBrewer.getField(2);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void updateProgressBar(int packet, int data) {
		drinksBrewer.setField(packet, data);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int par1) {
		super.transferStackInSlot(player, par1);
		ItemStack stack = null;
		Slot i = (Slot) this.inventorySlots.get(par1);

		if ((i != null) && i.getHasStack()) {
			ItemStack var4 = i.getStack();
			stack = var4.copy();
			if (par1 == 2) {
				i.onSlotChange(var4, stack);
			} else if ((par1 != 1) && (par1 != 0) && (par1 != 3)) {
				if (DBRecipes.instance().getBrewingResult(var4) != null) {
					if (!this.mergeItemStack(var4, 0, 1, false)) { return null; }
				} else if (drinksBrewer.getItemFuelValue(var4) > 0) {
					if (!this.mergeItemStack(var4, 3, 2, false)) { return null; }
				} else if ((par1 >= 4) && (par1 < 30)) {
					if (!this.mergeItemStack(var4, 30, 39, false)) { return null; }
				} 
			} else if (!this.mergeItemStack(var4, 4, 39, false)) { return null; }

			if (var4.stackSize == 0) {
				i.putStack((ItemStack) null);
			} else {
				i.onSlotChanged();
			}

			if (var4.stackSize == stack.stackSize) { return null; }

			i.onPickupFromSlot(player, var4);
		}
		return stack;
	}

	@Override
	public void addCraftingToCrafters(ICrafting craft) {
		super.addCraftingToCrafters(craft);
		craft.func_175173_a(this, drinksBrewer);
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return this.drinksBrewer.isUseableByPlayer(entityplayer);
	}
}

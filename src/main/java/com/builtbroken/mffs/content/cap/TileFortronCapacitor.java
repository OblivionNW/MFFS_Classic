package com.builtbroken.mffs.content.cap;

import com.builtbroken.jlib.data.vector.IPos3D;
import com.builtbroken.mc.imp.transform.vector.Location;
import com.builtbroken.mffs.MFFS;
import com.builtbroken.mffs.MFFSSettings;
import com.builtbroken.mffs.api.card.ICardInfinite;
import com.builtbroken.mffs.api.card.ICoordLink;
import com.builtbroken.mffs.api.fortron.FrequencyGrid;
import com.builtbroken.mffs.api.fortron.IFortronCapacitor;
import com.builtbroken.mffs.api.fortron.IFortronFrequency;
import com.builtbroken.mffs.api.modules.IFieldModule;
import com.builtbroken.mffs.api.utils.FortronHelper;
import com.builtbroken.mffs.api.vector.Vector3D;
import com.builtbroken.mffs.common.TransferMode;
import com.builtbroken.mffs.common.items.card.ItemCardFrequency;
import com.builtbroken.mffs.common.items.card.ItemCardLink;
import com.builtbroken.mffs.common.items.modules.upgrades.ItemModuleScale;
import com.builtbroken.mffs.common.items.modules.upgrades.ItemModuleSpeed;
import com.builtbroken.mffs.prefab.ModuleInventory;
import com.builtbroken.mffs.prefab.tile.TileModuleAcceptor;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Calclavia
 */
public class TileFortronCapacitor extends TileModuleAcceptor implements IFortronCapacitor
{

    /* Current distribution method */
    private TransferMode mode = TransferMode.EQUALIZE; //TODO phase out transfer mode

    /**
     * Constructor.
     */
    public TileFortronCapacitor()
    {
        this.fortronCapacity = 700; //TODO move to config
        this.fortronCapacityBoostPerCard = 10; //TODO move to config
        this.moduleInventory = new ModuleInventory(this, 2, getSizeInventory());
    }

    @Override
    public void updateEntity()
    {
        super.updateEntity();

        if (this.isActive())
        {
            int cost = getFortronCost() + MFFSSettings.CAPACITOR_POWER_DRAIN; //TODO remove, storage shouldn't cost energy
            if (cost > 0)
            {
                requestFortron(cost, true);
            }

            //TODO: Change the draining to remove X% of transfered fortron.
            if (this.ticks % 10 == 0)
            { //cannot run if there is 0 energy!
                Set<IFortronFrequency> connected = new HashSet<>();
                for (ItemStack stack : getCards()) //TODO replace with iterator
                {
                    if (stack == null)
                    {
                        continue;
                    }

                    if (stack.getItem() instanceof ICardInfinite) //TODO change card to add energy to machine or for getEnergy() to return infinite
                    {
                        setFortronEnergy(getFortronCapacity());
                    }
                    else if (stack.getItem() instanceof ICoordLink) //TODO phase out
                    {
                        Location link = ((ICoordLink) stack.getItem()).getLink(stack);
                        if (link != null)
                        {
                            TileEntity link_machine = link.getTileEntity(this.worldObj);
                            if (link_machine instanceof IFortronFrequency)
                            {
                                connected.add(this);
                                connected.add((IFortronFrequency) link_machine);
                            }
                        }
                    }
                }
                if (connected.isEmpty())
                {
                    getLinkedDevices(connected);
                }

                FortronHelper.transfer(this, connected, mode, getTransmissionRate()); //TODO replace with internal method
            }
        }
    }

    /**
     * Returns true if automation is allowed to insert the given stack (ignoring stack size) into the given slot.
     *
     * @param slot
     * @param stack
     */
    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) //TODO phase out for inventory object
    {
        if (slot == 0)
        {
            return stack.getItem() instanceof ICardInfinite || stack.getItem() instanceof ItemCardLink;
        }
        else if (slot == 1)
        {
            return stack.getItem() instanceof ItemCardFrequency || stack.getItem() instanceof ItemCardLink;
        }
        return stack.getItem() instanceof IFieldModule;
    }

    /**
     * @return
     */
    @Override
    public Set<ItemStack> getCards() //TODO phase out for inventory object
    {
        Set<ItemStack> set = new HashSet<>();
        set.add(super.getCard());
        set.add(getStackInSlot(1));
        return set;
    }

    @Override
    public void getLinkedDevices(Set<IFortronFrequency> list)
    {
        list.addAll(FrequencyGrid.instance().getFortronTilesExcluding(this, new Vector3D((IPos3D)this), getTransmissionRange(), getFrequency()));
    }

    @Override
    public int getTransmissionRange()
    {
        return 15 + getModuleCount(ItemModuleScale.class); //TODO move magic number to config
    }

    @Override
    public int getTransmissionRate()
    {
        return 250 + 50 * getModuleCount(ItemModuleSpeed.class); //TODO move magic number to config
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        nbt.setByte("transferMode", (byte) mode.ordinal());
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        this.mode = TransferMode.values()[nbt.getByte("transferMode")];
    }

    @Override
    public int getSizeInventory()
    {
        return 5;
    }

    public TransferMode getTransferMode()
    {
        return this.mode;
    }

    @Override
    public float getAmplifier()
    {
        return .001F; //TODO why so low?
    }

    @Override
    public void writeDescPacket(ByteBuf buf)
    {
        super.writeDescPacket(buf);
    }

    @Override
    public void readDescPacket(ByteBuf buf)
    {
        super.readDescPacket(buf);
    }

    @Override
    public List<ItemStack> getRemovedItems(EntityPlayer entityPlayer)
    {
        List<ItemStack> stack = super.getRemovedItems(entityPlayer);
        stack.add(new ItemStack(MFFS.fortronCapacitor));
        return stack;
    }
}

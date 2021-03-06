package com.builtbroken.mffs.api.card;

import com.builtbroken.mc.imp.transform.vector.Location;
import net.minecraft.item.ItemStack;

/**
 * A grid ModularForcefieldSystem uses to search for machines with frequencies that can be linked and spread Fortron
 * energy.
 *
 * @author Calclavia
 */
@Deprecated //Will be replaced by VoltzEngine version
public interface ICoordLink
{
    void setLink(ItemStack paramItemStack, Location paramVectorWorld);

    Location getLink(ItemStack paramItemStack);
}
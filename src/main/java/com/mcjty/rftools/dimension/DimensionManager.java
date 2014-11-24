package com.mcjty.rftools.dimension;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import java.util.HashMap;
import java.util.Map;

public class DimensionManager extends WorldSavedData {
    public static final String DIMMANAGER_NAME = "DimensionManager";
    private static DimensionManager instance = null;

    private final Map<Integer, DimensionDescriptor> dimensions = new HashMap<Integer, DimensionDescriptor>();

    public DimensionManager(String identifier) {
        super(identifier);
    }

    public static void clearInstance() {
        instance = null;
    }

    public static DimensionManager getDimensionManager(World world) {
        if (world.isRemote) {
            return null;
        }
        if (instance != null) {
            return instance;
        }
        instance = (DimensionManager) world.mapStorage.loadData(DimensionManager.class, DIMMANAGER_NAME);
        if (instance == null) {
            instance = new DimensionManager(DIMMANAGER_NAME);
        }
        return instance;
    }



    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        dimensions.clear();
        NBTTagList lst = tagCompound.getTagList("dimensions", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < lst.tagCount() ; i++) {
            NBTTagCompound tc = lst.getCompoundTagAt(i);
            int id = tc.getInteger("id");
            DimensionDescriptor descriptor = new DimensionDescriptor(tc);
            dimensions.put(id, descriptor);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        NBTTagList lst = new NBTTagList();
        for (Map.Entry<Integer,DimensionDescriptor> me : dimensions.entrySet()) {
            NBTTagCompound tc = new NBTTagCompound();
            tc.setInteger("id", me.getKey());
            me.getValue().writeToNBT(tc);
            lst.appendTag(tc);
        }
        tagCompound.setTag("dimensions", lst);
    }
}

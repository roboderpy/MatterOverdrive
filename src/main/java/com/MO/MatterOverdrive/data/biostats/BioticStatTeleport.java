package com.MO.MatterOverdrive.data.biostats;

import com.MO.MatterOverdrive.MatterOverdrive;
import com.MO.MatterOverdrive.Reference;
import com.MO.MatterOverdrive.entity.AndroidPlayer;
import com.MO.MatterOverdrive.handler.KeyHandler;
import com.MO.MatterOverdrive.network.packet.server.PacketTeleportPlayer;
import com.MO.MatterOverdrive.proxy.ClientProxy;
import com.MO.MatterOverdrive.util.MOPhysicsHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fluids.IFluidBlock;
import org.lwjgl.input.Keyboard;

/**
 * Created by Simeon on 5/29/2015.
 */
public class BioticStatTeleport extends AbstractBioticStat {

    public static final String EFFECT_KEY_LAST_TELEPORT = "LastTeleport";
    public static final int TELEPORT_DELAY = 40;
    public static final int ENERGY_PER_TELEPORT = 4096;
    @SideOnly(Side.CLIENT)
    private boolean hasPressedKey;
    public BioticStatTeleport(String name, int xp)
    {
        super(name, xp);
        setShowOnHud(true);
    }

    @Override
    public String getDetails(int level)
    {
        return super.getDetails(level).replace("$0", Keyboard.getKeyName(ClientProxy.keyHandler.getBinding(KeyHandler.ABILITY_USE_KEY).getKeyCode())).replace("$1", EnumChatFormatting.YELLOW.toString() + ENERGY_PER_TELEPORT + " RF" + EnumChatFormatting.GRAY);
    }

    @Override
    public void onAndroidUpdate(AndroidPlayer android, int level)
    {

    }

    @Override
    public void onKeyPress(AndroidPlayer androidPlayer, int level, int keycode, boolean down)
    {
        if (keycode == ClientProxy.keyHandler.getBinding(KeyHandler.ABILITY_USE_KEY).getKeyCode())
        {
            if (!down && hasPressedKey) {
                Vec3 pos = getPos(androidPlayer);
                if (pos != null) {
                    MatterOverdrive.packetPipeline.sendToServer(new PacketTeleportPlayer(pos.xCoord, pos.yCoord, pos.zCoord));
                    hasPressedKey = false;
                }
            }else
            {
                hasPressedKey = true;
            }
        }
    }

    public Vec3 getPos(AndroidPlayer androidPlayer) {
        MovingObjectPosition position = MOPhysicsHelper.rayTraceForBlocks(androidPlayer.getPlayer(), androidPlayer.getPlayer().worldObj, 32, 0, Vec3.createVectorHelper(0,0,0),true,true);
        if (position != null && position.typeOfHit != MovingObjectPosition.MovingObjectType.MISS)
        {
            ForgeDirection side = ForgeDirection.getOrientation(position.sideHit);
            Vec3 pos = getTopSafeBlock(androidPlayer.getPlayer().worldObj, position.blockX, position.blockY, position.blockZ,position.sideHit);
            if (pos != null) {
                return Vec3.createVectorHelper(pos.xCoord + 0.5, pos.yCoord, pos.zCoord + 0.5);
            }
            return null;
        }

        position = MOPhysicsHelper.rayTrace(androidPlayer.getPlayer(), androidPlayer.getPlayer().worldObj, 6, 0, Vec3.createVectorHelper(0, 0, 0),true,true);
        if (position != null)
        {
            return position.hitVec;
        }
        return null;
    }

    public Vec3 getTopSafeBlock(World world,int x,int y,int z,int side)
    {
        int airBlockCount = 0;
        int height = Math.min(y + 8, world.getActualHeight());
        for (int i = y;i < height;i++)
        {
            if (!world.getBlock(x,i,z).isBlockSolid(world,x,i,z,world.getBlockMetadata(x,i,z)) || world.getBlock(x,i,z) instanceof IFluidBlock)
            {
                airBlockCount++;
            }else
            {
                airBlockCount = 0;
            }

            if (airBlockCount >= 2)
            {
                return Vec3.createVectorHelper(x,i-1,z);
            }
        }

        ForgeDirection direction = ForgeDirection.getOrientation(side);
        x += direction.offsetX;
        y += direction.offsetY;
        z += direction.offsetZ;

        if (!world.getBlock(x,y+1,z).isBlockSolid(world,x,y,z,world.getBlockMetadata(x,y+1,z)) && !world.getBlock(x,y+2,z).isBlockSolid(world,x,y,z,world.getBlockMetadata(x,y+2,z)))
        {
            return Vec3.createVectorHelper(x,y,z);
        }

        return null;
    }

    @Override
    public void onLivingEvent(AndroidPlayer androidPlayer, int level, LivingEvent event) {

    }

    @Override
    public void changeAndroidStats(AndroidPlayer androidPlayer, int level, boolean enabled)
    {
        if (androidPlayer.getPlayer().worldObj.isRemote) {
            if (!isEnabled(androidPlayer, level)) {
                hasPressedKey = false;
            }
        }
    }

    @Override
    public boolean isEnabled(AndroidPlayer android, int level)
    {
        return super.isEnabled(android,level) && android.getEffectLong(EFFECT_KEY_LAST_TELEPORT) <= android.getPlayer().worldObj.getTotalWorldTime() && android.extractEnergy(ENERGY_PER_TELEPORT,true) == ENERGY_PER_TELEPORT;
    }

    @Override
    public boolean isActive(AndroidPlayer androidPlayer, int level)
    {
        return isEnabled(androidPlayer,level);
    }

    @SideOnly(Side.CLIENT)
    public boolean getHasPressedKey()
    {
        return hasPressedKey;
    }
}

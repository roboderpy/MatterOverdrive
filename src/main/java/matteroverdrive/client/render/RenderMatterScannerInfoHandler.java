/*
 * This file is part of Matter Overdrive
 * Copyright (c) 2015., Simeon Radivoev, All rights reserved.
 *
 * Matter Overdrive is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Matter Overdrive is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Matter Overdrive.  If not, see <http://www.gnu.org/licenses>.
 */

package matteroverdrive.client.render;

import matteroverdrive.Reference;
import matteroverdrive.api.IScannable;
import matteroverdrive.api.inventory.IBlockScanner;
import matteroverdrive.client.RenderHandler;
import matteroverdrive.client.render.tileentity.TileEntityRendererPatternMonitor;
import matteroverdrive.entity.player.MOPlayerCapabilityProvider;
import matteroverdrive.proxy.ClientProxy;
import matteroverdrive.util.MatterHelper;
import matteroverdrive.util.RenderUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

/**
 * Created by Simeon on 5/11/2015.
 */
public class RenderMatterScannerInfoHandler implements IWorldLastRenderer {
    public final ResourceLocation spinnerTexture = new ResourceLocation(Reference.PATH_ELEMENTS + "spinner.png");
    private final DecimalFormat healthFormater = new DecimalFormat("#.##");

    public static void rotateFromSide(EnumFacing side, float yaw) {
        if (side == EnumFacing.UP) {
            glRotatef(Math.round(yaw / 90) * 90 - 180, 0, -1, 0);
            GlStateManager.rotate(90, 1, 0, 0);
        } else if (side == EnumFacing.DOWN) {
            glRotatef(Math.round(yaw / 90) * 90 - 180, 0, -1, 0);
            GlStateManager.rotate(-90, 1, 0, 0);
        } else if (side == EnumFacing.WEST) {
            GlStateManager.rotate(90, 0, 1, 0);
            GlStateManager.rotate(180, 0, 0, 1);
        } else if (side == EnumFacing.EAST) {
            GlStateManager.rotate(-90, 0, 1, 0);
            GlStateManager.rotate(180, 0, 0, 1);
        } else if (side == EnumFacing.NORTH) {
            GlStateManager.rotate(180, 0, 0, 1);
        } else if (side == EnumFacing.SOUTH) {
            GlStateManager.rotate(180, 0, 1, 0);
            GlStateManager.rotate(180, 0, 0, 1);
        }
    }

    public void onRenderWorldLast(RenderHandler renderHandler, RenderWorldLastEvent event) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        boolean holdingPad = false;
        EnumHand hand = EnumHand.MAIN_HAND;
        ItemStack heldItem = ItemStack.EMPTY;
        if (!player.getHeldItem(EnumHand.MAIN_HAND).isEmpty()) {
            hand = EnumHand.MAIN_HAND;
            heldItem = player.getHeldItem(EnumHand.MAIN_HAND);
            if (heldItem.getItem() instanceof IBlockScanner) {
                holdingPad = true;
            } else {
                heldItem = ItemStack.EMPTY;
            }
        } else if (!player.getHeldItem(EnumHand.OFF_HAND).isEmpty()) {
            hand = EnumHand.OFF_HAND;
            heldItem = player.getHeldItem(EnumHand.OFF_HAND);
            if (heldItem.getItem() instanceof IBlockScanner) {
                holdingPad = true;
            } else {
                heldItem = ItemStack.EMPTY;
            }
        }

        if (holdingPad && !heldItem.isEmpty() && player.getActiveHand() == hand) {
            GlStateManager.pushMatrix();
            renderInfo(Minecraft.getMinecraft().player, heldItem, event.getPartialTicks());
            GlStateManager.popMatrix();
        } else if (MOPlayerCapabilityProvider.GetAndroidCapability(Minecraft.getMinecraft().player).isAndroid()) {
            renderInfo(Minecraft.getMinecraft().player, Minecraft.getMinecraft().objectMouseOver, null, event.getPartialTicks());
        }
    }

    private void renderInfo(EntityPlayer player, ItemStack scanner, float ticks) {
        IBlockScanner scannerItem = (IBlockScanner) scanner.getItem();
        RayTraceResult position = scannerItem.getScanningPos(scanner, player);
        renderInfo(player, position, scanner, ticks);
    }

    private void renderInfo(EntityPlayer player, RayTraceResult position, ItemStack scanner, float ticks) {
        Vec3d playerPos = player.getPositionEyes(ticks);

        glPushAttrib(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
        GlStateManager.disableDepth();
        GlStateManager.blendFunc(GL_ONE, GL_ONE);
        GlStateManager.enableBlend();
        RenderUtils.applyColor(Reference.COLOR_HOLO);

        if (position != null) {

            if (position.typeOfHit == RayTraceResult.Type.BLOCK) {

                IBlockState blockState = player.world.getBlockState(position.getBlockPos());
                if (blockState != null) {
                    renderBlockInfo(blockState, position, player, playerPos, scanner, scanner == null);
                }
            } else if (position.typeOfHit == RayTraceResult.Type.ENTITY) {
                if (position.entityHit != null) {
                    renderEntityInfo(position.entityHit, position, player, playerPos, ticks);
                }
            }
        }
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.popAttrib();
    }

    private void renderBlockInfo(IBlockState block, RayTraceResult position, EntityPlayer player, Vec3d playerPos, ItemStack scanner, boolean infoOnly) {
        double offset = 0.1;
        EnumFacing side = position.sideHit;
        List<String> infos = new ArrayList<>();
        if (block instanceof IScannable) {
            ((IScannable) block).addInfo(player.world, position.getBlockPos().getX(), position.getBlockPos().getY(), position.getBlockPos().getZ(), infos);

        } else if (player.world.getTileEntity(position.getBlockPos()) instanceof IScannable) {
            ((IScannable) player.world.getTileEntity(position.getBlockPos())).addInfo(player.world, position.getBlockPos().getX(), position.getBlockPos().getY(), position.getBlockPos().getZ(), infos);
        } else if (infoOnly) {
            return;
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(side.getDirectionVec().getX() * 0.5 + (position.getBlockPos().getX() + 0.5) - playerPos.x, side.getDirectionVec().getY() * 0.5 + (position.getBlockPos().getY() + 0.5) - playerPos.y + player.getEyeHeight(), side.getDirectionVec().getZ() * 0.5 + (position.getBlockPos().getZ() + 0.5) - playerPos.z);
        rotateFromSide(side, player.rotationYaw);
        GlStateManager.translate(-0.5, -0.5, -offset);
        drawInfoPlane(0);

        ItemStack blockItemStack = block.getBlock().getPickBlock(block, position, player.world, position.getBlockPos(), player);

        int matter = MatterHelper.getMatterAmountFromItem(blockItemStack);
        if (matter > 0) {
            infos.add("Matter: " + MatterHelper.formatMatter(matter));
        }
        String blockName = "Unknown";

        try {
            if (!blockItemStack.isEmpty()) {
                blockName = blockItemStack.getDisplayName();
            } else {
                blockName = player.world.getBlockState(position.getBlockPos()).getBlock().getLocalizedName();
            }
        } catch (Exception e) {
            blockName = player.world.getBlockState(position.getBlockPos()).getBlock().getLocalizedName();
        } finally {
            drawInfoList(blockName, infos);
        }

        if (!(block instanceof IScannable) && scanner != null) {
            drawProgressBar(scanner, player);
        }
        GlStateManager.popMatrix();
    }

    private void drawProgressBar(ItemStack scanner, EntityPlayer player) {
        GlStateManager.pushMatrix();
        renderer().bindTexture(spinnerTexture);

        int count = player.getItemInUseCount();
        int maxCount = scanner.getMaxItemUseDuration();

        GlStateManager.alphaFunc(GL_GREATER, (float) count / (float) maxCount);
        RenderUtils.applyColorWithMultipy(Reference.COLOR_HOLO, 0.5f);
        RenderUtils.drawPlane(0.35, 0.45, -0.1, 0.3, 0.3);
        GlStateManager.popMatrix();
    }

    private void renderEntityInfo(Entity entity, RayTraceResult position, EntityPlayer player, Vec3d playerPos, float ticks) {
        if (!entity.isInvisibleToPlayer(player)) {
            double offset = 0.1;
            Vec3d entityPos;
            String name = entity.getDisplayName().getFormattedText();
            List<String> infos = new ArrayList<>();
            if (entity instanceof EntityLivingBase) {
                entityPos = entity.getPositionEyes(ticks);
                entityPos = entityPos.addVector(0, entity.getEyeHeight(), 0);
                infos.add("Health: " + (healthFormater.format(((EntityLivingBase) entity).getHealth()) + " / " + ((EntityLivingBase) entity).getMaxHealth()));

                GlStateManager.pushMatrix();
                GlStateManager.translate(entityPos.x - playerPos.x, entityPos.y - playerPos.y, entityPos.z - playerPos.z);
                GlStateManager.rotate(player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * ticks, 0, -1, 0);
                GlStateManager.rotate(player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * ticks, 1, 0, 0);
                GlStateManager.translate(1, 0, 0);
                GlStateManager.rotate(180, 0, 0, 1);
                GlStateManager.translate(-0.5, -0.5, -offset);
                drawInfoPlane(0.5);
                drawInfoList(name, infos);
                GlStateManager.popMatrix();
            }
        }
    }

    private void drawInfoPlane(double opacity) {
        if (opacity > 0) {
            GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.disableTexture2D();
            GlStateManager.color(0, 0, 0, (float) opacity);
            RenderUtils.drawPlane(1);
            GlStateManager.enableTexture2D();
        }

        GlStateManager.blendFunc(GL_ONE, GL_ONE_MINUS_SRC_COLOR);
        RenderUtils.applyColorWithMultipy(Reference.COLOR_HOLO, 0.05f);
        renderer().bindTexture(TileEntityRendererPatternMonitor.screenTextureBack);
        RenderUtils.drawPlane(0, 0, -0.01, 1, 1);
    }

    private void drawInfoList(String name, List<String> infos) {
        GlStateManager.pushMatrix();
        int width = fontRenderer().getStringWidth(name);
        GlStateManager.translate(0.5, 0.5, -0.05);
        GlStateManager.scale(0.01, 0.01, 0.01);
        fontRenderer().drawString(name, -width / 2, -40, Reference.COLOR_HOLO.getColor());

        for (int i = 0; i < infos.size(); i++) {
            width = fontRenderer().getStringWidth(infos.get(i));
            fontRenderer().drawString(infos.get(i), -width / 2, -24 + 16 * i, Reference.COLOR_HOLO.getColor());
        }
        GlStateManager.popMatrix();
    }

    private FontRenderer fontRenderer() {
        return ClientProxy.moFontRender;
    }

    private TextureManager renderer() {
        return Minecraft.getMinecraft().renderEngine;
    }
}

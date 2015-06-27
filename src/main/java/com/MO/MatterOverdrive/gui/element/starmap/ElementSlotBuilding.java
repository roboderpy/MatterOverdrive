package com.MO.MatterOverdrive.gui.element.starmap;

import cofh.lib.gui.GuiBase;
import com.MO.MatterOverdrive.Reference;
import com.MO.MatterOverdrive.api.inventory.starmap.IBuilding;
import com.MO.MatterOverdrive.container.slot.MOSlot;
import com.MO.MatterOverdrive.gui.element.ElementInventorySlot;
import com.MO.MatterOverdrive.tile.TileEntityMachineStarMap;
import net.minecraft.util.IIcon;

import java.text.DecimalFormat;

/**
 * Created by Simeon on 6/23/2015.
 */
public class ElementSlotBuilding extends ElementInventorySlot
{
    TileEntityMachineStarMap starMap;

    public ElementSlotBuilding(GuiBase gui, MOSlot slot, int posX, int posY, int width, int height, String type, IIcon icon,TileEntityMachineStarMap starMap) {
        super(gui, slot, posX, posY, width, height, type, icon);
        this.starMap = starMap;
    }

    @Override
    public void drawForeground(int mouseX, int mouseY)
    {
        if (starMap.getPlanet() != null) {
            if (getSlot().getStack() != null) {
                if (getSlot().getStack().getItem() instanceof IBuilding)
                {
                    if (starMap.getPlanet().canBuild((IBuilding)getSlot().getStack().getItem(),getSlot().getStack())) {
                        int buildTime = ((IBuilding) getSlot().getStack().getItem()).getBuildTime(getSlot().getStack());
                        int maxBuildTime = ((IBuilding) getSlot().getStack().getItem()).maxBuildTime(getSlot().getStack(), starMap.getPlanet());
                        getFontRenderer().drawString(DecimalFormat.getPercentInstance().format((double) buildTime / (double) maxBuildTime), posX - 24, posY + 6, Reference.COLOR_HOLO.getColor());
                    }else
                    {
                        String info = "Can't build";
                        int width = getFontRenderer().getStringWidth(info);
                        getFontRenderer().drawString(info, posX - width - 4, posY + 7, Reference.COLOR_HOLO_RED.getColor());
                    }
                }
            }
        }
    }
}

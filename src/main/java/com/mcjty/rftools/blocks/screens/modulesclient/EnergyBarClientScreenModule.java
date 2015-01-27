package com.mcjty.rftools.blocks.screens.modulesclient;

import com.mcjty.gui.RenderHelper;
import com.mcjty.gui.events.ButtonEvent;
import com.mcjty.gui.events.ColorChoiceEvent;
import com.mcjty.gui.events.TextEvent;
import com.mcjty.gui.layout.HorizontalAlignment;
import com.mcjty.gui.layout.HorizontalLayout;
import com.mcjty.gui.layout.VerticalLayout;
import com.mcjty.gui.widgets.*;
import com.mcjty.rftools.blocks.screens.ModuleGuiChanged;
import com.mcjty.varia.Coordinate;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class EnergyBarClientScreenModule implements ClientScreenModule {
    private String line = "";
    private int color = 0xffffff;
    private int rfcolor = 0xffffff;
    private int rfcolor_neg = 0xffffff;
    private int dim = 0;
    private boolean hidebar = false;
    private boolean hidetext = false;
    private boolean showdiff = false;
    private Coordinate coordinate = Coordinate.INVALID;

    @Override
    public TransformMode getTransformMode() {
        return TransformMode.TEXT;
    }

    @Override
    public int getHeight() {
        return 10;
    }

    @Override
    public void render(FontRenderer fontRenderer, int currenty, String screenData) {
        GL11.glDisable(GL11.GL_LIGHTING);
        if (!line.isEmpty()) {
            fontRenderer.drawString(line, 7, currenty, color);
        }

        if (coordinate.isValid()) {
            if (showdiff) {
                renderEnergyDiff(fontRenderer, currenty, screenData);
            } else {
                renderEnergyLevel(fontRenderer, currenty, screenData);
            }
        } else {
            fontRenderer.drawString("<invalid>", 7 + 40, currenty, 0xff0000);
        }
    }

    private void renderEnergyDiff(FontRenderer fontRenderer, int currenty, String screenData) {
        if (screenData == null) {
            screenData = "?";
        }
        if (screenData.startsWith("-")) {
            fontRenderer.drawString(screenData + " RF/tick", 7 + 40, currenty, rfcolor_neg);
        } else {
            fontRenderer.drawString("+" + screenData + " RF/tick", 7 + 40, currenty, rfcolor);
        }
    }

    private void renderEnergyLevel(FontRenderer fontRenderer, int currenty, String screenData) {
        int energy = 0;
        int maxEnergy = 0;
        if (screenData != null) {
            int i = screenData.indexOf('/');
            if (i >= 0) {
                energy = Integer.parseInt(screenData.substring(0, i));
                maxEnergy = Integer.parseInt(screenData.substring(i+1));
            }
        }

        if (maxEnergy > 0) {
            if (!hidebar) {
                int width = 80;
                long value = (long)energy * width / (long)maxEnergy;
                if (value < 0) {
                    value = 0;
                } else if (value > width) {
                    value = width;
                }
                RenderHelper.drawHorizontalGradientRect(7 + 40, currenty, (int) (7 + 40 + value), currenty + 8, 0xffff0000, 0xff333300);
            }
            if (!hidetext) {
                fontRenderer.drawString(energy + "RF", 7 + 40, currenty, rfcolor);
            }
        }
    }

    @Override
    public Panel createGui(Minecraft mc, Gui gui, final NBTTagCompound currentData, final ModuleGuiChanged moduleGuiChanged) {
        Panel panel = new Panel(mc, gui).setLayout(new VerticalLayout());
        TextField textField = new TextField(mc, gui).setDesiredHeight(16).setTooltips("Text to use as label").addTextEvent(new TextEvent() {
            @Override
            public void textChanged(Widget parent, String newText) {
                currentData.setString("text", newText);
                moduleGuiChanged.updateData();
            }
        });
        panel.addChild(textField);
        addColorPanel(mc, gui, currentData, moduleGuiChanged, panel);
        addOptionPanel(mc, gui, currentData, moduleGuiChanged, panel);
        addMonitorPanel(mc, gui, currentData, panel);

        if (currentData != null) {
            textField.setText(currentData.getString("text"));
        }

        return panel;
    }

    private void addOptionPanel(Minecraft mc, Gui gui, final NBTTagCompound currentData, final ModuleGuiChanged moduleGuiChanged, Panel panel) {
        Panel optionPanel = new Panel(mc, gui).setLayout(new HorizontalLayout()).setDesiredHeight(16);

        final ToggleButton barButton = new ToggleButton(mc, gui).setText("Bar").setTooltips("Toggle visibility of the", "energy bar");
        barButton.addButtonEvent(new ButtonEvent() {
            @Override
            public void buttonClicked(Widget parent) {
                currentData.setBoolean("hidebar", !barButton.isPressed());
                moduleGuiChanged.updateData();
            }
        });
        optionPanel.addChild(barButton);

        final ToggleButton textButton = new ToggleButton(mc, gui).setText("Text").setTooltips("Toggle visibility of the", "energy text");
        textButton.addButtonEvent(new ButtonEvent() {
            @Override
            public void buttonClicked(Widget parent) {
                currentData.setBoolean("hidetext", !textButton.isPressed());
                moduleGuiChanged.updateData();
            }
        });
        optionPanel.addChild(textButton);

        final ToggleButton diffButton = new ToggleButton(mc, gui).setText("Diff").setTooltips("Toggle display of RF/tick", "difference instead of RF");
        diffButton.addButtonEvent(new ButtonEvent() {
            @Override
            public void buttonClicked(Widget parent) {
                currentData.setBoolean("showdiff", !diffButton.isPressed());
                moduleGuiChanged.updateData();
            }
        });
        optionPanel.addChild(diffButton);

        barButton.setPressed(!currentData.getBoolean("hidebar"));
        textButton.setPressed(!currentData.getBoolean("hidetext"));
        diffButton.setPressed(!currentData.getBoolean("showdiff"));

        panel.addChild(optionPanel);
    }

    private void addMonitorPanel(Minecraft mc, Gui gui, final NBTTagCompound currentData, Panel panel) {
        Panel monitorPanel = new Panel(mc, gui).setLayout(new HorizontalLayout()).
                setDesiredHeight(16);
        String monitoring;
        if (currentData.hasKey("monitorx")) {
            int dim = currentData.getInteger("dim");
            World world = mc.thePlayer.worldObj;
            if (dim == world.provider.dimensionId) {
                int x = currentData.getInteger("monitorx");
                int y = currentData.getInteger("monitory");
                int z = currentData.getInteger("monitorz");
                monitoring = currentData.getString("monitorname");
                Block block = world.getBlock(x, y, z);
                monitorPanel.addChild(new BlockRender(mc, gui).setRenderItem(block)).setDesiredWidth(20);
                monitorPanel.addChild(new Label(mc, gui).setText(x + "," + y + "," + z).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT).setDesiredWidth(150));
            } else {
                monitoring = "<unreachable>";
            }
        } else {
            monitoring = "<not set>";
        }
        panel.addChild(monitorPanel);
        panel.addChild(new Label(mc, gui).setText(monitoring));
    }

    private void addColorPanel(Minecraft mc, Gui gui, final NBTTagCompound currentData, final ModuleGuiChanged moduleGuiChanged, Panel panel) {
        ColorChoiceLabel labelColorSelector = addColorSelector(mc, gui, currentData, moduleGuiChanged, "color").setTooltips("Color for the label");
        ColorChoiceLabel rfColorSelector = addColorSelector(mc, gui, currentData, moduleGuiChanged, "rfcolor").setTooltips("Color for the RF text");
        ColorChoiceLabel rfNegColorSelector = addColorSelector(mc, gui, currentData, moduleGuiChanged, "rfcolor_neg").setTooltips("Color for the negative", "RF/tick ratio");
        Panel colorPanel = new Panel(mc, gui).setLayout(new HorizontalLayout()).
                addChild(new Label(mc, gui).setText("L:")).addChild(labelColorSelector).
                addChild(new Label(mc, gui).setText("+:")).addChild(rfColorSelector).
                addChild(new Label(mc, gui).setText("-:")).addChild(rfNegColorSelector).
                setDesiredHeight(12);
        panel.addChild(colorPanel);
    }

    private ColorChoiceLabel addColorSelector(Minecraft mc, Gui gui, final NBTTagCompound currentData, final ModuleGuiChanged moduleGuiChanged, final String tagName) {
        ColorChoiceLabel colorChoiceLabel = new ColorChoiceLabel(mc, gui).addColors(0xffffff, 0xff0000, 0x00ff00, 0x0000ff, 0xffff00, 0xff00ff, 0x00ffff).setDesiredWidth(26).setDesiredHeight(14).addChoiceEvent(new ColorChoiceEvent() {
            @Override
            public void choiceChanged(Widget parent, Integer newColor) {
                currentData.setInteger(tagName, newColor);
                moduleGuiChanged.updateData();
            }
        });
        if (currentData != null) {
            int currentColor = currentData.getInteger(tagName);
            if (currentColor != 0) {
                colorChoiceLabel.setCurrentColor(currentColor);
            }
        }
        return colorChoiceLabel;
    }

    @Override
    public void setupFromNBT(NBTTagCompound tagCompound, int dim, int x, int y, int z) {
        if (tagCompound != null) {
            line = tagCompound.getString("text");
            if (tagCompound.hasKey("color")) {
                color = tagCompound.getInteger("color");
            } else {
                color = 0xffffff;
            }
            if (tagCompound.hasKey("rfcolor")) {
                rfcolor = tagCompound.getInteger("rfcolor");
            } else {
                rfcolor = 0xffffff;
            }
            if (tagCompound.hasKey("rfcolor_neg")) {
                rfcolor_neg = tagCompound.getInteger("rfcolor_neg");
            } else {
                rfcolor_neg = 0xffffff;
            }

            hidebar = tagCompound.getBoolean("hidebar");
            hidetext = tagCompound.getBoolean("hidetext");
            showdiff = tagCompound.getBoolean("showdiff");

            coordinate = Coordinate.INVALID;
            if (tagCompound.hasKey("monitorx")) {
                this.dim = tagCompound.getInteger("dim");
                if (dim == this.dim) {
                    Coordinate c = new Coordinate(tagCompound.getInteger("monitorx"), tagCompound.getInteger("monitory"), tagCompound.getInteger("monitorz"));
                    int dx = Math.abs(c.getX() - x);
                    int dy = Math.abs(c.getY() - y);
                    int dz = Math.abs(c.getZ() - z);
                    if (dx <= 16 && dy <= 16 && dz <= 16) {
                        coordinate = c;
                    }
                }
            }
        }
    }

    @Override
    public boolean needsServerData() {
        return true;
    }
}

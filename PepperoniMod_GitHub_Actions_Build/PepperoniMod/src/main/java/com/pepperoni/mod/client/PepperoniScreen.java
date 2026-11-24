package com.pepperoni.mod.client;

import com.pepperoni.mod.entity.PepperoniEntity;
import com.pepperoni.mod.network.PepperoniNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class PepperoniScreen extends Screen {
    private final int entityId;
    private final int bond;
    private final String mode;

    public PepperoniScreen(int entityId, int bond, String mode) {
        super(Component.literal("Pepperoni ♥"));
        this.entityId = entityId;
        this.bond = bond;
        this.mode = mode;
    }

    @Override
    protected void init() {
        int left = this.width / 2 - 105;
        int top = this.height / 2 - 92;
        addRenderableWidget(Button.builder(Component.literal("Talk ♥"), b -> send("talk"))
                .bounds(left, top + 32, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Give gift"), b -> send("gift"))
                .bounds(left + 110, top + 32, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Follow me"), b -> send("follow"))
                .bounds(left, top + 58, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Go mining"), b -> send("mine"))
                .bounds(left + 110, top + 58, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Fight mobs"), b -> send("fight"))
                .bounds(left, top + 84, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Sleep together"), b -> send("sleep"))
                .bounds(left + 110, top + 84, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Close"), b -> onClose())
                .bounds(left + 55, top + 118, 100, 20).build());
    }

    private void send(String action) {
        PepperoniNetwork.sendToServer(entityId, action);
        if (action.equals("talk") || action.equals("gift") || action.equals("sleep")) onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        int left = this.width / 2 - 112;
        int top = this.height / 2 - 102;
        graphics.fill(left, top, left + 224, top + 158, 0xEE4A1630);
        graphics.fill(left + 2, top + 2, left + 222, top + 156, 0xEEFFDCEB);
        graphics.drawCenteredString(font, "Pepperoni ♥", width / 2, top + 9, 0xA3134B);
        graphics.drawCenteredString(font, "Bond: " + bond + " / 100", width / 2, top + 22, 0x6E2446);
        graphics.drawCenteredString(font, "Current mode: " + mode, width / 2, top + 108, 0x6E2446);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override public boolean isPauseScreen() { return false; }
}

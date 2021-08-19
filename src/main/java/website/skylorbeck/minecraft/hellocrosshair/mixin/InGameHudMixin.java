package website.skylorbeck.minecraft.hellocrosshair.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RaycastContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.logging.Level;
import java.util.logging.Logger;

import static net.minecraft.client.gui.DrawableHelper.GUI_ICONS_TEXTURE;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    int crosshairState = 0;
    int crosshairStatePrevious = 0;
    @Shadow @Final private MinecraftClient client;

    @Inject(at = @At(value = "INVOKE",target = "Lnet/minecraft/client/gui/hud/InGameHud;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V"), method = "renderCrosshair",
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;blendFuncSeparate(Lcom/mojang/blaze3d/platform/GlStateManager$SrcFactor;Lcom/mojang/blaze3d/platform/GlStateManager$DstFactor;Lcom/mojang/blaze3d/platform/GlStateManager$SrcFactor;Lcom/mojang/blaze3d/platform/GlStateManager$DstFactor;)V"),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getAttackCooldownProgress(F)F")
            ))
    public void injectedRender(MatrixStack matrices, CallbackInfo ci){
        assert client.world != null;
        HitResult hit = client.crosshairTarget;
        switch (hit.getType()){
            case MISS -> {
            }
            case BLOCK -> {
                Block block = client.world.getBlockState(((BlockHitResult) hit).getBlockPos()).getBlock();
                if (block.getDefaultState().hasBlockEntity()){
                    increaseState();
                } else {
                    decreaseState();
                }
            }
            case ENTITY -> {
                increaseState();
            }
        }
        if (crosshairState>0)
        renderInjected(matrices);
    }

    public void renderInjected(MatrixStack matrices){
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, new Identifier("textures/gui/hello.png"));
        InGameHud inGameHud = ((InGameHud)(Object)this);
        inGameHud.drawTexture(matrices, (((InGameHudAccessor)inGameHud).getScaledWidth()-9)/2,(((InGameHudAccessor)inGameHud).getScaledHeight()-9)/2 , (crosshairState-1)*9, 0, 9, 9);
        RenderSystem.setShaderTexture(0, GUI_ICONS_TEXTURE);
    }
    private void increaseState(){
        if (crosshairState<4) {
            if (crosshairState == crosshairStatePrevious){
                crosshairState++;
            } else {
                crosshairStatePrevious = crosshairState;
            }
        }
    }
    private void decreaseState(){
        if (crosshairState>0) {
            if (crosshairState == crosshairStatePrevious) {
                crosshairState--;
            } else {
                crosshairStatePrevious = crosshairState;
            }
        }
    }
}

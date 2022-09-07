package com.dan.signsearcher.mixin.minecraft;

import com.dan.signsearcher.ext.BlockEntityExt;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Accessor
    public abstract BufferBuilderStorage getBufferBuilders();

    @ModifyVariable(
            //public void render(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f)
            method = "render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/util/math/Matrix4f;)V",

            //VertexConsumerProvider.Immediate immediate = this.bufferBuilders.getEntityVertexConsumers();
            at = @At(
                    value = "INVOKE",
                    target = "net/minecraft/client/render/BufferBuilderStorage.getEntityVertexConsumers()Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;"
            ),
            ordinal = 4
    )
    public boolean forceOutline(boolean orig) {
        return true;
    }

    @Redirect(
            //public void render(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f)
            method = "render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/util/math/Matrix4f;)V",

            //BlockEntityRenderDispatcher.INSTANCE.render(blockEntity, tickDelta, matrices, vertexConsumerProvider3);
            at = @At(
                    value = "INVOKE",
                    target = "net/minecraft/client/render/block/entity/BlockEntityRenderDispatcher.render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V",
                    ordinal = 0
            )
    )
    public void renderBlockEntity(BlockEntityRenderDispatcher dispatcher, BlockEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vcp, MatrixStack _matrices, float _tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f positionMatrix) {
        if (((BlockEntityExt) blockEntity).isGlowing()) {
            OutlineVertexConsumerProvider outlineVcp = this.getBufferBuilders().getOutlineVertexConsumers();
            int color = ((BlockEntityExt) blockEntity).getGlowColor();
            outlineVcp.setColor(
                    (color >> 16) & 0xff,
                    (color >> 8) & 0xff,
                    color & 0xff,
                    0xff
            );
            vcp = outlineVcp;
        }
        dispatcher.render(blockEntity, tickDelta, matrices, vcp);
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void a(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f positionMatrix, CallbackInfo ci) {
        renderBlockOutline = true;
    }

    @Redirect(
            //public void render(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f)
            method = "render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/util/math/Matrix4f;)V",

            //BlockEntityRenderDispatcher.INSTANCE.render(blockEntity, tickDelta, matrices, vertexConsumerProvider3);
            at = @At(
                    value = "INVOKE",
                    target = "net/minecraft/client/render/block/entity/BlockEntityRenderDispatcher.render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V",
                    ordinal = 1
            )
    )
    public void renderWithOutline2(BlockEntityRenderDispatcher dispatcher, BlockEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vcp) {
        if (((BlockEntityExt) blockEntity).isGlowing()) {
            OutlineVertexConsumerProvider outlineVcp = this.getBufferBuilders().getOutlineVertexConsumers();
            int color = ((BlockEntityExt) blockEntity).getGlowColor();
            outlineVcp.setColor(
                    (color >> 16) & 0xff,
                    (color >> 8) & 0xff,
                    color & 0xff,
                    0xff
            );
            outlineVcp.draw();
        }
        dispatcher.render(blockEntity, tickDelta, matrices, vcp);
    }
}

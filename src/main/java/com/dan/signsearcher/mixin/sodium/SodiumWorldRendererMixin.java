package com.dan.signsearcher.mixin.sodium;

import com.dan.signsearcher.ext.BlockEntityExt;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.SortedSet;

@Mixin(SodiumWorldRenderer.class)
public class SodiumWorldRendererMixin {

    @Redirect(
            //public void renderTileEntities(MatrixStack matrices, BufferBuilderStorage bufferBuilders, Long2ObjectMap<SortedSet<BlockBreakingInfo>> blockBreakingProgressions,
            //                             Camera camera, float tickDelta) {
            method = "renderTileEntities",

            //BlockEntityRenderDispatcher.INSTANCE.render(blockEntity, tickDelta, matrices, consumer);
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/block/entity/BlockEntityRenderDispatcher;render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V",
                    ordinal = 0
            )
    )
    public void renderWithOutline(BlockEntityRenderDispatcher dispatcher, BlockEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vcp, MatrixStack _matrices, BufferBuilderStorage bufferBuilders, Long2ObjectMap<SortedSet<BlockBreakingInfo>> _blockBreakingProgressions, Camera _camera, float _tickDelta) {
        if (((BlockEntityExt) blockEntity).isGlowing()) {
                OutlineVertexConsumerProvider outlineVcp = bufferBuilders.getOutlineVertexConsumers();
                int color = ((BlockEntityExt) blockEntity).getGlowColor();
                outlineVcp.setColor(
            255 ,0, 0, 0);
            // some color input
                vcp = outlineVcp;
            }
            dispatcher.render(blockEntity, tickDelta, matrices, vcp);

    }
}

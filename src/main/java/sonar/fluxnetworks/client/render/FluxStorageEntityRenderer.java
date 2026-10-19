package sonar.fluxnetworks.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import sonar.fluxnetworks.FluxNetworks;
import sonar.fluxnetworks.common.device.TileFluxStorage;

import javax.annotation.Nonnull;

/**
 * Renders the coloured energy level inside Flux Storage blocks.
 */
public class FluxStorageEntityRenderer implements BlockEntityRenderer<TileFluxStorage, FluxStorageEntityRenderer.State> {

    private static final Identifier ENERGY_TEXTURE = FluxNetworks.location("textures/block/flux_storage_energy.png");

    public static final BlockEntityRendererProvider<TileFluxStorage, State> PROVIDER = ctx -> new FluxStorageEntityRenderer();

    private static final float START = 2.0f / 16.0f;
    private static final float END = 14.0f / 16.0f;
    private static final float OFFSET = 1.0f / 16.0f;
    private static final float WIDTH = 12.0f / 16.0f;
    private static final float HEIGHT = 13.0f / 16.0f;

    private static final int ALPHA = 150;

    public static class State extends BlockEntityRenderState {
        int color;
        long energy;
        long capacity;
    }

    private FluxStorageEntityRenderer() {
    }

    @Nonnull
    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(@Nonnull TileFluxStorage entity, @Nonnull State state, float partialTicks,
                                   @Nonnull Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);
        state.color = entity.mClientColor;
        state.energy = entity.getTransferBuffer();
        state.capacity = entity.getMaxTransferLimit();
    }

    @Override
    public void submit(@Nonnull State state, @Nonnull PoseStack poseStack, @Nonnull SubmitNodeCollector collector,
                       @Nonnull CameraRenderState camera) {
        submit(poseStack, collector, state.color, state.energy, state.capacity);
    }

    public static void submit(@Nonnull PoseStack poseStack, @Nonnull SubmitNodeCollector collector,
                              int color, long energy, long capacity) {
        if (energy <= 0 || capacity <= 0) {
            return;
        }
        final float renderHeight = Math.min(HEIGHT * energy / capacity, HEIGHT);
        final int r = color >> 16 & 0xFF;
        final int g = color >> 8 & 0xFF;
        final int b = color & 0xFF;
        final int overlay = OverlayTexture.NO_OVERLAY;
        RenderType type = RenderTypes.entityTranslucent(ENERGY_TEXTURE);

        submitSide(poseStack, collector, type, Direction.NORTH, START, OFFSET, END, WIDTH, renderHeight, r, g, b, overlay, -1);
        submitSide(poseStack, collector, type, Direction.SOUTH, START, OFFSET, END, WIDTH, renderHeight, r, g, b, overlay, -1);
        submitSide(poseStack, collector, type, Direction.EAST, START, OFFSET, END, WIDTH, renderHeight, r, g, b, overlay, -1);
        submitSide(poseStack, collector, type, Direction.WEST, START, OFFSET, END, WIDTH, renderHeight, r, g, b, overlay, -1);
        if (renderHeight < HEIGHT) {
            submitSide(poseStack, collector, type, Direction.UP, OFFSET, START + renderHeight, OFFSET,
                    END, -END, r, g, b, overlay, 1);
        }
    }

    private static void submitSide(@Nonnull PoseStack stack, @Nonnull SubmitNodeCollector collector, RenderType type,
                                   @Nonnull Direction dir, float x, float y, float z, float width, float height,
                                   int r, int g, int b, int overlay, int normalY) {
        stack.pushPose();
        stack.translate(0.5, 0.5, 0.5);
        stack.mulPose(dir.getRotation());
        stack.translate(-0.5, -0.5, -0.5);
        collector.submitCustomGeometry(stack, type, (pose, consumer) -> {
            vertex(consumer, pose, x, y, z - height, r, g, b, overlay, normalY);
            vertex(consumer, pose, x + width, y, z - height, r, g, b, overlay, normalY);
            vertex(consumer, pose, x + width, y, z, r, g, b, overlay, normalY);
            vertex(consumer, pose, x, y, z, r, g, b, overlay, normalY);
        });
        stack.popPose();
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z,
                               int r, int g, int b, int overlay, int normalY) {
        consumer.addVertex(pose, x, y, z).setColor(r, g, b, ALPHA)
                .setUv(x, z).setOverlay(overlay).setLight(LightCoordsUtil.FULL_BRIGHT)
                .setNormal(pose, 0, normalY, 0);
    }
}

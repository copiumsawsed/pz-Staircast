package pzmod.staircast;

import me.zed_0xff.zombie_buddy.Patch;
import net.bytebuddy.asm.Advice;
import zombie.iso.IsoCamera;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoObject;
import zombie.iso.Vector3;
import zombie.iso.fboRenderChunk.FBORenderCell;

public class Patch_FBORenderCell {
    @Patch(className = "zombie.iso.fboRenderChunk.FBORenderCell", methodName = "renderInternal")
    public static class Patch_renderInternal {
        @Patch.OnEnter
        public static void enter(@Patch.This FBORenderCell self) {
            try {
                Mod.instance.trace("FBORenderCell::renderInternal");

                var fs = IsoCamera.frameState;
                int playerIndex = fs.playerIndex;
                if (FakeFrameState.isRendering(playerIndex)) {
                    var ffs = FakeFrameState.get(playerIndex);
                    ffs.isFakeSquareExterior = FakeFrameState.apply(ffs.fakeSquare, ffs.floorSquare);
                    FakeFrameState.apply(fs, ffs.fakePos, ffs.fakeSquare);
                    FakeFrameState.apply(Game.getCamChar(fs), ffs.fakePos, ffs.fakeSquare);
                }
            } catch (Throwable t) {
                FakeFrameState.recoverFromError(t);
            }
        }
    }

    @Patch(className = "zombie.iso.fboRenderChunk.FBORenderCell", methodName = "renderTilesInternal")
    public static class Patch_renderTilesInternal {
        @Patch.OnExit
        public static void exit() {
            try {
                Mod.instance.trace("FBORenderCell::renderTilesInternal exit");

                int playerIndex = IsoCamera.frameState.playerIndex;
                var fs = IsoCamera.frameState;
                if (FakeFrameState.isRendering(playerIndex)) {
                    var ffs = FakeFrameState.get(playerIndex);
                    if (ffs.isFakeSquareExterior != null) {
                        FakeFrameState.reset(ffs.fakeSquare, ffs.isFakeSquareExterior);
                        ffs.isFakeSquareExterior = null;
                    }
                    FakeFrameState.apply(fs, ffs.realPos, ffs.realSquare);
                    FakeFrameState.apply(Game.getCamChar(fs), ffs.realPos, ffs.realSquare);
                }
            } catch (Throwable t) {
                FakeFrameState.recoverFromError(t);
            }
        }
    }

    @Patch(className = "zombie.iso.fboRenderChunk.FBORenderCell", methodName = "renderPlayers")
    public static class Patch_renderPlayers {
        @Patch.OnEnter
        public static void enter(
                @Patch.Local("backupPos") Vector3 backupPos,
                @Patch.Local("backupSquare") IsoGridSquare backupSquare,
                @Patch.Argument(0) int playerIndex)
        {
            try {
                Mod.instance.trace("FBORenderCell::renderPlayers");

                if (!Mod.instance.debugOptions.renderPlayer.getValue()) {
                    return;
                }

                var fs = IsoCamera.frameState;
                var camChar = Game.getCamChar(fs);
                if (FakeFrameState.isRendering(playerIndex)) {
                    var ffs = FakeFrameState.get(playerIndex);
                    backupPos = camChar.getPosition(new Vector3());
                    backupSquare = camChar.getCurrentSquare();
                    FakeFrameState.apply(camChar, ffs.realPos, ffs.realSquare);
                }
            } catch (Throwable t) {
                FakeFrameState.recoverFromError(t);
            }
        }

        @Patch.OnExit
        public static void exit(@Patch.Local("backupPos") Vector3 backupPos, @Patch.Local("backupSquare") IsoGridSquare backupSquare) {
            if (backupPos != null) {
                try {
                    var camChar = IsoCamera.frameState.camCharacter;
                    FakeFrameState.apply(camChar, backupPos, backupSquare);
                } catch (Throwable t) {
                    FakeFrameState.recoverFromError(t);
                }
            }
        }
    }

    @Patch(className = "zombie.iso.fboRenderChunk.FBORenderCell", methodName = "isPotentiallyObscuringObject")
    public static class Patch_isPotentiallyObscuringObject {
        @Patch.OnEnter(skipOn = true)
        public static boolean enter(@Patch.Argument(0) IsoObject object) {
            try {
                Mod.instance.trace("FBORenderCell::isPotentiallyObscuringObject");

                var playerIndex = IsoCamera.frameState.playerIndex;
                return object != null
                       && object.getSprite() != null
                       && FakeFrameState.isRendering(playerIndex)
                       && FakeFrameState.get(playerIndex).fakeSquare.z == object.square.z;
            } catch (Throwable t) {
                FakeFrameState.recoverFromError(t);
                return false;
            }
        }

        @Patch.OnExit
        public static void exit(@Advice.Enter boolean skipped, @Patch.Return(readOnly = false) boolean ret) {
            if (skipped) {
                ret = true;
            }
        }
    }
}

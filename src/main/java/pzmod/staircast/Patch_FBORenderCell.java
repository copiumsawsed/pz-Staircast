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
            var fs = IsoCamera.frameState;
            int playerIndex = fs.playerIndex;
            if (FakeFrameState.isRendering(playerIndex)) {
                var ffs = FakeFrameState.get(playerIndex);
                FakeFrameState.apply(fs, ffs.fakePos, ffs.fakeSquare);
                FakeFrameState.apply(fs.camCharacter, ffs.fakePos, ffs.fakeSquare);
            }
        }
    }

    @Patch(className = "zombie.iso.fboRenderChunk.FBORenderCell", methodName = "renderTilesInternal")
    public static class Patch_renderTilesInternal {
        @Patch.OnExit
        public static void exit() {
            int playerIndex = IsoCamera.frameState.playerIndex;
            var fs = IsoCamera.frameState;
            if (FakeFrameState.isRendering(playerIndex)) {
                var ffs = FakeFrameState.get(playerIndex);
                FakeFrameState.apply(fs, ffs.realPos, ffs.realSquare);
                FakeFrameState.apply(fs.camCharacter, ffs.realPos, ffs.realSquare);
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
            var fs = IsoCamera.frameState;
            var camChar = fs.camCharacter;
            if (FakeFrameState.isRendering(playerIndex)) {
                var ffs = FakeFrameState.get(playerIndex);
                backupPos = camChar.getPosition(new Vector3());
                backupSquare = camChar.getCurrentSquare();
                FakeFrameState.apply(camChar, ffs.realPos, ffs.realSquare);
            }
        }

        @Patch.OnExit
        public static void exit(@Patch.Local("backupPos") Vector3 backupPos, @Patch.Local("backupSquare") IsoGridSquare backupSquare) {
            if (backupPos != null) {
                var camChar = IsoCamera.frameState.camCharacter;
                FakeFrameState.apply(camChar, backupPos, backupSquare);
            }
        }
    }

    @Patch(className = "zombie.iso.fboRenderChunk.FBORenderCell", methodName = "isPotentiallyObscuringObject")
    public static class Patch_isPotentiallyObscuringObject {
        @Patch.OnEnter(skipOn = true)
        public static boolean enter(@Patch.Argument(0) IsoObject object) {
            var playerIndex = IsoCamera.frameState.playerIndex;
            return object != null
                   && FakeFrameState.isRendering(playerIndex)
                   && FakeFrameState.get(playerIndex).fakeSquare.z == object.square.z;
        }

        @Patch.OnExit
        public static void exit(@Advice.Enter boolean skipped, @Patch.Return(readOnly = false) boolean ret) {
            if (skipped) {
                ret = true;
            }
        }
    }
}

package pzmod.staircast;

import me.zed_0xff.zombie_buddy.Patch;
import zombie.iso.IsoCamera;
import zombie.iso.IsoGridSquare;
import zombie.iso.Vector3;
import zombie.iso.fboRenderChunk.FBORenderTrees;

public class Patch_FBORenderTrees {
    @Patch(className = "zombie.iso.fboRenderChunk.FBORenderTrees", methodName = "init", warmUp = true)
    public static class Patch_init {
        @Patch.OnEnter
        public static void enter(
                @Patch.This FBORenderTrees self, // FIXME retransformation is not applied if class is not loaded
                @Patch.Local("backupPos") Vector3 backupPos,
                @Patch.Local("backupSquare") IsoGridSquare backupSquare)
        {
            Mod.instance.trace("FBORenderTrees::init");

            var fs = IsoCamera.frameState;
            if (FakeFrameState.isRendering(fs.playerIndex)) {
                var ffs = FakeFrameState.get(fs.playerIndex);
                backupPos = Game.getCamCharPos(fs);
                backupSquare = Game.getCamCharSquare(fs);
                FakeFrameState.apply(fs, ffs.realPos, ffs.realSquare);
            }
        }

        @Patch.OnExit
        public static void exit(@Patch.Local("backupPos") Vector3 backupPos, @Patch.Local("backupSquare") IsoGridSquare backupSquare) {
            if (backupPos != null) {
                FakeFrameState.apply(IsoCamera.frameState, backupPos, backupSquare);
            }
        }
    }
}

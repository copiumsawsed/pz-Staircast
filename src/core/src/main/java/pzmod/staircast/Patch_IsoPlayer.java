package pzmod.staircast;

import me.zed_0xff.zombie_buddy.Patch;
import zombie.characters.IsoPlayer;
import zombie.iso.IsoCamera;
import zombie.iso.IsoGridSquare;
import zombie.iso.Vector3;

public class Patch_IsoPlayer {
    @Patch(className = "zombie.characters.IsoPlayer", methodName = "render")
    public static class Patch_render {
        @Patch.OnEnter
        public static void enter(
                @Patch.This IsoPlayer self,
                @Patch.Local("backupPos") Vector3 backupPos,
                @Patch.Local("backupSquare") IsoGridSquare backupSquare,
                @Patch.Argument(value = 0, readOnly = false) float x,
                @Patch.Argument(value = 1, readOnly = false) float y,
                @Patch.Argument(value = 2, readOnly = false) float z)
        {
            try {
                Mod.instance.trace("IsoPlayer::render");

                if (Game.isFboChunkRenderEnabled() || !Mod.instance.debugOptions.renderPlayer.getValue()) {
                    return;
                }

                var fs = IsoCamera.frameState;
                var camChar = Game.getCamChar(fs);
                if (FakeFrameState.isRendering(fs.playerIndex)) {
                    var ffs = FakeFrameState.get(fs.playerIndex);
                    if (self == camChar) {
                        backupPos = camChar.getPosition(new Vector3());
                        backupSquare = camChar.getCurrentSquare();
                        FakeFrameState.apply(camChar, ffs.realPos, ffs.realSquare);
                        x = ffs.realPos.x;
                        y = ffs.realPos.y;
                        z = ffs.realPos.z;
                    }
                }
            } catch (Throwable t) {
                FakeFrameState.recoverFromError(t);
            }
        }

        @Patch.OnExit
        public static void exit(@Patch.Local("backupPos") Vector3 backupPos, @Patch.Local("backupSquare") IsoGridSquare backupSquare) {
            if (backupPos != null) {
                try {
                    FakeFrameState.apply(Game.getCamChar(IsoCamera.frameState), backupPos, backupSquare);
                } catch (Throwable t) {
                    FakeFrameState.recoverFromError(t);
                }
            }
        }
    }
}
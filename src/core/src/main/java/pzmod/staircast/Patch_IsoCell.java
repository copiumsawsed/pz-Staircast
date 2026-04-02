package pzmod.staircast;

import me.zed_0xff.zombie_buddy.Patch;
import zombie.iso.IsoCamera;
import zombie.iso.IsoCell;

public class Patch_IsoCell {
    @Patch(className = "zombie.iso.IsoCell", methodName = "renderInternal")
    public static class Patch_renderInternal {
        @Patch.OnEnter
        public static void enter(@Patch.This IsoCell self) {
            Mod.instance.trace("IsoCell::renderInternal");

            var fs = IsoCamera.frameState;
            int playerIndex = fs.playerIndex;
            if (FakeFrameState.isRendering(playerIndex)) {
                var ffs = FakeFrameState.get(playerIndex);
                ffs.isFakeSquareExterior = FakeFrameState.apply(ffs.fakeSquare, ffs.floorSquare);
                FakeFrameState.apply(fs, ffs.fakePos, ffs.fakeSquare);
                FakeFrameState.apply(Game.getCamChar(fs), ffs.fakePos, ffs.fakeSquare);
            }
        }
    }

    @Patch(className = "zombie.iso.IsoCell", methodName = "renderTilesInternal")
    public static class Patch_renderTilesInternal {
        @Patch.OnExit
        public static void exit() {
            Mod.instance.trace("IsoCell::renderTilesInternal exit");

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
        }
    }
}

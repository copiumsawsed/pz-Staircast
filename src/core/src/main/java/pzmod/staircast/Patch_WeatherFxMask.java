package pzmod.staircast;

import me.zed_0xff.zombie_buddy.Patch;
import zombie.characters.IsoPlayer;
import zombie.iso.IsoCamera;
import zombie.iso.IsoGridSquare;
import zombie.iso.Vector3;

public class Patch_WeatherFxMask {
    @Patch(className = "zombie.iso.weather.fx.WeatherFxMask", methodName = "initMask", warmUp = true)
    public static class Patch_initMask {
        @Patch.OnEnter
        public static void enter(
                @Patch.Local("backupPos") Vector3 backupPos,
                @Patch.Local("backupSquare") IsoGridSquare backupSquare,
                @Patch.Local("fakeSquareExterior") Boolean fakeSquareExterior)
        {
            try {
                Mod.instance.trace("WeatherFxMask::initMask");

                var fs = IsoCamera.frameState;
                int playerIndex = fs.playerIndex;
                var ffs = FakeFrameState.get(playerIndex);
                if (FakeFrameState.isRendering(playerIndex)) {
                    var camChar = Game.getCamChar(fs);
                    backupPos = camChar.getPosition(new Vector3());
                    backupSquare = camChar.getCurrentSquare();
                    fakeSquareExterior = FakeFrameState.apply(ffs.fakeSquare, ffs.floorSquare);
                    FakeFrameState.apply(camChar, ffs.fakePos, ffs.fakeSquare);
                }
            } catch (Throwable t) {
                FakeFrameState.recoverFromError(t);
            }
        }

        @Patch.OnExit
        public static void exit(
                @Patch.Local("backupPos") Vector3 backupPos,
                @Patch.Local("backupSquare") IsoGridSquare backupSquare,
                @Patch.Local("fakeSquareExterior") Boolean fakeSquareExterior)
        {
            if (backupPos != null) {
                try {
                    FakeFrameState.apply(Game.getCamChar(IsoCamera.frameState), backupPos, backupSquare);
                    if (fakeSquareExterior != null) {
                        var ffs = FakeFrameState.get(IsoCamera.frameState.playerIndex);
                        FakeFrameState.reset(ffs.fakeSquare, fakeSquareExterior);
                    }
                } catch (Throwable t) {
                    FakeFrameState.recoverFromError(t);
                }
            }
        }
    }

    @Patch(className = "zombie.iso.weather.fx.WeatherFxMask", methodName = "renderFxMask", warmUp = true)
    public static class Patch_renderFxMask {
        @Patch.OnEnter
        public static void enter(
                @Patch.Local("backupPos") Vector3 backupPos,
                @Patch.Local("backupSquare") IsoGridSquare backupSquare,
                @Patch.Local("fakeSquareExterior") Boolean fakeSquareExterior,
                @Patch.Argument(0) int playerIndex)
        {
            try {
                Mod.instance.trace("WeatherFxMask::renderFxMask");

                var ffs = FakeFrameState.get(playerIndex);
                if (FakeFrameState.isRendering(playerIndex)) {
                    var player = IsoPlayer.players[playerIndex];
                    backupPos = player.getPosition(new Vector3());
                    backupSquare = player.getCurrentSquare();
                    fakeSquareExterior = FakeFrameState.apply(ffs.fakeSquare, ffs.floorSquare);
                    FakeFrameState.apply(ffs.fakeSquare, ffs.realSquare);
                    FakeFrameState.apply(player, ffs.fakePos, ffs.fakeSquare);
                    if (Mod.instance.debugOptions.testGracefulFailure.getValue()) {
                        throw new RuntimeException("StaircastGracefulFailure");
                    }
                }
            } catch (Throwable t) {
                FakeFrameState.recoverFromError(t);
            }
        }

        @Patch.OnExit
        public static void exit(
                @Patch.Local("backupPos") Vector3 backupPos,
                @Patch.Local("backupSquare") IsoGridSquare backupSquare,
                @Patch.Local("fakeSquareExterior") Boolean fakeSquareExterior,
                @Patch.Argument(0) int playerIndex)
        {
            if (backupPos != null) {
                try {
                    if (fakeSquareExterior != null) {
                        var ffs = FakeFrameState.get(playerIndex);
                        FakeFrameState.reset(ffs.fakeSquare, fakeSquareExterior);
                    }
                    FakeFrameState.apply(IsoPlayer.players[playerIndex], backupPos, backupSquare);
                } catch (Throwable t) {
                    FakeFrameState.recoverFromError(t);
                }
            }
        }
    }
}

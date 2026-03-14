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
        public static void enter(@Patch.Local("backupPos") Vector3 backupPos, @Patch.Local("backupSquare") IsoGridSquare backupSquare) {
            var fs = IsoCamera.frameState;
            int playerIndex = fs.playerIndex;
            var ffs = FakeFrameState.get(playerIndex);
            if (FakeFrameState.isRendering(playerIndex)) {
                backupPos = fs.camCharacter.getPosition(new Vector3());
                backupSquare = fs.camCharacter.getCurrentSquare();
                FakeFrameState.apply(fs.camCharacter, ffs.fakePos, ffs.fakeSquare);
            }
        }

        @Patch.OnExit
        public static void exit(@Patch.Local("backupPos") Vector3 backupPos, @Patch.Local("backupSquare") IsoGridSquare backupSquare) {
            if (backupPos != null) {
                FakeFrameState.apply(IsoCamera.frameState.camCharacter, backupPos, backupSquare);
            }
        }
    }

    @Patch(className = "zombie.iso.weather.fx.WeatherFxMask", methodName = "renderFxMask", warmUp = true)
    public static class Patch_renderFxMask {
        @Patch.OnEnter
        public static void enter(
                @Patch.Local("backupPos") Vector3 backupPos,
                @Patch.Local("backupSquare") IsoGridSquare backupSquare,
                @Patch.Argument(0) int playerIndex)
        {
            var ffs = FakeFrameState.get(playerIndex);
            if (FakeFrameState.isRendering(playerIndex)) {
                var player = IsoPlayer.players[playerIndex];
                backupPos = player.getPosition(new Vector3());
                backupSquare = player.getCurrentSquare();
                FakeFrameState.apply(player, ffs.fakePos, ffs.fakeSquare);
            }
        }

        @Patch.OnExit
        public static void exit(
                @Patch.Local("backupPos") Vector3 backupPos,
                @Patch.Local("backupSquare") IsoGridSquare backupSquare,
                @Patch.Argument(0) int nPlayer)
        {
            if (backupPos != null) {
                FakeFrameState.apply(IsoPlayer.players[nPlayer], backupPos, backupSquare);
            }
        }
    }
}

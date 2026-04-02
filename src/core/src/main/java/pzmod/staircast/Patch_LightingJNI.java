package pzmod.staircast;

import me.zed_0xff.zombie_buddy.Patch;
import zombie.characters.IsoPlayer;
import zombie.iso.Vector3;

public class Patch_LightingJNI {
    @Patch(className = "zombie.iso.LightingJNI", methodName = "checkPlayerTorches")
    public static class Patch_checkPlayerTorches {
        @Patch.OnEnter
        public static void enter(
                @Patch.Local("backupPos") Vector3 backupPos,
                @Patch.Argument(0) IsoPlayer player,
                @Patch.Argument(1) int playerIndex)
        {
            try {
                Mod.instance.trace("LightingJNI::checkPlayerTorches");

                int playerIdx = playerIndex;
                if (Game.isClient()) {
                    if (player != IsoPlayer.getInstance()) {
                        return;
                    }
                    playerIdx = IsoPlayer.getPlayerIndex();
                }
                if (player != null && FakeFrameState.isRendering(playerIdx)) {
                    var ffs = FakeFrameState.get(playerIdx);
                    if (!ffs.renderLighting) {
                        return;
                    }
                    backupPos = player.getPosition(new Vector3());
                    FakeFrameState.apply(player, ffs.fakePos);
                }
            } catch (Throwable t) {
                FakeFrameState.recoverFromError(t);
            }
        }

        @Patch.OnExit
        public static void exit(@Patch.Local("backupPos") Vector3 backupPos, @Patch.Argument(0) IsoPlayer player) {
            if (backupPos != null) {
                try {
                    FakeFrameState.apply(player, backupPos);
                } catch (Throwable t) {
                    FakeFrameState.recoverFromError(t);
                }
            }
        }
    }

    @Patch(className = "zombie.iso.LightingJNI", methodName = "updatePlayer")
    public static class Patch_updatePlayer {
        @Patch.OnEnter
        public static void enter(@Patch.Local("backupPos") Vector3 backupPos, @Patch.Argument(0) int playerIndex) {
            try {
                Mod.instance.trace("LightingJNI::updatePlayer");

                var player = IsoPlayer.players[playerIndex];
                if (player != null && FakeFrameState.isRendering(playerIndex)) {
                    var ffs = FakeFrameState.get(playerIndex);
                    if (!ffs.renderLighting) {
                        return;
                    }
                    backupPos = player.getPosition(new Vector3());
                    FakeFrameState.apply(player, ffs.fakePos);
                }
            } catch (Throwable t) {
                FakeFrameState.recoverFromError(t);
            }
        }

        @Patch.OnExit
        public static void exit(@Patch.Local("backupPos") Vector3 backupPos, @Patch.Argument(0) int playerIndex) {
            if (backupPos != null) {
                try {
                    FakeFrameState.apply(IsoPlayer.players[playerIndex], backupPos);
                } catch (Throwable t) {
                    FakeFrameState.recoverFromError(t);
                }
            }
        }
    }
}

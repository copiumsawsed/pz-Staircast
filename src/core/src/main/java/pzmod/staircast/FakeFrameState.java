package pzmod.staircast;

import zombie.characters.IsoGameCharacter;
import zombie.debug.DebugLog;import zombie.debug.DebugType;import zombie.iso.IsoCamera;
import zombie.iso.IsoGridSquare;
import zombie.iso.SpriteDetails.IsoFlagType;
import zombie.iso.Vector3;

public class FakeFrameState {
    public static FakeFrameState[] frameState = new FakeFrameState[4];

    public Vector3 realPos = new Vector3();
    public Vector3 fakePos = new Vector3();
    public IsoGridSquare realSquare;
    public IsoGridSquare fakeSquare;
    public IsoGridSquare floorSquare;
    public Boolean isFakeSquareExterior;
    public int frameCounter;
    public float lastViewpointZ;
    public boolean renderLighting;

    public static boolean isRendering(int playerIndex) {
        if (playerIndex < 0 || playerIndex >= frameState.length) {
            return false;
        }
        return frameState[playerIndex] != null && frameState[playerIndex].frameCounter == IsoCamera.frameState.frameCount;
    }

    public static FakeFrameState get(int playerIndex) {
        return frameState[playerIndex];
    }

    public static void apply(IsoGameCharacter c, Vector3 pos) {
        c.setX(pos.x);
        c.setY(pos.y);
        c.setZ(pos.z);
    }

    public static void apply(IsoGameCharacter c, IsoGridSquare square) {
        c.setCurrent(square);
    }

    public static Boolean apply(IsoGridSquare dst, IsoGridSquare src) {
        if (dst.room == null && src.room != null) {
            dst.room = src.room;
            Game.assignRoomId(dst, src.getRoomID());
            var isExterior = Game.squareHas(dst, IsoFlagType.exterior);
            if (isExterior) {
                Game.setSquareProperty(dst, IsoFlagType.exterior, false);
            }
            return isExterior;
        }
        return null;
    }

    public static void apply(IsoGameCharacter c, Vector3 pos, IsoGridSquare square) {
        apply(c, pos);
        apply(c, square);
    }

    public static void apply(IsoCamera.FrameState fs, Vector3 pos, IsoGridSquare square) {
        Game.setCamCharPos(fs, pos.x, pos.y, pos.z);
        Game.setCamCharSquare(fs, square);
    }

    public static void reset(IsoGridSquare square, boolean exterior) {
        if (Mod.instance.debugOptions.assignRoomPermanently.getValue()) {
            return;
        }
        square.room = null;
        Game.assignRoomId(square, -1);
        if (exterior) {
            Game.setSquareProperty(square, IsoFlagType.exterior, true);
        }
    }

    public static void recoverFromError(Throwable t) {
        if (t != null) {
            t.printStackTrace();
        }
        try {
            var fs = IsoCamera.frameState;
            var playerIndex = fs.playerIndex;
            var camChar = Game.getCamChar(fs);
            if (isRendering(playerIndex)) {
                var ffs = get(playerIndex);
                if (camChar != null) {
                    apply(camChar, ffs.realPos, ffs.realSquare);
                    Game.setLastPos(camChar, ffs.realPos.x, ffs.realPos.y, ffs.realPos.z);
                }
                apply(fs, ffs.realPos, ffs.realSquare);
                if (ffs.fakeSquare != null && ffs.isFakeSquareExterior != null) {
                    reset(ffs.fakeSquare, ffs.isFakeSquareExterior);
                    ffs.isFakeSquareExterior = null;
                }
            }
        }
        catch (Throwable tt) {
            System.err.println("Staircast: failed to recover from error");
            tt.printStackTrace();
        }
        Mod.instance.debugOptions.enable.setValue(false);
        DebugLog.log(DebugType.Mod, "Staircast: mod disabled due to an error");
    }
}

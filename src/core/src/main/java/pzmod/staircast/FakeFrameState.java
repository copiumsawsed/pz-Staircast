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
}

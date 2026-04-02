package pzmod.staircast;

import zombie.ai.states.SwipeStatePlayer;
import zombie.characters.IsoGameCharacter;
import zombie.debug.BooleanDebugOption;
import zombie.debug.options.IDebugOptionGroup;
import zombie.debug.options.OptionGroup;
import zombie.iso.*;
import zombie.iso.SpriteDetails.IsoFlagType;
import zombie.iso.SpriteDetails.IsoObjectType;
import zombie.network.GameClient;

import java.lang.reflect.Field;

public final class Game {
    public static IsoGameCharacter getCamChar(IsoCamera.FrameState fs) {
        return fs.CamCharacter;
    }

    public static IsoGameCharacter getCamChar() {
        return IsoCamera.getCamCharacter();
    }

    public static void setCamCharPos(IsoCamera.FrameState fs, float x, float y, float z) {
        fs.CamCharacterX = x;
        fs.CamCharacterY = y;
        fs.CamCharacterZ = z;
    }

    public static void setCamCharSquare(IsoCamera.FrameState fs, IsoGridSquare square) {
        fs.CamCharacterSquare = square;
    }

    public static IsoGridSquare getCamCharSquare(IsoCamera.FrameState fs) {
        return fs.CamCharacterSquare;
    }

    public static Vector3 getCamCharPos(IsoCamera.FrameState fs) {
        return new Vector3(fs.CamCharacterX, fs.CamCharacterY, fs.CamCharacterZ);
    }

    public static Field getDrawWorldField() throws NoSuchFieldException {
        return IsoWorld.class.getDeclaredField("bDrawWorld");
    }

    public static boolean isClient() {
        return GameClient.bClient;
    }

    public static boolean isFboChunkRenderEnabled() {
        return false;
    }

    public static OptionGroup createOptionGroup(IDebugOptionGroup parent, String name) {
        if (parent != null) {
            return new OptionGroup(parent, name);
        }
        return new OptionGroup(name);
    }

    public static BooleanDebugOption newOption(IDebugOptionGroup parentGroup, String name, boolean defaultValue) {
        return OptionGroup.newOption(parentGroup, name, defaultValue);
    }

    public static BooleanDebugOption newDebugOnlyOption(IDebugOptionGroup parentGroup, String name, boolean defaultValue) {
        return OptionGroup.newDebugOnlyOption(parentGroup, name, defaultValue);
    }

    public static Vector3 getBoneWorldPos(IsoMovingObject target, String boneName, Vector3 bonePos) {
        return SwipeStatePlayer.getBoneWorldPos(target, boneName, bonePos);
    }

    public static boolean squareHas(IsoGridSquare square, IsoObjectType type) {
        return square.Has(type);
    }

    public static boolean squareHas(IsoGridSquare square, IsoFlagType flag) {
        return square.Is(flag);
    }

    public static void setSquareProperty(IsoGridSquare square, IsoFlagType flag, boolean set) {
        if (set == squareHas(square, flag)) {
            return;
        }
        if (set) {
            square.getProperties().Set(flag);
        } else {
            square.getProperties().UnSet(flag);
        }
    }

    public static void assignRoomId(IsoGridSquare square, long roomId) {
        square.roomID = (int) roomId;
    }

    public static void setLastPos(IsoMovingObject o, float x, float y, float z) {
        o.setLx(x);
        o.setLy(y);
        o.setLz(z);
    }
}

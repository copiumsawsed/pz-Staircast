package pzmod.staircast;

import zombie.CombatManager;
import zombie.characters.IsoGameCharacter;
import zombie.core.PerformanceSettings;
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
        return fs.camCharacter;
    }

    public static IsoGameCharacter getCamChar() {
        return IsoCamera.getCameraCharacter();
    }

    public static void setCamCharPos(IsoCamera.FrameState fs, float x, float y, float z) {
        fs.camCharacterX = x;
        fs.camCharacterY = y;
        fs.camCharacterZ = z;
    }

    public static void setCamCharSquare(IsoCamera.FrameState fs, IsoGridSquare square) {
        fs.camCharacterSquare = square;
    }

    public static IsoGridSquare getCamCharSquare(IsoCamera.FrameState fs) {
        return fs.camCharacterSquare;
    }

    public static Vector3 getCamCharPos(IsoCamera.FrameState fs) {
        return new Vector3(fs.camCharacterX, fs.camCharacterY, fs.camCharacterZ);
    }

    public static Field getDrawWorldField() throws NoSuchFieldException {
        return IsoWorld.class.getDeclaredField("drawWorld");
    }

    public static boolean isClient() {
        return GameClient.client;
    }

    public static boolean isFboChunkRenderEnabled() {
        return PerformanceSettings.fboRenderChunk;
    }

    public static OptionGroup createOptionGroup(IDebugOptionGroup parent, String name) {
        return new OptionGroup(parent, name);
    }

    public static BooleanDebugOption newOption(IDebugOptionGroup parentGroup, String name, boolean defaultValue) {
        return BooleanDebugOption.newOption(parentGroup, name, defaultValue);
    }

    public static BooleanDebugOption newDebugOnlyOption(IDebugOptionGroup parentGroup, String name, boolean defaultValue) {
        return BooleanDebugOption.newDebugOnlyOption(parentGroup, name, defaultValue);
    }

    public static Vector3 getBoneWorldPos(IsoMovingObject target, String boneName, Vector3 bonePos) {
        return CombatManager.getBoneWorldPos(target, boneName, bonePos);
    }

    public static boolean squareHas(IsoGridSquare square, IsoObjectType type) {
        return square.has(type);
    }

    public static boolean squareHas(IsoGridSquare square, IsoFlagType flag) {
        return square.has(flag);
    }

    public static void setSquareProperty(IsoGridSquare square, IsoFlagType flag, boolean set) {
        if (set == !squareHas(square, flag)) {
            if (set) {
                square.getProperties().set(flag);
            } else {
                square.getProperties().unset(flag);
            }
        }
    }

    public static void assignRoomId(IsoGridSquare square, long roomId) {
        square.roomId = roomId;
    }

    public static void setLastPos(IsoMovingObject o, float x, float y, float z) {
        o.setLastX(x);
        o.setLastX(y);
        o.setLastX(z);
    }
}

package pzmod.staircast;

import me.zed_0xff.zombie_buddy.Patch;
import org.lwjglx.input.Keyboard;
import zombie.CombatManager;
import zombie.core.Core;
import zombie.core.PerformanceSettings;
import zombie.core.math.PZMath;
import zombie.input.GameKeyboard;
import zombie.iso.IsoCamera;
import zombie.iso.IsoWorld;
import zombie.iso.SpriteDetails.IsoObjectType;
import zombie.iso.Vector3;

import java.lang.reflect.Field;

public class Patch_IsoWorld {
    public static Field field_drawWorld;

    static {
        try {
            field_drawWorld = IsoWorld.class.getDeclaredField("drawWorld");
            field_drawWorld.trySetAccessible();
        } catch (NoSuchFieldException ignore) {
        }
    }

    public static boolean getDrawWorld(IsoWorld world) {
        try {
            return field_drawWorld.getBoolean(world);
        } catch (Exception ignore) {
            return false;
        }
    }

    @Patch(className = "zombie.iso.IsoWorld", methodName = "renderInternal")
    public static class Patch_renderInternal {
        @Patch.OnEnter
        public static void enter(@Patch.This IsoWorld self) { // @Advice.FieldValue("drawWorld") boolean drawWorld) { // FIXME can't get it working
            var camChar = IsoCamera.getCameraCharacter();
            if (camChar == null || !PerformanceSettings.fboRenderChunk || !getDrawWorld(self)) {
                return;
            }

            final var viewpointHeight = 0.55f;
            final var viewpointSnapThreshold = 0.02f;
            final var stepHeight = 0.11f;
            // Is player on stairs?
            var fs = IsoCamera.frameState;
            var square = fs.camCharacterSquare;
            if (square != null
                && PZMath.fastfloor(fs.camCharacterZ + viewpointHeight) >= fs.camCharacterZ
                && camChar.getVehicle() == null
                && camChar.hasActiveModel()
                && square.HasElevatedFloor())
            {
                // Is player heading upstairs?
                var stairsNorth = square.HasStairsNorth();
                var heading = PZMath.wrap(camChar.getLookAngleRadians() - (stairsNorth ? PZMath.PI : PZMath.PI / 2f), PZMath.PI2);
                var cone = (PZMath.PI - PZMath.PI / 4) / 2 * (1 / viewpointHeight * PZMath.frac(fs.camCharacterZ + viewpointHeight));
                if (heading > PZMath.PI + cone && heading < PZMath.PI2 - cone) {
                    return;
                }

                // Does next floor square exist?
                var stairTop = square.HasStairTop();
                var topOffset = stairTop ? 1 : (square.has(IsoObjectType.stairsMN) || square.has(IsoObjectType.stairsMW) ? 2 : 3);
                var floorSquare = camChar.getCell().getGridSquare(
                        square.x - (stairsNorth ? 0 : topOffset),
                        square.y - (stairsNorth ? topOffset : 0),
                        square.z + 1);
                if (floorSquare == null) {
                    return;
                }

                var ffs = FakeFrameState.get(fs.playerIndex);
                // Is the next level rendered by the game?
                // FIXME
                /*if (ffs != null && floorSquare != ffs.renderedSquare) {
                    var cutaway = floorSquare.chunk.getCutawayDataForLevel(floorSquare.getZ());
                    if (cutaway != null && cutaway.shouldRenderSquare(fs.playerIndex, floorSquare)) {
                        return;
                    }
                }*/

                var upperSquare = square.getSquareAbove();
                var targetSquare = upperSquare != null ? upperSquare : floorSquare;

                // Can the player character actually see the next level?
                var headPos = new Vector3();
                CombatManager.getBoneWorldPos(camChar, "Bip01_Head", headPos);
                headPos.z += 0.05f;
                var headZ = ffs != null
                        ? (Math.abs(headPos.z - ffs.lastViewpointZ) > viewpointSnapThreshold ? headPos.z : ffs.lastViewpointZ)
                        : headPos.z;
                if (PZMath.fastfloor(headZ) < targetSquare.z) {
                    return;
                }

                if (ffs == null) {
                    FakeFrameState.frameState[fs.playerIndex] = ffs = new FakeFrameState();
                }
                ffs.renderLighting = stairTop;

                // Workaround to not let zombies hide on stairs in the dark.
                if (PZMath.fastfloor(fs.camCharacterZ + stepHeight) < targetSquare.z) {
                    for (int y = square.y - 1; y <= square.y + 1; ++y) {
                        for (int x = square.x - 1; x <= square.x + 1; ++x) {
                            var stairSquare = camChar.getCell().getGridSquare(x, y, square.z);
                            if (stairSquare != null && stairSquare.HasStairs() && stairSquare.getZombie() != null) {
                                ffs.renderLighting = false;
                                break;
                            }
                        }
                    }
                }

                // TODO configurable key?
                if (Core.debug && GameKeyboard.isKeyDown(Keyboard.KEY_Z)) {
                    return;
                }

                ffs.lastViewpointZ = headPos.z;
                ffs.realPos.set(fs.camCharacterX, fs.camCharacterY, fs.camCharacterZ);
                ffs.realSquare = fs.camCharacterSquare;
                ffs.frameCounter = fs.frameCount;
                ffs.fakePos.set(targetSquare.getX() + 0.5f, targetSquare.getY() + 0.5f, targetSquare.getZ());
                ffs.fakeSquare = targetSquare;
                ffs.renderedSquare = floorSquare;
            }
        }
    }
}

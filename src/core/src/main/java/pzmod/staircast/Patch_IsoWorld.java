package pzmod.staircast;

import me.zed_0xff.zombie_buddy.Patch;
import zombie.core.math.PZMath;
import zombie.debug.LineDrawer;
import zombie.iso.IsoCamera;
import zombie.iso.IsoWorld;
import zombie.iso.SpriteDetails.IsoFlagType;
import zombie.iso.SpriteDetails.IsoObjectType;
import zombie.iso.Vector3;

import java.lang.reflect.Field;

public class Patch_IsoWorld {
    public static Field field_drawWorld;

    static {
        try {
            field_drawWorld = Game.getDrawWorldField();
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
            try {
                render(self);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        public static void render(IsoWorld self) {
            Mod.instance.trace("IsoWorld::renderInternal");

            if (!Mod.instance.debugOptions.enable.getValue()) {
                return;
            }
            var camChar = Game.getCamChar();
            if (camChar == null || !getDrawWorld(self)) {
                return;
            }

            final var viewpointHeight = 0.55f;
            final var viewpointSnapThreshold = 0.02f;
            final var stepHeight = 0.11f;
            // Is player on stairs?
            var fs = IsoCamera.frameState;
            var square = Game.getCamCharSquare(fs);
            var charPos = Game.getCamCharPos(fs);
            if (square != null
                && PZMath.fastfloor(charPos.z + viewpointHeight) >= charPos.z
                && camChar.getVehicle() == null
                && camChar.hasActiveModel()
                && square.HasElevatedFloor())
            {
                // Is player heading upstairs?
                var stairsNorth = square.HasStairsNorth();
                var heading = PZMath.wrap(camChar.getLookAngleRadians() - (stairsNorth ? PZMath.PI : PZMath.PI / 2f), PZMath.PI2);
                var cone = (PZMath.PI - PZMath.PI / 4) / 2 * (1 / viewpointHeight * PZMath.frac(charPos.z + viewpointHeight));
                if (heading > PZMath.PI + cone && heading < PZMath.PI2 - cone) {
                    return;
                }

                // Does next floor square exist?
                var stairTop = Game.squareHas(square, IsoObjectType.stairsTN) || Game.squareHas(square, IsoObjectType.stairsTW);
                var stairMid = Game.squareHas(square, IsoObjectType.stairsMN) || Game.squareHas(square, IsoObjectType.stairsMW);
                var topOffset = stairTop ? 1 : (stairMid ? 2 : 3);
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

                var upperSquare = square.getCell().getGridSquare(square.x, square.y, square.z + 1);
                var targetSquare = upperSquare != null ? upperSquare : floorSquare;

                // Can the player character actually see the next level?
                var headPos = new Vector3();
                Game.getBoneWorldPos(camChar, "Bip01_Head", headPos);
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
                ffs.renderLighting = stairTop && Mod.instance.debugOptions.renderLighting.getValue();

                // Workaround to not let zombies hide on stairs in the dark.
                if (PZMath.fastfloor(charPos.z + stepHeight) < targetSquare.z) {
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

                if (Mod.instance.debugOptions.useFloorSquare.getValue()) {
                    targetSquare = floorSquare;
                }
                ffs.lastViewpointZ = headPos.z;
                ffs.realPos.set(charPos);
                ffs.realSquare = square;
                ffs.floorSquare = floorSquare;
                ffs.fakeSquare = targetSquare;
                if (Mod.instance.debugOptions.useSquarePos.getValue()) {
                    ffs.fakePos.set(targetSquare.getX() + 0.5f, targetSquare.getY() + 0.5f, targetSquare.getZ());
                } else {
                    ffs.fakePos.set(charPos.x, charPos.y, targetSquare.getZ());
                }

                if (Mod.instance.debugOptions.drawTargetSquare.getValue()) {
                    var exterior = targetSquare.getBuilding() == null && Game.squareHas(targetSquare, IsoFlagType.exterior);
                    LineDrawer.addRect(targetSquare.x, targetSquare.y, targetSquare.z, 1f, 1f, 0.7f, 0.7f, exterior ? 1f : 0f);
                    if (targetSquare.getBuilding() == null) {
                        LineDrawer.addLine(
                                targetSquare.x,
                                targetSquare.y,
                                targetSquare.z,
                                targetSquare.x + 1,
                                targetSquare.y + 1,
                                targetSquare.z,
                                0.3f, 0.3f, 1f, 1f);
                    }
                    if (Game.squareHas(targetSquare, IsoFlagType.exterior)) {
                        LineDrawer.addLine(
                                targetSquare.x + 1,
                                targetSquare.y,
                                targetSquare.z,
                                targetSquare.x,
                                targetSquare.y + 1,
                                targetSquare.z,
                                1.0f, 0.3f, 0.3f, 1f);
                    }

                }
                if (Mod.instance.debugOptions.drawTargetPos.getValue()) {
                    LineDrawer.DrawIsoCircle(ffs.fakePos.x, ffs.fakePos.y, ffs.fakePos.z, 0.3f, 12, 0f, 1f, 0f, 1f);
                }

                // Update this last in case of an exception.
                ffs.frameCounter = fs.frameCount;
            }
        }
    }
}

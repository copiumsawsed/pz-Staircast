package pzmod.staircast;

import zombie.debug.BooleanDebugOption;
import zombie.debug.options.OptionGroup;

public class DebugOptions {
    public final OptionGroup group = Game.createOptionGroup(null, "Mod.Staircast");

    public final BooleanDebugOption enable = Game.newOption(this.group, "Enable", true);
    public final BooleanDebugOption logCalls = debugOption("LogCalls", false);
    public final BooleanDebugOption renderPlayer = debugOption("RenderPlayer", true);
    public final BooleanDebugOption renderLighting = debugOption("RenderLighting", true);
    public final BooleanDebugOption drawTargetSquare = debugOption("DrawTargetSquare", false);
    public final BooleanDebugOption drawTargetPos = debugOption("DrawTargetPosition", false);
    public final BooleanDebugOption useSquarePos = debugOption("UseSquarePosition", false);
    public final BooleanDebugOption useFloorSquare = debugOption("UseFloorSquare", false);
    public final BooleanDebugOption assignRoomPermanently = debugOption("AssignRoomPermanently", true);

    private BooleanDebugOption debugOption(String name, boolean defaultValue) {
        return Game.newDebugOnlyOption(group, name, defaultValue);
    }
}

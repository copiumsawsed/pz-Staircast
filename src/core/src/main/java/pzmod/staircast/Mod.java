package pzmod.staircast;

import zombie.debug.DebugLog;
import zombie.debug.DebugType;

public class Mod {
    public static Mod instance = new Mod();

    public DebugOptions debugOptions = new DebugOptions();

    public void init() {
        zombie.debug.DebugOptions.instance.addChild(debugOptions.group);
    }

    public void trace(String str) {
        if (debugOptions.logCalls.getValue()) {
            DebugLog.log(DebugType.Mod, "@ " + str);
        }
    }
}

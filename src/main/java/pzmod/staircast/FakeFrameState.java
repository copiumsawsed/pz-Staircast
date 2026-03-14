package pzmod.staircast;

import zombie.characters.IsoGameCharacter;
import zombie.iso.IsoCamera;
import zombie.iso.IsoGridSquare;
import zombie.iso.Vector3;

public class FakeFrameState {
    public static FakeFrameState[] frameState = new FakeFrameState[4];

    public Vector3 realPos = new Vector3();
    public Vector3 fakePos = new Vector3();
    public IsoGridSquare realSquare;
    public IsoGridSquare fakeSquare;
    public int frameCounter;
    public float lastViewpointZ;
    public boolean renderLighting;
    public IsoGridSquare renderedSquare;

    static public boolean isRendering(int playerIndex) {
        return frameState[playerIndex] != null && frameState[playerIndex].frameCounter == IsoCamera.frameState.frameCount;
    }

    static public FakeFrameState get(int playerIndex) {
        return frameState[playerIndex];
    }

    static public void apply(IsoGameCharacter c, Vector3 pos) {
        c.setZ(pos.z);
    }

    static public void apply(IsoGameCharacter c, IsoGridSquare square) {
        c.setCurrent(square);
    }

    static public void apply(IsoGameCharacter c, Vector3 pos, IsoGridSquare square) {
        apply(c, pos);
        apply(c, square);
    }

    static public void apply(IsoCamera.FrameState fs, Vector3 pos, IsoGridSquare square) {
        fs.camCharacterZ = pos.z;
        fs.camCharacterSquare = square;
    }
}

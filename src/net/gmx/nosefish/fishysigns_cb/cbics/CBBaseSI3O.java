package net.gmx.nosefish.fishysigns_cb.cbics;

import net.gmx.nosefish.fishylib.worldmath.FishyLocationInt;
import net.gmx.nosefish.fishysigns.iobox.LeverIOBox;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;

public abstract class CBBaseSI3O extends CBBaseIC {

	public CBBaseSI3O(UnloadedSign sign) {
		super(sign);
	}

	@Override
	public void initializeOutputBox() {
		outputBox = LeverIOBox.createAndRegister(3, new LeverClickBlocker(this));
		FishyLocationInt[] outputLocations = new FishyLocationInt[]{
				this.getCentreOutput(1),
				this.getLeftOutput(1),
				this.getRightOutput(1)
		};
		outputBox.setAllOutputLocations(outputLocations);
		outputBox.finishInit();
	}
}

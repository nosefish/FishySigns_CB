package net.gmx.nosefish.fishysigns_cb.cbics.logic;

import java.util.regex.Pattern;

import net.gmx.nosefish.fishysigns.annotation.FishySignIdentifier;
import net.gmx.nosefish.fishysigns.iobox.IOSignal;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;
import net.gmx.nosefish.fishysigns_cb.cbics.CBBase3ISO;

public class MC3020 extends CBBase3ISO {
	@FishySignIdentifier
	public static final Pattern[] regEx = {
		null,
		Pattern.compile("\\[MC3020\\].*", Pattern.CASE_INSENSITIVE),
		null,
		null
	};
	
	public MC3020(UnloadedSign sign) {
		super(sign);
	}

	@Override
	public String getCode() {
		return "[MC3020]";
	}

	@Override
	public String getName() {
		return "XOR";
	}

	@Override
	public String getHelpText() {
		return "Logic gate: XOR. output = input1 XOR input2. Outputs high if the inputs are not equal";
	}

	@Override
	protected void initializeIC() {
		refresh();
	}

	@Override
	public void handleDirectInputChange(IOSignal oldS, IOSignal newS, long tickStamp) {
		boolean xor = newS.getState(0) != newS.getState(1);
		this.updateOutput(IOSignal.factory(xor), tickStamp);
	}

}

package net.gmx.nosefish.fishysigns_cb.cbics.logic;

import java.util.regex.Pattern;

import net.gmx.nosefish.fishysigns.annotation.FishySignIdentifier;
import net.gmx.nosefish.fishysigns.iobox.IOSignal;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;
import net.gmx.nosefish.fishysigns_cb.cbics.CBBase3ISO;

public class MC3021 extends CBBase3ISO {
	@FishySignIdentifier
	public static final Pattern[] regEx = {
		null,
		Pattern.compile("\\[MC3021\\].*",Pattern.CASE_INSENSITIVE),
		null,
		null
	};
	
	
	public MC3021(UnloadedSign sign) {
		super(sign);
	}

	@Override
	public String getCode() {
		return "[MC3021]";
	}

	@Override
	public String getName() {
		return "XNOR";
	}

	@Override
	public String getHelpText() {
		return "Logic gate: XNOR. output = input1 XNOR input2 - output is high if the inputs are equal.";
	}

	@Override
	protected void initializeIC() {
		this.refresh();
	}

	@Override
	public void handleDirectInputChange(IOSignal oldS, IOSignal newS, long tickStamp) {
		boolean xnor = newS.getState(0) == newS.getState(1);
		this.updateOutput(IOSignal.factory(xnor), tickStamp);
	}

}

package net.gmx.nosefish.fishysigns_cb.cbics.logic;

import java.util.regex.Pattern;

import net.gmx.nosefish.fishysigns.annotation.FishySignIdentifier;
import net.gmx.nosefish.fishysigns.iobox.IOSignal;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;
import net.gmx.nosefish.fishysigns_cb.cbics.CBBase3ISO;

public class MC3002 extends CBBase3ISO {
	

	@FishySignIdentifier
	public static final Pattern[] regEx = {
		null,
		Pattern.compile("\\[MC3002\\].*", Pattern.CASE_INSENSITIVE),
		null,
		null
		};

	public MC3002(UnloadedSign sign) {
		super(sign);
	}

	@Override
	public String getCode() {
		return "[MC3002]";
	}

	@Override
	public String getName() {
		return "AND";
	}

	@Override
	public String getHelpText() {
		return "Logic gate: AND. Output is high if all inputs are high";
	}

	@Override
	protected void initializeIC() {
		refresh();
	}

	@Override
	public void handleDirectInputChange(IOSignal oldS, IOSignal newS, long tickStamp) {
		// AND the inputs
		boolean and = newS.getState(0) && newS.getState(1) && newS.getState(2);
		this.updateOutput(IOSignal.factory(and), tickStamp);
	}

}

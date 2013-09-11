package net.gmx.nosefish.fishysigns_cb.cbics.logic;

import java.util.regex.Pattern;

import net.gmx.nosefish.fishysigns.annotation.FishySignIdentifier;
import net.gmx.nosefish.fishysigns.iobox.IOSignal;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;
import net.gmx.nosefish.fishysigns_cb.cbics.CBBase3ISO;

public class MC3003 extends CBBase3ISO {


@FishySignIdentifier
public static final Pattern[] regEx = {
	null,
	Pattern.compile("\\[MC3003\\].*", Pattern.CASE_INSENSITIVE),
	null,
	null
};


	public MC3003(UnloadedSign sign) {
		super(sign);
	}

	@Override
	public String getCode() {
		return "[MC3003]";
	}

	@Override
	public String getName() {
		return "NAND";
	}

	@Override
	public String getHelpText() {
		return "Logic gate: 3-Input NAND. Output is high if all inputs are low";
	}

	@Override
	protected void initializeIC() {
		this.refresh();
	}

	@Override
	public void handleDirectInputChange(IOSignal oldS, IOSignal newS) {
		boolean nand = !(newS.getState(0) || newS.getState(1) || newS.getState(2));
		updateOutput(IOSignal.factory(nand));
	}

}

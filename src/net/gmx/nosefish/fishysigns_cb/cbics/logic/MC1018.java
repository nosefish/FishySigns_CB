package net.gmx.nosefish.fishysigns_cb.cbics.logic;

import java.util.regex.Pattern;

import net.gmx.nosefish.fishysigns.annotation.FishySignIdentifier;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;
import net.gmx.nosefish.fishysigns.iobox.FishySignSignal;
import net.gmx.nosefish.fishysigns_cb.cbics.CBBaseIC;


public class MC1018 extends CBBaseIC {
	@FishySignIdentifier
	public static final Pattern[] regEx = {
		null,
		Pattern.compile("\\[MC1018\\].*", Pattern.CASE_INSENSITIVE),
		null,
		null
		};
	
	
	public MC1018(UnloadedSign sign) {
		super(sign);
	}

	@Override
	public String getCode() {
		return "[MC1018]";
	}

	@Override
	public String getName() {
		return "FALLING TOGGLE";
	}

	@Override
	public String getHelpText() {
		return "Logic gate: Falling edge t-FlipFlop. Toggles output when input changes from high to low";
	}

	@Override
	protected void initializeIC() {
		// nothing to do
	}

	@Override
	public void handleDirectInputChange(FishySignSignal oldS, FishySignSignal newS) {
		if (oldS == newS) {
			// refresh, shouldn't happen
			return;
		}
		if (oldS.getState(0) && ! newS.getState(0)) {
			this.updateOutput(new FishySignSignal(! outputBox.getSignal().getState(0)));
		}
	}
}

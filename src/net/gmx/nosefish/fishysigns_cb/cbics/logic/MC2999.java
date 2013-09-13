package net.gmx.nosefish.fishysigns_cb.cbics.logic;

import java.util.regex.Pattern;

import net.gmx.nosefish.fishysigns.annotation.FishySignIdentifier;
import net.gmx.nosefish.fishysigns.iobox.IOSignal;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;
import net.gmx.nosefish.fishysigns_cb.cbics.CBBaseSI3O;

public class MC2999 extends CBBaseSI3O {
	@FishySignIdentifier
	public static final Pattern[] regEx = {
		null,
		Pattern.compile("\\[MC2999\\].*", Pattern.CASE_INSENSITIVE),
		null,
		null
	};
	
	protected int state;
	
	public MC2999(UnloadedSign sign) {
		super(sign);
	}

	@Override
	public String getCode() {
		return "[MC2999]";
	}

	@Override
	public String getName() {
		return "MARQUEE";
	}

	@Override
	public String getHelpText() {
		// TODO: improve
		return "Every time the input goes high, the next output is activated.";
	}

	@Override
	protected void initializeIC() {
		boolean[] out = outputBox.getSignal().toArray();
		boolean found = false;
		// determine state and make sure it's valid
		state = 0;
		for (int pin = 0; pin < out.length; pin++ ) {
			if (out[pin]) {
				if (found) {
					out[pin] = false;
				} else {
					found = true;
					state = pin;
				}
			}
		}
		this.updateOutputNow(IOSignal.factory(out));
	}

	@Override
	public void handleDirectInputChange(IOSignal oldS, IOSignal newS, long tickStamp) {
		if (isRisingEdge(oldS, newS, 0)) {
			state = (state == 2 ? 0 : state + 1);
			boolean[] out = new boolean[]{false, false, false};
			out[state] = true;
			this.updateOutput(IOSignal.factory(out), tickStamp);
		}
	}

}

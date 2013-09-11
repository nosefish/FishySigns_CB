package net.gmx.nosefish.fishysigns_cb.cbics.logic;

import java.util.regex.Pattern;

import net.gmx.nosefish.fishysigns.annotation.FishySignIdentifier;
import net.gmx.nosefish.fishysigns.iobox.IOSignal;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;
import net.gmx.nosefish.fishysigns_cb.cbics.CBBase3ISO;

public class MC3034 extends CBBase3ISO {
	@FishySignIdentifier
	public static final Pattern[] regEx = {
		null,
		Pattern.compile("\\[MC3034\\].*", Pattern.CASE_INSENSITIVE),
		null,
		null
	};

	
	public MC3034(UnloadedSign sign) {
		super(sign);
	}

	@Override
	public String getCode() {
		return "[MC3034]";
	}

	@Override
	public String getName() {
		return "D EDGE FLIPFLOP";
	}

	@Override
	public String getHelpText() {
		return "Logic gate: rising-edge-triggered D-FlipFlop. Input 1: clock. Input 2: D. Input 3: reset.";
	}

	@Override
	protected void initializeIC() {
	}

	@Override
	public void handleDirectInputChange(IOSignal oldS, IOSignal newS) {
		boolean D = newS.getState(1);
		boolean reset = newS.getState(2);
		boolean clk = isRisingEdge(oldS, newS, 0);
		if (reset) {
			updateOutput(IOSignal.L);
		} else if (clk) {
           updateOutput(IOSignal.factory(D));
        }
	}

}

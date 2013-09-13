package net.gmx.nosefish.fishysigns_cb.cbics.logic;

import java.util.regex.Pattern;

import net.gmx.nosefish.fishysigns.annotation.FishySignIdentifier;
import net.gmx.nosefish.fishysigns.iobox.IOSignal;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;
import net.gmx.nosefish.fishysigns_cb.cbics.CBBase3ISO;

public class MC3032 extends CBBase3ISO {
	@FishySignIdentifier
	public static final Pattern[] regEx = {
		null,
		Pattern.compile("\\[MC3032\\].*", Pattern.CASE_INSENSITIVE),
		null,
		null
	};

	
	public MC3032(UnloadedSign sign) {
		super(sign);
	}

	@Override
	public String getCode() {
		return "[MC3032]";
	}

	@Override
	public String getName() {
		return "JKEDGE FLIPFLOP";
	}

	@Override
	public String getHelpText() {
		return "Logic gate: falling edge triggered J-K-FlipFlop." +
				"Input 1: clock. Input 2: J. imput 3: K.";
	}

	@Override
	protected void initializeIC() {
		//TODO: for pure CraftBook wire input2 to pin1.
		// In CBX, only input 2 is used and input swap is possible.
		// Decide (CBX, of course, it's primarily for MCO),
		// or offer a config option once the config system is in place
	}

	@Override
	public void handleDirectInputChange(IOSignal oldS, IOSignal newS, long tickStamp) {
        boolean j = newS.getState(1); //Set
        boolean k = newS.getState(2); //Reset
        boolean clk = isFallingEdge(oldS, newS, 0);
        if (clk) {
            if (j && k) {
                this.toggleOutput(0, tickStamp);
            } else if (j && !k) {
            	this.updateOutput(IOSignal.H, tickStamp);
            } else if (!j && k) {
            	this.updateOutput(IOSignal.L, tickStamp);
            }
        }
	}

}

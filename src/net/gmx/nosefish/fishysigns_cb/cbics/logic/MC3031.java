package net.gmx.nosefish.fishysigns_cb.cbics.logic;

import java.util.regex.Pattern;

import net.gmx.nosefish.fishysigns.annotation.FishySignIdentifier;
import net.gmx.nosefish.fishysigns.iobox.IOSignal;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;
import net.gmx.nosefish.fishysigns_cb.cbics.CBBase3ISO;

public class MC3031 extends CBBase3ISO {
	@FishySignIdentifier
	public static final Pattern[] regEx = {
		null,
		Pattern.compile("\\[MC3031\\].*", Pattern.CASE_INSENSITIVE),
		null,
		null
	};

	
	public MC3031(UnloadedSign sign) {
		super(sign);
	}

	@Override
	public String getCode() {
		return "[MC3031]";
	}

	@Override
	public String getName() {
		return "INV RS NAND LAT";
	}

	@Override
	public String getHelpText() {
		return "Logic gate: RS-NAND-Latch with inverted inputs. Input 1: set. Input 2: reset.";
	}

	@Override
	protected void initializeIC() {
		//TODO: for pure CraftBook wire input2 to pin1.
		// In CBX, only input 2 is used and input swap is possible.
		// Decide (CBX, of course, it's primarily for MCO),
		// or offer a config option once the config system is in place
	}

	@Override
	public void handleDirectInputChange(IOSignal oldS, IOSignal newS) {
		boolean set = newS.getState(0);
		boolean reset = newS.getState(1);
        if (!set && !reset) {
        	updateOutput(IOSignal.H);
        } else if (set && !reset) {
        	updateOutput(IOSignal.L);
        } else if (!set && reset) {
        	updateOutput(IOSignal.H);
        }
	}

}

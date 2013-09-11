package net.gmx.nosefish.fishysigns_cb.cbics.logic;

import java.util.regex.Pattern;

import net.gmx.nosefish.fishysigns.annotation.FishySignIdentifier;
import net.gmx.nosefish.fishysigns.iobox.IOSignal;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;
import net.gmx.nosefish.fishysigns_cb.cbics.CBBase3ISO;

public class MC3040 extends CBBase3ISO {
	@FishySignIdentifier
	public static final Pattern[] regEx = {
		null,
		Pattern.compile("\\[MC3040\\].*", Pattern.CASE_INSENSITIVE),
		null,
		null
	};

	
	public MC3040(UnloadedSign sign) {
		super(sign);
	}

	@Override
	public String getCode() {
		return "[MC3040]";
	}

	@Override
	public String getName() {
		return "MULTIPLEXER";
	}

	@Override
	public String getHelpText() {
		return "Logic gate: multiplexer. If input 3 is high, input 1 is passed to the output," +
				"if input 3 is low, input 2 is forwarded.";
	}

	@Override
	protected void initializeIC() {
		refresh();
	}

	@Override
	public void handleDirectInputChange(IOSignal oldS, IOSignal newS) {
		boolean in1 = newS.getState(0);
		boolean in2 = newS.getState(1);
		boolean sel = newS.getState(2);
		boolean out = sel ? in1 : in2;
        updateOutput(IOSignal.factory(out));
	}

}

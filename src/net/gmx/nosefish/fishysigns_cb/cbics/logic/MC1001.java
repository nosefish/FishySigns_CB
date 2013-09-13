package net.gmx.nosefish.fishysigns_cb.cbics.logic;

import java.util.regex.Pattern;

import net.gmx.nosefish.fishysigns.annotation.FishySignIdentifier;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;
import net.gmx.nosefish.fishysigns.iobox.IOSignal;
import net.gmx.nosefish.fishysigns_cb.cbics.CBBaseIC;


public class MC1001 extends CBBaseIC {
	@FishySignIdentifier
	public static final Pattern[] regEx = {
		null,
		Pattern.compile("\\[MC1001\\].*", Pattern.CASE_INSENSITIVE),
		null,
		null
		};

	
	public MC1001(UnloadedSign sign) {
		super(sign);
	}

	@Override
	public String getCode() {
		return "[MC1001]";
	}

	@Override
	public String getName() {
		return "NOT";
	}

	@Override
	public String getHelpText() {
		return "Logic function: O = NOT(I). Inverts the input signal.";
	}

	@Override
	protected void initializeIC() {
		refresh();
	}

	@Override
	public void handleDirectInputChange(IOSignal oldS, IOSignal newS, long tickStamp) {
		this.updateOutput(newS.getInverse(), tickStamp);
	}

}

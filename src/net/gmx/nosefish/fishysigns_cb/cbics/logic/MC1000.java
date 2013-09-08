package net.gmx.nosefish.fishysigns_cb.cbics.logic;

import java.util.regex.Pattern;

import net.gmx.nosefish.fishysigns.annotation.FishySignIdentifier;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;
import net.gmx.nosefish.fishysigns.iobox.FishySignSignal;
import net.gmx.nosefish.fishysigns_cb.cbics.CBBaseIC;

public class MC1000 extends CBBaseIC {
	@FishySignIdentifier
	public static final Pattern[] regEx = {
		null,
		Pattern.compile("\\[MC1000\\].*", Pattern.CASE_INSENSITIVE),
		null,
		null
		};


	public MC1000(UnloadedSign sign) {
		super(sign);
	}
	
	@Override
	public String getCode() {
		return "[MC1000]";
	}

	@Override
	public String getName() {
		return "REPEATER";
	}

	@Override
	public String getHelpText() {
		return "Logic Gate: repeater. The output is the same as the input. Not all that useful.";
	}
	
	@Override
	protected void initializeIC() {
		refresh();
	}

	@Override
	public void handleDirectInputChange(FishySignSignal oldS,
			FishySignSignal newS) {
		this.updateOutput(newS);
	}


}

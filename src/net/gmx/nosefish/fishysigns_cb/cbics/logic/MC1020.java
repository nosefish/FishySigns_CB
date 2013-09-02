package net.gmx.nosefish.fishysigns_cb.cbics.logic;

import java.util.Random;
import java.util.regex.Pattern;

import net.gmx.nosefish.fishysigns.annotation.FishySignIdentifier;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;
import net.gmx.nosefish.fishysigns.signs.plumbing.FishySignSignal;
import net.gmx.nosefish.fishysigns_cb.cbics.CBBaseIC;

//TODO: self-triggered version
public class MC1020 extends CBBaseIC {
	final static Random rng = new Random(System.nanoTime()); 
	
	@FishySignIdentifier
	public static final Pattern[] regEx = {
		null,
		Pattern.compile("\\[MC1020\\]", Pattern.CASE_INSENSITIVE),
		null,
		null
		};

	public MC1020(UnloadedSign sign) {
		super(sign);
	}

	@Override
	public String getCode() {
		return "MC1020";
	}

	@Override
	public String getName() {
		return "1-BIT RANDOM";
	}

	@Override
	public String getHelpText() {
		return "Logic gate: 1-bit random number generator. Random output when input changes from low to high.";
	}

	@Override
	public boolean shouldRefreshOnLoad() {
		return false;
	}

	@Override
	protected void onRedstoneInputChange(FishySignSignal oldS, FishySignSignal newS) {
		if (oldS == null) {
			return;
		}
		if (! oldS.getState(0) && newS.getState(0)) {
			this.updateOutput(new FishySignSignal(rng.nextBoolean()));
		}
	}

}

package net.gmx.nosefish.fishysigns_cb.cbics.logic;

import java.util.Random;
import java.util.regex.Pattern;

import net.gmx.nosefish.fishysigns.annotation.FishySignIdentifier;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;
import net.gmx.nosefish.fishysigns.iobox.FishySignSignal;
import net.gmx.nosefish.fishysigns_cb.cbics.CBBaseZISO;


public class MC1020 extends CBBaseZISO {
	final static Random rng = new Random(System.nanoTime()); 
	
	private boolean isSelfTriggered = false;
	
	@FishySignIdentifier
	public static final Pattern[] regEx = {
		null,
		Pattern.compile("\\[MC1020\\][S]?", Pattern.CASE_INSENSITIVE),
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
		String part1 = "Logic gate: 1-bit random number generator. ";
		String part2 = (allowSelfTrigger() ? "Self-triggered version." :
		                                     "Random output when input changes from low to high.");
		return  part1 + part2;
	}

	@Override
	public boolean shouldRefreshOnLoad() {
		return false;
	}

	@Override
	public void handleDirectInputChange(FishySignSignal oldS, FishySignSignal newS) {
		if (isSelfTriggered || oldS == null) {
			return;
		}
		if (! oldS.getState(0) && newS.getState(0)) {
			this.setRandomOutput();
		}
	}

	@Override
	public void handleServerOddTick(int tickNumber) {
		this.setRandomOutput();
	}
	
	private void setRandomOutput() {
		this.updateOutput(new FishySignSignal(rng.nextBoolean()));
	}

	@Override
	public void initialize() {
		isSelfTriggered = getOptionsFromSign().equalsIgnoreCase("S");
		super.initialize();
	}
	
	@Override
	protected boolean allowSelfTrigger() {
		return isSelfTriggered;
	}


	@Override
	protected void onUnload() {
		// nothing to do
	}

}

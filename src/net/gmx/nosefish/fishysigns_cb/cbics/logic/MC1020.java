package net.gmx.nosefish.fishysigns_cb.cbics.logic;

import java.util.Random;
import java.util.regex.Pattern;

import net.gmx.nosefish.fishysigns.annotation.FishySignIdentifier;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;
import net.gmx.nosefish.fishysigns.signtools.FishyParser;
import net.gmx.nosefish.fishysigns.signtools.PatternLib;
import net.gmx.nosefish.fishysigns.iobox.IOSignal;
import net.gmx.nosefish.fishysigns.iobox.ServerOddTickInputBox;
import net.gmx.nosefish.fishysigns.iobox.ServerOddTickInputBox.IServerOddTickHandler;
import net.gmx.nosefish.fishysigns_cb.cbics.CBBaseIC;


public class MC1020 extends CBBaseIC implements IServerOddTickHandler {
	final static Random rng = new Random(System.nanoTime());
	
	protected static final String key_SELF_TRIGGERED = "ST";
	
	protected boolean isSelfTriggered = false;
	
	@FishySignIdentifier
	public static final Pattern[] regEx = {
		null,
		Pattern.compile("\\[MC1020\\].*", Pattern.CASE_INSENSITIVE),
		null,
		null
		};

	public MC1020(UnloadedSign sign) {
		super(sign);
	}

	@Override
	public String getCode() {
		return "[MC1020]";
	}

	@Override
	public String getName() {
		return "1-BIT RANDOM";
	}

	@Override
	public String getHelpText() {
		String part1 = "Logic gate: 1-bit random number generator. ";
		String part2 = (isSelfTriggered ? "Self-triggered version." :
		                                     "Random output when input changes from low to high.");
		return  part1 + part2;
	}

	@Override
	protected synchronized void constructOptionRules() {
		super.constructOptionRules();
		// self-triggered, "S" after the IC-code
		icOptionRules[1].add(
				new FishyParser.Rule(
						PatternLib.pattern_CB_SELF_TRIGGERED,
						new FishyParser.Token(key_SELF_TRIGGERED)));
	}
	
	@Override
	protected synchronized void initializeIC() {
		isSelfTriggered = icOptions.containsKey(key_SELF_TRIGGERED);
		if (isSelfTriggered) {
			ServerOddTickInputBox.createAndRegister(this);
		}
	}

	@Override
	protected void initializeRSInputBox() {
		if (icOptions.containsKey(key_SELF_TRIGGERED)) {
			// do not create a DirectInputBox for self-triggered variant
			return;
		}
		super.initializeRSInputBox();
	}

	@Override
	public void handleDirectInputChange(IOSignal oldS, IOSignal newS) {
		// only called for redstone-triggered variant
		if (oldS == newS) {
			return;
		}
		if (! oldS.getState(0) && newS.getState(0)) {
			this.setRandomOutput();
		}
	}

	@Override
	public void handleServerOddTick(int tickNumber) {
		// only called for self-triggered variant
		this.setRandomOutput();
	}
	
	protected void setRandomOutput() {
		this.updateOutput(IOSignal.factory(rng.nextBoolean()));
	}



	@Override
	protected void onUnload() {
		// nothing to do
	}

}

package net.gmx.nosefish.fishysigns_cb.cbics.logic;

import java.util.Random;
import java.util.regex.Pattern;

import net.gmx.nosefish.fishylib.worldmath.FishyLocationInt;
import net.gmx.nosefish.fishysigns.annotation.FishySignIdentifier;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;
import net.gmx.nosefish.fishysigns.iobox.IOSignal;
import net.gmx.nosefish.fishysigns.iobox.LeverIOBox;


public class MC2020 extends MC1020 {
	final static Random rng = MC1020.rng;
	
	@FishySignIdentifier
	public static final Pattern[] regEx = {
		null,
		Pattern.compile("\\[MC2020\\].*", Pattern.CASE_INSENSITIVE),
		null,
		null
		};

	public MC2020(UnloadedSign sign) {
		super(sign);
	}

	@Override
	public String getCode() {
		return "[MC2020]";
	}

	@Override
	public String getName() {
		return "3-BIT RANDOM";
	}

	@Override
	public String getHelpText() {
		String part1 = "Logic gate: 3-bit random number generator. ";
		String part2 = (isSelfTriggered ? "Self-triggered version." :
		                                     "Random output when input changes from low to high.");
		return  part1 + part2;
	}

	@Override
	public void initializeOutputBox() {
		// we need 3 outputs instead of the standard single one
		outputBox = LeverIOBox.createAndRegister(3, new LeverClickBlocker(this));
		FishyLocationInt[] outputLocations = new FishyLocationInt[]{
				this.getCentreOutput(1),
				this.getLeftOutput(1),
				this.getRightOutput(1)
		};
		outputBox.setAllOutputLocations(outputLocations);
		outputBox.finishInit();
	}
	
	@Override
	protected void setRandomOutput() {
		// 3 outputs
		this.updateOutput(IOSignal.factory(
				rng.nextBoolean(),
				rng.nextBoolean(),
				rng.nextBoolean()));
	}
}

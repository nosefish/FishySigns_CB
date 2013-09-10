package net.gmx.nosefish.fishysigns_cb.cbics.world;


import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

import net.gmx.nosefish.fishylib.worldmath.FishyLocationInt;
import net.gmx.nosefish.fishylib.worldmath.FishyVectorInt;
import net.gmx.nosefish.fishysigns.annotation.FishySignIdentifier;
import net.gmx.nosefish.fishysigns.iobox.IOSignal;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;
import net.gmx.nosefish.fishysigns.signtools.FishyParser;
import net.gmx.nosefish.fishysigns.signtools.FishyParser.Rule;
import net.gmx.nosefish.fishysigns.signtools.FishyParser.Token;
import net.gmx.nosefish.fishysigns.signtools.PatternLib;
import net.gmx.nosefish.fishysigns.task.common.MultiLightningTask;
import net.gmx.nosefish.fishysigns_cb.cbics.CBBaseIC;

public class MC1203 extends CBBaseIC {
	@FishySignIdentifier
	public static final Pattern[] regEx = {
		null,
		Pattern.compile("\\[MC1203\\].*", Pattern.CASE_INSENSITIVE),
		null,
		null };
	
	static Random rng = new Random();
	
	protected static final String key_RADIUS = "R";
	protected static final String key_OFFSET = "XYZ";
	protected static final String key_R_O_DELIMITER = "=";
	protected static final String key_CHANCE = "P";
	
	protected static final double maxRadius = 5.0;
	
	protected volatile FishyLocationInt[] targets;
	protected volatile double radius = 0.0;
	protected volatile double chance = 1.0;
	
	public MC1203(UnloadedSign sign) {
		super(sign);
	}

	@Override
	public String getCode() {
		return "[MC1203]";
	}

	@Override
	public String getName() {
		return "ZEUS BOLT";
	}

	@Override
	public String getHelpText() {
		return "Lightning when input goes high";
	}
	
	@Override
	protected void constructOptionRules() {
		super.constructOptionRules();
		icOptionRules[2].add(new Rule(
				PatternLib.pattern_POSITIVE_FLOAT, 
				new Token(key_RADIUS)));
		icOptionRules[2].add(new Rule(
				Pattern.compile("\\="), 
				new Token(key_R_O_DELIMITER)));
		icOptionRules[2].add(new Rule(
				PatternLib.pattern_FISHY_VECTOR_INT, 
				new Token(key_OFFSET)));
		icOptionRules[3].add(new Rule(
				PatternLib.pattern_POSITIVE_FLOAT, 
				new Token(key_CHANCE)));
	};

	@Override
	public synchronized boolean validateOnCreate(String playerName) {
		if (!super.validateOnCreate(playerName)) {
			return false;
		}
		// TODO: anything to do here?
		return true;
	}
	
	@Override
	protected synchronized void initializeIC() {
		FishyLocationInt target = getLocation().addIntVector(BACK.toUnitIntVector());
		if (icOptions.containsKey(key_OFFSET)) {
			String offsetString = icOptions.get(key_OFFSET).getValue();
			FishyVectorInt offset = FishyParser.parseVectorInt(offsetString);
			target = target.addIntVector(offset);
		}
		if (icOptions.containsKey(key_RADIUS)) {
			String radiusString = icOptions.get(key_RADIUS).getValue();
			radius = Double.parseDouble(radiusString);
		}
		if (icOptions.containsKey(key_CHANCE)) {
			String chanceString = icOptions.get(key_CHANCE).getValue();
			chance = Double.parseDouble(chanceString);
			if (chance > 1.0) {
				chance = 1.0;
				this.setLine(3, "1.0");
			}
		}
		if (radius > 0.5) {
			targets = findBlocksInRadius(target, Math.min(maxRadius, radius));
		} else {
			targets = new FishyLocationInt[]{target};
		}
	}

	@Override
	public void handleDirectInputChange(IOSignal oldS, IOSignal newS) {
		if (isRisingEdge(oldS, newS, 0)) {
			MultiLightningTask lightningTask = new MultiLightningTask(targets, chance);
			lightningTask.submit();
		} 
	}
	
	
	public FishyLocationInt[] findBlocksInRadius(FishyLocationInt location, double radius) {
		double radius2 = radius * radius;
		Set<FishyLocationInt> result = new HashSet<FishyLocationInt>(4 * (int)radius2);
		// check all locations in one quadrant of the surrounding square
		for (int x = 0; x <= (int)radius + 1; x++) {
			for (int z = 0; z <= (int)radius + 1; z++) {
				FishyVectorInt vec = new FishyVectorInt(x, 0, z);
				if (vec.lengthSquared() < radius2) {
					result.add(location.addIntVector(vec));
					// use symmetry
					result.add(location.addIntVector(new FishyVectorInt(-x, 0, -z)));
					result.add(location.addIntVector(new FishyVectorInt(-x, 0, z)));
					result.add(location.addIntVector(new FishyVectorInt(x, 0, -z)));
				}
			}
		}
		return result.toArray(new FishyLocationInt[result.size()]);
	}

}

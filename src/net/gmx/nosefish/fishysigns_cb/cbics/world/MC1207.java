package net.gmx.nosefish.fishysigns_cb.cbics.world;

import java.util.regex.Pattern;

import net.canarymod.api.world.blocks.BlockType;
import net.gmx.nosefish.fishylib.worldmath.FishyLocationInt;
import net.gmx.nosefish.fishylib.worldmath.FishyVectorInt;
import net.gmx.nosefish.fishysigns.Log;
import net.gmx.nosefish.fishysigns.annotation.FishySignIdentifier;
import net.gmx.nosefish.fishysigns.iobox.IOSignal;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;
import net.gmx.nosefish.fishysigns.signtools.PatternLib;
import net.gmx.nosefish.fishysigns.signtools.FishyParser.Rule;
import net.gmx.nosefish.fishysigns.signtools.FishyParser.Token;
import net.gmx.nosefish.fishysigns.task.FishyTask;
import net.gmx.nosefish.fishysigns.task.common.MessagePlayerTask;
import net.gmx.nosefish.fishysigns.task.common.SetBlockTask;
import net.gmx.nosefish.fishysigns_cb.cbics.CBBaseIC;

public class MC1207 extends CBBaseIC {
	@FishySignIdentifier
	public static final Pattern[] regEx = {
		null,
		Pattern.compile("\\[MC1207\\].*", Pattern.CASE_INSENSITIVE),
		null,
		null
		};
	
	protected static final String key_AXIS = "Ax";
	protected static final String key_SIGN = "SGN";
	protected static final String key_DISTANCE = "dist";
	protected static final String key_BLOCK_ID = "BID";
	protected static final String key_BLOCK_DATA = "DAT";
	protected static final String key_COLON = ":";
	protected static final String key_HOLD = "H";
	
	protected static final Pattern pattern_AXIS = 
			Pattern.compile("^[XYZ]");
	protected static final Pattern pattern_SIGN = 
			Pattern.compile("^[+\\-]");
	protected static final Pattern pattern_HOLD = 
			Pattern.compile("^H$");
	
	public static final int maxDistance = 32;
	
	protected volatile boolean hold;
	protected volatile boolean force;
	protected volatile short blockId;
	protected volatile short blockData;
	protected volatile FishyLocationInt target;
	
	public MC1207(UnloadedSign sign) {
		super(sign);
	}
	
	@Override
	public String getCode() {
		return "[MC1207]";
	}

	@Override
	public String getName() {
		return "FLEX SET";
	}

	@Override
	public String getHelpText() {
		// TODO: improve
		return "Sets a block. Maximum distance is " + maxDistance;
	}

	@Override
	public void constructOptionRules() {
		super.constructOptionRules();
		// 3rdLine:  Axis[+-]distance:id:data
		icOptionRules.get(2).add(new Rule(
				pattern_AXIS,
				new Token(key_AXIS))); // required
		icOptionRules.get(2).add(new Rule(
				pattern_SIGN,
				new Token(key_SIGN))); // required
		icOptionRules.get(2).add(new Rule(
				PatternLib.pattern_POSITIVE_INTEGER,
				new Token(key_DISTANCE))); // required
		icOptionRules.get(2).add(new Rule(
				PatternLib.pattern_COLON,
				new Token(key_COLON)));
		icOptionRules.get(2).add(new Rule(
				PatternLib.pattern_POSITIVE_INTEGER,
				new Token(key_BLOCK_ID))); // required
		icOptionRules.get(2).add(new Rule(
				PatternLib.pattern_COLON,
				new Token(key_COLON)));
		icOptionRules.get(2).add(new Rule(
				PatternLib.pattern_POSITIVE_INTEGER,
				new Token(key_BLOCK_DATA)));
		// 4th line: hold "H"
		icOptionRules.get(3).add(new Rule(
				pattern_HOLD,
				new Token(key_HOLD)));
	}
	
	@Override
	public synchronized boolean validateOnCreate(String playerName) {
		if (! super.validateOnCreate(playerName)) {
			return false;
		}
		String syntaxReminder = "Syntax: Axis+-distance:id[:datavalue]";
		if (! icOptions.containsKey(key_AXIS)) {
			String message = 
					"This IC will not work! Axis (X/Y/Z) value not found on 3rd line." + syntaxReminder;
			FishyTask sendMsg = new MessagePlayerTask(playerName, message);
			sendMsg.submit();
			return false;
		}
		if (! icOptions.containsKey(key_SIGN)) {
			String message = 
					"This IC will not work! Axis sign (+/-) not found on 3rd line." + syntaxReminder;
			FishyTask sendMsg = new MessagePlayerTask(playerName, message);
			sendMsg.submit();
			return false;
		}
		if (! icOptions.containsKey(key_DISTANCE)) {
			String message = 
					"This IC will not work! Distance value not found on 3rd line." + syntaxReminder;
			FishyTask sendMsg = new MessagePlayerTask(playerName, message);
			sendMsg.submit();
			return false;
		}
		if (! icOptions.containsKey(key_BLOCK_ID)) {
			String message = 
					"This IC will not work! No block id found on 3rd line." + syntaxReminder;
			FishyTask sendMsg = new MessagePlayerTask(playerName, message);
			sendMsg.submit();
			return false;
		}
		return true;
	}
	
	@Override
	public boolean validateOnLoad() {
        return (super.validateOnLoad() &&
            icOptions.containsKey(key_AXIS) &&
            icOptions.containsKey(key_SIGN) &&
            icOptions.containsKey(key_DISTANCE) &&
            icOptions.containsKey(key_BLOCK_ID));
	}
	
	@Override
	protected synchronized void initializeIC() {
		int distance = 1;
		// numeric conversions
		try {
			distance = Integer.parseInt(icOptions.get(key_DISTANCE).getValue());
			blockId = Short.parseShort(icOptions.get(key_BLOCK_ID).getValue());
			if (icOptions.containsKey(key_BLOCK_DATA)) {
				blockData = Short.parseShort(icOptions.get(key_BLOCK_DATA).getValue());
			}
		} catch(NumberFormatException e) {
			Log.get().warn("Some silly bugger entered bogus values on an IC sign. Location: "
			                       + this.getLocation().toString());
			this.anchorRaised(this); // detach all i/o-boxes
		}
		hold = icOptions.containsKey(key_HOLD);
		// sanity check for distance
		if (distance == 0 || distance > maxDistance) {
			distance = 1;
		}
		// get sign of distance
		if ("-".equals(icOptions.get(key_SIGN).getValue())) {
			distance = -distance;
		}
		target = findTarget(icOptions.get(key_AXIS).getValue(), distance);
	}

	
	protected FishyLocationInt findTarget(String axisString, int distance) {
		FishyVectorInt axisVector = FishyVectorInt.ZERO;
        switch (axisString) {
            case "X":
                axisVector = FishyVectorInt.UNIT_X;
                break;
            case "Y":
                axisVector = FishyVectorInt.UNIT_Y;
                break;
            case "Z":
                axisVector = FishyVectorInt.UNIT_Z;
                break;
        }
		axisVector = axisVector.scalarIntMult(distance);
		return this.location.addIntVector(BACK.toUnitIntVector()).addIntVector(axisVector);
	}
	
	@Override
	public void handleDirectInputChange(IOSignal oldS, IOSignal newS, long tickStamp) {
		updateOutput(newS, tickStamp);
		if (isRisingEdge(oldS, newS, 0)) {
			FishyTask setter = new SetBlockTask(blockId, blockData, true, target);
			setter.submit();
		} else if (isFallingEdge(oldS, newS, 0)) {
			FishyTask setter = new SetBlockTask(
					BlockType.Air.getId(), BlockType.Air.getData(), true, target);
			setter.submit();			
		}
	}
}

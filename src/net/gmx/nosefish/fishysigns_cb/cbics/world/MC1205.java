package net.gmx.nosefish.fishysigns_cb.cbics.world;

import java.util.regex.Pattern;

import net.gmx.nosefish.fishylib.worldmath.FishyDirection;
import net.gmx.nosefish.fishylib.worldmath.FishyLocationInt;
import net.gmx.nosefish.fishysigns.Log;
import net.gmx.nosefish.fishysigns.annotation.FishySignIdentifier;
import net.gmx.nosefish.fishysigns.iobox.IOSignal;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;
import net.gmx.nosefish.fishysigns.signtools.FishyParser.Rule;
import net.gmx.nosefish.fishysigns.signtools.FishyParser.Token;
import net.gmx.nosefish.fishysigns.signtools.PatternLib;
import net.gmx.nosefish.fishysigns.task.FishyTask;
import net.gmx.nosefish.fishysigns.task.common.MessagePlayerTask;
import net.gmx.nosefish.fishysigns.task.common.SetBlockTask;
import net.gmx.nosefish.fishysigns_cb.cbics.CBBaseIC;

public class MC1205 extends CBBaseIC {

	@FishySignIdentifier
	public static final Pattern[] regEx = {
		null,
		Pattern.compile("\\[MC1205\\].*", Pattern.CASE_INSENSITIVE),
		null,
		null };
	
	protected static final String key_BLOCK_ID = "BID";
	protected static final String key_BLOCK_DATA = "DAT";
	protected static final String key_FORCE = "F!";
	protected static final String key_COLON = ":";
	
	
	protected volatile short blockId;
	protected volatile short blockData;
	protected volatile boolean force;
	
	protected volatile FishyLocationInt target;
	
	public MC1205(UnloadedSign sign) {
		super(sign);
	}

	@Override
	public String getCode() {
		return "[MC1205]";
	}

	@Override
	public String getName() {
		return "SET BLOCK ABOVE";
	}

	@Override
	public String getHelpText() {
		return "Sets a block directly above the block the sign is attached to.";
	}
	
	@Override
	public void constructOptionRules() {
		super.constructOptionRules();
		// 3rdLine:  id:data
		icOptionRules[2].add(new Rule(
				PatternLib.pattern_POSITIVE_INTEGER,
				new Token(key_BLOCK_ID)));
		icOptionRules[2].add(new Rule(
				PatternLib.pattern_COLON,
				new Token(key_COLON)));
		icOptionRules[2].add(new Rule(
				PatternLib.pattern_POSITIVE_INTEGER,
				new Token(key_BLOCK_DATA)));
		// 4th line: "Force"
		icOptionRules[3].add(new Rule(
				Pattern.compile("^FORCE$", Pattern.CASE_INSENSITIVE),
				new Token(key_FORCE)));
	}

	@Override
	public synchronized boolean validateOnCreate(String playerName) {
		if (! super.validateOnCreate(playerName)) {
			return false;
		}
		if (! icOptions.containsKey(key_BLOCK_ID)) {
			String message = "This IC will not work! No block id found on 3rd line. Syntax: id[:datavalue]";
			FishyTask sendMsg = new MessagePlayerTask(playerName, message);
			sendMsg.submit();
			return false;
		}
		return true;
	}
	
	@Override
	public synchronized boolean validateOnLoad() {
		if (! super.validateOnLoad()) {
			return false;
		}
		if (! icOptions.containsKey(key_BLOCK_ID)) {
			return false;
		}
		return true;
	}
	
	@Override
	protected synchronized void initializeIC() {
		try {
			blockId = (short) Short.parseShort(icOptions.get(key_BLOCK_ID).getValue());
			if (icOptions.containsKey(key_BLOCK_DATA)) {
				blockData = (short) Short.parseShort(icOptions.get(key_BLOCK_DATA).getValue());
			}
		} catch(NumberFormatException e) {
			Log.get().logWarning("Some silly bugger entered bogus values on an IC sign. Location: "
			                       + this.getLocation().toString());
		}
		force = icOptions.containsKey(key_FORCE);
			target = findTarget();
	}

	protected FishyLocationInt findTarget() {
		return this.getLocation()
				.addIntVector(BACK.toUnitIntVector())
				.addIntVector(FishyDirection.UP.toUnitIntVector());
	}
	
	@Override
	public void handleDirectInputChange(IOSignal oldS, IOSignal newS, long tickStamp) {
			if (newS.getState(0)) {
				FishyTask setBlock = new SetBlockTask(blockId, blockData, force, target);
				setBlock.submit();
			}
	}
}

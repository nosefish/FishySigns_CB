package net.gmx.nosefish.fishysigns_cb.cbics.logic;

import java.util.regex.Pattern;

import net.gmx.nosefish.fishysigns.Log;
import net.gmx.nosefish.fishysigns.annotation.FishySignIdentifier;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;
import net.gmx.nosefish.fishysigns.signtools.FishyParser;
import net.gmx.nosefish.fishysigns.signtools.PatternLib;
import net.gmx.nosefish.fishysigns.task.common.MessagePlayerTask;
import net.gmx.nosefish.fishysigns_cb.cbics.CBBaseZISO;

public class MC0420 extends CBBaseZISO {
	@FishySignIdentifier
	public static final Pattern[] regEx = {
		null,
		Pattern.compile("\\[MC0420\\].*", Pattern.CASE_INSENSITIVE),
		null,
		null };

	protected static final String key_DIVIDER = "DIV";
	protected static final int MIN_RATE = 3;
	
	int divider = MIN_RATE;
	int counter = 0;
	
	public MC0420(UnloadedSign sign) {
		super(sign);
	}

	@Override
	public String getCode() {
		return "[MC0420]";
	}

	@Override
	public String getName() {
		return "CLOCK";
	}

	@Override
	public String getHelpText() {
		return "Clock. Specify a rate (in 2*number of server ticks) on the 3rd line. " +
				"Minimum value is 3. Higher number = slower clock";
	}
	
	@Override
	public void constructOptionRules() {
		super.constructOptionRules();
		icOptionRules[2].add(new FishyParser.Rule(
				PatternLib.pattern_POSITIVE_INTEGER,
				new FishyParser.Token(key_DIVIDER)));
	}

	@Override
	public synchronized boolean validateOnCreate(String playerName) {
		if (! super.validateOnCreate(playerName)) {
			return false;
		}
		if (! icOptions.containsKey(key_DIVIDER)) {
			String message = "This IC will not work! You must specify the rate as a number on the 3rd line.";
			MessagePlayerTask sendMsg = new MessagePlayerTask(playerName, message);
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
		if (! icOptions.containsKey(key_DIVIDER)) {
			return false;
		}
		return true;
	}
	
	@Override
	protected synchronized void initializeIC() {
		try {
			divider = Integer.parseInt(icOptions.get(key_DIVIDER).getValue());
		} catch (NumberFormatException e) {
			Log.get().logStacktrace("DIVIDER option is not an int! Check the validateOnX methods!", e);
		} catch (NullPointerException e) {
			Log.get().logStacktrace("DIVIDER option is null! Check the validateOnX methods!", e);
		}
		if (divider < MIN_RATE) {
			divider = MIN_RATE;
			this.setLine(2, Integer.toString(MIN_RATE));
			this.updateSignTextInWorld();
		}
	}

	@Override
	public void handleServerOddTick(long tick) {
		counter ++;
		if (counter >= divider) {
			counter = 0;
			this.toggleOutput(0, tick);
		}
	}
}

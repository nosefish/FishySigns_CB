package net.gmx.nosefish.fishysigns_cb.cbics.logic;

import java.util.regex.Pattern;

import net.gmx.nosefish.fishysigns.Log;
import net.gmx.nosefish.fishysigns.annotation.FishySignIdentifier;
import net.gmx.nosefish.fishysigns.iobox.IOSignal;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;
import net.gmx.nosefish.fishysigns.signtools.FishyParser;
import net.gmx.nosefish.fishysigns.signtools.PatternLib;
import net.gmx.nosefish.fishysigns.task.common.MessagePlayerTask;
import net.gmx.nosefish.fishysigns_cb.cbics.CBBaseIC;

public class MC1420 extends CBBaseIC {
	@FishySignIdentifier
	public static final Pattern[] regEx = {
		null,
		Pattern.compile("\\[MC1420\\].*", Pattern.CASE_INSENSITIVE),
		null,
		null };

	protected static String key_DIVIDER = "DIV";
	protected static String key_START_CLOCK = "CLK";
	
	private volatile int clockCount = 0;
	private volatile int divider = 1;
	
	public MC1420(UnloadedSign sign) {
		super(sign);
	}

	@Override
	public String getCode() {
		return "[MC1420]";
	}

	@Override
	public String getName() {
		return "CLOCK DIVIDER";
	}

	@Override
	public String getHelpText() {
		return "Increments a counter whenever the input changes. When the counter reaches the number" +
				"specified on the 3rd line, toggles the output and resets the counter";
	}
	
	@Override
	public void constructOptionRules() {
		super.constructOptionRules();
		icOptionRules[2].add(new FishyParser.Rule(
				PatternLib.pattern_POSITIVE_INTEGER,
				new FishyParser.Token(key_DIVIDER)));
		icOptionRules[3].add(new FishyParser.Rule(
				PatternLib.pattern_POSITIVE_INTEGER,
				new FishyParser.Token(key_START_CLOCK)));
	}

	@Override
	public synchronized boolean validateOnCreate(String playerName) {
		if (! super.validateOnCreate(playerName)) {
			return false;
		}
		if (! icOptions.containsKey(key_DIVIDER)) {
			String message = "This IC will not work! You must specify the divider as a number on the 3rd line.";
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
	public void handleDirectInputChange(IOSignal oldS, IOSignal newS, long tickStamp) {
		if (oldS.getState(0) != newS.getState(0)) {
			clockCount++;
			if (clockCount >= divider) {
				clockCount = 0;
				this.toggleOutput(0, tickStamp);
			}
			this.setLine(3, Integer.toString(clockCount));
			// TODO: update without sending the packet to players
			this.updateSignTextInWorld();
		}
	}

	@Override
	protected synchronized void initializeIC() {
		try {
			divider = Integer.parseInt(icOptions.get(key_DIVIDER).getValue());
		} catch (NumberFormatException e) {
			Log.get().trace("DIVIDER option is not an int! Check the validateOnX methods!", e);
		} catch (NullPointerException e) {
			Log.get().trace("DIVIDER option is null! Check the validateOnX methods!", e);
		}
		if (icOptions.containsKey(key_START_CLOCK)) {
			try {
				clockCount = Integer.parseInt(icOptions.get(key_START_CLOCK).getValue());
			} catch (NumberFormatException e) {
				Log.get().trace("START_CLOCK option is not an int! Check the validateOnX methods!", e);
			} catch (NullPointerException e) {
				Log.get().trace("START_CLOCK option is null!", e);
			}
			this.setLine(3, "");
			this.updateSignTextInWorld();
		}
	}
	
	@Override
	public void onUnload() {
	}
}

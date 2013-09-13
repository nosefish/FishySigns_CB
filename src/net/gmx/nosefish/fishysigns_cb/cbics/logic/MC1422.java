package net.gmx.nosefish.fishysigns_cb.cbics.logic;

import java.util.regex.Pattern;

import net.gmx.nosefish.fishysigns.Log;
import net.gmx.nosefish.fishysigns.annotation.FishySignIdentifier;
import net.gmx.nosefish.fishysigns.iobox.IOSignal;
import net.gmx.nosefish.fishysigns.iobox.ServerOddTickInputBox;
import net.gmx.nosefish.fishysigns.iobox.ServerOddTickInputBox.IServerOddTickHandler;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;
import net.gmx.nosefish.fishysigns.signtools.FishyParser.Rule;
import net.gmx.nosefish.fishysigns.signtools.FishyParser.Token;
import net.gmx.nosefish.fishysigns.signtools.PatternLib;
import net.gmx.nosefish.fishysigns.task.common.MessagePlayerTask;
import net.gmx.nosefish.fishysigns_cb.cbics.CBBaseIC;

public class MC1422 extends CBBaseIC implements IServerOddTickHandler {
	@FishySignIdentifier
	public static final Pattern[] regEx = {
		null,
		Pattern.compile("\\[MC1422\\].*", Pattern.CASE_INSENSITIVE),
		null,
		null};
	
	protected static final String key_HILO = "HL";
	protected static final String key_COLON = ":";
	protected static final String key_TICKS = "TI";
	protected static final String key_TICKS_REMAINING = "TR";
	
	protected volatile int ticks = 0;
	protected volatile int ticksRemaining = 0;
	protected volatile boolean risingEgde = true;
	
	public MC1422(UnloadedSign sign) {
		super(sign);
	}

	@Override
	public String getCode() {
		return "[MC1422]";
	}

	@Override
	public String getName() {
		return "MONOSTABLE";
	}

	@Override
	public String getHelpText() {
		return "Stays on for the specified number of 2*n ticks" +
				" when the input goes high (option H) / low (option L), then turns off.";
	}
	
	@Override
	protected synchronized void constructOptionRules() {
		super.constructOptionRules();
		//syntax of line 2 examples: "H:5", "L:15"
		icOptionRules[2].add(new Rule(
				Pattern.compile("H|L"),
				new Token(key_HILO)));
		icOptionRules[2].add(new Rule(
				Pattern.compile(":"),
				new Token(key_COLON)));
		icOptionRules[2].add(new Rule(
				PatternLib.pattern_POSITIVE_INTEGER,
				new Token(key_TICKS)));
		// remaining ticks on last line
		icOptionRules[3].add(new Rule(
				PatternLib.pattern_POSITIVE_INTEGER,
				new Token(key_TICKS_REMAINING)));
	}

	@Override
	public synchronized boolean validateOnCreate(String playerName) {
		if (! super.validateOnCreate(playerName)) {
			return false;
		}
		if (! (icOptions.containsKey(key_HILO)
				&& icOptions.containsKey(key_COLON)
				&& icOptions.containsKey(key_TICKS))) {
			String message = "This IC will not work! Parameters needed on 3rd line. " +
					"Syntax: (H|L):n, where n is a positive integer.";
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
		if (! (icOptions.containsKey(key_HILO)
				&& icOptions.containsKey(key_COLON)
				&& icOptions.containsKey(key_TICKS))) {
			return false;
		}
		return true;
	}
	
	@Override
	protected synchronized void initializeIC() {
		try {
			ticks = Integer.parseInt(icOptions.get(key_TICKS).getValue());
			if (icOptions.containsKey(key_TICKS_REMAINING)) {
				ticksRemaining = Integer.parseInt(icOptions.get(key_TICKS_REMAINING).getValue());
			}
			risingEgde = "H".equals(icOptions.get(key_HILO).getValue()); 
		} catch (NullPointerException e) {
			Log.get().logStacktrace("One of the required options did not exist", e);
		} catch (NumberFormatException e) {
			Log.get().logStacktrace("Option key_TICKS or key_TICKS_REMAINING not a valid integer", e);
		}
		ServerOddTickInputBox.createAndRegister(this);
		
	}

	@Override
	public void handleDirectInputChange(IOSignal oldS, IOSignal newS, long tickStamp) {
		if (   (  risingEgde && isRisingEdge(oldS, newS, 0))
			|| (! risingEgde && isFallingEdge(oldS, newS, 0))) {
			this.updateOutput(IOSignal.H, tickStamp);
			ticksRemaining = ticks;
		}
	}

	@Override
	public void handleServerOddTick(long tick) {
		if (ticksRemaining <= 0) {
			return;
		}
		--ticksRemaining;
		// save remaining ticks on last line
		//TODO: find out how to do this without sending a network packet
		this.setLine(3, Integer.toString(ticksRemaining));
		this.updateSignTextInWorld();
		if (ticksRemaining == 0) {
			this.updateOutput(IOSignal.L, tick);
		}
	}

}

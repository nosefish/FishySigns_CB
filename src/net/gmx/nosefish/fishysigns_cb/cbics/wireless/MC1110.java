package net.gmx.nosefish.fishysigns_cb.cbics.wireless;

import java.util.regex.Pattern;

import net.gmx.nosefish.fishysigns.annotation.FishySignIdentifier;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;
import net.gmx.nosefish.fishysigns.radio.RadioTower;
import net.gmx.nosefish.fishysigns.signtools.FishyParser;
import net.gmx.nosefish.fishysigns.signtools.PatternLib;
import net.gmx.nosefish.fishysigns.iobox.IOSignal;
import net.gmx.nosefish.fishysigns.task.FishyTask;
import net.gmx.nosefish.fishysigns.task.common.MessagePlayerTask;
import net.gmx.nosefish.fishysigns_cb.cbics.CBBaseIC;

public class MC1110 extends CBBaseIC {
	@FishySignIdentifier
	public static final Pattern[] regEx = {
		null,
		Pattern.compile("\\[MC1110\\].*", Pattern.CASE_INSENSITIVE),
		null,
		null };
	
	protected static final String key_BAND_NAME = "BN";
	
	/**
	 * The RadioTower used for broadcasting IOSignals
	 */
	public static final RadioTower<IOSignal> tower = new RadioTower<>();
	
	private volatile String bandName;

	public MC1110(UnloadedSign sign) {
		super(sign);
	}

	@Override
	public String getCode() {
		return "[MC1110]";
	}

	@Override
	public String getName() {
		return "TRANSMITTER";
	}

	@Override
	public String getHelpText() {
		return "Wireless transmitter: Transmits its input state" +
		       " on the radio band specified on the 3rd line.";
	}

	@Override
	public void constructOptionRules() {
		super.constructOptionRules();
		icOptionRules.get(2).add(
				new FishyParser.Rule(
						PatternLib.pattern_NONEMPTY_STRING,
						new FishyParser.Token(key_BAND_NAME)));
	}
	
	@Override
	public boolean validateOnCreate(String playerName) {
		if (! super.validateOnCreate(playerName)) {
			return false;
		}
		if (! icOptions.containsKey(key_BAND_NAME)) {
			String message = "This receiver will not work! You must specify a band name on the 3rd line!";
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
		if (! icOptions.containsKey(key_BAND_NAME)) {
			return false;
		}
		return true;
	}

	@Override
	protected void initializeIC() {
		bandName = icOptions.get(key_BAND_NAME).getValue();
		refresh();
	}
	
	@Override
	public void handleDirectInputChange(IOSignal oldS, IOSignal newS, long tickStamp) {
		this.updateOutput(newS, tickStamp);
		tower.broadcast(bandName, newS);
	}
}

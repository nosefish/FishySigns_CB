package net.gmx.nosefish.fishysigns_cb.cbics.wireless;

import java.util.regex.Pattern;

import net.gmx.nosefish.fishysigns.annotation.FishySignIdentifier;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;
import net.gmx.nosefish.fishysigns.iobox.IOSignal;
import net.gmx.nosefish.fishysigns.iobox.RadioAntennaInputBox;
import net.gmx.nosefish.fishysigns.iobox.RadioAntennaInputBox.IRadioInputHandler;
import net.gmx.nosefish.fishysigns.signtools.FishyParser;
import net.gmx.nosefish.fishysigns.signtools.RegExCollection;
import net.gmx.nosefish.fishysigns.task.FishyTask;
import net.gmx.nosefish.fishysigns.task.common.MessagePlayerTask;
import net.gmx.nosefish.fishysigns_cb.cbics.CBBaseIC;

public class MC1111
     extends CBBaseIC
  implements IRadioInputHandler<IOSignal>{
	
	@FishySignIdentifier
	public static final Pattern[] regEx = {
		null,
		Pattern.compile("\\[MC1111\\].*", Pattern.CASE_INSENSITIVE),
		null,
		null };
	
	protected static final String key_SELF_TRIGGERED = "ST";
	protected static final String key_BAND_NAME = "BN";
	
	// these fields are only changed once, in initialize()
	private volatile boolean autoUpdate = false;
	private volatile RadioAntennaInputBox<IOSignal> antenna;
	
	public MC1111(UnloadedSign sign) {
		super(sign);
	}
	
	@Override
	public String getCode() {
		return "[MC1111]";
	}

	@Override
	public String getName() {
		return "RECEIVER";
	}

	@Override
	public String getHelpText() {
		return "Wireless receiver: Outputs the state of the radio signal on the band specified on the 3rd line.";
	}
	
	@Override
	public void constructOptionRules() {
		super.constructOptionRules();
		icOptionRules[1].add(
				new FishyParser.Rule(
						RegExCollection.pattern_CB_SELF_TRIGGERED,
						new FishyParser.Token(key_SELF_TRIGGERED)));
		icOptionRules[2].add(
				new FishyParser.Rule(
						RegExCollection.pattern_NONEMPTY_STRING,
						new FishyParser.Token(key_BAND_NAME)));
	}


	@Override
	public synchronized boolean validateOnCreate(String playerName) {
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
	protected synchronized void initializeIC() {
		autoUpdate = icOptions.containsKey(key_SELF_TRIGGERED);
		String bandName = icOptions.get(key_BAND_NAME).getValue();
		this.initializeRadioAntenna(bandName);
		if (autoUpdate) {
			this.refresh();
		}
	}

	
	protected void initializeRadioAntenna(String bandName) {
		this.antenna = RadioAntennaInputBox.createAndRegister(
				MC1110.tower, bandName, this, IOSignal.class);
	}

	protected void refresh() {
		IOSignal signal = this.antenna.getLastBroadcast();
		if (signal == null) {
			signal = IOSignal.L;
		}
		this.outputBox.updateOutput(signal);
	}

	@Override
	public void handleDirectInputChange(IOSignal oldS, IOSignal newS) {
		if (autoUpdate) {
			// self-updating receivers ignore redstone input
			return;
		}
		// rising edge triggered. A refresh does not count as a rising edge.
		if (oldS != newS && (! oldS.getState(0) && newS.getState(0))) {
			this.updateOutputFromRadio();
		}
	}
	
	@Override
	public void handleRadioBroadcast(IOSignal signal) {
		if (! autoUpdate) {
			return;
		}
		if (signal == null) {
			outputBox.updateOutput(IOSignal.L);
		} else {
			outputBox.updateOutput(signal);
		}
	}

	private void updateOutputFromRadio() {
		IOSignal signal = antenna.getLastBroadcast();
		if (signal == null) {
			signal = IOSignal.L;
		}
		this.outputBox.updateOutput(signal);
	}


}

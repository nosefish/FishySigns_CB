package net.gmx.nosefish.fishysigns_cb.cbics.wireless;

import java.util.regex.Pattern;

import net.gmx.nosefish.fishysigns.annotation.FishySignIdentifier;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;
import net.gmx.nosefish.fishysigns.iobox.FishySignSignal;
import net.gmx.nosefish.fishysigns.iobox.RadioAntennaInputBox;
import net.gmx.nosefish.fishysigns.iobox.RadioAntennaInputBox.IRadioInputHandler;
import net.gmx.nosefish.fishysigns.task.FishyTask;
import net.gmx.nosefish.fishysigns.task.common.MessagePlayerTask;
import net.gmx.nosefish.fishysigns_cb.cbics.CBBaseIC;

public class MC1111 
     extends CBBaseIC
     implements IRadioInputHandler<FishySignSignal>{
	@FishySignIdentifier
	public static final Pattern[] regEx = {
		null,
		Pattern.compile("\\[MC1111\\][S]?", Pattern.CASE_INSENSITIVE),
		null,
		null };
	
	// these fields are only changed once, in initialize()
	private volatile boolean autoUpdate = false;
	private volatile RadioAntennaInputBox<FishySignSignal> antenna;
	
	public MC1111(UnloadedSign sign) {
		super(sign);
	}

	@Override
	public String getCode() {
		return "MC1111";
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
	public boolean shouldRefreshOnLoad() {
		return autoUpdate;
	}


	
	@Override
	public void handleDirectInputChange(FishySignSignal oldS, FishySignSignal newS) {
		if (autoUpdate) {
			// self-updating receivers ignore redstone input
			return;
		}
		// rising edge triggered. A refresh does not count as a rising edge.
		if (oldS != newS && (! oldS.getState(0) && newS.getState(0))) {
			this.updateOutputFromRadio();
		}
	}
	
	private void updateOutputFromRadio() {
		FishySignSignal signal = antenna.getLastBroadcast();
		if (signal == null) {
			signal = new FishySignSignal(false);
		}
		this.outputBox.updateOutput(signal);
	}
	
	@Override
	public void handleRadioBroadcast(FishySignSignal signal) {
		if (! autoUpdate) {
			return;
		}
		if (signal == null) {
			outputBox.updateOutput(new FishySignSignal(false));
		} else {
			outputBox.updateOutput(signal);
		}
	}
	
	@Override
	public boolean validateOnLoad() {
		return super.validateOnLoad() && (! this.getLine(2).isEmpty());
	}
	
	@Override
	public boolean validateOnCreate(String playerName) {
		if (! super.validateOnCreate(playerName)) {
			return false;
		}
		if (this.getLine(2).isEmpty()) {
			String message = "This receiver will not work! You must specify a band name on the 3rd line!";
			FishyTask sendMsg = new MessagePlayerTask(playerName, message);
			sendMsg.submit();
			return false;
		}
		return true;
	}
	
	@Override
	public void initialize() {
		autoUpdate = this.getOptionsFromSign().equalsIgnoreCase("S");
		String bandName = this.getLine(2);
		super.initialize();
		this.initializeRadioAntenna(bandName);
		this.refresh();
	}
	
	protected void initializeRadioAntenna(String bandName) {
		this.antenna = RadioAntennaInputBox.createAndRegister(
				MC1110.tower, bandName, this, FishySignSignal.class);
	}
	
	protected void refresh() {
		if (autoUpdate) {
			FishySignSignal signal = this.antenna.getLastBroadcast();
			if (signal == null) {
				signal = new FishySignSignal(false);
			}
			this.outputBox.updateOutput(signal);
		}
	}


}

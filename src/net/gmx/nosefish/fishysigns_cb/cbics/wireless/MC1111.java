package net.gmx.nosefish.fishysigns_cb.cbics.wireless;

import java.util.regex.Pattern;

import net.gmx.nosefish.fishysigns.Log;
import net.gmx.nosefish.fishysigns.activator.Activator;
import net.gmx.nosefish.fishysigns.activator.ActivatorRadio;
import net.gmx.nosefish.fishysigns.annotation.FishySignIdentifier;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;
import net.gmx.nosefish.fishysigns.signs.plumbing.FishySignSignal;
import net.gmx.nosefish.fishysigns.task.FishyTask;
import net.gmx.nosefish.fishysigns.task.common.MessagePlayerTask;
import net.gmx.nosefish.fishysigns_cb.cbics.CBBaseIC;

public class MC1111 extends CBBaseIC {
	@FishySignIdentifier
	public static final Pattern[] regEx = {
		null,
		Pattern.compile("\\[MC1111\\][S]?", Pattern.CASE_INSENSITIVE),
		null,
		null };
	
	
	// these fields are only changed once, in initialize()
	private volatile String bandName;
	private volatile boolean autoUpdate = false;
	
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
	protected void refresh() {
		super.refresh();
		if (autoUpdate) {
			FishySignSignal signal = MC1110.tower.getLastBroadcast(bandName);
			if (signal == null) {
				signal = new FishySignSignal(false);
			}
			this.outputBox.updateOutput(signal);
		}
	}
	
	@Override
	protected void onRedstoneInputChange(FishySignSignal oldS,
			FishySignSignal newS) {
		if (autoUpdate) {
			// self-updating receivers ignore redstone input
			return;
		}
		// rising edge triggered. A refresh does not count as a rising edge.
		if (oldS != null && (! oldS.getState(0) && newS.getState(0))) {
			this.updateOutputFromRadio();
		}
	}
	
	private void updateOutputFromRadio() {
		FishySignSignal signal = MC1110.tower.getLastBroadcast(bandName);
		if (signal == null) {
			signal = new FishySignSignal(false);
		}
		this.outputBox.updateOutput(signal);
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
		bandName = this.getLine(2);
		super.initialize();
		MC1110.tower.tuneIn(this.getID(), bandName);
	}
	
	@Override
	public void remove() {
		super.remove();
		MC1110.tower.stopListening(this.getID(), bandName);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void activate(Activator activator) {
		if (!(activator instanceof ActivatorRadio<?>)) {
			super.activate(activator);
			return;
		}
		ActivatorRadio<FishySignSignal> radioActivator;
		try {
			radioActivator = (ActivatorRadio<FishySignSignal>) activator;
		} catch (ClassCastException e) {
			Log.get().logStacktrace("ActivatorRadio for MC1111 did not contain a FishySignSignal.", e);
			return;
		}
		if (! bandName.equals(radioActivator.getBandName())) {
			Log.get().logWarning("MC1111: band names do not match - expected " + bandName + 
					", recieved " + radioActivator.getBandName());
		}
		FishySignSignal signal = radioActivator.getSignal();
		if ((signal != null) && this.autoUpdate) {
			this.outputBox.updateOutput(signal);
		}
	}

}

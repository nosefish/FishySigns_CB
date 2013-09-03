package net.gmx.nosefish.fishysigns_cb.cbics.wireless;

import java.util.regex.Pattern;

import net.gmx.nosefish.fishysigns.annotation.FishySignIdentifier;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;
import net.gmx.nosefish.fishysigns.radio.RadioTower;
import net.gmx.nosefish.fishysigns.signs.plumbing.FishySignSignal;
import net.gmx.nosefish.fishysigns.task.FishyTask;
import net.gmx.nosefish.fishysigns.task.common.MessagePlayerTask;
import net.gmx.nosefish.fishysigns_cb.cbics.CBBaseIC;

public class MC1110 extends CBBaseIC {
	@FishySignIdentifier
	public static final Pattern[] regEx = {
		null,
		Pattern.compile("\\[MC1110\\]", Pattern.CASE_INSENSITIVE),
		null,
		null };
	
	public static final RadioTower<FishySignSignal> tower = new RadioTower<FishySignSignal>();
	
	private volatile String bandName;

	public MC1110(UnloadedSign sign) {
		super(sign);
	}

	@Override
	public String getCode() {
		return "MC1110";
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
	public boolean shouldRefreshOnLoad() {
		return true;
	}
	
	@Override
	protected void onRedstoneInputChange(FishySignSignal oldS,	FishySignSignal newS) {
		outputBox.updateOutput(newS);
		tower.broadcast(bandName, newS);
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
			String message = "This transmitter will not work! You must specify a band name on the 3rd line!";
			FishyTask sendMsg = new MessagePlayerTask(playerName, message);
			sendMsg.submit();
			return false;
		}
		return true;
	}
	
	@Override
	public void initialize() {
		bandName = this.getLine(2);
		super.initialize();
	}
}

package net.gmx.nosefish.fishysigns_cb.cbics.sensors;


import java.util.regex.Pattern;

import net.gmx.nosefish.fishysigns.annotation.FishySignIdentifier;
import net.gmx.nosefish.fishysigns.exception.DisabledException;
import net.gmx.nosefish.fishysigns.plugin.engine.ServerTicker;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;
import net.gmx.nosefish.fishysigns.iobox.IOSignal;
import net.gmx.nosefish.fishysigns_cb.cbics.CBBaseIC;


// WTF is the use of this one?
public class MC1025 extends CBBaseIC {
	@FishySignIdentifier
	public static final Pattern[] regEx = {
		null,
		Pattern.compile("\\[MC1025\\].*", Pattern.CASE_INSENSITIVE),
		null,
		null};
	
	
	public MC1025(UnloadedSign sign) {
		super(sign);
	}

	@Override
	public String getCode() {
		return "[MC1025]";
	}

	@Override
	public String getName() {
		return "SERVER TIME";
	}

	@Override
	public String getHelpText() {
		return "Sensor: high when server time is odd, low when it's even. Rising-edge triggered.";
	}

	@Override
	protected void initializeIC() {
		// nothing to do
	}

	@Override
	public void handleDirectInputChange(IOSignal oldS, IOSignal newS) {
		if (oldS == null) {
			return;
		}
		if (! oldS.getState(0) && newS.getState(0)) {
			try {
				this.updateOutput(IOSignal.factory(
						(ServerTicker.getInstance().getTickCount() & 0x1) != 0));
			} catch (DisabledException e) {
				// I don't care. Nobody will notice anyway.
			}
		}

	}


	@Override
	protected void onUnload() {
		// nothing to do
	}

}

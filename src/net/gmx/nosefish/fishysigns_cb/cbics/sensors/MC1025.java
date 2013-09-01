package net.gmx.nosefish.fishysigns_cb.cbics.sensors;


import java.util.regex.Pattern;

import net.canarymod.Canary;
import net.gmx.nosefish.fishysigns.annotation.FishySignIdentifier;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;
import net.gmx.nosefish.fishysigns.signs.plumbing.FishySignSignal;
import net.gmx.nosefish.fishysigns.world.WorldValuePublisher;
import net.gmx.nosefish.fishysigns_cb.cbics.CBBaseIC;

// WTF is the use of this one?
public class MC1025 extends CBBaseIC {
	@FishySignIdentifier
	public static final Pattern[] regEx = {
		null,
		Pattern.compile("\\[MC1025\\]", Pattern.CASE_INSENSITIVE),
		null,
		null};
	
	
	public MC1025(UnloadedSign sign) {
		super(sign);
	}

	@Override
	public String getCode() {
		return "MC1025";
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
	public boolean shouldRefreshOnLoad() {
		return false;
	}

	@Override
	protected void onRedstoneInputChange(FishySignSignal oldS, FishySignSignal newS) {
		if (oldS == null) {
			return;
		}
		if (! oldS.getState(0) && newS.getState(0)) {
			WorldValuePublisher.publish(); // we wouldn't want this important IC to get stale values
			this.updateOutput(new FishySignSignal(Canary.getServer().getCurrentTick() % 2 > 0));
		}

	}

}

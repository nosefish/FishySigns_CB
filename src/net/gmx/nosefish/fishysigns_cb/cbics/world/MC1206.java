package net.gmx.nosefish.fishysigns_cb.cbics.world;

import java.util.regex.Pattern;

import net.gmx.nosefish.fishylib.worldmath.FishyDirection;
import net.gmx.nosefish.fishylib.worldmath.FishyLocationInt;
import net.gmx.nosefish.fishysigns.annotation.FishySignIdentifier;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;

public class MC1206 extends MC1205 {
	@FishySignIdentifier
	public static final Pattern[] regEx = {
		null,
		Pattern.compile("\\[MC1206\\].*", Pattern.CASE_INSENSITIVE),
		null,
		null };
	
	public MC1206(UnloadedSign sign) {
		super(sign);
	}
	
	@Override
	public String getCode() {
		return "[MC1206]";
	}

	@Override
	public String getName() {
		return "SET BLOCK BELOW";
	}

	@Override
	public String getHelpText() {
		return "Sets a block directly below the block the sign is attached to.";
	}
	
    @Override
	protected FishyLocationInt findTarget() {
		return this.getLocation()
				.addIntVector(BACK.toUnitIntVector())
				.addIntVector(FishyDirection.DOWN.toUnitIntVector());
	}
}

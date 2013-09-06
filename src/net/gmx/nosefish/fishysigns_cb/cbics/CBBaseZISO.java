package net.gmx.nosefish.fishysigns_cb.cbics;

import net.gmx.nosefish.fishysigns.iobox.ServerOddTickInputBox;
import net.gmx.nosefish.fishysigns.iobox.ServerOddTickInputBox.IServerOddTickHandler;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;

public abstract class CBBaseZISO
              extends CBBaseIC
           implements IServerOddTickHandler {

	public CBBaseZISO(UnloadedSign sign) {
		super(sign);
	}
	
	@Override
	public void initialize() {
		super.initialize();
		if (this.allowSelfTrigger()){
			initializeServerOddTickInputBox();
		}
	}
	
	protected void initializeServerOddTickInputBox() {
		ServerOddTickInputBox.createAndRegister(this);
	}
	
	//Not exactly elegant, but necessary to make dual-mode ICs easy to set up.
	protected boolean allowSelfTrigger() {
		return true;
	}
}

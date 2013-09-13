package net.gmx.nosefish.fishysigns_cb.cbics;

import net.gmx.nosefish.fishysigns.iobox.IOSignal;
import net.gmx.nosefish.fishysigns.iobox.ServerOddTickInputBox;
import net.gmx.nosefish.fishysigns.iobox.ServerOddTickInputBox.IServerOddTickHandler;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;

/**
 * IC that does not take any redstone input, but
 * is triggered every second tick
 * 
 * @author Stefan Steinheimer (nosefish)
 *
 */
public abstract class CBBaseZISO
              extends CBBaseIC
           implements IServerOddTickHandler {

	public CBBaseZISO(UnloadedSign sign) {
		super(sign);
	}
	
	@Override
	public void initialize() {
		super.initialize();
		initializeServerOddTickInputBox();
	}
	
	protected void initializeServerOddTickInputBox() {
		ServerOddTickInputBox.createAndRegister(this);
	}
	
	@Override
	protected void refresh() {
		// avoid NPE
		return;
	}
	
	@Override
	protected void initializeDirectInputBox() {
		// do not create a DirectInputBox
		return;
	}
	
	@Override
	public void handleDirectInputChange(IOSignal oldS, IOSignal newS, long tickStamp) {
		// this won't be called anyway, but we can't get rid of the interface
		return;
	}
	
	

}

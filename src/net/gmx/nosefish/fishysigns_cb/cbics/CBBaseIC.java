package net.gmx.nosefish.fishysigns_cb.cbics;

import net.gmx.nosefish.fishysigns.iobox.RightClickInputBox;
import net.gmx.nosefish.fishysigns.iobox.RightClickInputBox.IRightClickInputHandler;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;
import net.gmx.nosefish.fishysigns.signs.FishyICSign;
import net.gmx.nosefish.fishysigns.task.FishyTask;
import net.gmx.nosefish.fishysigns.task.common.MessagePlayerTask;
import net.gmx.nosefish.fishysigns.world.FishyLocationBlockState;

public abstract class CBBaseIC
              extends FishyICSign
           implements IRightClickInputHandler {
	private boolean newlyCreated = false;

	public CBBaseIC(UnloadedSign sign) {
		super(sign);
	}

	/**
	 * Gets the IC-code.
	 * The code is the part between the square brackets on
	 * the second line of the sign, usually of the form [MC####].
	 * 
	 * @return the code string of this sign
	 */
	public abstract String getCode();
	
	/**
	 * Gets the name of this IC that appears on the first line
	 * , e.g. <i>NOT</i>. Must not be longer than 15 characters.
	 * 
	 * @return the name
	 */
	public abstract String getName();
	
	/**
	 * Gets the help text. Every IC must have one.
	 * 
	 * Example:<br>
	 * <i>Logic gate: NOT. Inverts the input signal.</i>
	 * 
	 * @return the help text
	 */
	public abstract String getHelpText();
	
	/**
	 * Determines if <code>refresh</code> is called when
	 * the IC is loaded or first created.
	 * 
	 * <b>Caution:</b> <code>onRedstoneInputChange</code> will be called
	 * with a <code>null</code> oldSignal!
	 * 
	 * @return <code>true</code> if this IC should be updated when loaded
	 */
	public abstract boolean shouldRefreshOnLoad();

	
	@Override
	public boolean validateOnCreate(String playerName) {
		newlyCreated = true;
		return super.validateOnCreate(playerName);
	}
	
	@Override
	public void initialize() {
		super.initialize();
		if (newlyCreated) {
			this.rewriteTextOnCreation();
			this.updateSignTextInWorld();
		}
		if (this.shouldRefreshOnLoad()) {
			inputBox.refreshHandler();
		}
		initializeOnSignRightClickBox();
	}
	
	protected void initializeOnSignRightClickBox() {
		RightClickInputBox.createAndRegister(this.getLocation(), this);
	}
	
	protected synchronized String getOptionsFromSign() {
		String options = "";
		int optionsStartIndex = this.text[1].indexOf("]") + 1;
		if (0 < optionsStartIndex) {
			options = this.text[1].substring(optionsStartIndex);
		}
		return options;
	}
	
	@Override
	public void handleRightClick(String playerName, FishyLocationBlockState block) {
		FishyTask sendMsg = new MessagePlayerTask(playerName, this.getHelpText());
		sendMsg.submit();
	}
	
	/**
	 * Sets line 0 to the name (from <code>getName</code>) and converts
	 * the [mcXXXX] code on line 1 to upper case. Called in <code>initialize</code>
	 * for newly created signs.
	 */
	protected synchronized void rewriteTextOnCreation() {
		this.text[0] = getName();
		int mcCodeStartIndex = text[1].indexOf("[");
		int mcCodeEndIndex = text[1].indexOf("]");
		if ((0 <= mcCodeStartIndex) && (mcCodeStartIndex < mcCodeEndIndex)) {
			String mcCode = text[1].substring(mcCodeStartIndex, mcCodeEndIndex + 1);
			text[1] = text[1].replace(mcCode, mcCode.toUpperCase());
		}
	}
	
}

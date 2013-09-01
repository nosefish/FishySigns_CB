package net.gmx.nosefish.fishysigns_cb.cbics;

import net.gmx.nosefish.fishysigns.activator.Activator;
import net.gmx.nosefish.fishysigns.activator.ActivatorPlayerRightClick;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;
import net.gmx.nosefish.fishysigns.signs.FishyICSign;
import net.gmx.nosefish.fishysigns.task.FishyTask;
import net.gmx.nosefish.fishysigns.task.common.MessagePlayerTask;
import net.gmx.nosefish.fishysigns.watcher.PlayerRightClickWatcher;

public abstract class CBBaseIC extends FishyICSign {
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
	 * Gets the name of this IC, e.g. <i>NOT</i>.
	 * 
	 * @return the name
	 */
	public abstract String getName();
	
	/**
	 * Gets the help text. Every IC must have one.
	 * 
	 * Example:<br>
	 * <i>Logic function: O = NOT(I). Inverts the input signal.</i>
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
			this.refresh();
		}
		PlayerRightClickWatcher.getInstance().register(this, this.location);
	}
	
	@Override
	public void remove() {
		// FishyIC will handle the removal from PlayerRightClickWatcher
		super.remove();
	}
	
	@Override
	public void activate(Activator activator) {
		super.activate(activator);
		if (activator instanceof ActivatorPlayerRightClick) {
			ActivatorPlayerRightClick aprc = (ActivatorPlayerRightClick) activator;
			if (aprc.getBlockState().getLocation().equalsLocation(this.location)) {
				FishyTask sendMsg = new MessagePlayerTask(aprc.getPlayerName(), this.getHelpText());
				sendMsg.submit();
			}
		}
	}
	
	/**
	 * Calls <code>onRedstoneInputChange</code> with the current input
	 * signal.
	 * 
	 * <b>Caution:</b> <code>onRedstoneInputChange</code> will be called
	 * with a <code>null</code> oldSignal!
	 */
	protected void refresh() {
		this.onRedstoneInputChange(null, inputBox.getSignal());
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

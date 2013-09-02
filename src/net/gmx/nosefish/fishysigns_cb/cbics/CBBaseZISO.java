package net.gmx.nosefish.fishysigns_cb.cbics;

import net.gmx.nosefish.fishysigns.activator.Activator;
import net.gmx.nosefish.fishysigns.activator.ActivatorServerTick;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;
import net.gmx.nosefish.fishysigns.watcher.ServerOddTickWatcher;

public abstract class CBBaseZISO extends CBBaseIC {

	public CBBaseZISO(UnloadedSign sign) {
		super(sign);
	}
	
	protected abstract void onSelfTriggered(int tickNumber);

	@Override
	public void initialize() {
		super.initialize();
		if (this.allowSelfTrigger()){
			ServerOddTickWatcher.getInstance().register(this.getID());
		}
	}
	
	public void remove() {
		super.remove();
		if (this.allowSelfTrigger()){
			ServerOddTickWatcher.getInstance().remove(this.getID());
		}
	}
	
	
	//Not exactly elegant, but necessary to make dual-mode ICs easy to set up.
	protected boolean allowSelfTrigger() {
		return true;
	}
	
	@Override
	public void activate(Activator activator) {
		if (! (activator instanceof ActivatorServerTick)) {
			super.activate(activator);
			return;
		}
		if (allowSelfTrigger()) {
			ActivatorServerTick ast = (ActivatorServerTick) activator;
			int tick = ast.getTick();
			if ((tick & 0x1) == 1) {
				this.onSelfTriggered(tick);
			}
		}
	}
}

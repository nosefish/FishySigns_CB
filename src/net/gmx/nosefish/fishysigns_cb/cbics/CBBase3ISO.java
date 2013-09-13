package net.gmx.nosefish.fishysigns_cb.cbics;

import net.gmx.nosefish.fishylib.worldmath.FishyLocationInt;
import net.gmx.nosefish.fishysigns.iobox.DirectInputBox;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;

public abstract class CBBase3ISO extends CBBaseIC {

	public CBBase3ISO(UnloadedSign sign) {
		super(sign);
	}

	@Override
	protected void initializeDirectInputBox() {
		FishyLocationInt[] inputLocations = this.getInputLocations();
		if (inputLocations.length != 3) {
			throw new InputLocationExeption("Invalid number of input locations: expected 3, but has"
					+ inputLocations.length);
		}
		inputBox = DirectInputBox.createAndRegister(location, 3, 3, this);
		inputBox.setAllInputPins(inputLocations);
		inputBox.wireOneToOne();
		inputBox.finishInit();
	}

	public static class InputLocationExeption extends RuntimeException {
		private static final long serialVersionUID = -2320891307088964439L;

		public InputLocationExeption() {
			super();
		}

		public InputLocationExeption(String message, Throwable cause) {
			super(message, cause);
		}

		public InputLocationExeption(String message) {
			super(message);
		}

		public InputLocationExeption(Throwable cause) {
			super(cause);
		}
		
		
	}
}

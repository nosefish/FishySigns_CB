package net.gmx.nosefish.fishysigns_cb.cbics;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import net.gmx.nosefish.fishysigns.anchor.IAnchorable;
import net.gmx.nosefish.fishysigns.iobox.IOSignal;
import net.gmx.nosefish.fishysigns.iobox.RightClickInputBox;
import net.gmx.nosefish.fishysigns.iobox.RightClickInputBox.IRightClickInputHandler;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;
import net.gmx.nosefish.fishysigns.signs.FishyICSign;
import net.gmx.nosefish.fishysigns.signtools.FishyParser;
import net.gmx.nosefish.fishysigns.signtools.FishyParser.Rule;
import net.gmx.nosefish.fishysigns.signtools.FishyParser.Token;
import net.gmx.nosefish.fishysigns.signtools.StringTools;
import net.gmx.nosefish.fishysigns.task.FishyTask;
import net.gmx.nosefish.fishysigns.task.common.MessagePlayerTask;
import net.gmx.nosefish.fishysigns.world.FishyLocationBlockState;

/**
 * Base class for all ICs. Adds the ability to
 * send help text to players right-clicking the sign
 * and defines methods that all ICs must implement.
 * 
 * @author Stefan Steinheimer (nosefish)
 *
 */
public abstract class CBBaseIC
              extends FishyICSign {

	/**
	 * Access must be synchronized on "this"!
	 * Values for lines without defined rules are empty.
	 * Will be null after <code>initialize</code> to save memory!
	 */
	@SuppressWarnings("unchecked")
	protected volatile List<Rule>[] icOptionRules = new List[] {
		new ArrayList<Rule>(1),
		new ArrayList<Rule>(1),
		new ArrayList<Rule>(1),
		new ArrayList<Rule>(1)
		};
	
	public static final long DEFAULT_DELAY = 2L;
	
	/**
	 * Access must be synchronized on "this"!
	 * Will be null after <code>initialize</code> to save memory!
	 * It is recommended to process the options in 
	 * <code>initializeIC</code> and store references to
	 * the values that are needed later elsewhere.
	 */
	protected Map<String, Token> icOptions = new LinkedHashMap<String, Token>(4);
	
	protected boolean signTextChanged = false;
	
	public CBBaseIC(UnloadedSign sign) {
		super(sign);
		this.constructOptionRules();
	}

	/**
	 * Gets the IC-code.
	 * The code is the part between the square brackets on
	 * the second line of the sign, usually of the form [MC####].
	 * 
	 * Return value must be defined at construction time.
	 * 
	 * @return the code string of this sign
	 */
	public abstract String getCode();
	
	/**
	 * Gets the name of this IC that appears on the first line,
	 * e.g. <i>NOT</i>. Must not be longer than 15 characters.
	 * 
	 * @return the name
	 */
	public abstract String getName();
	
	/**
	 * Gets the help text.
	 * 
	 * Example:<br>
	 * <i>Logic gate: NOT. Inverts the input signal.</i>
	 * 
	 * @return the help text
	 */
	public abstract String getHelpText();
	
	/**
	 * Put all IC-specific initialization here. I will be called
	 * from the <code>initialize</code> method. Overriding
	 * <code>initialize</code> itself is only recommended for
	 * abstract base classes in order to ensure a correct 
	 * initialization sequence.
	 */
	protected abstract void initializeIC();

	/**
	 * Sets the rules for sign options. In CBBaseIC,
	 * two rules are added:<br>
	 * Line 0 must match getName() (Option IC_NAME) and
	 * line 1 must start with the return value 
	 * of the IC's <code>getCode</code> method (Option IC_CODE).
	 * 
	 * Called in the constructor.
	 */
	protected void constructOptionRules() {
		// name on the first line
		icOptionRules[0].add(
				new Rule(Pattern.compile("^" + Pattern.quote(getName()) + "$"),
						new Token("IC_NAME")));
		// second line starts with IC code
		icOptionRules[1].add(
				new Rule(Pattern.compile("^" + Pattern.quote(getCode())),
						new Token("IC_CODE")));
	}
	
	

	@Override
	public synchronized boolean validateOnCreate(String playerName) {
		if (! super.validateOnCreate(playerName)) {
			return false;
		}
		this.capitalizeICCode();
		this.setLine0ToName();
		signTextChanged = true;
		this.readOptions();
		if (icOptions.containsKey(FishyParser.key_NO_MATCH)) {
			String message = new StringBuilder()
			.append("This ")
			.append(getCode())
			.append("-IC will not work. Unknown sign options:\n")
			.append(icOptions.get(FishyParser.key_NO_MATCH).getValue())
			.toString();
			MessagePlayerTask sendMsg = new MessagePlayerTask(playerName, message);
			sendMsg.submit();
			return false;
		}
		return true;
	}
	
	

	@Override
	public synchronized boolean validateOnLoad() {
		if (! super.validateOnLoad()) {
			return false;
		}
		this.readOptions();
		if (icOptions.containsKey(FishyParser.key_NO_MATCH)) {
			return false;
		}
		return true;
	}

	/**
	 * Sets the first line of the sign to the
	 * name.
	 * 
	 * Does not update the sign in the world.
	 * Call <code>updateSignTextInWorld</code>
	 * when you're done making changes.
	 */
	protected void setLine0ToName() {
		this.setLine(0, getName());
	}

	/**
	 * Converts the IC's code to upper case.
	 * 
	 * Does not update the sign in the world.
	 * Call <code>updateSignTextInWorld</code>
	 * when you're done making changes.
	 */
	protected void capitalizeICCode() {
		Pattern pattern = Pattern.compile(Pattern.quote(this.getCode()), Pattern.CASE_INSENSITIVE);
		this.setLine(1, StringTools.patternInStringToUpperCase(pattern,this.getLine(1)));
	}

	/**
	 * Creates the set of options from the sign text
	 */
	protected synchronized void readOptions() {
		for (int line = 0; line < 4; ++line) {
			Map<String, Token> found =
					FishyParser.findTokens(this.getLine(line), icOptionRules[line]);
			if (found.containsKey(FishyParser.key_NO_MATCH)) {
				Token foundNoMatch = found.get(FishyParser.key_NO_MATCH);
				Token prevNoMatch = icOptions.get(FishyParser.key_NO_MATCH);
				String prevNoMatchString = "";
				if (prevNoMatch != null) {
					prevNoMatchString = prevNoMatch.getValue();
				}
				String noMatchString = new StringBuilder()
				.append(prevNoMatchString)
				.append(("".equals(prevNoMatchString)?"":" / "))
				.append("Line ")
				.append(line)
				.append(": '")
				.append(found.get(FishyParser.key_NO_MATCH).getValue())
				.append("'")
				.toString();
				foundNoMatch.setValue(noMatchString);
			}
			icOptions.putAll(found);
		}
	}
	

	/**
	 * Initializes the IC after creation.
	 * This method should only be overridden by
	 * abstract base classes. Concrete ICs should
	 * perform their initialization in <code>initializeIC</code>
	 * to ensure a correct sequence of operations.
	 */
	@Override
	public void initialize() {
		super.initialize();
		initializeOnSignRightClickBox();
		initializeIC();
		if (signTextChanged) {
			this.updateSignTextInWorld();
			signTextChanged = false;
		}
		synchronized(this) {
			icOptionRules = null;
			icOptions = null;
		}
	}

	/**
	 * Instantiates and initializes the <code>RightClickInputBox</code>
	 * that handles the display of help text when a player right-clicks
	 * an IC sign.
	 */
	protected void initializeOnSignRightClickBox() {
		RightClickInputBox.createAndRegister(this.getLocation(), new SendHelpOnRighClickHandler());
	}

	/**
	 * Calls the <code>refreshHandler</code> method of the <code>inputBox</code>.
 	 * When called, the handleDirectInput method will be called
	 * with the current input signal for both <code>oldSignal</code>
	 * and <code>newSignal</code> (same instance of FishySignSignal).
	 */
	protected void refresh() {
		inputBox.refreshHandler();
	}
	
	protected void updateOutput(IOSignal signal, long inputEventTickStamp) {
		long targetTick = inputEventTickStamp + DEFAULT_DELAY;
		this.getOutputBox().updateOutputOnTick(signal, targetTick);
	}
	
	protected void updateOutputNow(IOSignal signal) {
		this.getOutputBox().updateOutputNow(signal);
	}
	
	protected void toggleOutput(int pin, long inputEventTickStamp) {
		long targetTick = inputEventTickStamp + DEFAULT_DELAY;
		this.getOutputBox().toggleOutputOnTick(pin, targetTick);
		
	}
	
	protected void toggleOutputNow(int pin) {
		this.getOutputBox().toggleOutputNow(pin);
	}
	
	/**
	 * Handler to display the IC's help text to the
	 * right-clicking player.
	 * 
	 * @author Stefan Steinheimer (nosefish)
	 *
	 */
	protected class SendHelpOnRighClickHandler implements IRightClickInputHandler { 
		/**
		 * Sends a message with help text to the player.
		 */
		@Override
		public void handleRightClick(String playerName, FishyLocationBlockState block) {
			FishyTask sendMsg = new MessagePlayerTask(playerName, CBBaseIC.this.getHelpText());
			sendMsg.submit();
		}

		/**
		 * Anchors the RightClickInputBox to the IC.
		 */
		@Override
		public void anchor(IAnchorable toAnchor) {
			CBBaseIC.this.anchor(toAnchor);			
		}
	}
}

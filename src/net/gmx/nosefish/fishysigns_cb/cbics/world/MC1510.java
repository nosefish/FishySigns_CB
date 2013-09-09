package net.gmx.nosefish.fishysigns_cb.cbics.world;

import java.util.regex.Pattern;

import net.canarymod.chat.TextFormat;
import net.gmx.nosefish.fishysigns.annotation.FishySignIdentifier;
import net.gmx.nosefish.fishysigns.iobox.FishySignSignal;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;
import net.gmx.nosefish.fishysigns.signtools.FishyParser;
import net.gmx.nosefish.fishysigns.signtools.RegExCollection;
import net.gmx.nosefish.fishysigns.signtools.StringTools;
import net.gmx.nosefish.fishysigns.task.FishyTask;
import net.gmx.nosefish.fishysigns.task.common.MessageAllPlayersTask;
import net.gmx.nosefish.fishysigns.task.common.MessagePlayerTask;
import net.gmx.nosefish.fishysigns_cb.cbics.CBBaseIC;

public class MC1510 extends CBBaseIC {
	@FishySignIdentifier
	public static final Pattern[] regEx = {
		null,
		Pattern.compile("\\[MC1510\\].*", Pattern.CASE_INSENSITIVE),
		null,
		null };
	
	protected final String str_BROADCAST = "BROADCAST";
	
	protected final String key_PLAYER = "PL";
	protected final String key_MESSAGE = "MSG";
	
	protected volatile String messageToSend = "";
	protected volatile String playerName = "";

	public MC1510(UnloadedSign sign) {
		super(sign);
	}

	@Override
	public String getCode() {
		return "[MC1510]";
	}

	@Override
	public String getName() {
		return "MESSAGE SENDER";
	}

	@Override
	public String getHelpText() {
		return "Sends the text on the 4th line to the player specified on the 2nd line." +
				" BROADCAST instead of a name will send it to all players";
	}

	@Override
	public void constructOptionRules() {
		super.constructOptionRules();
		icOptionRules[2].add(new FishyParser.Rule(
				RegExCollection.pattern_NONEMPTY_STRING,
				new FishyParser.Token(key_PLAYER)));
		icOptionRules[3].add(new FishyParser.Rule(
				RegExCollection.pattern_NONEMPTY_STRING,
				new FishyParser.Token(key_MESSAGE)));
	}
	
	@Override
	public synchronized boolean validateOnCreate(String playerName) {
		if (! super.validateOnCreate(playerName)) {
			return false;
		}
		if (! icOptions.containsKey(key_PLAYER)) {
			String message = "This IC will not work! " +
					"You must specify a player name ore BROADCAST on the 3rd line.";
			FishyTask sendMsg = new MessagePlayerTask(playerName, message);
			sendMsg.submit();
			return false;
		}
		if (! icOptions.containsKey(key_MESSAGE)) {
			String message = "This IC will not work! " +
					"You must specify a message on the 4rd line.";
			FishyTask sendMsg = new MessagePlayerTask(playerName, message);
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
		if (! icOptions.containsKey(key_PLAYER)) {
			return false;
		}
		if (! icOptions.containsKey(key_MESSAGE)) {
			return false;
		}
		return true;
	}
	
	
	@Override
	protected synchronized void initializeIC() {
		playerName = icOptions.get(key_PLAYER).getValue();
		messageToSend = icOptions.get(key_MESSAGE).getValue();
		messageToSend = StringTools.replaceAmpersandWithParagraph(messageToSend);
	}

	@Override
	public void handleDirectInputChange(FishySignSignal oldS, FishySignSignal newS) {
		if (! oldS.getState(0) && newS.getState(0) && ! messageToSend.isEmpty()) {
			FishyTask sendMsg;
			if (str_BROADCAST.equals(playerName) ) {
				sendMsg = new MessageAllPlayersTask(messageToSend, TextFormat.WHITE);
			} else {
				sendMsg = new MessagePlayerTask(playerName, messageToSend, TextFormat.WHITE);
			}
			sendMsg.submit();
		}
	}

}

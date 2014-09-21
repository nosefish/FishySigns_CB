package net.gmx.nosefish.fishysigns_cb.cbmech;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

import net.canarymod.Canary;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.world.World;
import net.canarymod.api.world.blocks.Block;
import net.canarymod.api.world.blocks.Sign;
import net.canarymod.api.world.blocks.TileEntity;
import net.canarymod.chat.Colors;
import net.gmx.nosefish.fishylib.blocks.BlockInfo;
import net.gmx.nosefish.fishylib.datastructures.ConcurrentMapWithSet;
import net.gmx.nosefish.fishylib.worldmath.FishyLocationInt;
import net.gmx.nosefish.fishysigns.annotation.FishySignIdentifier;
import net.gmx.nosefish.fishysigns.iobox.RightClickInputBox;
import net.gmx.nosefish.fishysigns.iobox.RightClickInputBox.IRightClickInputHandler;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;
import net.gmx.nosefish.fishysigns.signs.FishySign;
import net.gmx.nosefish.fishysigns.signtools.StringTools;
import net.gmx.nosefish.fishysigns.task.FishyTask;
import net.gmx.nosefish.fishysigns.task.common.MessagePlayerTask;
import net.gmx.nosefish.fishysigns.world.FishyLocationBlockState;

public class Lift extends FishySign implements IRightClickInputHandler {
	@FishySignIdentifier
	public static final Pattern[] regEx = {
		null,
		Pattern.compile("\\[Lift( (Up|Down))?\\]", Pattern.CASE_INSENSITIVE),
		null,
		null};

	// map: (world, x, 0, z) - y values with lift signs in that column
	protected static ConcurrentMapWithSet<FishyLocationInt, Integer>
	                 liftColumns = new ConcurrentMapWithSet<>();

	private volatile boolean isNewSign = false;
	private final Lift.Type liftType;

	public Lift(UnloadedSign sign) {
		super(sign);
		this.liftType = Lift.Type.getLiftType(sign.getText()[1]);
	}

	public Lift.Type getLiftType() {
		return this.liftType;
	}

	public Integer getLinkedLiftY() {
		Integer myY = this.location.getIntY();
		Integer targetY;
		SortedSet<Integer> liftsInColumn = new TreeSet<>(
            Lift.liftColumns.get(this.getLiftColumn()));
		switch (this.liftType) {
		case UP:
			// lowest above
			SortedSet<Integer> above = liftsInColumn.tailSet(myY + 1);
			targetY = above.isEmpty() ? null : above.first();
			break;
		case DOWN:
			// highest below
			SortedSet<Integer> below = liftsInColumn.headSet(myY);
			targetY = below.isEmpty() ? null : below.last();
			break;
		default:
			targetY = null;
			break;
		}
		return targetY;
	}

	@Override
	public boolean validateOnCreate(String playerName) {
		String message;
		boolean isValid;
		if (this.isWallSign()) {
			message = "Lift created.";
			isValid = true;
		} else {
			message = "This lift will not work! Lifts must be attached to a wall!";
			isValid = false;
		}
		FishyTask sendMsg = new MessagePlayerTask(playerName, message);
		sendMsg.submit();
		isNewSign= true;
		return isValid;
	}

	@Override
	public boolean validateOnLoad() {
		return this.isWallSign();
	}

    @Override
    public void initialize() {
        RightClickInputBox.createAndRegister(this.getLocation(), this);
        Lift.liftColumns.put(this.getLiftColumn(), this.location.getIntY());
        if (isNewSign) {
            this.setLine(1, StringTools.patternInStringToUpperCase(regEx[1], this.getLine(1)));
            this.updateSignTextInWorld();
        }
    }

	@Override
	public void onUnload() {
		Lift.liftColumns.removeValue(this.location.getIntY());
	}

	protected FishyLocationInt getLiftColumn() {
		return new FishyLocationInt(this.location.getWorld(),
				                    this.location.getIntX(),
				                    0,
				                    this.location.getIntZ());
	}

	public static enum Type{
		UP(Pattern.compile("\\[Lift Up\\]", Pattern.CASE_INSENSITIVE)),
        DOWN(Pattern.compile("\\[Lift Down\\]", Pattern.CASE_INSENSITIVE)),
        NONE(Pattern.compile("\\[Lift\\]", Pattern.CASE_INSENSITIVE));
		private final Pattern regEx;

		private Type(Pattern p) {
			this.regEx = p;
		}

		public static Type getLiftType(String str){
			for (Type t : Type.values()) {
				if (t.regEx.matcher(str).matches()) {
					return t;
				}
			}
			throw new IllegalArgumentException(
                "String does not specify a lift: " + str);
		}
	};

	/**
	 * Does the dirty work for the [Lift] sign in the server thread.
	 *
	 * @author Stefan Steinheimer (nosefish)
	 *
	 */
	public static class LiftPlayerTask extends FishyTask {
		private static final int OBSTRUCTED = -1;
		private static final int NO_FLOOR = -2;
		private static final String NO_SIGN_MESSAGE =
            "The linked lift sign has mysteriously disappeared.";
		private final String playerName;
		private final FishyLocationInt targetSignLocation;

		public LiftPlayerTask(String playerName,
                              FishyLocationInt targetSignLocation) {
			this.playerName = playerName;
			this.targetSignLocation = targetSignLocation;
		}

		@Override
		public void doStuff() {
			Player player = Canary.getServer().getPlayer(playerName);
			if (player == null) {
				return;
			}
			String message = "";
			FishyLocationInt playerLoc = new FishyLocationInt(
                player.getLocation());
			int safeY = findSafeY(playerLoc.getIntX(),
                                  targetSignLocation.getIntY(),
                                  playerLoc.getIntZ());
			String direction = null;
			if (safeY > playerLoc.getIntY()) {
				direction = "up";
			} else if (safeY < playerLoc.getIntY()) {
				direction = "down";
			}
			switch (safeY) {
			case OBSTRUCTED:
				message = "You would end up in a wall!";
				break;
			case NO_FLOOR:
				message = "You would have no ground to stand on!";
				break;
			default:
				message = getFloorMessage();
				if (message.equals(NO_SIGN_MESSAGE)) {
					break;
				}
				player.teleportTo(player.getX(),
                                  (double)safeY,
                                  player.getZ(),
                                  player.getPitch(),
                                  player.getRotation(),
                                  player.getWorld());
				break;
			}
			if (message.isEmpty()) {
				if (direction == null) {
					message = "The lift tried to move you, but you ended up in the same place.";
				} else {
					message = "The lift moved you " + direction + " a floor.";
				}
			}
			player.message(Colors.ORANGE + message);
		}

		private String getFloorMessage() {
			String message = "";
			World world = this.targetSignLocation.getWorld().getWorldIfLoaded();
			// If the world is null, I want an exception. It shouldn't be.
			TileEntity tileEntity = world.getTileEntityAt(
					                    targetSignLocation.getIntX(),
					                    targetSignLocation.getIntY(),
					                    targetSignLocation.getIntZ());
			if (tileEntity == null || !(tileEntity instanceof Sign)) {
				message = NO_SIGN_MESSAGE;
			} else {
				Sign sign = (Sign)tileEntity;
				String floorMessage = sign.getTextOnLine(0);
				if (!floorMessage.isEmpty()) {
					message = "Floor: " + floorMessage;
				}
			}
			return message;
		}

		private int findSafeY(int x, int y, int z) {
			World world = targetSignLocation.getWorld().getWorldIfLoaded();
			int freeBlocks = 0;
			Block block;
			// we need 2 free blocks above each other,
			// and a solid block to stand on,
			// no more than 5 below the sign.
			// All blocks between the sign Y and the
			// solid block must be non-solid;
			// the one above the sign may be solid.
			block = world.getBlockAt(x, y + 1, z);
			if (block == null || BlockInfo.canPlayerPassThrough(block.getTypeId())) {
				freeBlocks++;
			}
			for (int tpTargetY = y; y - tpTargetY <= 5; tpTargetY--) {
				block = world.getBlockAt(x, tpTargetY, z);
				if (block == null || BlockInfo.canPlayerPassThrough(block.getTypeId())) {
					freeBlocks++;
				} else {
					// solid block
					if (freeBlocks >= 2) {
						// Our y is now in a solid block. We need the one above.
						return tpTargetY + 1;
					} else {
						return OBSTRUCTED;
					}
				}
			}
			return NO_FLOOR;
		}
	}

	@Override
	public void handleRightClick(String playerName, FishyLocationBlockState block) {
		String message = "";
		if (this.getLiftType().equals(Lift.Type.NONE)) {
			message = "This is the end-point of a one-way lift.";
		} else {
			// "Lift Up" or "Lift Down"
			Integer targetY = this.getLinkedLiftY();
			if (targetY == null) {
				message = "This lift is not linked to another [Lift] sign.";
			} else {
				// move the player
				FishyLocationInt targetSignLocation = 
                    new FishyLocationInt(
                        this.location.getWorld(),
                        this.location.getIntX(),
                        targetY,
                        this.location.getIntZ()
                    );
				FishyTask liftPlayer = new LiftPlayerTask(playerName, 
                                                          targetSignLocation);
				liftPlayer.submit();
			}
		}
		if (! message.isEmpty()) {
			FishyTask sendMsg = new MessagePlayerTask(playerName, message);
			sendMsg.submit();
		}
	}
}

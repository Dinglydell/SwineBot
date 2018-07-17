package dinglydell.swinebot;

import java.util.UUID;

import net.minecraft.server.v1_11_R1.DamageSource;
import net.minecraft.server.v1_11_R1.Entity;
import net.minecraft.server.v1_11_R1.EntityCreature;
import net.minecraft.server.v1_11_R1.EntityHuman;
import net.minecraft.server.v1_11_R1.EntityPlayer;
import net.minecraft.server.v1_11_R1.EnumMoveType;
import net.minecraft.server.v1_11_R1.MathHelper;
import net.minecraft.server.v1_11_R1.Packet;
import net.minecraft.server.v1_11_R1.PacketPlayOutAnimation;
import net.minecraft.server.v1_11_R1.PacketPlayOutEntity.PacketPlayOutEntityLook;
import net.minecraft.server.v1_11_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_11_R1.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_11_R1.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_11_R1.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_11_R1.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_11_R1.PathfinderGoalFloat;
import net.minecraft.server.v1_11_R1.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_11_R1.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_11_R1.PathfinderGoalMeleeAttack;
import net.minecraft.server.v1_11_R1.PathfinderGoalMoveTowardsRestriction;
import net.minecraft.server.v1_11_R1.PathfinderGoalMoveTowardsTarget;
import net.minecraft.server.v1_11_R1.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_11_R1.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_11_R1.PlayerConnection;
import net.minecraft.server.v1_11_R1.PlayerInteractManager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_11_R1.CraftServer;
import org.bukkit.craftbukkit.v1_11_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.mojang.authlib.GameProfile;

import dinglydell.swinebot.ai.PathfinderGoalNearestBot;
import dinglydell.swinebot.entity.EntityPlayerDummy;

public class Bot extends EntityCreature {

	//protected WorldServer world;
	//protected EntityPlayer entity;
	//public PathfinderGoalSelector goalSelector;
	//public PathfinderGoalSelector targetSelector;
	protected EntityPlayer player;
	protected EntityHuman human;

	public Bot(Location location, String npcName) {
		super(((CraftWorld) location.getWorld()).getHandle());
		player = new EntityPlayerDummy(this,
				((CraftServer) Bukkit.getServer()).getServer(),
				((CraftWorld) location.getWorld()).getHandle(),
				new GameProfile(UUID.randomUUID(), npcName),
				new PlayerInteractManager(
						((CraftWorld) location.getWorld()).getHandle()));
		//player.playerConnection = new PlayerConnection((CraftServer) Bukkit.getServer()).getServer(), null, player);
		//MinecraftServer nmsServer = ;
		//world = ;
		// GameProfile gameProfile = ;
		//changeSkin(gameProfile);

		setLocation(location.getX(),
				location.getY(),
				location.getZ(),
				location.getYaw(),
				location.getPitch());
		setToMyPosition();
		for (EntityHuman h : world.players) {
			Player p = (Player) h.getBukkitEntity();
			showToPlayer(p);
		}

	}

	public void start() {
		//new Pathfin
		this.goalSelector.a(0, new PathfinderGoalFloat(this));
		//this.goalSelector.a(2, new PathfinderGoalZombieAttack(this, 1.0D, false));
		this.goalSelector.a(2, new PathfinderGoalMeleeAttack(this, 0.5d, true));
		this.goalSelector.a(3, new PathfinderGoalMoveTowardsTarget(this, 0.5d,
				999f));
		this.goalSelector.a(5, new PathfinderGoalMoveTowardsRestriction(this,
				1.0D));
		//this.goalSelector.a(7, new PathfinderGoalRandomStrollLand(this, 1.0D));
		this.goalSelector.a(8, new PathfinderGoalLookAtPlayer(this,
				EntityHuman.class, 8.0F));
		this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
		targetSelector.a(0, new PathfinderGoalHurtByTarget(this, true,
				EntityPlayer.class));
		targetSelector.a(1, new PathfinderGoalNearestBot(this, true));
		targetSelector.a(2,
				new PathfinderGoalNearestAttackableTarget<EntityPlayer>(this,
						EntityPlayer.class, true));
		//new Navigation(arg0, arg1)
	}

	public void showToPlayer(Player p) {
		PlayerConnection connection = ((CraftPlayer) p).getHandle().playerConnection;
		connection.sendPacket(new PacketPlayOutPlayerInfo(
				PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER,
				new EntityPlayer[] { player }));
		connection.sendPacket(new PacketPlayOutNamedEntitySpawn(player));

	}

	public void leave() {

		for (EntityHuman h : world.players) {
			Player p = (Player) h.getBukkitEntity();
			PlayerConnection connection = ((CraftPlayer) p).getHandle().playerConnection;

			connection.sendPacket(new PacketPlayOutPlayerInfo(
					PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER,
					new EntityPlayer[] { player }));
			connection
					.sendPacket(new PacketPlayOutEntityDestroy(player.getId()));
		}
	}

	public void look(float yaw, float pitch) {
		this.yaw = yaw;
		this.pitch = pitch;
		byte y = (byte) (this.yaw * 256.0F / 360.0);
		byte p = (byte) (this.pitch * 256.0F / 360.0);
		sendPackets(new PacketPlayOutEntityLook(getId(), y, p, onGround),
				new PacketPlayOutEntityHeadRotation(player, y));
	}

	/** Sends packets to all players */
	public void sendPackets(Packet<?>... packets) {
		for (EntityHuman h : world.players) {
			Player p = (Player) h.getBukkitEntity();
			PlayerConnection connection = ((CraftPlayer) p).getHandle().playerConnection;
			for (Packet<?> packet : packets) {
				connection.sendPacket(packet);
			}
		}

	}

	public void move() {
		double vel = 0.1;
		float yaw = this.yaw * (float) Math.PI / 180.0F;
		double vx = -vel * MathHelper.sin(yaw);
		double vz = vel * MathHelper.cos(yaw);
		this.move(EnumMoveType.SELF, vx, 0, vz);
		sendPackets(new PacketPlayOutEntityTeleport(player),
				new PacketPlayOutEntityHeadRotation(player,
						(byte) (this.yaw * 256.0F / 360.0)));
	}

	// attack entity
	@Override
	public boolean B(Entity entity) {
		//player.B(entity);
		player.attack(entity);
		sendPackets(new PacketPlayOutAnimation(player, 0));
		return super.B(entity);
	}

	public void tick() {
		this.n();
		doTick();
		if (player.invulnerableTicks > 0) {
			player.invulnerableTicks--;
		}
		if (player.noDamageTicks > 0) {
			player.noDamageTicks--;
		}
		if (player.getHealth() <= 0.0) {
			//die();

		}
		setToMyPosition();
	}

	private void setToMyPosition() {
		player.setPosition(locX, locY, locZ);
		player.setPositionRotation(locX, locY, locZ, yaw, pitch);
		player.motX = this.motX;
		player.motY = this.motY;
		player.motZ = this.motZ;
		sendPackets(new PacketPlayOutEntityTeleport(player),
				new PacketPlayOutEntityHeadRotation(player,
						(byte) (this.yaw * 256.0F / 360.0)));

	}

	public void jump() {
		//setJumpState
		this.l(true);

	}

	@Override
	public boolean damageEntity(DamageSource damagesource, float f) {
		player.damageEntity(damagesource, f);
		return false;
	}

	public boolean isLookingAtMe(Player player) {

		double dist = player.getLocation().distanceSquared(new Location(
				this.world.getWorld(), locX, locY, locZ));
		if (dist > 16) {
			return false;

		}
		EntityPlayer p = ((CraftPlayer) player).getHandle();
		//world.ray
		return p.hasLineOfSight(this.player);
	}

	public int getPlayerId() {
		return player.getId();

	}

	public EntityPlayer getPlayer() {
		return player;
	}

}

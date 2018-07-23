package dinglydell.swinebot;

import java.lang.reflect.Field;
import java.util.UUID;

import net.minecraft.server.v1_11_R1.BlockPosition;
import net.minecraft.server.v1_11_R1.DamageSource;
import net.minecraft.server.v1_11_R1.Entity;
import net.minecraft.server.v1_11_R1.EntityCreature;
import net.minecraft.server.v1_11_R1.EntityHuman;
import net.minecraft.server.v1_11_R1.EntityLiving;
import net.minecraft.server.v1_11_R1.EntityPlayer;
import net.minecraft.server.v1_11_R1.EnumItemSlot;
import net.minecraft.server.v1_11_R1.EnumMoveType;
import net.minecraft.server.v1_11_R1.ItemStack;
import net.minecraft.server.v1_11_R1.MathHelper;
import net.minecraft.server.v1_11_R1.Packet;
import net.minecraft.server.v1_11_R1.PacketPlayOutAnimation;
import net.minecraft.server.v1_11_R1.PacketPlayOutEntity.PacketPlayOutEntityLook;
import net.minecraft.server.v1_11_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_11_R1.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_11_R1.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_11_R1.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_11_R1.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_11_R1.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_11_R1.PathfinderGoalFloat;
import net.minecraft.server.v1_11_R1.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_11_R1.PathfinderGoalMeleeAttack;
import net.minecraft.server.v1_11_R1.PathfinderGoalMoveTowardsTarget;
import net.minecraft.server.v1_11_R1.PlayerConnection;
import net.minecraft.server.v1_11_R1.PlayerInteractManager;
import net.minecraft.server.v1_11_R1.PlayerInventory;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_11_R1.CraftServer;
import org.bukkit.craftbukkit.v1_11_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerRespawnEvent;

import com.mojang.authlib.GameProfile;

import dinglydell.swinebot.ai.PathfinderGoalNearestBotOrPlayer;
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
		this.goalSelector.a(4, new PathfinderGoalMoveTowardsTarget(this, 0.5d,
				999f));
		//this.goalSelector.a(5, new PathfinderGoalMoveTowardsRestriction(this,
		//		1.0D));
		//this.goalSelector.a(7, new PathfinderGoalRandomStrollLand(this, 1.0D));
		//this.goalSelector.a(8, new PathfinderGoalLookAtPlayer(this,
		//		EntityHuman.class, 8.0F));
		//this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
		targetSelector.a(1, new PathfinderGoalHurtByTarget(this, true,
				EntityPlayer.class));
		targetSelector.a(2, new PathfinderGoalNearestBotOrPlayer(this, true));
		//targetSelector.a(2,
		//		new PathfinderGoalNearestAttackableTarget<EntityPlayer>(this,
		//				EntityPlayer.class, true));
		//new Navigation(arg0, arg1)
	}

	public void showToPlayer(Player p) {
		PlayerConnection connection = ((CraftPlayer) p).getHandle().playerConnection;
		connection.sendPacket(new PacketPlayOutPlayerInfo(
				PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER,
				new EntityPlayer[] { player }));
		connection.sendPacket(new PacketPlayOutNamedEntitySpawn(player));
		PacketPlayOutEntityEquipment[] eqs = getInventoryPackets();
		for (PacketPlayOutEntityEquipment eq : eqs) {
			connection.sendPacket(eq);
		}

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
		// hack the cooldown it's bad
		try {
			Field f = EntityLiving.class.getDeclaredField("aE");
			f.setAccessible(true);
			f.set(player, 999);
		} catch (Exception e) {
			e.printStackTrace();
		}
		getAttributeMap()
				.b(player.getItemInMainHand().a(EnumItemSlot.MAINHAND));
		//player.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(  )
		player.attack(entity);

		sendPackets(new PacketPlayOutAnimation(player, 0));
		if (entity instanceof Bot) {
			((Bot) entity).knockback(yaw);
		}
		return super.B(entity);
	}

	public void knockback(float yaw) {
		int i = 1;
		f((double) (-MathHelper.sin(yaw * (float) Math.PI / 180.0F) * (float) i * 0.5F),
				0.2D,
				(double) (MathHelper.cos(yaw * (float) Math.PI / 180.0F)
						* (float) i * 0.5F));

	}

	public void tick() {
		this.n();
		doTick();
		this.A_();
		if (player.invulnerableTicks > 0) {
			player.invulnerableTicks--;
		}
		if (player.noDamageTicks > 0) {
			player.noDamageTicks--;
		}
		player.A_();
		updateEquipment();
		if (player.getHealth() <= 0.0) {
			//die();
			respawn();

		}
		setToMyPosition();
	}

	private void updateEquipment() {
		for (EnumItemSlot slot : EnumItemSlot.values()) {

			//		switch(slot.a()){
			//			case ARMOR:
			//				this.bv
			//				break;
			//				case HAND:
			//					break;
			//			}
			//			player.getEquipment()
			//
			// ItemStack itemstack;
			//
			// ItemStack itemstack1 = getEquipment(enumitemslot);
			//
			// if (!ItemStack.matches(itemstack1, itemstack)) {
			//   ((WorldServer)this.world).getTracker().a(this, new PacketPlayOutEntityEquipment(getId(), enumitemslot, itemstack1));
			//   if (!itemstack.isEmpty()) {
			//     getAttributeMap().a(itemstack.a(enumitemslot));
			//   }
			//
			//   if (!itemstack1.isEmpty()) {
			//     getAttributeMap().b(itemstack1.a(enumitemslot));
			//   }
			//
			//   switch (enumitemslot.a()) {
			//   player.setSlot(slot, itemstack1.isEmpty() ? ItemStack.a : itemstack1.cloneItemStack());
			//   case ARMOR:
			//
			//     player.bu.set(enumitemslot.b(), itemstack1.isEmpty() ? ItemStack.a : itemstack1.cloneItemStack());
			//     break;
			//
			//   case HAND:
			//     player.bv.set(enumitemslot.b(), itemstack1.isEmpty() ? ItemStack.a : itemstack1.cloneItemStack());
			//   }
			//
			// }
		}

	}

	private void setToMyPosition() {
		if (player.locX != locX || player.locY != locY || player.locZ != locZ
				|| player.yaw != yaw || player.pitch != pitch) {
			//player.setPosition(locX, locY, locZ);
			player.setPositionRotation(locX, locY, locZ, yaw, pitch);
			sendPackets(new PacketPlayOutEntityTeleport(player),
					new PacketPlayOutEntityHeadRotation(player,
							(byte) (this.yaw * 256.0F / 360.0)));
		}

		//if (player.isBurning() != isBurning()) {
		//	player.fireTicks = isBurning() ? fireTicks : 0;
		//	sendPackets(new PacketPlayOutEntityMetadata(player.getId(),
		//			player.getDataWatcher(), true));
		//}

	}

	public void jump() {
		//setJumpState
		this.l(true);

	}

	//public void setInventoryItem(int i, ItemStack stack){
	//		this.player.inventory.setItem(i, stack);
	//}

	public void setSlot(EnumItemSlot slot, ItemStack stack) {
		player.setSlot(slot, stack);
		sendPackets(new PacketPlayOutEntityEquipment(player.getId(), slot,
				stack));

	}

	@Override
	public boolean damageEntity(DamageSource damagesource, float f) {
		Bukkit.getServer()
				.getConsoleSender()
				.sendMessage(player.getName() + " Health: "
						+ player.getHealth());
		if (damagesource.getEntity() instanceof EntityLiving) {
			a((EntityLiving) damagesource.getEntity());
		}
		return player.damageEntity(damagesource, f);
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

	private void respawn() {
		BlockPosition spawn = world.getSpawn();
		//locX = (double) spawn.getX();
		//locY = (double) spawn.getY() + 2;
		//locZ = (double) spawn.getZ();
		player.setHealth(20);

		Location loc = new Location(world.getWorld(), spawn.getX(),
				spawn.getY() + 5, spawn.getZ());
		PlayerRespawnEvent ev = new PlayerRespawnEvent(
				player.getBukkitEntity(), loc, false);
		Bukkit.getServer().getPluginManager().callEvent(ev);
		//		b.player.setHealth(20);
		//set inventory
		this.player.inventory = ((CraftPlayer) ev.getPlayer()).getHandle().inventory;
		Bot b = (Bot) teleportTo(ev.getRespawnLocation(), false);
		this.dead = true;

		Bukkit.getServer().getConsoleSender()
				.sendMessage(b.player.getItemInMainHand().toString());
		player.dead = true;
	}

	private void setInventory(PlayerInventory inventory) {
		player.inventory = inventory;
		sendPackets(getInventoryPackets());

	}

	private PacketPlayOutEntityEquipment[] getInventoryPackets() {
		PacketPlayOutEntityEquipment[] eqs = new PacketPlayOutEntityEquipment[EnumItemSlot
				.values().length];
		for (int i = 0; i < eqs.length; i++) {
			EnumItemSlot s = EnumItemSlot.values()[i];
			eqs[i] = new PacketPlayOutEntityEquipment(player.getId(), s,
					player.getEquipment(s));
		}
		return eqs;

	}

	/**
	 * Deletes the entity and creates a copy at the destination.
	 * 
	 * @return The new entity
	 */
	public Entity teleportTo(Location location, boolean isPortal) {
		leave();

		SwineBot.npcs.remove(this);
		Bot b = SwineBot.createNPC(location, player.getName());
		b.setInventory(player.inventory);
		b.player.setHealth(player.getHealth());
		b.start();
		//locX = location.getX();
		//locY = location.getY();
		//locZ = location.getZ();
		return this;
	}

}

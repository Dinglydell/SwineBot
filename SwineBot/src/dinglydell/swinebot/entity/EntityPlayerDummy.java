package dinglydell.swinebot.entity;

import net.minecraft.server.v1_11_R1.Container;
import net.minecraft.server.v1_11_R1.DamageSource;
import net.minecraft.server.v1_11_R1.EntityPlayer;
import net.minecraft.server.v1_11_R1.ItemStack;
import net.minecraft.server.v1_11_R1.MinecraftServer;
import net.minecraft.server.v1_11_R1.NonNullList;
import net.minecraft.server.v1_11_R1.PacketPlayOutEntityStatus;
import net.minecraft.server.v1_11_R1.PlayerInteractManager;
import net.minecraft.server.v1_11_R1.WorldServer;

import com.mojang.authlib.GameProfile;

import dinglydell.swinebot.Bot;

/** A fake player with no real connection */
public class EntityPlayerDummy extends EntityPlayer {

	protected Bot bot;

	public EntityPlayerDummy(Bot bot, MinecraftServer minecraftserver,
			WorldServer worldserver, GameProfile gameprofile,
			PlayerInteractManager playerinteractmanager) {
		super(minecraftserver, worldserver, gameprofile, playerinteractmanager);
		this.bot = bot;

	}

	@Override
	public void a(Container container, NonNullList<ItemStack> nonnulllist) {
		// I'm a dummy, dummy
		//super.a(container, nonnulllist);
	}

	@Override
	public void enterCombat() {
		//super.enterCombat();
	}

	@Override
	public void exitCombat() {
		// TODO Auto-generated method stub
		//	super.exitCombat();
	}

	@Override
	public boolean damageEntity(DamageSource damagesource, float f) {

		if (super.damageEntity(damagesource, f)) {
			bot.sendPackets(new PacketPlayOutEntityStatus(this, (byte) 2));
			return true;
		}
		return false;
	}

	@Override
	public void die(DamageSource source) {
		//super.die();

	}

}

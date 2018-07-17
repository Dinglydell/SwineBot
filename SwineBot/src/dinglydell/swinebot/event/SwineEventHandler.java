package dinglydell.swinebot.event;

import java.lang.reflect.Field;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;

import javax.sound.midi.Receiver;

import net.minecraft.server.v1_11_R1.ChatComponentText;
import net.minecraft.server.v1_11_R1.EntityPlayer;
import net.minecraft.server.v1_11_R1.PacketPlayInUseEntity;
import net.minecraft.server.v1_11_R1.PacketPlayInUseEntity.EnumEntityUseAction;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_11_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import dinglydell.swinebot.Bot;
import dinglydell.swinebot.SwineBot;

public class SwineEventHandler implements Listener {
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){
		for(Bot b: SwineBot.npcs){
			b.showToPlayer(event.getPlayer());
		}
		injectPlayer(event.getPlayer());

	}
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event){
		removePlayer(event.getPlayer());
	}
	private void removePlayer(Player player){
		Channel channel = ((CraftPlayer)player).getHandle().playerConnection.networkManager.channel;
		//final Player p = player;
		channel.pipeline().remove(player.getName());
	}
	private void injectPlayer(final Player player){
		ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler(){
			@Override
			public void channelRead(
					ChannelHandlerContext context,
					Object packet) throws Exception {

				if(packet instanceof PacketPlayInUseEntity){

					PacketPlayInUseEntity p = (PacketPlayInUseEntity) packet;
					if(p.a() == EnumEntityUseAction.ATTACK){
						Bukkit.getServer().getConsoleSender().sendMessage(packet.toString());
						if(p.a(((CraftWorld) player.getWorld()).getHandle()) == null){
							//gets messy here
							Field idField = PacketPlayInUseEntity.class.getDeclaredField("a");
							idField.setAccessible(true);
							int id = (int) idField.get(p);
							for(Bot b : SwineBot.npcs){
								if(id == b.getPlayerId()){
									((CraftPlayer)player).getHandle().attack(b.getPlayer());
									break;
								}
							}
						}
					}
				}
				super.channelRead(context, packet);
			}
		};

		ChannelPipeline pipeline = ((CraftPlayer)player).getHandle().playerConnection.networkManager.channel.pipeline();

		pipeline.addBefore("packet_handler", player.getName(), channelDuplexHandler);
	}
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event){
		for(Bot b: SwineBot.npcs){
			b.showToPlayer(event.getPlayer());
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event){
		for(Bot b : SwineBot.npcs){
			if(event.getTo().getY() - event.getPlayer().getLocation().getY() > 0) {
				//b.jump();
			} else {
				//b.look(event.getTo().getYaw(), event.getTo().getPitch());
				//b.move();
			}
//org.bukkit.event.block.
		}
	}
@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event){
		if(event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK){
			//if(event.getDamager() instanceof CraftPlayer){

				//event.getPlayer().sendMessage("Bop");

				for(Bot b : SwineBot.npcs){
					if(b.isLookingAtMe(event.getPlayer())){
			//			event.getPlayer().sendMessage("<" + b.getName() + "> AHHHH!");
					}
				}
			//}
		}

		 PlayerInteractAtEntityEvent ev;
	}
	@EventHandler
	public void onPlayerAnimationEntity(PlayerAnimationEvent event) {
		if(event.getAnimationType() == PlayerAnimationType.ARM_SWING){
			Player p = event.getPlayer();

			for(Bot b : SwineBot.npcs){

			}
			//p.sendMessage("Boop!");
		}



	}
}

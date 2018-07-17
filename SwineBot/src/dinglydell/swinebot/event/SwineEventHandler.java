package dinglydell.swinebot.event;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.v1_11_R1.PacketPlayInUseEntity;
import net.minecraft.server.v1_11_R1.PacketPlayInUseEntity.EnumEntityUseAction;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_11_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import dinglydell.swinebot.Bot;
import dinglydell.swinebot.SwineBot;
import dinglydell.swinebot.entity.EntityPlayerDummy;

public class SwineEventHandler implements Listener {
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		for (Bot b : SwineBot.npcs) {
			b.showToPlayer(event.getPlayer());
		}
		injectPlayer(event.getPlayer());

	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		removePlayer(event.getPlayer());
	}

	public static void removePlayer(final Player player) {
		final Channel channel = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel;
		//final Player p = player;
		channel.eventLoop().submit(new Runnable() {

			@Override
			public void run() {
				channel.pipeline().remove(player.getName());

			}
		});

	}

	public static void injectPlayer(final Player player) {
		final List<Bot> botList = SwineBot.npcs;

		ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {
			@Override
			public void channelRead(ChannelHandlerContext context, Object packet)
					throws Exception {

				if (packet instanceof PacketPlayInUseEntity) {

					PacketPlayInUseEntity p = (PacketPlayInUseEntity) packet;
					if (p.a() == EnumEntityUseAction.ATTACK) {
						//Bukkit.getServer().getConsoleSender()
						//	.sendMessage(packet.toString());
						if (p.a(((CraftWorld) player.getWorld()).getHandle()) == null) {
							//gets messy here
							Field idField = PacketPlayInUseEntity.class
									.getDeclaredField("a");
							idField.setAccessible(true);
							int id = (int) idField.get(p);
							ArrayList<Bot> botsCopy = new ArrayList<Bot>(
									botList);
							for (Bot b : botsCopy) {
								if (id == b.getPlayerId()) {
									((CraftPlayer) player).getHandle()
											.attack(b);
									b.knockback(player.getLocation().getYaw());
									break;
								}
							}
						}
					}
				}
				super.channelRead(context, packet);
			}

		};

		ChannelPipeline pipeline = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel
				.pipeline();

		pipeline.addBefore("packet_handler",
				player.getName(),
				channelDuplexHandler);
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		double x = Math.random() * 100;
		double z = Math.random() * 100;
		//sample test
		event.setRespawnLocation(new Location(event.getPlayer().getWorld(), x,
				event.getPlayer().getWorld()
						.getHighestBlockYAt((int) x, (int) z), z));
		if (!(((CraftPlayer) event.getPlayer()).getHandle() instanceof EntityPlayerDummy)) {
			for (Bot b : SwineBot.npcs) {
				b.showToPlayer(event.getPlayer());
			}
		}
	}

}

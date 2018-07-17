package dinglydell.swinebot.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.server.v1_11_R1.EntityCreature;
import net.minecraft.server.v1_11_R1.EntityLiving;
import net.minecraft.server.v1_11_R1.EntityPlayer;
import net.minecraft.server.v1_11_R1.IEntitySelector;
import net.minecraft.server.v1_11_R1.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_11_R1.PathfinderGoalTarget;

import org.bukkit.event.entity.EntityTargetEvent;

import dinglydell.swinebot.Bot;
import dinglydell.swinebot.SwineBot;

public class PathfinderGoalNearestBotOrPlayer extends PathfinderGoalTarget {
	private final PathfinderGoalNearestAttackableTarget.DistanceComparator comparator;

	protected EntityLiving target;

	public PathfinderGoalNearestBotOrPlayer(EntityCreature entitycreature,
			boolean flag) {
		super(entitycreature, flag);

		comparator = new PathfinderGoalNearestAttackableTarget.DistanceComparator(
				entitycreature);
	}

	//there's a very real chance here I've got all the obf methods/fields completely wrong
	@Override
	public boolean a() {
		//target chance

		//get target distance
		//double d = this.i();

		if (SwineBot.npcs.size() <= 1) {
			return false;
		} else {
			List<Bot> bots = new ArrayList<Bot>(SwineBot.npcs);
			List<EntityPlayer> players = this.e.world.a(EntityPlayer.class, e
					.getBoundingBox().grow(64.0, 8.0, 64.0), IEntitySelector.e);
			if (players.size() == 0 && bots.size() == 1) {
				return false;
			}
			Collections.sort(bots, comparator);
			Collections.sort(players, comparator);

			Bot b = bots.size() == 0 ? null : bots.get(0);
			if (b == this.e) {
				b = bots.get(1);
			}
			EntityPlayer p = players.size() == 0 ? null : players.get(0);
			if (b == null) {
				this.target = p;
				return true;
			}
			if (p == null) {
				this.target = b;
				return true;
			}
			double playerDist = p.getBukkitEntity().getLocation()
					.distance(this.e.getBukkitEntity().getLocation());
			double botDist = b.getBukkitEntity().getLocation()
					.distance(this.e.getBukkitEntity().getLocation());
			if (playerDist > botDist) {
				this.target = b;
			} else {
				this.target = p;
			}
			return true;
		}

	}

	@Override
	public void c() {
		this.e.setGoalTarget(target,
				EntityTargetEvent.TargetReason.CLOSEST_PLAYER,
				true);
		super.c();
	}

	@Override
	public boolean b() {

		return super.b() && this.e.getRandom().nextInt(10) != 0;
	}

}

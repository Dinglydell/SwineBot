package dinglydell.swinebot.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dinglydell.swinebot.Bot;
import dinglydell.swinebot.SwineBot;
import net.minecraft.server.v1_11_R1.EntityCreature;
import net.minecraft.server.v1_11_R1.PathfinderGoalNearestAttackableTarget;

public class PathfinderGoalNearestBot extends
		PathfinderGoalNearestAttackableTarget<Bot> {
private final PathfinderGoalNearestAttackableTarget.DistanceComparator comparator;
	public PathfinderGoalNearestBot(EntityCreature entitycreature,
			 boolean flag) {
		super(entitycreature, Bot.class, flag);
		comparator = new PathfinderGoalNearestAttackableTarget.DistanceComparator(entitycreature);
	}

	//there's a very real chance here I've got all the obf methods/fields completely wrong
	@Override
	public boolean a() {
		//target chance
		if(this.h > 0 && this.e.getRandom().nextInt(this.h) != 0){
			return false;
		} else {
			//get target distance
			//double d = this.i();



			if(SwineBot.npcs.size() <= 1) {
				return false;
			} else {
				List<Bot> bots = new ArrayList<Bot>(SwineBot.npcs);
				Collections.sort(bots, comparator);
				this.d = bots.get(0);
				if(this.d == this.e){
					this.d = bots.get(1);
				}
				return true;
			}

		}
	}

}

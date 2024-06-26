package io.github.togar2.pvp;

import io.github.togar2.pvp.config.PvPConfig;
import io.github.togar2.pvp.enchantment.CustomEnchantments;
import io.github.togar2.pvp.entity.CustomPlayer;
import io.github.togar2.pvp.entity.Tracker;
import io.github.togar2.pvp.food.FoodComponents;
import io.github.togar2.pvp.potion.effect.CustomPotionEffects;
import io.github.togar2.pvp.potion.item.CustomPotionTypes;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeInstance;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.EntityInstanceEvent;

public class PvpExtension {
	
	public static EventNode<EntityInstanceEvent> events() {
		return PvPConfig.DEFAULT.createNode();
	}
	
	public static EventNode<EntityInstanceEvent> legacyEvents() {
		return PvPConfig.LEGACY.createNode();
	}
	
	/**
	 * Disables or enables legacy attack for a player.
	 * With legacy attack, the player has no attack speed and 1.0 attack damage instead of 2.0.
	 *
	 * @param player the player
	 * @param legacyAttack {@code true} if legacy attack should be enabled
	 */
	public static void setLegacyAttack(Player player, boolean legacyAttack) {
		AttributeInstance speed = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
		AttributeInstance damage = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
		if (legacyAttack) {
			speed.setBaseValue(100);
			damage.setBaseValue(1.0F);
		} else {
			speed.setBaseValue(speed.getAttribute().defaultValue());
			damage.setBaseValue(damage.getAttribute().defaultValue());
		}
	}
	
	/**
	 * Initialize the PvP extension.
	 */
	public static void init() {
		FoodComponents.registerAll();
		CustomEnchantments.registerAll();
		CustomPotionEffects.registerAll();
		CustomPotionTypes.registerAll();
		
		Tracker.register(MinecraftServer.getGlobalEventHandler());
		MinecraftServer.getConnectionManager().setPlayerProvider(CustomPlayer::new);
		//MinecraftServer.getPacketListenerManager().setPlayListener(ClientUseItemPacket.class, PvPUseItemListener::useItemListener);
	}
}

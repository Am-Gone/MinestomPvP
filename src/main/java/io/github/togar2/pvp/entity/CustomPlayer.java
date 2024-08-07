package io.github.togar2.pvp.entity;

import net.minestom.server.ServerFlag;
import net.minestom.server.collision.Aerodynamics;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.collision.PhysicsUtils;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityVelocityEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.TimedPotion;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Function;

@SuppressWarnings("UnstableApiUsage")
public class CustomPlayer extends Player implements PvpPlayer {
	private boolean velocityUpdate = false;
	private PhysicsResult previousPhysicsResult = null;
	
	public CustomPlayer(@NotNull UUID uuid, @NotNull String username, @NotNull PlayerConnection playerConnection) {
		super(uuid, username, playerConnection);
	}
	
	@Override
	public void setVelocity(@NotNull Vec velocity) {
		EntityVelocityEvent entityVelocityEvent = new EntityVelocityEvent(this, velocity);
		EventDispatcher.callCancellable(entityVelocityEvent, () -> {
			this.velocity = entityVelocityEvent.getVelocity();
			velocityUpdate = true;
		});
	}
	
	@Override
	public void setVelocityNoUpdate(Function<Vec, Vec> function) {
		velocity = function.apply(velocity);
	}
	
	@Override
	public void sendImmediateVelocityUpdate() {
		if (velocityUpdate) {
			velocityUpdate = false;
			sendPacketToViewersAndSelf(getVelocityPacket());
		}
	}
	
	@Override
	protected void movementTick() {
		this.gravityTickCount = onGround ? 0 : gravityTickCount + 1;
		if (vehicle != null) return;
		
		final double tps = ServerFlag.SERVER_TICKS_PER_SECOND;
		
		// Slow falling effect
		Aerodynamics aerodynamics = getAerodynamics();
		if (velocity.y() < 0 && hasEffect(PotionEffect.SLOW_FALLING))
			aerodynamics = aerodynamics.withGravity(0.01);
		
		PhysicsResult physicsResult = PhysicsUtils.simulateMovement(position, velocity.div(ServerFlag.SERVER_TICKS_PER_SECOND), boundingBox,
				instance.getWorldBorder(), instance, aerodynamics, hasNoGravity(), hasPhysics, onGround, isFlying(), previousPhysicsResult);
		this.previousPhysicsResult = physicsResult;
		
		Chunk finalChunk = ChunkUtils.retrieve(instance, currentChunk, physicsResult.newPosition());
		if (!ChunkUtils.isLoaded(finalChunk)) return;
		
		velocity = physicsResult.newVelocity().mul(tps);
		onGround = physicsResult.isOnGround();
		
		// Levitation effect
		TimedPotion levitation = getEffect(PotionEffect.LEVITATION);
		if (levitation != null) {
			velocity = velocity.withY(
					((0.05 * (double)
							(levitation.potion().amplifier() + 1)
							- (velocity.y() / tps)) * 0.2) * tps
			);
		}

		sendImmediateVelocityUpdate();
	}
}

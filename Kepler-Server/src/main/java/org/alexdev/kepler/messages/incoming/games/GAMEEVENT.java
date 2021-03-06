package org.alexdev.kepler.messages.incoming.games;

import org.alexdev.kepler.game.games.Game;
import org.alexdev.kepler.game.games.GameObject;
import org.alexdev.kepler.game.games.battleball.BattleBallGame;
import org.alexdev.kepler.game.games.battleball.BattleBallPowerUp;
import org.alexdev.kepler.game.games.battleball.events.ActivatePowerUpEvent;
import org.alexdev.kepler.game.games.player.GamePlayer;
import org.alexdev.kepler.game.games.snowstorm.SnowStormGame;
import org.alexdev.kepler.game.games.snowstorm.SnowStormObject;
import org.alexdev.kepler.game.games.snowstorm.events.SnowStormAvatarMoveEvent;
import org.alexdev.kepler.game.player.Player;
import org.alexdev.kepler.messages.outgoing.games.SNOWSTORM_GAMESTATUS;
import org.alexdev.kepler.messages.types.MessageEvent;
import org.alexdev.kepler.server.netty.streams.NettyRequest;

public class GAMEEVENT implements MessageEvent {
    @Override
    public void handle(Player player, NettyRequest reader) throws Exception {
        if (!player.getRoomUser().isWalkingAllowed()) {
            return;
        }

        GamePlayer gamePlayer = player.getRoomUser().getGamePlayer();

        if (gamePlayer == null) {
            return;
        }

        Game game = gamePlayer.getGame();

        int eventType = reader.readInt(); // Instance ID? Useless?

        // Walk request
        if (eventType == 0) {
            if (!player.getRoomUser().isWalkingAllowed()) {
                return;
            }

            int X = reader.readInt();
            int Y = reader.readInt();


            int newX = SnowStormGame.convertToGameCoordinate(X);
            int newY = SnowStormGame.convertToGameCoordinate(Y);

            System.out.println("SnowStorm walk request: " + X + ",      " + Y);
            System.out.println("SnowStorm walk request: " + newX + ",   " + newY);

           player.getRoomUser().walkTo(newX, newY);
        }

        // Jump request
        if (eventType == 2) {
            if (!player.getRoomUser().isWalkingAllowed()) {
                return;
            }

            int X = reader.readInt();
            int Y = reader.readInt();

            player.getRoomUser().bounceTo(X, Y);
        }

        // Use power up request
        if (eventType == 4) {
            int powerId = reader.readInt();

            if (game instanceof BattleBallGame) {
                BattleBallGame battleballGame = (BattleBallGame) game;

                if (!battleballGame.getStoredPowers().containsKey(gamePlayer)) {
                    return;
                }

                var powerList = battleballGame.getStoredPowers().get(gamePlayer);

                BattleBallPowerUp powerUp = null;

                for (BattleBallPowerUp power : powerList) {
                    if (power.getId() == powerId) {
                        powerUp = power;
                        break;
                    }
                }

                if (powerUp != null) {
                    battleballGame.getEventsQueue().add(new ActivatePowerUpEvent(gamePlayer, powerUp));
                    powerList.remove(powerUp);

                    powerUp.usePower(gamePlayer, player.getRoomUser().getPosition());
                }
            }
        }
    }
}

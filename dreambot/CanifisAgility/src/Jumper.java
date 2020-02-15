
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;

import org.dreambot.api.input.event.impl.InteractionEvent;
import org.dreambot.api.input.event.impl.InteractionSetting;
import org.dreambot.api.input.mouse.destination.impl.EntityDestination;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.filter.Filter;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.utilities.impl.Condition;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.items.GroundItem;

@ScriptManifest(category = Category.AGILITY, name = "CanifisAgility", author="redbandit",
        version=1.0, description = "Runs Canifis Rooftop Agility Course")

public class Jumper extends AbstractScript {

    public static final int MARK_ID = 11849;    //mark of grace id
    public static final int OBSTACLES[] = {10819,10820,10821,10828,10822,10831,10823,10832};    //obstacle ids

    public static final Tile START = new Tile(3507,3488);   //start location

    private int prev;

    private Player player;

    @Override
    public void onStart(){
        prev = OBSTACLES[OBSTACLES.length - 1];
        player = getPlayers().localPlayer();

        getCamera().rotateTo(0,383);
    }

    @Override
    public int onLoop() {

        if(player.getAnimation() == -1 && !player.isMoving()) { //if not moving

            if(player.getZ() == 0) {    //if on ground floor

                if(!isTileOnScreen(START) || START.distance() > 3) goToStart();   //goes to start of course
                else if(nextObstacle().interact()) prev = OBSTACLES[OBSTACLES.length - 1];

            } else {    //if on rooftops

                GroundItem mark = getGroundItems().closest(new Filter<GroundItem>() {
                    @Override
                    public boolean match(GroundItem groundItem) {
                        return groundItem.getID() == MARK_ID;
                    }
                });

                if(mark != null && mark.distance() < 5) {
                    mark.interact();    //take mark of grace
                } else {

                    GameObject obstacle = nextObstacle();   //get next obstacle
                    InteractionEvent ie = new InteractionEvent(new EntityDestination(getClient(), obstacle));

                    if(obstacle.isOnScreen()) { //if obstacle is on screen

                        if (ie.interact("Jump",InteractionSetting.MOVE)
                                || ie.interact("Vault",InteractionSetting.MOVE)) {

                            sleep(Calculations.random(200,500));
                            sleepUntil(new Condition() {
                                @Override
                                public boolean verify() {
                                    return !player.isMoving() && player.getAnimation() == -1;
                                }
                            }, 6500);

                            prev = obstacle.getID();

                            sleep(Calculations.random(2200,2700));

                        }

                    } else {
//                        getCamera().rotateToEntity(obstacle);

                        getWalking().walkOnScreen(nextTile(obstacle.getTile()));

                        sleep(Calculations.random(200,500));
                        sleepUntil(new Condition() {
                            @Override
                            public boolean verify() {
                                return !player.isMoving();
                            }
                        }, 7500);

                    }
                }
            }
        }

        return Calculations.random(500, 900);
    }

    public boolean isTileOnScreen(Tile t){
        return getMap().isTileOnScreen(t) && getMap().isVisible(t);
//        return getClient().getViewport().isOnGameScreen(getMap().tileToScreen(t));
    }

    /** Goes to start of course */
    public void goToStart() {
        getWalking().walk(START.getRandomizedTile(1));

        sleepUntil(new Condition() {
            @Override
            public boolean verify() {
                return !player.isMoving();
            }
        }, 13000);
    }

    public Tile nextTile(Tile dest) {
//        new Area(player.getTile(),dest).getNearestTile(nextObstacle());
        Tile center = new Area(player.getTile(),dest).getCenter().getRandomizedTile(1);;
        if(!isTileOnScreen(center)) return nextTile(center);
        return center;
    }

    /** returns next obstacle */
    public GameObject nextObstacle() {
        return getGameObjects().closest(new Filter<GameObject>() {
            @Override
            public boolean match(GameObject gameObject) {
                return indexOf(gameObject.getID()) != -1 && gameObject.getID() != prev;
            }
        });
    }

    public int indexOf(int id) {
        for(int i = 0; i < OBSTACLES.length; i++) {
            if(id == OBSTACLES[i]) return i;
        }
        return -1;
    }

}
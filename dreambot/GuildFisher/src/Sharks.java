
import org.dreambot.api.input.event.impl.InteractionEvent;
import org.dreambot.api.input.event.impl.InteractionSetting;
import org.dreambot.api.input.mouse.destination.impl.EntityDestination;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.depositbox.DepositBox;
import org.dreambot.api.methods.filter.Filter;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;

import org.dreambot.api.utilities.impl.Condition;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.interactive.Player;

@ScriptManifest(category = Category.FISHING, name = "GuildFisher", author="redbandit",
        version=1.1, description = "catches sharks at the fishing guild")

public class Sharks extends AbstractScript {

    public static final int SPOT_ID = 1511;
    public static final int SHARK_ID = 383;
    public static final int DRAGON_HARPOON_ID = 383;

    public static final int MAX_IDLE = 290000;

    public static String SPOT = "Net";  //type of fishing spot
    public static String TOOL = "Harpoon";  //tool to use


    private long lastActive;

    private Player player;
    private Area north_dock, south_dock, box_area;


    @Override
    public void onStart(){
        lastActive = System.currentTimeMillis() - MAX_IDLE;
        player = getPlayers().localPlayer();    //init player

        box_area = new Area(2592,3417,2588,3415);
        north_dock = new Area(2605,3426,2598,3419);
        south_dock = new Area(2612,3417,2602,3410);

        getCamera().rotateTo(0,383);
    }

    @Override
    public int onLoop() {

        if(!player.isMoving() && player.getAnimation() == -1) {    //if not moving

            if(getInventory().isFull()) {   //if inventory is full

                bank();

            } else { //if inventory is not full

                sleep(Calculations.random(2700,3500));

                if(atDock(player)) {    //if at docks

                    log("Looking for a new spot...");

                    boolean atNorthDock = atNorthDock(player);
                    NPC spot = atNorthDock ? spot(north_dock) : spot(south_dock);
                    InteractionEvent ie = new InteractionEvent(new EntityDestination(getClient(), spot));

                    if(spot == null) {

                        log("spot not found");

                        Area dock = atNorthDock ? south_dock : north_dock;
                        getWalking().walk(dock.getCenter().getRandomizedTile(3));

                        sleepUntil(new Condition() {
                            @Override
                            public boolean verify() {
                                return !player.isMoving();
                            }
                        }, 13000);

                    } else if(!isTileOnScreen(spot.getTile())) {
//                        getWalking().walk(spot.getTile().getRandomizedTile(1));

                        getWalking().walkOnScreen(nextTile(spot.getTile()));

                        sleepUntil(new Condition() {
                            @Override
                            public boolean verify() {
                                return !player.isMoving();
                            }
                        },4900);
                    } else if(ie.interact(TOOL,InteractionSetting.MOVE)) {
                        lastActive = System.currentTimeMillis();
                        sleepUntil(new Condition(){
                            public boolean verify(){
                                return getLocalPlayer().getAnimation() != -1;
                            }
                        },9500);
                    }

                } else {
                    getWalking().walk(north_dock.getRandomTile());  //go to north dock
                }

            }

       }

        return Calculations.random(1300, 2600);
    }

    public boolean bank() {
        log("BANK");

        DepositBox box = getDepositBox();
        if(!box_area.contains(player)) { //if not near bank

            return getWalking().walk(box_area.getRandomTile());    //go to bank

        } else if(!getDepositBox().isOpen()) { //if bank is not open

            box = getDepositBox(); //init deposit box
            return box.open();

        } else {    //if bank is open

            if(getInventory().count(SHARK_ID) == 28)
                box.depositAllItems();  //deposit all items
            else
                box.depositAll(SHARK_ID);   //deposit all sharks

        }
        return getInventory().size() <= 2;
    }

    public NPC spot(Area dock) {
        return getNpcs().closest(new Filter<NPC>() {
            @Override
            public boolean match(NPC npc) {
                return npc.getID() == SPOT_ID && dock.contains(npc);
                //return npc.hasAction(SPOT) && npc.hasAction(TOOL) && dock.contains(npc);
            }
        });
    }

    public Tile nextTile(Tile dest) {
//        Tile t = north_dock.getNearestTile(spot);
        Tile t = new Area(player.getTile(),dest).getCenter().getRandomizedTile(1);;
        if(!isTileOnScreen(t)) return nextTile(t);
        return t;
    }

    public boolean isTileOnScreen(Tile t){ return getMap().isTileOnScreen(t) && getMap().isVisible(t); }

    public boolean atNorthDock(Player player) { return north_dock.contains(player); }

    public boolean atSouthDock(Player player) {
        return south_dock.contains(player);
    }

    public boolean atDock(Player player) { return atNorthDock(player) || atSouthDock(player); }

}



/*

    if not moving

        if inventory is full
            if not near bank
                go to bank
            else if bank is not open
                open bank
            else
                deposit sharks

        else
            if not at either dock
                go to north dock
            else if not fishing
                find nearest spot on current dock
                if there are none
                    go to other dock
                else
                    fish at nearest spot


     //////////////

     fix camera movements
        if spot is not in viewport
            walk near spot
            rotate camera

 */
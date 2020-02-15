
import org.dreambot.api.input.event.impl.InteractionEvent;
import org.dreambot.api.input.event.impl.InteractionSetting;
import org.dreambot.api.input.mouse.destination.impl.EntityDestination;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.utilities.impl.Condition;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;

import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;

import java.sql.Timestamp;

@ScriptManifest(category = Category.SMITHING, name = "BlastFurnace", author="redbandit",
        version=1.0, description = "smiths adamantite bars")

public class Adamant extends AbstractScript {

    //item ids
    static final int COAL_BAG_ID = 12019;
    static final int COAL_ID = 453;
    static final int ORE_ID = 449;
    static final int BAR_ID = 2361;
    static final int GOLD_ID = 995;

    static final int SEC = 20;    //gold per second

    static final int BELT_ID = 9100;
    static final int DISP_ID = 9092;
    static final int COFFER_ID = 29330;

    static final Tile BANK_TILE = new Tile(1948,4957);
    static final Tile BELT_TILE = new Tile(1942, 4967, 0);


    private boolean emptied, moreCoal;
    private int barsMade;
    private long stopAt;

    private enum State { SMELT, TAKE, BANK }
    private State state;

    private Player player;
    private Bank bank;
    private GameObject belt, disp;


    @Override
    public void onStart(){

        emptied = moreCoal = true;
        barsMade = 0;
        state = State.BANK;

        player = getPlayers().localPlayer();
        bank = getBank();
        belt = getGameObjects().closest(BELT_ID);
        disp = getGameObjects().closest(DISP_ID);

//        log( (belt != null && disp != null && bank != null) ? "true" : "false");
        if(player == null || bank == null || belt == null || disp == null) stop();

        GameObject coffer = getGameObjects().closest(COFFER_ID);    //get coffer
        int amount = getInventory().count(GOLD_ID); //get amount of gold in inventory
        long dur = (amount / SEC) * 1000;   //total duration of run in milliseconds
        amount = ((amount + 999)/1000) * 1000;  //round gold to nearest 1000

        getWalking().setRunThreshold(Calculations.random(23,37));

        getCamera().rotateTo(0,383);    //rotate camera

        //deposit coins in coffer
        getWalking().walk(coffer.getTile());
        sleep(Calculations.random(600,900));
        sleepUntil(new Condition() {
            @Override
            public boolean verify() {
                return !player.isMoving();
            }
        },15000);
        coffer.interactForceLeft("Use");
        sleep(Calculations.random(1000,1500));
        getWidgets().getWidget(219).getChild(0).getChild(1).interact();
        sleep(Calculations.random(700,1000));
        getKeyboard().type(amount);

        stopAt = System.currentTimeMillis() + dur;  //stop time
        log("Scheduled to stop in "+(dur/60000)+" minutes at "+(new Timestamp(stopAt)));
    }

    @Override
    public int onLoop() {

//        log(state.toString());

        if(!player.isMoving()) {
            if (state.equals(State.BANK)) {  //BANK
                if (bank()) state = State.SMELT;
            } else if (state.equals(State.SMELT)) {  //SMELT
                if (smelt()) {
                    state = moreCoal ? State.BANK : State.TAKE;
                    moreCoal = !moreCoal;
                }
            } else if (state.equals(State.TAKE)) {   //TAKE BARS
                if (take()) state = State.BANK;
                getWalking().setRunThreshold(Calculations.random(23, 37));
            }
        }

        if(System.currentTimeMillis() >= stopAt) {
            log("Stopped at "+(new Timestamp(System.currentTimeMillis())));
            log(barsMade + " bars made");
            stop();
        }

        return Calculations.random(500,900);
    }


    public boolean bank() {

        boolean done = false;
        Inventory inv = getInventory();

        if(!BANK_TILE.getArea(2).contains(player)) {    //if away from bank
            getWalking().walk(BANK_TILE.getRandomizedTile(1));  //go to bank
        } else if(!bank.isOpen()) { //if bank is not open
            bank.open();    //open bank
        } else if(inv.count(BAR_ID) > 0 || (inv.count(COAL_ID) == 0 && emptied)) {

            barsMade += inv.count(BAR_ID);
            bank.depositAll(BAR_ID);    //deposit all bars
            sleepUntil(new Condition() {
                @Override
                public boolean verify() {
                    return inv.count(BAR_ID) == 0;
                }
            },2000);

            bank.withdrawAll(COAL_ID);  //withdraw coal
            sleepUntil(new Condition() {
                @Override
                public boolean verify() {
                    return inv.count(COAL_ID) > 0;
                }
            },2000);

        } else if(inv.count(COAL_ID) > 0 && emptied) {

            bank.close();   //close bank

            sleepUntil(new Condition() {
                @Override
                public boolean verify() {
                    return !bank.isOpen();
                }
            },3000);

            emptied = !inv.get(COAL_BAG_ID).interact(); //fill coal bag

        } else {

            if(moreCoal)
                bank.withdrawAll(COAL_ID);
            else
                bank.withdrawAll(ORE_ID);

            sleepUntil(new Condition() {
                @Override
                public boolean verify() {
                    return inv.isFull();
                }
            },3000);

            done = true;
        }

        return done;
    }

    public boolean smelt() {

        Inventory inv = getInventory();

        if(!player.getTile().equals(BELT_TILE)) {
            getWalking().walk(BELT_TILE);
            sleep(Calculations.random(300,600));
            sleepUntil(new Condition() {
                @Override
                public boolean verify() {
                    return !player.isMoving();
                }
            },15000);
        } else if(inv.count(COAL_ID) > 0 || inv.count(ORE_ID) > 0) {
            belt.interactForceLeft("Put-ore-on");
            sleep(Calculations.random(300,600));
            sleepUntil(new Condition() {
                @Override
                public boolean verify() {
                    return inv.count(COAL_ID) == 0 && inv.count(ORE_ID) == 0;
                }
            },2000);

            if(!emptied) {
                emptied = inv.get(COAL_BAG_ID).interact("Empty");
                sleep(Calculations.random(300,600));
                sleepUntil(new Condition() {
                    @Override
                    public boolean verify() {
                        return inv.count(COAL_ID) > 0;
                    }
                },2000);
            }

        }

        return emptied && inv.count(COAL_ID) == 0 && inv.count(ORE_ID) == 0;
    }

    public boolean take() {

        if(!disp.isOnScreen()) {
            getWalking().walk(disp.getTile().getRandomizedTile(1));
        } else if(!disp.getSurroundingArea(1).contains(player) && getInventory().count(BAR_ID) == 0) {

            sleepUntil(new Condition() {
                @Override
                public boolean verify() {
                    return disp.hasAction("Take");
                }
            },4000);
            sleep(Calculations.random(500,900));

            InteractionEvent ie = new InteractionEvent(new EntityDestination(getClient(), disp));
            ie.interact("Take",InteractionSetting.MOVE);

            sleep(Calculations.random(400,700));
            sleepUntil(new Condition() {
                @Override
                public boolean verify() {
                    return !player.isMoving();
                }
            },6000);
            sleep(Calculations.random(1000,2000));
        } else {    //take bars
            getWidgets().getWidget(270).getChild(14).interact();
            sleepUntil(new Condition() {
                @Override
                public boolean verify() {
                    return getInventory().count(BAR_ID) > 0;
                }
            },2000);
        }

        return getInventory().count(BAR_ID) > 0;
    }

}
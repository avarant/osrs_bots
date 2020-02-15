
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.wrappers.widgets.WidgetChild;
import org.dreambot.api.wrappers.items.Item;

import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;

import java.sql.Timestamp;

@ScriptManifest(category = Category.COMBAT, name = "NMZ Strength", author="redbandit",
        version=1.0, description = "Nightmare Zone")

public class Nghtmare extends AbstractScript {

    //CLICK RANDOM SPOT FOR CONVEYOR BELT, THEN CLIC ON BELT WHEN IN VIEW
    //WAIT NEAR DISPENSER FOR BARS, HOVER UNTIL BARS ARE READY
    //FIX START CAMERA MOVEMENTS
    //ADD RANDOM CAMERA MOVEMENTS
    //OCCASIONALLY CHECK STATS AND SMITHING EXP
    //DECREASE DELAY BETWEEN ACTIONS

    public static final int
                            //item ids
                            ROCK_CAKE = 7510,
                            OVERLOAD = 11730,
                            ABSORPTION = 11734,

                            //game object, npc ids
                            ONION_ID = 1120,    //npc dominic onion id
                            COFFER_ID = 26292,
                            VIAL_ID = 26291,    //dream vial
                            DEPOSIT = 26000,    //amount of gold to put in coffer

                            OVERLOAD_COOLDOWN = 300000;

    private int strength_lvl;
    private long overload_timer;

    private Combat combat;
    private Item rockcake;

    @Override
    public void onStart(){
        if(getInventory().count(ROCK_CAKE) == 0 || outOfAbsorption()) stop();

        Area area = new Area(2604,3119,2610,3112);
        if(!area.contains(getLocalPlayer())) stop();

        log(new Timestamp(System.currentTimeMillis()).toString());

        strength_lvl = getSkills().getRealLevel(Skill.STRENGTH);
        overload_timer = System.currentTimeMillis() - OVERLOAD_COOLDOWN;
        combat = getCombat();
        rockcake = getInventory().get(ROCK_CAKE);


        Widgets widgets = getWidgets();

        if(!setupDream(widgets)) stop();    //talk to dominic onion and fill coffer

        //enter dream
        getGameObjects().closest(VIAL_ID).interact();
        sleep(Calculations.random(2000,3000));
        widgets.getWidget(129).getChild(6).getChild(9).interact();

        sleep(Calculations.random(5000,6000));

        getInventory().get(ABSORPTION,ABSORPTION+1,ABSORPTION+2,ABSORPTION+3).interact();
    }

    @Override
    public int onLoop() {

        if((outOfOverload() || outOfAbsorption()) && shouldOverload()) stop();

        if(shouldOverload() || (System.currentTimeMillis() - overload_timer > OVERLOAD_COOLDOWN)) {
            getInventory().get(OVERLOAD,OVERLOAD+1,OVERLOAD+2,OVERLOAD+3).interact();   //drink overload
            overload_timer = System.currentTimeMillis();
            log("OVERLOAD "+new Timestamp(overload_timer));
            sleep(Calculations.random(6600,7200));
        }
        else if(combat.getHealthPercent() > 2) rockcake.interact();  //eat rock cake
        else if(combat.getHealthPercent() > 1) {
            sleep(Calculations.random(2500,4300));
            rockcake.interact("Guzzle");    //guzzle rock cake
        }
        else if(shouldAbsorption()) getInventory().get(ABSORPTION,ABSORPTION +1,ABSORPTION+2,ABSORPTION+3).interact();  //drink absorption
        else if(getLocalPlayer().isInCombat() && getCombat().getSpecialPercentage() > Calculations.random(68,93)) { //special attack
            combat.openTab();
            sleep(Calculations.random(1000,2000));
            combat = getCombat();   //update
            combat.toggleSpecialAttack(true);
            sleep(Calculations.random(900,1100));
            getTabs().open(Tab.INVENTORY);
        }

        return Calculations.random(900,1100);
    }



    public boolean shouldOverload() { return combat.getHealthPercent() > 50 && getSkills().getBoostedLevels(Skill.STRENGTH) == strength_lvl; }

    public boolean shouldAbsorption() { return Integer.parseInt(getWidgets().getWidget(202).getChild(3).getChild(5).getText()) < Calculations.random(69,207); }

    public boolean outOfAbsorption() {
        Inventory inv = getInventory();
        int total = 0;
        for (int i = 0; i <= 3; i++)
            total += inv.count(ABSORPTION + i);
        return total == 0;
    }

    public boolean outOfOverload() {
        Inventory inv = getInventory();
        int total = 0;
        for (int i = 0; i <= 3; i++)
            total += inv.count(OVERLOAD + i);
        return total == 0;
    }

    /** sets up dream and fills coffer */
    public boolean setupDream(Widgets widgets) {
        WidgetChild rumble, clicktocontinue, agree;

        //talk to dominic onion to arrange dream
        getNpcs().closest(ONION_ID).interact("Dream");
        sleep(Calculations.random(1000,2000));
        rumble = widgets.getWidget(219).getChild(0).getChild(3);
        rumble.interact();
        sleep(Calculations.random(700,1000));
        rumble = widgets.getWidget(219).getChild(0).getChild(4);
        rumble.interact();
        sleep(Calculations.random(1000,2000));
        clicktocontinue = widgets.getWidget(231).getChild(3);
        clicktocontinue.interact();
        sleep(Calculations.random(1000,2000));
        clicktocontinue.interact();
        sleep(Calculations.random(1000,2000));
        agree = widgets.getWidget(219).getChild(0).getChild(1);
        boolean cond1 = agree.interact();

        //deposit gold in coffer
        getGameObjects().closest(COFFER_ID).interact();
        sleep(Calculations.random(3000,4000));
        clicktocontinue.interact();
        sleep(Calculations.random(700,1000));
        boolean cond2 = agree.interact();
        sleep(Calculations.random(1000,2000));
        getKeyboard().type(DEPOSIT);
        sleep(Calculations.random(700,1000));

        return cond1 && cond2;
    }

}

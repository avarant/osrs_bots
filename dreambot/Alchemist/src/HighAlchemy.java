
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.filter.Filter;
import org.dreambot.api.methods.magic.Normal;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.impl.Condition;
import org.dreambot.api.wrappers.items.Item;

import java.sql.Timestamp;


@ScriptManifest(category = Category.MAGIC, name = "Alchemist", author="redbandit",
        version=1.0, description = "high alchemy")

public class HighAlchemy extends AbstractScript {

    public static final int COIN = 561; //coin id
    public static final int RUNE = 561; //nature rune id

    private int item_id;

    @Override
    public void onStart(){
        item_id = 63;

        if(!getTabs().isOpen(Tab.MAGIC)) {
            getTabs().open(Tab.MAGIC);
        }

        log(new Timestamp(System.currentTimeMillis()).toString());
    }

    @Override
    public int onLoop() {   //3 second loop

        if(getInventory().count(RUNE) == 0) stop();

        sleep(Calculations.random(100,250));

        getMagic().castSpell(Normal.HIGH_LEVEL_ALCHEMY);

        sleepUntil(new Condition() {
            @Override
            public boolean verify() {
                return !getTabs().isOpen(Tab.MAGIC);
            }
        },3700);

        sleep(Calculations.random(200,400));

        getInventory().get(item_id).interact();

        sleepUntil(new Condition() {
            @Override
            public boolean verify() {
                return getTabs().isOpen(Tab.MAGIC);
            }
        },3700);

        return Calculations.random(700,1200);
    }

}

package me.ramonrpa.customquestviewer;

import fr.skytasul.quests.BeautyQuests;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CustomQuestManager {

    private Map<Player, CustomQuest> customQuests = new HashMap<>();

    public CustomQuest getPlayerCQuest(Player p){
        return customQuests.get(p);
    }

    public void removePlayerCQuest(Player p){
        if (customQuests.containsKey(p))  {
            customQuests.remove(p).cancel();
            sendEmptyQuest(p);
        }
    }

    public void create(Player p){
        removePlayerCQuest(p);
        CustomQuest quest = new CustomQuest(p);
        quest.run();
        customQuests.put(p, quest);
    }

    public void unload(){
        for (CustomQuest s : customQuests.values()){
            s.cancel();
            removePlayerCQuest(s.getPlayer());
        }
        if (!customQuests.isEmpty()) BeautyQuests.getInstance().getLogger().info(customQuests.size() + " customQuests deleted.");
    }

    private void sendEmptyQuest(Player p) {
        com.ramonrpa.customversion.api.Quest quest = new com.ramonrpa.customversion.api.Quest("", UUID.randomUUID(), p);
        quest.send();
    }
}

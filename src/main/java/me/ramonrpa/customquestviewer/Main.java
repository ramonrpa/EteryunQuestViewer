package me.ramonrpa.customquestviewer;


import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.players.events.PlayerAccountJoinEvent;
import fr.skytasul.quests.players.events.PlayerAccountLeaveEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {

    private CustomQuestManager customCustomQuestManager;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        customCustomQuestManager = new CustomQuestManager();
    }

    @EventHandler
    public void onAccountJoin(PlayerAccountJoinEvent e) {
        customCustomQuestManager.create(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        customCustomQuestManager.removePlayerCQuest(e.getPlayer());
    }

    public CustomQuestManager getCustomCustomQuestManager() { return customCustomQuestManager;}
}

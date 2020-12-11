package me.ramonrpa.customquestviewer;

import com.ramonrpa.customversion.api.Objective;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.events.*;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayerQuestDatas;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.structure.QuestBranch;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class CustomQuest extends BukkitRunnable implements Listener  {

    private PlayerAccount acc;
    private Player p;
    private Quest shown = null;
    private List<Quest> launched;
    private boolean hid = false;
    private int changeTime = 1;

    CustomQuest(Player player) {
        Bukkit.getPluginManager().registerEvents(this, BeautyQuests.getInstance());
        this.p = player;
        this.acc = PlayersManager.getPlayerAccount(p);

        launched = QuestsAPI.getQuestsStarteds(acc, true);

        super.runTaskTimerAsynchronously(BeautyQuests.getInstance(), 2L, 20L);
    }

    public void run() {
        try {
            if (!p.isOnline()) return;
            if (hid) return;

            if (launched.isEmpty()) {
                shown = null;
                sendEmptyQuest();
            }

            if (!launched.isEmpty()) {
                int lastID = launched.indexOf(shown);
                int id = lastID + 1;
                if (id >= launched.size() || lastID == -1) id = 0;
                if (lastID != id) {
                    shown = launched.get(id);
                }
                if (shown != null) {
                    QuestBranch questBranch = shown.getBranchesManager().getPlayerBranch(acc);

                    if (questBranch != null) {
                        Field asyncRewardField = questBranch.getClass().getDeclaredField("asyncReward");
                        asyncRewardField.setAccessible(true);
                        List<PlayerAccount> asyncReward = (List<PlayerAccount>) asyncRewardField.get(questBranch);

                        PlayerQuestDatas datas;

                        if (!acc.hasQuestDatas(shown) || (datas = acc.getQuestDatas(shown)).getBranch() != questBranch.getID()) {
                            sendEmptyQuest();
                        } else {
                            LinkedHashMap<AbstractStage, QuestBranch> endStages = questBranch.getEndingStages();

                            if (asyncReward.contains(acc))
                                sendEmptyQuest();
                            else {
                                com.ramonrpa.customversion.api.Quest quest = new com.ramonrpa.customversion.api.Quest(shown.getName(), UUID.randomUUID(), p);
                                if (datas.isInEndingStages()) {
                                    int i = 0;
                                    for (AbstractStage stage : endStages.keySet()) {
                                        boolean focused = datas.getStage() == i ? true : false;
                                        Objective obj = new Objective(UUID.randomUUID(), stage.getDescriptionLine(acc, QuestBranch.Source.SCOREBOARD), focused);
                                        quest.addObjective(obj);
                                        i++;
                                    }
                                } else {
                                    LinkedList<AbstractStage> regularStages = questBranch.getRegularStages();
                                    for (int i = datas.getStage(); i < regularStages.size(); i++) {
                                        boolean focused = datas.getStage() == i ? true : false;
                                        Objective obj = new Objective(UUID.randomUUID(), regularStages.get(i).getDescriptionLine(acc, QuestBranch.Source.SCOREBOARD), focused);
                                        quest.addObjective(obj);
                                    }
                                }
                                quest.send();
                            }
                        }
                    }
                } else {
                    sendEmptyQuest();
                }
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onQuestFinished(QuestFinishEvent e) {
        if (e.getPlayerAccount() == acc) questRemove(e.getQuest());
    }

    @EventHandler
    public void onStageReset(PlayerQuestResetEvent e) {
        if (e.getPlayerAccount() == acc) questRemove(e.getQuest());
    }

    @EventHandler
    public void onQuestRemove(QuestRemoveEvent e) {
        questRemove(e.getQuest());
    }

    @EventHandler
    public void onQuestCreate(QuestCreateEvent e) {
        if (e.isEdited()) {
            if (e.getQuest().hasStarted(acc)) launched.add(e.getQuest());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuestLaunch(QuestLaunchEvent e) {
        if (e.getPlayerAccount() == acc && e.getQuest().isScoreboardEnabled()) {
            launched.add(launched.indexOf(shown) + 1, e.getQuest());
            shown = e.getQuest();
        }
    }

    private void questRemove(Quest quest) {
        int id = launched.indexOf(quest);
        if (id == -1) return;
        launched.remove(quest);
        if (quest == shown) {
            if (!launched.isEmpty()) {
                shown = launched.get(id >= launched.size() ? 0 : id);
            } else shown = null;
            run();
        }
    }

    public Quest getShownQuest() {
        return shown;
    }

    private void sendEmptyQuest() {
        com.ramonrpa.customversion.api.Quest quest = new com.ramonrpa.customversion.api.Quest("", UUID.randomUUID(), p);
        quest.send();
    }

    public void hide(){
        hid = true;
        sendEmptyQuest();
    }

    public void show(){
        hid = false;
        run();
    }

    public Player getPlayer() {
        return p;
    }

    public void setShownQuest(Quest quest) {
        if (!launched.contains(quest)) throw new IllegalArgumentException("Quest is not running for player.");
        shown = quest;
        run();
    }
}

package com.kiddo.ui.home;

public class QuestItem {
    public String blockId;
    public String id;
    public String text;
    public int xp;
    public boolean done;
    public QuestItem(String blockId, String id, String text, int xp, boolean done) {
        this.blockId = blockId;
        this.id = id;
        this.text = text;
        this.xp = xp;
        this.done = done;
    }
}

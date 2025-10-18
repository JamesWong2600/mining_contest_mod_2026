package org.link_uuid.miningcontest.data.variable;

public class variable {
    public int session;
    public int player_amount;
    public int time;
    public int getSession() {
        return session;
    }
    public int getTime() {
        return time;
    }
    public int get_player_amount() {
        return player_amount;
    }

    // Setter
    public void setSession(int session) {
        this.session = session;
    }
    public void setTime(int time) {
        this.time = time;
    }
    public void set_player_amount(int player_amount) {
        this.player_amount = player_amount;
    }
}
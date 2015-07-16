package pl.droidsonroids.bootcamp.yo.model;

import android.support.annotation.NonNull;

import java.text.Collator;

public class User implements Comparable<User>{
    int id;
    String name;
    boolean sentNotification = false;

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public boolean isSentNotification() {
        return sentNotification;
    }

    public void setSentNotification(boolean value) {
        sentNotification = value;
    }

    @Override
    public int compareTo(@NonNull User user) {
        return Collator.getInstance().compare(name.toLowerCase(), user.getName().toLowerCase());
    }
}

package at.fhooe.mc.conconii;

import java.util.ArrayList;

/**
 * Customized observer pattern class
 *
 * @author Robsen & Gix
 */
public class Observable {
    private final ArrayList<Observer> mObservers = new ArrayList<>();

    /**
     * Called to register observers.
     * Can't hold the same observer multiple times.
     * @see Observer
     * @param observer The observer to register.
     */
    public void attach(Observer observer) {
        if (!mObservers.contains(observer))
            mObservers.add(observer);
    }

    /**
     * Called to unregister observers.
     * @see Observer
     * @param observer The observer to unregister.
     */
    public void detach(Observer observer) {
        mObservers.remove(observer);
    }

    /**
     * For generally notifying all observers.
     * @see Observer
     */
    public void notifyAllObservers() {
        for (int i = 0; i < mObservers.size(); i++) {
            mObservers.get(i).update();
        }
    }

    /**
     * Is called to notify all Observers with a specific message.
     * @see Observer
     * @param msg The specific notification message.
     */
    public void notifyAllObservers(String msg) {
        for (int i = 0; i < mObservers.size(); i++) {
            mObservers.get(i).update(msg);
        }
    }

}

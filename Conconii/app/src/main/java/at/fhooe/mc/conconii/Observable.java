package at.fhooe.mc.conconii;

import java.util.ArrayList;

/**
 * Customized observer pattern class
 *
 * @author Robsen & Gix
 */
public class Observable {
    private static final ArrayList<Observer> mObservers = new ArrayList<>();

    /**
     * Called to register observers.
     * Can't hold the same observer multiple times.
     *
     * @param observer The observer to register.
     * @see Observer
     */
    public void attach(Observer observer) {
        if (!mObservers.contains(observer))
            mObservers.add(observer);
    }

    /**
     * Called to unregister observers.
     *
     * @param observer The observer to unregister.
     * @see Observer
     */
    public void detach(Observer observer) {
        mObservers.remove(observer);
    }

    /**
     * For generally notifying all observers.
     *
     * @see Observer
     */
    public void notifyAllObservers() {
        for (int i = 0; i < mObservers.size(); i++) {
            mObservers.get(i).update();
        }
    }

    /**
     * Is called to notify all Observers with a specific message.
     *
     * @param msg The specific notification message.
     * @see Observer
     */
    public void notifyAllObservers(String msg) {
        for (int i = 0; i < mObservers.size(); i++) {
            mObservers.get(i).update(msg);
        }
    }

}

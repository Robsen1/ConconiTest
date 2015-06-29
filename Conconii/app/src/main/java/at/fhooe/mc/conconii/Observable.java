package at.fhooe.mc.conconii;

import java.util.ArrayList;

/**
 * Created by Robsen & Gix
 * Class for Observer Pattern
 */
public class Observable {
    private final ArrayList<Observer> mObservers = new ArrayList<>();

    public void attach(Observer observer) {
        if (!mObservers.contains(observer))
            mObservers.add(observer);
    }

    public void detach(Observer observer) {
        mObservers.remove(observer);
    }

    // reserved for future uses
    public void notifyAllObservers() {
        for (int i = 0; i < mObservers.size(); i++) {
            mObservers.get(i).update();
        }
    }

    public void notifyAllObservers(String msg) {
        for (int i = 0; i < mObservers.size(); i++) {
            mObservers.get(i).update(msg);
        }
    }

}

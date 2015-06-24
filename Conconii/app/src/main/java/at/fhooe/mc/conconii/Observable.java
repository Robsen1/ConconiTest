package at.fhooe.mc.conconii;

import java.util.ArrayList;

/**
 * Created by Robsen & Gix
 */
public class Observable {
    private ArrayList<Observer> mObservers = new ArrayList<>();

    public void attach(Observer observer){
        mObservers.add(observer);
    }

    public void detach(Observer observer){
        mObservers.add(observer);
    }

    public void notifyAllObservers(){
        for(int i=0;i<mObservers.size();i++){
            mObservers.get(i).update();
        }
    }
    public void notifyAllObservers(String msg){
        for(int i=0;i<mObservers.size();i++){
            mObservers.get(i).update(msg);
        }
    }

}

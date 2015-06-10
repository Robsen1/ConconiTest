package at.fhooe.mc.conconii;

import java.util.ArrayList;

/**
 * Created by Robsen & Gix
 */
public class DataManager {
    private static ArrayList<ActualData> mDataList = new ArrayList<>();

    public static ArrayList<ActualData> getDataList(){
        return mDataList;
    }

    public static void addData(ActualData newData){
        mDataList.add(newData);
    }

    //get bluetooth data

    //get GPS data
}

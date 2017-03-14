package marco.mrm.trybenchmarks.sqlite;


import android.database.sqlite.*;
import android.database.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by marco on 03/03/2017.
 */

public class DataSet {
    String headers[]=null;
    int SQLTypes[]=null;
    List<Object[]> rows=new ArrayList<Object[]>();

    public static DataSet createRandomly(int numRows,int numColsNumeric,int numColsString,int sizeColString){
        DataSet dataSet=new DataSet();
        Random  random=new Random();


        int numCols=numColsNumeric+numColsString;
        dataSet.headers=new String[numCols];
        dataSet.SQLTypes=new int[numCols];

        Object[] sampleRow=new Object[numCols];

//I calculate  values o be inserted into columns
        StringBuilder strb=new StringBuilder("");
        for(int i=0;i<sizeColString;i++)strb.append('A');
        String stringVal=strb.toString();
        int numVal= random.nextInt();

        for(int i=0;i<numColsNumeric;i++){
            dataSet.headers[i]="COL_NUMERIC_"+i;
            dataSet.SQLTypes[i]=SQLiteCursor.FIELD_TYPE_INTEGER;
            sampleRow[i]=(Integer)numVal;
        }
        for(int i=0;i<numColsString;i++){
            dataSet.headers[i+numColsNumeric]="COL_STRING_"+i;
            dataSet.SQLTypes[i+numColsNumeric]=SQLiteCursor.FIELD_TYPE_STRING;
            sampleRow[i+numColsNumeric]=stringVal;
        }


        for(int i=0;i<numRows;i++)dataSet.rows.add(sampleRow);

         return dataSet;
    }

}

package marco.mrm.trybenchmarks.sqlite;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.*;

/**
 * Created by marco on 03/03/2017.
 */

public class SQLiteHelp {
    public static SQLiteDatabase openDatabase(String name){

        return SQLiteDatabase.openOrCreateDatabase(name,null);
    }
    public static void createTable(SQLiteDatabase database, String tableName,DataSet dataSet){
        StringBuilder query=new StringBuilder();
        query.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append(" ( ");
        for(int i=0;i<dataSet.headers.length;i++){
            query.append(dataSet.headers[i]);
            switch (dataSet.SQLTypes[i]){
                case SQLiteCursor.FIELD_TYPE_INTEGER:
                    query.append(" INTEGER ");break;
                case SQLiteCursor.FIELD_TYPE_STRING:
                    query.append(" TEXT ");break;

            }
            if (i!=(dataSet.headers.length-1)){
                query.append(',');
            }
        }
        query.append(")");
        database.execSQL(query.toString());
    }
    public static void dropTable(SQLiteDatabase database, String tableName){
        try {
            database.execSQL("DROP TABLE " + tableName);
        }catch (Exception e){}
    }
}

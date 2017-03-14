package marco.mrm.trybenchmarks.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.*;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import marco.mrm.trybenchmarks.MainActivity;

/**
 * Created by marco on 03/03/2017.
 */
//http://www.javaperformancetuning.com/tips/jdbc.shtml#REF1
public class SQLBenchmark implements Runnable {
    private Context context=null;
    static final String DEBUG_TAG_SQLITE="BENCH_SQLITE";
    static final String TABLE_BENCH="TABLE_BENCH";
    static  String pathLogFile="";
    static BufferedWriter fwLogFile;
    public SQLBenchmark(Context context){
        this.context=context;
    }
    public  void run(){
        try {

            String pathDB = context.getExternalFilesDir(null) + "/prove.db3";
            pathLogFile=context.getExternalFilesDir(null) + "/Log_" + (new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()))+".txt";
            fwLogFile =new BufferedWriter(new FileWriter(pathLogFile));

          //  Toast.makeText(context,"Bench started",Toast.LENGTH_SHORT).show();
            LogPerform("Benchmark started");


            SQLiteDatabase database = SQLiteHelp.openDatabase(pathDB);
            SQLiteHelp.dropTable(database,TABLE_BENCH);
            performSetBenchmarks(database,10000,50,0,0);
            performSetBenchmarks(database,10000,100,0,0);
            performSetBenchmarks(database,10000,0,50,10);
            performSetBenchmarks(database,10000,0,50,100);
            performSetBenchmarks(database,10000,0,100,10);
            performSetBenchmarks(database,10000,0,100,100);
            performSetBenchmarks(database,5000,50,50,10);
            performSetBenchmarks(database,5000,50,50,100);

         //   Toast.makeText(context,"Bench finished",Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            Log.e("",e.getMessage());

        }
        LogPerform("----------------");
        LogPerform("Benchmark finished");
        try{
            fwLogFile.close();
        }catch (Exception e){}
    }
    public  void performSetBenchmarks (SQLiteDatabase database,int numRows,int numColsNumeric,int numColsString,int sizeColString)throws Exception{
        DataSet dataSet = DataSet.createRandomly(numRows,numColsNumeric,numColsString,sizeColString);  //(5000,40,0,1);

        SQLiteHelp.createTable(database, TABLE_BENCH, dataSet);

        long startTime,endTime;

        LogPerform("----------------");
        LogPerform("N rows:" + numRows + " \tN cols(numeric):" + numColsNumeric + " \tN cols(string):" + numColsString + " \tLength strings:" + sizeColString );
        //InsertDataset[] arrayInserter=new InsertDataset[]{new SimpleInsertion(),new CompiledInsertion(),new CompiledInsertion_groupedRows()};
        for(InsertDataset objInsert:new InsertDataset[]{new SimpleInsertion(),new CompiledInsertion(),new CompiledInsertion_groupedRows()}){
            performSingleBenchmark(objInsert,database,dataSet);
            System.gc ();
            System.runFinalization ();
        }

        SQLiteHelp.dropTable(database,TABLE_BENCH);
        Thread.sleep(300);
    }

    private void performSingleBenchmark(InsertDataset objInsertion, SQLiteDatabase database,DataSet dataSet)throws Exception{
        database.execSQL("DELETE FROM "+TABLE_BENCH);
        Thread.sleep(500);
        long startTime,endTime;
        startTime=System.currentTimeMillis();
        objInsertion.insert(database,TABLE_BENCH,dataSet);
        endTime=System.currentTimeMillis();
        LogPerform("Time " +objInsertion.getClass().getSimpleName() + ": "+(endTime-startTime)+" ms");
    }

    public interface InsertDataset{
        public void insert(SQLiteDatabase database,String tableName,DataSet dataSet);
    }
    public static void  LogPerform(String str){
        Log.d(DEBUG_TAG_SQLITE,str);
        MainActivity.addText(str);
        try {
            fwLogFile.write(str+"\n");
            fwLogFile.flush();
        }catch (Exception e){}
    }
     class SimpleInsertion implements InsertDataset {
         public void insert(SQLiteDatabase database, String tableName, DataSet dataSet) {
            try {
                database.beginTransaction();

                for (int i = 0; i < dataSet.rows.size(); i++) {
                    StringBuilder query = new StringBuilder("INSERT INTO ").append(tableName).append(" ( ");
                    for (int j = 0; j < dataSet.headers.length; j++) {
                        query.append(dataSet.headers[j]);
                        if (j != (dataSet.headers.length - 1)) query.append(",");
                    }
                    query.append(") VALUES (");
                    for (int j = 0; j < dataSet.headers.length; j++) {
                        if (dataSet.SQLTypes[j] != SQLiteCursor.FIELD_TYPE_INTEGER)
                            query.append("'");
                        query.append(dataSet.rows.get(i)[j]);
                        if (dataSet.SQLTypes[j] != SQLiteCursor.FIELD_TYPE_INTEGER)
                            query.append("'");
                        if (j != (dataSet.headers.length - 1)) query.append(",");
                    }
                    query.append(")");
                    database.execSQL(query.toString());
                }
                database.setTransactionSuccessful();
            } catch (Exception e) {
                throw e;
            } finally {
                database.endTransaction();
            }
        }
    };
    class CompiledInsertion implements InsertDataset {
        public void insert(SQLiteDatabase database, String tableName, DataSet dataSet) {
            try {
                StringBuilder query = new StringBuilder("INSERT INTO ").append(tableName).append(" ( ");
                for (int j = 0; j < dataSet.headers.length; j++) {
                    query.append(dataSet.headers[j]);
                    if (j != (dataSet.headers.length - 1)) query.append(",");
                }
                query.append(") VALUES (");
                for (int j = 0; j < dataSet.headers.length; j++) {
                    query.append(" ? ");
                    if (j != (dataSet.headers.length - 1)) query.append(",");
                }
                query.append(" )");
                database.beginTransaction();
                SQLiteStatement stmt = database.compileStatement(query.toString());

                for (int i = 0; i < dataSet.rows.size(); i++) {
//!! bindings indicies starts from 1 !!
                    stmt.clearBindings();
                /*for (int j = 0; j < dataSet.headers.length; j++) {
                    if (dataSet.SQLTypes[j] == SQLiteCursor.FIELD_TYPE_INTEGER){
                        long num;
                        if (dataSet.rows.get(i)[j] instanceof Integer){
                            num=(long)(int)dataSet.rows.get(i)[j];
                        }else if(dataSet.rows.get(i)[j] instanceof Integer) {
                            num = (long) dataSet.rows.get(i)[j];
                        }else{
                            num=Long.parseLong((String)dataSet.rows.get(i)[j]);
                        }
                        stmt.bindLong(j + 1, num);
                    }

                    else if (dataSet.SQLTypes[j] == SQLiteCursor.FIELD_TYPE_STRING)
                        stmt.bindString(j + 1, (String) dataSet.rows.get(i)[j]);
                }*/
                    bindRow(stmt, dataSet.rows.get(i), dataSet.SQLTypes);
                    stmt.execute();

                }
                database.setTransactionSuccessful();
            } catch (Exception e) {
                throw e;
            } finally {
                database.endTransaction();
            }
        }
    }

    /**
     * I put several record on same insertion (the is an hard limit of 999 fields per query, each query can insert 999/#(N fields)
     */
    class CompiledInsertion_groupedRows implements InsertDataset {
        public void insert(SQLiteDatabase database, String tableName, DataSet dataSet) {
            try {

                StringBuilder query = new StringBuilder("INSERT INTO ").append(tableName).append(" ( ");
                for (int j = 0; j < dataSet.headers.length; j++) {
                    query.append(dataSet.headers[j]);
                    if (j != (dataSet.headers.length - 1)) query.append(",");
                }
                query.append(") VALUES (");
                for (int j = 0; j < dataSet.headers.length; j++) {
                    query.append(" ? ");
                    if (j != (dataSet.headers.length - 1)) query.append(",");
                }
                query.append(" )");

                int numQueriesGrouped = 999 / (Math.max(1, dataSet.headers.length));
                StringBuilder queryGrouped = new StringBuilder("INSERT INTO ").append(tableName).append(" ( ");
                for (int j = 0; j < dataSet.headers.length; j++) {
                    queryGrouped.append(dataSet.headers[j]);
                    if (j != (dataSet.headers.length - 1)) queryGrouped.append(",");
                }
                queryGrouped.append(") VALUES ");
                for (int i = 0; i < numQueriesGrouped; i++) {
                    queryGrouped.append(" ( ");
                    for (int j = 0; j < dataSet.headers.length; j++) {
                        queryGrouped.append(" ? ");
                        if (j != (dataSet.headers.length - 1)) queryGrouped.append(",");
                    }
                    queryGrouped.append(" )");
                    if (i != (numQueriesGrouped - 1)) queryGrouped.append(" , ");
                }


                database.beginTransaction();
                SQLiteStatement stmt = database.compileStatement(query.toString());
                SQLiteStatement stmtGrouped = database.compileStatement(queryGrouped.toString());

                int iRow = 0;
                for (; iRow < (dataSet.rows.size() - numQueriesGrouped + 1); iRow += numQueriesGrouped) {
//!! bindings indicies starts from 1 !!
                    stmtGrouped.clearBindings();
                    for (int j = 0; j < numQueriesGrouped; j++) {
                        bindRow(stmtGrouped, dataSet.rows.get(iRow + j), dataSet.SQLTypes, 1 + dataSet.headers.length * j);
                    }
                    stmtGrouped.execute();
                }
                for (; iRow < (dataSet.rows.size()); iRow++) {
//!! bindings indicies starts from 1 !!
                    stmt.clearBindings();
                    bindRow(stmt, dataSet.rows.get(iRow), dataSet.SQLTypes);
                    stmt.execute();
                }
                database.setTransactionSuccessful();
            } catch (Exception e) {
                throw e;
            } finally {
                database.endTransaction();
            }
        }
    }
    protected static void bindRow(SQLiteStatement stmt,Object[] row, int[] SQLTypes){
        bindRow(stmt,row,SQLTypes,1);
    }
    protected static void bindRow(SQLiteStatement stmt,Object[] row, int[] SQLTypes,int offsetBinding){
        for (int j = 0; j < SQLTypes.length; j++) {
            if (SQLTypes[j] == SQLiteCursor.FIELD_TYPE_INTEGER){
                long num;
                if (row[j] instanceof Integer){
                    num=(long)(int)row[j];
                }else if(row[j] instanceof Integer) {
                    num = (long) row[j];
                }else{
                    num=Long.parseLong((String)row[j]);
                }
                stmt.bindLong(j + offsetBinding, num);
            }

            else if (SQLTypes[j] == SQLiteCursor.FIELD_TYPE_STRING)
                stmt.bindString(j + offsetBinding, (String) row[j]);

        }
    }
}

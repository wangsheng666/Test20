package com.example.a123.test20;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    WordsDBHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        //为ListView注册上下文菜单
        ListView list = (ListView) findViewById(R.id.list);
        registerForContextMenu(list);


        //创建SQLiteOpenHelper对象，注意第一次运行时，此时数据库并没有被创建
        mDbHelper = new WordsDBHelper(this);

        //在列表显示全部单词
        ArrayList<Map<String, String>> items=getAll();
        setWordsListView(items);

    }
    public ArrayList<Map<String,String>> getAll(){
        ArrayList<Map<String,String>> items=new ArrayList<Map<String, String>>();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String sql="select * from words order by word desc";
        Cursor c=db.rawQuery(sql,null);
        while(c.moveToNext()){
            Map<String, String> item = new HashMap<String, String>();
            item.put(Words.Word.COLUMN_NAME_WORD, c.getString(c.getColumnIndex(Words.Word.COLUMN_NAME_WORD)));
            item.put(Words.Word.COLUMN_NAME_MEANING,c.getString(c.getColumnIndex(Words.Word.COLUMN_NAME_MEANING)));
            item.put(Words.Word.COLUMN_NAME_SAMPLE,c.getString(c.getColumnIndex(Words.Word.COLUMN_NAME_SAMPLE)));
            Log.i("Test", c.getString(c.getColumnIndex(Words.Word.COLUMN_NAME_WORD))+" "+c.getString(c.getColumnIndex(Words.Word.COLUMN_NAME_MEANING))+" "+
                    c.getString(c.getColumnIndex(Words.Word.COLUMN_NAME_SAMPLE)));
            items.add(item);
        }
        return items;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDbHelper.close();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add_find, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {

            case R.id.add_menu:
                //新增单词
                InsertDialog();
                return true;
        }


        return super.onOptionsItemSelected(item);
    }
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.delete_alter, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        TextView textId=null;
        TextView textWord=null;
        TextView textMeaning=null;
        TextView textSample=null;

        AdapterView.AdapterContextMenuInfo info=null;
        View itemView=null;

        switch (item.getItemId()){
            case R.id.delete_menu:
                //删除单词
                info=(AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
                itemView=info.targetView;
                textId =(TextView)itemView.findViewById(R.id.word);
                if(textId!=null){
                    String strId=textId.getText().toString();
                    DeleteDialog(strId);
                }
                break;
            case R.id.change_menu:
                //修改单词
                info=(AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
                itemView=info.targetView;
                textWord =(TextView)itemView.findViewById(R.id.word);
                textMeaning =(TextView)itemView.findViewById(R.id.meaning);
                textSample =(TextView)itemView.findViewById(R.id.example);
                if(textWord!=null && textMeaning!=null && textSample!=null){

                    String strWord=textWord.getText().toString();
                    String strMeaning=textMeaning.getText().toString();
                    String strSample=textSample.getText().toString();
                    UpdateDialog( strWord, strMeaning, strSample);
                }
                break;
        }
        return true;
    }

    //设置适配器，在列表中显示单词
    private void setWordsListView(ArrayList<Map<String, String>> items){
        SimpleAdapter adapter = new SimpleAdapter(this, items, R.layout.item,
                new String[]{Words.Word.COLUMN_NAME_WORD, Words.Word.COLUMN_NAME_MEANING, Words.Word.COLUMN_NAME_SAMPLE},
                new int[]{R.id.word, R.id.meaning, R.id.example});

        ListView list = (ListView) findViewById(R.id.list);
        list.setAdapter(adapter);
    }

    //使用Sql语句插入单词
    private void InsertUserSql(String strWord, String strMeaning, String strSample){
        String sql="insert into  words(word,meaning,sample) values(?,?,?)";

        //Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.execSQL(sql,new String[]{strWord,strMeaning,strSample});
    }

    //使用insert方法增加单词
    private void Insert(String strWord, String strMeaning, String strSample) {

        //Gets the data repository in write mode*/
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(Words.Word.COLUMN_NAME_WORD, strWord);
        values.put(Words.Word.COLUMN_NAME_MEANING, strMeaning);
        values.put(Words.Word.COLUMN_NAME_SAMPLE, strSample);

        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                Words.Word.TABLE_NAME,
                null,
                values);
    }


    //新增对话框
    private void InsertDialog() {
        final TableLayout tableLayout = (TableLayout) getLayoutInflater().inflate(R.layout.add_change_layout, null);
        new AlertDialog.Builder(this)
                .setTitle("新增单词")//标题
                .setView(tableLayout)//设置视图
                //确定按钮及其动作
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String strWord=((EditText)tableLayout.findViewById(R.id.txtWord)).getText().toString();
                        String strMeaning=((EditText)tableLayout.findViewById(R.id.txtMeaning)).getText().toString();
                        String strSample=((EditText)tableLayout.findViewById(R.id.txtExample)).getText().toString();

                        //既可以使用Sql语句插入，也可以使用使用insert方法插入
                        // InsertUserSql(strWord, strMeaning, strSample);
                        Insert(strWord, strMeaning, strSample);

                        ArrayList<Map<String, String>> items=getAll();
                        setWordsListView(items);

                    }
                })
                //取消按钮及其动作
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create()//创建对话框
                .show();//显示对话框


    }

    //使用Sql语句删除单词
    private void DeleteUseSql(String strId) {
        String sql="delete from words where _id='"+strId+"'";

        //Gets the data repository in write mode*/
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        db.execSQL(sql);
    }

    //删除单词
    private void Delete(String strId) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        // 定义where子句
        String selection = Words.Word._ID + " = ?";

        // 指定占位符对应的实际参数
        String[] selectionArgs = {strId};

        // Issue SQL statement.
        db.delete(Words.Word.TABLE_NAME, selection, selectionArgs);
    }


    //删除对话框
    private void DeleteDialog(final String strId){
        new AlertDialog.Builder(this).setTitle("删除单词").setMessage("是否真的删除单词?").setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //既可以使用Sql语句删除，也可以使用使用delete方法删除
                DeleteUseSql(strId);
                //Delete(strId);
                setWordsListView(getAll());
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }).create().show();
    }

    //使用Sql语句更新单词
    private void UpdateUseSql(String strId,String strWord, String strMeaning, String strSample) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String sql="update words set word=?,meaning=?,sample=? where _id=?";
        db.execSQL(sql, new String[]{strWord, strMeaning, strSample,strId});
    }

    //使用方法更新
    private void Update(String strId,String strWord, String strMeaning, String strSample) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();


        // New value for one column
        ContentValues values = new ContentValues();
        values.put(Words.Word.COLUMN_NAME_WORD, strWord);
        values.put(Words.Word.COLUMN_NAME_MEANING, strMeaning);
        values.put(Words.Word.COLUMN_NAME_SAMPLE, strSample);

        String selection = Words.Word._ID + " = ?";
        String[] selectionArgs = {strId};

        int count = db.update(
                Words.Word.TABLE_NAME,
                values,
                selection,
                selectionArgs);
    }

    //修改对话框
    private void UpdateDialog( final String strWord, final String strMeaning, final String strSample) {
        final TableLayout tableLayout = (TableLayout) getLayoutInflater().inflate(R.layout.add_change_layout, null);
        ((EditText)tableLayout.findViewById(R.id.txtWord)).setText(strWord);
        ((EditText)tableLayout.findViewById(R.id.txtMeaning)).setText(strMeaning);
        ((EditText)tableLayout.findViewById(R.id.txtExample)).setText(strSample);
        new AlertDialog.Builder(this)
                .setTitle("修改单词")//标题
                .setView(tableLayout)//设置视图
                //确定按钮及其动作
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String strNewWord = ((EditText) tableLayout.findViewById(R.id.txtWord)).getText().toString();
                        String strNewMeaning = ((EditText) tableLayout.findViewById(R.id.txtMeaning)).getText().toString();
                        String strNewSample = ((EditText) tableLayout.findViewById(R.id.txtExample)).getText().toString();

                        //既可以使用Sql语句更新，也可以使用使用update方法更新

                        //  Update(strId, strNewWord, strNewMeaning, strNewSample);
                        setWordsListView(getAll());
                    }
                })
                //取消按钮及其动作
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create()//创建对话框
                .show();//显示对话框


    }




}

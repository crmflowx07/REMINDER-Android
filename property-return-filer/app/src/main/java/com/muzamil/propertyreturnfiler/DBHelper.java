package com.muzamil.propertyreturnfiler;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
public class DBHelper extends SQLiteOpenHelper{public DBHelper(Context c){super(c,"property_return_filer.db",null,1);}public void onCreate(SQLiteDatabase db){db.execSQL("CREATE TABLE clients(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT,whatsapp TEXT,nextDue TEXT)");}public void onUpgrade(SQLiteDatabase db,int a,int b){}}

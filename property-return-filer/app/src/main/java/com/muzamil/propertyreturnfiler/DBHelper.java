package com.muzamil.propertyreturnfiler;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;
import java.util.*;

public class DBHelper extends SQLiteOpenHelper {
    public static final int VERSION = 3;
    public DBHelper(Context c){ super(c,"property_return_filer.db",null,VERSION); }

    @Override public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE clients(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,whatsapp TEXT,phone TEXT,cnic TEXT,ntn TEXT,business TEXT,taxType TEXT,status TEXT,nextDue TEXT,email TEXT,address TEXT,notes TEXT,createdAt INTEGER)");
        db.execSQL("CREATE TABLE filings(id INTEGER PRIMARY KEY AUTOINCREMENT,clientId INTEGER,month TEXT,year INTEGER,type TEXT,dueDate TEXT,status TEXT,filedDate TEXT,notes TEXT)");
        db.execSQL("CREATE TABLE reminders(id INTEGER PRIMARY KEY AUTOINCREMENT,clientId INTEGER,title TEXT,message TEXT,scheduledAt INTEGER,repeatRule TEXT,status TEXT,channel TEXT)");
        db.execSQL("CREATE TABLE payments(id INTEGER PRIMARY KEY AUTOINCREMENT,clientId INTEGER,title TEXT,amount REAL,dueDate TEXT,status TEXT,notes TEXT)");
        db.execSQL("CREATE TABLE documents(id INTEGER PRIMARY KEY AUTOINCREMENT,clientId INTEGER,title TEXT,category TEXT,status TEXT,notes TEXT)");
        seed(db);
    }

    @Override public void onUpgrade(SQLiteDatabase db,int oldV,int newV){
        db.execSQL("DROP TABLE IF EXISTS documents");
        db.execSQL("DROP TABLE IF EXISTS payments");
        db.execSQL("DROP TABLE IF EXISTS reminders");
        db.execSQL("DROP TABLE IF EXISTS filings");
        db.execSQL("DROP TABLE IF EXISTS clients");
        onCreate(db);
    }

    private long seedClient(SQLiteDatabase db,String n,String w,String b,String type,String due){
        ContentValues v=new ContentValues();v.put("name",n);v.put("whatsapp",w);v.put("phone",w);v.put("business",b);v.put("taxType",type);v.put("status","Active");v.put("nextDue",due);v.put("createdAt",System.currentTimeMillis());return db.insert("clients",null,v);
    }
    private void seed(SQLiteDatabase db){
        long a=seedClient(db,"Ali Traders","+923001234567","Ali Traders","Sales Tax","30 Aug 2026");
        long b=seedClient(db,"Khan Property Solutions","+923117654321","Khan Property Solutions","Income Tax","15 Sep 2026");
        long c=seedClient(db,"Muzamil Estate Services","+923221112233","Muzamil Estate Services","Both","30 Sep 2026");
        addFiling(db,a,"August",2026,"Sales Tax","30 Aug 2026","Pending");
        addFiling(db,b,"September",2026,"Income Tax","15 Sep 2026","Waiting Documents");
        addFiling(db,c,"September",2026,"Both","30 Sep 2026","Pending");
        addReminder(db,a,"August return reminder","Kindly August return documents provide kar dein.",System.currentTimeMillis()+86400000L,"Monthly","Scheduled","WhatsApp");
    }

    private void addFiling(SQLiteDatabase db,long clientId,String month,int year,String type,String due,String status){ContentValues v=new ContentValues();v.put("clientId",clientId);v.put("month",month);v.put("year",year);v.put("type",type);v.put("dueDate",due);v.put("status",status);db.insert("filings",null,v);}
    private void addReminder(SQLiteDatabase db,long clientId,String title,String message,long at,String repeat,String status,String channel){ContentValues v=new ContentValues();v.put("clientId",clientId);v.put("title",title);v.put("message",message);v.put("scheduledAt",at);v.put("repeatRule",repeat);v.put("status",status);v.put("channel",channel);db.insert("reminders",null,v);}

    public long saveClient(long id,String name,String whatsapp,String phone,String cnic,String ntn,String business,String taxType,String status,String nextDue,String email,String address,String notes){
        ContentValues v=new ContentValues();v.put("name",name);v.put("whatsapp",whatsapp);v.put("phone",phone);v.put("cnic",cnic);v.put("ntn",ntn);v.put("business",business);v.put("taxType",taxType);v.put("status",status);v.put("nextDue",nextDue);v.put("email",email);v.put("address",address);v.put("notes",notes);v.put("createdAt",System.currentTimeMillis());
        if(id>0){getWritableDatabase().update("clients",v,"id=?",new String[]{String.valueOf(id)});return id;}return getWritableDatabase().insert("clients",null,v);
    }
    public void deleteClient(long id){SQLiteDatabase d=getWritableDatabase();d.delete("filings","clientId=?",new String[]{""+id});d.delete("reminders","clientId=?",new String[]{""+id});d.delete("payments","clientId=?",new String[]{""+id});d.delete("documents","clientId=?",new String[]{""+id});d.delete("clients","id=?",new String[]{""+id});}

    public Client client(long id){Cursor c=getReadableDatabase().rawQuery("SELECT id,name,whatsapp,phone,cnic,ntn,business,taxType,status,nextDue,email,address,notes FROM clients WHERE id=?",new String[]{""+id});Client x=null;if(c.moveToFirst())x=fromClient(c);c.close();return x;}
    private Client fromClient(Cursor c){return new Client(c.getLong(0),c.getString(1),c.getString(2),c.getString(3),c.getString(4),c.getString(5),c.getString(6),c.getString(7),c.getString(8),c.getString(9),c.getString(10),c.getString(11),c.getString(12));}
    public List<Client> clients(String q){ArrayList<Client> out=new ArrayList<>();String like="%"+(q==null?"":q)+"%";Cursor c=getReadableDatabase().rawQuery("SELECT id,name,whatsapp,phone,cnic,ntn,business,taxType,status,nextDue,email,address,notes FROM clients WHERE name LIKE ? OR business LIKE ? OR whatsapp LIKE ? OR ntn LIKE ? ORDER BY id DESC",new String[]{like,like,like,like});while(c.moveToNext())out.add(fromClient(c));c.close();return out;}
    public int countClients(){return scalar("SELECT COUNT(*) FROM clients");}
    public int countPending(){return scalar("SELECT COUNT(*) FROM filings WHERE status!='Filed' AND status!='Completed'");}
    public int countFiled(){return scalar("SELECT COUNT(*) FROM filings WHERE status='Filed' OR status='Completed'");}
    public int countReminders(){return scalar("SELECT COUNT(*) FROM reminders WHERE status='Scheduled'");}
    private int scalar(String sql){Cursor c=getReadableDatabase().rawQuery(sql,null);c.moveToFirst();int v=c.getInt(0);c.close();return v;}

    public long addFiling(long clientId,String month,int year,String type,String due,String status,String notes){ContentValues v=new ContentValues();v.put("clientId",clientId);v.put("month",month);v.put("year",year);v.put("type",type);v.put("dueDate",due);v.put("status",status);v.put("notes",notes);return getWritableDatabase().insert("filings",null,v);}
    public void setFilingStatus(long id,String status){ContentValues v=new ContentValues();v.put("status",status);if("Filed".equals(status)||"Completed".equals(status))v.put("filedDate",new java.text.SimpleDateFormat("dd MMM yyyy",Locale.US).format(new Date()));getWritableDatabase().update("filings",v,"id=?",new String[]{""+id});}
    public List<Filing> filings(long clientId){ArrayList<Filing> l=new ArrayList<>();Cursor c=getReadableDatabase().rawQuery("SELECT id,month,year,type,dueDate,status,filedDate,notes FROM filings WHERE clientId=? ORDER BY year DESC,id DESC",new String[]{""+clientId});while(c.moveToNext())l.add(new Filing(c.getLong(0),c.getString(1),c.getInt(2),c.getString(3),c.getString(4),c.getString(5),c.getString(6),c.getString(7)));c.close();return l;}

    public long addReminder(long clientId,String title,String message,long at,String repeat,String channel){ContentValues v=new ContentValues();v.put("clientId",clientId);v.put("title",title);v.put("message",message);v.put("scheduledAt",at);v.put("repeatRule",repeat);v.put("status","Scheduled");v.put("channel",channel);return getWritableDatabase().insert("reminders",null,v);}
    public List<Reminder> reminders(long clientId){ArrayList<Reminder> l=new ArrayList<>();String where=clientId>0?" WHERE r.clientId="+clientId:"";Cursor c=getReadableDatabase().rawQuery("SELECT r.id,r.clientId,c.name,r.title,r.message,r.scheduledAt,r.repeatRule,r.status,r.channel FROM reminders r LEFT JOIN clients c ON c.id=r.clientId"+where+" ORDER BY r.scheduledAt ASC",null);while(c.moveToNext())l.add(new Reminder(c.getLong(0),c.getLong(1),c.getString(2),c.getString(3),c.getString(4),c.getLong(5),c.getString(6),c.getString(7),c.getString(8)));c.close();return l;}
    public void deleteReminder(long id){getWritableDatabase().delete("reminders","id=?",new String[]{""+id});}

    public long addPayment(long clientId,String title,double amount,String due,String status,String notes){ContentValues v=new ContentValues();v.put("clientId",clientId);v.put("title",title);v.put("amount",amount);v.put("dueDate",due);v.put("status",status);v.put("notes",notes);return getWritableDatabase().insert("payments",null,v);}
    public List<Payment> payments(long clientId){ArrayList<Payment> l=new ArrayList<>();Cursor c=getReadableDatabase().rawQuery("SELECT id,title,amount,dueDate,status,notes FROM payments WHERE clientId=? ORDER BY id DESC",new String[]{""+clientId});while(c.moveToNext())l.add(new Payment(c.getLong(0),c.getString(1),c.getDouble(2),c.getString(3),c.getString(4),c.getString(5)));c.close();return l;}

    public long addDocument(long clientId,String title,String category,String status,String notes){ContentValues v=new ContentValues();v.put("clientId",clientId);v.put("title",title);v.put("category",category);v.put("status",status);v.put("notes",notes);return getWritableDatabase().insert("documents",null,v);}
    public List<Document> documents(long clientId){ArrayList<Document> l=new ArrayList<>();Cursor c=getReadableDatabase().rawQuery("SELECT id,title,category,status,notes FROM documents WHERE clientId=? ORDER BY id DESC",new String[]{""+clientId});while(c.moveToNext())l.add(new Document(c.getLong(0),c.getString(1),c.getString(2),c.getString(3),c.getString(4)));c.close();return l;}

    public static class Client{public long id;public String name,whatsapp,phone,cnic,ntn,business,taxType,status,nextDue,email,address,notes;Client(long id,String name,String whatsapp,String phone,String cnic,String ntn,String business,String taxType,String status,String nextDue,String email,String address,String notes){this.id=id;this.name=name;this.whatsapp=whatsapp;this.phone=phone;this.cnic=cnic;this.ntn=ntn;this.business=business;this.taxType=taxType;this.status=status;this.nextDue=nextDue;this.email=email;this.address=address;this.notes=notes;}}
    public static class Filing{public long id;public String month,type,due,status,filed,notes;public int year;Filing(long i,String m,int y,String t,String d,String s,String f,String n){id=i;month=m;year=y;type=t;due=d;status=s;filed=f;notes=n;}}
    public static class Reminder{public long id,clientId,at;public String client,title,message,repeat,status,channel;Reminder(long i,long c,String cn,String t,String m,long a,String r,String s,String ch){id=i;clientId=c;client=cn;title=t;message=m;at=a;repeat=r;status=s;channel=ch;}}
    public static class Payment{public long id;public String title,due,status,notes;public double amount;Payment(long i,String t,double a,String d,String s,String n){id=i;title=t;amount=a;due=d;status=s;notes=n;}}
    public static class Document{public long id;public String title,category,status,notes;Document(long i,String t,String c,String s,String n){id=i;title=t;category=c;status=s;notes=n;}}
}
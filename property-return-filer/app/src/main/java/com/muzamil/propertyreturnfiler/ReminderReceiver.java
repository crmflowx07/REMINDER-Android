package com.muzamil.propertyreturnfiler;
import android.app.*;import android.content.*;import android.os.Build;
public class ReminderReceiver extends BroadcastReceiver{
 public static final String CHANNEL="fbr_reminders";
 @Override public void onReceive(Context c,Intent i){
  NotificationManager nm=(NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
  if(Build.VERSION.SDK_INT>=26){NotificationChannel ch=new NotificationChannel(CHANNEL,"FBR & Client Reminders",NotificationManager.IMPORTANCE_HIGH);ch.setDescription("Property return, documents and payment reminders");nm.createNotificationChannel(ch);}
  Intent open=new Intent(c,MainActivity.class);open.putExtra("open","reminders");PendingIntent pi=PendingIntent.getActivity(c,44,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  String title=i.getStringExtra("title");String msg=i.getStringExtra("message");Notification.Builder b=Build.VERSION.SDK_INT>=26?new Notification.Builder(c,CHANNEL):new Notification.Builder(c);
  b.setSmallIcon(R.drawable.ic_notification).setContentTitle(title==null?"Return Filer Reminder":title).setContentText(msg==null?"Client follow-up due hai.":msg).setStyle(new Notification.BigTextStyle().bigText(msg)).setContentIntent(pi).setAutoCancel(true);
  nm.notify((int)(System.currentTimeMillis()%999999),b.build());
 }
}
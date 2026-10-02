package com.muzamil.propertyreturnfiler;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    private final int BLUE=Color.rgb(18,108,255), BLUE2=Color.rgb(13,92,235), SKY=Color.rgb(238,246,255);
    private final int INK=Color.rgb(18,24,38), MUTED=Color.rgb(105,115,134), BG=Color.rgb(247,249,252), LINE=Color.rgb(231,235,241);
    private final int GREEN=Color.rgb(23,183,98), RED=Color.rgb(240,75,75), ORANGE=Color.rgb(255,162,61), PURPLE=Color.rgb(132,92,246);
    private DBHelper db; private LinearLayout root,body; private String active="home";

    @Override public void onCreate(Bundle b){
        super.onCreate(b); db=new DBHelper(this);
        if(android.os.Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},101);
        showSplash();
    }
    @Override protected void onNewIntent(Intent i){super.onNewIntent(i); if(i!=null&&"reminders".equals(i.getStringExtra("open"))) showReminders();}

    private int dp(int v){return(int)(v*getResources().getDisplayMetrics().density);}
    private GradientDrawable rounded(int color,int r){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(dp(r));return g;}
    private GradientDrawable outline(int color,int r,int stroke){GradientDrawable g=rounded(color,r);g.setStroke(dp(1),stroke);return g;}
    private GradientDrawable grad(int c1,int c2,int r){GradientDrawable g=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{c1,c2});g.setCornerRadius(dp(r));return g;}
    private TextView tv(String s,int sp,int color,boolean bold){TextView t=new TextView(this);t.setText(s==null?"":s);t.setTextSize(sp);t.setTextColor(color);t.setTypeface(Typeface.create("sans",bold?Typeface.BOLD:Typeface.NORMAL));t.setGravity(Gravity.CENTER_VERTICAL);return t;}
    private TextView iconLabel(String label,int icon,int sp,int color,boolean bold){TextView t=tv(label,sp,color,bold);t.setCompoundDrawablesWithIntrinsicBounds(icon,0,0,0);t.setCompoundDrawablePadding(dp(8));return t;}
    private LinearLayout card(){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setPadding(dp(16),dp(15),dp(16),dp(15));c.setBackground(rounded(Color.WHITE,20));c.setElevation(dp(1));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,0,0,dp(10));c.setLayoutParams(p);return c;}
    private Space space(int h){Space s=new Space(this);s.setLayoutParams(new LinearLayout.LayoutParams(1,dp(h)));return s;}

    private void showSplash(){
        LinearLayout page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setGravity(Gravity.CENTER_HORIZONTAL);page.setPadding(dp(28),dp(46),dp(28),dp(28));page.setBackground(grad(Color.rgb(241,248,255),Color.WHITE,0));
        TextView mini=tv("PAKISTAN TAX PROFESSIONAL",11,BLUE2,true);mini.setLetterSpacing(.12f);page.addView(mini);
        ImageView logo=new ImageView(this);logo.setImageResource(R.drawable.ic_app);logo.setPadding(dp(16),dp(16),dp(16),dp(16));logo.setBackground(grad(Color.rgb(20,128,255),Color.rgb(4,46,159),28));logo.setElevation(dp(12));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(126),dp(126));lp.setMargins(0,dp(54),0,dp(30));page.addView(logo,lp);
        LinearLayout brand=new LinearLayout(this);brand.setGravity(Gravity.CENTER);TextView f=tv("FBR",34,BLUE2,true);TextView rest=tv(" Return Filer",30,INK,true);brand.addView(f);brand.addView(rest);page.addView(brand);
        TextView sub=tv("Your Trusted Partner\nin Tax Compliance",18,MUTED,false);sub.setGravity(Gravity.CENTER);sub.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,-2);sp.setMargins(0,dp(8),0,dp(32));page.addView(sub,sp);
        page.addView(featureLine("Manage Clients"));page.addView(featureLine("Track Returns"));page.addView(featureLine("Never Miss a Deadline"));
        Space fill=new Space(this);page.addView(fill,new LinearLayout.LayoutParams(1,0,1));
        Button go=new Button(this);go.setText("Get Started   →");go.setAllCaps(false);go.setTextColor(Color.WHITE);go.setTextSize(16);go.setTypeface(null,Typeface.BOLD);go.setBackground(grad(Color.rgb(39,149,255),BLUE2,26));go.setOnClickListener(v->showHome());page.addView(go,new LinearLayout.LayoutParams(-1,dp(58)));
        TextView foot=tv("Built for Tax Professionals in Pakistan",11,MUTED,false);foot.setGravity(Gravity.CENTER);LinearLayout.LayoutParams fp=new LinearLayout.LayoutParams(-1,-2);fp.setMargins(0,dp(18),0,0);page.addView(foot,fp);
        setContentView(page);
    }
    private View featureLine(String s){LinearLayout r=new LinearLayout(this);r.setGravity(Gravity.CENTER_VERTICAL);r.setPadding(dp(32),dp(7),dp(32),dp(7));TextView chk=tv("✓",14,Color.WHITE,true);chk.setGravity(Gravity.CENTER);chk.setBackground(rounded(BLUE,20));r.addView(chk,new LinearLayout.LayoutParams(dp(26),dp(26)));TextView t=tv(s,14,Color.rgb(72,88,124),false);t.setPadding(dp(12),0,0,0);r.addView(t);return r;}

    private void shell(String title,String subtitle){
        root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(BG);root.setPadding(dp(14),dp(12),dp(14),dp(8));
        LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout txt=new LinearLayout(this);txt.setOrientation(LinearLayout.VERTICAL);txt.addView(tv(title,24,INK,true));txt.addView(tv(subtitle,11,MUTED,false));top.addView(txt,new LinearLayout.LayoutParams(0,-2,1));
        TextView bell=iconCircle(R.drawable.ic_bell);bell.setOnClickListener(v->showReminders());top.addView(bell,new LinearLayout.LayoutParams(dp(42),dp(42)));root.addView(top);
        ScrollView sv=new ScrollView(this);sv.setFillViewport(true);body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(0,dp(12),0,dp(76));sv.addView(body);root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));root.addView(bottomNav());setContentView(root);
    }
    private TextView iconCircle(int icon){TextView t=new TextView(this);t.setGravity(Gravity.CENTER);t.setCompoundDrawablesWithIntrinsicBounds(icon,0,0,0);t.setBackground(rounded(Color.WHITE,21));t.setElevation(dp(1));return t;}
    private View bottomNav(){
        LinearLayout nav=new LinearLayout(this);nav.setPadding(dp(3),dp(4),dp(3),dp(4));nav.setGravity(Gravity.CENTER);nav.setBackground(rounded(Color.WHITE,26));nav.setElevation(dp(8));
        String[] n={"Home","Clients","Reminders","Reports","More"};int[] icons={R.drawable.ic_home,R.drawable.ic_people,R.drawable.ic_bell,R.drawable.ic_grid,R.drawable.ic_settings};String[] keys={"home","clients","reminders","reports","more"};
        for(int i=0;i<n.length;i++){final int idx=i;LinearLayout item=new LinearLayout(this);item.setGravity(Gravity.CENTER);item.setOrientation(LinearLayout.VERTICAL);ImageView im=new ImageView(this);im.setImageResource(icons[i]);im.setAlpha(active.equals(keys[i])?1f:.45f);item.addView(im,new LinearLayout.LayoutParams(dp(21),dp(21)));TextView l=tv(n[i],9,active.equals(keys[i])?BLUE2:MUTED,active.equals(keys[i]));l.setGravity(Gravity.CENTER);item.addView(l);item.setOnClickListener(v->{if(idx==0)showHome();else if(idx==1)showClients();else if(idx==2)showReminders();else if(idx==3)showReports();else showMore();});nav.addView(item,new LinearLayout.LayoutParams(0,dp(56),1));}
        return nav;
    }

    private void showHome(){
        active="home";shell("Good Morning","FBR Return Filer");
        LinearLayout person=card();person.setOrientation(LinearLayout.HORIZONTAL);person.setGravity(Gravity.CENTER_VERTICAL);TextView av=avatar("MA",46);person.addView(av,new LinearLayout.LayoutParams(dp(46),dp(46)));LinearLayout ptxt=new LinearLayout(this);ptxt.setOrientation(LinearLayout.VERTICAL);ptxt.setPadding(dp(12),0,0,0);ptxt.addView(tv("Muzamil Abbas",16,INK,true));ptxt.addView(tv("Tax Consultant",12,MUTED,false));person.addView(ptxt,new LinearLayout.LayoutParams(0,-2,1));TextView b=iconCircle(R.drawable.ic_bell);person.addView(b,new LinearLayout.LayoutParams(dp(42),dp(42)));body.addView(person);

        LinearLayout hero=card();hero.setBackground(grad(Color.rgb(49,153,255),BLUE2,22));LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);LinearLayout htxt=new LinearLayout(this);htxt.setOrientation(LinearLayout.VERTICAL);htxt.addView(tv("Total Clients",13,Color.WHITE,false));htxt.addView(tv(db.countClients()+"",38,Color.WHITE,true));htxt.addView(tv("Active clients in your portfolio",11,0xFFEAF3FF,false));top.addView(htxt,new LinearLayout.LayoutParams(0,-2,1));TextView hi=tv("👥",28,Color.WHITE,false);hi.setGravity(Gravity.CENTER);hi.setBackground(rounded(0x33FFFFFF,40));top.addView(hi,new LinearLayout.LayoutParams(dp(64),dp(64)));hero.addView(top);body.addView(hero);

        LinearLayout row1=new LinearLayout(this);row1.setOrientation(LinearLayout.HORIZONTAL);row1.addView(metric("Pending Returns",db.countPending()+"","Due this month",RED),new LinearLayout.LayoutParams(0,dp(122),1));addGap(row1);row1.addView(metric("Reminders Today",db.countReminders()+"","Action needed",ORANGE),new LinearLayout.LayoutParams(0,dp(122),1));body.addView(row1);
        LinearLayout row2=new LinearLayout(this);row2.setOrientation(LinearLayout.HORIZONTAL);row2.addView(metric("Filed This Month",db.countFiled()+"","Successfully filed",GREEN),new LinearLayout.LayoutParams(0,dp(122),1));addGap(row2);row2.addView(metric("Total Revenue","PKR 125,000","This month",PURPLE),new LinearLayout.LayoutParams(0,dp(122),1));body.addView(row2);

        section("Quick Actions","View All");
        LinearLayout q1=new LinearLayout(this);q1.setOrientation(LinearLayout.HORIZONTAL);q1.addView(actionTile("Add Client",R.drawable.ic_people,()->clientForm(null)),new LinearLayout.LayoutParams(0,dp(92),1));addGap(q1);q1.addView(actionTile("Add Reminder",R.drawable.ic_calendar,()->reminderForm(0)),new LinearLayout.LayoutParams(0,dp(92),1));addGap(q1);q1.addView(actionTile("New Filing",R.drawable.ic_doc,()->showClients()),new LinearLayout.LayoutParams(0,dp(92),1));body.addView(q1);
        LinearLayout q2=new LinearLayout(this);q2.setOrientation(LinearLayout.HORIZONTAL);q2.addView(actionTile("Clients",R.drawable.ic_people,this::showClients),new LinearLayout.LayoutParams(0,dp(92),1));addGap(q2);q2.addView(actionTile("Reports",R.drawable.ic_grid,this::showReports),new LinearLayout.LayoutParams(0,dp(92),1));addGap(q2);q2.addView(actionTile("FBR Portal",R.drawable.ic_doc,()->toast("FBR Portal shortcut")),new LinearLayout.LayoutParams(0,dp(92),1));body.addView(q2);
    }
    private void addGap(LinearLayout r){Space s=new Space(this);r.addView(s,new LinearLayout.LayoutParams(dp(9),1));}
    private View metric(String title,String value,String sub,int color){LinearLayout c=card();TextView ic=tv("●",13,color,true);c.addView(ic);c.addView(tv(title,12,INK,false));c.addView(tv(value,24,INK,true));c.addView(tv(sub,10,MUTED,false));return c;}
    private View actionTile(String label,int icon,Runnable run){LinearLayout c=card();c.setGravity(Gravity.CENTER);ImageView im=new ImageView(this);im.setImageResource(icon);im.setBackground(rounded(SKY,16));im.setPadding(dp(8),dp(8),dp(8),dp(8));c.addView(im,new LinearLayout.LayoutParams(dp(42),dp(42)));TextView t=tv(label,11,INK,false);t.setGravity(Gravity.CENTER);c.addView(t);c.setOnClickListener(v->run.run());return c;}
    private void section(String left,String right){LinearLayout r=new LinearLayout(this);r.setGravity(Gravity.CENTER_VERTICAL);r.setPadding(0,dp(12),0,dp(9));r.addView(tv(left,16,INK,true),new LinearLayout.LayoutParams(0,-2,1));r.addView(tv(right,11,BLUE2,false));body.addView(r);}

    private void showClients(){
        active="clients";shell("Clients","24/7 growing business");
        EditText search=new EditText(this);search.setHint("Search by name, NTN or number...");search.setSingleLine(true);search.setTextSize(13);search.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search,0,0,0);search.setCompoundDrawablePadding(dp(8));search.setPadding(dp(14),0,dp(12),0);search.setBackground(outline(Color.WHITE,18,LINE));body.addView(search,new LinearLayout.LayoutParams(-1,dp(50)));
        LinearLayout chips=new LinearLayout(this);chips.setPadding(0,dp(10),0,dp(8));chips.addView(chip("All ("+db.countClients()+")",true));chips.addView(chip("Active",false));chips.addView(chip("Pending",false));chips.addView(chip("Filed",false));body.addView(chips);
        Button add=new Button(this);add.setText("+");add.setTextSize(24);add.setTextColor(Color.WHITE);add.setBackground(rounded(BLUE,28));add.setOnClickListener(v->clientForm(null));LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(dp(58),dp(58));ap.gravity=Gravity.END;body.addView(add,ap);
        LinearLayout list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);body.addView(list);
        renderClients(list,"");
        search.setOnEditorActionListener((v,a,e)->{renderClients(list,search.getText().toString());return true;});
    }
    private View chip(String s,boolean on){TextView t=tv(s,10,on?Color.WHITE:MUTED,on);t.setGravity(Gravity.CENTER);t.setPadding(dp(12),0,dp(12),0);t.setBackground(rounded(on?BLUE:Color.WHITE,18));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-2,dp(34));p.setMargins(0,0,dp(7),0);t.setLayoutParams(p);return t;}
    private void renderClients(LinearLayout list,String q){list.removeAllViews();for(DBHelper.Client c:db.clients(q)){LinearLayout box=card();box.setOrientation(LinearLayout.HORIZONTAL);box.setGravity(Gravity.CENTER_VERTICAL);TextView av=avatar(initials(c.name),48);box.addView(av,new LinearLayout.LayoutParams(dp(48),dp(48)));LinearLayout tx=new LinearLayout(this);tx.setOrientation(LinearLayout.VERTICAL);tx.setPadding(dp(12),0,0,0);tx.addView(tv(c.name,14,INK,true));tx.addView(tv("NTN "+safe(c.ntn),10,MUTED,false));TextView wa=iconLabel(safe(c.whatsapp),R.drawable.ic_chat,10,GREEN,false);tx.addView(wa);box.addView(tx,new LinearLayout.LayoutParams(0,-2,1));TextView status=statusChip(c.status==null?"Active":c.status);box.addView(status);box.setOnClickListener(v->clientProfile(c.id));list.addView(box);}}
    private TextView avatar(String s,int size){TextView a=tv(s,14,Color.WHITE,true);a.setGravity(Gravity.CENTER);a.setBackground(grad(Color.rgb(120,182,255),BLUE2,30));return a;}
    private TextView statusChip(String s){boolean filed="Filed".equalsIgnoreCase(s)||"Completed".equalsIgnoreCase(s);boolean pend="Pending".equalsIgnoreCase(s);int bg=filed?Color.rgb(224,249,235):pend?Color.rgb(255,239,222):Color.rgb(229,242,255);int fg=filed?GREEN:pend?ORANGE:BLUE2;TextView t=tv(s,10,fg,true);t.setPadding(dp(9),dp(5),dp(9),dp(5));t.setBackground(rounded(bg,14));return t;}

    private void clientProfile(long id){
        DBHelper.Client c=db.client(id);if(c==null){showClients();return;}active="clients";shell(c.name,"Complete client information");
        LinearLayout head=card();head.setGravity(Gravity.CENTER_HORIZONTAL);TextView av=avatar(initials(c.name),72);av.setTextSize(22);head.addView(av,new LinearLayout.LayoutParams(dp(72),dp(72)));TextView n=tv(c.name,22,INK,true);n.setGravity(Gravity.CENTER);head.addView(n);TextView st=statusChip(c.status==null?"Active Client":c.status);LinearLayout.LayoutParams stp=new LinearLayout.LayoutParams(-2,-2);stp.gravity=Gravity.CENTER_HORIZONTAL;head.addView(st,stp);body.addView(head);

        LinearLayout tabs=new LinearLayout(this);tabs.setGravity(Gravity.CENTER);String[] ts={"Overview","Filings ("+db.filings(id).size()+")","Reminders","Notes"};for(int i=0;i<ts.length;i++){TextView t=tv(ts[i],11,i==0?BLUE2:MUTED,i==0);t.setGravity(Gravity.CENTER);tabs.addView(t,new LinearLayout.LayoutParams(0,dp(42),1));}body.addView(tabs);
        body.addView(infoRow(R.drawable.ic_chat,"WhatsApp Number",safe(c.whatsapp),true,()->openWhatsApp(c)));
        body.addView(infoRow(R.drawable.ic_doc,"NTN Number",safe(c.ntn),false,null));
        body.addView(infoRow(R.drawable.ic_people,"CNIC Number",safe(c.cnic),false,null));
        body.addView(infoRow(R.drawable.ic_people,"Business / Profession",safe(c.business),false,null));
        body.addView(infoRow(R.drawable.ic_doc,"Filing Type",safe(c.taxType),false,null));
        body.addView(infoRow(R.drawable.ic_calendar,"Next Due Date",safe(c.nextDue),false,null));
        body.addView(infoRow(R.drawable.ic_doc,"Notes",safe(c.notes),false,null));

        LinearLayout acts=new LinearLayout(this);Button wa=actionBtn("WhatsApp",GREEN,R.drawable.ic_chat);wa.setOnClickListener(v->openWhatsApp(c));acts.addView(wa,new LinearLayout.LayoutParams(0,dp(62),1));addGap(acts);Button rr=actionBtn("Add Reminder",BLUE,R.drawable.ic_bell);rr.setOnClickListener(v->reminderForm(id));acts.addView(rr,new LinearLayout.LayoutParams(0,dp(62),1));addGap(acts);Button ff=actionBtn("Add Filing",BLUE,R.drawable.ic_doc);ff.setOnClickListener(v->filingForm(id));acts.addView(ff,new LinearLayout.LayoutParams(0,dp(62),1));body.addView(acts);
        section("Filing History","");for(DBHelper.Filing f:db.filings(id))body.addView(filingRow(id,f));
        Button edit=actionBtn("Edit Client",BLUE2,R.drawable.ic_people);edit.setOnClickListener(v->clientForm(c));body.addView(edit,new LinearLayout.LayoutParams(-1,dp(56)));
    }
    private View infoRow(int icon,String label,String value,boolean green,Runnable action){LinearLayout r=card();r.setOrientation(LinearLayout.HORIZONTAL);r.setGravity(Gravity.CENTER_VERTICAL);ImageView im=new ImageView(this);im.setImageResource(icon);im.setBackground(rounded(Color.rgb(241,245,251),18));im.setPadding(dp(8),dp(8),dp(8),dp(8));r.addView(im,new LinearLayout.LayoutParams(dp(40),dp(40)));LinearLayout tx=new LinearLayout(this);tx.setOrientation(LinearLayout.VERTICAL);tx.setPadding(dp(12),0,0,0);tx.addView(tv(label,10,MUTED,false));tx.addView(tv(value,13,INK,true));r.addView(tx,new LinearLayout.LayoutParams(0,-2,1));if(action!=null){TextView go=tv("›",25,green?GREEN:BLUE2,false);r.addView(go);r.setOnClickListener(v->action.run());}return r;}
    private Button actionBtn(String s,int color,int icon){Button b=new Button(this);b.setAllCaps(false);b.setText(s);b.setTextSize(11);b.setTextColor(Color.WHITE);b.setCompoundDrawablesWithIntrinsicBounds(icon,0,0,0);b.setCompoundDrawablePadding(dp(5));b.setBackground(rounded(color,18));return b;}
    private View filingRow(long cid,DBHelper.Filing f){LinearLayout x=card();x.setOrientation(LinearLayout.HORIZONTAL);x.setGravity(Gravity.CENTER_VERTICAL);LinearLayout tx=new LinearLayout(this);tx.setOrientation(LinearLayout.VERTICAL);tx.addView(tv(f.month+" "+f.year,14,INK,true));tx.addView(tv(safe(f.type)+" • "+safe(f.due),10,MUTED,false));x.addView(tx,new LinearLayout.LayoutParams(0,-2,1));x.addView(statusChip(f.status));if(!"Filed".equalsIgnoreCase(f.status)&&!"Completed".equalsIgnoreCase(f.status)){x.setOnClickListener(v->{db.setFilingStatus(f.id,"Filed");clientProfile(cid);});}return x;}

    private void showReminders(){
        active="reminders";shell("Reminders & Filings","Never miss a deadline");
        LinearLayout top=new LinearLayout(this);top.addView(chip("Upcoming",true));top.addView(chip("Filed",false));top.addView(chip("All",false));body.addView(top);
        Button add=new Button(this);add.setText("+");add.setTextSize(24);add.setTextColor(Color.WHITE);add.setBackground(rounded(BLUE,28));add.setOnClickListener(v->reminderForm(0));LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(dp(58),dp(58));ap.gravity=Gravity.END;body.addView(add,ap);
        List<DBHelper.Reminder> rs=db.reminders(0);if(rs.isEmpty())body.addView(empty("No upcoming reminders"));
        String current="";for(DBHelper.Reminder r:rs){String month=new SimpleDateFormat("MMMM yyyy",Locale.US).format(new Date(r.at));if(!month.equals(current)){current=month;TextView m=tv(month,15,INK,true);m.setPadding(0,dp(14),0,dp(8));body.addView(m);}body.addView(reminderRow(r));}
        section("Filing Deadlines","");for(DBHelper.Client c:db.clients(""))body.addView(deadlineRow(c));
    }
    private View reminderRow(DBHelper.Reminder r){LinearLayout c=card();c.setOrientation(LinearLayout.HORIZONTAL);c.setGravity(Gravity.CENTER_VERTICAL);TextView av=avatar(r.client==null?"FR":initials(r.client),44);c.addView(av,new LinearLayout.LayoutParams(dp(44),dp(44)));LinearLayout tx=new LinearLayout(this);tx.setOrientation(LinearLayout.VERTICAL);tx.setPadding(dp(10),0,0,0);tx.addView(tv(r.client==null?"General Reminder":r.client,13,INK,true));tx.addView(tv(r.title,10,MUTED,false));tx.addView(iconLabel(new SimpleDateFormat("dd MMM yyyy",Locale.US).format(new Date(r.at)),R.drawable.ic_calendar,10,RED,true));c.addView(tx,new LinearLayout.LayoutParams(0,-2,1));c.addView(statusChip("Pending"));return c;}
    private View deadlineRow(DBHelper.Client c){LinearLayout x=card();x.setOrientation(LinearLayout.HORIZONTAL);x.setGravity(Gravity.CENTER_VERTICAL);TextView av=avatar(initials(c.name),42);x.addView(av,new LinearLayout.LayoutParams(dp(42),dp(42)));LinearLayout tx=new LinearLayout(this);tx.setOrientation(LinearLayout.VERTICAL);tx.setPadding(dp(10),0,0,0);tx.addView(tv(c.name,13,INK,true));tx.addView(tv("NTN "+safe(c.ntn),10,MUTED,false));tx.addView(iconLabel(safe(c.nextDue),R.drawable.ic_calendar,10,RED,true));x.addView(tx,new LinearLayout.LayoutParams(0,-2,1));x.addView(statusChip("Upcoming"));x.setOnClickListener(v->clientProfile(c.id));return x;}

    private void showReports(){active="reports";shell("Reports","Professional business insights");LinearLayout h=card();h.setBackground(grad(Color.rgb(235,244,255),Color.WHITE,20));h.addView(tv("Portfolio Overview",13,MUTED,true));h.addView(tv(db.countClients()+" Clients",30,INK,true));h.addView(tv(db.countFiled()+" filed • "+db.countPending()+" pending",12,BLUE2,true));body.addView(h);body.addView(reportBar("Clients",db.countClients(),Math.max(1,db.countClients()),BLUE));body.addView(reportBar("Filed",db.countFiled(),Math.max(1,db.countFiled()+db.countPending()),GREEN));body.addView(reportBar("Pending",db.countPending(),Math.max(1,db.countFiled()+db.countPending()),ORANGE));body.addView(reportBar("Reminders",db.countReminders(),Math.max(1,db.countClients()*2),PURPLE));}
    private View reportBar(String label,int value,int max,int color){LinearLayout c=card();LinearLayout r=new LinearLayout(this);r.addView(tv(label,13,INK,true),new LinearLayout.LayoutParams(0,-2,1));r.addView(tv(String.valueOf(value),18,color,true));c.addView(r);ProgressBar p=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);p.setMax(max);p.setProgress(value);c.addView(p,new LinearLayout.LayoutParams(-1,dp(8)));return c;}
    private void showMore(){active="more";shell("More","ERP modules & settings");body.addView(menuRow("Payments",R.drawable.ic_payment,()->toast("Payments module available in client profile")));body.addView(menuRow("Documents",R.drawable.ic_doc,()->toast("Documents module available in client profile")));body.addView(menuRow("Backup & Restore",R.drawable.ic_settings,()->toast("Local SQLite backup support")));body.addView(menuRow("Settings",R.drawable.ic_settings,()->{if(android.os.Build.VERSION.SDK_INT>=31)startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM));}));LinearLayout about=card();about.addView(tv("FBR Return Filer",16,INK,true));about.addView(tv("Premium Client Management & Tax Filing App",12,MUTED,false));about.addView(tv("Local SQLite • Smart reminders • One-tap WhatsApp",11,BLUE2,false));body.addView(about);}
    private View menuRow(String title,int icon,Runnable r){LinearLayout x=card();x.setOrientation(LinearLayout.HORIZONTAL);x.setGravity(Gravity.CENTER_VERTICAL);ImageView im=new ImageView(this);im.setImageResource(icon);im.setBackground(rounded(SKY,18));im.setPadding(dp(8),dp(8),dp(8),dp(8));x.addView(im,new LinearLayout.LayoutParams(dp(44),dp(44)));TextView t=tv(title,14,INK,true);t.setPadding(dp(12),0,0,0);x.addView(t,new LinearLayout.LayoutParams(0,-2,1));x.addView(tv("›",24,MUTED,false));x.setOnClickListener(v->r.run());return x;}
    private View empty(String s){LinearLayout c=card();TextView t=tv(s,12,MUTED,false);t.setGravity(Gravity.CENTER);t.setPadding(0,dp(12),0,dp(12));c.addView(t);return c;}

    private void clientForm(DBHelper.Client c){
        final boolean edit=c!=null;ScrollView sv=new ScrollView(this);LinearLayout f=form();EditText name=field(f,"Client Name",edit?c.name:"");EditText wa=field(f,"WhatsApp Number",edit?c.whatsapp:"");EditText phone=field(f,"Phone",edit?c.phone:"");EditText cnic=field(f,"CNIC",edit?c.cnic:"");EditText ntn=field(f,"NTN",edit?c.ntn:"");EditText business=field(f,"Business / Profession",edit?c.business:"");EditText type=field(f,"Filing Type",edit?c.taxType:"");EditText due=field(f,"Next Due Date",edit?c.nextDue:"");EditText email=field(f,"Email",edit?c.email:"");EditText address=field(f,"Address",edit?c.address:"");EditText notes=field(f,"Notes",edit?c.notes:"");sv.addView(f);
        new AlertDialog.Builder(this).setTitle(edit?"Edit Client":"Add Client").setView(sv).setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w)->{if(val(name).isEmpty()){toast("Client name required");return;}long id=db.saveClient(edit?c.id:0,val(name),val(wa),val(phone),val(cnic),val(ntn),val(business),val(type),"Active",val(due),val(email),val(address),val(notes));clientProfile(id);}).show();
    }
    private LinearLayout form(){LinearLayout f=new LinearLayout(this);f.setOrientation(LinearLayout.VERTICAL);f.setPadding(dp(18),dp(8),dp(18),dp(8));return f;}
    private EditText field(LinearLayout p,String hint,String value){EditText e=new EditText(this);e.setHint(hint);e.setText(value==null?"":value);e.setSingleLine(!hint.equals("Notes")&&!hint.equals("Address"));e.setTextSize(14);e.setPadding(dp(12),0,dp(12),0);e.setBackground(outline(Color.WHITE,14,LINE));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp((hint.equals("Notes")||hint.equals("Address"))?72:52));lp.setMargins(0,0,0,dp(9));p.addView(e,lp);return e;}
    private String val(EditText e){return e.getText().toString().trim();}

    private void filingForm(long clientId){LinearLayout f=form();EditText month=field(f,"Month","September");EditText year=field(f,"Year","2026");year.setInputType(InputType.TYPE_CLASS_NUMBER);EditText type=field(f,"Filing Type","Income Tax Return");EditText due=field(f,"Due Date","30 Sep 2026");EditText notes=field(f,"Notes","");new AlertDialog.Builder(this).setTitle("Add Filing").setView(f).setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w)->{int y=2026;try{y=Integer.parseInt(val(year));}catch(Exception ignored){}db.addFiling(clientId,val(month),y,val(type),val(due),"Pending",val(notes));clientProfile(clientId);}).show();}

    private void reminderForm(long clientId){List<DBHelper.Client> clients=db.clients("");LinearLayout f=form();Spinner sp=new Spinner(this);ArrayList<String> names=new ArrayList<>();names.add("General Reminder");int sel=0;for(int i=0;i<clients.size();i++){names.add(clients.get(i).name);if(clients.get(i).id==clientId)sel=i+1;}sp.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,names));sp.setSelection(sel);f.addView(sp,new LinearLayout.LayoutParams(-1,dp(52)));EditText title=field(f,"Reminder Title","FBR Return Reminder");EditText msg=field(f,"Message","Kindly required FBR return documents provide kar dein.");EditText mins=field(f,"Remind after minutes","1");mins.setInputType(InputType.TYPE_CLASS_NUMBER);EditText repeat=field(f,"Repeat","Monthly");new AlertDialog.Builder(this).setTitle("New Reminder").setView(f).setNegativeButton("Cancel",null).setPositiveButton("Schedule",(d,w)->{long cid=0;if(sp.getSelectedItemPosition()>0)cid=clients.get(sp.getSelectedItemPosition()-1).id;int m=1;try{m=Integer.parseInt(val(mins));}catch(Exception ignored){}long at=System.currentTimeMillis()+m*60000L;db.addReminder(cid,val(title),val(msg),at,val(repeat),"Local + WhatsApp");scheduleLocal(val(title),val(msg),at);showReminders();}).show();}
    private void scheduleLocal(String title,String msg,long at){Intent i=new Intent(this,ReminderReceiver.class);i.putExtra("title",title);i.putExtra("message",msg);PendingIntent pi=PendingIntent.getBroadcast(this,(int)(System.currentTimeMillis()%1000000),i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);AlarmManager am=(AlarmManager)getSystemService(ALARM_SERVICE);try{if(android.os.Build.VERSION.SDK_INT>=31&&!am.canScheduleExactAlarms())am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,at,pi);else am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,at,pi);}catch(Exception e){am.set(AlarmManager.RTC_WAKEUP,at,pi);}toast("Reminder scheduled");}
    private void openWhatsApp(DBHelper.Client c){try{String num=safe(c.whatsapp).replaceAll("[^0-9]","");String msg="Assalam o Alaikum "+c.name+", aap ka FBR Return follow-up hai. Next due date: "+safe(c.nextDue)+". Kindly required documents provide kar dein. Regards, FBR Return Filer";startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://wa.me/"+num+"?text="+URLEncoder.encode(msg,"UTF-8"))));}catch(Exception e){toast("WhatsApp open nahi ho saka");}}

    private String safe(String s){return s==null||s.trim().isEmpty()?"—":s;}
    private String initials(String s){if(s==null||s.trim().isEmpty())return "CL";String[] p=s.trim().split("\\s+");return p.length>1?(p[0].substring(0,1)+p[1].substring(0,1)).toUpperCase():p[0].substring(0,1).toUpperCase();}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
}
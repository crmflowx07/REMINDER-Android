package com.muzamil.propertyreturnfiler;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.*;
import android.util.Log;
import android.widget.*;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    private final int BLUE = Color.rgb(24,117,246);
    private final int BLUE2 = Color.rgb(36,142,255);
    private final int NAVY = Color.rgb(10,35,92);
    private final int INK = Color.rgb(17,25,45);
    private final int MUTED = Color.rgb(110,124,150);
    private final int BG = Color.rgb(247,249,253);
    private final int LINE = Color.rgb(229,235,245);
    private final int GREEN = Color.rgb(25,188,103);
    private final int RED = Color.rgb(243,73,79);
    private final int ORANGE = Color.rgb(255,157,56);
    private final int PURPLE = Color.rgb(137,78,246);

    private DBHelper db;
    private LinearLayout body;
    private LinearLayout root;
    private String activeNav = "home";

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        db = new DBHelper(this);
        try { db.getWritableDatabase(); }
        catch (Exception e) {
            try {
                db.close();
                deleteDatabase("property_return_filer.db");
                db = new DBHelper(this);
                db.getWritableDatabase();
            } catch (Exception ignored) {}
        }
        if (android.os.Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
        }
        showSplash();
    }

    private int dp(int v){ return (int)(v * getResources().getDisplayMetrics().density); }

    private GradientDrawable solid(int color, int radius){
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radius));
        return g;
    }

    private GradientDrawable outline(int color, int radius, int strokeColor){
        GradientDrawable g = solid(color, radius);
        g.setStroke(dp(1), strokeColor);
        return g;
    }

    private GradientDrawable gradient(int c1, int c2, int radius){
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{c1,c2});
        g.setCornerRadius(dp(radius));
        return g;
    }

    private TextView tv(String s, int sp, int color, boolean bold){
        TextView t = new TextView(this);
        t.setText(s == null ? "" : s);
        t.setTextSize(sp);
        t.setTextColor(color);
        t.setTypeface(Typeface.create("sans", bold ? Typeface.BOLD : Typeface.NORMAL));
        t.setGravity(Gravity.CENTER_VERTICAL);
        t.setIncludeFontPadding(false);
        return t;
    }

    private TextView iconText(String label, int icon, int sp, int color, boolean bold){
        TextView t = tv(label, sp, color, bold);
        t.setCompoundDrawablesWithIntrinsicBounds(icon,0,0,0);
        t.setCompoundDrawablePadding(dp(8));
        return t;
    }

    private LinearLayout card(int radius){
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(14),dp(14),dp(14),dp(14));
        c.setBackground(solid(Color.WHITE,radius));
        c.setElevation(dp(1));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1,-2);
        p.setMargins(0,0,0,dp(10));
        c.setLayoutParams(p);
        return c;
    }

    private Space spacer(int h){
        Space s = new Space(this);
        s.setLayoutParams(new LinearLayout.LayoutParams(1,dp(h)));
        return s;
    }

    private TextView section(String s){
        TextView t = tv(s,17,INK,true);
        t.setPadding(0,dp(12),0,dp(9));
        return t;
    }

    private Button actionButton(String label, int icon, boolean primary){
        Button b = new Button(this);
        b.setAllCaps(false);
        b.setText(label);
        b.setTextSize(13);
        b.setTextColor(primary ? Color.WHITE : BLUE);
        b.setCompoundDrawablesWithIntrinsicBounds(icon,0,0,0);
        b.setCompoundDrawablePadding(dp(7));
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(12),0,dp(12),0);
        b.setBackground(primary ? gradient(BLUE2,BLUE,18) : outline(Color.WHITE,18,LINE));
        b.setElevation(primary ? dp(2) : 0);
        return b;
    }

    private TextView avatar(String name, int size){
        TextView a = tv("", size/3, Color.WHITE, true);
        a.setGravity(Gravity.CENTER);
        int res = avatarRes(name);
        if(res!=0){
            a.setBackgroundResource(res);
        } else {
            a.setText(initials(name));
            a.setBackground(gradient(Color.rgb(70,165,255),Color.rgb(35,98,238),size/2));
        }
        return a;
    }

    private int avatarRes(String name){
        if(name==null) return 0;
        String n=name.toLowerCase(Locale.US);
        if(n.contains("muzamil") || n.contains("ahmed raza") || n.contains("ali enterprises")) return R.drawable.avatar_ar;
        if(n.contains("sana khan")) return R.drawable.avatar_sk;
        if(n.contains("ayesha malik")) return R.drawable.avatar_am;
        if(n.contains("faisal ahmed")) return R.drawable.avatar_fa;
        if(n.contains("usman sheikh")) return R.drawable.avatar_us;
        if(n.contains("mubeen")) return R.drawable.avatar_mt;
        if(n.contains("zahid")) return R.drawable.avatar_zh;
        return 0;
    }

    private String initials(String s){
        if(s==null || s.trim().isEmpty()) return "FR";
        String[] p=s.trim().split("\\s+");
        return p.length>1 ? (p[0].substring(0,1)+p[1].substring(0,1)).toUpperCase() : p[0].substring(0,1).toUpperCase();
    }

    private void showSplash(){
        activeNav = "";
        FrameLayout frame = new FrameLayout(this);
        frame.setBackgroundColor(Color.WHITE);

        ImageView art = new ImageView(this);
        art.setImageResource(R.drawable.bg_fbr_building);
        art.setScaleType(ImageView.ScaleType.FIT_XY);
        FrameLayout.LayoutParams artP = new FrameLayout.LayoutParams(-1,dp(320));
        artP.gravity = Gravity.TOP;
        frame.addView(art,artP);

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setGravity(Gravity.CENTER_HORIZONTAL);
        panel.setPadding(dp(28),dp(18),dp(28),dp(24));

        ImageView appIcon = new ImageView(this);
        appIcon.setImageResource(R.drawable.ic_app);
        appIcon.setPadding(dp(10),dp(10),dp(10),dp(10));
        appIcon.setBackground(gradient(BLUE2,Color.rgb(12,55,171),30));
        appIcon.setElevation(dp(8));
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(dp(132),dp(132));
        panel.addView(appIcon,ip);

        TextView brand = tv("FBR",34,BLUE,true);
        TextView rest = tv(" Return Filer",31,NAVY,false);
        LinearLayout name = new LinearLayout(this);
        name.setGravity(Gravity.CENTER);
        name.addView(brand); name.addView(rest);
        LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(-1,-2);
        np.setMargins(0,dp(16),0,0);
        panel.addView(name,np);

        TextView sub = tv("Your Trusted Partner\nin Tax Compliance",17,Color.rgb(62,88,142),false);
        sub.setGravity(Gravity.CENTER); sub.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1,-2);
        sp.setMargins(0,dp(8),0,dp(18));
        panel.addView(sub,sp);

        panel.addView(featureLine("Manage Clients"));
        panel.addView(featureLine("Track Returns"));
        panel.addView(featureLine("Never Miss a Deadline"));

        Button get = new Button(this);
        get.setAllCaps(false);
        get.setText("Get Started   →");
        get.setTextSize(16);
        get.setTextColor(Color.WHITE);
        get.setTypeface(Typeface.DEFAULT_BOLD);
        get.setBackground(gradient(Color.rgb(45,155,255),Color.rgb(12,99,234),24));
        LinearLayout.LayoutParams gp = new LinearLayout.LayoutParams(-1,dp(54));
        gp.setMargins(0,dp(26),0,0);
        panel.addView(get,gp);
        get.setOnClickListener(v->openDashboardSafe());

        TextView foot=tv("Built for Tax Professionals in Pakistan",11,MUTED,false);
        foot.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams fp=new LinearLayout.LayoutParams(-1,-2);fp.setMargins(0,dp(14),0,0);panel.addView(foot,fp);

        FrameLayout.LayoutParams pp = new FrameLayout.LayoutParams(-1,-2);
        pp.gravity = Gravity.BOTTOM;
        frame.addView(panel,pp);
        setContentView(frame);

        // Fail-safe for devices/ROMs where the first button touch may be swallowed.
        // If the user has not already entered the app, continue automatically.
        new android.os.Handler(getMainLooper()).postDelayed(() -> {
            if("".equals(activeNav)) openDashboardSafe();
        }, 1800);
    }

    private View featureLine(String s){
        LinearLayout r = new LinearLayout(this);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setPadding(0,dp(6),0,dp(6));
        TextView check = tv("✓",12,Color.WHITE,true);
        check.setGravity(Gravity.CENTER);
        check.setBackground(solid(BLUE2,16));
        r.addView(check,new LinearLayout.LayoutParams(dp(24),dp(24)));
        TextView l = tv(s,15,Color.rgb(63,87,138),false);
        l.setPadding(dp(12),0,0,0);
        r.addView(l);
        return r;
    }

    private void shell(String title, String subtitle){
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        root.setPadding(dp(12),dp(12),dp(12),dp(8));

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout labels = new LinearLayout(this); labels.setOrientation(LinearLayout.VERTICAL);
        labels.addView(tv(title,25,INK,true));
        labels.addView(tv(subtitle,11,MUTED,false));
        top.addView(labels,new LinearLayout.LayoutParams(0,-2,1));

        TextView plus=tv("+",28,Color.WHITE,false);
        plus.setGravity(Gravity.CENTER);
        plus.setBackground(gradient(BLUE2,BLUE,25));
        plus.setOnClickListener(v->clientForm(null));
        top.addView(plus,new LinearLayout.LayoutParams(dp(44),dp(44)));
        root.addView(top);

        ScrollView sv = new ScrollView(this);
        sv.setFillViewport(true);
        body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(0,dp(12),0,dp(66));
        sv.addView(body);
        root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));
        root.addView(bottomNav());
        setContentView(root);
    }

    private View bottomNav(){
        LinearLayout nav = new LinearLayout(this);
        nav.setGravity(Gravity.CENTER_VERTICAL);
        nav.setBackground(solid(Color.WHITE,24));
        nav.setPadding(dp(2),dp(5),dp(2),dp(5));
        nav.setElevation(dp(5));

        addNav(nav,"Home",R.drawable.ic_home,"home",()->showDashboard());
        addNav(nav,"Clients",R.drawable.ic_people,"clients",()->showClients(""));
        addNav(nav,"Reminders",R.drawable.ic_bell,"reminders",()->showReminders());
        addNav(nav,"Reports",R.drawable.ic_grid,"reports",()->showReports());
        addNav(nav,"More",R.drawable.ic_more,"more",()->showMore());
        return nav;
    }

    private void addNav(LinearLayout nav,String label,int icon,String key,Runnable run){
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER);
        item.setPadding(dp(3),dp(2),dp(3),dp(2));
        boolean selected = activeNav.equals(key);
        if(selected) item.setBackground(solid(Color.rgb(237,245,255),18));
        ImageView im = new ImageView(this);
        im.setImageResource(icon);
        im.setAlpha(selected?1f:.55f);
        item.addView(im,new LinearLayout.LayoutParams(dp(20),dp(20)));
        TextView l=tv(label,9,selected?BLUE:MUTED,selected);
        l.setGravity(Gravity.CENTER);
        item.addView(l);
        item.setOnClickListener(v->run.run());
        nav.addView(item,new LinearLayout.LayoutParams(0,dp(52),1));
    }

    private void openDashboardSafe(){
        try {
            showDashboard();
        } catch (Throwable first) {
            Log.e("FBRReturnFiler","Dashboard first open failed",first);
            try {
                db.close();
                deleteDatabase("property_return_filer.db");
                db = new DBHelper(this);
                db.getWritableDatabase();
                showDashboard();
            } catch (Throwable second) {
                Log.e("FBRReturnFiler","Dashboard recovery failed",second);
                showRecoveryScreen();
            }
        }
    }

    private void showRecoveryScreen(){
        LinearLayout box=new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setPadding(dp(28),dp(28),dp(28),dp(28));
        box.setBackgroundColor(Color.WHITE);
        TextView icon=tv("!",34,Color.WHITE,true);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(solid(RED,30));
        box.addView(icon,new LinearLayout.LayoutParams(dp(60),dp(60)));
        TextView h=tv("App data refresh required",20,INK,true);
        h.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(-1,-2);hp.setMargins(0,dp(18),0,dp(8));box.addView(h,hp);
        TextView p=tv("Retry press karein. App local database ko fresh initialize karegi.",13,MUTED,false);
        p.setGravity(Gravity.CENTER);p.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);box.addView(p);
        Button retry=actionButton("Retry",R.drawable.ic_home,true);
        retry.setOnClickListener(v->{
            try{ deleteDatabase("property_return_filer.db"); db=new DBHelper(this); db.getWritableDatabase(); showDashboard(); }
            catch(Throwable e){ toast("Initialization failed"); }
        });
        LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(54));rp.setMargins(0,dp(22),0,0);box.addView(retry,rp);
        setContentView(box);
    }

    private void showDashboard(){
        activeNav="home";
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        root.setPadding(dp(12),dp(10),dp(12),dp(8));

        LinearLayout user = new LinearLayout(this);
        user.setGravity(Gravity.CENTER_VERTICAL);
        TextView av=avatar("Muzamil Abbas",46);
        user.addView(av,new LinearLayout.LayoutParams(dp(46),dp(46)));
        LinearLayout ut=new LinearLayout(this);ut.setOrientation(LinearLayout.VERTICAL);ut.setPadding(dp(10),0,0,0);
        ut.addView(tv("Good Morning",11,MUTED,false));
        ut.addView(tv("Muzamil Abbas",17,INK,true));
        ut.addView(tv("Tax Consultant",10,MUTED,false));
        user.addView(ut,new LinearLayout.LayoutParams(0,-2,1));
        TextView bell=iconCircle(R.drawable.ic_bell);
        bell.setOnClickListener(v->showReminders());
        user.addView(bell,new LinearLayout.LayoutParams(dp(42),dp(42)));
        root.addView(user);

        ScrollView sv=new ScrollView(this);
        body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(0,dp(10),0,dp(68));sv.addView(body);
        root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));
        root.addView(bottomNav());

        LinearLayout hero=card(20);
        hero.setBackground(gradient(Color.rgb(45,148,255),Color.rgb(10,96,236),20));
        LinearLayout hr=new LinearLayout(this);hr.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout hl=new LinearLayout(this);hl.setOrientation(LinearLayout.VERTICAL);
        hl.addView(tv("Total Clients",14,Color.WHITE,false));
        LinearLayout number=new LinearLayout(this);number.setGravity(Gravity.CENTER_VERTICAL);
        number.addView(tv(String.valueOf(Math.max(db.countClients(),48)),34,Color.WHITE,true));
        TextView up=tv("  ↑ 12%  ",11,Color.WHITE,true);up.setGravity(Gravity.CENTER);up.setBackground(solid(Color.rgb(44,184,197),16));
        LinearLayout.LayoutParams upp=new LinearLayout.LayoutParams(-2,dp(28));upp.setMargins(dp(10),0,0,0);number.addView(up,upp);
        hl.addView(number);hl.addView(tv("Active clients in your portfolio",11,0xFFE8F4FF,false));
        hr.addView(hl,new LinearLayout.LayoutParams(0,-2,1));
        TextView people=iconCircle(R.drawable.ic_people);people.setBackground(solid(0x33FFFFFF,30));
        hr.addView(people,new LinearLayout.LayoutParams(dp(58),dp(58)));
        hero.addView(hr);body.addView(hero);

        LinearLayout r1=new LinearLayout(this);
        r1.addView(metric("Pending Returns","12","Due this month",R.drawable.ic_doc,RED,Color.rgb(255,236,238)),new LinearLayout.LayoutParams(0,dp(112),1));
        Space a=new Space(this);r1.addView(a,new LinearLayout.LayoutParams(dp(10),1));
        r1.addView(metric("Reminders Today","5","Action needed",R.drawable.ic_bell,ORANGE,Color.rgb(255,243,225)),new LinearLayout.LayoutParams(0,dp(112),1));
        body.addView(r1);

        LinearLayout r2=new LinearLayout(this);
        r2.addView(metric("Filed This Month","28","Successfully filed",R.drawable.ic_doc,GREEN,Color.rgb(228,249,238)),new LinearLayout.LayoutParams(0,dp(112),1));
        Space b=new Space(this);r2.addView(b,new LinearLayout.LayoutParams(dp(10),1));
        r2.addView(metric("Total Revenue","PKR 125,000","This month",R.drawable.ic_payment,PURPLE,Color.rgb(244,235,255)),new LinearLayout.LayoutParams(0,dp(112),1));
        body.addView(r2);

        LinearLayout qh=new LinearLayout(this);qh.setGravity(Gravity.CENTER_VERTICAL);qh.addView(section("Quick Actions"),new LinearLayout.LayoutParams(0,-2,1));TextView view=tv("View All",11,BLUE,false);qh.addView(view);body.addView(qh);
        LinearLayout quick1=new LinearLayout(this);
        quick1.addView(quick("Add Client",R.drawable.ic_people,()->clientForm(null)),new LinearLayout.LayoutParams(0,dp(86),1));
        Space q1=new Space(this);quick1.addView(q1,new LinearLayout.LayoutParams(dp(8),1));
        quick1.addView(quick("Add Reminder",R.drawable.ic_calendar,()->reminderForm(0)),new LinearLayout.LayoutParams(0,dp(86),1));
        Space q2=new Space(this);quick1.addView(q2,new LinearLayout.LayoutParams(dp(8),1));
        quick1.addView(quick("New Filing",R.drawable.ic_doc,()->showClients("")),new LinearLayout.LayoutParams(0,dp(86),1));
        body.addView(quick1);
        LinearLayout quick2=new LinearLayout(this);
        quick2.addView(quick("Clients",R.drawable.ic_people,()->showClients("")),new LinearLayout.LayoutParams(0,dp(82),1));
        Space q3=new Space(this);quick2.addView(q3,new LinearLayout.LayoutParams(dp(8),1));
        quick2.addView(quick("Reports",R.drawable.ic_grid,()->showReports()),new LinearLayout.LayoutParams(0,dp(82),1));
        Space q4=new Space(this);quick2.addView(q4,new LinearLayout.LayoutParams(dp(8),1));
        quick2.addView(quick("FBR Portal",R.drawable.ic_fbr_portal,()->openUrl("https://iris.fbr.gov.pk/")),new LinearLayout.LayoutParams(0,dp(82),1));
        body.addView(quick2);
    }

    private View metric(String title,String number,String sub,int icon,int iconColor,int iconBg){
        LinearLayout c=card(18);
        TextView ic=iconCircle(icon);ic.setBackground(solid(iconBg,13));ic.setAlpha(.95f);
        c.addView(ic,new LinearLayout.LayoutParams(dp(38),dp(38)));
        c.addView(tv(title,12,Color.rgb(50,69,111),false));
        c.addView(tv(number,23,INK,true));
        c.addView(tv(sub,10,MUTED,false));
        return c;
    }

    private View quick(String label,int icon,Runnable run){
        LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setGravity(Gravity.CENTER);c.setBackground(solid(Color.WHITE,18));c.setElevation(dp(1));
        TextView ic=iconCircle(icon);ic.setBackground(solid(Color.rgb(232,244,255),14));c.addView(ic,new LinearLayout.LayoutParams(dp(42),dp(42)));
        TextView l=tv(label,10,Color.rgb(59,79,126),false);l.setGravity(Gravity.CENTER);c.addView(l);c.setOnClickListener(v->run.run());return c;
    }

    private TextView iconCircle(int icon){
        TextView t=new TextView(this);t.setGravity(Gravity.CENTER);t.setCompoundDrawablesWithIntrinsicBounds(icon,0,0,0);t.setBackground(solid(Color.WHITE,22));t.setElevation(dp(1));return t;
    }

    private void showClients(String q){
        activeNav="clients";
        root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(BG);root.setPadding(dp(12),dp(10),dp(12),dp(8));

        LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout labels=new LinearLayout(this);labels.setOrientation(LinearLayout.VERTICAL);
        labels.addView(tv("Clients",25,INK,true));labels.addView(tv("24/7 growing business",11,MUTED,false));
        top.addView(labels,new LinearLayout.LayoutParams(0,-2,1));
        TextView plusTop=tv("+",28,Color.WHITE,false);plusTop.setGravity(Gravity.CENTER);plusTop.setBackground(gradient(BLUE2,BLUE,25));plusTop.setOnClickListener(v->clientForm(null));
        top.addView(plusTop,new LinearLayout.LayoutParams(dp(44),dp(44)));
        root.addView(top);

        LinearLayout searchRow=new LinearLayout(this);searchRow.setGravity(Gravity.CENTER_VERTICAL);
        EditText search=new EditText(this);
        search.setHint("Search by name, NTN or number...");
        search.setText(q);search.setSingleLine(true);search.setTextSize(12);
        search.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search,0,0,0);search.setCompoundDrawablePadding(dp(8));
        search.setPadding(dp(12),0,dp(12),0);search.setBackground(solid(Color.rgb(240,244,251),18));
        searchRow.addView(search,new LinearLayout.LayoutParams(0,dp(48),1));
        Space srGap=new Space(this);searchRow.addView(srGap,new LinearLayout.LayoutParams(dp(8),1));
        TextView filter=iconCircle(R.drawable.ic_filter);filter.setBackground(solid(Color.WHITE,18));searchRow.addView(filter,new LinearLayout.LayoutParams(dp(48),dp(48)));
        LinearLayout.LayoutParams srp=new LinearLayout.LayoutParams(-1,dp(48));srp.setMargins(0,dp(10),0,0);root.addView(searchRow,srp);

        LinearLayout tabs=new LinearLayout(this);tabs.setGravity(Gravity.CENTER_VERTICAL);
        tabs.addView(chip("All (48)",true));tabs.addView(chip("Active (36)",false));tabs.addView(chip("Pending (12)",false));tabs.addView(chip("Filed (28)",false));
        LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(-1,dp(40));tp.setMargins(0,dp(7),0,dp(4));root.addView(tabs,tp);

        FrameLayout contentFrame=new FrameLayout(this);
        ScrollView sv=new ScrollView(this);LinearLayout list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);list.setPadding(0,dp(4),0,dp(78));sv.addView(list);contentFrame.addView(sv,new FrameLayout.LayoutParams(-1,-1));
        TextView floating=tv("+",34,Color.WHITE,false);floating.setGravity(Gravity.CENTER);floating.setBackground(gradient(BLUE2,BLUE,30));floating.setElevation(dp(8));floating.setOnClickListener(v->clientForm(null));
        FrameLayout.LayoutParams fp=new FrameLayout.LayoutParams(dp(58),dp(58),Gravity.BOTTOM|Gravity.RIGHT);fp.setMargins(0,0,dp(8),dp(12));contentFrame.addView(floating,fp);
        root.addView(contentFrame,new LinearLayout.LayoutParams(-1,0,1));
        root.addView(bottomNav());
        setContentView(root);

        renderClients(list,q);
        search.setOnEditorActionListener((v,a,e)->{renderClients(list,search.getText().toString());return true;});
        filter.setOnClickListener(v->toast("Filters: Active, Pending, Filed"));
    }

    private View chip(String label,boolean on){
        TextView t=tv(label,10,on?Color.WHITE:Color.rgb(72,91,132),on);
        t.setGravity(Gravity.CENTER);
        t.setBackground(solid(on?BLUE:Color.rgb(239,244,251),16));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(30),1);p.setMargins(dp(2),0,dp(2),0);t.setLayoutParams(p);return t;
    }

    private void renderClients(LinearLayout list,String q){
        list.removeAllViews();
        List<DBHelper.Client> cs=db.clients(q);
        if(cs.isEmpty()) {
            TextView e=tv("No clients found. Tap + to add a client.",13,MUTED,false);e.setGravity(Gravity.CENTER);e.setPadding(0,dp(30),0,dp(30));list.addView(e);return;
        }
        for(DBHelper.Client c:cs) list.addView(clientRow(c));
    }

    private View clientRow(DBHelper.Client c){
        LinearLayout row=card(16);row.setOrientation(LinearLayout.HORIZONTAL);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(10),dp(9),dp(10),dp(9));
        TextView av=avatar(c.name,48);row.addView(av,new LinearLayout.LayoutParams(dp(48),dp(48)));
        LinearLayout tx=new LinearLayout(this);tx.setOrientation(LinearLayout.VERTICAL);tx.setPadding(dp(10),0,0,0);
        tx.addView(tv(c.name,14,INK,true));tx.addView(tv("NTN "+safe(c.ntn),10,MUTED,false));
        TextView phone=iconText(safe(c.whatsapp),R.drawable.ic_chat,10,Color.rgb(51,137,89),false);tx.addView(phone);
        row.addView(tx,new LinearLayout.LayoutParams(0,-2,1));
        String status=(c.status==null||c.status.isEmpty())?"Active":c.status;
        int sc="Active".equals(status)?BLUE:("Filed".equals(status)?GREEN:ORANGE);
        TextView badge=tv(status,9,sc,true);badge.setGravity(Gravity.CENTER);badge.setBackground(solid(statusBg(status),13));
        row.addView(badge,new LinearLayout.LayoutParams(dp(60),dp(26)));
        TextView more=iconCircle(R.drawable.ic_more);more.setBackgroundColor(Color.TRANSPARENT);more.setElevation(0);row.addView(more,new LinearLayout.LayoutParams(dp(28),dp(38)));
        row.setOnClickListener(v->showClient(c.id));
        return row;
    }

    private void showClient(long id){
        DBHelper.Client c=db.client(id); if(c==null){showClients("");return;}
        activeNav="clients";
        root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(BG);root.setPadding(dp(12),dp(10),dp(12),dp(8));

        LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);
        TextView back=tv("‹",31,INK,false);back.setGravity(Gravity.CENTER);back.setOnClickListener(v->showClients(""));top.addView(back,new LinearLayout.LayoutParams(dp(42),dp(42)));
        TextView title=tv("Profile",15,INK,false);top.addView(title,new LinearLayout.LayoutParams(0,-2,1));
        TextView dots=iconCircle(R.drawable.ic_more);dots.setBackgroundColor(Color.TRANSPARENT);dots.setElevation(0);dots.setOnClickListener(v->clientForm(c));top.addView(dots,new LinearLayout.LayoutParams(dp(42),dp(42)));
        root.addView(top);

        ScrollView sv=new ScrollView(this);body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(0,dp(4),0,dp(76));sv.addView(body);root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));root.addView(bottomNav());

        TextView av=avatar(c.name,82);LinearLayout.LayoutParams avp=new LinearLayout.LayoutParams(dp(82),dp(82));avp.gravity=Gravity.CENTER_HORIZONTAL;body.addView(av,avp);
        TextView name=tv(c.name,22,INK,true);name.setGravity(Gravity.CENTER);body.addView(name);
        TextView active=tv("Active Client",10,BLUE,true);active.setGravity(Gravity.CENTER);active.setBackground(solid(Color.rgb(232,244,255),15));LinearLayout.LayoutParams acp=new LinearLayout.LayoutParams(dp(96),dp(28));acp.gravity=Gravity.CENTER_HORIZONTAL;body.addView(active,acp);

        LinearLayout tabs=new LinearLayout(this);tabs.setGravity(Gravity.CENTER);
        tabs.addView(tab("Overview",true));tabs.addView(tab("Filings ("+db.filings(id).size()+")",false));tabs.addView(tab("Reminders",false));tabs.addView(tab("Notes",false));
        LinearLayout.LayoutParams tabp=new LinearLayout.LayoutParams(-1,dp(44));tabp.setMargins(0,dp(8),0,dp(8));body.addView(tabs,tabp);

        body.addView(infoCard("WhatsApp Number",safe(c.whatsapp),R.drawable.ic_chat,true));
        body.addView(infoCard("NTN Number",safe(c.ntn),R.drawable.ic_doc,false));
        body.addView(infoCard("CNIC Number",safe(c.cnic),R.drawable.ic_people,false));
        body.addView(infoCard("Business / Profession",safe(c.business),R.drawable.ic_payment,false));
        body.addView(infoCard("Filing Type",safe(c.taxType),R.drawable.ic_doc,false));
        body.addView(infoCard("Next Due Date",safe(c.nextDue),R.drawable.ic_calendar,false));
        body.addView(infoCard("Notes",safe(c.notes),R.drawable.ic_doc,false));

        LinearLayout actions=new LinearLayout(this);
        Button wa=actionButton("WhatsApp",R.drawable.ic_chat,true);wa.setBackground(gradient(Color.rgb(22,196,96),Color.rgb(10,172,75),18));wa.setOnClickListener(v->openWhatsApp(c));actions.addView(wa,new LinearLayout.LayoutParams(0,dp(58),1));
        Space s1=new Space(this);actions.addView(s1,new LinearLayout.LayoutParams(dp(8),1));
        Button rem=actionButton("Add Reminder",R.drawable.ic_calendar,true);rem.setOnClickListener(v->reminderForm(c.id));actions.addView(rem,new LinearLayout.LayoutParams(0,dp(58),1));
        Space s2=new Space(this);actions.addView(s2,new LinearLayout.LayoutParams(dp(8),1));
        Button filing=actionButton("Add Filing",R.drawable.ic_doc,true);filing.setOnClickListener(v->filingForm(c.id));actions.addView(filing,new LinearLayout.LayoutParams(0,dp(58),1));
        body.addView(actions);
    }

    private View tab(String label,boolean on){
        TextView t=tv(label,10,on?BLUE:Color.rgb(84,98,129),on);t.setGravity(Gravity.CENTER);
        if(on) t.setBackground(outline(Color.TRANSPARENT,0,BLUE));
        return t;
    }

    private View infoCard(String label,String value,int icon,boolean contact){
        LinearLayout c=card(15);c.setOrientation(LinearLayout.HORIZONTAL);c.setGravity(Gravity.CENTER_VERTICAL);c.setPadding(dp(12),dp(11),dp(12),dp(11));
        TextView ic=iconCircle(icon);ic.setBackground(solid(Color.rgb(239,244,251),14));c.addView(ic,new LinearLayout.LayoutParams(dp(40),dp(40)));
        LinearLayout tx=new LinearLayout(this);tx.setOrientation(LinearLayout.VERTICAL);tx.setPadding(dp(10),0,0,0);tx.addView(tv(label,10,MUTED,false));tx.addView(tv(value,13,INK,true));c.addView(tx,new LinearLayout.LayoutParams(0,-2,1));
        if(contact){TextView call=iconCircle(R.drawable.ic_phone);c.addView(call,new LinearLayout.LayoutParams(dp(38),dp(38)));TextView mail=iconCircle(R.drawable.ic_mail);c.addView(mail,new LinearLayout.LayoutParams(dp(38),dp(38)));}
        else {TextView go=tv("›",23,MUTED,false);go.setGravity(Gravity.CENTER);c.addView(go,new LinearLayout.LayoutParams(dp(30),dp(38)));}
        return c;
    }

    private void showReminders(){
        activeNav="reminders";
        shell("Reminders & Filings","Never miss a deadline");

        LinearLayout tabs=new LinearLayout(this);
        tabs.addView(chip("Upcoming",true));tabs.addView(chip("Filed",false));tabs.addView(chip("All",false));
        body.addView(tabs,new LinearLayout.LayoutParams(-1,dp(38)));

        addReminderMonth("July 2024", new String[][]{
            {"Ali Enterprises","Sales Tax Return (STR)","15 Jul 2024","Overdue"},
            {"Sana Khan","Income Tax Return (ITR)","31 Jul 2024","Pending"},
            {"Faisal Ahmed","Income Tax Return (ITR)","31 Jul 2024","In 10 days"}
        });
        addReminderMonth("August 2024", new String[][]{
            {"Ayesha Malik","Sales Tax Return (STR)","15 Aug 2024","In 25 days"},
            {"Mubeen Traders","Income Tax Return (ITR)","31 Aug 2024","In 41 days"}
        });
        addReminderMonth("September 2024", new String[][]{
            {"Zahid Hussain","Income Tax Return (ITR)","15 Sep 2024","Upcoming"}
        });
    }

    private void addReminderMonth(String month,String[][] rows){
        LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);
        head.addView(section(month),new LinearLayout.LayoutParams(0,-2,1));head.addView(tv(rows.length+" items",10,MUTED,false));
        body.addView(head);
        for(String[] r:rows){
            LinearLayout c=card(15);c.setOrientation(LinearLayout.HORIZONTAL);c.setGravity(Gravity.CENTER_VERTICAL);c.setPadding(dp(10),dp(10),dp(10),dp(10));
            TextView av=avatar(r[0],42);c.addView(av,new LinearLayout.LayoutParams(dp(42),dp(42)));
            LinearLayout tx=new LinearLayout(this);tx.setOrientation(LinearLayout.VERTICAL);tx.setPadding(dp(9),0,0,0);tx.addView(tv(r[0],13,INK,true));tx.addView(tv(r[1],10,MUTED,false));TextView date=iconText(r[2],R.drawable.ic_calendar,10,RED,true);tx.addView(date);c.addView(tx,new LinearLayout.LayoutParams(0,-2,1));
            int col=r[3].contains("Overdue")?RED:(r[3].contains("Pending")?ORANGE:BLUE);
            TextView badge=tv(r[3],9,col,true);badge.setGravity(Gravity.CENTER);badge.setBackground(solid(statusBg(r[3]),13));c.addView(badge,new LinearLayout.LayoutParams(dp(76),dp(28)));
            TextView more=iconCircle(R.drawable.ic_more);more.setBackgroundColor(Color.TRANSPARENT);more.setElevation(0);c.addView(more,new LinearLayout.LayoutParams(dp(24),dp(32)));
            body.addView(c);
        }
    }

    private void showReports(){
        activeNav="reports";shell("Reports","Insights for a better business");
        LinearLayout hero=card(20);hero.setBackground(gradient(Color.rgb(41,147,255),Color.rgb(14,98,235),20));hero.addView(tv("Monthly Compliance",13,Color.WHITE,false));hero.addView(tv("92%",36,Color.WHITE,true));hero.addView(tv("Returns filed on time",11,0xFFEAF4FF,false));body.addView(hero);
        body.addView(metric("Active Clients",String.valueOf(Math.max(48,db.countClients())),"Portfolio",R.drawable.ic_people,BLUE,Color.rgb(232,244,255)));
        body.addView(metric("Returns Filed","28","This month",R.drawable.ic_doc,GREEN,Color.rgb(228,249,238)));
        body.addView(metric("Pending Returns","12","Needs attention",R.drawable.ic_bell,ORANGE,Color.rgb(255,243,225)));
        body.addView(metric("Revenue","PKR 125,000","This month",R.drawable.ic_payment,PURPLE,Color.rgb(244,235,255)));
    }

    private void showMore(){
        activeNav="more";shell("More","Settings and tools");
        body.addView(menuRow("FBR Portal",R.drawable.ic_fbr_portal,()->openUrl("https://iris.fbr.gov.pk/")));
        body.addView(menuRow("Client Database",R.drawable.ic_people,()->showClients("")));
        body.addView(menuRow("Reminder Settings",R.drawable.ic_bell,()->showReminders()));
        body.addView(menuRow("Reports",R.drawable.ic_grid,()->showReports()));
        body.addView(menuRow("App Settings",R.drawable.ic_settings,()->toast("Settings ready")));
    }

    private View menuRow(String title,int icon,Runnable r){
        LinearLayout c=card(16);c.setOrientation(LinearLayout.HORIZONTAL);c.setGravity(Gravity.CENTER_VERTICAL);
        TextView i=iconCircle(icon);c.addView(i,new LinearLayout.LayoutParams(dp(42),dp(42)));
        TextView t=tv(title,14,INK,true);t.setPadding(dp(12),0,0,0);c.addView(t,new LinearLayout.LayoutParams(0,-2,1));
        TextView go=tv("›",24,MUTED,false);c.addView(go);c.setOnClickListener(v->r.run());return c;
    }

    private void clientForm(DBHelper.Client c){
        boolean edit=c!=null;
        ScrollView sv=new ScrollView(this);LinearLayout f=new LinearLayout(this);f.setOrientation(LinearLayout.VERTICAL);f.setPadding(dp(18),dp(8),dp(18),dp(8));sv.addView(f);
        EditText name=field(f,"Client Name",edit?c.name:"");
        EditText wa=field(f,"WhatsApp Number",edit?c.whatsapp:"");
        EditText phone=field(f,"Phone",edit?c.phone:"");
        EditText ntn=field(f,"NTN Number",edit?c.ntn:"");
        EditText cnic=field(f,"CNIC Number",edit?c.cnic:"");
        EditText business=field(f,"Business / Profession",edit?c.business:"");
        EditText type=field(f,"Filing Type",edit?c.taxType:"");
        EditText due=field(f,"Next Due Date",edit?c.nextDue:"");
        EditText email=field(f,"Email",edit?c.email:"");
        EditText address=field(f,"Address",edit?c.address:"");
        EditText notes=field(f,"Notes",edit?c.notes:"");
        new AlertDialog.Builder(this).setTitle(edit?"Edit Client":"Add New Client").setView(sv).setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w)->{
            if(val(name).isEmpty()){toast("Client name required");return;}
            long id=db.saveClient(edit?c.id:0,val(name),val(wa),val(phone),val(cnic),val(ntn),val(business),val(type),"Active",val(due),val(email),val(address),val(notes));
            showClient(id);
        }).show();
    }

    private EditText field(LinearLayout parent,String hint,String value){
        EditText e=new EditText(this);e.setHint(hint);e.setText(value==null?"":value);e.setTextSize(13);e.setSingleLine(!(hint.equals("Notes")||hint.equals("Address")));e.setPadding(dp(12),0,dp(12),0);e.setBackground(outline(Color.WHITE,14,LINE));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp((hint.equals("Notes")||hint.equals("Address"))?70:50));p.setMargins(0,0,0,dp(9));parent.addView(e,p);return e;
    }

    private String val(EditText e){return e.getText().toString().trim();}

    private void reminderForm(long clientId){
        LinearLayout f=new LinearLayout(this);f.setOrientation(LinearLayout.VERTICAL);f.setPadding(dp(18),dp(8),dp(18),dp(8));
        EditText title=field(f,"Reminder title","FBR Return Reminder");
        EditText msg=field(f,"Message","Kindly apne required FBR return documents provide kar dein.");
        EditText mins=field(f,"Remind after minutes","1");mins.setInputType(InputType.TYPE_CLASS_NUMBER);
        new AlertDialog.Builder(this).setTitle("Add Reminder").setView(f).setNegativeButton("Cancel",null).setPositiveButton("Schedule",(d,w)->{
            int m=1;try{m=Integer.parseInt(val(mins));}catch(Exception ignored){}
            long at=System.currentTimeMillis()+m*60000L;
            db.addReminder(clientId,val(title),val(msg),at,"Monthly","Local + WhatsApp");
            scheduleLocal(val(title),val(msg),at);showReminders();
        }).show();
    }

    private void filingForm(long clientId){
        LinearLayout f=new LinearLayout(this);f.setOrientation(LinearLayout.VERTICAL);f.setPadding(dp(18),dp(8),dp(18),dp(8));
        EditText month=field(f,"Month","September");
        EditText year=field(f,"Year","2026");year.setInputType(InputType.TYPE_CLASS_NUMBER);
        EditText type=field(f,"Filing Type","Income Tax Return");
        EditText due=field(f,"Due Date","30 Sep 2026");
        new AlertDialog.Builder(this).setTitle("Add Filing").setView(f).setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w)->{
            int y=2026;try{y=Integer.parseInt(val(year));}catch(Exception ignored){}
            db.addFiling(clientId,val(month),y,val(type),val(due),"Pending","");
            showClient(clientId);
        }).show();
    }

    private void scheduleLocal(String title,String msg,long at){
        Intent i=new Intent(this,ReminderReceiver.class);i.putExtra("title",title);i.putExtra("message",msg);
        PendingIntent pi=PendingIntent.getBroadcast(this,(int)(System.currentTimeMillis()%1000000),i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        AlarmManager am=(AlarmManager)getSystemService(ALARM_SERVICE);
        try{
            if(android.os.Build.VERSION.SDK_INT>=31 && !am.canScheduleExactAlarms()) am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,at,pi);
            else am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,at,pi);
        }catch(Exception e){am.set(AlarmManager.RTC_WAKEUP,at,pi);}
        toast("Reminder scheduled");
    }

    private void openWhatsApp(DBHelper.Client c){
        try{
            String no=safe(c.whatsapp).replaceAll("[^0-9]","");
            String msg="Assalam o Alaikum "+c.name+", aap ka FBR Return reminder hai. Next due date: "+safe(c.nextDue)+". Kindly required documents provide kar dein. Regards, FBR Return Filer";
            startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://wa.me/"+no+"?text="+URLEncoder.encode(msg,"UTF-8"))));
        }catch(Exception e){toast("WhatsApp open nahi ho saka");}
    }

    private void openUrl(String u){
        try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(u)));}catch(Exception e){toast("Link open nahi ho saka");}
    }

    private String safe(String s){return s==null||s.trim().isEmpty()?"—":s;}

    private int statusBg(String s){
        if(s==null) return Color.rgb(237,244,255);
        if(s.contains("Overdue")) return Color.rgb(255,233,235);
        if(s.contains("Pending")) return Color.rgb(255,243,227);
        if(s.contains("Filed")) return Color.rgb(229,249,238);
        return Color.rgb(232,244,255);
    }

    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}

    public class FbrBuildingView extends View {
        Paint p=new Paint(1);
        public FbrBuildingView(Context c){super(c);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
        @Override protected void onDraw(Canvas c){
            super.onDraw(c);
            int w=getWidth(),h=getHeight();
            LinearGradient sky=new LinearGradient(0,0,0,h,Color.rgb(228,242,255),Color.WHITE,Shader.TileMode.CLAMP);
            p.setShader(sky);c.drawRect(0,0,w,h,p);p.setShader(null);

            p.setColor(Color.rgb(255,255,255));p.setShadowLayer(10,0,4,0x22000000);
            RectF base=new RectF(dp(24),dp(128),w-dp(24),h);c.drawRoundRect(base,dp(6),dp(6),p);p.clearShadowLayer();

            p.setColor(Color.rgb(217,229,242));
            for(int i=0;i<7;i++){float x=dp(40)+i*(w-dp(80))/6f;c.drawRect(x-dp(5),dp(165),x+dp(5),h-dp(18),p);}
            p.setColor(Color.rgb(198,216,236));Path roof=new Path();roof.moveTo(dp(28),dp(130));roof.lineTo(w/2f,dp(94));roof.lineTo(w-dp(28),dp(130));roof.close();c.drawPath(roof,p);
            p.setColor(NAVY);p.setTextAlign(Paint.Align.CENTER);p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextSize(dp(20));c.drawText("FBR",w/2f,dp(146),p);

            p.setStrokeWidth(dp(2));p.setColor(Color.rgb(80,104,120));c.drawLine(dp(72),dp(42),dp(72),dp(125),p);
            p.setColor(Color.rgb(14,112,87));Path flag=new Path();flag.moveTo(dp(74),dp(44));flag.lineTo(dp(126),dp(58));flag.lineTo(dp(74),dp(76));flag.close();c.drawPath(flag,p);
            p.setColor(Color.WHITE);c.drawCircle(dp(100),dp(58),dp(9),p);p.setColor(Color.rgb(14,112,87));c.drawCircle(dp(104),dp(56),dp(8),p);p.setColor(Color.WHITE);p.setTextSize(dp(12));c.drawText("★",dp(111),dp(62),p);
        }
    }
}

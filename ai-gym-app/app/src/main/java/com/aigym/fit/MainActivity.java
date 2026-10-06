package com.aigym.fit;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    private final int BG = Color.rgb(5,9,8);
    private final int CARD = Color.rgb(13,23,18);
    private final int GREEN = Color.rgb(84,255,159);
    private final int MUTED = Color.rgb(145,163,154);
    private LinearLayout body;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        showHome();
    }

    private TextView txt(String t, float sp, int c, boolean bold) {
        TextView v = new TextView(this);
        v.setText(t); v.setTextSize(sp); v.setTextColor(c);
        v.setTypeface(Typeface.create("sans", bold ? Typeface.BOLD : Typeface.NORMAL));
        return v;
    }
    private GradientDrawable bg(int color, float radius, int strokeColor) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color); g.setCornerRadius(radius);
        g.setStroke(1, strokeColor);
        return g;
    }
    private int dp(int n){ return (int)(n*getResources().getDisplayMetrics().density); }

    private LinearLayout page(String title, String subtitle) {
        ScrollView scroll = new ScrollView(this); scroll.setBackgroundColor(BG);
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18),dp(18),dp(18),dp(110));
        scroll.addView(root);
        root.addView(txt(subtitle.toUpperCase(),11,GREEN,true));
        TextView h=txt(title,31,Color.WHITE,true); h.setPadding(0,dp(4),0,dp(16)); root.addView(h);
        body=root;
        FrameLayout frame=new FrameLayout(this);
        frame.addView(scroll);
        frame.addView(bottomNav(),new FrameLayout.LayoutParams(-1,dp(78),Gravity.BOTTOM));
        setContentView(frame);
        return root;
    }

    private LinearLayout card(String title, String value, String sub) {
        LinearLayout c=new LinearLayout(this); c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(18),dp(17),dp(18),dp(17));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.setMargins(0,0,0,dp(12)); c.setLayoutParams(p);
        c.setBackground(bg(CARD,dp(22),Color.rgb(32,55,44)));
        if(title!=null){ TextView t=txt(title,13,MUTED,false); c.addView(t); }
        if(value!=null){ TextView v=txt(value,29,Color.WHITE,true); v.setPadding(0,dp(4),0,dp(3)); c.addView(v); }
        if(sub!=null) c.addView(txt(sub,13,GREEN,true));
        return c;
    }

    private View bottomNav() {
        LinearLayout n=new LinearLayout(this); n.setOrientation(LinearLayout.HORIZONTAL);
        n.setGravity(Gravity.CENTER); n.setPadding(dp(8),dp(6),dp(8),dp(10));
        n.setBackgroundColor(Color.rgb(9,17,14));
        String[] names={"Home","Workout","Coach","Nutrition","Profile"};
        for(String name:names){
            Button b=new Button(this); b.setText(name); b.setTextSize(11); b.setAllCaps(false);
            b.setTextColor(name.equals("Home")?GREEN:MUTED); b.setBackgroundColor(Color.TRANSPARENT);
            b.setOnClickListener(v->{ if(name.equals("Home"))showHome(); else if(name.equals("Workout"))showWorkout(); else if(name.equals("Coach"))showCoach(); else if(name.equals("Nutrition"))showNutrition(); else showProfile(); });
            n.addView(b,new LinearLayout.LayoutParams(0,-1,1));
        }
        return n;
    }

    private Button action(String text) {
        Button b=new Button(this); b.setText(text); b.setAllCaps(false); b.setTextSize(15); b.setTextColor(Color.rgb(4,16,9));
        b.setTypeface(Typeface.DEFAULT_BOLD); b.setBackground(bg(GREEN,dp(15),GREEN));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(54)); p.setMargins(0,dp(12),0,dp(8)); b.setLayoutParams(p); return b;
    }

    private void showHome(){
        LinearLayout r=page("Edward 👋","Good Morning");
        r.addView(card("Today's Progress","234","Calories Burned"));
        TextView q=txt("Quick Actions",21,Color.WHITE,true); q.setPadding(0,dp(8),0,dp(10)); r.addView(q);
        String[] qs={"Workout Plan","AI Coach","Nutrition","Progress"};
        LinearLayout row=new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL);
        for(String s:qs){ TextView v=txt(s,12,Color.WHITE,true); v.setGravity(Gravity.CENTER); v.setPadding(dp(6),dp(20),dp(6),dp(20)); v.setBackground(bg(CARD,dp(18),Color.rgb(32,55,44))); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(86),1); p.setMargins(dp(3),0,dp(3),dp(12)); row.addView(v,p); }
        r.addView(row);
        LinearLayout w=card("TODAY'S WORKOUT","Push Day","Chest • Shoulders • Triceps");
        w.addView(action("Start Now")); r.addView(w);
        LinearLayout stats=card("This Week","7 Workouts","On track");
        stats.addView(txt("72.5 kg  •  4 Active Plans",14,MUTED,false)); r.addView(stats);
    }

    private void showWorkout(){
        LinearLayout r=page("Push Day","Workout");
        r.addView(txt("5 exercises  •  approx. 50 min",14,MUTED,false));
        String[][] ex={{"Bench Press","4 sets × 12 reps"},{"Incline Dumbbell Press","4 sets × 10 reps"},{"Shoulder Press","4 sets × 12 reps"},{"Triceps Pushdown","4 sets × 15 reps"},{"Dips","3 sets × 12 reps"}};
        for(String[] e:ex){ LinearLayout c=card(e[0],e[1],"60s rest"); r.addView(c); }
        r.addView(action("Start Workout"));
    }

    private void showCoach(){
        LinearLayout r=page("AI Coach","Online");
        LinearLayout c=card(null,"Hi Edward! 👋","How can I help you today?");
        r.addView(c);
        String[] a={"Create a workout plan","Suggest a meal plan","Analyze my progress","Answer fitness questions"};
        for(String s:a){ Button b=new Button(this); b.setText(s); b.setAllCaps(false); b.setTextColor(Color.WHITE); b.setBackground(bg(CARD,dp(14),Color.rgb(41,72,58))); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(52)); p.setMargins(0,dp(5),0,dp(5)); b.setLayoutParams(p); r.addView(b); }
        EditText in=new EditText(this); in.setHint("Type your message…"); in.setHintTextColor(MUTED); in.setTextColor(Color.WHITE); in.setSingleLine(true); in.setPadding(dp(14),0,dp(14),0); in.setBackground(bg(CARD,dp(14),Color.rgb(41,72,58))); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(56)); p.setMargins(0,dp(14),0,0); r.addView(in,p);
    }

    private void showNutrition(){
        LinearLayout r=page("Nutrition","Today");
        r.addView(card("Daily Calories","1,450","of 2,200 kcal"));
        r.addView(card("Protein","120g / 180g","67%"));
        r.addView(card("Carbs","150g / 250g","60%"));
        r.addView(card("Fats","45g / 70g","64%"));
        TextView h=txt("Meals",21,Color.WHITE,true); h.setPadding(0,dp(8),0,dp(10)); r.addView(h);
        r.addView(card("Breakfast","Oatmeal with fruits","320 kcal"));
        r.addView(card("Lunch","Chicken rice bowl","450 kcal"));
    }

    private void showProfile(){
        LinearLayout r=page("Profile","Premium Member");
        LinearLayout p=card("Edward James","48 Workouts","12 Weeks  •  72.5 kg");
        r.addView(p);
        String[] items={"My Plan","Achievements","Settings","Help & Support"};
        for(String s:items){ TextView v=txt(s+"   ›",16,Color.WHITE,true); v.setPadding(dp(16),dp(18),dp(16),dp(18)); v.setBackground(bg(CARD,dp(16),Color.rgb(32,55,44))); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.setMargins(0,0,0,dp(10)); r.addView(v,lp); }
        r.addView(card("Premium Plan","$9.99 / month","Active"));
    }
}

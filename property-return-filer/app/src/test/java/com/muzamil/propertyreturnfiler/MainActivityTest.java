package com.muzamil.propertyreturnfiler;

import static org.junit.Assert.*;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.EditText;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class MainActivityTest {

    @Test
    public void getStartedDashboardAndClientsFlowWorks() {
        ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class).setup();
        MainActivity activity = controller.get();

        View getStarted = findText(activity.getWindow().getDecorView(), "Get Started");
        assertNotNull("Splash must contain Get Started", getStarted);
        getStarted.performClick();
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        assertNotNull("Dashboard must show Good Morning", findText(activity.getWindow().getDecorView(), "Good Morning"));
        assertNotNull("Dashboard must show Total Clients", findText(activity.getWindow().getDecorView(), "Total Clients"));
        assertNotNull("Dashboard must show Quick Actions", findText(activity.getWindow().getDecorView(), "Quick Actions"));

        View clients = findText(activity.getWindow().getDecorView(), "Clients");
        assertNotNull("Dashboard must expose Clients", clients);
        clients.performClick();
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        assertNotNull("Clients screen must show search field", findHint(activity.getWindow().getDecorView(), "Search by name"));
        assertNotNull("Seeded client Ahmed Raza must render", findText(activity.getWindow().getDecorView(), "Ahmed Raza"));
    }

    private View findText(View v, String needle) {
        if (v instanceof TextView) {
            CharSequence s = ((TextView)v).getText();
            if (s != null && s.toString().contains(needle)) return v;
        }
        if (v instanceof ViewGroup) {
            ViewGroup g=(ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++){
                View hit=findText(g.getChildAt(i),needle);
                if(hit!=null)return hit;
            }
        }
        return null;
    }

    private View findHint(View v, String needle) {
        if (v instanceof EditText) {
            CharSequence s=((EditText)v).getHint();
            if(s!=null && s.toString().contains(needle))return v;
        }
        if(v instanceof ViewGroup){
            ViewGroup g=(ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++){
                View hit=findHint(g.getChildAt(i),needle);
                if(hit!=null)return hit;
            }
        }
        return null;
    }
}
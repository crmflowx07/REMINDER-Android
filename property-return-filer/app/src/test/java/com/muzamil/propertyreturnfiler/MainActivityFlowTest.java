package com.muzamil.propertyreturnfiler;

import static org.junit.Assert.*;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.os.Looper;
import org.robolectric.Shadows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 29)
public class MainActivityFlowTest {
    private TextView findText(View v, String needle){
        if(v instanceof TextView){
            CharSequence t=((TextView)v).getText();
            if(t!=null && t.toString().contains(needle)) return (TextView)v;
        }
        if(v instanceof ViewGroup){
            ViewGroup g=(ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++){
                TextView found=findText(g.getChildAt(i),needle);
                if(found!=null) return found;
            }
        }
        return null;
    }

    @Test public void getStartedOpensDashboard(){
        MainActivity activity= Robolectric.buildActivity(MainActivity.class).setup().get();
        View root=activity.getWindow().getDecorView();
        TextView getStarted=findText(root,"Get Started");
        assertNotNull("Get Started button must exist", getStarted);
        assertTrue("Get Started must be clickable", getStarted.isClickable());
        assertTrue("Get Started listener must execute", getStarted.callOnClick());
        Shadows.shadowOf(Looper.getMainLooper()).idle();

        View dashboard=activity.getWindow().getDecorView();
        assertNotNull("Dashboard must show Total Clients", findText(dashboard,"Total Clients"));
        assertNotNull("Dashboard must show Quick Actions", findText(dashboard,"Quick Actions"));
        assertNotNull("Dashboard must show Good Morning", findText(dashboard,"Good Morning"));
    }
}

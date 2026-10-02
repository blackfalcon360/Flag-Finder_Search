package flagfinder.blackfalcon.jan;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SplashActivity extends Activity {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable goNext = new Runnable() {
        @Override
        public void run() {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setBackgroundColor(Color.parseColor("#1E3A5F"));

        TextView line1 = new TextView(this);
        line1.setText("Brought to you by:");
        line1.setTextSize(18);
        line1.setTextColor(Color.parseColor("#CFD8DC"));
        line1.setGravity(Gravity.CENTER);

        TextView line2 = new TextView(this);
        line2.setText("Black Falcon \uD83E\uDD85");
        line2.setTextSize(32);
        line2.setTypeface(null, Typeface.BOLD);
        line2.setTextColor(Color.WHITE);
        line2.setGravity(Gravity.CENTER);
        line2.setPadding(0, (int) (10 * getResources().getDisplayMetrics().density), 0, 0);

        root.addView(line1, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(line2, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        setContentView(root);
        handler.postDelayed(goNext, 2200);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(goNext);
        super.onDestroy();
    }
}

package com.mi.FoodChoice;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.umeng.fb.fragment.FeedbackFragment;

public class FeedbackActivity extends FragmentActivity {

    private FeedbackFragment mFeedbackFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initSystemBar();
        initActionBar();
        setContentView(R.layout.feedback_activity);
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            String conversation_id = getIntent().getStringExtra(FeedbackFragment.BUNDLE_KEY_CONVERSATION_ID);
            mFeedbackFragment = FeedbackFragment.newInstance(conversation_id);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.feedback_content,mFeedbackFragment)
                    .commit();
        }
    }

    @Override
    protected void onNewIntent(android.content.Intent intent) {
        // mFeedbackFragment.addPushDevReply();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initSystemBar() {
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setNavigationBarTintEnabled(true);
        tintManager.setTintColor(getResources().getColor(R.color.dark_primary_color));
    }

    private void initActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_bg));
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }
}

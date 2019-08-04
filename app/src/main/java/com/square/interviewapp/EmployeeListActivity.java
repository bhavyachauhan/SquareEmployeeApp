package com.square.interviewapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonParseException;
import com.square.interviewapp.databinding.EmployeeListContentBinding;
import com.square.interviewapp.model.EmployeeDetails;
import com.square.interviewapp.retrofit.SquareAPIService;
import com.square.interviewapp.utils.EmployeeDataLoader;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * An activity representing a list of Employees. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link EmployeeDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class EmployeeListActivity extends AppCompatActivity {

    public static final String TAG = EmployeeListActivity.class.getCanonicalName();
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    List<EmployeeDetails> employeeDetailsList;
    BroadcastReceiver internetConnectivityListener;
    EmployeeListAdapter adapter;
    private RecyclerView recyclerView;
    private LinearLayout progressBarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        if (findViewById(R.id.employee_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        recyclerView = findViewById(R.id.employee_list);
        assert recyclerView != null;
        adapter = new EmployeeListAdapter(this, new ArrayList<EmployeeDetails>(), mTwoPane);
        recyclerView.setAdapter(adapter);
        progressBarLayout = findViewById(R.id.progress_bar_layout);
        registerBroadcastListener();

        new RecyclerViewSetupTask(this).execute();
    }

    private static class RecyclerViewSetupTask extends AsyncTask<Void, Void, List<EmployeeDetails>> {

        WeakReference<EmployeeListActivity> activityWeakReference;

        RecyclerViewSetupTask(EmployeeListActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            if (activityWeakReference.get() != null && activityWeakReference.get().progressBarLayout != null) {
                activityWeakReference.get().progressBarLayout.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected List<EmployeeDetails> doInBackground(Void... voids) {
            if (activityWeakReference.get() == null) {
                return null;
            }
            try {
                return EmployeeDataLoader.getEmployeeDetailsList(activityWeakReference.get());
//                return EmployeeDataLoader.getMalformedEmployeeDetailsList(activityWeakReference.get());
//                return EmployeeDataLoader.getEmptyEmployeeDetailsList(activityWeakReference.get());
            } catch (JsonParseException e) {
                e.printStackTrace();
                Log.e(TAG, "Received malformed response, ignoring result.", e);
                activityWeakReference.get().showError(R.drawable.error_icon, R.string.malformed_data_error, null);
            } catch (IOException e) {
                if (!(e instanceof UnknownHostException)) {
                    e.printStackTrace();
                    Log.e(TAG, "Received malformed response, ignoring result.", e);
                    activityWeakReference.get().showError(R.drawable.error_icon, R.string.download_failed, null);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<EmployeeDetails> employeeDetails) {
            if (employeeDetails != null && activityWeakReference.get() != null) {
                EmployeeListActivity activity = activityWeakReference.get();
                if (employeeDetails.isEmpty()) {
                    activity.showError(R.drawable.error_icon, R.string.no_records_found, null);
                } else {
                    activity.hideError();
                    activity.employeeDetailsList = employeeDetails;
                    if (activity.adapter == null) {
                        activity.recyclerView.setAdapter(new EmployeeListAdapter(activity, employeeDetails, activity.mTwoPane));
                    } else {
                        activity.adapter.updateEmployeeList(employeeDetails);
                    }
                    activity.progressBarLayout.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    private void showError(final int iconId, final int messageId, final View.OnClickListener listener) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.error_layout).setVisibility(View.VISIBLE);
                ((ImageView)findViewById(R.id.error_icon)).setImageResource(iconId);
                ((TextView)findViewById(R.id.error_message)).setText(messageId);
                Button retryButton = findViewById(R.id.retry_button);
                retryButton.setVisibility(listener == null ? View.INVISIBLE : View.VISIBLE);
                retryButton.setOnClickListener(listener);
            }
        });
    }

    private void hideError() {
        findViewById(R.id.error_layout).setVisibility(View.INVISIBLE);
    }

    private void registerBroadcastListener() {
        internetConnectivityListener = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showError(R.drawable.disconnected_icon, R.string.not_connected_error, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                new RecyclerViewSetupTask(EmployeeListActivity.this).execute();
                            }
                        });
                        progressBarLayout.setVisibility(View.INVISIBLE);
                    }
                });
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(internetConnectivityListener, new IntentFilter(SquareAPIService.INTERNET_DISCONNECTED_BROADCAST_KEY));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (internetConnectivityListener == null) {
            registerBroadcastListener();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(internetConnectivityListener);
        internetConnectivityListener = null;
    }

    static class EmployeeListAdapter extends RecyclerView.Adapter<ViewHolder> {

        private final EmployeeListActivity mParentActivity;
        List<EmployeeDetails> mEmployeeList;
        private final boolean mTwoPane;

        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EmployeeDetails item = (EmployeeDetails) view.getTag();
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putParcelable(EmployeeDetailFragment.ARG_LIST_ITEM, item);
                    EmployeeDetailFragment fragment = new EmployeeDetailFragment();
                    fragment.setArguments(arguments);
                    mParentActivity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.employee_detail_container, fragment)
                            .commit();
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, EmployeeDetailActivity.class);
                    intent.putExtra(EmployeeDetailFragment.ARG_LIST_ITEM, item);

                    context.startActivity(intent);
                }
            }
        };

        EmployeeListAdapter(EmployeeListActivity parent, List<EmployeeDetails> employeeList, boolean twoPane) {
            mParentActivity = parent;
            mEmployeeList = employeeList;
            mTwoPane = twoPane;
        }

        void updateEmployeeList(List<EmployeeDetails> employeeDetails) {
            mEmployeeList = employeeDetails;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            EmployeeListContentBinding binding = DataBindingUtil.inflate(inflater, R.layout.employee_list_content, parent, false);
            return new ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            EmployeeDetails details = mEmployeeList.get(position);
            holder.bind(details, mOnClickListener);
        }

        @Override
        public int getItemCount() {
            return mEmployeeList.size();
        }

    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        EmployeeListContentBinding binding;
        ImageView thumbImageView;
        TextView nameTextView, emailTextView, phoneTextView;

        ViewHolder(@NonNull EmployeeListContentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            thumbImageView = itemView.findViewById(R.id.employee_thumb_imageview);
            nameTextView = itemView.findViewById(R.id.name_textview);
            emailTextView = itemView.findViewById(R.id.email_textview);
            phoneTextView = itemView.findViewById(R.id.phone_textview);
        }

        void bind(final EmployeeDetails details, final View.OnClickListener listener) {
            itemView.setTag(details);
            binding.setListener(listener);
            binding.setEmployeeDetails(details);
            binding.executePendingBindings();
        }

    }

}

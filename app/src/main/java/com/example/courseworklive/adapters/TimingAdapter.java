package com.example.courseworklive.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.courseworklive.R;
import com.example.courseworklive.models.Logs;

import java.util.List;

public class TimingAdapter extends RecyclerView.Adapter<TimingAdapter.TimingViewHolder> {

    /**
     * This is the Timing Adapter used in the Recycler View
     * The main variables are the List that contains all the Subjects
     * as well as the listener used to pass the touch events back to the Dashboard Activity.
     * */

    private List<Logs> logsArrayList;
    private OnTimingListener timingListener;

    public TimingAdapter(List<Logs> logs, OnTimingListener timingListener){
        logsArrayList = logs;
        this.timingListener = timingListener;
    }

    //retrieving the template that is used to present the logs
    @Override
    public TimingAdapter.TimingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_log, parent, false);
        return new TimingAdapter.TimingViewHolder(view, timingListener);
    }

    //loading the data to the template used
    @Override
    public void onBindViewHolder(TimingAdapter.TimingViewHolder holder, final int position) {
        final Logs logs = logsArrayList.get(position);
        holder.date.setText(Logs.getDate(logs.getDate()));
        holder.length.setText(Logs.getTimeSpentMins(logs.getTimeSpent()));
    }

    //the size of the list of all elements used
    @Override
    public int getItemCount() {
        return logsArrayList.size();
    }

    /**
     * The view holder that loads the template that will present the data
     * In this case the Cardview used
     * But mainly the elements inside the Cardview
     * */
    public class TimingViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView date;
        private TextView length;
        private CardView cardView;
        OnTimingListener onTimingListener;

        //assigning the UI elements
        public TimingViewHolder(View itemView, OnTimingListener onTimingListener) {
            super(itemView);
            itemView.setOnClickListener(this);
            date = itemView.findViewById(R.id.logDate);
            length = itemView.findViewById(R.id.logTime);
            cardView = itemView.findViewById(R.id.overviewRecycler);
            this.onTimingListener = onTimingListener;
        }

        //assigning the listener
        @Override
        public void onClick(View view) {
            onTimingListener.onItemClick(getAdapterPosition());
        }
    }

    //adding a new Dataset in the adapter
    public void newDataSet(List<Logs> dataset){
        logsArrayList = dataset;
    }

    //the listener interface that is used to pass the onClick event to the activity
    public interface OnTimingListener {
        void onItemClick(int item);
    }

}

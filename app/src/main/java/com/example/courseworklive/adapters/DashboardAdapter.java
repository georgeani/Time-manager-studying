package com.example.courseworklive.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.courseworklive.R;
import com.example.courseworklive.models.Logs;
import com.example.courseworklive.models.Subject;

import java.util.List;

public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.DashboardViewHolder> {

    /**
     * This is the Dashboard Adapter used in the Recycler View
     * The main variables are the List that contains all the Subjects
     * as well as the listener used to pass the touch events back to the Dashboard Activity
     * */
    private List<Subject> subjectList;
    private OnSubjectDashboardListener onSubjectListener;

    public DashboardAdapter(List<Subject> subjectList, OnSubjectDashboardListener onSubjectListener){
        this.subjectList = subjectList;
        this.onSubjectListener = onSubjectListener;
    }

    //retrieving the template that is used to present the subjects
    @Override
    public DashboardAdapter.DashboardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dashboard, parent, false);
        return new DashboardViewHolder(view, onSubjectListener);
    }

    //loading the data to the template used
    @Override
    public void onBindViewHolder(DashboardAdapter.DashboardViewHolder holder, final int position) {
        final Subject subject = subjectList.get(position);
        holder.title.setText("Subject: " + subject.getSubjectTitle());
        holder.effort.setText("Suggested Effort: " + subject.getEffort());
        holder.dedicated.setText("Dedicated: " + Logs.getTimeSpentHours(subject.getTotalHours()));
    }

    //the size of the list of all elements used
    @Override
    public int getItemCount() {
        return subjectList.size();
    }

    /**
     * The view holder that loads the template that will present the data
     * In this case the Cardview used
     * But mainly the elements inside the Cardview
     * */
    public class DashboardViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener  {
        private TextView title;
        private TextView effort;
        private TextView dedicated;
        private CardView cardView;
        OnSubjectDashboardListener onSubjectListener;

        //assigning the UI elements
        public DashboardViewHolder(View itemView, OnSubjectDashboardListener onSubjectListener) {
            super(itemView);
            itemView.setOnClickListener(this);
            title = itemView.findViewById(R.id.textviewSubjectTitleDashboard);
            effort = itemView.findViewById(R.id.textviewEffortTitleDashboard);
            dedicated = itemView.findViewById(R.id.textviewDedicatedTitle);
            cardView = itemView.findViewById(R.id.listviewSubjects);
            this.onSubjectListener = onSubjectListener;
        }

        //assigning the listener
        @Override
        public void onClick(View view){
            onSubjectListener.onItemClick(getAdapterPosition());
        }

    }

    //the listener interface that is used to pass the onClick event to the activity
    public interface OnSubjectDashboardListener {
        void onItemClick(int item);
    }

}

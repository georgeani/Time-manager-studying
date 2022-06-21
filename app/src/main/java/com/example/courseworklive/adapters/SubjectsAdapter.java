package com.example.courseworklive.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.courseworklive.R;
import com.example.courseworklive.models.Subject;

import java.util.List;

public class SubjectsAdapter extends RecyclerView.Adapter<SubjectsAdapter.MyViewHolder> {

    /**
     * This is the Subjects Adapter used in the Recycler View
     * The main variables are the List that contains all the Subjects
     * as well as the listener used to pass the touch events back to the Dashboard Activity
     * and the Context load the recycler view UI.
     * */

    private List<Subject> subjectList;
    private Context context;
    private OnSubjectListener onSubjectListener;

    public SubjectsAdapter(List<Subject> subjectList, Context context, OnSubjectListener onSubjectListener) {
        this.subjectList = subjectList;
        this.context = context;
        this.onSubjectListener = onSubjectListener;
    }

    //used to remove a subject from the list
    public void removeSubject(int position){
        subjectList.remove(position);
    }

    //retrieving the template that is used to present the subjects
    @Override
    public SubjectsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subject, parent, false);
        return new MyViewHolder(view, onSubjectListener);
    }

    //loading the data to the template used
    @Override
    public void onBindViewHolder(SubjectsAdapter.MyViewHolder holder, final int position) {
        final Subject subject = subjectList.get(position);
        holder.title.setText("Subject: " + subject.getSubjectTitle());
        holder.effort.setText("Suggested Effort: " + subject.getEffort());
        holder.weight.setText("Weight: " + subject.getWeight());
        holder.start.setText("Starting Date: " + Subject.getDate(subject.getStartingDate()));
        holder.end.setText("End Date: " + Subject.getDate(subject.getEndDate()));
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
    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView title;
        private TextView weight;
        private TextView effort;
        private TextView start;
        private TextView end;
        private CardView cardView;
        OnSubjectListener onSubjectListener;

        //assigning the UI elements
        public MyViewHolder(View itemView, OnSubjectListener onSubjectListener) {
            super(itemView);
            itemView.setOnClickListener(this);
            title = itemView.findViewById(R.id.textviewSubjectTitle);
            weight = itemView.findViewById(R.id.textviewSubjectWeight);
            effort = itemView.findViewById(R.id.textviewEffortTitle);
            start = itemView.findViewById(R.id.textviewStartTitle);
            end = itemView.findViewById(R.id.textviewEndTitle);
            cardView = itemView.findViewById(R.id.listviewSubjects);
            this.onSubjectListener = onSubjectListener;
        }

        //assigning the listener
        @Override
        public void onClick(View view) {
            onSubjectListener.onItemClick(getAdapterPosition());
        }
    }

    //the listener interface that is used to pass the onClick event to the activity
    public interface OnSubjectListener {
        void onItemClick(int item);
    }

}

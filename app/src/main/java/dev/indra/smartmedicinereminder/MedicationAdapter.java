package dev.indra.smartmedicinereminder.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import com.google.android.material.switchmaterial.SwitchMaterial;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dev.indra.smartmedicinereminder.R;
import dev.indra.smartmedicinereminder.model.Medication;

public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.ViewHolder> {

    private final Context context;
    private final List<Medication> medicationList;
    private final OnMedicationListener onMedicationListener;

    public MedicationAdapter(Context context, List<Medication> medicationList, OnMedicationListener onMedicationListener) {
        this.context = context;
        this.medicationList = medicationList;
        this.onMedicationListener = onMedicationListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_medication, parent, false);
        return new ViewHolder(view, onMedicationListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Medication medication = medicationList.get(position);

        holder.tvName.setText(medication.getName());
        holder.tvDosage.setText(medication.getDosage());
        holder.tvTime.setText(medication.getTimeString());
        holder.switchActive.setChecked(medication.isActive());

        if (medication.getNotes() != null && !medication.getNotes().isEmpty()) {
            holder.tvNotes.setVisibility(View.VISIBLE);
            holder.tvNotes.setText(medication.getNotes());
        } else {
            holder.tvNotes.setVisibility(View.GONE);
        }

        holder.switchActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
                onMedicationListener.onSwitchChanged(holder.getAdapterPosition(), isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return medicationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDosage, tvTime, tvNotes;
//        Switch switchActive;
        SwitchMaterial switchActive;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView, OnMedicationListener onMedicationListener) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvName);
            tvDosage = itemView.findViewById(R.id.tvDosage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvNotes = itemView.findViewById(R.id.tvNotes);
            switchActive = itemView.findViewById(R.id.switchActive);
            btnDelete = itemView.findViewById(R.id.btnDelete);

            itemView.setOnClickListener(v -> {
                if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                    onMedicationListener.onMedicationClick(getAdapterPosition());
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                    onMedicationListener.onDeleteClick(getAdapterPosition());
                }
            });
        }
    }

    public interface OnMedicationListener {
        void onMedicationClick(int position);
        void onDeleteClick(int position);
        void onSwitchChanged(int position, boolean isChecked);
    }
}
package com.techotd.matrimo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.techotd.matrimo.R;
import com.techotd.matrimo.model.TemplateModel;

import java.util.List;

public class TemplateAdapter extends RecyclerView.Adapter<TemplateAdapter.ViewHolder> {
    private Context context;
    private List<TemplateModel> templates;
    private OnTemplateClickListener listener;

    public interface OnTemplateClickListener {
        void onTemplateClick(TemplateModel template);
    }

    public TemplateAdapter(Context context, List<TemplateModel> templates, OnTemplateClickListener listener) {
        this.context = context;
        this.templates = templates;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_template, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TemplateModel template = templates.get(position);
        holder.name.setText(template.getName());
        Glide.with(context).load(template.getPreviewResId()).into(holder.preview);
        holder.itemView.setOnClickListener(v -> listener.onTemplateClick(template));
    }

    @Override
    public int getItemCount() {
        return templates.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView preview;

        ViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.template_name);
            preview = view.findViewById(R.id.template_preview);
        }
    }
}
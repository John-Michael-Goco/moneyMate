package com.moneymate.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.moneymate.R;

public class CategoryFragment extends Fragment {

    private String[] categoryTitles;
    private int[] images;

    public CategoryFragment() {
    }
    public CategoryFragment(String[] categoryTitles, int[] images) {
        this.categoryTitles = categoryTitles;
        this.images = images;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_category, container, false);
        GridLayout cardGrid = root.findViewById(R.id.cardGrid);

        // Populate the grid with cards
        for (int i = 0; i < categoryTitles.length; i++) {
            addCard(inflater, cardGrid, categoryTitles[i], images[i]);
        }

        return root;
    }

    private void addCard(LayoutInflater inflater, GridLayout cardGrid, String title, int imageResId) {
        // Ensure we inflate the correct card layout
        View cardView = inflater.inflate(R.layout.card_item, cardGrid, false);

        ImageView imageView = cardView.findViewById(R.id.cardImage);
        TextView textView = cardView.findViewById(R.id.cardTitle);

        imageView.setImageResource(imageResId);
        textView.setText(title);

        cardGrid.addView(cardView);
    }
}

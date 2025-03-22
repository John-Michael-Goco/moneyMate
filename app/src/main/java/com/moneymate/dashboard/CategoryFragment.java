package com.moneymate.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
        LinearLayout cardContainer = root.findViewById(R.id.cardContainer);

        // Populate the LinearLayout with cards
        for (int i = 0; i < categoryTitles.length; i++) {
            addCard(inflater, cardContainer, categoryTitles[i], images[i]);
        }

        return root;
    }

    private void addCard(LayoutInflater inflater, LinearLayout cardContainer, String title, int imageResId) {
        // Inflate card layout and add to LinearLayout
        View cardView = inflater.inflate(R.layout.card_item, cardContainer, false);

        ImageView imageView = cardView.findViewById(R.id.cardImage);
        TextView textView = cardView.findViewById(R.id.cardTitle);

        imageView.setImageResource(imageResId);
        textView.setText(title);

        cardContainer.addView(cardView);
    }
}

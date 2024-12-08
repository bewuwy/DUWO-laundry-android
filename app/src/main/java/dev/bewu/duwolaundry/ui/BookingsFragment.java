package dev.bewu.duwolaundry.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.bewu.duwolaundry.LaundryApplication;
import dev.bewu.duwolaundry.MultiPossScraper;
import dev.bewu.duwolaundry.R;


public class BookingsFragment extends Fragment {

    public BookingsFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bookings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MultiPossScraper scraper = ((LaundryApplication) requireContext().getApplicationContext())
                .getMultiPossScraper();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {

            List<String> bookings = scraper.fetchBookings();
            String bookingsString;
            if (bookings.isEmpty()) {
                bookingsString = getString(R.string.no_bookings);
            } else {
                bookingsString = String.join("\n\n", bookings);
            }

            handler.post(() -> {

                TextView bookingsText = requireActivity().findViewById(R.id.bookingsText);
                bookingsText.setText(bookingsString);
            });
        });
    }
}
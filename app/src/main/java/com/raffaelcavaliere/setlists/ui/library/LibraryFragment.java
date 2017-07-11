package com.raffaelcavaliere.setlists.ui.library;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.raffaelcavaliere.setlists.R;

import java.util.ArrayList;
import java.util.List;

public class LibraryFragment extends Fragment {
    private OnFragmentInteractionListener mListener;

    public static TabLayout tabLayout;
    public static ViewPager viewPager;
    public static int int_items = 3;
    private int startPosition = 0;
    private List<LibraryUpdateListener> updateListeners;

    public LibraryFragment() {
        updateListeners = new ArrayList<>();
    }

    public static LibraryFragment newInstance() {
        LibraryFragment fragment = new LibraryFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            startPosition = savedInstanceState.getInt("position", 0);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("position", viewPager.getCurrentItem());
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_library, container, false);
        tabLayout = (TabLayout) v.findViewById(R.id.library_tabs);
        viewPager = (ViewPager) v.findViewById(R.id.library_pager);
        viewPager.setAdapter(new Adapter(getChildFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);
        viewPager.setCurrentItem(startPosition);
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public synchronized void registerLibraryUpdateListener(LibraryUpdateListener listener) {
        updateListeners.add(listener);
    }

    public synchronized void unregisterLibraryUpdateListener(LibraryUpdateListener listener) {
        updateListeners.remove(listener);
    }

    public synchronized void libraryUpdated() {
        for (LibraryUpdateListener listener : updateListeners) {
            listener.onLibraryUpdate();
        }
    }

    public interface LibraryUpdateListener {
        void onLibraryUpdate();
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    class Adapter extends FragmentPagerAdapter {

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position)
        {
            switch (position){
                case 0 : return ArtistsFragment.newInstance();
                case 1 : return SongsFragment.newInstance();
                case 2 : return SetsFragment.newInstance();
            }
            return null;
        }

        @Override
        public int getCount() {
            return int_items;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            switch (position){
                case 0 :
                    return getString(R.string.tab_artists);
                case 1 :
                    return getString(R.string.tab_songs);
                case 2:
                    return getString(R.string.tab_setlists);
            }
            return null;
        }
    }
}

package com.example.styleplt.adapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.styleplt.fragment.BoardFragment;
import com.example.styleplt.fragment.HomeFragment;
import com.example.styleplt.fragment.ProfileFragment;

import org.jetbrains.annotations.NotNull;

public class ContentsPagerAdapter extends FragmentStateAdapter {
    private int mPageCount = 3;



    public ContentsPagerAdapter(AppCompatActivity fm) {
        super(fm);
        //this.mPageCount = pageCount;
    }

    @NonNull
    @NotNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {

            case 0:
                HomeFragment homeFragment = new HomeFragment();
                return homeFragment;
            case 1:
                 BoardFragment boardFragment= new BoardFragment();
                return boardFragment;
            case 2:
                ProfileFragment profileFragment = new ProfileFragment();
                return profileFragment;

            default:
                return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public int getItemCount() {
        return mPageCount;
    }
}

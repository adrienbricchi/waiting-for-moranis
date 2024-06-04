/*
 * Waiting For Moranis
 * Copyright (C) 2020-2024
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.adrienbricchi.waitingformoranis.ui.main;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import org.adrienbricchi.waitingformoranis.databinding.ActivityMainBinding;
import org.adrienbricchi.waitingformoranis.ui.main.movieList.MovieListFragment;
import org.adrienbricchi.waitingformoranis.ui.main.showList.ShowListFragment;
import org.adrienbricchi.waitingformoranis.ui.preferences.SettingsActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static androidx.viewpager2.widget.ViewPager2.*;
import static org.adrienbricchi.waitingformoranis.R.dimen.fab_margin;
import static org.adrienbricchi.waitingformoranis.R.id.action_settings;
import static org.adrienbricchi.waitingformoranis.R.menu.menu_main;
import static org.adrienbricchi.waitingformoranis.R.string.onboarding_text_movie;
import static org.adrienbricchi.waitingformoranis.R.string.onboarding_text_show;


public class MainActivity extends AppCompatActivity {


    private static final String LOG_TAG = "MainActivity";

    public static final String FRAGMENT_REQUEST = "main_activity";
    public static final String FRAGMENT_ADD_FAB_BUTTON_CLICKED = "add_fab_button_clicked";


    // Every time a DB request is performed in sub-fragments, It gives back the result count to this main activity cache.
    // This is not very elegant, but this cache prevents some painful-ugly callbacks to the sub-Fragments.
    private final Map<Integer, Integer> itemsCountCache = new HashMap<>();

    private ActivityMainBinding binding;
    private Animator openingAnimation;
    private Animator closingAnimation;


    // <editor-fold desc="LifeCycle">


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this);
        binding.viewPager.setAdapter(sectionsPagerAdapter);

        new TabLayoutMediator(
                binding.tabs,
                binding.viewPager,
                true,
                sectionsPagerAdapter::setupTabTitleAndIcon
        ).attach();

        binding.addMovieFab.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putBoolean(FRAGMENT_ADD_FAB_BUTTON_CLICKED, true);
            getSupportFragmentManager().setFragmentResult(FRAGMENT_REQUEST, bundle);
        });

        binding.tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override public void onTabSelected(TabLayout.Tab tab) {
                binding.addMovieFab.hide();
            }


            @Override public void onTabUnselected(TabLayout.Tab tab) { }


            @Override public void onTabReselected(TabLayout.Tab tab) { }

        });

        binding.addMovieFab.addOnShowAnimationListener(new Animator.AnimatorListener() {

            @Override public void onAnimationStart(Animator animation) {
                Log.d(LOG_TAG, "--SHOW------- onFabAnimationStart " + itemsCountCache);
                int newFragmentItemsCount = itemsCountCache.getOrDefault(binding.viewPager.getCurrentItem(), 0);
                if (newFragmentItemsCount == 0) {
                    triggerOnBoardOpeningAnimation();
                }
            }


            @Override public void onAnimationEnd(Animator animation) {
                Log.d(LOG_TAG, "--SHOW------- onFabAnimationEnd");
            }


            @Override public void onAnimationCancel(Animator animation) {
                Log.d(LOG_TAG, "--SHOW------- onFabAnimationCancel " + itemsCountCache);
                Optional.ofNullable(openingAnimation)
                        .filter(a -> a.isRunning() || a.isStarted())
                        .ifPresent(Animator::cancel);
            }


            @Override public void onAnimationRepeat(Animator animation) {
                Log.d(LOG_TAG, "--SHOW------- onFabAnimationRepeat");
            }

        });

        binding.addMovieFab.addOnHideAnimationListener(new Animator.AnimatorListener() {

            @Override public void onAnimationStart(Animator animation) {
                Log.d(LOG_TAG, "-------HIDE-- onFabAnimationStart");
                int previousFragmentItemsCount = itemsCountCache.getOrDefault(binding.viewPager.getCurrentItem(), 0);
                if (previousFragmentItemsCount == 0) {
                    Log.d(LOG_TAG, "previousFragmentItemsCount == 0");
                    triggerOnBoardCloseAnimation();
                }
            }


            @Override public void onAnimationEnd(Animator animation) {
                Log.d(LOG_TAG, "-------HIDE-- onFabAnimationEnd");
            }


            @Override public void onAnimationCancel(Animator animation) {
                Log.d(LOG_TAG, "-------HIDE-- onFabAnimationCancel");
                Optional.ofNullable(closingAnimation)
                        .filter(a -> a.isRunning() || a.isStarted())
                        .ifPresent(Animator::cancel);
            }


            @Override public void onAnimationRepeat(Animator animation) {
                Log.d(LOG_TAG, "-------HIDE-- onFabAnimationRepeat");
            }

        });

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {

            @Override public void onPageScrollStateChanged(int state) {
                switch (state) {

                    case SCROLL_STATE_DRAGGING:
                        binding.addMovieFab.hide();
                        break;

                    case SCROLL_STATE_SETTLING:
                        break;

                    case SCROLL_STATE_IDLE:
                    default:
                        binding.addMovieFab.show();
                }
            }

        });

        getSupportFragmentManager().setFragmentResultListener(
                MovieListFragment.FRAGMENT_REQUEST,
                this,
                (requestKey, bundle) -> {
                    Log.d(LOG_TAG, "MovieListFragment.FRAGMENT_RESULT_MOVIES_COUNT");

                    itemsCountCache.put(0, bundle.getInt(MovieListFragment.FRAGMENT_RESULT_MOVIES_COUNT));
                    boolean isOnBoardVisible = binding.onboardingView.getVisibility() == VISIBLE;
                    boolean hasMovies = bundle.getInt(MovieListFragment.FRAGMENT_RESULT_MOVIES_COUNT) > 0;

                    if (!hasMovies && !isOnBoardVisible) {
                        Log.d(LOG_TAG, "!hasMovies && !isOnBoardVisible");
                        triggerOnBoardOpeningAnimation();
                    } else if (isOnBoardVisible && hasMovies) {
                        Log.d(LOG_TAG, "isOnBoardVisible && hasMovies");
                        triggerOnBoardCloseAnimation();
                    }
                }
        );

        getSupportFragmentManager().setFragmentResultListener(
                ShowListFragment.FRAGMENT_REQUEST,
                this,
                (requestKey, bundle) -> {
                    Log.d(LOG_TAG, "ShowListFragment.FRAGMENT_RESULT_SHOWS_COUNT");

                    itemsCountCache.put(1, bundle.getInt(ShowListFragment.FRAGMENT_RESULT_SHOWS_COUNT));
                    boolean isOnBoardVisible = binding.onboardingView.getVisibility() == VISIBLE;
                    boolean hasShows = bundle.getInt(ShowListFragment.FRAGMENT_RESULT_SHOWS_COUNT) > 0;

                    if (!hasShows && !isOnBoardVisible) {
                        Log.d(LOG_TAG, "!hasShows && !isOnBoardVisible");
                        triggerOnBoardOpeningAnimation();
                    } else if (isOnBoardVisible && hasShows) {
                        Log.d(LOG_TAG, "isOnBoardVisible && hasShows");
                        triggerOnBoardCloseAnimation();
                    }
                }
        );
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == action_settings) {

            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    // </editor-fold desc="LifeCycle">


    private @NonNull Point computeFabCenterCoordinates() {

        int buttonMargin = getResources().getDimensionPixelSize(fab_margin);
        int buttonWidth = binding.addMovieFab.getWidth();
        int buttonHeight = binding.addMovieFab.getHeight();
        int cx = (getApplicationContext().getResources().getConfiguration().getLayoutDirection() == LAYOUT_DIRECTION_LTR)
                 ? binding.onboardingView.getRight() - buttonMargin - (buttonWidth / 2)
                 : binding.onboardingView.getLeft() + buttonMargin + (buttonWidth / 2);
        int cy = binding.onboardingView.getBottom() - buttonMargin - (buttonHeight / 2);

        return new Point(cx, cy);
    }


    private void triggerOnBoardCloseAnimation() {

        if (Optional.ofNullable(closingAnimation)
                    .map(a -> a.isStarted() || a.isRunning())
                    .orElse(false)) {
            return;
        }

        Point fabCenter = computeFabCenterCoordinates();
        float initialRadius = (float) Math.hypot(fabCenter.x, fabCenter.y);

        // Create the animation (the final radius is zero)
        closingAnimation = ViewAnimationUtils.createCircularReveal(
                binding.onboardingView,
                fabCenter.x,
                fabCenter.y,
                initialRadius,
                0f
        );

        // Make the view invisible when the animation is done
        closingAnimation.addListener(new AnimatorListenerAdapter() {

            @Override public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                Log.d(LOG_TAG, "closingAnimation onAnimationStart");
                binding.onboardingView.setVisibility(VISIBLE);
            }


            @Override public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (closingAnimation != null) {
                    Log.d(LOG_TAG, "closingAnimation onAnimationEnd");
                    binding.onboardingView.setVisibility(GONE);
                }
            }

        });

        // Start the animation
        openingAnimation = null;
        binding.onboardingView.clearAnimation();
        closingAnimation.start();
    }


    private void triggerOnBoardOpeningAnimation() {

        if (Optional.ofNullable(openingAnimation)
                    .map(a -> a.isStarted() || a.isRunning())
                    .orElse(false)) {
            return;
        }

        Point fabCenter = computeFabCenterCoordinates();
        float finalRadius = (float) Math.hypot(fabCenter.x, fabCenter.y);

        // Create the animation (the initial radius is zero)
        openingAnimation = ViewAnimationUtils.createCircularReveal(
                binding.onboardingView,
                fabCenter.x,
                fabCenter.y,
                0f,
                finalRadius
        );

        // Make the view visible when the animation starts
        openingAnimation.addListener(new AnimatorListenerAdapter() {

            @Override public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                Log.d(LOG_TAG, "openingAnimation onAnimationStart");
                binding.onboardingViewText.setText((binding.viewPager.getCurrentItem() == 0)
                                                   ? onboarding_text_movie
                                                   : onboarding_text_show);

                binding.onboardingView.setVisibility(VISIBLE);
            }


            @Override public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (openingAnimation != null) {
                    Log.d(LOG_TAG, "openingAnimation onAnimationEnd");
                    binding.onboardingView.setVisibility(VISIBLE);
                }
            }

        });

        // Start the animation
        closingAnimation = null;
        binding.onboardingView.clearAnimation();
        openingAnimation.start();
    }


}

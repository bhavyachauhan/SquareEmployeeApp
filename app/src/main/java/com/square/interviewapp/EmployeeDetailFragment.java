package com.square.interviewapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.square.interviewapp.databinding.EmployeeDetailBinding;
import com.square.interviewapp.model.EmployeeDetails;
import com.square.interviewapp.utils.ImageLoader;

/**
 * A fragment representing a single Employee detail screen.
 * This fragment is either contained in a {@link EmployeeListActivity}
 * in two-pane mode (on tablets) or a {@link EmployeeDetailActivity}
 * on handsets.
 */
public class EmployeeDetailFragment extends Fragment {

    @SuppressWarnings("WeakerAccess")
    public static final String ARG_LIST_ITEM = "list_item";

    private EmployeeDetails mItem;
    private Animator currentAnimator;
    private LinearLayout progressBarLayout;
    private ImageView expandedImageView;
    private ViewGroup rootLayout;
    private ViewGroup largeImageViewContainer;

    public EmployeeDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null && getArguments().containsKey(ARG_LIST_ITEM)) {
            mItem = getArguments().getParcelable(ARG_LIST_ITEM);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        EmployeeDetailBinding binding = DataBindingUtil.inflate(inflater, R.layout.employee_detail, container, false);

        final View rootView = binding.getRoot();

        if (mItem != null) {
            binding.setEmployeeDetails(mItem);
            progressBarLayout = rootView.findViewById(R.id.progress_bar_layout);
            expandedImageView = rootView.findViewById(R.id.large_imageview);
            rootLayout = rootView.findViewById(R.id.root_layout);
            largeImageViewContainer = rootView.findViewById(R.id.large_imageview_container);
            binding.setListener(listener);
        }

        return rootView;
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.thumb_imageview && view instanceof ImageView) {
                onThumbClicked((ImageView) view);
            }
        }
    };

    private void onThumbClicked(final ImageView thumbImageView) {
        final int duration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        progressBarLayout.setVisibility(View.VISIBLE);
        ImageLoader.get(getContext()).loadImage(mItem.getPhotoUrlLarge(), expandedImageView, new ImageLoader.OnCompleteListener() {
            @Override
            public void onSuccess() {
                if (getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBarLayout.setVisibility(View.INVISIBLE);
                        zoomImageFromThumb(rootLayout, thumbImageView, largeImageViewContainer, duration);
                    }
                });
            }

            @Override
            public void onFailure() {
                progressBarLayout.setVisibility(View.INVISIBLE);
                showError();
            }
        });
    }

    private void showError() {
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.error_title)
                        .setMessage("Failed to download image. Please try agian.")
                        .setNeutralButton(R.string.okay, null).create().show();
            }
        });
    }

    // Reference: https://developer.android.com/training/animation/zoom
    private void zoomImageFromThumb(final View rootLayout, final View thumbView, final View expandedView, final int duration) {
        if (currentAnimator != null) {
            currentAnimator.cancel();
        }


        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        rootLayout.getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        expandedView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedView.setPivotX(0f);
        expandedView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedView, View.SCALE_X,
                        startScale, 1f))
                .with(ObjectAnimator.ofFloat(expandedView,
                        View.SCALE_Y, startScale, 1f));
        set.setDuration(duration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                currentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                currentAnimator = null;
            }
        });
        set.start();
        currentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        expandedView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentAnimator != null) {
                    currentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(expandedView, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(expandedView,
                                        View.Y, startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(expandedView,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(expandedView,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(duration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedView.setVisibility(View.GONE);
                        currentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedView.setVisibility(View.GONE);
                        currentAnimator = null;
                    }
                });
                set.start();
                currentAnimator = set;
            }
        });
    }
}

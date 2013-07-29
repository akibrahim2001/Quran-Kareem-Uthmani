package com.cepmuvakkit.kuran.ui.fragment;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.PaintDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.actionbarsherlock.app.SherlockFragment;
import com.cepmuvakkit.kuran.data.Constants;
import com.cepmuvakkit.kuran.data.QuranInfo;
import com.cepmuvakkit.kuran.ui.PagerActivity;
import com.cepmuvakkit.kuran.ui.helpers.AyahTracker;
import com.cepmuvakkit.kuran.ui.helpers.QuranDisplayHelper;
import com.cepmuvakkit.kuran.ui.util.TranslationTask;
import com.cepmuvakkit.kuran.widgets.TranslationView;
import com.cepmuvakkit.kuran.R;

public class TranslationFragment extends SherlockFragment
    implements AyahTracker {
  private static final String TAG = "TranslationPageFragment";
  private static final String PAGE_NUMBER_EXTRA = "pageNumber";

  private static final String SI_PAGE_NUMBER = "SI_PAGE_NUMBER";
  private static final String SI_HIGHLIGHTED_AYAH = "SI_HIGHLIGHTED_AYAH";

  private int mPageNumber;
  private int mHighlightedAyah;
  private TranslationView mTranslationView;
  private PaintDrawable mLeftGradient, mRightGradient = null;

  private View mMainView;
  private ImageView mLeftBorder, mRightBorder;

  private Resources mResources;
  private SharedPreferences mPrefs;
  private boolean mJustCreated;

  public static TranslationFragment newInstance(int page) {
    final TranslationFragment f = new TranslationFragment();
    final Bundle args = new Bundle();
    args.putInt(PAGE_NUMBER_EXTRA, page);
    f.setArguments(args);
    return f;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mPageNumber = getArguments() != null ?
        getArguments().getInt(PAGE_NUMBER_EXTRA) : -1;
    if (savedInstanceState != null) {
      int page = savedInstanceState.getInt(SI_PAGE_NUMBER, -1);
      if (page == mPageNumber) {
        int highlightedAyah =
            savedInstanceState.getInt(SI_HIGHLIGHTED_AYAH, -1);
        if (highlightedAyah > 0) {
          mHighlightedAyah = highlightedAyah;
        }
      }
    }
    int width = getActivity().getWindowManager()
        .getDefaultDisplay().getWidth();
    mLeftGradient = QuranDisplayHelper.getPaintDrawable(width, 0);
    mRightGradient = QuranDisplayHelper.getPaintDrawable(0, width);
    setHasOptionsMenu(true);
  }

  @Override
  public View onCreateView(LayoutInflater inflater,
                           ViewGroup container, Bundle savedInstanceState) {
    final View view = inflater.inflate(
        R.layout.translation_layout, container, false);
    view.setBackgroundDrawable((mPageNumber % 2 == 0 ?
        mLeftGradient : mRightGradient));

    mPrefs = PreferenceManager
        .getDefaultSharedPreferences(getActivity());
    mResources = getResources();


    mLeftBorder = (ImageView) view.findViewById(R.id.left_border);
    mRightBorder = (ImageView) view.findViewById(R.id.right_border);

    mTranslationView = (TranslationView) view
        .findViewById(R.id.translation_text);
    mTranslationView.setTranslationClickedListener(
        new TranslationView.TranslationClickedListener() {
          @Override
          public void onTranslationClicked() {
            ((PagerActivity) getActivity()).toggleActionBar();
          }
        });

    mMainView = view;
    updateView();
    mJustCreated = true;

    String database = mPrefs.getString(
        Constants.PREF_ACTIVE_TRANSLATION, null);
    refresh(database);
    return view;
  }

  private void updateView() {
    if (getActivity() == null || mResources == null ||
        mMainView == null || !isAdded()) {
      return;
    }

    mMainView.setBackgroundDrawable((mPageNumber % 2 == 0 ?
        mLeftGradient : mRightGradient));

    if (!mPrefs.getBoolean(Constants.PREF_USE_NEW_BACKGROUND, true)) {
      mMainView.setBackgroundColor(mResources.getColor(R.color.page_background));
    }
    if (mPrefs.getBoolean(Constants.PREF_NIGHT_MODE, false)) {
      mMainView.setBackgroundColor(Color.BLACK);
    }

    int lineImageId = R.drawable.dark_line;
    int leftBorderImageId = R.drawable.border_left;
    int rightBorderImageId = R.drawable.border_right;
    if (mPrefs.getBoolean(Constants.PREF_NIGHT_MODE, false)) {
      leftBorderImageId = R.drawable.night_left_border;
      rightBorderImageId = R.drawable.night_right_border;
      lineImageId = R.drawable.light_line;
    }

    if (mPageNumber % 2 == 0) {
      mRightBorder.setVisibility(View.GONE);
      mLeftBorder.setBackgroundResource(leftBorderImageId);
    } else {
      mRightBorder.setVisibility(View.VISIBLE);
      mRightBorder.setBackgroundResource(rightBorderImageId);
      mLeftBorder.setBackgroundResource(lineImageId);
    }
  }

  @Override
  public void highlightAyah(int sura, int ayah) {
    if (mTranslationView != null) {
      mHighlightedAyah = QuranInfo.getAyahId(sura, ayah);
      mTranslationView.highlightAyah(mHighlightedAyah);
    }
  }

  @Override
  public void unHighlightAyat() {
    if (mTranslationView != null) {
      mTranslationView.unhighlightAyat();
      mHighlightedAyah = -1;
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    if (!mJustCreated) {
      updateView();
      mTranslationView.refresh();
    }
    mJustCreated = false;
  }

  public void refresh(String database) {
    if (database != null) {
      Activity activity = getActivity();
      if (activity != null) {
        new TranslationTask(activity, mPageNumber,
            mHighlightedAyah, database, mTranslationView).execute();
      }
    }
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    if (mHighlightedAyah > 0) {
      outState.putInt(SI_HIGHLIGHTED_AYAH, mHighlightedAyah);
    }
    super.onSaveInstanceState(outState);
  }
}

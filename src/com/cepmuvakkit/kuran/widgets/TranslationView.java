package com.cepmuvakkit.kuran.widgets;

import android.app.Service;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.text.ClipboardManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.cepmuvakkit.kuran.common.QuranAyah;
import com.cepmuvakkit.kuran.data.QuranInfo;
import com.cepmuvakkit.kuran.ui.util.ArabicTypefaceSpan;
import com.cepmuvakkit.kuran.util.ArabicStyle;
import com.cepmuvakkit.kuran.util.QuranScreenInfo;
import com.cepmuvakkit.kuran.util.QuranSettings;
import com.cepmuvakkit.kuran.R;

import java.util.List;

public class TranslationView extends ScrollView {

   private Context mContext;
   private int mDividerColor;
   private int mLeftRightMargin;
   private int mTopBottomMargin;
   private int mTextStyle;
   private int mHighlightedStyle;
   private int mFontSize;
   private int mHeaderColor;
   private int mHeaderStyle;
   private boolean mIsArabic;
   private boolean mUseArabicFont;
   private boolean mShouldReshape;
   private int mLastHighlightedAyah;
   private boolean mIsNightMode;
   private int mNightModeTextColor;

   private List<QuranAyah> mAyat;

   private LinearLayout mLinearLayout;
   private TranslationClickedListener mTranslationClickedListener;

   public TranslationView(Context context){
      super(context);
      init(context);
   }

   public TranslationView(Context context, AttributeSet attrs){
      super(context, attrs);
      init(context);
   }

   public TranslationView(Context context, AttributeSet attrs, int defStyle){
      super(context, attrs, defStyle);
      init(context);
   }

   public void init(Context context){
      mContext = context;
      setFillViewport(true);
      mLinearLayout = new LinearLayout(context);
      mLinearLayout.setOrientation(LinearLayout.VERTICAL);
      addView(mLinearLayout, ScrollView.LayoutParams.MATCH_PARENT,
              ScrollView.LayoutParams.WRAP_CONTENT);
      mLinearLayout.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            if (mTranslationClickedListener != null){
               mTranslationClickedListener.onTranslationClicked();
            }
         }
      });

      Resources resources = getResources();
      mDividerColor = resources.getColor(R.color.translation_hdr_color);
      mLeftRightMargin = resources.getDimensionPixelSize(
          R.dimen.translation_left_right_margin);
      mTopBottomMargin = resources.getDimensionPixelSize(
          R.dimen.translation_top_bottom_margin);
      mHeaderColor = resources.getColor(R.color.translation_sura_header);
      mHeaderStyle = R.style.translation_sura_title;
      initResources();
   }

   private void initResources(){
      mFontSize = QuranSettings.getTranslationTextSize(mContext);

      mIsArabic = QuranSettings.isArabicNames(mContext);
      mShouldReshape = QuranSettings.isReshapeArabic(mContext);
      mUseArabicFont = QuranSettings.needArabicFont(mContext);

      mIsNightMode = QuranSettings.isNightMode(mContext);
      if (mIsNightMode) {
         int brightness = QuranSettings.getNightModeTextBrightness(mContext);
         mNightModeTextColor = Color.rgb(brightness, brightness, brightness);
      }
      mTextStyle = mIsNightMode ? R.style.TranslationText_NightMode :
              R.style.TranslationText;
      mHighlightedStyle = mIsNightMode?
              R.style.TranslationText_NightMode_Highlighted :
              R.style.TranslationText_Highlighted;
   }

   public void refresh(){
      initResources();
      setAyahs(mAyat);
   }

   public void setAyahs(List<QuranAyah> ayat){
      mLastHighlightedAyah = -1;

      mLinearLayout.removeAllViews();
      mAyat = ayat;

      int currentSura = 0;
      for (QuranAyah ayah : ayat){
         if (ayah.getSura() != currentSura){
            addSuraHeader(ayah.getSura());
            currentSura = ayah.getSura();
         }
         addTextForAyah(ayah);
      }
   }

   public void unhighlightAyat(){
      if (mLastHighlightedAyah > 0){
         TextView text = (TextView)mLinearLayout
                 .findViewById(mLastHighlightedAyah);
         if (text != null){
            text.setTextAppearance(getContext(), mTextStyle);
            if (mIsNightMode) text.setTextColor(mNightModeTextColor);
            text.setTextSize(mFontSize);
         }
      }
      mLastHighlightedAyah = -1;
   }

   public void highlightAyah(int ayahId){
      if (mLastHighlightedAyah > 0){
         TextView text = (TextView)mLinearLayout.
                 findViewById(mLastHighlightedAyah);
         text.setTextColor(Color.BLACK);
      }

      TextView text = (TextView)mLinearLayout.findViewById(ayahId);
      if (text != null){
         text.setTextAppearance(getContext(), mHighlightedStyle);
         text.setTextSize(mFontSize);
         mLastHighlightedAyah = ayahId;

         int screenHeight = QuranScreenInfo.getInstance().getHeight();
         int y = text.getTop() - (int)(0.25 * screenHeight);
         smoothScrollTo(getScrollX(), y);
      }
      else { mLastHighlightedAyah = -1; }
   }

   private OnClickListener mOnAyahClickListener = new OnClickListener() {
      @Override
      public void onClick(View v) {
         if (mTranslationClickedListener != null){
            mTranslationClickedListener.onTranslationClicked();
         }
      }
   };

   private OnLongClickListener mOnCopyAyahListener = new OnLongClickListener(){
      @Override
      public boolean onLongClick(View v) {
         if (v instanceof TextView){
            ClipboardManager mgr = (ClipboardManager)mContext.
                    getSystemService(Service.CLIPBOARD_SERVICE);
            mgr.setText(((TextView)v).getText());
            Toast.makeText(mContext, R.string.ayah_copied_popup,
                    Toast.LENGTH_SHORT).show();
         }
         return true;
      }
   };

   private void addTextForAyah(QuranAyah ayah){
      LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
              LayoutParams.MATCH_PARENT,
              LayoutParams.WRAP_CONTENT);
      params.setMargins(mLeftRightMargin, mTopBottomMargin,
              mLeftRightMargin, mTopBottomMargin);

      TextView ayahHeader = new TextView(mContext);
      ayahHeader.setTextAppearance(mContext, mTextStyle);
      if (mIsNightMode) ayahHeader.setTextColor(mNightModeTextColor);
      ayahHeader.setTextSize(mFontSize);
      ayahHeader.setText(ayah.getSura() + ":" + ayah.getAyah());
      ayahHeader.setTypeface(null, Typeface.BOLD);
      mLinearLayout.addView(ayahHeader, params);

      TextView ayahView = new TextView(mContext);
      ayahView.setId(
              QuranInfo.getAyahId(ayah.getSura(), ayah.getAyah()));
      ayahView.setOnClickListener(mOnAyahClickListener);

      ayahView.setTextAppearance(mContext, mTextStyle);
      if (mIsNightMode){ ayahView.setTextColor(mNightModeTextColor); }
      ayahView.setTextSize(mFontSize);

      // arabic
      String ayahText = ayah.getText();
      if (!TextUtils.isEmpty(ayahText)){
         // Ayah Text
         ayahView.setLineSpacing(1.4f, 1.4f);

         boolean customFont = false;
         if (mShouldReshape){
            ayahText = ArabicStyle.reshape(mContext, ayahText);
            if (mUseArabicFont){
               customFont = true;
            }
         }
         SpannableString arabicText = new SpannableString(ayahText);

         CharacterStyle spanType;
         if (customFont){
            spanType = new ArabicTypefaceSpan(mContext, true);
         }
         else { spanType = new StyleSpan(Typeface.BOLD); }

         arabicText.setSpan(spanType, 0, ayahText.length(),
                 Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
         ayahView.setText(arabicText);
         ayahView.append("\n\n");
      }

      // translation
      String translationText = ayah.getTranslation();
      boolean customFont = false;
      if (mShouldReshape){
         if (ayah.isArabic()){
            translationText = ArabicStyle.reshape(mContext,
                    translationText);
            customFont = true;
         }
      }


      SpannableString translation = new SpannableString(translationText);
      if (customFont){
         ArabicTypefaceSpan span = new ArabicTypefaceSpan(mContext, false);
         translation.setSpan(span, 0, translation.length(),
                 Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      }
      ayahView.append(translation);

      params = new LinearLayout.LayoutParams(
              LayoutParams.MATCH_PARENT,
              LayoutParams.WRAP_CONTENT);
      params.setMargins(mLeftRightMargin, mTopBottomMargin,
              mLeftRightMargin, mTopBottomMargin);

      if (Build.VERSION.SDK_INT >= 11){
         ayahView.setTextIsSelectable(true);
      }
      else {
         ayahView.setOnLongClickListener(mOnCopyAyahListener);
      }

      mLinearLayout.addView(ayahView, params);
   }

   private void addSuraHeader(int currentSura){
      View view = new View(mContext);
      
      view.setBackgroundColor(mHeaderColor);
      LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
              LayoutParams.MATCH_PARENT, 2);
      params.topMargin = mTopBottomMargin;
      mLinearLayout.addView(view, params);

      String suraName = QuranInfo.getSuraName(mContext, currentSura, true);

      TextView headerView = new TextView(mContext);
      params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
              LayoutParams.WRAP_CONTENT);
      params.leftMargin = mLeftRightMargin;
      params.rightMargin = mLeftRightMargin;
      params.topMargin = mTopBottomMargin / 2;
      params.bottomMargin = mTopBottomMargin / 2;
      headerView.setTextAppearance(mContext, mHeaderStyle);
      if (mIsArabic){
         if (mShouldReshape){
            suraName = ArabicStyle.reshape(mContext, suraName);
         }

         if (mUseArabicFont){
            headerView.setTypeface(ArabicStyle.getTypeface(mContext));
         }
      }
      headerView.setText(suraName);
      mLinearLayout.addView(headerView, params);

      view = new View(mContext);
      view.setBackgroundColor(mDividerColor);
      mLinearLayout.addView(view, LayoutParams.MATCH_PARENT, 2);
   }

   public void setTranslationClickedListener(
           TranslationClickedListener listener){
      mTranslationClickedListener = listener;
   }

   public interface TranslationClickedListener {
      public void onTranslationClicked();
   }
}

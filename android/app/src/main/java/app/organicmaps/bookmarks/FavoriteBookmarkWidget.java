package app.organicmaps.bookmarks;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;

import app.organicmaps.MwmActivity;
import app.organicmaps.R;
import app.organicmaps.bookmarks.data.BookmarkCategory;
import app.organicmaps.bookmarks.data.BookmarkInfo;
import app.organicmaps.bookmarks.data.BookmarkManager;
import app.organicmaps.bookmarks.data.Icon;

public class FavoriteBookmarkWidget extends AppWidgetProvider {
    private static final String PREFS_NAME = "FavoriteBookmarkWidget";
    private static final String PREF_PREFIX_KEY = "favorite_bookmark_";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId);
        }
    }

    public static void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);

        int categoryIndex = prefs.getInt(PREF_PREFIX_KEY + appWidgetId + "_category", -1);
        int bookmarkIndex = prefs.getInt(PREF_PREFIX_KEY + appWidgetId + "_bookmark", -1);
        String bookmarkName = prefs.getString(PREF_PREFIX_KEY + appWidgetId + "_name", "");
        int iconColor = prefs.getInt(PREF_PREFIX_KEY + appWidgetId + "_color", 0);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_favorite_bookmark);

        if (categoryIndex != -1 && bookmarkIndex != -1) {
            try {
                BookmarkCategory category = BookmarkManager.INSTANCE.getCategories().get(categoryIndex);
                long bookmarkId = BookmarkManager.INSTANCE.getBookmarkIdByPosition(category.getId(), bookmarkIndex);
                BookmarkInfo bookmarkInfo = BookmarkManager.INSTANCE.getBookmarkInfo(bookmarkId);

                if (bookmarkInfo != null) {
                    views.setTextViewText(R.id.tv__bookmark_name, bookmarkInfo.getName());

                    int color = iconColor != 0 ? iconColor : bookmarkInfo.getIcon().getColor();

                    int iconResId = BookmarkManager.INSTANCE.getBookmarkIcon(bookmarkId);

                    if (iconResId == 0) {
                        iconResId = R.drawable.ic_bookmarks;
                    }

                    views.setImageViewResource(R.id.iv__bookmark_color, iconResId);
                }
            } catch (Exception e) {
                views.setTextViewText(R.id.tv__bookmark_name, bookmarkName);
                views.setImageViewResource(R.id.iv__bookmark_color, R.drawable.ic_bookmarks);
            }
        } else {
            views.setTextViewText(R.id.tv__bookmark_name, context.getString(R.string.select_bookmark));
            views.setImageViewResource(R.id.iv__bookmark_color, R.drawable.ic_bookmarks);
        }
        Intent intent = new Intent(context, MwmActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.setAction("app.organicmaps.action.SHOW_BOOKMARK");
        
        intent.putExtra("FROM_WIDGET", true);
        if (categoryIndex >= 0 && bookmarkIndex >= 0) {
            intent.putExtra("CATEGORY_INDEX", categoryIndex);
            intent.putExtra("BOOKMARK_INDEX", bookmarkIndex);
            
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    appWidgetId,
                    intent, 
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent);
        }
        
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    public static void saveBookmarkPref(Context context, int appWidgetId,
                                        int categoryIndex, int bookmarkIndex,
                                        String bookmarkName, int iconColor) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putInt(PREF_PREFIX_KEY + appWidgetId + "_category", categoryIndex);
        prefs.putInt(PREF_PREFIX_KEY + appWidgetId + "_bookmark", bookmarkIndex);
        prefs.putString(PREF_PREFIX_KEY + appWidgetId + "_name", bookmarkName);
        prefs.putInt(PREF_PREFIX_KEY + appWidgetId + "_color", iconColor);
        prefs.apply();
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        for (int appWidgetId : appWidgetIds) {
            prefs.remove(PREF_PREFIX_KEY + appWidgetId + "_category");
            prefs.remove(PREF_PREFIX_KEY + appWidgetId + "_bookmark");
            prefs.remove(PREF_PREFIX_KEY + appWidgetId + "_name");
            prefs.remove(PREF_PREFIX_KEY + appWidgetId + "_color");
        }
        prefs.apply();
    }
}

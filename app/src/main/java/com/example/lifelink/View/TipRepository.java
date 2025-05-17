package com.example.lifelink.View;


import com.example.lifelink.Model.Tip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TipRepository {
    private static final List<Tip> todayTips = new ArrayList<>();

    /** Call this to record a new tip (prepended). */
    public static void addTip(Tip tip) {
        todayTips.add(0, tip);
    }

    /** Returns a fresh copy so callers can loop safely. */
    public static List<Tip> getTips() {
        return new ArrayList<>(todayTips);
    }

    /** Optional: call this at midnight or on logout to clear yesterday’s tips. */
    public static void clear() {
        todayTips.clear();
    }
    private static final List<Tip> tipsPool = new ArrayList<>();

    public static void prepareTipsPool(Map<String, List<String>> allTips, List<String> selectedCategories) {
        tipsPool.clear();
        for (String cat : selectedCategories) {
            List<String> tips = allTips.get(cat);
            if (tips != null) {
                for (String text : tips) {
                    tipsPool.add(new Tip("💡 Wellness Tip", text));
                }
            }
        }
    }

    public static List<Tip> getTipsPool() {
        return tipsPool;
    }

}

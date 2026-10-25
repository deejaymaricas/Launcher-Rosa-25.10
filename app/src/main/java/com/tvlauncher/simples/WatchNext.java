package com.tvlauncher.simples;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lê a linha "Continue assistindo / Play Next" do próprio Android TV (Watch Next), que os apps
 * (Netflix, Crunchyroll etc.) alimentam. Precisa da permissão android.permission.READ_TV_LISTINGS.
 */
final class WatchNext {
    static final String PERM = "android.permission.READ_TV_LISTINGS";
    static final Uri URI = Uri.parse("content://android.media.tv/watch_next_program");

    private WatchNext() {
    }

    static boolean hasPermission(Context c) {
        return Build.VERSION.SDK_INT < 23 || c.checkSelfPermission(PERM) == PackageManager.PERMISSION_GRANTED;
    }

    private static String str(Cursor q, String col) {
        int i = q.getColumnIndex(col);
        if (i < 0 || q.isNull(i)) return "";
        try {
            return q.getString(i);
        } catch (Exception e) {
            return "";
        }
    }

    private static long num(Cursor q, String col) {
        int i = q.getColumnIndex(col);
        if (i < 0 || q.isNull(i)) return 0;
        try {
            return q.getLong(i);
        } catch (Exception e) {
            return 0;
        }
    }

    /** Lê o Watch Next e atualiza o histórico. Retorna um texto de status (também gravado). */
    static String sync(Context c) {
        String status;
        String breakdown = "";
        if (Build.VERSION.SDK_INT < 26) {
            status = "indisponível (precisa de Android 8 ou mais novo)";
            WatchStore.setInfo(c, status);
            return status;
        }
        List<WatchStore.Item> list = new ArrayList<>();
        Map<String, String> artUris = new HashMap<>();
        Map<String, int[]> tally = new HashMap<>(); // app -> [continuar, próximo, novo, lista, outro]
        Cursor q = null;
        try {
            q = c.getContentResolver().query(URI, null, null, null, null);
            if (q == null) {
                status = "o sistema não respondeu (sem Watch Next nesta TV)";
                WatchStore.setInfo(c, status);
                return status;
            }
            long n = System.currentTimeMillis();
            int rows = 0;
            while (q.moveToNext()) {
                rows++;
                String pkg = str(q, "package_name");
                String title = str(q, "title");
                if (pkg.isEmpty() || title.isEmpty() || pkg.contains("youtube")) continue;
                long type = num(q, "watch_next_type");
                long pos = num(q, "last_playback_position_millis");
                long dur = num(q, "duration_millis");
                int[] tl = tally.get(pkg);
                if (tl == null) {
                    tl = new int[5];
                    tally.put(pkg, tl);
                }
                tl[type >= 0 && type <= 3 ? (int) type : 4]++;
                // 0 = continuar, 1 = próximo episódio. Novidades/lista de desejos ficam de fora,
                // a não ser que o app tenha salvo um minuto assistido.
                if (type != 0 && type != 1 && pos <= 0) continue;
                WatchStore.Item it = new WatchStore.Item();
                it.pkg = pkg;
                it.title = title;
                it.key = "wn|" + pkg + "|" + title;
                it.pos = pos;
                it.dur = dur;
                it.uri = str(q, "intent_uri");
                String s = str(q, "season_display_number"), e = str(q, "episode_display_number");
                if (!s.isEmpty() && !e.isEmpty()) it.ep = "T" + s + " E" + e;
                else if (!e.isEmpty()) it.ep = "E" + e;
                else it.ep = str(q, "episode_title");
                long t = num(q, "last_engagement_time_utc_millis");
                it.time = t > 0 ? t : n - list.size();
                String art = str(q, "thumbnail_uri");
                if (art.isEmpty()) art = str(q, "poster_art_uri");
                if (!art.isEmpty()) artUris.put(it.key, art);
                list.add(it);
            }
            StringBuilder tb = new StringBuilder();
            for (Map.Entry<String, int[]> e : tally.entrySet()) {
                int[] v = e.getValue();
                tb.append("\n   ").append(e.getKey()).append(": continuar ").append(v[0])
                        .append(", próximo ").append(v[1]).append(", novo ").append(v[2])
                        .append(", lista ").append(v[3]).append(", outro ").append(v[4]);
            }
            breakdown = tb.toString();
            status = "OK: " + list.size() + " item(ns) de " + rows + " no sistema";
        } catch (SecurityException e) {
            status = "SEM PERMISSÃO (libere READ_TV_LISTINGS)";
            WatchStore.setInfo(c, status);
            return status;
        } catch (Exception e) {
            status = "erro: " + e;
            WatchStore.setInfo(c, status);
            return status;
        } finally {
            if (q != null) q.close();
        }

        Map<String, Bitmap> arts = new HashMap<>();
        for (WatchStore.Item it : list) {
            if (!WatchStore.hasArt(c, it.key) && artUris.containsKey(it.key)) {
                Bitmap b = fetch(c, artUris.get(it.key));
                if (b != null) arts.put(it.key, b);
            }
        }
        WatchStore.replaceWatchNext(c, list, arts);
        StringBuilder pk = new StringBuilder();
        for (WatchStore.Item it : list) {
            if (pk.indexOf(it.pkg) < 0) pk.append(pk.length() > 0 ? ", " : "").append(it.pkg);
        }
        status += list.isEmpty() ? "" : " (" + pk + ")";
        WatchStore.setInfo(c, status + (breakdown.isEmpty() ? "" : "\n   Linhas do sistema por app:" + breakdown));
        return status;
    }

    static Bitmap fetch(Context c, String u) {
        try {
            Uri uri = Uri.parse(u);
            InputStream in;
            String sc = uri.getScheme();
            if ("http".equals(sc) || "https".equals(sc)) {
                HttpURLConnection h = (HttpURLConnection) new URL(u).openConnection();
                h.setConnectTimeout(8000);
                h.setReadTimeout(10000);
                in = h.getInputStream();
            } else {
                in = c.getContentResolver().openInputStream(uri);
            }
            if (in == null) return null;
            Bitmap b = BitmapFactory.decodeStream(in);
            in.close();
            return b;
        } catch (Exception e) {
            return null;
        }
    }
}

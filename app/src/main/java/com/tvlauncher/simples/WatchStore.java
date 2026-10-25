package com.tvlauncher.simples;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Histórico de "Continue assistindo": guardado em prefs "watch" + miniaturas em files/watch. */
final class WatchStore {
    static final int MAX = 15;

    static class Item {
        String key, pkg, title, sub;
        String uri = "";   // intent do app para abrir o episódio exato (Watch Next)
        String ep = "";    // "T3 E7"
        int src;           // 0 = sessão de mídia, 1 = Watch Next do sistema
        long pos, dur, time;
    }

    private WatchStore() {
    }

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences("watch", Context.MODE_PRIVATE);
    }

    static File artFile(Context c, String key) {
        File d = new File(c.getFilesDir(), "watch");
        if (!d.exists()) d.mkdirs();
        return new File(d, Integer.toHexString(key.hashCode()) + ".jpg");
    }

    static synchronized String stamp(Context c) {
        return prefs(c).getString("items", "");
    }

    static synchronized void setInfo(Context c, String info) {
        prefs(c).edit().putString("wnInfo", info).apply();
    }

    private static JSONObject dismissed(Context c) {
        try {
            return new JSONObject(prefs(c).getString("dism", "{}"));
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    private static void dismiss(Context c, Item it) {
        try {
            JSONObject d = dismissed(c);
            d.put(it.key, it.time);
            prefs(c).edit().putString("dism", d.toString()).apply();
        } catch (Exception ignored) {
        }
    }

    static synchronized String getInfo(Context c) {
        return prefs(c).getString("wnInfo", "ainda não verificado");
    }

    static synchronized List<Item> load(Context c) {
        List<Item> out = new ArrayList<>();
        try {
            JSONArray a = new JSONArray(prefs(c).getString("items", "[]"));
            for (int i = 0; i < a.length(); i++) {
                JSONObject o = a.getJSONObject(i);
                Item it = new Item();
                it.key = o.getString("key");
                it.pkg = o.getString("pkg");
                it.title = o.getString("title");
                it.sub = o.optString("sub", "");
                it.uri = o.optString("uri", "");
                it.ep = o.optString("ep", "");
                it.src = o.optInt("src", 0);
                it.pos = o.optLong("pos");
                it.dur = o.optLong("dur");
                it.time = o.optLong("time");
                if (!it.pkg.contains("youtube")) out.add(it);
            }
        } catch (Exception ignored) {
        }
        return out;
    }

    private static void write(Context c, List<Item> l) {
        JSONArray a = new JSONArray();
        try {
            for (Item it : l) {
                JSONObject o = new JSONObject();
                o.put("key", it.key);
                o.put("pkg", it.pkg);
                o.put("title", it.title);
                o.put("sub", it.sub == null ? "" : it.sub);
                o.put("uri", it.uri == null ? "" : it.uri);
                o.put("ep", it.ep == null ? "" : it.ep);
                o.put("src", it.src);
                o.put("pos", it.pos);
                o.put("dur", it.dur);
                o.put("time", it.time);
                a.put(o);
            }
        } catch (Exception ignored) {
        }
        prefs(c).edit().putString("items", a.toString()).apply();
    }

    private static void writeArt(Context c, String key, Bitmap art) {
        if (art == null) return;
        try {
            Bitmap s = art;
            if (art.getWidth() > 640) {
                int h = Math.max(1, art.getHeight() * 640 / art.getWidth());
                s = Bitmap.createScaledBitmap(art, 640, h, true);
            }
            FileOutputStream o = new FileOutputStream(artFile(c, key));
            s.compress(Bitmap.CompressFormat.JPEG, 88, o);
            o.close();
        } catch (Exception ignored) {
        }
    }

    /** Salva/atualiza um item de sessão de mídia e o põe no começo. Terminou (>=95%)? Remove. */
    static synchronized void save(Context c, Item n, Bitmap art) {
        List<Item> l = load(c);
        Item old = null;
        for (int i = 0; i < l.size(); i++) {
            if (l.get(i).key.equals(n.key)) {
                old = l.remove(i);
                break;
            }
        }
        if (n.dur > 0 && n.pos >= n.dur * 95L / 100L) {
            artFile(c, n.key).delete();
            write(c, l);
            return;
        }
        if (old != null && (n.uri == null || n.uri.isEmpty())) {
            n.uri = old.uri;
            n.ep = old.ep;
            n.src = old.src;
        }
        writeArt(c, n.key, art);
        n.time = System.currentTimeMillis();
        l.add(0, n);
        while (l.size() > MAX) {
            Item d = l.remove(l.size() - 1);
            artFile(c, d.key).delete();
        }
        write(c, l);
    }

    /**
     * Troca os itens vindos do Watch Next (fonte do sistema) pelos recém-lidos.
     * Itens de sessão do mesmo app ficam de fora, porque o Watch Next é mais completo.
     */
    static synchronized void replaceWatchNext(Context c, List<Item> wn, Map<String, Bitmap> arts) {
        // itens que o usuário removeu só voltam se o app atualizar o horário deles
        JSONObject dis = dismissed(c);
        List<Item> keep = new ArrayList<>();
        for (Item w : wn) {
            if (w.time > dis.optLong(w.key, -1)) keep.add(w);
        }
        wn = keep;
        Set<String> wkeys = new HashSet<>();
        Set<String> wpk = new HashSet<>();
        for (Item w : wn) {
            wkeys.add(w.key);
            wpk.add(w.pkg);
            w.src = 1;
            Bitmap b = arts.get(w.key);
            if (b != null) writeArt(c, w.key, b);
        }
        List<Item> out = new ArrayList<>();
        for (Item o : load(c)) {
            boolean drop = wkeys.contains(o.key) || o.src == 1 || wpk.contains(o.pkg);
            if (drop) {
                if (!wkeys.contains(o.key)) artFile(c, o.key).delete();
            } else {
                out.add(o);
            }
        }
        out.addAll(wn);
        Collections.sort(out, (x, y) -> Long.compare(y.time, x.time));
        while (out.size() > MAX) {
            Item d = out.remove(out.size() - 1);
            artFile(c, d.key).delete();
        }
        write(c, out);
    }

    static synchronized boolean hasArt(Context c, String key) {
        return artFile(c, key).exists();
    }

    static synchronized void remove(Context c, String key) {
        List<Item> l = load(c);
        for (int i = 0; i < l.size(); i++) {
            if (l.get(i).key.equals(key)) {
                Item gone = l.remove(i);
                if (gone.src == 1) dismiss(c, gone);
                break;
            }
        }
        artFile(c, key).delete();
        write(c, l);
    }

    static synchronized void clear(Context c) {
        for (Item it : load(c)) {
            if (it.src == 1) dismiss(c, it);
            artFile(c, it.key).delete();
        }
        write(c, new ArrayList<Item>());
    }
}

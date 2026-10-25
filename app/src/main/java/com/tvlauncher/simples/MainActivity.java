package com.tvlauncher.simples;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StatFs;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class MainActivity extends Activity {

    static final String VENDING = "com.android.vending";
    static final String SPOTIFY = "com.spotify.tv.android";
    static final String[] PRIORITY = {
            VENDING, SPOTIFY, "com.google.android.youtube.tv", "com.netflix.ninja",
            "com.amazon.amazonvideo.livingroom", "com.crunchyroll.crunchyroid"};

    static final String[] BG_NAMES = {
            "Rosa noite", "Rosa pôr do sol", "Rosa neon", "Vinho rosé", "Algodão doce",
            "Meia-noite", "Grafite", "Oceano", "Floresta"};
    static final int[][] BG_COLORS = {
            {0xFF1B0A14, 0xFF4A1030, 0xFF8E2A5E},
            {0xFF2A0A1F, 0xFFC2185B, 0xFFFF80AB},
            {0xFF12000F, 0xFF7B1FA2, 0xFFE91E63},
            {0xFF2D0A14, 0xFF8E1B3A},
            {0xFF42204E, 0xFFE0709F, 0xFFFFB6D0},
            {0xFF0F2027, 0xFF203A43, 0xFF2C5364},
            {0xFF141414, 0xFF2B2B2B},
            {0xFF000046, 0xFF1CB5E0},
            {0xFF0B3D2E, 0xFF1B7F5C}};

    static final int[] ACCENTS = {0xFFFF4081, 0xFFFF80AB, 0xFFE91E8C, 0xFFF48FB1};
    static final String[] ACCENT_NAMES = {"Rosa choque", "Rosa claro", "Magenta", "Rosa bebê"};
    static final int[][] SIZES = {{140, 79}, {160, 90}, {192, 108}};
    static final String[] SIZE_NAMES = {"pequeno", "médio", "grande"};
    static final int DEF_DIM = 20;
    static final String[] FONT_NAMES = {"Padrão (Roboto)", "Leve", "Condensada", "Média", "Serifada"};
    static final String[] FONT_FAMILIES = {"sans-serif", "sans-serif-light", "sans-serif-condensed",
            "sans-serif-medium", "serif"};
    static final String[] CLOCK_NAMES = {"pequeno", "médio", "grande", "enorme"};
    static final int[] CLOCK_SP = {36, 50, 64, 80};
    static final String[] BGMODE_NAMES = {"desligado (fundo fixo)", "trocar a cada hora",
            "trocar a cada dia", "trocar toda vez que apertar Home"};
    static final String[] SHORT_IDS = {"wifi", "bluetooth", "memoria", "medidor", "cache", "config"};
    static final String[] SHORT_NAMES = {"Wi-Fi", "Bluetooth", "Limpar memória",
            "Medidor de RAM e armazenamento", "Limpar cache", "Configurações"};
    static final String[] BLUR_NAMES = {"sem desfoque", "leve", "médio", "forte"};

    static final String[] SEC_IDS = {"spotify", "continuar", "atalhos", "favoritos", "recentes", "apps", "limpeza"};
    static final String[] SEC_NAMES = {"Spotify / Tocando agora", "Continue assistindo",
            "Atalhos (Wi-Fi, Bluetooth, Configurações)",
            "Favoritos", "Abertos recentemente", "Todos os apps", "Memória e limpeza"};

    static final String[][] CITIES = {
            {"São Paulo", "-23.5505", "-46.6333"}, {"Rio de Janeiro", "-22.9068", "-43.1729"},
            {"Belo Horizonte", "-19.9167", "-43.9345"}, {"Brasília", "-15.7939", "-47.8828"},
            {"Curitiba", "-25.4284", "-49.2733"}, {"Porto Alegre", "-30.0346", "-51.2177"},
            {"Salvador", "-12.9777", "-38.5016"}, {"Recife", "-8.0476", "-34.8770"},
            {"Fortaleza", "-3.7319", "-38.5267"}, {"Manaus", "-3.1190", "-60.0217"},
            {"Belém", "-1.4558", "-48.4902"}, {"Goiânia", "-16.6869", "-49.2648"},
            {"Florianópolis", "-27.5954", "-48.5480"}, {"Campinas", "-22.9099", "-47.0626"}};

    static class App {
        String pkg;
        String label;
        Drawable img;
        boolean banner;
    }

    SharedPreferences sp;
    PackageManager pm;
    final Handler handler = new Handler(Looper.getMainLooper());

    FrameLayout root;
    ImageView bgView;
    View dim;
    ScrollView scroll;
    LinearLayout content;
    TextView clockTv, dateTv, weatherTv;
    LinearLayout sectionsBox, nowCard;
    int accent = ACCENTS[0];
    float npK = 1f;
    final Random rnd = new Random();
    TextView ramTv, stoTv;
    View ramFill, ramRest, stoFill, stoRest;
    int tileWdp = 160;
    String lastWeather = "Carregando clima...";
    ImageView npArt;
    TextView npTitle, npSub, npElapsed, npTotal;
    ImageView npPlayIcon, npShuffle, npRepeat;
    SeekBar npSeek;
    long curDuration = 0, seekTarget = 0, userUntil = 0;
    boolean seekWarned = false;

    final List<App> allApps = new ArrayList<>();
    final Map<String, App> appMap = new HashMap<>();
    String lastSig = "";
    int tileW, tileH;

    MediaSessionManager msm;
    ComponentName listener;
    MediaController current;
    boolean mediaOk = false;

    final MediaController.Callback mcb = new MediaController.Callback() {
        @Override
        public void onMetadataChanged(MediaMetadata metadata) {
            updateNowPlaying();
        }

        @Override
        public void onPlaybackStateChanged(PlaybackState state) {
            updateNowPlaying();
        }

        @Override
        public void onSessionDestroyed() {
            refreshSessions();
        }
    };

    final MediaSessionManager.OnActiveSessionsChangedListener sessL =
            controllers -> pickSession(controllers);

    final Runnable tick = new Runnable() {
        @Override
        public void run() {
            updateClock();
            updateMeters();
            handler.postDelayed(this, 15000);
        }
    };

    final Runnable progressTick = new Runnable() {
        @Override
        public void run() {
            if (current != null) {
                PlaybackState st = current.getPlaybackState();
                if (st != null && st.getState() == PlaybackState.STATE_PLAYING) updateProgress();
            }
            handler.postDelayed(this, 500);
        }
    };

    final Runnable seekRun = new Runnable() {
        @Override
        public void run() {
            if (current == null) return;
            PlaybackState st = current.getPlaybackState();
            if (st != null && (st.getActions() & PlaybackState.ACTION_SEEK_TO) == 0) {
                if (!seekWarned) {
                    seekWarned = true;
                    toast("Este app não permite pular para outro trecho da música.");
                }
                return;
            }
            current.getTransportControls().seekTo(seekTarget);
        }
    };

    final Runnable weatherRefresh = new Runnable() {
        @Override
        public void run() {
            fetchWeather();
        }
    };

    // ------------------------------------------------------------------ ciclo de vida

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        sp = getSharedPreferences("cfg", MODE_PRIVATE);
        pm = getPackageManager();
        accent = loadAccent();
        applySize();

        msm = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        listener = new ComponentName(this, NowPlayingService.class);

        buildUi();
        setContentView(root);
        applyBackground();
        buildLists();
        lastSig = signature();
        updateNowPlaying();
        tick.run();
        fetchWeather();
        focusFirst();
    }

    int clampI(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    int pct(String key, int def) {
        return sp.getInt(key, def);
    }

    int tilePct() {
        if (sp.contains("tilePct")) return clampI(sp.getInt("tilePct", 83), 10, 100);
        int i = clampI(sp.getInt("size", 1), 0, 2);
        return i == 0 ? 73 : (i == 1 ? 83 : 100);
    }

    void applySize() {
        int p = tilePct();
        tileWdp = Math.max(24, 192 * p / 100);
        tileW = dp(tileWdp);
        tileH = dp(Math.max(14, 108 * p / 100));
    }

    int iconPx() {
        return Math.max(dp(10), (int) (tileH * 0.62f));
    }

    /** Raio dos cantos: porcentagem de 24 dp, vezes um multiplicador por tipo de cartão. */
    int rad(float mult) {
        return dp(Math.round(24f * pct("radius", 33) / 100f * mult));
    }

    /** Aplica a opacidade escolhida (0 a 200%) ao alfa de uma cor de cartão. */
    int ca(int argb) {
        int a = (argb >>> 24) * pct("opacity", 100) / 100;
        a = clampI(a, 0, 255);
        return (a << 24) | (argb & 0x00FFFFFF);
    }

    Typeface font(boolean bold) {
        int i = clampI(sp.getInt("font", 0), 0, FONT_FAMILIES.length - 1);
        return Typeface.create(FONT_FAMILIES[i], bold ? Typeface.BOLD : Typeface.NORMAL);
    }

    int loadAccent() {
        int i = sp.getInt("accent", 0);
        if (i == ACCENTS.length && sp.contains("accentRgb")) {
            return 0xFF000000 | sp.getInt("accentRgb", 0xFF4081);
        }
        return ACCENTS[clampI(i, 0, ACCENTS.length - 1)];
    }

    int accentBg(int alpha) {
        return (accent & 0x00FFFFFF) | (alpha << 24);
    }

    void rebuildAll() {
        applySize();
        buildUi();
        setContentView(root);
        applyBackground();
        buildLists();
        updateClock();
        updateNowPlaying();
        focusFirst();
    }

    @Override
    protected void onResume() {
        super.onResume();
        maybeRotateBg(false);
        syncWatchNext(false);
        setupMedia();
        handler.removeCallbacks(progressTick);
        handler.post(progressTick);
        String sig = signature();
        if (!sig.equals(lastSig)) {
            buildLists();
            lastSig = sig;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(progressTick);
        try {
            msm.removeOnActiveSessionsChangedListener(sessL);
        } catch (Exception ignored) {
        }
        if (current != null) {
            try {
                current.unregisterCallback(mcb);
            } catch (Exception ignored) {
            }
            current = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        maybeRotateBg(true);
        scroll.smoothScrollTo(0, 0);
        focusFirst();
    }

    @Override
    public void onBackPressed() {
        scroll.smoothScrollTo(0, 0);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            showSettings();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // ------------------------------------------------------------------ utilidades

    int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }

    List<String> getList(String key) {
        String s = sp.getString(key, "");
        List<String> r = new ArrayList<>();
        if (s != null && !s.isEmpty()) r.addAll(Arrays.asList(s.split(",")));
        return r;
    }

    void putList(String key, List<String> l) {
        sp.edit().putString(key, TextUtils.join(",", l)).apply();
    }

    void startSafe(Intent i) {
        try {
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        } catch (Exception e) {
            toast("Não disponível nesta TV");
        }
    }

    TextView tv(String s, int sizeSp, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(sizeSp);
        t.setTextColor(Color.WHITE);
        t.setTypeface(font(bold));
        return t;
    }

    void focusStyle(final View v, int radiusDp, final int normal) {
        final GradientDrawable d = new GradientDrawable();
        d.setCornerRadius(dp(radiusDp));
        d.setColor(normal);
        v.setBackground(d);
        v.setFocusable(true);
        v.setClickable(true);
        v.setOnFocusChangeListener((x, f) -> {
            d.setColor(f ? accent : normal);
            d.setStroke(f ? dp(2) : 0, Color.WHITE);
            x.animate().scaleX(f ? 1.06f : 1f).scaleY(f ? 1.06f : 1f).setDuration(110).start();
        });
    }

    TextView btn(String text, View.OnClickListener l) {
        TextView t = tv(text, 15, false);
        t.setGravity(Gravity.CENTER);
        focusStyle(t, 10, accentBg(0x44));
        t.setPadding(dp(16), dp(9), dp(16), dp(9));
        t.setOnClickListener(l);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
        lp.leftMargin = dp(8);
        t.setLayoutParams(lp);
        return t;
    }

    View sectionView(String title, View body) {
        LinearLayout s = new LinearLayout(this);
        s.setOrientation(LinearLayout.VERTICAL);
        float sk = pct("secText", 100) / 100f;
        TextView t = tv(title, Math.max(7, Math.round(18 * sk)), true);
        t.setTextColor(accent);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
        lp.topMargin = Math.max(dp(2), Math.round(dp(14) * sk));
        s.addView(t, lp);
        if (body instanceof HorizontalScrollView) {
            LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(-1, -2);
            blp.leftMargin = -dp(20);
            blp.rightMargin = -dp(20);
            s.addView(body, blp);
        } else {
            s.addView(body);
        }
        return s;
    }

    /** Deixa o cartão com zoom (foco) aparecer inteiro, sem ser cortado pelos contêineres. */
    void noClip(View v) {
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            g.setClipChildren(false);
            g.setClipToPadding(false);
            for (int i = 0; i < g.getChildCount(); i++) noClip(g.getChildAt(i));
        }
    }

    HorizontalScrollView hscroll(LinearLayout row) {
        HorizontalScrollView hs = new HorizontalScrollView(this);
        hs.setHorizontalScrollBarEnabled(false);
        hs.setClipToPadding(false);
        hs.setPadding(dp(20), dp(16), dp(20), dp(16));
        hs.addView(row, new ViewGroup.LayoutParams(-2, -2));
        return hs;
    }

    // ------------------------------------------------------------------ interface

    void buildUi() {
        root = new FrameLayout(this);
        root.setBackgroundColor(0xFF101420);

        bgView = new ImageView(this);
        bgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        root.addView(bgView, new FrameLayout.LayoutParams(-1, -1));

        dim = new View(this);
        root.addView(dim, new FrameLayout.LayoutParams(-1, -1));

        scroll = new ScrollView(this);
        scroll.setVerticalScrollBarEnabled(false);
        root.addView(scroll, new FrameLayout.LayoutParams(-1, -1));

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(48), dp(24), dp(48), dp(40));
        content.setClipChildren(false);
        content.setClipToPadding(false);
        scroll.addView(content, new ViewGroup.LayoutParams(-1, -2));

        // barra superior
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        clockTv = tv("--:--", CLOCK_SP[clampI(sp.getInt("clockSize", 1), 0, CLOCK_SP.length - 1)], true);
        dateTv = tv("", 15, false);
        dateTv.setTextColor(0xCCFFFFFF);
        left.addView(clockTv);
        left.addView(dateTv);
        if (sp.getBoolean("hideDate", false)) dateTv.setVisibility(View.GONE);
        top.addView(left);

        weatherTv = tv(lastWeather, 17, false);
        if (sp.getBoolean("hideWeather", false)) weatherTv.setVisibility(View.INVISIBLE);
        LinearLayout.LayoutParams wlp = new LinearLayout.LayoutParams(0, -2, 1f);
        wlp.leftMargin = dp(32);
        top.addView(weatherTv, wlp);

        top.addView(btn("Personalizar", v -> showSettings()));
        content.addView(top);


        buildNowCard();

        sectionsBox = new LinearLayout(this);
        sectionsBox.setOrientation(LinearLayout.VERTICAL);
        content.addView(sectionsBox, new LinearLayout.LayoutParams(-1, -2));
    }

    ImageView iconBtn(int res, int boxDp, int padDp, int normal, final Runnable r) {
        ImageView v = new ImageView(this);
        v.setImageResource(res);
        v.setColorFilter(Color.WHITE);
        v.setScaleType(ImageView.ScaleType.FIT_CENTER);
        focusStyle(v, boxDp / 2, normal);
        v.setPadding(dp(padDp), dp(padDp), dp(padDp), dp(padDp));
        v.setOnClickListener(x -> r.run());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(boxDp), dp(boxDp));
        lp.leftMargin = Math.max(dp(2), Math.round(dp(14) * npK));
        lp.rightMargin = Math.max(dp(2), Math.round(dp(14) * npK));
        v.setLayoutParams(lp);
        return v;
    }

    int sk(int dpv) {
        return Math.max(1, Math.round(dpv * npK));
    }

    int sks(int sp0) {
        return Math.max(7, Math.round(sp0 * npK));
    }

    void buildNowCard() {
        npK = clampI(pct("npPct", 100), 10, 100) / 100f;
        LinearLayout card = new LinearLayout(this);
        nowCard = card;
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(sk(16), sk(14), sk(16), sk(14));
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(rad(2f));
        bg.setColor(ca(0x66000000));
        bg.setStroke(dp(1), accentBg(0x90));
        card.setBackground(bg);

        // linha 1: capa, título/artista, botão abrir Spotify
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);

        npArt = new ImageView(this);
        npArt.setScaleType(ImageView.ScaleType.CENTER_CROP);
        GradientDrawable artBg = new GradientDrawable();
        artBg.setCornerRadius(rad(1f));
        artBg.setColor(0xFF2A2F4A);
        npArt.setBackground(artBg);
        npArt.setClipToOutline(true);
        top.addView(npArt, new LinearLayout.LayoutParams(sk(76), sk(76)));

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        focusStyle(info, 10, 0x00000000);
        info.setPadding(sk(14), sk(8), sk(14), sk(8));
        npTitle = tv("", sks(21), true);
        npTitle.setSingleLine(true);
        npTitle.setEllipsize(TextUtils.TruncateAt.END);
        npSub = tv("", sks(15), false);
        npSub.setTextColor(0xFFB0B8D0);
        npSub.setSingleLine(true);
        npSub.setEllipsize(TextUtils.TruncateAt.END);
        info.addView(npTitle);
        info.addView(npSub);
        info.setOnClickListener(v -> openPlayer());
        LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(0, -2, 1f);
        ilp.leftMargin = sk(12);
        top.addView(info, ilp);

        top.addView(btn("Abrir Spotify", v -> {
            if (!launchPackage(SPOTIFY)) {
                toast("Spotify não está instalado. Abra a Play Store e instale.");
            }
        }));
        card.addView(top, new LinearLayout.LayoutParams(-1, -2));

        // linha 2: tempo + barra de progresso
        LinearLayout seekRow = new LinearLayout(this);
        seekRow.setOrientation(LinearLayout.HORIZONTAL);
        seekRow.setGravity(Gravity.CENTER_VERTICAL);
        npElapsed = tv("00:00", sks(13), false);
        npTotal = tv("00:00", sks(13), false);
        npElapsed.setTextColor(0xCCFFFFFF);
        npTotal.setTextColor(0xCCFFFFFF);
        npSeek = new SeekBar(this);
        npSeek.setMax(100);
        npSeek.setKeyProgressIncrement(1);
        npSeek.setProgressTintList(ColorStateList.valueOf(accent));
        npSeek.setProgressBackgroundTintList(ColorStateList.valueOf(0x66FFFFFF));
        final GradientDrawable thumbSmall = thumb(Math.max(8, sk(14))), thumbBig = thumb(Math.max(14, sk(26)));
        npSeek.setBackground(null);
        npSeek.setThumb(thumbSmall);
        npSeek.setOnFocusChangeListener((x, f) -> npSeek.setThumb(f ? thumbBig : thumbSmall));
        npSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int p, boolean fromUser) {
                if (!fromUser) return;
                npElapsed.setText(fmt(p * 1000L));
                seekTarget = p * 1000L;
                userUntil = SystemClock.uptimeMillis() + 2000;
                handler.removeCallbacks(seekRun);
                handler.postDelayed(seekRun, 500);
            }

            @Override
            public void onStartTrackingTouch(SeekBar sb) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar sb) {
                handler.removeCallbacks(seekRun);
                seekRun.run();
            }
        });
        seekRow.addView(npElapsed, new LinearLayout.LayoutParams(Math.max(dp(36), sk(56)), -2));
        seekRow.addView(npSeek, new LinearLayout.LayoutParams(0, Math.max(dp(20), sk(36)), 1f));
        npTotal.setGravity(Gravity.END);
        seekRow.addView(npTotal, new LinearLayout.LayoutParams(Math.max(dp(36), sk(56)), -2));
        LinearLayout.LayoutParams srlp = new LinearLayout.LayoutParams(-1, -2);
        srlp.topMargin = sk(10);
        card.addView(seekRow, srlp);

        // linha 3: embaralhar, anterior, tocar/pausar, próxima, repetir
        LinearLayout ctl = new LinearLayout(this);
        ctl.setOrientation(LinearLayout.HORIZONTAL);
        ctl.setGravity(Gravity.CENTER);
        npShuffle = iconBtn(R.drawable.ic_shuffle, sk(44), sk(10), 0x00000000,
                () -> customToggle("SHUFFLE", "ALEAT"));
        ImageView prev = iconBtn(R.drawable.ic_prev, sk(44), sk(10), 0x00000000, () -> {
            if (current != null) current.getTransportControls().skipToPrevious();
        });
        npPlayIcon = iconBtn(R.drawable.ic_play, sk(56), sk(14), accent, () -> {
            if (current == null) {
                openPlayer();
                return;
            }
            PlaybackState st = current.getPlaybackState();
            if (st != null && st.getState() == PlaybackState.STATE_PLAYING) {
                current.getTransportControls().pause();
            } else {
                current.getTransportControls().play();
            }
        });
        ImageView next = iconBtn(R.drawable.ic_next, sk(44), sk(10), 0x00000000, () -> {
            if (current != null) current.getTransportControls().skipToNext();
        });
        npRepeat = iconBtn(R.drawable.ic_repeat, sk(44), sk(10), 0x00000000,
                () -> customToggle("REPEAT", "REPET"));
        ctl.addView(npShuffle);
        ctl.addView(prev);
        ctl.addView(npPlayIcon);
        ctl.addView(next);
        ctl.addView(npRepeat);
        card.addView(ctl, new LinearLayout.LayoutParams(-1, -2));
    }

    GradientDrawable thumb(int sizeDp) {
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.OVAL);
        g.setColor(Color.WHITE);
        g.setSize(dp(sizeDp), dp(sizeDp));
        return g;
    }

    String fmt(long ms) {
        long sec = Math.max(0, ms / 1000);
        return String.format(Locale.US, "%02d:%02d", sec / 60, sec % 60);
    }

    long pos(PlaybackState st) {
        long p = st.getPosition();
        if (p < 0) p = 0;
        if (st.getState() == PlaybackState.STATE_PLAYING) {
            long dt = SystemClock.elapsedRealtime() - st.getLastPositionUpdateTime();
            p += (long) (dt * st.getPlaybackSpeed());
        }
        return Math.max(0, p);
    }

    void updateProgress() {
        if (npSeek == null) return;
        if (SystemClock.uptimeMillis() < userUntil) return;
        if (current == null) {
            npSeek.setMax(100);
            npSeek.setProgress(0);
            npElapsed.setText("00:00");
            npTotal.setText("00:00");
            return;
        }
        PlaybackState st = current.getPlaybackState();
        long dur = curDuration;
        long p = st == null ? 0 : pos(st);
        if (dur > 0) {
            npSeek.setMax((int) (dur / 1000));
            npSeek.setKeyProgressIncrement(1); // o Android redefine o passo ao mudar o max
            npSeek.setProgress((int) (Math.min(p, dur) / 1000));
        } else {
            npSeek.setMax(100);
            npSeek.setProgress(0);
        }
        npElapsed.setText(fmt(p));
        npTotal.setText(dur > 0 ? fmt(dur) : "--:--");
    }

    boolean matches(PlaybackState.CustomAction ca, String k1, String k2) {
        String id = String.valueOf(ca.getAction()).toUpperCase(Locale.ROOT);
        String nm = String.valueOf(ca.getName()).toUpperCase(Locale.ROOT);
        return id.contains(k1) || nm.contains(k1) || id.contains(k2) || nm.contains(k2);
    }

    void customToggle(String k1, String k2) {
        if (current != null) {
            PlaybackState st = current.getPlaybackState();
            if (st != null) {
                for (PlaybackState.CustomAction ca : st.getCustomActions()) {
                    if (matches(ca, k1, k2)) {
                        current.getTransportControls().sendCustomAction(ca, null);
                        return;
                    }
                }
            }
        }
        toast("O Spotify não liberou esse botão para o launcher.");
    }

    void syncWatchNext(final boolean report) {
        final Context ctx = getApplicationContext();
        new Thread(() -> {
            final String st = WatchNext.sync(ctx);
            handler.post(() -> {
                String sig = signature();
                if (!sig.equals(lastSig)) {
                    buildLists();
                    lastSig = sig;
                }
                if (report) toast("Continue assistindo do sistema: " + st);
            });
        }).start();
    }

    void askWatchNextPermission() {
        if (WatchNext.hasPermission(this)) {
            syncWatchNext(true);
            return;
        }
        requestPermissions(new String[]{WatchNext.PERM}, 9);
    }

    void tintToggle(ImageView v, String k1, String k2) {
        boolean found = false, on = false;
        if (current != null) {
            PlaybackState st = current.getPlaybackState();
            if (st != null) {
                for (PlaybackState.CustomAction ca : st.getCustomActions()) {
                    if (matches(ca, k1, k2)) {
                        found = true;
                        String id = (String.valueOf(ca.getAction()) + " " + ca.getName()).toUpperCase(Locale.ROOT);
                        on = id.contains("OFF") || id.contains("DESATIV") || id.contains("DISABLE");
                    }
                }
            }
        }
        v.setColorFilter(!found ? 0x66FFFFFF : (on ? accent : Color.WHITE));
    }

    // ------------------------------------------------------------------ apps

    int prio(String p) {
        for (int i = 0; i < PRIORITY.length; i++) if (PRIORITY[i].equals(p)) return i;
        return 100;
    }

    void addFrom(String category, Map<String, App> m) {
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.addCategory(category);
        for (ResolveInfo ri : pm.queryIntentActivities(i, 0)) {
            String pkg = ri.activityInfo.packageName;
            if (m.containsKey(pkg) || pkg.equals(getPackageName())) continue;
            App a = new App();
            a.pkg = pkg;
            a.label = String.valueOf(ri.loadLabel(pm));
            Drawable b = null;
            try {
                b = ri.activityInfo.loadBanner(pm);
            } catch (Exception ignored) {
            }
            if (b != null) {
                a.img = b;
                a.banner = true;
            } else {
                a.img = ri.loadIcon(pm);
                a.banner = false;
            }
            m.put(pkg, a);
        }
    }

    void loadApps() {
        Map<String, App> m = new LinkedHashMap<>();
        addFrom(Intent.CATEGORY_LEANBACK_LAUNCHER, m);
        if (sp.getBoolean("phoneApps", false)) addFrom(Intent.CATEGORY_LAUNCHER, m);

        // A Play Store sempre aparece, mesmo que não tenha entrada de TV.
        if (!m.containsKey(VENDING)) {
            Intent li = pm.getLeanbackLaunchIntentForPackage(VENDING);
            if (li == null) li = pm.getLaunchIntentForPackage(VENDING);
            if (li != null) {
                try {
                    ApplicationInfo ai = pm.getApplicationInfo(VENDING, 0);
                    App a = new App();
                    a.pkg = VENDING;
                    a.label = "Google Play Store";
                    Drawable b = ai.loadBanner(pm);
                    if (b != null) {
                        a.img = b;
                        a.banner = true;
                    } else {
                        a.img = ai.loadIcon(pm);
                    }
                    m.put(VENDING, a);
                } catch (Exception ignored) {
                }
            }
        }

        allApps.clear();
        allApps.addAll(m.values());
        Collections.sort(allApps, (x, y) -> {
            int px = prio(x.pkg), py = prio(y.pkg);
            if (px != py) return px - py;
            return x.label.compareToIgnoreCase(y.label);
        });
        appMap.clear();
        for (App a : allApps) appMap.put(a.pkg, a);
    }

    List<String> getFavs() {
        if (!sp.contains("favs")) {
            List<String> d = new ArrayList<>();
            for (String p : PRIORITY) if (appMap.containsKey(p)) d.add(p);
            putList("favs", d);
            return d;
        }
        return getList("favs");
    }

    String signature() {
        StringBuilder b = new StringBuilder();
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
        for (ResolveInfo r : pm.queryIntentActivities(i, 0)) b.append(r.activityInfo.packageName).append(',');
        b.append('|').append(sp.getBoolean("phoneApps", false));
        b.append('|').append(sp.getString("recent", ""));
        b.append('|').append(WatchStore.stamp(this).hashCode());
        b.append('|').append(pm.getLaunchIntentForPackage(VENDING) != null);
        return b.toString();
    }

    List<String> secOrder() {
        List<String> out = new ArrayList<>();
        for (String id : getList("order")) {
            if (Arrays.asList(SEC_IDS).contains(id) && !out.contains(id)) out.add(id);
        }
        for (String id : SEC_IDS) {
            if (out.contains(id)) continue;
            if (id.equals("continuar") && out.contains("spotify")) out.add(out.indexOf("spotify") + 1, id);
            else out.add(id);
        }
        return out;
    }

    String secName(String id) {
        for (int i = 0; i < SEC_IDS.length; i++) if (SEC_IDS[i].equals(id)) return SEC_NAMES[i];
        return id;
    }

    void buildLists() {
        loadApps();
        buildSections();
    }

    void buildSections() {
        buildSectionsInner();
        noClip(sectionsBox);
        updateMeters();
    }

    void buildSectionsInner() {
        ramTv = null;
        stoTv = null;
        sectionsBox.removeAllViews();
        List<String> hs = getList("hsec");
        List<String> hidden = getList("hidden");
        List<App> visible = new ArrayList<>();
        for (App a : allApps) {
            if (!hidden.contains(a.pkg) || a.pkg.equals(VENDING)) visible.add(a);
        }

        for (String id : secOrder()) {
            if (hs.contains(id)) continue;

            if (id.equals("spotify")) {
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
                lp.topMargin = dp(16);
                sectionsBox.addView(nowCard, lp);

            } else if (id.equals("continuar")) {
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                int shown = 0;
                for (WatchStore.Item it : WatchStore.load(this)) {
                    App a = appMap.get(it.pkg);
                    if (a != null && !hidden.contains(a.pkg)) {
                        row.addView(makeWatchTile(it, a));
                        shown++;
                    }
                }
                if (shown == 0) {
                    row.addView(emptyHint(mediaOk
                            ? "O que você assistir (Netflix, YouTube, Prime...) aparece aqui para continuar de onde parou."
                            : "Ative \"Tocando agora\" em Personalizar para registrar o que você assiste."));
                }
                sectionsBox.addView(sectionView("Continue assistindo", hscroll(row)));

            } else if (id.equals("atalhos")) {
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                List<String> hsh = getList("hshort");
                if (!hsh.contains("wifi")) {
                    row.addView(shortcutTile("Wi-Fi", () -> startSafe(new Intent(Settings.ACTION_WIFI_SETTINGS))));
                }
                if (!hsh.contains("bluetooth")) {
                    row.addView(shortcutTile("Bluetooth", () -> startSafe(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS))));
                }
                if (!hsh.contains("config")) {
                    row.addView(shortcutTile("Configurações", () -> startSafe(new Intent(Settings.ACTION_SETTINGS))));
                }
                if (row.getChildCount() == 0) {
                    row.addView(emptyHint("Todos os atalhos estão ocultos. Personalizar > Atalhos."));
                }
                sectionsBox.addView(sectionView("Atalhos", hscroll(row)));

            } else if (id.equals("favoritos")) {
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                for (String p : getFavs()) {
                    App a = appMap.get(p);
                    if (a != null && visible.contains(a)) row.addView(makeTile(a, "fav"));
                }
                if (row.getChildCount() == 0) {
                    row.addView(emptyHint("Segure OK em um app da lista para fixar aqui."));
                }
                sectionsBox.addView(sectionView("Favoritos", hscroll(row)));

            } else if (id.equals("recentes")) {
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                int shown = 0;
                for (String p : getList("recent")) {
                    App a = appMap.get(p);
                    if (a != null && visible.contains(a) && shown < 10) {
                        row.addView(makeTile(a, "rec"));
                        shown++;
                    }
                }
                if (shown == 0) row.addView(emptyHint("Os apps que você abrir aparecem aqui."));
                sectionsBox.addView(sectionView("Abertos recentemente", hscroll(row)));

            } else if (id.equals("limpeza")) {
                List<String> hsh2 = getList("hshort");
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                if (!hsh2.contains("medidor")) row.addView(meterTile());
                if (!hsh2.contains("memoria")) {
                    row.addView(shortcutTile("Limpar\nmemória", () -> cleanMemory()));
                }
                if (!hsh2.contains("cache")) {
                    row.addView(shortcutTile("Limpar\ncache", () -> cleanCache()));
                }
                if (row.getChildCount() > 0) {
                    sectionsBox.addView(sectionView("Memória e limpeza", hscroll(row)));
                }

            } else if (id.equals("apps")) {
                if (sp.getBoolean("gridApps", false)) {
                    int wDp = (int) (getResources().getDisplayMetrics().widthPixels
                            / getResources().getDisplayMetrics().density);
                    int cols = Math.max(3, (wDp - 96) / (tileWdp + 12));
                    LinearLayout grid = new LinearLayout(this);
                    grid.setOrientation(LinearLayout.VERTICAL);
                    grid.setPadding(0, dp(8), 0, 0);
                    LinearLayout row = null;
                    int n = 0;
                    for (App a : visible) {
                        if (n % cols == 0) {
                            row = new LinearLayout(this);
                            row.setOrientation(LinearLayout.HORIZONTAL);
                            grid.addView(row);
                        }
                        row.addView(makeTile(a, "all"));
                        n++;
                    }
                    sectionsBox.addView(sectionView("Todos os apps", grid));
                } else {
                    LinearLayout row = new LinearLayout(this);
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    for (App a : visible) row.addView(makeTile(a, "all"));
                    sectionsBox.addView(sectionView("Todos os apps", hscroll(row)));
                }
            }
        }
    }

    TextView emptyHint(String s) {
        TextView t = tv(s, 14, false);
        t.setTextColor(0x99FFFFFF);
        t.setPadding(0, dp(30), 0, dp(30));
        return t;
    }

    View shortcutTile(String label, final Runnable r) {
        final FrameLayout card = new FrameLayout(this);
        final GradientDrawable d = new GradientDrawable();
        final int normal = ca(accentBg(0x44));
        d.setCornerRadius(rad(1f));
        d.setColor(normal);
        card.setBackground(d);
        card.setFocusable(true);
        card.setClickable(true);
        TextView t = tv(label, 15, true);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(8), 0, dp(8), 0);
        card.addView(t, new FrameLayout.LayoutParams(-1, -1));
        card.setOnFocusChangeListener((x, f) -> {
            d.setColor(f ? accent : normal);
            d.setStroke(f ? dp(2) : 0, Color.WHITE);
            x.animate().scaleX(f ? 1.1f : 1f).scaleY(f ? 1.1f : 1f).setDuration(110).start();
        });
        card.setOnClickListener(v -> r.run());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(tileW, tileH);
        lp.rightMargin = dp(12);
        lp.bottomMargin = dp(8);
        card.setLayoutParams(lp);
        return card;
    }

    void focusFirst() {
        if (sectionsBox == null) return;
        sectionsBox.post(() -> {
            View f = sectionsBox.findFocus();
            if (f != null) return;
            ArrayList<View> fs = new ArrayList<>();
            sectionsBox.addFocusables(fs, View.FOCUS_DOWN);
            if (!fs.isEmpty()) fs.get(0).requestFocus();
        });
    }

    View makeTile(final App a, final String origin) {
        LinearLayout t = new LinearLayout(this);
        t.setOrientation(LinearLayout.VERTICAL);
        t.setFocusable(true);
        t.setClickable(true);

        final FrameLayout card = new FrameLayout(this);
        final GradientDrawable d = new GradientDrawable();
        d.setCornerRadius(rad(1f));
        d.setColor(ca(0xFF1E2230));
        card.setBackground(d);
        card.setClipToOutline(true);

        ImageView iv = new ImageView(this);
        Drawable.ConstantState cs = a.img == null ? null : a.img.getConstantState();
        iv.setImageDrawable(cs != null ? cs.newDrawable() : a.img);
        if (a.banner) {
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            card.addView(iv, new FrameLayout.LayoutParams(-1, -1));
        } else {
            iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
            card.addView(iv, new FrameLayout.LayoutParams(iconPx(), iconPx(), Gravity.CENTER));
        }
        t.addView(card, new LinearLayout.LayoutParams(tileW, tileH));

        TextView l = tv(a.label, 12, false);
        l.setSingleLine(true);
        l.setEllipsize(TextUtils.TruncateAt.END);
        l.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(tileW, -2);
        llp.topMargin = dp(5);
        t.addView(l, llp);

        t.setOnFocusChangeListener((x, f) -> {
            d.setStroke(f ? dp(3) : 0, accent);
            x.animate().scaleX(f ? 1.1f : 1f).scaleY(f ? 1.1f : 1f).setDuration(110).start();
        });
        t.setOnClickListener(v -> {
            if (!launchPackage(a.pkg)) toast("Não foi possível abrir " + a.label);
        });
        t.setOnLongClickListener(v -> {
            showAppMenu(a);
            return true;
        });

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
        lp.rightMargin = dp(12);
        lp.bottomMargin = dp(8);
        t.setLayoutParams(lp);
        return t;
    }

    String subText(WatchStore.Item it, App a) {
        String s = a.label;
        if (it.ep != null && !it.ep.isEmpty()) s += "  •  " + it.ep;
        else if (it.dur > 0) s += "  •  faltam " + Math.max(1, (it.dur - it.pos) / 60000) + " min";
        return s;
    }

    View makeWatchTile(final WatchStore.Item it, final App a) {
        final int w = (int) (tileW * 1.6f), h = (int) (tileH * 1.6f);
        LinearLayout t = new LinearLayout(this);
        t.setOrientation(LinearLayout.VERTICAL);
        t.setFocusable(true);
        t.setClickable(true);

        final FrameLayout card = new FrameLayout(this);
        final GradientDrawable d = new GradientDrawable();
        d.setCornerRadius(rad(1f));
        d.setColor(ca(0xFF1E2230));
        card.setBackground(d);
        card.setClipToOutline(true);

        ImageView iv = new ImageView(this);
        Bitmap art = BitmapFactory.decodeFile(WatchStore.artFile(this, it.key).getPath());
        if (art != null) {
            iv.setImageBitmap(art);
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            card.addView(iv, new FrameLayout.LayoutParams(-1, -1));
        } else {
            Drawable.ConstantState cs = a.img == null ? null : a.img.getConstantState();
            iv.setImageDrawable(cs != null ? cs.newDrawable() : a.img);
            if (a.banner) {
                iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                card.addView(iv, new FrameLayout.LayoutParams(-1, -1));
            } else {
                iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
                card.addView(iv, new FrameLayout.LayoutParams(iconPx(), iconPx(), Gravity.CENTER));
            }
        }
        if (it.dur > 0) {
            FrameLayout bar = new FrameLayout(this);
            bar.setBackgroundColor(0x99000000);
            View fill = new View(this);
            fill.setBackgroundColor(accent);
            int fw = (int) (w * Math.min(1.0, (double) it.pos / it.dur));
            bar.addView(fill, new FrameLayout.LayoutParams(Math.max(fw, dp(2)), -1));
            card.addView(bar, new FrameLayout.LayoutParams(-1, dp(5), Gravity.BOTTOM));
        }
        t.addView(card, new LinearLayout.LayoutParams(w, h));

        TextView l = tv(it.title, 12, true);
        l.setSingleLine(true);
        l.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(w, -2);
        llp.topMargin = dp(5);
        t.addView(l, llp);

        String sub = subText(it, a);
        TextView l2 = tv(sub, 11, false);
        l2.setTextColor(0xB3FFFFFF);
        l2.setSingleLine(true);
        l2.setEllipsize(TextUtils.TruncateAt.END);
        t.addView(l2, new LinearLayout.LayoutParams(w, -2));

        t.setOnFocusChangeListener((x, f) -> {
            d.setStroke(f ? dp(3) : 0, accent);
            x.animate().scaleX(f ? 1.08f : 1f).scaleY(f ? 1.08f : 1f).setDuration(110).start();
        });
        t.setOnClickListener(v -> resumeWatch(it, a));
        t.setOnLongClickListener(v -> {
            watchMenu(it, a);
            return true;
        });

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
        lp.rightMargin = dp(14);
        lp.bottomMargin = dp(8);
        t.setLayoutParams(lp);
        return t;
    }

    /** Abre o app. Se ele ainda tem uma sessão ativa, volta direto para a tela do player. */
    void resumeWatch(WatchStore.Item it, App a) {
        if (it.uri != null && !it.uri.isEmpty()) {
            try {
                Intent i = Intent.parseUri(it.uri, Intent.URI_INTENT_SCHEME);
                ComponentName cn = i.getComponent();
                boolean mine = it.pkg.equals(i.getPackage()) || (cn != null && it.pkg.equals(cn.getPackageName()));
                if (mine) {
                    i.setSelector(null);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    addRecent(it.pkg);
                    return;
                }
            } catch (Exception ignored) {
            }
        }
        try {
            for (MediaController c : msm.getActiveSessions(listener)) {
                if (c.getPackageName().equals(it.pkg)) {
                    PendingIntent pi = c.getSessionActivity();
                    if (pi != null) {
                        pi.send();
                        addRecent(it.pkg);
                        return;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        if (!launchPackage(it.pkg)) toast("Não foi possível abrir " + a.label);
        else if (it.dur > 0) {
            toast("Abra \"" + it.title + "\" e continue de " + fmt(it.pos));
        }
    }

    boolean launchPackage(String pkg) {
        Intent i = pm.getLeanbackLaunchIntentForPackage(pkg);
        if (i == null) i = pm.getLaunchIntentForPackage(pkg);
        if (i == null) return false;
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(i);
            addRecent(pkg);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    void addRecent(String pkg) {
        List<String> r = getList("recent");
        r.remove(pkg);
        r.add(0, pkg);
        while (r.size() > 10) r.remove(r.size() - 1);
        putList("recent", r);
    }

    void showAppMenu(final App a) {
        final List<String> names = new ArrayList<>();
        final List<Runnable> acts = new ArrayList<>();
        final List<String> favs = getFavs();
        final int idx = favs.indexOf(a.pkg);

        if (idx >= 0) {
            names.add("Remover dos favoritos");
            acts.add(() -> {
                favs.remove(a.pkg);
                putList("favs", favs);
                buildLists();
            });
            names.add("Mover para a esquerda");
            acts.add(() -> moveFav(favs, idx, -1));
            names.add("Mover para a direita");
            acts.add(() -> moveFav(favs, idx, 1));
        } else {
            names.add("Fixar nos favoritos");
            acts.add(() -> {
                favs.add(a.pkg);
                putList("favs", favs);
                buildLists();
            });
        }
        if (!a.pkg.equals(VENDING)) {
            names.add("Ocultar app");
            acts.add(() -> {
                List<String> h = getList("hidden");
                if (!h.contains(a.pkg)) h.add(a.pkg);
                putList("hidden", h);
                buildLists();
            });
        }
        names.add("Informações do app (parar, limpar dados)");
        acts.add(() -> startSafe(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + a.pkg))));

        new AlertDialog.Builder(this).setTitle(a.label)
                .setItems(names.toArray(new String[0]), (dlg, w) -> acts.get(w).run())
                .show();
    }

    void moveFav(List<String> favs, int idx, int dir) {
        int j = idx + dir;
        if (j < 0 || j >= favs.size()) return;
        Collections.swap(favs, idx, j);
        putList("favs", favs);
        buildLists();
    }

    void cleanMemory() {
        final ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final long before = availMem(am);
        for (App a : allApps) {
            if (!a.pkg.equals(getPackageName())) {
                try {
                    am.killBackgroundProcesses(a.pkg);
                } catch (Exception ignored) {
                }
            }
        }
        handler.postDelayed(() -> {
            long after = availMem(am);
            long gain = Math.max(0, (after - before) / 1048576);
            toast("Memória livre: " + (after / 1048576) + " MB (+" + gain + " MB)");
            updateMeters();
        }, 1500);
    }

    void cleanCache() {
        new AlertDialog.Builder(this).setTitle("Limpar cache")
                .setMessage("O Android não permite que um app comum limpe o cache dos outros. "
                        + "Dois caminhos:\n\n"
                        + "1) Abrir o Armazenamento da TV e escolher \"Dados em cache\" > OK.\n\n"
                        + "2) Pelo computador, com a depuração sem fio:\n"
                        + "adb shell pm trim-caches 999G\n\n"
                        + "Para um app só: segure OK nele > Informações do app > Limpar cache.")
                .setPositiveButton("Abrir armazenamento", (dlg, w) -> startSafe(
                        new Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS)))
                .setNegativeButton("Fechar", null)
                .show();
    }

    long availMem(ActivityManager am) {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        return mi.availMem;
    }

    // ------------------------------------------------------------------ relógio e clima

    void updateClock() {
        Date now = new Date();
        clockTv.setText(new SimpleDateFormat(sp.getBoolean("clock12", false) ? "h:mm a" : "HH:mm",
                Locale.getDefault()).format(now));
        dateTv.setText(new SimpleDateFormat("EEEE, d 'de' MMMM", new Locale("pt", "BR")).format(now));
    }

    String wmo(int c) {
        if (c == 0) return "Céu limpo";
        if (c <= 2) return "Poucas nuvens";
        if (c == 3) return "Nublado";
        if (c == 45 || c == 48) return "Neblina";
        if (c >= 51 && c <= 57) return "Garoa";
        if (c >= 61 && c <= 67) return "Chuva";
        if (c >= 71 && c <= 77) return "Neve";
        if (c >= 80 && c <= 82) return "Pancadas de chuva";
        if (c >= 95) return "Tempestade";
        return "Tempo variável";
    }

    String readUrl(String u) throws IOException {
        HttpURLConnection c = (HttpURLConnection) new URL(u).openConnection();
        c.setConnectTimeout(10000);
        c.setReadTimeout(15000);
        c.setRequestProperty("User-Agent", "LauncherSimples/2.0");
        BufferedReader r = new BufferedReader(new InputStreamReader(c.getInputStream(), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) sb.append(line);
        r.close();
        return sb.toString();
    }

    void fetchWeather() {
        final String city = sp.getString("city", "São Paulo");
        final String lat = sp.getString("lat", "-23.5505");
        final String lon = sp.getString("lon", "-46.6333");
        new Thread(() -> {
            String text;
            try {
                String r = readUrl("https://api.open-meteo.com/v1/forecast?latitude=" + lat
                        + "&longitude=" + lon
                        + "&current=temperature_2m,weather_code"
                        + "&daily=temperature_2m_max,temperature_2m_min&timezone=auto&forecast_days=1");
                JSONObject j = new JSONObject(r);
                JSONObject cur = j.getJSONObject("current");
                JSONObject day = j.getJSONObject("daily");
                text = city + "  •  " + Math.round(cur.getDouble("temperature_2m")) + "°C  •  "
                        + wmo(cur.getInt("weather_code")) + "  •  "
                        + Math.round(day.getJSONArray("temperature_2m_min").getDouble(0)) + "° / "
                        + Math.round(day.getJSONArray("temperature_2m_max").getDouble(0)) + "°";
            } catch (Exception e) {
                text = city + "  •  clima indisponível";
            }
            final String shown = text;
            handler.post(() -> {
                lastWeather = shown;
                weatherTv.setText(shown);
                handler.removeCallbacks(weatherRefresh);
                handler.postDelayed(weatherRefresh, 30 * 60 * 1000L);
            });
        }).start();
    }

    void searchCity(final String name) {
        if (name.isEmpty()) return;
        new Thread(() -> {
            try {
                String r = readUrl("https://geocoding-api.open-meteo.com/v1/search?count=1&language=pt&name="
                        + URLEncoder.encode(name, "UTF-8"));
                JSONArray arr = new JSONObject(r).optJSONArray("results");
                if (arr == null || arr.length() == 0) {
                    handler.post(() -> toast("Cidade não encontrada"));
                    return;
                }
                JSONObject c = arr.getJSONObject(0);
                sp.edit().putString("city", c.getString("name"))
                        .putString("lat", String.valueOf(c.getDouble("latitude")))
                        .putString("lon", String.valueOf(c.getDouble("longitude"))).apply();
                handler.post(() -> fetchWeather());
            } catch (Exception e) {
                handler.post(() -> toast("Sem conexão com a internet"));
            }
        }).start();
    }

    // ------------------------------------------------------------------ tocando agora

    void setupMedia() {
        try {
            msm.removeOnActiveSessionsChangedListener(sessL);
        } catch (Exception ignored) {
        }
        try {
            msm.addOnActiveSessionsChangedListener(sessL, listener);
            mediaOk = true;
            pickSession(msm.getActiveSessions(listener));
        } catch (SecurityException e) {
            mediaOk = false;
            pickSession(null);
        }
    }

    void refreshSessions() {
        try {
            pickSession(msm.getActiveSessions(listener));
        } catch (Exception e) {
            pickSession(null);
        }
    }

    void pickSession(List<MediaController> l) {
        MediaController best = null;
        if (l != null) {
            for (MediaController c : l) {
                if (!c.getPackageName().startsWith("com.spotify")) continue;
                PlaybackState s = c.getPlaybackState();
                if (s != null && s.getState() == PlaybackState.STATE_PLAYING) {
                    best = c;
                    break;
                }
                if (best == null) best = c;
            }
        }
        if (current != null && (best == null
                || !current.getSessionToken().equals(best.getSessionToken()))) {
            try {
                current.unregisterCallback(mcb);
            } catch (Exception ignored) {
            }
            current = null;
        }
        if (best != null && current == null) {
            current = best;
            current.registerCallback(mcb);
        }
        updateNowPlaying();
    }

    void updateNowPlaying() {
        if (npTitle == null) return;
        if (current == null) {
            npTitle.setText(mediaOk ? "Nada tocando agora" : "Tocando agora desativado");
            npSub.setText(mediaOk ? "Abra o Spotify e a música aparece aqui"
                    : "Personalizar > Ativar \"Tocando agora\"");
            npArt.setImageDrawable(null);
            npPlayIcon.setImageResource(R.drawable.ic_play);
            curDuration = 0;
            tintToggle(npShuffle, "SHUFFLE", "ALEAT");
            tintToggle(npRepeat, "REPEAT", "REPET");
            updateProgress();
            return;
        }
        MediaMetadata m = current.getMetadata();
        String t = null, a = null;
        Bitmap b = null;
        curDuration = 0;
        if (m != null) {
            t = m.getString(MediaMetadata.METADATA_KEY_TITLE);
            if (t == null) t = m.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE);
            a = m.getString(MediaMetadata.METADATA_KEY_ARTIST);
            if (a == null) a = m.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST);
            if (a == null) a = m.getString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE);
            b = m.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART);
            if (b == null) b = m.getBitmap(MediaMetadata.METADATA_KEY_ART);
            if (b == null) b = m.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON);
            curDuration = m.getLong(MediaMetadata.METADATA_KEY_DURATION);
        }
        npTitle.setText(t != null ? t : "Tocando");
        npSub.setText(a != null ? a : current.getPackageName());
        if (b != null) npArt.setImageBitmap(b);
        else npArt.setImageDrawable(null);
        PlaybackState st = current.getPlaybackState();
        boolean playing = st != null && st.getState() == PlaybackState.STATE_PLAYING;
        npPlayIcon.setImageResource(playing ? R.drawable.ic_pause : R.drawable.ic_play);
        tintToggle(npShuffle, "SHUFFLE", "ALEAT");
        tintToggle(npRepeat, "REPEAT", "REPET");
        updateProgress();
    }

    void openPlayer() {
        if (current != null) {
            try {
                PendingIntent pi = current.getSessionActivity();
                if (pi != null) {
                    pi.send();
                    return;
                }
            } catch (Exception ignored) {
            }
            if (launchPackage(current.getPackageName())) return;
        }
        if (!launchPackage(SPOTIFY)) toast("Spotify não está instalado. Abra a Play Store e instale.");
    }

    void explainListener() {
        new AlertDialog.Builder(this).setTitle("Ativar \"Tocando agora\"")
                .setMessage("O Android exige a permissão de acesso às notificações para mostrar "
                        + "o Spotify aqui. Pelo computador, rode:\n\n"
                        + "adb shell cmd notification allow_listener "
                        + "com.tvlauncher.simples/com.tvlauncher.simples.NowPlayingService\n\n"
                        + "Ou tente abrir os ajustes da TV pelo botão abaixo.")
                .setPositiveButton("Abrir ajustes", (dlg, w) -> startSafe(
                        new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")))
                .setNegativeButton("Fechar", null)
                .show();
    }

    // ------------------------------------------------------------------ fundo

    void applyBackground() {
        String t = sp.getString("bg", "g:0");
        boolean done = false;
        if (t.equals("img")) {
            Bitmap b = BitmapFactory.decodeFile(new File(getFilesDir(), "fundo.jpg").getPath());
            if (b != null) {
                int bl = sp.getInt("blur", 0);
                if (bl > 0) b = blurred(b, bl);
                bgView.setImageBitmap(b);
                done = true;
            }
        }
        if (!done) {
            int idx = 0;
            try {
                if (t.startsWith("g:")) idx = Integer.parseInt(t.substring(2));
            } catch (Exception ignored) {
            }
            if (idx < 0 || idx >= BG_COLORS.length) idx = 0;
            GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.TL_BR, BG_COLORS[idx]);
            bgView.setImageDrawable(g);
        }
        int pct = sp.getInt("dim", DEF_DIM);
        dim.setBackgroundColor(Color.argb(255 * pct / 100, 0, 0, 0));
    }

    void pickGradient() {
        new AlertDialog.Builder(this).setTitle("Cores e degradês")
                .setItems(BG_NAMES, (dlg, w) -> {
                    sp.edit().putString("bg", "g:" + w).apply();
                    applyBackground();
                }).show();
    }

    void pickImage() {
        String perm = Build.VERSION.SDK_INT >= 33
                ? "android.permission.READ_MEDIA_IMAGES"
                : "android.permission.READ_EXTERNAL_STORAGE";
        if (Build.VERSION.SDK_INT >= 23
                && checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{perm}, 7);
            return;
        }
        showImages();
    }

    @Override
    public void onRequestPermissionsResult(int rc, String[] perms, int[] res) {
        super.onRequestPermissionsResult(rc, perms, res);
        if (rc == 7) showImages();
        else if (rc == 8) rotateBg();
        else if (rc == 9) {
            if (WatchNext.hasPermission(this)) {
                syncWatchNext(true);
            } else {
                new AlertDialog.Builder(this).setTitle("Permissão não liberada")
                        .setMessage("Libere pelo computador:\n\n"
                                + "adb shell pm grant com.tvlauncher.simples android.permission.READ_TV_LISTINGS\n\n"
                                + "Depois reabra o launcher. Se o comando também falhar, esta TV não permite "
                                + "que um launcher comum leia o \"Continue assistindo\" do sistema. "
                                + "Nesse caso o launcher usa só o que os apps informam como sessão de mídia.")
                        .setPositiveButton("OK", null).show();
            }
        }
    }

    void scan(File d, int depth, List<File> out) {
        if (d == null || depth < 0 || out.size() >= 60) return;
        File[] fs = d.listFiles();
        if (fs == null) return;
        for (File f : fs) {
            if (f.isDirectory()) {
                scan(f, depth - 1, out);
            } else {
                String n = f.getName().toLowerCase(Locale.ROOT);
                if (n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".png") || n.endsWith(".webp")) {
                    out.add(f);
                }
            }
        }
    }

    void showImages() {
        final List<File> imgs = new ArrayList<>();
        File[] roots = {
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                getExternalFilesDir(null)};
        for (File r : roots) scan(r, 2, imgs);

        if (imgs.isEmpty()) {
            new AlertDialog.Builder(this).setTitle("Nenhuma imagem encontrada")
                    .setMessage("Copie uma foto para a pasta Download da TV. Pelo computador:\n\n"
                            + "adb push minha-foto.jpg /sdcard/Download/\n\n"
                            + "Depois abra esta opção de novo. Se preferir, use a opção "
                            + "\"foto aleatória da internet\".")
                    .setPositiveButton("OK", null).show();
            return;
        }
        String[] names = new String[imgs.size()];
        for (int i = 0; i < names.length; i++) {
            File f = imgs.get(i);
            names[i] = f.getName() + "  (" + (f.length() / 1024) + " KB)";
        }
        new AlertDialog.Builder(this).setTitle("Escolha a imagem de fundo")
                .setItems(names, (dlg, w) -> importImage(imgs.get(w))).show();
    }

    void importImage(final File f) {
        importImage(f, false);
    }

    void importImage(final File f, final boolean quiet) {
        if (!quiet) toast("Aplicando fundo...");
        new Thread(() -> {
            try {
                BitmapFactory.Options o = new BitmapFactory.Options();
                o.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(f.getPath(), o);
                int s = 1;
                while (o.outWidth / (s * 2) >= 1920) s *= 2;
                BitmapFactory.Options o2 = new BitmapFactory.Options();
                o2.inSampleSize = s;
                saveBg(BitmapFactory.decodeFile(f.getPath(), o2), quiet);
            } catch (Exception e) {
                if (!quiet) handler.post(() -> toast("Não consegui usar essa imagem"));
            }
        }).start();
    }

    void downloadRandom() {
        toast("Baixando foto...");
        new Thread(() -> {
            try {
                HttpURLConnection c = (HttpURLConnection) new URL("https://picsum.photos/1920/1080").openConnection();
                c.setConnectTimeout(15000);
                c.setReadTimeout(30000);
                c.setInstanceFollowRedirects(true);
                c.setRequestProperty("User-Agent", "Mozilla/5.0");
                InputStream in = c.getInputStream();
                Bitmap b = BitmapFactory.decodeStream(in);
                in.close();
                saveBg(b);
            } catch (Exception e) {
                handler.post(() -> toast("Não consegui baixar a foto. Verifique a internet."));
            }
        }).start();
    }

    void saveBg(Bitmap b) throws IOException {
        saveBg(b, false);
    }

    void saveBg(Bitmap b, final boolean quiet) throws IOException {
        if (b == null) throw new IOException("bitmap nulo");
        FileOutputStream out = new FileOutputStream(new File(getFilesDir(), "fundo.jpg"));
        b.compress(Bitmap.CompressFormat.JPEG, 88, out);
        out.close();
        sp.edit().putString("bg", "img").apply();
        handler.post(() -> {
            applyBackground();
            if (!quiet) toast("Fundo atualizado");
        });
    }

    // ------------------------------------------------------------------ menu Personalizar

    void showSections() {
        final List<String> order = secOrder();
        final List<String> hs = getList("hsec");
        String[] names = new String[order.size()];
        for (int i = 0; i < names.length; i++) {
            String id = order.get(i);
            names[i] = (i + 1) + ". " + secName(id) + (hs.contains(id) ? "   [oculta]" : "");
        }
        new AlertDialog.Builder(this).setTitle("Seções da tela inicial")
                .setItems(names, (dlg, w) -> sectionActions(order.get(w)))
                .setNegativeButton("Voltar", (dlg, w) -> showSettings())
                .show();
    }

    void sectionActions(final String id) {
        final List<String> order = secOrder();
        final List<String> hs = getList("hsec");
        final int i = order.indexOf(id);
        final boolean hid = hs.contains(id);
        String[] items = {"Subir", "Descer", hid ? "Mostrar" : "Ocultar"};
        new AlertDialog.Builder(this).setTitle(secName(id))
                .setItems(items, (dlg, w) -> {
                    if (w == 0 && i > 0) Collections.swap(order, i, i - 1);
                    if (w == 1 && i < order.size() - 1) Collections.swap(order, i, i + 1);
                    if (w == 2) {
                        if (hid) hs.remove(id);
                        else hs.add(id);
                        putList("hsec", hs);
                    }
                    putList("order", order);
                    buildSections();
                    showSections();
                }).show();
    }

    // ------------------------------------------------------------------ menus de personalização

    void showMenu(String title, List<String> names, final List<Runnable> acts, final Runnable back) {
        AlertDialog.Builder bd = new AlertDialog.Builder(this).setTitle(title)
                .setItems(names.toArray(new String[0]), (dlg, w) -> acts.get(w).run());
        if (back != null) bd.setNegativeButton("Voltar", (dlg, w) -> back.run());
        bd.show();
    }

    String onOff(boolean v) {
        return v ? "ligado" : "desligado";
    }

    void showSettings() {
        final List<String> names = new ArrayList<>();
        final List<Runnable> acts = new ArrayList<>();

        names.add("Seções: ordem, ocultar e mostrar");
        acts.add(() -> showSections());

        names.add("Atalhos: escolher quais aparecem  >");
        acts.add(() -> showShortcutsMenu());

        names.add("Fundo  >");
        acts.add(() -> showBgMenu());
        names.add("Aparência (cor, cantos, transparência, fonte)  >");
        acts.add(() -> showLookMenu());
        names.add("Relógio e clima  >");
        acts.add(() -> showClockMenu());
        names.add("Tamanhos (em %)  >");
        acts.add(() -> showSizeMenu());

        names.add("Mostrar apps de celular: " + (sp.getBoolean("phoneApps", false) ? "sim" : "não"));
        acts.add(() -> {
            sp.edit().putBoolean("phoneApps", !sp.getBoolean("phoneApps", false)).apply();
            buildLists();
            lastSig = signature();
            showSettings();
        });

        names.add("Mostrar apps ocultos de novo");
        acts.add(() -> {
            putList("hidden", new ArrayList<String>());
            buildLists();
            toast("Apps ocultos restaurados");
        });

        names.add("Ativar \"Tocando agora\" e \"Continue assistindo\"");
        acts.add(() -> explainListener());

        names.add("Liberar \"Continue assistindo\" do sistema (Netflix, Crunchyroll...)");
        acts.add(() -> askWatchNextPermission());

        names.add("Diagnóstico do Continue assistindo");
        acts.add(() -> showDiagnostic());

        names.add("Voltar tudo ao padrão");
        acts.add(() -> {
            sp.edit().clear().apply();
            accent = loadAccent();
            rebuildAll();
            fetchWeather();
            toast("Configurações restauradas");
        });

        showMenu("Personalizar", names, acts, null);
    }

    // ---- diagnóstico

    String stateName(int st) {
        switch (st) {
            case PlaybackState.STATE_PLAYING:
                return "tocando";
            case PlaybackState.STATE_PAUSED:
                return "pausado";
            case PlaybackState.STATE_BUFFERING:
                return "carregando";
            case PlaybackState.STATE_STOPPED:
                return "parado";
            case PlaybackState.STATE_NONE:
                return "nenhum";
            default:
                return "outro (" + st + ")";
        }
    }

    void showDiagnostic() {
        StringBuilder sb = new StringBuilder();
        try {
            List<MediaController> l = msm.getActiveSessions(listener);
            sb.append("Acesso às notificações: ATIVO\n\n");
            if (l.isEmpty()) {
                sb.append("Nenhum app está com sessão de mídia agora.\n"
                        + "Abra um vídeo (Netflix, Crunchyroll...), toque por alguns segundos, "
                        + "aperte Home e abra este diagnóstico de novo.\n\n");
            }
            for (MediaController c : l) {
                MediaMetadata m = c.getMetadata();
                PlaybackState st = c.getPlaybackState();
                String t = null;
                long dur = 0;
                if (m != null) {
                    t = m.getString(MediaMetadata.METADATA_KEY_TITLE);
                    if (t == null) t = m.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE);
                    dur = m.getLong(MediaMetadata.METADATA_KEY_DURATION);
                }
                sb.append("• ").append(c.getPackageName())
                        .append("\n   título: ").append(t == null ? "(o app não informa)" : t)
                        .append("\n   estado: ").append(st == null ? "(nenhum)" : stateName(st.getState()))
                        .append("\n   posição: ").append(st == null ? "--" : fmt(pos(st)))
                        .append("  /  duração: ").append(dur > 0 ? fmt(dur) : "--")
                        .append("\n\n");
            }
        } catch (SecurityException e) {
            sb.append("Acesso às notificações: INATIVO\nRode no computador:\n"
                    + "adb shell cmd notification allow_listener "
                    + "com.tvlauncher.simples/com.tvlauncher.simples.NowPlayingService\n\n");
        } catch (Exception e) {
            sb.append("Não consegui ler as sessões: ").append(e).append("\n\n");
        }
        sb.append("Continue assistindo do sistema (Watch Next): ")
                .append(WatchNext.hasPermission(this) ? "permissão ok" : "SEM permissão")
                .append(" - ").append(WatchStore.getInfo(this)).append("\n\n");
        List<WatchStore.Item> saved = WatchStore.load(this);
        sb.append("Itens salvos em Continue assistindo: ").append(saved.size());
        for (WatchStore.Item it : saved) {
            sb.append("\n   - ").append(it.title).append(" (").append(it.pkg).append(") ")
                    .append(fmt(it.pos)).append(it.dur > 0 ? " / " + fmt(it.dur) : "");
        }
        sb.append("\n\nO YouTube não entra na lista (ele já tem a própria).");
        new AlertDialog.Builder(this).setTitle("Diagnóstico")
                .setMessage(sb.toString())
                .setPositiveButton("Fechar", null)
                .setNegativeButton("Voltar", (dlg, w) -> showSettings())
                .show();
    }

    // ---- atalhos

    void showShortcutsMenu() {
        final List<String> names = new ArrayList<>();
        final List<Runnable> acts = new ArrayList<>();
        final List<String> hsh = getList("hshort");
        for (int i = 0; i < SHORT_IDS.length; i++) {
            final String id = SHORT_IDS[i];
            names.add(SHORT_NAMES[i] + ": " + (hsh.contains(id) ? "oculto" : "visível") + " (toque para mudar)");
            acts.add(() -> {
                List<String> h2 = getList("hshort");
                if (h2.contains(id)) h2.remove(id);
                else h2.add(id);
                putList("hshort", h2);
                buildSections();
                showShortcutsMenu();
            });
        }
        showMenu("Atalhos", names, acts, () -> showSettings());
    }

    // ---- fundo

    void showBgMenu() {
        final List<String> names = new ArrayList<>();
        final List<Runnable> acts = new ArrayList<>();
        final Runnable back = () -> showSettings();

        names.add("Cores e degradês");
        acts.add(() -> pickGradient());
        names.add("Escolher imagem da TV (Download, Fotos)");
        acts.add(() -> pickImage());
        names.add("Foto aleatória da internet");
        acts.add(() -> downloadRandom());

        int mode = clampI(sp.getInt("bgMode", 0), 0, BGMODE_NAMES.length - 1);
        names.add("Fundo automático: " + BGMODE_NAMES[mode]);
        acts.add(() -> pickBgMode());
        names.add("Trocar o fundo agora (pasta Download/Fundos)");
        acts.add(() -> {
            if (!storageOk()) {
                requestPermissions(new String[]{storagePerm()}, 8);
            } else {
                rotateBg();
            }
        });

        names.add("Desfoque do fundo: " + BLUR_NAMES[clampI(sp.getInt("blur", 0), 0, 3)] + " (toque para mudar)");
        acts.add(() -> {
            sp.edit().putInt("blur", (sp.getInt("blur", 0) + 1) % BLUR_NAMES.length).apply();
            applyBackground();
            showBgMenu();
        });

        names.add("Escurecimento do fundo: " + sp.getInt("dim", DEF_DIM) + "% (toque para mudar)");
        acts.add(() -> {
            int cur = sp.getInt("dim", DEF_DIM);
            cur = cur >= 80 ? 0 : cur + 10;
            sp.edit().putInt("dim", cur).apply();
            applyBackground();
            showBgMenu();
        });
        showMenu("Fundo", names, acts, back);
    }

    void pickBgMode() {
        new AlertDialog.Builder(this).setTitle("Fundo automático")
                .setItems(BGMODE_NAMES, (dlg, w) -> {
                    sp.edit().putInt("bgMode", w).putLong("bgLast", 0).apply();
                    if (w > 0) {
                        toast("Copie as fotos para a pasta Download/Fundos da TV (pelo adb: "
                                + "adb push foto.jpg /sdcard/Download/Fundos/)");
                        if (!storageOk()) requestPermissions(new String[]{storagePerm()}, 8);
                        else maybeRotateBg(false);
                    }
                    showBgMenu();
                }).setNegativeButton("Voltar", (dlg, w) -> showBgMenu()).show();
    }

    String storagePerm() {
        return Build.VERSION.SDK_INT >= 33 ? "android.permission.READ_MEDIA_IMAGES"
                : "android.permission.READ_EXTERNAL_STORAGE";
    }

    boolean storageOk() {
        return Build.VERSION.SDK_INT < 23
                || checkSelfPermission(storagePerm()) == PackageManager.PERMISSION_GRANTED;
    }

    List<File> bgImages() {
        List<File> out = new ArrayList<>();
        scan(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Fundos"), 1, out);
        if (out.isEmpty()) {
            scan(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Fundos"), 1, out);
        }
        return out;
    }

    void maybeRotateBg(boolean homePress) {
        int mode = sp.getInt("bgMode", 0);
        if (mode == 0) return;
        long now = System.currentTimeMillis(), last = sp.getLong("bgLast", 0);
        boolean due = (mode == 1 && now - last >= 3600000L)
                || (mode == 2 && now - last >= 86400000L)
                || (mode == 3 && homePress);
        if (due && storageOk()) rotateBg();
    }

    void rotateBg() {
        List<File> imgs = bgImages();
        if (imgs.isEmpty()) {
            toast("Nenhuma imagem em Download/Fundos. Copie fotos para essa pasta da TV.");
            return;
        }
        String cur = sp.getString("bgName", "");
        File pick = imgs.get(rnd.nextInt(imgs.size()));
        for (int i = 0; i < 6 && imgs.size() > 1 && pick.getName().equals(cur); i++) {
            pick = imgs.get(rnd.nextInt(imgs.size()));
        }
        sp.edit().putLong("bgLast", System.currentTimeMillis()).putString("bgName", pick.getName()).apply();
        importImage(pick, true);
    }

    Bitmap blurred(Bitmap src, int level) {
        int f = level == 1 ? 6 : (level == 2 ? 12 : 20);
        int w = Math.max(8, src.getWidth() / f), h = Math.max(8, src.getHeight() / f);
        Bitmap small = Bitmap.createScaledBitmap(src, w, h, true);
        Bitmap m = small.copy(Bitmap.Config.ARGB_8888, true);
        boxBlur(m, level + 1);
        return m;
    }

    void boxBlur(Bitmap bm, int passes) {
        int w = bm.getWidth(), h = bm.getHeight();
        int[] px = new int[w * h], out = new int[w * h];
        bm.getPixels(px, 0, w, 0, 0, w, h);
        for (int ps = 0; ps < passes; ps++) {
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int a = 0, r = 0, g = 0, b = 0, n = 0;
                    for (int dy = -1; dy <= 1; dy++) {
                        int yy = y + dy;
                        if (yy < 0 || yy >= h) continue;
                        for (int dx = -1; dx <= 1; dx++) {
                            int xx = x + dx;
                            if (xx < 0 || xx >= w) continue;
                            int c = px[yy * w + xx];
                            a += (c >>> 24);
                            r += (c >> 16) & 0xFF;
                            g += (c >> 8) & 0xFF;
                            b += c & 0xFF;
                            n++;
                        }
                    }
                    out[y * w + x] = ((a / n) << 24) | ((r / n) << 16) | ((g / n) << 8) | (b / n);
                }
            }
            int[] t = px;
            px = out;
            out = t;
        }
        bm.setPixels(px, 0, w, 0, 0, w, h);
    }

    // ---- aparência

    void showLookMenu() {
        final List<String> names = new ArrayList<>();
        final List<Runnable> acts = new ArrayList<>();
        final Runnable back = () -> showSettings();

        int ai = sp.getInt("accent", 0);
        String an = ai >= ACCENTS.length
                ? String.format("Livre (#%06X)", loadAccent() & 0xFFFFFF)
                : ACCENT_NAMES[clampI(ai, 0, ACCENTS.length - 1)];
        names.add("Cor de destaque: " + an);
        acts.add(() -> {
            String[] items = new String[ACCENT_NAMES.length + 1];
            System.arraycopy(ACCENT_NAMES, 0, items, 0, ACCENT_NAMES.length);
            items[ACCENT_NAMES.length] = "Cor livre (escolher qualquer cor)...";
            new AlertDialog.Builder(this).setTitle("Cor de destaque")
                    .setItems(items, (dlg, w) -> {
                        if (w < ACCENTS.length) {
                            sp.edit().putInt("accent", w).apply();
                            accent = ACCENTS[w];
                            rebuildAll();
                            showLookMenu();
                        } else {
                            pickCustomColor();
                        }
                    }).setNegativeButton("Voltar", (dlg, w) -> showLookMenu()).show();
        });

        names.add("Arredondamento dos cantos: " + pct("radius", 33) + "%");
        acts.add(() -> pickPercent("Arredondamento dos cantos", "radius", pct("radius", 33), 33, 0, 100, 5,
                () -> {
                    rebuildAll();
                    showLookMenu();
                }));

        names.add("Transparência dos cartões (opacidade): " + pct("opacity", 100) + "%");
        acts.add(() -> pickPercent("Opacidade dos cartões (menos = fundo aparece mais)", "opacity",
                pct("opacity", 100), 100, 0, 200, 10, () -> {
                    rebuildAll();
                    showLookMenu();
                }));

        names.add("Fonte: " + FONT_NAMES[clampI(sp.getInt("font", 0), 0, FONT_NAMES.length - 1)]);
        acts.add(() -> new AlertDialog.Builder(this).setTitle("Fonte")
                .setItems(FONT_NAMES, (dlg, w) -> {
                    sp.edit().putInt("font", w).apply();
                    rebuildAll();
                    showLookMenu();
                }).setNegativeButton("Voltar", (dlg, w) -> showLookMenu()).show());
        showMenu("Aparência", names, acts, back);
    }

    /** Escolha de porcentagem com barra (esquerda/direita no controle). */
    void pickPercent(String title, final String key, int cur, final int defReset, final int min,
                     final int max, final int step, final Runnable apply) {
        final TextView lab = tv(clampI(cur, min, max) + "%", 24, true);
        lab.setGravity(Gravity.CENTER);
        final SeekBar sb = new SeekBar(this);
        sb.setMax((max - min) / step);
        sb.setKeyProgressIncrement(1);
        sb.setProgress((clampI(cur, min, max) - min) / step);
        sb.setProgressTintList(ColorStateList.valueOf(accent));
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar b, int p, boolean u) {
                lab.setText((min + p * step) + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar b) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar b) {
            }
        });
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(24), dp(16), dp(24), dp(8));
        box.addView(lab, new LinearLayout.LayoutParams(-1, -2));
        box.addView(sb, new LinearLayout.LayoutParams(-1, dp(40)));
        new AlertDialog.Builder(this).setTitle(title).setView(box)
                .setPositiveButton("Aplicar", (dlg, w) -> {
                    sp.edit().putInt(key, min + sb.getProgress() * step).apply();
                    apply.run();
                })
                .setNeutralButton("Padrão", (dlg, w) -> {
                    sp.edit().putInt(key, defReset).apply();
                    apply.run();
                })
                .setNegativeButton("Cancelar", null).show();
    }

    /** Cor de destaque livre: três barras (vermelho, verde, azul) com prévia. */
    void pickCustomColor() {
        final int[] c = {Color.red(accent), Color.green(accent), Color.blue(accent)};
        final View prev = new View(this);
        final TextView hex = tv("", 18, true);
        hex.setGravity(Gravity.CENTER);
        final Runnable upd = () -> {
            int col = Color.rgb(c[0], c[1], c[2]);
            prev.setBackgroundColor(col);
            hex.setText(String.format("#%06X", col & 0xFFFFFF));
        };
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(24), dp(16), dp(24), dp(8));
        box.addView(prev, new LinearLayout.LayoutParams(-1, dp(44)));
        box.addView(hex, new LinearLayout.LayoutParams(-1, -2));
        String[] nm = {"Vermelho", "Verde", "Azul"};
        int[] tint = {0xFFFF5252, 0xFF69F0AE, 0xFF448AFF};
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            TextView l = tv(nm[i], 13, false);
            box.addView(l);
            SeekBar sb = new SeekBar(this);
            sb.setMax(255);
            sb.setKeyProgressIncrement(5);
            sb.setProgress(c[i]);
            sb.setProgressTintList(ColorStateList.valueOf(tint[i]));
            sb.setThumbTintList(ColorStateList.valueOf(tint[i]));
            sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar b, int p, boolean u) {
                    c[idx] = p;
                    upd.run();
                }

                @Override
                public void onStartTrackingTouch(SeekBar b) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar b) {
                }
            });
            box.addView(sb, new LinearLayout.LayoutParams(-1, dp(36)));
        }
        upd.run();
        new AlertDialog.Builder(this).setTitle("Cor livre").setView(box)
                .setPositiveButton("Aplicar", (dlg, w) -> {
                    int col = Color.rgb(c[0], c[1], c[2]);
                    sp.edit().putInt("accent", ACCENTS.length).putInt("accentRgb", col & 0xFFFFFF).apply();
                    accent = 0xFF000000 | (col & 0xFFFFFF);
                    rebuildAll();
                    showLookMenu();
                })
                .setNegativeButton("Cancelar", (dlg, w) -> showLookMenu()).show();
    }

    // ---- relógio e clima

    void showClockMenu() {
        final List<String> names = new ArrayList<>();
        final List<Runnable> acts = new ArrayList<>();

        names.add("Tamanho do relógio: " + CLOCK_NAMES[clampI(sp.getInt("clockSize", 1), 0, CLOCK_NAMES.length - 1)]
                + " (toque para mudar)");
        acts.add(() -> {
            sp.edit().putInt("clockSize", (sp.getInt("clockSize", 1) + 1) % CLOCK_NAMES.length).apply();
            rebuildAll();
            showClockMenu();
        });
        names.add("Formato da hora: " + (sp.getBoolean("clock12", false) ? "12 horas (AM/PM)" : "24 horas"));
        acts.add(() -> {
            sp.edit().putBoolean("clock12", !sp.getBoolean("clock12", false)).apply();
            updateClock();
            showClockMenu();
        });
        names.add("Data: " + (sp.getBoolean("hideDate", false) ? "oculta" : "visível"));
        acts.add(() -> {
            sp.edit().putBoolean("hideDate", !sp.getBoolean("hideDate", false)).apply();
            rebuildAll();
            showClockMenu();
        });
        names.add("Clima: " + (sp.getBoolean("hideWeather", false) ? "oculto" : "visível"));
        acts.add(() -> {
            sp.edit().putBoolean("hideWeather", !sp.getBoolean("hideWeather", false)).apply();
            rebuildAll();
            showClockMenu();
        });
        names.add("Cidade do clima: " + sp.getString("city", "São Paulo"));
        acts.add(() -> pickCity());
        showMenu("Relógio e clima", names, acts, () -> showSettings());
    }

    // ---- tamanhos

    void showSizeMenu() {
        final List<String> names = new ArrayList<>();
        final List<Runnable> acts = new ArrayList<>();
        final Runnable again = () -> {
            rebuildAll();
            showSizeMenu();
        };

        names.add("Cartões e ícones dos apps: " + tilePct() + "%");
        acts.add(() -> pickPercent("Tamanho dos cartões e ícones", "tilePct", tilePct(), 83, 10, 100, 5, again));
        names.add("Títulos e espaçamento das seções: " + pct("secText", 100) + "%");
        acts.add(() -> pickPercent("Tamanho dos títulos das seções", "secText", pct("secText", 100), 100, 10, 100, 5, again));
        names.add("Tocando agora (Spotify): " + pct("npPct", 100) + "%");
        acts.add(() -> pickPercent("Tamanho do Tocando agora", "npPct", pct("npPct", 100), 100, 10, 100, 5, again));
        names.add("Todos os apps: " + (sp.getBoolean("gridApps", false) ? "em grade" : "em linha") + " (toque para mudar)");
        acts.add(() -> {
            sp.edit().putBoolean("gridApps", !sp.getBoolean("gridApps", false)).apply();
            buildLists();
            showSizeMenu();
        });
        showMenu("Tamanhos", names, acts, () -> showSettings());
    }

    // ---- banner grande e cartão de medidores

    void watchMenu(final WatchStore.Item it, final App a) {
        new AlertDialog.Builder(this).setTitle(it.title)
                .setItems(new String[]{"Continuar", "Remover da lista", "Limpar toda a lista"},
                        (dlg, which) -> {
                            if (which == 0) {
                                resumeWatch(it, a);
                            } else if (which == 1) {
                                WatchStore.remove(this, it.key);
                                buildLists();
                            } else {
                                WatchStore.clear(this);
                                buildLists();
                            }
                        }).show();
    }

    View meterTile() {
        LinearLayout t = new LinearLayout(this);
        t.setOrientation(LinearLayout.VERTICAL);
        t.setGravity(Gravity.CENTER_VERTICAL);
        t.setPadding(dp(10), dp(4), dp(10), dp(4));
        final GradientDrawable d = new GradientDrawable();
        final int normal = ca(accentBg(0x44));
        d.setCornerRadius(rad(1f));
        d.setColor(normal);
        t.setBackground(d);
        t.setFocusable(true);
        t.setClickable(true);
        t.setOnFocusChangeListener((x, f) -> {
            d.setColor(f ? accent : normal);
            d.setStroke(f ? dp(2) : 0, Color.WHITE);
            x.animate().scaleX(f ? 1.06f : 1f).scaleY(f ? 1.06f : 1f).setDuration(110).start();
        });
        t.setOnClickListener(v -> {
            updateMeters();
            toast("Memória e armazenamento atualizados");
        });

        ramTv = tv("RAM", 11, true);
        ramTv.setSingleLine(true);
        ramTv.setEllipsize(TextUtils.TruncateAt.END);
        stoTv = tv("Armaz.", 11, true);
        stoTv.setSingleLine(true);
        stoTv.setEllipsize(TextUtils.TruncateAt.END);
        ramFill = new View(this);
        ramRest = new View(this);
        stoFill = new View(this);
        stoRest = new View(this);
        ramFill.setBackgroundColor(accent);
        stoFill.setBackgroundColor(accent);

        t.addView(ramTv);
        LinearLayout.LayoutParams b1 = new LinearLayout.LayoutParams(-1, dp(5));
        b1.topMargin = dp(3);
        b1.bottomMargin = dp(8);
        t.addView(barBox(ramFill, ramRest), b1);
        t.addView(stoTv);
        LinearLayout.LayoutParams b2 = new LinearLayout.LayoutParams(-1, dp(5));
        b2.topMargin = dp(3);
        t.addView(barBox(stoFill, stoRest), b2);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Math.round(tileW * 1.7f), tileH);
        lp.rightMargin = dp(12);
        lp.bottomMargin = dp(8);
        t.setLayoutParams(lp);
        return t;
    }

    LinearLayout barBox(View fill, View rest) {
        LinearLayout b = new LinearLayout(this);
        b.setOrientation(LinearLayout.HORIZONTAL);
        b.setBackgroundColor(0x44FFFFFF);
        b.addView(fill, new LinearLayout.LayoutParams(0, -1, 0.5f));
        b.addView(rest, new LinearLayout.LayoutParams(0, -1, 0.5f));
        return b;
    }

    void setBar(View fill, View rest, float f) {
        f = Math.max(0.02f, Math.min(1f, f));
        ((LinearLayout.LayoutParams) fill.getLayoutParams()).weight = f;
        ((LinearLayout.LayoutParams) rest.getLayoutParams()).weight = 1f - f;
        fill.requestLayout();
        rest.requestLayout();
    }

    String gb(long bytes) {
        return String.format(Locale.getDefault(), "%.1f", bytes / 1073741824.0);
    }

    void updateMeters() {
        if (ramTv == null || stoTv == null) return;
        try {
            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
            am.getMemoryInfo(mi);
            long tot = mi.totalMem, used = Math.max(0, tot - mi.availMem);
            ramTv.setText("RAM  " + gb(used) + " / " + gb(tot) + " GB");
            setBar(ramFill, ramRest, tot > 0 ? (float) used / tot : 0f);
            StatFs sf = new StatFs(Environment.getDataDirectory().getPath());
            long st = sf.getTotalBytes(), fr = sf.getAvailableBytes();
            stoTv.setText("Armaz.  " + gb(st - fr) + " / " + gb(st) + " GB");
            setBar(stoFill, stoRest, st > 0 ? (float) (st - fr) / st : 0f);
        } catch (Exception ignored) {
        }
    }

    void pickCity() {
        String[] names = new String[CITIES.length + 1];
        for (int i = 0; i < CITIES.length; i++) names[i] = CITIES[i][0];
        names[CITIES.length] = "Digitar outra cidade...";
        new AlertDialog.Builder(this).setTitle("Cidade do clima")
                .setItems(names, (dlg, w) -> {
                    if (w < CITIES.length) {
                        sp.edit().putString("city", CITIES[w][0])
                                .putString("lat", CITIES[w][1])
                                .putString("lon", CITIES[w][2]).apply();
                        fetchWeather();
                    } else {
                        final EditText et = new EditText(this);
                        et.setSingleLine(true);
                        et.setTextColor(Color.WHITE);
                        et.setHint("Nome da cidade");
                        new AlertDialog.Builder(this).setTitle("Digite a cidade").setView(et)
                                .setPositiveButton("Buscar", (d2, w2) -> searchCity(et.getText().toString().trim()))
                                .setNegativeButton("Cancelar", null).show();
                    }
                }).show();
    }
}

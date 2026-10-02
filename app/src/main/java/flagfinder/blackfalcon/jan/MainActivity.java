package flagfinder.blackfalcon.jan;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MainActivity extends Activity {

    // letter, English, Urdu, hex
    private static final String[][] COLORS = {
            {"R", "Red", "سرخ", "#D32F2F"},
            {"W", "White", "سفید", "#FFFFFF"},
            {"B", "Blue", "نیلا", "#1E4FB5"},
            {"G", "Green", "سبز", "#2E9E4F"},
            {"Y", "Yellow", "پیلا", "#FBC02D"},
            {"K", "Black", "کالا", "#111111"},
            {"O", "Orange", "نارنجی", "#F57C00"},
            {"L", "Light Blue", "آسمانی", "#4FC3F7"}
    };

    // code, English, Urdu
    private static final String[][] CONTINENTS = {
            {"ALL", "All", "سب"},
            {"AS", "Asia", "ایشیا"},
            {"AF", "Africa", "افریقہ"},
            {"EU", "Europe", "یورپ"},
            {"NA", "N. America", "شمالی امریکہ"},
            {"SA", "S. America", "جنوبی امریکہ"},
            {"OC", "Oceania", "اوقیانوسیہ"}
    };

    static class Country {
        String flag, en, ur, colors, cont, lang, cap, cur;
        String enL, urL, hay;
    }

    private final List<Country> all = new ArrayList<>();
    private final List<Country> shown = new ArrayList<>();
    private final Set<String> selected = new LinkedHashSet<>();
    private final Map<String, String> hexOf = new HashMap<>();
    private final Map<String, String> contName = new HashMap<>();
    private final List<TextView> chips = new ArrayList<>();
    private final List<TextView> contChips = new ArrayList<>();
    private String selCont = "ALL";
    private TextView countView;
    private CheckBox exactBox;
    private EditText searchBox;
    private BaseAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        for (String[] c : COLORS) hexOf.put(c[0], c[3]);
        contName.put("AS", "Asia");
        contName.put("AF", "Africa");
        contName.put("EU", "Europe");
        contName.put("NA", "North America");
        contName.put("SA", "South America");
        contName.put("OC", "Oceania");
        loadCountries();

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);
        root.setPadding(dp(10), dp(6), dp(10), dp(4));
        root.setFitsSystemWindows(true);

        TextView title = makeText("دنیا کے جھنڈے • World Flags", 20, true);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, dp(4));
        root.addView(title);

        // ---- color chips ----
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(4);
        for (final String[] c : COLORS) {
            final TextView chip = new TextView(this);
            chip.setGravity(Gravity.CENTER);
            chip.setTextSize(12);
            chip.setPadding(dp(2), dp(6), dp(2), dp(6));
            chip.setTag(c);
            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selected.contains(c[0])) selected.remove(c[0]);
                    else selected.add(c[0]);
                    styleChips();
                    refresh();
                }
            });
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams(
                    GridLayout.spec(GridLayout.UNDEFINED),
                    GridLayout.spec(GridLayout.UNDEFINED, 1f));
            lp.width = 0;
            lp.setMargins(dp(3), dp(2), dp(3), dp(2));
            grid.addView(chip, lp);
            chips.add(chip);
        }
        root.addView(grid, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // ---- continent chips ----
        HorizontalScrollView hs = new HorizontalScrollView(this);
        hs.setHorizontalScrollBarEnabled(false);
        LinearLayout contRow = new LinearLayout(this);
        contRow.setOrientation(LinearLayout.HORIZONTAL);
        for (final String[] c : CONTINENTS) {
            TextView chip = new TextView(this);
            chip.setGravity(Gravity.CENTER);
            chip.setTextSize(12);
            chip.setMinWidth(dp(74));
            chip.setPadding(dp(8), dp(5), dp(8), dp(5));
            chip.setTag(c);
            chip.setText(c[1] + "\n" + c[2]);
            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selCont = c[0];
                    styleContChips();
                    refresh();
                }
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(dp(3), dp(4), dp(3), dp(2));
            contRow.addView(chip, lp);
            contChips.add(chip);
        }
        hs.addView(contRow);
        root.addView(hs, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // ---- search box ----
        searchBox = new EditText(this);
        searchBox.setHint("ملک تلاش کریں • Search country / capital / currency");
        searchBox.setTextSize(14);
        searchBox.setSingleLine(true);
        searchBox.setInputType(InputType.TYPE_CLASS_TEXT);
        searchBox.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        searchBox.setPadding(dp(14), dp(8), dp(14), dp(8));
        GradientDrawable sbg = new GradientDrawable();
        sbg.setColor(Color.parseColor("#F1F3F4"));
        sbg.setCornerRadius(dp(22));
        searchBox.setBackground(sbg);
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void afterTextChanged(Editable s) { refresh(); }
        });
        searchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, android.view.KeyEvent e) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return true;
            }
        });
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        slp.setMargins(dp(3), dp(4), dp(3), dp(2));
        root.addView(searchBox, slp);

        // ---- options row ----
        LinearLayout opts = new LinearLayout(this);
        opts.setOrientation(LinearLayout.HORIZONTAL);
        opts.setGravity(Gravity.CENTER_VERTICAL);
        exactBox = new CheckBox(this);
        exactBox.setText("صرف یہی رنگ • Only these colors");
        exactBox.setTextSize(12);
        exactBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });
        opts.addView(exactBox, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        Button clear = new Button(this);
        clear.setText("صاف • Clear");
        clear.setTextSize(11);
        clear.setMinHeight(0);
        clear.setMinimumHeight(0);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selected.clear();
                selCont = "ALL";
                searchBox.setText("");
                exactBox.setChecked(false);
                styleChips();
                styleContChips();
                refresh();
            }
        });
        opts.addView(clear, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, dp(36)));
        root.addView(opts);

        countView = makeText("", 13, true);
        countView.setPadding(dp(4), dp(2), dp(4), dp(2));
        root.addView(countView);

        // ---- list ----
        ListView list = new ListView(this);
        adapter = new BaseAdapter() {
            @Override public int getCount() { return shown.size(); }
            @Override public Object getItem(int i) { return shown.get(i); }
            @Override public long getItemId(int i) { return i; }
            @Override public View getView(int i, View convertView, ViewGroup parent) {
                return buildRow(shown.get(i));
            }
        };
        list.setAdapter(adapter);
        root.addView(list, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        TextView credit = makeText("By: Black Falcon", 12, true);
        credit.setTextColor(Color.parseColor("#666666"));
        credit.setGravity(Gravity.END);
        credit.setPadding(0, dp(4), dp(4), 0);
        root.addView(credit);

        setContentView(root);
        styleChips();
        styleContChips();
        refresh();
    }

    private void loadCountries() {
        Locale ur = new Locale("ur");
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(getAssets().open("countries.txt"), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] p = line.split("\\|");
                if (p.length < 6) continue;
                String code = p[0];
                Country c = new Country();
                c.colors = p[1];
                c.cont = p[2];
                c.lang = p[3];
                c.cap = p[4];
                c.cur = p[5];
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < code.length(); i++) {
                    sb.appendCodePoint(0x1F1E6 + (code.charAt(i) - 'A'));
                }
                c.flag = sb.toString();
                Locale loc = new Locale("", code);
                c.en = loc.getDisplayCountry(Locale.ENGLISH);
                if (c.en == null || c.en.isEmpty()) c.en = code;
                c.ur = loc.getDisplayCountry(ur);
                if (c.ur == null || c.ur.isEmpty() || c.ur.equals(code)) c.ur = c.en;
                c.enL = c.en.toLowerCase(Locale.ROOT);
                c.urL = c.ur.toLowerCase(Locale.ROOT);
                String cn = contName.get(c.cont);
                c.hay = (c.en + " " + c.ur + " " + c.cap + " " + c.cur + " " + c.lang + " "
                        + (cn == null ? "" : cn)).toLowerCase(Locale.ROOT);
                all.add(c);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Collections.sort(all, (a, b) -> a.en.compareTo(b.en));
    }

    private int matchScore(Country c, String q) {
        if (c.enL.startsWith(q) || c.urL.startsWith(q)) return 0;
        if (c.enL.contains(q) || c.urL.contains(q)) return 1;
        return 2;
    }

    private void refresh() {
        final String q = searchBox.getText().toString().trim().toLowerCase(Locale.ROOT);
        boolean exact = exactBox.isChecked();
        shown.clear();
        for (Country c : all) {
            if (!selCont.equals("ALL") && !c.cont.equals(selCont)) continue;
            if (!q.isEmpty() && !c.hay.contains(q)) continue;
            if (!selected.isEmpty()) {
                boolean ok = true;
                for (String s : selected) {
                    if (c.colors.indexOf(s) < 0) { ok = false; break; }
                }
                if (!ok) continue;
                if (exact && c.colors.length() != selected.size()) continue;
            }
            shown.add(c);
        }
        final boolean byColor = !selected.isEmpty();
        Collections.sort(shown, (a, b) -> {
            if (!q.isEmpty()) {
                int sa = matchScore(a, q), sb = matchScore(b, q);
                if (sa != sb) return sa - sb;
            }
            if (byColor && a.colors.length() != b.colors.length()) {
                return a.colors.length() - b.colors.length();
            }
            return a.en.compareTo(b.en);
        });
        countView.setText("نتائج • Results: " + shown.size() + " / " + all.size());
        adapter.notifyDataSetChanged();
    }

    private void styleChips() {
        for (TextView chip : chips) {
            String[] c = (String[]) chip.getTag();
            boolean sel = selected.contains(c[0]);
            GradientDrawable gd = new GradientDrawable();
            gd.setColor(Color.parseColor(c[3]));
            gd.setCornerRadius(dp(10));
            gd.setStroke(dp(sel ? 4 : 1), sel ? Color.BLACK : Color.parseColor("#999999"));
            chip.setBackground(gd);
            chip.setAlpha(sel ? 1f : 0.6f);
            boolean darkText = c[0].equals("W") || c[0].equals("Y") || c[0].equals("O") || c[0].equals("L");
            chip.setTextColor(darkText ? Color.BLACK : Color.WHITE);
            chip.setTypeface(null, sel ? Typeface.BOLD : Typeface.NORMAL);
            chip.setText((sel ? "✓ " : "") + c[1] + "\n" + c[2]);
        }
    }

    private void styleContChips() {
        for (TextView chip : contChips) {
            String[] c = (String[]) chip.getTag();
            boolean sel = selCont.equals(c[0]);
            GradientDrawable gd = new GradientDrawable();
            gd.setColor(sel ? Color.parseColor("#1E3A5F") : Color.parseColor("#ECEFF1"));
            gd.setCornerRadius(dp(18));
            chip.setBackground(gd);
            chip.setTextColor(sel ? Color.WHITE : Color.parseColor("#333333"));
            chip.setTypeface(null, sel ? Typeface.BOLD : Typeface.NORMAL);
        }
    }

    private View buildRow(Country c) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(4), dp(8), dp(4), dp(8));

        TextView flag = new TextView(this);
        flag.setText(c.flag);
        flag.setTextSize(40);
        flag.setPadding(0, 0, dp(12), 0);
        row.addView(flag);

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);

        col.addView(makeText(c.en, 17, true));
        if (!c.ur.equals(c.en)) {
            TextView ur = makeText(c.ur, 15, false);
            ur.setTextColor(Color.parseColor("#555555"));
            col.addView(ur);
        }

        String cn = contName.get(c.cont);
        col.addView(detail("Continent:", cn == null ? c.cont : cn));
        col.addView(detail("Capital:", c.cap));
        col.addView(detail("Language:", c.lang));
        col.addView(detail("Currency:", c.cur));

        LinearLayout dots = new LinearLayout(this);
        dots.setOrientation(LinearLayout.HORIZONTAL);
        dots.setPadding(0, dp(5), 0, 0);
        for (int i = 0; i < c.colors.length(); i++) {
            View d = new View(this);
            GradientDrawable gd = new GradientDrawable();
            gd.setColor(Color.parseColor(hexOf.get(String.valueOf(c.colors.charAt(i)))));
            gd.setCornerRadius(dp(3));
            gd.setStroke(dp(1), Color.parseColor("#888888"));
            d.setBackground(gd);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(16), dp(16));
            lp.rightMargin = dp(4);
            dots.addView(d, lp);
        }
        col.addView(dots);

        row.addView(col, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        return row;
    }

    private TextView detail(String label, String value) {
        SpannableStringBuilder sb = new SpannableStringBuilder(label + " ");
        sb.setSpan(new StyleSpan(Typeface.BOLD), 0, label.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.append(value);
        TextView t = new TextView(this);
        t.setText(sb);
        t.setTextSize(12.5f);
        t.setTextColor(Color.parseColor("#333333"));
        return t;
    }

    private TextView makeText(String s, int sp, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(Color.parseColor("#111111"));
        if (bold) t.setTypeface(null, Typeface.BOLD);
        return t;
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }
}

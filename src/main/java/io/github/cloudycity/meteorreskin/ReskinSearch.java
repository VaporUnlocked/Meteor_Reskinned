// SPDX-License-Identifier: GPL-3.0-only
package io.github.cloudycity.meteorreskin;

import java.text.Normalizer;
import java.util.Locale;

/** Forgiving literal search, not executable regular expressions. */
public final class ReskinSearch {
    private ReskinSearch() {}
    public static String compact(String value) {
        if (value == null) return "";
        String text = Normalizer.normalize(value, Normalizer.Form.NFKD).toLowerCase(Locale.ROOT);
        StringBuilder result = new StringBuilder(text.length());
        text.codePoints().filter(Character::isLetterOrDigit).forEach(result::appendCodePoint);
        return result.toString();
    }
    public static boolean matches(String query, String... fields) {
        if (query == null || query.isBlank()) return true;
        String normalized = compact(query);
        if (normalized.isEmpty()) return false;
        StringBuilder joined = new StringBuilder();
        for (String field : fields) {
            String f = compact(field);
            if (f.contains(normalized)) return true;
            joined.append(' ').append(f);
        }
        // Multi-word queries may be entered out of order; do not join field boundaries.
        String[] terms = query.strip().split("[\\s_-]+");
        for (String term : terms) {
            String token = compact(term);
            if (!token.isEmpty() && joined.indexOf(token) < 0) return false;
        }
        return terms.length > 1;
    }
}

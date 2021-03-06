/*
 * -----------------------------------------------------------------------
 * Copyright © 2013-2015 Meno Hochschild, <http://www.menodata.de/>
 * -----------------------------------------------------------------------
 * This file (SymbolProviderSPI.java) is part of project Time4J.
 *
 * Time4J is free software: You can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * Time4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Time4J. If not, see <http://www.gnu.org/licenses/>.
 * -----------------------------------------------------------------------
 */

package net.time4j.i18n;

import net.time4j.format.NumberSymbolProvider;

import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
 * <p>{@code ServiceProvider}-implementation for accessing localized
 * number symbols. </p>
 *
 * <p>The underlying properties files are located in the folder
 * &quot;numbers&quot; relative to class path and are encoded in UTF-8.
 * The basic bundle name is &quot;symbol&quot;. </p>
 *
 * @author  Meno Hochschild
 */
public final class SymbolProviderSPI
    implements NumberSymbolProvider {

    //~ Konstruktoren -----------------------------------------------------

    /** For {@code java.util.ServiceLoader}. */
    public SymbolProviderSPI() {
        super();

    }

    //~ Methoden ----------------------------------------------------------

    @Override
    public Locale[] getAvailableLocales() {

        return NumberFormat.getAvailableLocales();

    }

    @Override
    public char getZeroDigit(Locale locale) {

        return lookup(
            locale,
            "zero",
            getSymbols(locale).getZeroDigit());

    }

    @Override
    public char getDecimalSeparator(Locale locale) {

        return lookup(
            locale,
            "separator",
            getSymbols(locale).getDecimalSeparator());

    }

    @Override
    public String getMinusSign(Locale locale) {

        return lookup(
            locale,
            "minus",
            String.valueOf(getSymbols(locale).getMinusSign()));

    }

    @Override
    public String toString() {

        return "SymbolProviderSPI";

    }

    private static DecimalFormatSymbols getSymbols(Locale loc) {

        return DecimalFormatSymbols.getInstance(loc);

    }

    private static char lookup(
        Locale locale,
        String key,
        char standard
    ) {

        ResourceBundle rb = getBundle(locale);

        if (
            (rb != null)
            && rb.containsKey(key)
        ) {
            return rb.getString(key).charAt(0);
        }

        return standard;

    }

    private static String lookup(
        Locale locale,
        String key,
        String standard
    ) {

        ResourceBundle rb = getBundle(locale);

        if (
            (rb != null)
            && rb.containsKey(key)
        ) {
            return rb.getString(key);
        }

        return standard;

    }

    private static ResourceBundle getBundle(Locale desired) {

        try {
            return ResourceBundle.getBundle(
                "numbers/symbol",
                desired,
                getLoader(),
                UTF8ResourceControl.SINGLETON);
        } catch (MissingResourceException ex) {
            return null;
        }

    }

	private static ClassLoader getLoader() {

		ClassLoader cl = Thread.currentThread().getContextClassLoader();

		if (cl == null) {
			cl = NumberSymbolProvider.class.getClassLoader();
		}

        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }

		return cl;

	}

}

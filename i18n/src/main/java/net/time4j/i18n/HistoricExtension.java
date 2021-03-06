/*
 * -----------------------------------------------------------------------
 * Copyright © 2013-2015 Meno Hochschild, <http://www.menodata.de/>
 * -----------------------------------------------------------------------
 * This file (HistoricExtension.java) is part of project Time4J.
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

import net.time4j.PlainDate;
import net.time4j.engine.AttributeQuery;
import net.time4j.engine.ChronoElement;
import net.time4j.engine.ChronoEntity;
import net.time4j.engine.ChronoExtension;
import net.time4j.engine.ValidationElement;
import net.time4j.format.Attributes;
import net.time4j.format.Leniency;
import net.time4j.history.ChronoHistory;
import net.time4j.history.HistoricDate;
import net.time4j.history.HistoricEra;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;


/**
 * <p>Defines a historic extension of {@code PlainDate}. </p>
 *
 * @author  Meno Hochschild
 * @since   3.1
 */
public class HistoricExtension
    implements ChronoExtension {

    //~ Methoden ----------------------------------------------------------

    @Override
    public boolean accept(Class<?> chronoType) {

        return (chronoType == PlainDate.class);

    }

    @Override
    public Set<ChronoElement<?>> getElements(
        Locale locale,
        AttributeQuery attributes
    ) {

        if (locale.getCountry().isEmpty()) {
            return Collections.emptySet();
        }

        return getHistory(locale, attributes).getElements();

    }

    @Override
    public <T extends ChronoEntity<T>> T resolve(
        T entity,
        Locale locale,
        AttributeQuery attributes
    ) {

        ChronoHistory history = getHistory(locale, attributes);
        HistoricEra era = null;

        if (entity.contains(history.era())) {
            era = entity.get(history.era());
        } else {
            if (attributes.get(Attributes.LENIENCY, Leniency.SMART).isLax()) {
                era = HistoricEra.AD;
            } else {
                String message = "Parsing of historical dates requires the presence of an era.";
                if (entity.isValid(ValidationElement.ERROR_MESSAGE, message)) {
                    entity.with(ValidationElement.ERROR_MESSAGE, message);
                }
            }
        }

        if (
            (era != null)
            && entity.contains(history.yearOfEra())
            && entity.contains(history.month())
            && entity.contains(history.dayOfMonth())
        ) {
            int yearOfEra = entity.get(history.yearOfEra());
            int month = entity.get(history.month());
            int dayOfMonth = entity.get(history.dayOfMonth());
            HistoricDate hd = HistoricDate.of(era, yearOfEra, month, dayOfMonth);
            PlainDate date = history.convert(hd);
            entity.with(history.era(), null);
            entity.with(history.yearOfEra(), null);
            entity.with(history.month(), null);
            entity.with(history.dayOfMonth(), null);
            return entity.with(PlainDate.COMPONENT, date);
        }

        return entity;

    }

    private static ChronoHistory getHistory(
        Locale locale,
        AttributeQuery attributes
    ) {

        if (attributes.contains(ChronoHistory.ATTRIBUTE_HISTORIC_VARIANT)) {
            switch (attributes.get(ChronoHistory.ATTRIBUTE_HISTORIC_VARIANT)) {
                case INTRODUCTION_ON_1582_10_15:
                    return ChronoHistory.ofFirstGregorianReform();
                case PROLEPTIC_JULIAN:
                    return ChronoHistory.PROLEPTIC_JULIAN;
                case PROLEPTIC_GREGORIAN:
                    return ChronoHistory.PROLEPTIC_GREGORIAN;
                case SWEDEN:
                    return ChronoHistory.ofSweden();
                default:
                    if (attributes.contains(ChronoHistory.ATTRIBUTE_CUTOVER_DATE)) {
                        PlainDate date = attributes.get(ChronoHistory.ATTRIBUTE_CUTOVER_DATE);
                        return ChronoHistory.ofGregorianReform(date);
                    }
            }
        }

        return ChronoHistory.of(locale);

    }

}

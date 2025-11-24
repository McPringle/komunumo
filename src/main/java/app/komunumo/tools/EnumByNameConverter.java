/*
 * Komunumo - Open Source Community Manager
 * Copyright (C) Marcus Fihlon and the individual contributors to Komunumo.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package app.komunumo.tools;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.Converter;

import java.util.Locale;

/**
 * <p>A generic {@link Converter} implementation for mapping between {@link Enum} values
 * and {@code VARCHAR} columns in a database.</p>
 *
 * <p>This abstract base class maps enum values using {@link Enum#name()} for writing
 * to the database and {@link Enum#valueOf(Class, String)} for reading from it.
 * It assumes that the enum names are stored in the database in {@code UPPERCASE},
 * matching the result of {@code Enum.name()}.</p>
 *
 * <h2>Usage</h2>
 *
 * <p>Extend this class to create a type-safe jOOQ converter for a specific enum:</p>
 *
 * <pre>{@code
 * public final class EventStatusConverter extends EnumByNameConverter<EventStatus> {
 *     public EventStatusConverter() {
 *         super(EventStatus.class);
 *     }
 * }
 * }</pre>
 *
 * <p>This is particularly useful for integrating with jOOQ's code generator via {@code <forcedType>}
 * configuration in order to automatically map enum fields in records.</p>
 *
 * @param <T> the enum type being converted
 */
public abstract class EnumByNameConverter<T extends Enum<T>> implements Converter<String, T> {

    private final Class<T> enumClass;

    /**
     * <p>Constructs a new converter for the given enum type.</p>
     *
     * @param enumClass the class of the enum to convert
     */
    protected EnumByNameConverter(final @NotNull Class<T> enumClass) {
        this.enumClass = enumClass;
    }

    /**
     * <p>Converts a database {@code String} value to the corresponding enum constant.</p>
     *
     * <p>Trims and uppercases the input string before using {@link Enum#valueOf(Class, String)}.
     * This allows for case-insensitive and whitespace-tolerant matching.</p>
     *
     * <p>If the input is {@code null} or blank, this method returns {@code null}.
     * If the value does not match any enum constant, an {@link IllegalArgumentException} is thrown.</p>
     *
     * @param databaseValue the string value from the database
     * @return the corresponding enum constant, or {@code null} if the input is {@code null} or blank
     * @throws IllegalArgumentException if the value is not a valid enum name
     * @see Enum#valueOf(Class, String)
     * @see #to(T)
     */
    @Override
    public @Nullable T from(final @Nullable String databaseValue) {
        return databaseValue == null || databaseValue.isBlank() ? null
                : Enum.valueOf(enumClass, databaseValue.trim().toUpperCase(Locale.ROOT));
    }

    /**
     * <p>Converts an enum constant to its {@code String} representation suitable for the database.</p>
     *
     * <p>This implementation returns {@link Enum#name()}, which should match the value
     * stored in the database.</p>
     *
     * @param enumValue the enum constant
     * @return the name of the enum, or {@code null} if the input is {@code null}
     * @see Enum#name()
     * @see #from(String)
     */
    @Override
    public @Nullable String to(final @Nullable T enumValue) {
        return enumValue == null ? null : enumValue.name();
    }

    /**
     * <p>Returns the source type handled by this converter, which is {@link String}.</p>
     *
     * @return the source type class
     * @see #toType()
     */
    @Override
    public @NotNull Class<String> fromType() {
        return String.class;
    }

    /**
     * <p>Returns the target enum type handled by this converter.</p>
     *
     * @return the enum type class
     * @see #fromType()
     */
    @Override
    public @NotNull Class<T> toType() {
        return enumClass;
    }

}

/*
 * MIT License
 *
 * Copyright (c) 2021 Hasan Demirtaş
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.github.portlek.synergy.api;

import io.github.portlek.configs.ConfigHolder;
import io.github.portlek.configs.FieldLoader;
import io.github.portlek.configs.configuration.ConfigurationSection;
import io.github.portlek.configs.loaders.DataSerializer;
import io.github.portlek.configs.loaders.GenericFieldLoader;
import io.github.portlek.configs.loaders.SectionFieldLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * an interface to determine key stores.
 */
public interface KeyStore extends Id, Named, DataSerializer {

  /**
   * deserializes the given section.
   *
   * @param section the section to deserialize.
   *
   * @return key store instance.
   */
  @NotNull
  static Optional<KeyStore> deserialize(@NotNull final ConfigurationSection section) {
    final var id = section.getString("id");
    final var name = section.getString("name");
    final var password = section.getString("password");
    if (id == null || name == null || password == null) {
      return Optional.empty();
    }
    return Optional.of(new Impl(id, name, password));
  }

  /**
   * obtains the password.
   *
   * @return password.
   */
  @NotNull
  String getPassword();

  @Override
  default void serialize(@NotNull final ConfigurationSection section) {
    section.set("id", this.getId());
    section.set("name", this.getName());
    section.set("password", this.getPassword());
  }

  /**
   * a simple implementation of {@link KeyStore}.
   */
  @Getter
  @ToString
  @EqualsAndHashCode
  @RequiredArgsConstructor
  final class Impl implements KeyStore {

    /**
     * the id.
     */
    @NotNull
    private final String id;

    /**
     * the name.
     */
    @NotNull
    private final String name;

    /**
     * the password.
     */
    @NotNull
    private final String password;
  }

  /**
   * a class that loads the key store.
   */
  final class Loader extends SectionFieldLoader<KeyStore> {

    /**
     * the instance.
     */
    public static final FieldLoader.Func INSTANCE = Loader::new;

    /**
     * ctor.
     *
     * @param holder the holder.
     * @param section the section.
     */
    private Loader(@NotNull final ConfigHolder holder, @NotNull final ConfigurationSection section) {
      super(holder, section, KeyStore.class);
    }

    @NotNull
    @Override
    public Optional<KeyStore> toFinal(@NotNull final ConfigurationSection section, @NotNull final String path,
                                      @Nullable final KeyStore fieldValue) {
      return KeyStore.deserialize(section.getSectionOrCreate(path));
    }
  }

  /**
   * an interface to determine pool of key stores.
   */
  @ToString
  @EqualsAndHashCode
  @RequiredArgsConstructor
  final class Pool {

    /**
     * the key stores.
     */
    @NotNull
    @Getter
    private final List<Impl> keyStores;

    /**
     * ctor.
     *
     * @param keyStores the key stores.
     */
    public Pool(@NotNull final Impl... keyStores) {
      this(List.of(keyStores));
    }

    /**
     * a class that represents field loader of {@link Pool}.
     */
    public static final class Loader extends GenericFieldLoader<List<Map<String, Object>>, Pool> {

      /**
       * the instance.
       */
      public static final Func INSTANCE = Loader::new;

      /**
       * ctor.
       *
       * @param holder the holder.
       * @param section the section.
       */
      private Loader(@NotNull final ConfigHolder holder, @NotNull final ConfigurationSection section) {
        super(holder, section, Pool.class);
      }

      @NotNull
      @Override
      public Optional<List<Map<String, Object>>> toConfigObject(@NotNull final ConfigurationSection section,
                                                                @NotNull final String path) {
        return Optional.of(section.getMapList(path).stream()
          .map(map -> {
            final var finalMap = new HashMap<String, Object>();
            map.forEach((o, o2) ->
              finalMap.put(String.valueOf(o), o2));
            return finalMap;
          })
          .collect(Collectors.toList()));
      }

      @NotNull
      @Override
      public Optional<Pool> toFinal(@NotNull final List<Map<String, Object>> rawValue,
                                    @Nullable final Pool fieldValue) {
        return Optional.of(new Pool(rawValue.stream()
          .map(map -> new Impl(
            (String) map.get("id"),
            (String) map.get("name"),
            (String) map.get("password")))
          .collect(Collectors.toList())));
      }

      @NotNull
      @Override
      public Optional<List<Map<String, Object>>> toRaw(@NotNull final Pool finalValue) {
        return Optional.of(finalValue.getKeyStores().stream()
          .map(keyStore -> Map.<String, Object>of(
            "id", keyStore.getId(),
            "name", keyStore.getName(),
            "password", keyStore.getPassword()))
          .collect(Collectors.toList()));
      }
    }
  }
}

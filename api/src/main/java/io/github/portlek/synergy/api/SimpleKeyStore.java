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

import io.github.portlek.configs.configuration.ConfigurationSection;
import io.github.portlek.configs.loaders.DataSerializer;
import io.github.portlek.configs.loaders.SectionFieldLoader;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a simple implementation of {@link KeyStore}.
 */
@Getter
@RequiredArgsConstructor
public final class SimpleKeyStore implements KeyStore, DataSerializer {

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

  /**
   * deserializes the given section.
   *
   * @param section the section to deserialize.
   *
   * @return key store instance.
   */
  @NotNull
  public static Optional<KeyStore> deserialize(@NotNull final ConfigurationSection section) {
    final var id = section.getString("id");
    final var name = section.getString("name");
    final var password = section.getString("password");
    if (id == null || name == null || password == null) {
      return Optional.empty();
    }
    return Optional.of(new SimpleKeyStore(id, name, password));
  }

  @Override
  public void serialize(@NotNull final ConfigurationSection section) {
    section.set("id", this.id);
    section.set("name", this.name);
    section.set("password", this.password);
  }

  /**
   * a class that loads the key store.
   */
  public static final class Loader extends SectionFieldLoader<KeyStore> {

    /**
     * the instance.
     */
    public static final Supplier<Loader> INSTANCE = Loader::new;

    /**
     * ctor.
     */
    private Loader() {
      super(KeyStore.class);
    }

    @NotNull
    @Override
    public Optional<KeyStore> toFinal(@NotNull final ConfigurationSection section,
                                      @Nullable final KeyStore fieldValue) {
      return SimpleKeyStore.deserialize(section);
    }
  }
}

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

import java.io.File;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

/**
 * an interface to determine package context.
 */
public interface PackageContext {

  /**
   * obtains the dependency chain.
   *
   * @return dependency chain.
   */
  @NotNull
  List<Package> getDependencyChain();

  /**
   * obtains the destination.
   *
   * @return destination.
   */
  @NotNull
  File getDestination();

  /**
   * obtains the package manager.
   *
   * @return package manager.
   */
  @NotNull
  PackageManager getPackageManager();

  /**
   * obtains the properties.
   *
   * @return properties.
   */
  @NotNull
  Map<String, String> getProperties();

  /**
   * obtains the resources.
   *
   * @return resources.
   */
  @NotNull
  Map<String, Integer> getResources();

  /**
   * obtains the user.
   *
   * @return user.
   */
  @NotNull
  Object getUser();
}

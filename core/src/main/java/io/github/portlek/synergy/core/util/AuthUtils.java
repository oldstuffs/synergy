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

package io.github.portlek.synergy.core.util;

import io.github.portlek.synergy.proto.Protocol;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;
import org.apache.commons.codec.binary.Hex;
import org.jasypt.encryption.pbe.StandardPBEByteEncryptor;
import org.jetbrains.annotations.NotNull;

/**
 * a class that contains utility methods for authentication.
 */
public final class AuthUtils {

  /**
   * ctor.
   */
  private AuthUtils() {
  }

  /**
   * creates hash.
   *
   * @param key the key to create.
   * @param message the message to create.
   *
   * @return a newly created hash.
   */
  @NotNull
  public static String createHash(@NotNull final String key, final byte @NotNull [] message) {
    final MessageDigest digest;
    try {
      digest = MessageDigest.getInstance("SHA-1");
    } catch (final NoSuchAlgorithmException e) {
      throw new AssertionError(e);
    }
    digest.update(message);
    digest.update(key.getBytes(StandardCharsets.UTF_8));
    return Hex.encodeHexString(digest.digest());
  }

  /**
   * creates package checksum.
   *
   * @param filePath the file path to create.
   *
   * @return a newly created package checksum.
   *
   * @throws IOException if an I/O error occurs.
   */
  @NotNull
  public static String createPackageChecksum(@NotNull final String filePath) throws IOException {
    final var path = Path.of(filePath);
    try (final var is = new CheckedInputStream(Files.newInputStream(path), new Adler32())) {
      var buf = new byte[1024 * 1024];
      var total = 0;
      var c = 0;
      while (total < 100 * 1024 * 1024 && (c = is.read(buf)) >= 0) {
        total += c;
      }
      final var bb = ByteBuffer.allocate(Long.BYTES);
      bb.putLong(path.toFile().length());
      buf = bb.array();
      is.getChecksum().update(buf, 0, buf.length);
      return Long.toHexString(is.getChecksum().getValue());
    }
  }

  /**
   * decrypts the given bytes with the key.
   *
   * @param bytes the bytes to decrypt.
   * @param key the key to decrypt.
   *
   * @return decrypted byte array.
   */
  public static byte @NotNull [] decrypt(final byte @NotNull [] bytes, @NotNull final String key) {
    return AuthUtils.getEncryptor(key).decrypt(bytes);
  }

  /**
   * encrypts the given bytes with the key.
   *
   * @param bytes the bytes to encrypt.
   * @param key the key to encrypt.
   *
   * @return encrypted byte array.
   */
  public static byte @NotNull [] encrypt(final byte @NotNull [] bytes, @NotNull final String key) {
    return AuthUtils.getEncryptor(key).encrypt(bytes);
  }

  /**
   * validates the given hash.
   *
   * @param hash the hash to validate.
   * @param key the key to validate.
   * @param message the message to validate.
   *
   * @return {@code true} if the hash is valid.
   */
  public static boolean validateHash(@NotNull final String hash, @NotNull final String key,
                                     @NotNull final String message) {
    return AuthUtils.validateHash(hash, key, message.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * validates the given hash.
   *
   * @param hash the hash to validate.
   * @param key the key to validate.
   * @param message the message to validate.
   *
   * @return {@code true} if the hash is valid.
   */
  public static boolean validateHash(@NotNull final String hash, @NotNull final String key,
                                     final byte @NotNull [] message) {
    return hash.equals(AuthUtils.createHash(key, message));
  }

  /**
   * validates the given payload.
   *
   * @param payload the payload to validate.
   * @param key the key to validate.
   *
   * @return {@code true} if the hash is valid.
   */
  public static boolean validateHash(@NotNull final Protocol.AuthenticatedMessage payload, @NotNull final String key) {
    return AuthUtils.validateHash(payload.getHash(), key, payload.getPayload().toByteArray());
  }

  /**
   * obtains a new encryptor.
   *
   * @param key the key to get.
   *
   * @return a newly created encryptor.
   */
  @NotNull
  private static StandardPBEByteEncryptor getEncryptor(@NotNull final String key) {
    final var encryptor = new StandardPBEByteEncryptor();
    encryptor.setPassword(key);
    encryptor.setAlgorithm("PBEWithSHA1AndRC4_128");
    encryptor.setKeyObtentionIterations(4000);
    return encryptor;
  }
}

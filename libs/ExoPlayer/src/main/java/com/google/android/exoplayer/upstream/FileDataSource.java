/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.exoplayer.upstream;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * A local file {@link DataSource}.
 */
public final class FileDataSource implements DataSource {

  /**
   * Thrown when IOException is encountered during local file read operation.
   */
  public static class FileDataSourceException extends IOException {

    public FileDataSourceException(IOException cause) {
      super(cause);
    }

  }

  private RandomAccessFile file;
  private long bytesRemaining;

  @Override
  public long open(DataSpec dataSpec) throws FileDataSourceException {
    try {
      file = new RandomAccessFile(dataSpec.uri.getPath(), "r");
      file.seek(dataSpec.position);
      bytesRemaining = dataSpec.length == DataSpec.LENGTH_UNBOUNDED
          ? file.length() - dataSpec.position
          : dataSpec.length;
      return bytesRemaining;
    } catch (IOException e) {
      throw new FileDataSourceException(e);
    }
  }

  @Override
  public int read(byte[] buffer, int offset, int readLength) throws FileDataSourceException {
    if (bytesRemaining == 0) {
      return -1;
    } else {
      int bytesRead = 0;
      try {
        bytesRead = file.read(buffer, offset, (int) Math.min(bytesRemaining, readLength));
      } catch (IOException e) {
        throw new FileDataSourceException(e);
      }
      bytesRemaining -= bytesRead;
      return bytesRead;
    }
  }

  @Override
  public void close() throws FileDataSourceException {
    if (file != null) {
      try {
        file.close();
      } catch (IOException e) {
        throw new FileDataSourceException(e);
      }
      file = null;
    }
  }

}

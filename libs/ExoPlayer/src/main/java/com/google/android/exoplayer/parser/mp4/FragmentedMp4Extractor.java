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
package com.google.android.exoplayer.parser.mp4;

import com.google.android.exoplayer.MediaFormat;
import com.google.android.exoplayer.ParserException;
import com.google.android.exoplayer.SampleHolder;
import com.google.android.exoplayer.parser.SegmentIndex;
import com.google.android.exoplayer.parser.mp4.Atom.ContainerAtom;
import com.google.android.exoplayer.parser.mp4.Atom.LeafAtom;
import com.google.android.exoplayer.upstream.NonBlockingInputStream;
import com.google.android.exoplayer.util.Assertions;
import com.google.android.exoplayer.util.MimeTypes;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.util.Pair;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

/**
 * Facilitates the extraction of data from the fragmented mp4 container format.
 * <p>
 * This implementation only supports de-muxed (i.e. single track) streams.
 */
public final class FragmentedMp4Extractor {

  /**
   * An attempt to read from the input stream returned 0 bytes of data.
   */
  public static final int RESULT_NEED_MORE_DATA = 1;
  /**
   * The end of the input stream was reached.
   */
  public static final int RESULT_END_OF_STREAM = 2;
  /**
   * A media sample was read.
   */
  public static final int RESULT_READ_SAMPLE_FULL = 4;
  /**
   * A media sample was partially read.
   */
  public static final int RESULT_READ_SAMPLE_PARTIAL = 8;
  /**
   * A moov atom was read. The parsed data can be read using {@link #getTrack()},
   * {@link #getFormat()} and {@link #getPsshInfo}.
   */
  public static final int RESULT_READ_MOOV = 16;
  /**
   * A sidx atom was read. The parsed data can be read using {@link #getSegmentIndex()}.
   */
  public static final int RESULT_READ_SIDX = 32;

  private static final int READ_TERMINATING_RESULTS = RESULT_NEED_MORE_DATA | RESULT_END_OF_STREAM
      | RESULT_READ_SAMPLE_FULL;
  private static final byte[] NAL_START_CODE = new byte[] {0, 0, 0, 1};
  private static final byte[] PIFF_SAMPLE_ENCRYPTION_BOX_EXTENDED_TYPE =
      new byte[] {-94, 57, 79, 82, 90, -101, 79, 20, -94, 68, 108, 66, 124, 100, -115, -12};

  // Parser states
  private static final int STATE_READING_ATOM_HEADER = 0;
  private static final int STATE_READING_ATOM_PAYLOAD = 1;
  private static final int STATE_READING_CENC_AUXILIARY_DATA = 2;
  private static final int STATE_READING_SAMPLE_START = 3;
  private static final int STATE_READING_SAMPLE_INCREMENTAL = 4;

  // Atom data offsets
  private static final int ATOM_HEADER_SIZE = 8;
  private static final int FULL_ATOM_HEADER_SIZE = 12;

  // Atoms that the parser cares about
  private static final Set<Integer> PARSED_ATOMS;
  static {
    HashSet<Integer> parsedAtoms = new HashSet<Integer>();
    parsedAtoms.add(Atom.TYPE_avc1);
    parsedAtoms.add(Atom.TYPE_esds);
    parsedAtoms.add(Atom.TYPE_hdlr);
    parsedAtoms.add(Atom.TYPE_mdat);
    parsedAtoms.add(Atom.TYPE_mdhd);
    parsedAtoms.add(Atom.TYPE_mfhd);
    parsedAtoms.add(Atom.TYPE_moof);
    parsedAtoms.add(Atom.TYPE_moov);
    parsedAtoms.add(Atom.TYPE_mp4a);
    parsedAtoms.add(Atom.TYPE_sidx);
    parsedAtoms.add(Atom.TYPE_stsd);
    parsedAtoms.add(Atom.TYPE_tfdt);
    parsedAtoms.add(Atom.TYPE_tfhd);
    parsedAtoms.add(Atom.TYPE_tkhd);
    parsedAtoms.add(Atom.TYPE_traf);
    parsedAtoms.add(Atom.TYPE_trak);
    parsedAtoms.add(Atom.TYPE_trex);
    parsedAtoms.add(Atom.TYPE_trun);
    parsedAtoms.add(Atom.TYPE_mvex);
    parsedAtoms.add(Atom.TYPE_mdia);
    parsedAtoms.add(Atom.TYPE_minf);
    parsedAtoms.add(Atom.TYPE_stbl);
    parsedAtoms.add(Atom.TYPE_pssh);
    parsedAtoms.add(Atom.TYPE_saiz);
    parsedAtoms.add(Atom.TYPE_uuid);
    PARSED_ATOMS = Collections.unmodifiableSet(parsedAtoms);
  }

  // Atoms that the parser considers to be containers
  private static final Set<Integer> CONTAINER_TYPES;
  static {
    HashSet<Integer> atomContainerTypes = new HashSet<Integer>();
    atomContainerTypes.add(Atom.TYPE_moov);
    atomContainerTypes.add(Atom.TYPE_trak);
    atomContainerTypes.add(Atom.TYPE_mdia);
    atomContainerTypes.add(Atom.TYPE_minf);
    atomContainerTypes.add(Atom.TYPE_stbl);
    atomContainerTypes.add(Atom.TYPE_avcC);
    atomContainerTypes.add(Atom.TYPE_moof);
    atomContainerTypes.add(Atom.TYPE_traf);
    atomContainerTypes.add(Atom.TYPE_mvex);
    CONTAINER_TYPES = Collections.unmodifiableSet(atomContainerTypes);
  }

  private final boolean enableSmoothStreamingWorkarounds;

  // Parser state
  private final ParsableByteArray atomHeader;
  private final Stack<ContainerAtom> containerAtoms;
  private final Stack<Integer> containerAtomEndPoints;

  private int parserState;
  private int atomBytesRead;
  private int rootAtomBytesRead;
  private int atomType;
  private int atomSize;
  private ParsableByteArray atomData;
  private ParsableByteArray cencAuxiliaryData;
  private int cencAuxiliaryBytesRead;
  private int sampleBytesRead;

  private int pendingSeekTimeMs;
  private int sampleIndex;
  private int pendingSeekSyncSampleIndex;
  private int lastSyncSampleIndex;

  // Data parsed from moov and sidx atoms
  private final HashMap<UUID, byte[]> psshData;
  private SegmentIndex segmentIndex;
  private Track track;
  private DefaultSampleValues extendsDefaults;

  // Data parsed from the most recent moof atom
  private TrackFragment fragmentRun;

  public FragmentedMp4Extractor() {
    this(false);
  }

  /**
   * @param enableSmoothStreamingWorkarounds Set to true if this extractor will be used to parse
   *     SmoothStreaming streams. This will enable workarounds for SmoothStreaming violations of
   *     the ISO base media file format (ISO 14496-12). Set to false otherwise.
   */
  public FragmentedMp4Extractor(boolean enableSmoothStreamingWorkarounds) {
    this.enableSmoothStreamingWorkarounds = enableSmoothStreamingWorkarounds;
    parserState = STATE_READING_ATOM_HEADER;
    atomHeader = new ParsableByteArray(ATOM_HEADER_SIZE);
    containerAtoms = new Stack<ContainerAtom>();
    containerAtomEndPoints = new Stack<Integer>();
    psshData = new HashMap<UUID, byte[]>();
  }

  /**
   * Returns the segment index parsed from the stream.
   *
   * @return The segment index, or null if a SIDX atom has yet to be parsed.
   */
  public SegmentIndex getSegmentIndex() {
    return segmentIndex;
  }

  /**
   * Returns the pssh information parsed from the stream.
   *
   * @return The pssh information. May be null if the MOOV atom has yet to be parsed of if it did
   *     not contain any pssh information.
   */
  public Map<UUID, byte[]> getPsshInfo() {
    return psshData.isEmpty() ? null : psshData;
  }

  /**
   * Sideloads pssh information into the extractor, so that it can be read through
   * {@link #getPsshInfo()}.
   *
   * @param uuid The UUID of the scheme for which information is being sideloaded.
   * @param data The corresponding data.
   */
  public void putPsshInfo(UUID uuid, byte[] data) {
    // TODO: This is for SmoothStreaming. Consider using something other than
    // FragmentedMp4Extractor.getPsshInfo to obtain the pssh data for that use case, so that we can
    // remove this method.
    psshData.put(uuid, data);
  }

  /**
   * Returns the format of the samples contained within the media stream.
   *
   * @return The sample media format, or null if a MOOV atom has yet to be parsed.
   */
  public MediaFormat getFormat() {
    return track == null ? null : track.mediaFormat;
  }

  /**
   * Returns the track information parsed from the stream.
   *
   * @return The track, or null if a MOOV atom has yet to be parsed.
   */
  public Track getTrack() {
    return track;
  }

  /**
   * Sideloads track information into the extractor, so that it can be read through
   * {@link #getTrack()}.
   *
   * @param track The track to sideload.
   */
  public void setTrack(Track track) {
    this.extendsDefaults = new DefaultSampleValues(0, 0, 0, 0);
    this.track = track;
  }

  /**
   * Consumes data from a {@link NonBlockingInputStream}.
   * <p>
   * The read terminates if the end of the input stream is reached, if an attempt to read from the
   * input stream returned 0 bytes of data, or if a sample is read. The returned flags indicate
   * both the reason for termination and data that was parsed during the read.
   * <p>
   * If the returned flags include {@link #RESULT_READ_SAMPLE_PARTIAL} then the sample has been
   * partially read into {@code out}. Hence the same {@link SampleHolder} instance must be passed
   * in subsequent calls until the whole sample has been read.
   *
   * @param inputStream The input stream from which data should be read.
   * @param out A {@link SampleHolder} into which the sample should be read.
   * @return One or more of the {@code RESULT_*} flags defined in this class.
   * @throws ParserException If an error occurs parsing the media data.
   */
  public int read(NonBlockingInputStream inputStream, SampleHolder out)
      throws ParserException {
    try {
      int results = 0;
      while ((results & READ_TERMINATING_RESULTS) == 0) {
        switch (parserState) {
          case STATE_READING_ATOM_HEADER:
            results |= readAtomHeader(inputStream);
            break;
          case STATE_READING_ATOM_PAYLOAD:
            results |= readAtomPayload(inputStream);
            break;
          case STATE_READING_CENC_AUXILIARY_DATA:
            results |= readCencAuxiliaryData(inputStream);
            break;
          default:
              results |= readOrSkipSample(inputStream, out);
            break;
        }
      }
      return results;
    } catch (Exception e) {
      throw new ParserException(e);
    }
  }

  /**
   * Seeks to a position before or equal to the requested time.
   *
   * @param seekTimeUs The desired seek time in microseconds.
   * @param allowNoop Allow the seek operation to do nothing if the seek time is in the current
   *     fragment run, is equal to or greater than the time of the current sample, and if there
   *     does not exist a sync frame between these two times.
   * @return True if the operation resulted in a change of state. False if it was a no-op.
   */
  public boolean seekTo(long seekTimeUs, boolean allowNoop) {
    pendingSeekTimeMs = (int) (seekTimeUs / 1000);
    if (allowNoop && fragmentRun != null
        && pendingSeekTimeMs >= fragmentRun.getSamplePresentationTime(0)
        && pendingSeekTimeMs <= fragmentRun.getSamplePresentationTime(fragmentRun.length - 1)) {
      int sampleIndexFound = 0;
      int syncSampleIndexFound = 0;
      for (int i = 0; i < fragmentRun.length; i++) {
        if (fragmentRun.getSamplePresentationTime(i) <= pendingSeekTimeMs) {
          if (fragmentRun.sampleIsSyncFrameTable[i]) {
            syncSampleIndexFound = i;
          }
          sampleIndexFound = i;
        }
      }
      if (syncSampleIndexFound == lastSyncSampleIndex && sampleIndexFound >= sampleIndex) {
        pendingSeekTimeMs = 0;
        return false;
      }
    }
    containerAtoms.clear();
    containerAtomEndPoints.clear();
    enterState(STATE_READING_ATOM_HEADER);
    return true;
  }

  private void enterState(int state) {
    switch (state) {
      case STATE_READING_ATOM_HEADER:
        atomBytesRead = 0;
        if (containerAtomEndPoints.isEmpty()) {
          rootAtomBytesRead = 0;
        }
        break;
      case STATE_READING_CENC_AUXILIARY_DATA:
        cencAuxiliaryBytesRead = 0;
        break;
      case STATE_READING_SAMPLE_START:
        sampleBytesRead = 0;
        break;
    }
    parserState = state;
  }

  private int readAtomHeader(NonBlockingInputStream inputStream) {
    int remainingBytes = ATOM_HEADER_SIZE - atomBytesRead;
    int bytesRead = inputStream.read(atomHeader.getData(), atomBytesRead, remainingBytes);
    if (bytesRead == -1) {
      return RESULT_END_OF_STREAM;
    }
    rootAtomBytesRead += bytesRead;
    atomBytesRead += bytesRead;
    if (atomBytesRead != ATOM_HEADER_SIZE) {
      return RESULT_NEED_MORE_DATA;
    }

    atomHeader.setPosition(0);
    atomSize = atomHeader.readInt();
    atomType = atomHeader.readInt();

    if (atomType == Atom.TYPE_mdat) {
      int cencAuxSize = fragmentRun.auxiliarySampleInfoTotalSize;
      if (cencAuxSize > 0) {
        cencAuxiliaryData = new ParsableByteArray(cencAuxSize);
        enterState(STATE_READING_CENC_AUXILIARY_DATA);
      } else {
        cencAuxiliaryData = null;
        enterState(STATE_READING_SAMPLE_START);
      }
      return 0;
    }

    if (PARSED_ATOMS.contains(atomType)) {
      if (CONTAINER_TYPES.contains(atomType)) {
        enterState(STATE_READING_ATOM_HEADER);
        containerAtoms.add(new ContainerAtom(atomType));
        containerAtomEndPoints.add(rootAtomBytesRead + atomSize - ATOM_HEADER_SIZE);
      } else {
        atomData = new ParsableByteArray(atomSize);
        System.arraycopy(atomHeader.getData(), 0, atomData.getData(), 0, ATOM_HEADER_SIZE);
        enterState(STATE_READING_ATOM_PAYLOAD);
      }
    } else {
      atomData = null;
      enterState(STATE_READING_ATOM_PAYLOAD);
    }

    return 0;
  }

  private int readAtomPayload(NonBlockingInputStream inputStream) {
    int bytesRead;
    if (atomData != null) {
      bytesRead = inputStream.read(atomData.getData(), atomBytesRead, atomSize - atomBytesRead);
    } else {
      bytesRead = inputStream.skip(atomSize - atomBytesRead);
    }
    if (bytesRead == -1) {
      return RESULT_END_OF_STREAM;
    }
    rootAtomBytesRead += bytesRead;
    atomBytesRead += bytesRead;
    if (atomBytesRead != atomSize) {
      return RESULT_NEED_MORE_DATA;
    }

    int results = 0;
    if (atomData != null) {
      results |= onLeafAtomRead(new LeafAtom(atomType, atomData));
    }

    while (!containerAtomEndPoints.isEmpty()
        && containerAtomEndPoints.peek() == rootAtomBytesRead) {
      containerAtomEndPoints.pop();
      results |= onContainerAtomRead(containerAtoms.pop());
    }

    enterState(STATE_READING_ATOM_HEADER);
    return results;
  }

  private int onLeafAtomRead(LeafAtom leaf) {
    if (!containerAtoms.isEmpty()) {
      containerAtoms.peek().add(leaf);
    } else if (leaf.type == Atom.TYPE_sidx) {
      segmentIndex = parseSidx(leaf.getData());
      return RESULT_READ_SIDX;
    }
    return 0;
  }

  private int onContainerAtomRead(ContainerAtom container) {
    if (container.type == Atom.TYPE_moov) {
      onMoovContainerAtomRead(container);
      return RESULT_READ_MOOV;
    } else if (container.type == Atom.TYPE_moof) {
      onMoofContainerAtomRead(container);
    } else if (!containerAtoms.isEmpty()) {
      containerAtoms.peek().add(container);
    }
    return 0;
  }

  private void onMoovContainerAtomRead(ContainerAtom moov) {
    List<Atom> moovChildren = moov.getChildren();
    for (int i = 0; i < moovChildren.size(); i++) {
      Atom child = moovChildren.get(i);
      if (child.type == Atom.TYPE_pssh) {
        ParsableByteArray psshAtom = ((LeafAtom) child).getData();
        psshAtom.setPosition(FULL_ATOM_HEADER_SIZE);
        UUID uuid = new UUID(psshAtom.readLong(), psshAtom.readLong());
        int dataSize = psshAtom.readInt();
        byte[] data = new byte[dataSize];
        psshAtom.readBytes(data, 0, dataSize);
        psshData.put(uuid, data);
      }
    }
    ContainerAtom mvex = moov.getContainerAtomOfType(Atom.TYPE_mvex);
    extendsDefaults = parseTrex(mvex.getLeafAtomOfType(Atom.TYPE_trex).getData());
    track = parseTrak(moov.getContainerAtomOfType(Atom.TYPE_trak));
  }

  private void onMoofContainerAtomRead(ContainerAtom moof) {
    fragmentRun = new TrackFragment();
    parseMoof(track, extendsDefaults, moof, fragmentRun, enableSmoothStreamingWorkarounds);
    sampleIndex = 0;
    lastSyncSampleIndex = 0;
    pendingSeekSyncSampleIndex = 0;
    if (pendingSeekTimeMs != 0) {
      for (int i = 0; i < fragmentRun.length; i++) {
        if (fragmentRun.sampleIsSyncFrameTable[i]) {
          if (fragmentRun.getSamplePresentationTime(i) <= pendingSeekTimeMs) {
            pendingSeekSyncSampleIndex = i;
          }
        }
      }
      pendingSeekTimeMs = 0;
    }
  }

  /**
   * Parses a trex atom (defined in 14496-12).
   */
  private static DefaultSampleValues parseTrex(ParsableByteArray trex) {
    trex.setPosition(FULL_ATOM_HEADER_SIZE + 4);
    int defaultSampleDescriptionIndex = trex.readUnsignedIntToInt() - 1;
    int defaultSampleDuration = trex.readUnsignedIntToInt();
    int defaultSampleSize = trex.readUnsignedIntToInt();
    int defaultSampleFlags = trex.readInt();
    return new DefaultSampleValues(defaultSampleDescriptionIndex, defaultSampleDuration,
        defaultSampleSize, defaultSampleFlags);
  }

  /**
   * Parses a trak atom (defined in 14496-12).
   */
  private static Track parseTrak(ContainerAtom trak) {
    ContainerAtom mdia = trak.getContainerAtomOfType(Atom.TYPE_mdia);
    int trackType = parseHdlr(mdia.getLeafAtomOfType(Atom.TYPE_hdlr).getData());
    Assertions.checkState(trackType == Track.TYPE_AUDIO || trackType == Track.TYPE_VIDEO);

    Pair<Integer, Long> header = parseTkhd(trak.getLeafAtomOfType(Atom.TYPE_tkhd).getData());
    int id = header.first;
    // TODO: This value should be used to set a duration field on the Track object
    // instantiated below, however we've found examples where the value is 0. Revisit whether we
    // should set it anyway (and just have it be wrong for bad media streams).
    // long duration = header.second;
    long timescale = parseMdhd(mdia.getLeafAtomOfType(Atom.TYPE_mdhd).getData());
    ContainerAtom stbl = mdia.getContainerAtomOfType(Atom.TYPE_minf)
        .getContainerAtomOfType(Atom.TYPE_stbl);

    Pair<MediaFormat, TrackEncryptionBox[]> sampleDescriptions =
        parseStsd(stbl.getLeafAtomOfType(Atom.TYPE_stsd).getData());
    return new Track(id, trackType, timescale, sampleDescriptions.first, sampleDescriptions.second);
  }

  /**
   * Parses a tkhd atom (defined in 14496-12).
   *
   * @return A {@link Pair} consisting of the track id and duration.
   */
  private static Pair<Integer, Long> parseTkhd(ParsableByteArray tkhd) {
    tkhd.setPosition(ATOM_HEADER_SIZE);
    int fullAtom = tkhd.readInt();
    int version = parseFullAtomVersion(fullAtom);

    tkhd.skip(version == 0 ? 8 : 16);

    int trackId = tkhd.readInt();
    tkhd.skip(4);
    long duration = version == 0 ? tkhd.readUnsignedInt() : tkhd.readUnsignedLongToLong();

    return Pair.create(trackId, duration);
  }

  /**
   * Parses an hdlr atom (defined in 14496-12).
   *
   * @param hdlr The hdlr atom to parse.
   * @return The track type.
   */
  private static int parseHdlr(ParsableByteArray hdlr) {
    hdlr.setPosition(FULL_ATOM_HEADER_SIZE + 4);
    return hdlr.readInt();
  }

  /**
   * Parses an mdhd atom (defined in 14496-12).
   *
   * @param mdhd The mdhd atom to parse.
   * @return The media timescale, defined as the number of time units that pass in one second.
   */
  private static long parseMdhd(ParsableByteArray mdhd) {
    mdhd.setPosition(ATOM_HEADER_SIZE);
    int fullAtom = mdhd.readInt();
    int version = parseFullAtomVersion(fullAtom);

    mdhd.skip(version == 0 ? 8 : 16);
    return mdhd.readUnsignedInt();
  }

  private static Pair<MediaFormat, TrackEncryptionBox[]> parseStsd(ParsableByteArray stsd) {
    stsd.setPosition(FULL_ATOM_HEADER_SIZE);
    int numberOfEntries = stsd.readInt();
    MediaFormat mediaFormat = null;
    TrackEncryptionBox[] trackEncryptionBoxes = new TrackEncryptionBox[numberOfEntries];
    for (int i = 0; i < numberOfEntries; i++) {
      int childStartPosition = stsd.getPosition();
      int childAtomSize = stsd.readInt();
      int childAtomType = stsd.readInt();
      if (childAtomType == Atom.TYPE_avc1 || childAtomType == Atom.TYPE_encv) {
        Pair<MediaFormat, TrackEncryptionBox> avc1 =
            parseAvc1FromParent(stsd, childStartPosition, childAtomSize);
        mediaFormat = avc1.first;
        trackEncryptionBoxes[i] = avc1.second;
      } else if (childAtomType == Atom.TYPE_mp4a || childAtomType == Atom.TYPE_enca) {
        Pair<MediaFormat, TrackEncryptionBox> mp4a =
            parseMp4aFromParent(stsd, childStartPosition, childAtomSize);
        mediaFormat = mp4a.first;
        trackEncryptionBoxes[i] = mp4a.second;
      }
      stsd.setPosition(childStartPosition + childAtomSize);
    }
    return Pair.create(mediaFormat, trackEncryptionBoxes);
  }

  private static Pair<MediaFormat, TrackEncryptionBox> parseAvc1FromParent(ParsableByteArray parent,
      int position, int size) {
    parent.setPosition(position + ATOM_HEADER_SIZE);

    parent.skip(24);
    int width = parent.readUnsignedShort();
    int height = parent.readUnsignedShort();
    parent.skip(50);

    List<byte[]> initializationData = null;
    TrackEncryptionBox trackEncryptionBox = null;
    int childPosition = parent.getPosition();
    while (childPosition - position < size) {
      parent.setPosition(childPosition);
      int childStartPosition = parent.getPosition();
      int childAtomSize = parent.readInt();
      int childAtomType = parent.readInt();
      if (childAtomType == Atom.TYPE_avcC) {
        initializationData = parseAvcCFromParent(parent, childStartPosition);
      } else if (childAtomType == Atom.TYPE_sinf) {
        trackEncryptionBox = parseSinfFromParent(parent, childStartPosition, childAtomSize);
      }
      childPosition += childAtomSize;
    }

    MediaFormat format = MediaFormat.createVideoFormat(MimeTypes.VIDEO_H264, MediaFormat.NO_VALUE,
        width, height, initializationData);
    return Pair.create(format, trackEncryptionBox);
  }

  private static Pair<MediaFormat, TrackEncryptionBox> parseMp4aFromParent(ParsableByteArray parent,
      int position, int size) {
    parent.setPosition(position + ATOM_HEADER_SIZE);
    // Start of the mp4a atom (defined in 14496-14)
    parent.skip(16);
    int channelCount = parent.readUnsignedShort();
    int sampleSize = parent.readUnsignedShort();
    parent.skip(4);
    int sampleRate = parent.readUnsignedFixedPoint1616();

    byte[] initializationData = null;
    TrackEncryptionBox trackEncryptionBox = null;
    int childPosition = parent.getPosition();
    while (childPosition - position < size) {
      parent.setPosition(childPosition);
      int childStartPosition = parent.getPosition();
      int childAtomSize = parent.readInt();
      int childAtomType = parent.readInt();
      if (childAtomType == Atom.TYPE_esds) {
        initializationData = parseEsdsFromParent(parent, childStartPosition);
        // TODO: Do we really need to do this? See [redacted]
        // Update sampleRate and sampleRate from the AudioSpecificConfig initialization data.
        Pair<Integer, Integer> audioSpecificConfig =
            CodecSpecificDataUtil.parseAudioSpecificConfig(initializationData);
        sampleRate = audioSpecificConfig.first;
        channelCount = audioSpecificConfig.second;
      } else if (childAtomType == Atom.TYPE_sinf) {
        trackEncryptionBox = parseSinfFromParent(parent, childStartPosition, childAtomSize);
      }
      childPosition += childAtomSize;
    }

    MediaFormat format = MediaFormat.createAudioFormat("audio/mp4a-latm", sampleSize, channelCount,
        sampleRate, Collections.singletonList(initializationData));
    return Pair.create(format, trackEncryptionBox);
  }

  private static List<byte[]> parseAvcCFromParent(ParsableByteArray parent, int position) {
    parent.setPosition(position + ATOM_HEADER_SIZE + 4);
    // Start of the AVCDecoderConfigurationRecord (defined in 14496-15)
    int nalUnitLength = (parent.readUnsignedByte() & 0x3) + 1;
    if (nalUnitLength != 4) {
      // readSample currently relies on a nalUnitLength of 4.
      // TODO: Consider handling the case where it isn't.
      throw new IllegalStateException();
    }
    List<byte[]> initializationData = new ArrayList<byte[]>();
    // TODO: We should try and parse these using CodecSpecificDataUtil.parseSpsNalUnit, and
    // expose the AVC profile and level somewhere useful; Most likely in MediaFormat.
    int numSequenceParameterSets = parent.readUnsignedByte() & 0x1F;
    for (int j = 0; j < numSequenceParameterSets; j++) {
      initializationData.add(parseChildNalUnit(parent));
    }
    int numPictureParamterSets = parent.readUnsignedByte();
    for (int j = 0; j < numPictureParamterSets; j++) {
      initializationData.add(parseChildNalUnit(parent));
    }
    return initializationData;
  }

  private static byte[] parseChildNalUnit(ParsableByteArray atom) {
    int length = atom.readUnsignedShort();
    int offset = atom.getPosition();
    atom.skip(length);
    return CodecSpecificDataUtil.buildNalUnit(atom.getData(), offset, length);
  }

  private static TrackEncryptionBox parseSinfFromParent(ParsableByteArray parent, int position,
      int size) {
    int childPosition = position + ATOM_HEADER_SIZE;

    TrackEncryptionBox trackEncryptionBox = null;
    while (childPosition - position < size) {
      parent.setPosition(childPosition);
      int childAtomSize = parent.readInt();
      int childAtomType = parent.readInt();
      if (childAtomType == Atom.TYPE_frma) {
        parent.readInt(); // dataFormat. Expect TYPE_avc1 (video) or TYPE_mp4a (audio).
      } else if (childAtomType == Atom.TYPE_schm) {
        parent.skip(4);
        parent.readInt(); // schemeType. Expect cenc
        parent.readInt(); // schemeVersion. Expect 0x00010000
      } else if (childAtomType == Atom.TYPE_schi) {
        trackEncryptionBox = parseSchiFromParent(parent, childPosition, childAtomSize);
      }
      childPosition += childAtomSize;
    }

    return trackEncryptionBox;
  }

  private static TrackEncryptionBox parseSchiFromParent(ParsableByteArray parent, int position,
      int size) {
    int childPosition = position + ATOM_HEADER_SIZE;
    while (childPosition - position < size) {
      parent.setPosition(childPosition);
      int childAtomSize = parent.readInt();
      int childAtomType = parent.readInt();
      if (childAtomType == Atom.TYPE_tenc) {
        parent.skip(4);
        int firstInt = parent.readInt();
        boolean defaultIsEncrypted = (firstInt >> 8) == 1;
        int defaultInitVectorSize = firstInt & 0xFF;
        byte[] defaultKeyId = new byte[16];
        parent.readBytes(defaultKeyId, 0, defaultKeyId.length);
        return new TrackEncryptionBox(defaultIsEncrypted, defaultInitVectorSize, defaultKeyId);
      }
      childPosition += childAtomSize;
    }
    return null;
  }

  private static byte[] parseEsdsFromParent(ParsableByteArray parent, int position) {
    parent.setPosition(position + ATOM_HEADER_SIZE + 4);
    // Start of the ES_Descriptor (defined in 14496-1)
    parent.skip(1); // ES_Descriptor tag
    int varIntByte = parent.readUnsignedByte();
    while (varIntByte > 127) {
      varIntByte = parent.readUnsignedByte();
    }
    parent.skip(2); // ES_ID

    int flags = parent.readUnsignedByte();
    if ((flags & 0x80 /* streamDependenceFlag */) != 0) {
      parent.skip(2);
    }
    if ((flags & 0x40 /* URL_Flag */) != 0) {
      parent.skip(parent.readUnsignedShort());
    }
    if ((flags & 0x20 /* OCRstreamFlag */) != 0) {
      parent.skip(2);
    }

    // Start of the DecoderConfigDescriptor (defined in 14496-1)
    parent.skip(1); // DecoderConfigDescriptor tag
    varIntByte = parent.readUnsignedByte();
    while (varIntByte > 127) {
      varIntByte = parent.readUnsignedByte();
    }
    parent.skip(13);

    // Start of AudioSpecificConfig (defined in 14496-3)
    parent.skip(1);  // AudioSpecificConfig tag
    varIntByte = parent.readUnsignedByte();
    int varInt = varIntByte & 0x7F;
    while (varIntByte > 127) {
      varIntByte = parent.readUnsignedByte();
      varInt = varInt << 8;
      varInt |= varIntByte & 0x7F;
    }
    byte[] initializationData = new byte[varInt];
    parent.readBytes(initializationData, 0, varInt);
    return initializationData;
  }

  private static void parseMoof(Track track, DefaultSampleValues extendsDefaults,
      ContainerAtom moof, TrackFragment out, boolean enableSmoothStreamingWorkarounds) {
    // TODO: Consider checking that the sequence number returned by parseMfhd is as expected.
    parseMfhd(moof.getLeafAtomOfType(Atom.TYPE_mfhd).getData());
    parseTraf(track, extendsDefaults, moof.getContainerAtomOfType(Atom.TYPE_traf),
        out, enableSmoothStreamingWorkarounds);
  }

  /**
   * Parses an mfhd atom (defined in 14496-12).
   *
   * @param mfhd The mfhd atom to parse.
   * @return The sequence number of the fragment.
   */
  private static int parseMfhd(ParsableByteArray mfhd) {
    mfhd.setPosition(FULL_ATOM_HEADER_SIZE);
    return mfhd.readUnsignedIntToInt();
  }

  /**
   * Parses a traf atom (defined in 14496-12).
   */
  private static void parseTraf(Track track, DefaultSampleValues extendsDefaults,
      ContainerAtom traf, TrackFragment out, boolean enableSmoothStreamingWorkarounds) {
    LeafAtom saiz = traf.getLeafAtomOfType(Atom.TYPE_saiz);
    if (saiz != null) {
      parseSaiz(saiz.getData(), out);
    }
    LeafAtom tfdtAtom = traf.getLeafAtomOfType(Atom.TYPE_tfdt);
    long decodeTime = tfdtAtom == null ? 0
        : parseTfdt(traf.getLeafAtomOfType(Atom.TYPE_tfdt).getData());
    LeafAtom tfhd = traf.getLeafAtomOfType(Atom.TYPE_tfhd);
    DefaultSampleValues fragmentHeader = parseTfhd(extendsDefaults, tfhd.getData());
    out.setSampleDescriptionIndex(fragmentHeader.sampleDescriptionIndex);

    LeafAtom trun = traf.getLeafAtomOfType(Atom.TYPE_trun);
    parseTrun(track, fragmentHeader, decodeTime, enableSmoothStreamingWorkarounds, trun.getData(),
        out);
    LeafAtom uuid = traf.getLeafAtomOfType(Atom.TYPE_uuid);
    if (uuid != null) {
      parseUuid(uuid.getData(), out);
    }
  }

  private static void parseSaiz(ParsableByteArray saiz, TrackFragment out) {
    saiz.setPosition(ATOM_HEADER_SIZE);
    int fullAtom = saiz.readInt();
    int flags = parseFullAtomFlags(fullAtom);
    if ((flags & 0x01) == 1) {
      saiz.skip(8);
    }
    int defaultSampleInfoSize = saiz.readUnsignedByte();
    int sampleCount = saiz.readUnsignedIntToInt();
    int totalSize = 0;
    int[] sampleInfoSizes = new int[sampleCount];
    if (defaultSampleInfoSize == 0) {
      for (int i = 0; i < sampleCount; i++) {
        sampleInfoSizes[i] = saiz.readUnsignedByte();
        totalSize += sampleInfoSizes[i];
      }
    } else {
      for (int i = 0; i < sampleCount; i++) {
        sampleInfoSizes[i] = defaultSampleInfoSize;
        totalSize += defaultSampleInfoSize;
      }
    }
    out.setAuxiliarySampleInfoTables(totalSize, sampleInfoSizes);
  }

  /**
   * Parses a tfhd atom (defined in 14496-12).
   *
   * @param extendsDefaults Default sample values from the trex atom.
   * @return The parsed default sample values.
   */
  private static DefaultSampleValues parseTfhd(DefaultSampleValues extendsDefaults,
      ParsableByteArray tfhd) {
    tfhd.setPosition(ATOM_HEADER_SIZE);
    int fullAtom = tfhd.readInt();
    int flags = parseFullAtomFlags(fullAtom);

    tfhd.skip(4); // trackId
    if ((flags & 0x01 /* base_data_offset_present */) != 0) {
      tfhd.skip(8);
    }

    int defaultSampleDescriptionIndex =
        ((flags & 0x02 /* default_sample_description_index_present */) != 0) ?
        tfhd.readUnsignedIntToInt() - 1 : extendsDefaults.sampleDescriptionIndex;
    int defaultSampleDuration = ((flags & 0x08 /* default_sample_duration_present */) != 0) ?
        tfhd.readUnsignedIntToInt() : extendsDefaults.duration;
    int defaultSampleSize = ((flags & 0x10 /* default_sample_size_present */) != 0) ?
        tfhd.readUnsignedIntToInt() : extendsDefaults.size;
    int defaultSampleFlags = ((flags & 0x20 /* default_sample_flags_present */) != 0) ?
        tfhd.readUnsignedIntToInt() : extendsDefaults.flags;
    return new DefaultSampleValues(defaultSampleDescriptionIndex, defaultSampleDuration,
        defaultSampleSize, defaultSampleFlags);
  }

  /**
   * Parses a tfdt atom (defined in 14496-12).
   *
   * @return baseMediaDecodeTime. The sum of the decode durations of all earlier samples in the
   *     media, expressed in the media's timescale.
   */
  private static long parseTfdt(ParsableByteArray tfdt) {
    tfdt.setPosition(ATOM_HEADER_SIZE);
    int fullAtom = tfdt.readInt();
    int version = parseFullAtomVersion(fullAtom);
    return version == 1 ? tfdt.readUnsignedLongToLong() : tfdt.readUnsignedInt();
  }

  /**
   * Parses a trun atom (defined in 14496-12).
   *
   * @param track The corresponding track.
   * @param defaultSampleValues Default sample values.
   * @param decodeTime The decode time.
   * @param trun The trun atom to parse.
   * @param out The {@TrackFragment} into which parsed data should be placed.
   */
  private static void parseTrun(Track track, DefaultSampleValues defaultSampleValues,
      long decodeTime, boolean enableSmoothStreamingWorkarounds, ParsableByteArray trun,
      TrackFragment out) {
    trun.setPosition(ATOM_HEADER_SIZE);
    int fullAtom = trun.readInt();
    int version = parseFullAtomVersion(fullAtom);
    int flags = parseFullAtomFlags(fullAtom);

    int numberOfEntries = trun.readUnsignedIntToInt();
    if ((flags & 0x01 /* data_offset_present */) != 0) {
      trun.skip(4);
    }

    boolean firstSampleFlagsPresent = (flags & 0x04 /* first_sample_flags_present */) != 0;
    int firstSampleFlags = defaultSampleValues.flags;
    if (firstSampleFlagsPresent) {
      firstSampleFlags = trun.readUnsignedIntToInt();
    }

    boolean sampleDurationsPresent = (flags & 0x100 /* sample_duration_present */) != 0;
    boolean sampleSizesPresent = (flags & 0x200 /* sample_size_present */) != 0;
    boolean sampleFlagsPresent = (flags & 0x400 /* sample_flags_present */) != 0;
    boolean sampleCompositionTimeOffsetsPresent =
        (flags & 0x800 /* sample_composition_time_offsets_present */) != 0;

    int[] sampleSizeTable = new int[numberOfEntries];
    int[] sampleDecodingTimeTable = new int[numberOfEntries];
    int[] sampleCompositionTimeOffsetTable = new int[numberOfEntries];
    boolean[] sampleIsSyncFrameTable = new boolean[numberOfEntries];

    long timescale = track.timescale;
    long cumulativeTime = decodeTime;
    for (int i = 0; i < numberOfEntries; i++) {
      // Use trun values if present, otherwise tfhd, otherwise trex.
      int sampleDuration = sampleDurationsPresent ? trun.readUnsignedIntToInt()
          : defaultSampleValues.duration;
      int sampleSize = sampleSizesPresent ? trun.readUnsignedIntToInt() : defaultSampleValues.size;
      int sampleFlags = (i == 0 && firstSampleFlagsPresent) ? firstSampleFlags
          : sampleFlagsPresent ? trun.readInt() : defaultSampleValues.flags;
      if (sampleCompositionTimeOffsetsPresent) {
        // Fragmented mp4 streams packaged for smooth streaming violate the BMFF spec by specifying
        // the sample offset as a signed integer in conjunction with a box version of 0.
        int sampleOffset;
        if (version == 0 && !enableSmoothStreamingWorkarounds) {
          sampleOffset = trun.readUnsignedIntToInt();
        } else {
          sampleOffset = trun.readInt();
        }
        sampleCompositionTimeOffsetTable[i] = (int) ((sampleOffset * 1000) / timescale);
      }
      sampleDecodingTimeTable[i] = (int) ((cumulativeTime * 1000) / timescale);
      sampleSizeTable[i] = sampleSize;
      boolean isSync = ((sampleFlags >> 16) & 0x1) == 0;
      if (track.type == Track.TYPE_VIDEO && enableSmoothStreamingWorkarounds && i != 0) {
        // Fragmented mp4 streams packaged for smooth streaming violate the BMFF spec by indicating
        // that every sample is a sync frame, when this is not actually the case.
        isSync = false;
      }
      if (isSync) {
        sampleIsSyncFrameTable[i] = true;
      }
      cumulativeTime += sampleDuration;
    }

    out.setSampleTables(sampleSizeTable, sampleDecodingTimeTable, sampleCompositionTimeOffsetTable,
        sampleIsSyncFrameTable);
  }

  private static void parseUuid(ParsableByteArray uuid, TrackFragment out) {
    uuid.setPosition(ATOM_HEADER_SIZE);
    byte[] extendedType = new byte[16];
    uuid.readBytes(extendedType, 0, 16);

    // Currently this parser only supports Microsoft's PIFF SampleEncryptionBox.
    if (!Arrays.equals(extendedType, PIFF_SAMPLE_ENCRYPTION_BOX_EXTENDED_TYPE)) {
      return;
    }

    // See "Portable encoding of audio-video objects: The Protected Interoperable File Format
    // (PIFF), John A. Bocharov et al, Section 5.3.2.1."
    int fullAtom = uuid.readInt();
    int flags = parseFullAtomFlags(fullAtom);

    if ((flags & 0x01 /* override_track_encryption_box_parameters */) != 0) {
      // TODO: Implement this.
      throw new IllegalStateException("Overriding TrackEncryptionBox parameters is unsupported");
    }

    boolean subsampleEncryption = (flags & 0x02 /* use_subsample_encryption */) != 0;
    int numberOfEntries = uuid.readUnsignedIntToInt();
    if (numberOfEntries != out.length) {
      throw new IllegalStateException("Length mismatch: " + numberOfEntries + ", " + out.length);
    }

    int sampleEncryptionDataLength = uuid.length() - uuid.getPosition();
    ParsableByteArray sampleEncryptionData = new ParsableByteArray(sampleEncryptionDataLength);
    uuid.readBytes(sampleEncryptionData.getData(), 0, sampleEncryptionData.length());
    out.setSmoothStreamingSampleEncryptionData(sampleEncryptionData, subsampleEncryption);
  }

  /**
   * Parses a sidx atom (defined in 14496-12).
   */
  private static SegmentIndex parseSidx(ParsableByteArray atom) {
    atom.setPosition(ATOM_HEADER_SIZE);
    int fullAtom = atom.readInt();
    int version = parseFullAtomVersion(fullAtom);

    atom.skip(4);
    long timescale = atom.readUnsignedInt();
    long earliestPresentationTime;
    long firstOffset;
    if (version == 0) {
      earliestPresentationTime = atom.readUnsignedInt();
      firstOffset = atom.readUnsignedInt();
    } else {
      earliestPresentationTime = atom.readUnsignedLongToLong();
      firstOffset = atom.readUnsignedLongToLong();
    }

    atom.skip(2);

    int referenceCount = atom.readUnsignedShort();
    int[] sizes = new int[referenceCount];
    long[] offsets = new long[referenceCount];
    long[] durationsUs = new long[referenceCount];
    long[] timesUs = new long[referenceCount];

    long offset = firstOffset;
    long time = earliestPresentationTime;
    for (int i = 0; i < referenceCount; i++) {
      int firstInt = atom.readInt();

      int type = 0x80000000 & firstInt;
      if (type != 0) {
        throw new IllegalStateException("Unhandled indirect reference");
      }
      long referenceDuration = atom.readUnsignedInt();

      sizes[i] = 0x7fffffff & firstInt;
      offsets[i] = offset;

      // Calculate time and duration values such that any rounding errors are consistent. i.e. That
      // timesUs[i] + durationsUs[i] == timesUs[i + 1].
      timesUs[i] = (time * 1000000L) / timescale;
      long nextTimeUs = ((time + referenceDuration) * 1000000L) / timescale;
      durationsUs[i] = nextTimeUs - timesUs[i];
      time += referenceDuration;

      atom.skip(4);
      offset += sizes[i];
    }

    return new SegmentIndex(atom.length(), sizes, offsets, durationsUs, timesUs);
  }

  private int readCencAuxiliaryData(NonBlockingInputStream inputStream) {
    int length = cencAuxiliaryData.length();
    int bytesRead = inputStream.read(cencAuxiliaryData.getData(), cencAuxiliaryBytesRead,
        length - cencAuxiliaryBytesRead);
    if (bytesRead == -1) {
      return RESULT_END_OF_STREAM;
    }
    cencAuxiliaryBytesRead += bytesRead;
    if (cencAuxiliaryBytesRead < length) {
      return RESULT_NEED_MORE_DATA;
    }
    enterState(STATE_READING_SAMPLE_START);
    return 0;
  }

  /**
   * Attempts to read or skip the next sample in the current mdat atom.
   * <p>
   * If there are no more samples in the current mdat atom then the parser state is transitioned
   * to {@link #STATE_READING_ATOM_HEADER} and 0 is returned.
   * <p>
   * If there's a pending seek to a sync frame, and if the next sample is before that frame, then
   * the sample is skipped. Otherwise it is read.
   * <p>
   * It is possible for a sample to be read or skipped in part if there is insufficent data
   * available from the {@link NonBlockingInputStream}. In this case the remainder of the sample
   * can be read in a subsequent call passing the same {@link SampleHolder}.
   *
   * @param inputStream The stream from which to read the sample.
   * @param out The holder into which to write the sample.
   * @return A combination of RESULT_* flags indicating the result of the call.
   */
  private int readOrSkipSample(NonBlockingInputStream inputStream, SampleHolder out) {
    if (sampleIndex >= fragmentRun.length) {
      // We've run out of samples in the current mdat atom.
      enterState(STATE_READING_ATOM_HEADER);
      return 0;
    }
    if (sampleIndex < pendingSeekSyncSampleIndex) {
      return skipSample(inputStream);
    }
    return readSample(inputStream, out);
  }

  private int skipSample(NonBlockingInputStream inputStream) {
    if (parserState == STATE_READING_SAMPLE_START) {
      ParsableByteArray sampleEncryptionData = cencAuxiliaryData != null ? cencAuxiliaryData
          : fragmentRun.smoothStreamingSampleEncryptionData;
      if (sampleEncryptionData != null) {
        TrackEncryptionBox encryptionBox =
            track.sampleDescriptionEncryptionBoxes[fragmentRun.sampleDescriptionIndex];
        int vectorSize = encryptionBox.initializationVectorSize;
        boolean subsampleEncryption = cencAuxiliaryData != null
            ? fragmentRun.auxiliarySampleInfoSizeTable[sampleIndex] > vectorSize
                : fragmentRun.smoothStreamingUsesSubsampleEncryption;
        sampleEncryptionData.skip(vectorSize);
        int subsampleCount = subsampleEncryption ? sampleEncryptionData.readUnsignedShort() : 1;
        if (subsampleEncryption) {
          sampleEncryptionData.skip((2 + 4) * subsampleCount);
        }
      }
    }

    int sampleSize = fragmentRun.sampleSizeTable[sampleIndex];
    int bytesRead = inputStream.skip(sampleSize - sampleBytesRead);
    if (bytesRead == -1) {
      return RESULT_END_OF_STREAM;
    }
    sampleBytesRead += bytesRead;
    if (sampleSize != sampleBytesRead) {
      enterState(STATE_READING_SAMPLE_INCREMENTAL);
      return RESULT_NEED_MORE_DATA;
    }
    sampleIndex++;
    enterState(STATE_READING_SAMPLE_START);
    return 0;
  }

  @SuppressLint("InlinedApi")
  private int readSample(NonBlockingInputStream inputStream, SampleHolder out) {
    int sampleSize = fragmentRun.sampleSizeTable[sampleIndex];
    ByteBuffer outputData = out.data;
    if (parserState == STATE_READING_SAMPLE_START) {
      out.timeUs = fragmentRun.getSamplePresentationTime(sampleIndex) * 1000L;
      out.flags = 0;
      if (fragmentRun.sampleIsSyncFrameTable[sampleIndex]) {
        out.flags |= MediaExtractor.SAMPLE_FLAG_SYNC;
        lastSyncSampleIndex = sampleIndex;
      }
      if (out.allowDataBufferReplacement
          && (out.data == null || out.data.capacity() < sampleSize)) {
        outputData = ByteBuffer.allocate(sampleSize);
        out.data = outputData;
      }
      ParsableByteArray sampleEncryptionData = cencAuxiliaryData != null ? cencAuxiliaryData
          : fragmentRun.smoothStreamingSampleEncryptionData;
      if (sampleEncryptionData != null) {
        readSampleEncryptionData(sampleEncryptionData, out);
      }
    }

    int bytesRead;
    if (outputData == null) {
      bytesRead = inputStream.skip(sampleSize - sampleBytesRead);
    } else {
      bytesRead = inputStream.read(outputData, sampleSize - sampleBytesRead);
    }
    if (bytesRead == -1) {
      return RESULT_END_OF_STREAM;
    }
    sampleBytesRead += bytesRead;

    if (sampleSize != sampleBytesRead) {
      enterState(STATE_READING_SAMPLE_INCREMENTAL);
      return RESULT_NEED_MORE_DATA | RESULT_READ_SAMPLE_PARTIAL;
    }

    if (outputData != null) {
      if (track.type == Track.TYPE_VIDEO) {
        // The mp4 file contains length-prefixed NAL units, but the decoder wants start code
        // delimited content. Replace length prefixes with start codes.
        int sampleOffset = outputData.position() - sampleSize;
        int position = sampleOffset;
        while (position < sampleOffset + sampleSize) {
          outputData.position(position);
          int length = readUnsignedIntToInt(outputData);
          outputData.position(position);
          outputData.put(NAL_START_CODE);
          position += length + 4;
        }
        outputData.position(sampleOffset + sampleSize);
      }
      out.size = sampleSize;
    } else {
      out.size = 0;
    }

    sampleIndex++;
    enterState(STATE_READING_SAMPLE_START);
    return RESULT_READ_SAMPLE_FULL;
  }

  @SuppressLint("InlinedApi")
  private void readSampleEncryptionData(ParsableByteArray sampleEncryptionData, SampleHolder out) {
    TrackEncryptionBox encryptionBox =
        track.sampleDescriptionEncryptionBoxes[fragmentRun.sampleDescriptionIndex];
    byte[] keyId = encryptionBox.keyId;
    boolean isEncrypted = encryptionBox.isEncrypted;
    int vectorSize = encryptionBox.initializationVectorSize;
    boolean subsampleEncryption = cencAuxiliaryData != null
        ? fragmentRun.auxiliarySampleInfoSizeTable[sampleIndex] > vectorSize
            : fragmentRun.smoothStreamingUsesSubsampleEncryption;

    byte[] vector = out.cryptoInfo.iv;
    if (vector == null || vector.length != 16) {
      vector = new byte[16];
    }
    sampleEncryptionData.readBytes(vector, 0, vectorSize);

    int subsampleCount = subsampleEncryption ? sampleEncryptionData.readUnsignedShort() : 1;
    int[] clearDataSizes = out.cryptoInfo.numBytesOfClearData;
    if (clearDataSizes == null || clearDataSizes.length < subsampleCount) {
      clearDataSizes = new int[subsampleCount];
    }
    int[] encryptedDataSizes = out.cryptoInfo.numBytesOfEncryptedData;
    if (encryptedDataSizes == null || encryptedDataSizes.length < subsampleCount) {
      encryptedDataSizes = new int[subsampleCount];
    }
    if (subsampleEncryption) {
      for (int i = 0; i < subsampleCount; i++) {
        clearDataSizes[i] = sampleEncryptionData.readUnsignedShort();
        encryptedDataSizes[i] = sampleEncryptionData.readUnsignedIntToInt();
      }
    } else {
      clearDataSizes[0] = 0;
      encryptedDataSizes[0] = fragmentRun.sampleSizeTable[sampleIndex];
    }
    out.cryptoInfo.set(subsampleCount, clearDataSizes, encryptedDataSizes, keyId, vector,
        isEncrypted ? MediaCodec.CRYPTO_MODE_AES_CTR : MediaCodec.CRYPTO_MODE_UNENCRYPTED);
    if (isEncrypted) {
      out.flags |= MediaExtractor.SAMPLE_FLAG_ENCRYPTED;
    }
  }

  /**
   * Parses the version number out of the additional integer component of a full atom.
   */
  private static int parseFullAtomVersion(int fullAtomInt) {
    return 0x000000FF & (fullAtomInt >> 24);
  }

  /**
   * Parses the atom flags out of the additional integer component of a full atom.
   */
  private static int parseFullAtomFlags(int fullAtomInt) {
    return 0x00FFFFFF & fullAtomInt;
  }

  /**
   * Reads an unsigned integer into an integer. This method is suitable for use when it can be
   * assumed that the top bit will always be set to zero.
   *
   * @throws IllegalArgumentException If the top bit of the input data is set.
   */
  private static int readUnsignedIntToInt(ByteBuffer data) {
    int result = 0xFF & data.get();
    for (int i = 1; i < 4; i++) {
      result <<= 8;
      result |= 0xFF & data.get();
    }
    if (result < 0) {
      throw new IllegalArgumentException("Top bit not zero: " + result);
    }
    return result;
  }

}

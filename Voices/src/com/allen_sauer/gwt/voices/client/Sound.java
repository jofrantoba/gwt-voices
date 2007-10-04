/*
 * Copyright 2007 Fred Sauer
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.allen_sauer.gwt.voices.client;

import com.allen_sauer.gwt.voices.client.handler.FiresSoundEvents;

public interface Sound extends FiresSoundEvents {
  public static final int LOAD_STATE_LOADED = 6;
  public static final int LOAD_STATE_SUPPORTED = 4;
  public static final int LOAD_STATE_SUPPORTED_NOT_LOADED = 5;
  public static final int LOAD_STATE_UNINITIALIZED = 1;
  public static final int LOAD_STATE_UNKNOWN = 2;
  public static final int LOAD_STATE_UNSUPPORTED = 3;

  /**
   * IANA assigned media type <code>audio/basic</code> for RFC 2045/2046.
   * Typical filename extensions include <code>.au</code> and
   * <code>.snd</code>.
   */
  public static final String MIME_TYPE_AUDIO_BASIC = "audio/basic";

  /**
   * IANA assigned media type <code>audio/mpeg</code> for RFC 3003. Typical
   * filename extensions include <code>.mp1</code>, <code>.mp2</code> and
   * <code>.mp3</code>.
   */
  public static final String MIME_TYPE_AUDIO_MPEG = "audio/mpeg";

  /**
   * Using <code>audio/x-aiff</code> instead of the more popular, but
   * unregistered, <code>audio/aiff</code>. Typical filename extension is
   * <code>.aif</code>.
   */
  public static final String MIME_TYPE_AUDIO_X_AIFF = "audio/x-aiff";

  /**
   * Using <code>audio/x-midi</code> instead of the more popular, but
   * unregistered, <code>audio/midi</code>. Typical filename extensions
   * include <code>.mid</code> and <code>.midi</code>.
   */
  public static final String MIME_TYPE_AUDIO_X_MIDI = "audio/x-midi";

  /**
   * Using <code>audio/x-wav</code> instead of the more popular, but
   * unregistered, <code>audio/wav</code>. Typical filename extension is
   * <code>.wav</code>.
   */
  public static final String MIME_TYPE_AUDIO_X_WAV = "audio/x-wav";

  public int getLoadState();

  public String getMimeType();

  public String getSoundType();

  public String getUrl();

  public void play();

  public void setBalance(int balance);

  public void setVolume(int volume);

  public void stop();
}
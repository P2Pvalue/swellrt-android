/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.swellrt.android.service.wave;

import org.waveprotocol.wave.model.conversation.ConversationBlip;

/**
 * Manages the collection of blip documents for all conversations in a wave,
 * providing access to blip documents.
 *
 * @param <D> the document type for blip documents
 */
public interface DocumentRegistry<D> {
  /**
   * Gets the document implementation for a blip.
   *
   * @param blip blip
   * @return document implementation for {@code blip}.
   */
  D get(ConversationBlip blip);
}

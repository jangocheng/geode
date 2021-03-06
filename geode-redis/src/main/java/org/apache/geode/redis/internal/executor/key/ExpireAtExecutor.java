/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package org.apache.geode.redis.internal.executor.key;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.geode.redis.internal.RedisConstants.ERROR_NOT_INTEGER;

import java.util.List;

import org.apache.geode.redis.internal.RedisConstants.ArityDef;
import org.apache.geode.redis.internal.data.ByteArrayWrapper;
import org.apache.geode.redis.internal.executor.AbstractExecutor;
import org.apache.geode.redis.internal.executor.Extendable;
import org.apache.geode.redis.internal.netty.Coder;
import org.apache.geode.redis.internal.netty.Command;
import org.apache.geode.redis.internal.netty.ExecutionHandlerContext;

public class ExpireAtExecutor extends AbstractExecutor implements Extendable {

  @Override
  public void executeCommand(Command command, ExecutionHandlerContext context) {
    List<byte[]> commandElems = command.getProcessedCommand();
    int TIMESTAMP_INDEX = 2;

    if (commandElems.size() != 3) {
      command.setResponse(
          Coder.getErrorResponse(
              context.getByteBufAllocator(),
              getArgsError()));
      return;
    }
    ByteArrayWrapper wKey = command.getKey();

    byte[] timestampByteArray = commandElems.get(TIMESTAMP_INDEX);
    long timestamp;
    try {
      timestamp = Coder.bytesToLong(timestampByteArray);
    } catch (NumberFormatException e) {
      command.setResponse(
          Coder.getErrorResponse(context.getByteBufAllocator(), ERROR_NOT_INTEGER));
      return;
    }

    if (!timeUnitMillis()) {
      timestamp = SECONDS.toMillis(timestamp);
    }

    RedisKeyCommands redisKeyCommands = getRedisKeyCommands(context);
    int result = redisKeyCommands.pexpireat(wKey, timestamp);
    command.setResponse(
        Coder.getIntegerResponse(
            context.getByteBufAllocator(),
            result));
  }

  protected boolean timeUnitMillis() {
    return false;
  }

  @Override
  public String getArgsError() {
    return ArityDef.EXPIREAT;
  }

}
